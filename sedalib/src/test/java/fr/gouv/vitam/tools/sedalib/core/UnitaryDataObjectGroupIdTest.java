package fr.gouv.vitam.tools.sedalib.core;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DataObjectGroupId and DataObjectGroupReferenceId are declared by MinimalDataObjectType in every
 * SEDA version, and read as StringType to build the DataObjectGroup model (see
 * AbstractUnitaryDataObject). They were missing from the 2.2 and 2.3 maps, which made any flat
 * manifest referencing an existing group unreadable.
 */
@ExtendWith(SedaContextExtension.class)
class UnitaryDataObjectGroupIdTest {

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void binaryDataObjectReadsBothGroupIds(SedaVersion version) throws Exception {
        SedaContext.setVersion(version);

        BinaryDataObject referencing = new BinaryDataObject();
        referencing.fromSedaXmlFragments(
            "<DataObjectGroupReferenceId>ID10</DataObjectGroupReferenceId>" +
            "<DataObjectVersion>BinaryMaster_1</DataObjectVersion>" +
            "<Uri>content/ID11.jpg</Uri>"
        );
        assertThat(referencing.getFirstNamedMetadata("DataObjectGroupReferenceId"))
            .isInstanceOf(StringType.class)
            .extracting(m -> ((StringType) m).getValue())
            .isEqualTo("ID10");

        BinaryDataObject defining = new BinaryDataObject();
        defining.fromSedaXmlFragments(
            "<DataObjectGroupId>ID10</DataObjectGroupId>" +
            "<DataObjectVersion>BinaryMaster_1</DataObjectVersion>" +
            "<Uri>content/ID11.jpg</Uri>"
        );
        assertThat(defining.getFirstNamedMetadata("DataObjectGroupId")).isInstanceOf(StringType.class);
    }

    @ParameterizedTest
    @EnumSource(value = SedaVersion.class, names = { "V2_1", "V2_2", "V2_3" })
    void physicalDataObjectReadsBothGroupIds(SedaVersion version) throws Exception {
        SedaContext.setVersion(version);

        PhysicalDataObject referencing = new PhysicalDataObject();
        referencing.fromSedaXmlFragments(
            "<DataObjectGroupReferenceId>ID10</DataObjectGroupReferenceId>" +
            "<DataObjectVersion>PhysicalMaster_1</DataObjectVersion>" +
            "<PhysicalId>940 W</PhysicalId>"
        );
        assertThat(referencing.getFirstNamedMetadata("DataObjectGroupReferenceId"))
            .isInstanceOf(StringType.class)
            .extracting(m -> ((StringType) m).getValue())
            .isEqualTo("ID10");

        PhysicalDataObject defining = new PhysicalDataObject();
        defining.fromSedaXmlFragments(
            "<DataObjectGroupId>ID10</DataObjectGroupId>" +
            "<DataObjectVersion>PhysicalMaster_1</DataObjectVersion>" +
            "<PhysicalId>940 W</PhysicalId>"
        );
        assertThat(defining.getFirstNamedMetadata("DataObjectGroupId")).isInstanceOf(StringType.class);
    }
}
