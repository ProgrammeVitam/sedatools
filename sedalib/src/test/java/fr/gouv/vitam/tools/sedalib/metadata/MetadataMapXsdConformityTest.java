package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListInterface;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.SchemaMetadataMapBuilder;
import fr.gouv.vitam.tools.sedaschema.SedaFlavor;
import fr.gouv.vitam.tools.sedaschema.SedaSchema;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.Particle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compares the hand written metadataMap of each ComplexListType class with the official SEDA XSD
 * read by seda-schema: same child elements, same order, same cardinality.
 * <p>
 * The test fails on any unlisted difference, so that the maps can't drift from the standard
 * silently. Abstract elements (extension points) are ignored. Differences that are design choices
 * of sedalib are listed in {@link #DESIGN_CHOICES}, the differences found when the test was
 * written and still to be fixed in {@link #KNOWN_DRIFT}: remove an entry there once the class is
 * fixed.
 */
class MetadataMapXsdConformityTest {

    /**
     * Each class with a metadataMap and the XSD complex type it implements, as the builder that
     * will replace the maps knows them.
     */
    static final Map<Class<?>, String> CLASS_TO_XSD_TYPE = SchemaMetadataMapBuilder.getXsdTypes();

    /**
     * Elements handled differently on purpose, by class: they are left out of the comparison.
     */
    static final Map<Class<?>, Set<String>> DESIGN_CHOICES = new LinkedHashMap<>();

    static {
        // RuleType carries StartDate inside its Rule metadata (see RuleType and RuleMetadataKind)
        for (Class<?> c : CLASS_TO_XSD_TYPE.keySet()) if (RuleType.class.isAssignableFrom(c)) DESIGN_CHOICES.put(
            c,
            Set.of("StartDate")
        );
        // DocumentContainer and DocumentPack are the Vitam compaction extension, outside of the
        // standard; OriginatingSystemIdReplyTo and TextContent are sedalib additions in 2.1, where
        // neither the official nor the Vitam XSD declares them, before they entered the 2.2 standard
        DESIGN_CHOICES.put(
            Content.class,
            Set.of("DocumentContainer", "DocumentPack", "OriginatingSystemIdReplyTo", "TextContent")
        );
        // the binary content is kept as a file and referenced by Uri, never inlined in the manifest
        DESIGN_CHOICES.put(BinaryDataObject.class, Set.of("Attachment"));
    }

    /**
     * Same, but only for one version.
     */
    static final Map<SedaVersion, Map<Class<?>, Set<String>>> VERSION_DESIGN_CHOICES = new LinkedHashMap<>();

    static {
        // UpdateOperation left the XSD in 2.3, sedalib keeps it as the Vitam extension it was in
        // 2.1 and 2.2, where it is still checked against the schema
        VERSION_DESIGN_CHOICES.put(
            SedaVersion.V2_3,
            Map.of(Management.class, Set.of("UpdateOperation"), ManagementMetadata.class, Set.of("UpdateOperation"))
        );
    }

    /**
     * Elements whose presence or cardinality differs from the XSD, to be fixed: left out of the
     * comparison until then. Empty: the maps match the schema.
     */
    private static final Map<Class<?>, Set<String>> KNOWN_DRIFT = new LinkedHashMap<>();

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void metadataMapsMatchTheXsd(SedaVersion version) throws Exception {
        // sedalib implements the SEDA Vitam actually uses, not the bare standard
        SedaSchema schema = SedaSchema.of(
            fr.gouv.vitam.tools.sedaschema.SedaVersion.valueOf(version.name()),
            SedaFlavor.VITAM
        );
        List<String> differences = new ArrayList<>();

        for (Map.Entry<Class<?>, String> e : CLASS_TO_XSD_TYPE.entrySet()) {
            Map<String, ComplexListMetadataKind> map = ComplexListInterface.metadataDefinitions(e.getKey()).get(
                version
            );
            ComplexTypeDef type = schema.getComplexType(e.getValue());
            // a class without map for this version, or whose type left the standard, is simply unused in it
            if (map == null || type == null) continue;
            Set<String> ignored = new LinkedHashSet<>(DESIGN_CHOICES.getOrDefault(e.getKey(), Set.of()));
            ignored.addAll(KNOWN_DRIFT.getOrDefault(e.getKey(), Set.of()));
            ignored.addAll(VERSION_DESIGN_CHOICES.getOrDefault(version, Map.of()).getOrDefault(e.getKey(), Set.of()));
            compare(e.getKey().getSimpleName() + " / " + e.getValue(), map, type, ignored, differences);
        }

        assertThat(differences)
            .as("Écarts entre les metadataMap et la XSD SEDA " + version + "\n" + String.join("\n", differences))
            .isEmpty();
    }

    private static void compare(
        String label,
        Map<String, ComplexListMetadataKind> map,
        ComplexTypeDef type,
        Set<String> ignored,
        List<String> differences
    ) {
        Map<String, Boolean> xsdMany = new LinkedHashMap<>();
        collectCardinalities(type.getContent(), false, xsdMany);
        xsdMany.keySet().removeAll(ignored);

        Set<String> missing = new LinkedHashSet<>(xsdMany.keySet());
        missing.removeAll(map.keySet());
        Set<String> extra = new LinkedHashSet<>(map.keySet());
        extra.removeAll(xsdMany.keySet());
        extra.removeAll(ignored);
        if (!missing.isEmpty()) differences.add(label + " : dans la XSD mais pas dans la classe " + missing);
        if (!extra.isEmpty()) differences.add(label + " : dans la classe mais pas dans la XSD " + extra);

        List<String> commonXsdOrder = new ArrayList<>();
        List<String> commonClassOrder = new ArrayList<>();
        for (String name : xsdMany.keySet()) if (map.containsKey(name)) commonXsdOrder.add(name);
        for (String name : map.keySet()) if (xsdMany.containsKey(name)) commonClassOrder.add(name);
        if (!commonXsdOrder.equals(commonClassOrder)) differences.add(
            label + " : ordre différent\n      XSD    " + commonXsdOrder + "\n      classe " + commonClassOrder
        );

        for (String name : commonXsdOrder) {
            boolean classMany = map.get(name).isMany();
            if (classMany != xsdMany.get(name)) differences.add(
                label +
                "/" +
                name +
                " : cardinalité " +
                (xsdMany.get(name) ? "multiple" : "unique") +
                " dans la XSD, " +
                (classMany ? "multiple" : "unique") +
                " dans la classe"
            );
        }
    }

    /**
     * Collects each concrete child element with whether it can occur more than once, an element
     * inside a repeatable group being repeatable. Abstract elements are extension points, not
     * metadata to map.
     */
    private static void collectCardinalities(Particle p, boolean inRepeatableGroup, Map<String, Boolean> out) {
        if (p == null) return;
        boolean repeatable = inRepeatableGroup || p.getOccurrence().isRepeatable();
        if (p.isElement()) {
            ElementDef def = p.getElement();
            if (def.isAbstract()) return;
            out.merge(def.getName(), repeatable, (a, b) -> a || b);
        } else if (p.isGroup()) {
            for (Particle c : p.getGroup().getParticles()) collectCardinalities(c, repeatable, out);
        }
    }
}
