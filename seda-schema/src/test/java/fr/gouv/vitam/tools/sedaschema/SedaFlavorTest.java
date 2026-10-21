package fr.gouv.vitam.tools.sedaschema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the Vitam flavour adds to the standard, version by version: the module bundles both so a
 * manifest can be read against the one it was written for.
 */
class SedaFlavorTest {

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2" })
    void vitamAddsItsOwnMetadataIn21And22(SedaVersion version) throws Exception {
        SedaSchema official = SedaSchema.of(version, SedaFlavor.OFFICIAL);
        SedaSchema vitam = SedaSchema.of(version, SedaFlavor.VITAM);

        assertThat(version.hasVitamVariant()).isTrue();
        for (String type : new String[] {
            "UpdateOperationType", // Management/UpdateOperation
            "ArchiveUnitIdentifierKeyType",
            "ManagementHistoryType", // ArchiveUnit/Management/History
            "ObjectGroupRefType", // ArchiveUnit/DataObjectGroup
        }) {
            assertThat(official.getComplexType(type)).as(type + " dans la XSD officielle " + version).isNull();
            assertThat(vitam.getComplexType(type)).as(type + " dans la XSD Vitam " + version).isNotNull();
        }
    }

    @Test
    void vitamBackPortsHoldRuleTo21() throws Exception {
        assertThat(SedaSchema.of(SedaVersion.V2_1, SedaFlavor.OFFICIAL).getComplexType("HoldRuleType")).isNull();
        assertThat(SedaSchema.of(SedaVersion.V2_1, SedaFlavor.VITAM).getComplexType("HoldRuleType")).isNotNull();
        // in the standard since 2.2
        assertThat(SedaSchema.of(SedaVersion.V2_2, SedaFlavor.OFFICIAL).getComplexType("HoldRuleType")).isNotNull();
    }

    @Test
    void vitamOpensTheExtensionPointsWithWildcards() throws Exception {
        // the *Abstract extension points are commented out and replaced by xsd:any, so that any
        // element is accepted where the standard expects a substitution group member
        SedaSchema official = SedaSchema.of(SedaVersion.V2_1, SedaFlavor.OFFICIAL);
        SedaSchema vitam = SedaSchema.of(SedaVersion.V2_1, SedaFlavor.VITAM);

        assertThat(official.getComplexType("DescriptiveMetadataContentType").getChildElement("Title")).isNotNull();
        assertThat(official.getRootElement("ArchiveTransfer")).isNotNull();
        assertThat(vitam.getRootElement("ArchiveTransfer")).isNotNull();
        assertThat(vitam.getTargetNamespace()).isEqualTo(official.getTargetNamespace());
    }

    @Test
    void bothFlavoursAreTheSameSchemaFrom23() throws Exception {
        assertThat(SedaVersion.V2_3.hasVitamVariant()).isFalse();
        assertThat(SedaVersion.V2_3.getMainXsdResource(SedaFlavor.VITAM)).isEqualTo(
            SedaVersion.V2_3.getMainXsdResource(SedaFlavor.OFFICIAL)
        );
        assertThat(SedaSchema.of(SedaVersion.V2_3, SedaFlavor.VITAM).getNamedTypes().keySet()).isEqualTo(
            SedaSchema.of(SedaVersion.V2_3, SedaFlavor.OFFICIAL).getNamedTypes().keySet()
        );
    }
}
