package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.HoldRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.SchemaMetadataMapBuilder;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The maps are no longer written by hand: they are built from the XSD, and this pins what the
 * builder answers. The structure itself is checked against the schema by construction; what has to
 * stay stable is the order, which drives the XML output, the Java class of each element and the
 * metadata sedalib holds outside of the schema.
 */
class SchemaMetadataMapBuilderTest {

    @Test
    void buildsContentInTheSchemaOrderWithTheVitamExtensionsAtTheEnd() throws Exception {
        assertThat(SchemaMetadataMapBuilder.build(Content.class, SedaVersion.V2_1).keySet())
            .startsWith("DescriptionLevel", "Title", "FilePlanPosition", "SystemId", "OriginatingSystemId")
            .endsWith(
                "Gps",
                "History",
                "OriginatingSystemIdReplyTo",
                "TextContent",
                "DocumentContainer",
                "DocumentPack"
            );
        // in the standard since 2.2, so in the schema order, before History
        assertThat(SchemaMetadataMapBuilder.build(Content.class, SedaVersion.V2_2).keySet()).endsWith(
            "Gps",
            "OriginatingSystemIdReplyTo",
            "TextContent",
            "History",
            "DocumentContainer",
            "DocumentPack"
        );
        // History is a Vitam extension in 2.3 too
        assertThat(SchemaMetadataMapBuilder.build(Content.class, SedaVersion.V2_3).keySet()).endsWith(
            "PersistentIdentifier",
            "History",
            "DocumentContainer",
            "DocumentPack"
        );
    }

    @Test
    void keepsUpdateOperationInManagementWhenItLeftTheSchema() throws Exception {
        assertThat(SchemaMetadataMapBuilder.build(Management.class, SedaVersion.V2_1).keySet()).containsExactly(
            "StorageRule",
            "AppraisalRule",
            "AccessRule",
            "DisseminationRule",
            "ReuseRule",
            "ClassificationRule",
            "LogBook",
            "NeedAuthorization",
            "UpdateOperation",
            "HoldRule"
        );
        // HoldRule before UpdateOperation from 2.2, and UpdateOperation left the 2.3 schema
        assertThat(SchemaMetadataMapBuilder.build(Management.class, SedaVersion.V2_2).keySet()).endsWith(
            "HoldRule",
            "UpdateOperation"
        );
        assertThat(SchemaMetadataMapBuilder.build(Management.class, SedaVersion.V2_3).keySet()).endsWith(
            "HoldRule",
            "UpdateOperation"
        );
    }

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void marksTheMembersOfARepeatingRuleBlock(SedaVersion version) throws Exception {
        Map<String, ComplexListMetadataKind> hold = SchemaMetadataMapBuilder.build(HoldRule.class, version);

        assertThat(hold.get("Rule")).isNotInstanceOf(RuleMetadataKind.class);
        assertThat(hold.get("HoldEndDate")).isInstanceOf(RuleMetadataKind.class);
        assertThat(hold.get("HoldOwner")).isInstanceOf(RuleMetadataKind.class);
        assertThat(hold.get("PreventRearrangement")).isInstanceOf(RuleMetadataKind.class);
        assertThat(hold.get("PreventInheritance")).isNotInstanceOf(RuleMetadataKind.class);
        // the rule start date is carried by the Rule metadata, so it is not an element of its own
        assertThat(hold).doesNotContainKey("StartDate");

        Map<String, ComplexListMetadataKind> appraisal = SchemaMetadataMapBuilder.build(AppraisalRule.class, version);
        assertThat(appraisal.keySet()).containsExactly("Rule", "PreventInheritance", "RefNonRuleId", "FinalAction");
        // an enumeration in the XSD, read as a plain string
        assertThat(appraisal.get("FinalAction").getMetadataClass()).isEqualTo(StringType.class);
    }

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void leavesTheInlinedBinaryOutOfTheDataObjects(SedaVersion version) throws Exception {
        Map<String, ComplexListMetadataKind> binary = SchemaMetadataMapBuilder.build(BinaryDataObject.class, version);

        assertThat(binary).doesNotContainKey("Attachment").containsKey("Uri");
        // read to rebuild the DataObjectGroup model, in every version
        assertThat(binary.get("DataObjectGroupId").getMetadataClass()).isEqualTo(StringType.class);
        assertThat(binary.get("DataObjectGroupReferenceId").getMetadataClass()).isEqualTo(StringType.class);
    }
}
