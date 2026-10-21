/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.resip.sedaobjecteditor.model;

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.content.Keyword;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.NamedTypeMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The sample metadata the editors propose, without the graphic interface.
 */
class MetadataSamplesTest {

    private static List<String> fieldNames(SEDAMetadata metadata) {
        return ((ComplexListType) metadata).getMetadataList()
            .stream()
            .map(SEDAMetadata::getXmlElementName)
            .collect(Collectors.toList());
    }

    private static Object valueOf(String type, String elementName, boolean minimal) throws SEDALibException {
        return ((NamedTypeMetadata) MetadataSamples.sample(SedaVersion.V2_1, type, elementName, minimal)).getValue();
    }

    @Test
    void buildsTheSimpleSamplesEmptyOrWithAnExampleValue() throws Exception {
        assertThat(valueOf("StringType", "Title", true)).isEqualTo("");
        assertThat(valueOf("StringType", "Title", false)).isEqualTo("Text");
        assertThat(valueOf("TextType", "Description", false)).isEqualTo("Text");
        assertThat(valueOf("IntegerType", "Size", true)).isNull();
        assertThat(valueOf("IntegerType", "Size", false)).isEqualTo(42L);
        assertThat(valueOf("DateType", "StartDate", true)).isNull();
        assertThat(valueOf("DateType", "StartDate", false)).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(valueOf("ArchiveUnitProfile", "ArchiveUnitProfile", false)).isEqualTo("AUP-00001");
        assertThat(MetadataSamples.sample(SedaVersion.V2_1, "StringType", "Title", true).getXmlElementName()).isEqualTo(
            "Title"
        );
    }

    @Test
    void buildsTheSampleOfTheExactClass() throws Exception {
        // ArchiveUnitProfile is a StringType with its own sample
        assertThat(
            MetadataSamples.sample(SedaVersion.V2_1, ArchiveUnitProfile.class, "ArchiveUnitProfile", false)
        ).isExactlyInstanceOf(ArchiveUnitProfile.class);
        assertThat(MetadataSamples.sample(SedaVersion.V2_1, StringType.class, "Title", false)).isExactlyInstanceOf(
            StringType.class
        );
    }

    @Test
    void findsTheMetadataClassesBySimpleName() throws Exception {
        assertThat(MetadataSamples.metadataClass("Keyword")).isEqualTo(Keyword.class);
        assertThat(MetadataSamples.metadataClass("AgentType")).isEqualTo(AgentType.class);
        assertThatThrownBy(() -> MetadataSamples.metadataClass("Unknown"))
            .isInstanceOf(SEDALibException.class)
            .hasMessageContaining("[Unknown] n'est pas connu");
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void keepsTheMainFieldsOnlyInAMinimalSample(SedaVersion sedaVersion) throws Exception {
        SEDAMetadata content = MetadataSamples.sample(sedaVersion, "Content", "Content", true);

        assertThat(content).isInstanceOf(Content.class);
        assertThat(((ComplexListType) content).getSedaVersion()).isEqualTo(sedaVersion);
        assertThat(fieldNames(content)).containsExactly("DescriptionLevel", "Title", "Description");
        assertThat(fieldNames(MetadataSamples.sample(sedaVersion, "Keyword", "Keyword", true))).containsExactly(
            "KeywordContent"
        );
    }

    @Test
    void showsTheRepeatableFieldsTwiceInAFullSample() throws Exception {
        SEDAMetadata writer = MetadataSamples.sample(SedaVersion.V2_1, "AgentType", "Writer", false);

        assertThat(writer.getXmlElementName()).isEqualTo("Writer");
        assertThat(fieldNames(writer).stream().filter("Role"::equals)).hasSize(2);
    }

    @Test
    void followsTheFieldsOfTheVersion() throws Exception {
        assertThat(fieldNames(MetadataSamples.sample(SedaVersion.V2_1, "Event", "Event", false))).doesNotContain(
            "LinkingAgentIdentifier"
        );
        assertThat(fieldNames(MetadataSamples.sample(SedaVersion.V2_2, "Event", "Event", false))).contains(
            "LinkingAgentIdentifier"
        );
    }

    @Test
    void hasNoSampleOfTheCompactionMetadata() {
        // a full Content holds the compaction ones, built by the compaction and never edited
        assertThatThrownBy(() -> MetadataSamples.sample(SedaVersion.V2_1, "Content", "Content", false))
            .isInstanceOf(SEDALibException.class)
            .hasMessageContaining("DocumentContainer");
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void buildsDataObjectSamplesInThePackageVersionWithoutAddingThem(SedaVersion sedaVersion) throws Exception {
        DataObjectPackage dataObjectPackage = new DataObjectPackage(sedaVersion);

        BinaryDataObject bdo = MetadataSamples.binaryDataObjectSample(dataObjectPackage, true);
        PhysicalDataObject pdo = MetadataSamples.physicalDataObjectSample(dataObjectPackage, true);

        assertThat(bdo.getSedaVersion()).isEqualTo(sedaVersion);
        assertThat(pdo.getSedaVersion()).isEqualTo(sedaVersion);
        assertThat(dataObjectPackage.getBdoInDataObjectPackageIdMap()).isEmpty();
        assertThat(dataObjectPackage.getPdoInDataObjectPackageIdMap()).isEmpty();
    }
}
