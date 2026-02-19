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
package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.CSVMetadataToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class CSVMetadataToDataObjectPackageImporterTest {

    @Test
    void importOKCSV1column() throws SEDALibException, InterruptedException, JsonProcessingException {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CSVMetadataToDataObjectPackageImporter cmi;

        // When loaded with the csv OK test file
        cmi = new CSVMetadataToDataObjectPackageImporter(
            "src/test/resources/PacketSamples/MetadataTestOK1col.csv",
            "windows-1252",
            ';',
            null
        );
        cmi.doImport();

        // Then
        String testAuID10 =
            "{\n" +
            "  \"archiveUnitProfileXmlData\" : null,\n" +
            "  \"managementXmlData\" : null,\n" +
            "  \"contentXmlData\" : \"<Content>\\n  <DescriptionLevel>RecordGrp</DescriptionLevel>\\n  <Title>Root2</Title>\\n</Content>\",\n" +
            "  \"childrenAuList\" : {\n" +
            "    \"inDataObjectPackageIdList\" : [ ]\n" +
            "  },\n" +
            "  \"dataObjectRefList\" : {\n" +
            "    \"inDataObjectPackageIdList\" : [ ]\n" +
            "  },\n" +
            "  \"inDataObjectPackageId\" : \"ID10\",\n" +
            "  \"onDiskPath\" : null\n" +
            "}";
        String testAuID17 =
            "{\n" +
            "  \"archiveUnitProfileXmlData\" : null,\n" +
            "  \"managementXmlData\" : null,\n" +
            "  \"contentXmlData\" : \"<Content>\\n  <DescriptionLevel>RecordGrp</DescriptionLevel>\\n  <Title>Root2</Title>\\n</Content>\",\n" +
            "  \"childrenAuList\" : {\n" +
            "    \"inDataObjectPackageIdList\" : [ ]\n" +
            "  },\n" +
            "  \"dataObjectRefList\" : {\n" +
            "    \"inDataObjectPackageIdList\" : [ ]\n" +
            "  },\n" +
            "  \"inDataObjectPackageId\" : \"ID17\",\n" +
            "  \"onDiskPath\" : null\n" +
            "}";
        // Root2 is either the first or last AU (random order due to hashmap use)
        ArchiveUnit au = cmi.getDataObjectPackage().getArchiveUnitById("ID17");
        String sau = mapper.writeValueAsString(au);
        if (sau.contains("Root2")) assertThat(sau).isEqualToNormalizingNewlines(testAuID17);
        else {
            au = cmi.getDataObjectPackage().getArchiveUnitById("ID10");
            sau = mapper.writeValueAsString(au);
            assertThat(sau).isEqualToNormalizingNewlines(testAuID10);
        }
    }

    @Test
    void importOKCSV3column()
        throws SEDALibException, InterruptedException, JsonProcessingException, FileNotFoundException {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CSVMetadataToDataObjectPackageImporter cmi;

        // When loaded with the csv OK test file
        cmi = new CSVMetadataToDataObjectPackageImporter(
            "src/test/resources/PacketSamples/MetadataTestOK3col.csv",
            "windows-1252",
            ';',
            null
        );
        cmi.doImport();

        // Then
        ArchiveUnit au = cmi.getDataObjectPackage().getArchiveUnitById("Import-6");
        String sau = mapper.writeValueAsString(au);
        assertThat(sau).isEqualToNormalizingNewlines(ResourceUtils.getResourceAsString("import/AU_Import_02.json"));
    }

    @Test
    void importOKCSV3columnWithManagement()
        throws SEDALibException, InterruptedException, JsonProcessingException, FileNotFoundException {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CSVMetadataToDataObjectPackageImporter cmi;

        // When loaded with the csv OK test file
        cmi = new CSVMetadataToDataObjectPackageImporter(
            "src/test/resources/PacketSamples/MetadataTestManagementOK3col.csv",
            "windows-1252",
            ';',
            null
        );
        cmi.doImport();

        ArchiveUnit au = cmi.getDataObjectPackage().getArchiveUnitById("Import-6");
        String sau = mapper.writeValueAsString(au);
        assertThat(sau).isEqualToNormalizingNewlines(ResourceUtils.getResourceAsString("import/AU_Import_01.json"));
    }

    @Test
    void importTagKOCSV() throws SEDALibException {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CSVMetadataToDataObjectPackageImporter cmi;

        // When loaded with the csv OK test file
        cmi = new CSVMetadataToDataObjectPackageImporter(
            "src/test/resources/PacketSamples/MetadataTestTagKO.csv",
            "windows-1252",
            ';',
            null
        );

        assertThatThrownBy(cmi::doImport).hasMessageContaining("Caractère interdit"); // for StringType;
    }

    @Test
    void importLineKOCSV() throws SEDALibException {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        CSVMetadataToDataObjectPackageImporter cmi;

        // When loaded with the csv OK test file
        cmi = new CSVMetadataToDataObjectPackageImporter(
            "src/test/resources/PacketSamples/MetadataTestLineKO.csv",
            "windows-1252",
            ';',
            null
        );

        assertThatThrownBy(cmi::doImport).hasMessageContaining("ligne 4"); // for StringType;
    }
}
