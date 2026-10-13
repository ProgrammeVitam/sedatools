package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListInterface;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.SchemaMetadataMapBuilder;
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
 * Checks that the map {@link SchemaMetadataMapBuilder} builds from the XSD is the hand written
 * one: same Java class and same cardinality for every element the schema declares.
 * <p>
 * This is the safety net of the migration. While it is green, replacing the maps by the builder
 * changes nothing that sedalib or ReSIP can observe; the day the maps are gone, this test goes
 * with them. The elements the builder leaves out on purpose are those of
 * {@link MetadataMapXsdConformityTest#DESIGN_CHOICES}: sedalib holds them elsewhere, or they are
 * outside of the schema.
 */
class SchemaMetadataMapBuilderTest {

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void buildsTheSameMapsAsTheHandWrittenOnes(SedaVersion version) throws Exception {
        List<String> differences = new ArrayList<>();

        for (Class<?> clazz : MetadataMapXsdConformityTest.CLASS_TO_XSD_TYPE.keySet()) {
            Map<String, ComplexListMetadataKind> handWritten = ComplexListInterface.metadataDefinitions(clazz).get(
                version
            );
            LinkedHashMap<String, ComplexListMetadataKind> built = SchemaMetadataMapBuilder.build(clazz, version);
            if (handWritten == null || built == null) continue;

            Set<String> ignored = new LinkedHashSet<>(
                MetadataMapXsdConformityTest.DESIGN_CHOICES.getOrDefault(clazz, Set.of())
            );
            ignored.addAll(
                MetadataMapXsdConformityTest.VERSION_DESIGN_CHOICES.getOrDefault(version, Map.of()).getOrDefault(
                    clazz,
                    Set.of()
                )
            );

            for (Map.Entry<String, ComplexListMetadataKind> e : built.entrySet()) {
                if (ignored.contains(e.getKey())) continue;
                String label = clazz.getSimpleName() + "." + e.getKey();
                ComplexListMetadataKind expected = handWritten.get(e.getKey());
                if (expected == null) {
                    differences.add(label + " : construit mais absent de la classe");
                    continue;
                }
                if (!expected.getMetadataClass().equals(e.getValue().getMetadataClass())) differences.add(
                    label +
                    " : classe " +
                    expected.getMetadataClass().getSimpleName() +
                    ", construite " +
                    e.getValue().getMetadataClass().getSimpleName()
                );
                if (expected.isMany() != e.getValue().isMany()) differences.add(
                    label + " : cardinalité " + expected.isMany() + ", construite " + e.getValue().isMany()
                );
            }
        }

        assertThat(differences)
            .as(
                "Écarts entre les metadataMap écrites et celles construites depuis la XSD " +
                version +
                "\n" +
                String.join("\n", differences)
            )
            .isEmpty();
    }
}
