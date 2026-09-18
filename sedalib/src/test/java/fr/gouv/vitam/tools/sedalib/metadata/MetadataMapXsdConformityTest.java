package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.content.AdditionalProof;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.Coverage;
import fr.gouv.vitam.tools.sedalib.metadata.content.CustodialHistory;
import fr.gouv.vitam.tools.sedalib.metadata.content.DataObjectReference;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.content.Gps;
import fr.gouv.vitam.tools.sedalib.metadata.content.Keyword;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.content.ReferencedObject;
import fr.gouv.vitam.tools.sedalib.metadata.content.RelatedObjectReference;
import fr.gouv.vitam.tools.sedalib.metadata.content.Signature;
import fr.gouv.vitam.tools.sedalib.metadata.content.SignatureDescription;
import fr.gouv.vitam.tools.sedalib.metadata.content.Signer;
import fr.gouv.vitam.tools.sedalib.metadata.content.SigningInformation;
import fr.gouv.vitam.tools.sedalib.metadata.content.TimestampingInformation;
import fr.gouv.vitam.tools.sedalib.metadata.content.Validator;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.Metadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.management.AccessRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.ArchiveUnitIdentifierKey;
import fr.gouv.vitam.tools.sedalib.metadata.management.ClassificationRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.DisseminationRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.HoldRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.LogBook;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.management.ReuseRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.StorageRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.UpdateOperation;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgencyType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListInterface;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DataObjectOrArchiveUnitReferenceType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.LinkingAgentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.PlaceType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
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
 * written and still to be fixed in {@link #KNOWN_DRIFT} and {@link #KNOWN_ORDER_DRIFT}: remove an
 * entry there once the class is fixed.
 */
class MetadataMapXsdConformityTest {

    /**
     * Each class with a metadataMap and the XSD complex type it implements.
     */
    private static final Map<Class<?>, String> CLASS_TO_XSD_TYPE = new LinkedHashMap<>();

    static {
        CLASS_TO_XSD_TYPE.put(Content.class, "DescriptiveMetadataContentType");
        CLASS_TO_XSD_TYPE.put(Management.class, "ManagementType");
        CLASS_TO_XSD_TYPE.put(AccessRule.class, "AccessRuleType");
        CLASS_TO_XSD_TYPE.put(AppraisalRule.class, "AppraisalRuleType");
        CLASS_TO_XSD_TYPE.put(ClassificationRule.class, "ClassificationRuleType");
        CLASS_TO_XSD_TYPE.put(DisseminationRule.class, "DisseminationRuleType");
        CLASS_TO_XSD_TYPE.put(HoldRule.class, "HoldRuleType");
        CLASS_TO_XSD_TYPE.put(ReuseRule.class, "ReuseRuleType");
        CLASS_TO_XSD_TYPE.put(StorageRule.class, "StorageRuleType");
        CLASS_TO_XSD_TYPE.put(LogBook.class, "LogBookType");
        CLASS_TO_XSD_TYPE.put(UpdateOperation.class, "UpdateOperationType");
        CLASS_TO_XSD_TYPE.put(ArchiveUnitIdentifierKey.class, "ArchiveUnitIdentifierKeyType");
        CLASS_TO_XSD_TYPE.put(Event.class, "EventType");
        CLASS_TO_XSD_TYPE.put(CustodialHistory.class, "CustodialHistoryType");
        CLASS_TO_XSD_TYPE.put(Keyword.class, "KeywordsType");
        CLASS_TO_XSD_TYPE.put(Coverage.class, "CoverageType");
        CLASS_TO_XSD_TYPE.put(Gps.class, "GpsType");
        CLASS_TO_XSD_TYPE.put(RelatedObjectReference.class, "RelatedObjectReferenceType");
        CLASS_TO_XSD_TYPE.put(DataObjectReference.class, "DataObjectRefType");
        CLASS_TO_XSD_TYPE.put(DataObjectOrArchiveUnitReferenceType.class, "DataObjectOrArchiveUnitReferenceType");
        CLASS_TO_XSD_TYPE.put(PersistentIdentifier.class, "PersistentIdType");
        CLASS_TO_XSD_TYPE.put(SigningInformation.class, "SigningInformationType");
        CLASS_TO_XSD_TYPE.put(SignatureDescription.class, "SignatureDescriptionType");
        CLASS_TO_XSD_TYPE.put(TimestampingInformation.class, "TimestampingInformationType");
        CLASS_TO_XSD_TYPE.put(AdditionalProof.class, "AdditionalProofType");
        CLASS_TO_XSD_TYPE.put(Signature.class, "SignatureType");
        CLASS_TO_XSD_TYPE.put(Signer.class, "SignerType");
        CLASS_TO_XSD_TYPE.put(Validator.class, "ValidatorType");
        CLASS_TO_XSD_TYPE.put(ReferencedObject.class, "ReferencedObjectType");
        CLASS_TO_XSD_TYPE.put(FileInfo.class, "FileInfoType");
        CLASS_TO_XSD_TYPE.put(FormatIdentification.class, "FormatIdentificationType");
        CLASS_TO_XSD_TYPE.put(Metadata.class, "CoreMetadataType");
        CLASS_TO_XSD_TYPE.put(PhysicalDimensions.class, "DimensionsType");
        CLASS_TO_XSD_TYPE.put(AgencyType.class, "OrganizationType");
        CLASS_TO_XSD_TYPE.put(AgentType.class, "AgentType");
        CLASS_TO_XSD_TYPE.put(PlaceType.class, "BirthOrDeathPlaceType");
        CLASS_TO_XSD_TYPE.put(LinkingAgentIdentifier.class, "LinkingAgentIdentifierType");
        CLASS_TO_XSD_TYPE.put(ManagementMetadata.class, "ManagementMetadataType");
        CLASS_TO_XSD_TYPE.put(Operation.class, "OperationType");
    }

    /**
     * Elements handled differently on purpose, by class: they are left out of the comparison.
     */
    private static final Map<Class<?>, Set<String>> DESIGN_CHOICES = new LinkedHashMap<>();

    static {
        // RuleType carries StartDate inside its Rule metadata (see RuleType and RuleMetadataKind)
        for (Class<?> c : CLASS_TO_XSD_TYPE.keySet()) if (RuleType.class.isAssignableFrom(c)) DESIGN_CHOICES.put(
            c,
            Set.of("StartDate")
        );
        // Vitam compaction extension, not part of the standard
        DESIGN_CHOICES.put(Content.class, Set.of("DocumentContainer", "DocumentPack"));
    }

    /**
     * Elements whose presence or cardinality differs from the XSD, found when this test was
     * written and to be fixed: left out of the comparison until then.
     */
    private static final Map<Class<?>, Set<String>> KNOWN_DRIFT = new LinkedHashMap<>();

    static {
        // History missing (2.1, 2.2); OriginatingSystemIdReplyTo and TextContent not in the 2.1 XSD;
        // DescriptionLanguage is single in the XSD, many in the class
        KNOWN_DRIFT.put(
            Content.class,
            Set.of("History", "OriginatingSystemIdReplyTo", "TextContent", "DescriptionLanguage")
        );
        // UpdateOperation is not in the 2.3 XSD any more
        KNOWN_DRIFT.put(Management.class, Set.of("UpdateOperation"));
        // UpdateOperation missing (2.1, 2.2)
        KNOWN_DRIFT.put(ManagementMetadata.class, Set.of("UpdateOperation"));
        KNOWN_DRIFT.put(ClassificationRule.class, Set.of("ClassificationAudience"));
        KNOWN_DRIFT.put(UpdateOperation.class, Set.of("ToDelete", "FullUpdate"));
        // single in the XSD, many in the class
        KNOWN_DRIFT.put(Event.class, Set.of("EventDetail"));
        KNOWN_DRIFT.put(CustodialHistory.class, Set.of("CustodialHistoryFile"));
        // many in the XSD, single in the class
        KNOWN_DRIFT.put(Signer.class, Set.of("Identifier"));
        KNOWN_DRIFT.put(SigningInformation.class, Set.of("AdditionalProof"));
        // typo: OrganisationDescriptiveMetadata in the class, OrganizationDescriptiveMetadata in the XSD
        KNOWN_DRIFT.put(AgencyType.class, Set.of("OrganizationDescriptiveMetadata", "OrganisationDescriptiveMetadata"));
    }

    /**
     * Classes whose metadataMap order differs from the XSD sequence (HoldRule placed before
     * LogBook), to be fixed.
     */
    private static final Set<Class<?>> KNOWN_ORDER_DRIFT = Set.of(Management.class, ManagementMetadata.class);

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void metadataMapsMatchTheXsd(SedaVersion version) throws Exception {
        SedaSchema schema = SedaSchema.of(fr.gouv.vitam.tools.sedaschema.SedaVersion.valueOf(version.name()));
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
            compare(
                e.getKey().getSimpleName() + " / " + e.getValue(),
                map,
                type,
                ignored,
                !KNOWN_ORDER_DRIFT.contains(e.getKey()),
                differences
            );
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
        boolean checkOrder,
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
        if (checkOrder && !commonXsdOrder.equals(commonClassOrder)) differences.add(
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
