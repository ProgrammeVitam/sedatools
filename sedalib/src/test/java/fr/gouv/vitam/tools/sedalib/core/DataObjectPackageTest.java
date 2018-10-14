package fr.gouv.vitam.tools.sedalib.core;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.LineEndNormalize;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

class DataObjectPackageTest {

    @Test
    void testCycleDetectionOK() throws SEDALibException, InterruptedException {
        // Given
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/TestSip.zip", "target/tmpJunit", null);
        si.doImport();
        SIPToArchiveTransferImporter wrongSi = new SIPToArchiveTransferImporter("src/test/resources/PacketSamples" +
                "/TestSipCyclic.zip", "target/tmpJunit"
                , null);
        wrongSi.doImport();

        // When test for acyclic
        si.getArchiveTransfer().getDataObjectPackage().verifyAcyclic();

        // Then ok
    }

    @Test
    void testCycleDetectionKO() throws SEDALibException, InterruptedException {
        // Given
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter("src/test/resources/PacketSamples" +
                "/TestSipCyclic.zip", "target/tmpJunit"
                , null);
        si.doImport();

        // When test for acyclic, then KO
        assertThatThrownBy(() -> {
            si.getArchiveTransfer().getDataObjectPackage().verifyAcyclic();
            fail("Devrait détecter un cycle");
        }).hasMessageContaining("ID4->ID8->ID20->ID34->ID8");
    }

    @Test
    void testDogNormalisation() throws SEDALibException, InterruptedException, JsonProcessingException {

        //Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/TestSipWrongDogReferences.zip", "target/tmpJunit", null);
        si.doImport();
        try {
            si.getArchiveTransfer().getDataObjectPackage().verifyDogUnicityCapacity();
            fail("Devrait détecter un problème de normalisation des DataObjectGroup");
        } catch (SEDALibException e) {
            System.err.println(e.getMessage());
            assert (e.getMessage().contains("impossible sur l'ArchiveUnit [ID21]"));
        }

        si = new SIPToArchiveTransferImporter("src/test/resources/PacketSamples/TestSipDogMerge.zip", "target" +
                "/tmpJunit", null);
        si.doImport();
        String testau = "{\r\n" + "  \"archiveUnitProfileXmlData\" : null,\r\n" + "  \"managementXmlData\" : null,\r\n"
                + "  \"contentXmlData\" : \"<Content>\\n              <DescriptionLevel>Item</DescriptionLevel>\\n              <Title>20160429_tuleap.pdf</Title>\\n              <Description>Document \\\"20160429_tuleap.pdf\\\" joint au message &lt;a8f34cc23a55bf2de3606d4e45609230@culture.gouv.fr></Description>\\n            </Content>\",\r\n"
                + "  \"childrenAuList\" : {\r\n"
                + "    \"inDataObjectPackageIdList\" : [ ]\r\n" + "  },\r\n" + "  \"dataObjectRefList\" : {\r\n"
                + "    \"inDataObjectPackageIdList\" : [ \"ID16\", \"ID22\", \"ID200\", \"ID201\" ]\r\n" + "  },\r\n"
                + "  \"inDataObjectPackageId\" : \"ID21\",\r\n" + "  \"onDiskPath\" : null\r\n" + "}";

        ArchiveUnit au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID21");
        String sau = mapper.writeValueAsString(au);
        assertEquals(LineEndNormalize(testau), LineEndNormalize(sau));

        si.getArchiveTransfer().getDataObjectPackage().normalizeUniqDataObjectGroup();

        testau = "{\r\n" +
                "  \"archiveUnitProfileXmlData\" : null,\r\n" +
                "  \"managementXmlData\" : null,\r\n" +
                "  \"contentXmlData\" : \"<Content>\\n              <DescriptionLevel>Item</DescriptionLevel>\\n              <Title>20160429_tuleap.pdf</Title>\\n              <Description>Document \\\"20160429_tuleap.pdf\\\" joint au message &lt;a8f34cc23a55bf2de3606d4e45609230@culture.gouv.fr></Description>\\n            </Content>\",\r\n" +
                "  \"childrenAuList\" : {\r\n" +
                "    \"inDataObjectPackageIdList\" : [ ]\r\n" +
                "  },\r\n" +
                "  \"dataObjectRefList\" : {\r\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID52\" ]\r\n" +
                "  },\r\n" +
                "  \"inDataObjectPackageId\" : \"ID21\",\r\n" +
                "  \"onDiskPath\" : null\r\n" +
                "}";

//		for (Map.Entry<String, ArchiveUnit> pair : si.getArchiveTransfer().getDataObjectPackage()
//				.getAuInDataObjectPackageIdMap().entrySet()) {
//			if (pair.getValue().contentXmlData.contains("20160429_tuleap.pdf")) {
//				au = pair.getValue();
//				break;
//			}
//		}
        au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID21");
        sau = mapper.writeValueAsString(au);
//		System.out.println(sau);
        assertEquals(LineEndNormalize(testau), LineEndNormalize(sau));
        System.err.println("La fusion des DOG a bien eue lieu");

        String testog = "{\r\n" +
                "  \"binaryDataObjectList\" : [ {\r\n" +
                "    \"dataObjectSystemId\" : null,\r\n" +
                "    \"dataObjectGroupSystemId\" : null,\r\n" +
                "    \"relationshipsXmlData\" : [ ],\r\n" +
                "    \"dataObjectGroupReferenceId\" : null,\r\n" +
                "    \"dataObjectGroupId\" : null,\r\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_1\",\r\n" +
                "    \"uri\" : \"content/ID17.ods\",\r\n" +
                "    \"messageDigest\" : \"ccc63de7306ced0b656f8f5bcb718304fefa93baed5bdb6e523146ff9ff9795ad22fff6077110fbd171df9553a24554fd5aa2b72cf76ffb4c24c7371be5f774e\",\r\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\r\n" +
                "    \"size\" : 50651,\r\n" +
                "    \"compressed\" : null,\r\n" +
                "    \"formatIdentification\" : {\r\n" +
                "      \"formatLitteral\" : \"OpenDocument Spreadsheet\",\r\n" +
                "      \"mimeType\" : \"application/vnd.oasis.opendocument.spreadsheet\",\r\n" +
                "      \"formatId\" : \"fmt/294\",\r\n" +
                "      \"encoding\" : null\r\n" +
                "    },\r\n" +
                "    \"fileInfo\" : {\r\n" +
                "      \"filename\" : \"201609-TdB-suivi-des-a.ods\",\r\n" +
                "      \"creatingApplicationName\" : null,\r\n" +
                "      \"creatingApplicationVersion\" : null,\r\n" +
                "      \"creatingOs\" : null,\r\n" +
                "      \"creatingOsVersion\" : null,\r\n" +
                "      \"lastModified\" : 1535484139000\r\n" +
                "    },\r\n" +
                "    \"metadataXmlData\" : null,\r\n" +
                "    \"otherMetadataXmlData\" : null,\r\n" +
                "    \"inDataObjectPackageId\" : \"ID17\",\r\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID17.ods\"\r\n" +
                "  }, {\r\n" +
                "    \"dataObjectSystemId\" : null,\r\n" +
                "    \"dataObjectGroupSystemId\" : null,\r\n" +
                "    \"relationshipsXmlData\" : [ ],\r\n" +
                "    \"dataObjectGroupReferenceId\" : null,\r\n" +
                "    \"dataObjectGroupId\" : null,\r\n" +
                "    \"dataObjectVersion\" : \"TextContent_1\",\r\n" +
                "    \"uri\" : \"content/ID19.txt\",\r\n" +
                "    \"messageDigest\" : \"7040a2d9f0a4ba697fde735cbe12f462af609eda6e35a0f3ddbddddbdaf8ffdd394c37a59bbb8ea4238f13169e0d634fa75cf3b251c4607144010d3552a87dd2\",\r\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\r\n" +
                "    \"size\" : 3307,\r\n" +
                "    \"compressed\" : null,\r\n" +
                "    \"formatIdentification\" : {\r\n" +
                "      \"formatLitteral\" : \"Plain Text File\",\r\n" +
                "      \"mimeType\" : \"text/plain\",\r\n" +
                "      \"formatId\" : \"x-fmt/111\",\r\n" +
                "      \"encoding\" : null\r\n" +
                "    },\r\n" +
                "    \"fileInfo\" : {\r\n" +
                "      \"filename\" : \"201609-TdB-suivi-des-a.txt\",\r\n" +
                "      \"creatingApplicationName\" : null,\r\n" +
                "      \"creatingApplicationVersion\" : null,\r\n" +
                "      \"creatingOs\" : null,\r\n" +
                "      \"creatingOsVersion\" : null,\r\n" +
                "      \"lastModified\" : 1535484139000\r\n" +
                "    },\r\n" +
                "    \"metadataXmlData\" : null,\r\n" +
                "    \"otherMetadataXmlData\" : null,\r\n" +
                "    \"inDataObjectPackageId\" : \"ID19\",\r\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID19.txt\"\r\n" +
                "  }, {\r\n" +
                "    \"dataObjectSystemId\" : null,\r\n" +
                "    \"dataObjectGroupSystemId\" : null,\r\n" +
                "    \"relationshipsXmlData\" : [ ],\r\n" +
                "    \"dataObjectGroupReferenceId\" : null,\r\n" +
                "    \"dataObjectGroupId\" : null,\r\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_1\",\r\n" +
                "    \"uri\" : \"content/ID23.pdf\",\r\n" +
                "    \"messageDigest\" : \"559dc14b4821f78aa138bb72923214c0f3635f0262f63999ba9c78d8df7833206f5e8310dedff60e9522c502ae3a5fe4e444c8e333efffac0f9c242b8f7a27f6\",\r\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\r\n" +
                "    \"size\" : 3868571,\r\n" +
                "    \"compressed\" : null,\r\n" +
                "    \"formatIdentification\" : {\r\n" +
                "      \"formatLitteral\" : \"Acrobat PDF 1.4 - Portable Document Format\",\r\n" +
                "      \"mimeType\" : \"application/pdf\",\r\n" +
                "      \"formatId\" : \"fmt/18\",\r\n" +
                "      \"encoding\" : null\r\n" +
                "    },\r\n" +
                "    \"fileInfo\" : {\r\n" +
                "      \"filename\" : \"20160429-tuleap.pdf\",\r\n" +
                "      \"creatingApplicationName\" : null,\r\n" +
                "      \"creatingApplicationVersion\" : null,\r\n" +
                "      \"creatingOs\" : null,\r\n" +
                "      \"creatingOsVersion\" : null,\r\n" +
                "      \"lastModified\" : 1535484139000\r\n" +
                "    },\r\n" +
                "    \"metadataXmlData\" : null,\r\n" +
                "    \"otherMetadataXmlData\" : null,\r\n" +
                "    \"inDataObjectPackageId\" : \"ID23\",\r\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID23.pdf\"\r\n" +
                "  }, {\r\n" +
                "    \"dataObjectSystemId\" : null,\r\n" +
                "    \"dataObjectGroupSystemId\" : null,\r\n" +
                "    \"relationshipsXmlData\" : [ ],\r\n" +
                "    \"dataObjectGroupReferenceId\" : null,\r\n" +
                "    \"dataObjectGroupId\" : null,\r\n" +
                "    \"dataObjectVersion\" : \"TextContent_1\",\r\n" +
                "    \"uri\" : \"content/ID24.txt\",\r\n" +
                "    \"messageDigest\" : \"14a0a17426b8b356f7769faede46fd09391689c5362939b8b5d8559fda5908b8579072cb802a87856f172401ded5f8bcf3c0315340da415b71e6f86deef72545\",\r\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\r\n" +
                "    \"size\" : 5104,\r\n" +
                "    \"compressed\" : null,\r\n" +
                "    \"formatIdentification\" : {\r\n" +
                "      \"formatLitteral\" : \"Plain Text File\",\r\n" +
                "      \"mimeType\" : \"text/plain\",\r\n" +
                "      \"formatId\" : \"x-fmt/111\",\r\n" +
                "      \"encoding\" : null\r\n" +
                "    },\r\n" +
                "    \"fileInfo\" : {\r\n" +
                "      \"filename\" : \"20160429-tuleap.pdf.txt\",\r\n" +
                "      \"creatingApplicationName\" : null,\r\n" +
                "      \"creatingApplicationVersion\" : null,\r\n" +
                "      \"creatingOs\" : null,\r\n" +
                "      \"creatingOsVersion\" : null,\r\n" +
                "      \"lastModified\" : 1535484139000\r\n" +
                "    },\r\n" +
                "    \"metadataXmlData\" : null,\r\n" +
                "    \"otherMetadataXmlData\" : null,\r\n" +
                "    \"inDataObjectPackageId\" : \"ID24\",\r\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID24.txt\"\r\n" +
                "  }, {\r\n" +
                "    \"dataObjectSystemId\" : null,\r\n" +
                "    \"dataObjectGroupSystemId\" : null,\r\n" +
                "    \"relationshipsXmlData\" : [ ],\r\n" +
                "    \"dataObjectGroupReferenceId\" : null,\r\n" +
                "    \"dataObjectGroupId\" : null,\r\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_2\",\r\n" +
                "    \"uri\" : \"content/ID200.json\",\r\n" +
                "    \"messageDigest\" : \"3e8c7ca5f7f0a742b8f424639b81ed9c5d9c6296ad22e5fdb90cb908e04a36c51f698beb0045931e6df4001214f4f49f7b0d6b8ba4461c7a188da10ac5586839\",\r\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\r\n" +
                "    \"size\" : 120,\r\n" +
                "    \"compressed\" : null,\r\n" +
                "    \"formatIdentification\" : {\r\n" +
                "      \"formatLitteral\" : \"JSON Data Interchange Format\",\r\n" +
                "      \"mimeType\" : \"application/json\",\r\n" +
                "      \"formatId\" : \"fmt/817\",\r\n" +
                "      \"encoding\" : null\r\n" +
                "    },\r\n" +
                "    \"fileInfo\" : {\r\n" +
                "      \"filename\" : \"SmallContract2.json\",\r\n" +
                "      \"creatingApplicationName\" : null,\r\n" +
                "      \"creatingApplicationVersion\" : null,\r\n" +
                "      \"creatingOs\" : null,\r\n" +
                "      \"creatingOsVersion\" : null,\r\n" +
                "      \"lastModified\" : 1535484139000\r\n" +
                "    },\r\n" +
                "    \"metadataXmlData\" : null,\r\n" +
                "    \"otherMetadataXmlData\" : null,\r\n" +
                "    \"inDataObjectPackageId\" : \"ID200\",\r\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID200.json\"\r\n" +
                "  }, {\r\n" +
                "    \"dataObjectSystemId\" : null,\r\n" +
                "    \"dataObjectGroupSystemId\" : null,\r\n" +
                "    \"relationshipsXmlData\" : [ ],\r\n" +
                "    \"dataObjectGroupReferenceId\" : null,\r\n" +
                "    \"dataObjectGroupId\" : null,\r\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_3\",\r\n" +
                "    \"uri\" : \"content/ID201.json\",\r\n" +
                "    \"messageDigest\" : \"3e8c7ca5f7f0a742b8f424639b81ed9c5d9c6296ad22e5fdb90cb908e04a36c51f698beb0045931e6df4001214f4f49f7b0d6b8ba4461c7a188da10ac5586839\",\r\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\r\n" +
                "    \"size\" : 120,\r\n" +
                "    \"compressed\" : null,\r\n" +
                "    \"formatIdentification\" : {\r\n" +
                "      \"formatLitteral\" : \"JSON Data Interchange Format\",\r\n" +
                "      \"mimeType\" : \"application/json\",\r\n" +
                "      \"formatId\" : \"fmt/817\",\r\n" +
                "      \"encoding\" : null\r\n" +
                "    },\r\n" +
                "    \"fileInfo\" : {\r\n" +
                "      \"filename\" : \"SmallContract3.json\",\r\n" +
                "      \"creatingApplicationName\" : null,\r\n" +
                "      \"creatingApplicationVersion\" : null,\r\n" +
                "      \"creatingOs\" : null,\r\n" +
                "      \"creatingOsVersion\" : null,\r\n" +
                "      \"lastModified\" : 1535484139000\r\n" +
                "    },\r\n" +
                "    \"metadataXmlData\" : null,\r\n" +
                "    \"otherMetadataXmlData\" : null,\r\n" +
                "    \"inDataObjectPackageId\" : \"ID201\",\r\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID201.json\"\r\n" +
                "  } ],\r\n" +
                "  \"physicalDataObjectList\" : [ {\r\n" +
                "    \"dataObjectSystemId\" : null,\r\n" +
                "    \"dataObjectGroupSystemId\" : null,\r\n" +
                "    \"relationshipsXmlData\" : [ ],\r\n" +
                "    \"dataObjectVersion\" : \"PhysicalMaster_1\",\r\n" +
                "    \"physicalIdXmlData\" : \"<PhysicalId>940 W</PhysicalId>\",\r\n" +
                "    \"physicalDimensionsXmlData\" : \"<PhysicalDimensions>\\n          <Width unit=\\\"centimetre\\\">10</Width>\\n          <Height unit=\\\"centimetre\\\">8</Height>\\n          <Depth unit=\\\"centimetre\\\">1</Depth>\\n          <Diameter unit=\\\"centimetre\\\">0</Diameter>\\n          <Weight unit=\\\"gram\\\">59</Weight>\\n        </PhysicalDimensions>\",\r\n" +
                "    \"inDataObjectPackageId\" : \"ID18\",\r\n" +
                "    \"onDiskPath\" : null\r\n" +
                "  } ],\r\n" +
                "  \"logBookXmlData\" : null,\r\n" +
                "  \"inDataObjectPackageId\" : \"ID52\",\r\n" +
                "  \"onDiskPath\" : null\r\n" +
                "}";
        DataObjectGroup og = si.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap()
                .get("ID52");
//		System.out.println(mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og).replaceAll("\"lastModified\" : .*", "");
        sog = LineEndNormalize(sog.replaceAll("\"onDiskPath\" : .*\"", ""));

        testog = testog.replaceAll("\"lastModified\" : .*", "");
        testog = LineEndNormalize(testog.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertEquals(sog, testog);
        System.err.println("La fusion des DOG a bien eue lieu");
    }

    @Test
    void testRegenerateIds() throws SEDALibException, InterruptedException, JsonProcessingException {
        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter("src/test/resources/PacketSamples/TestSip" +
                ".zip", "target/tmpJunit", null);
        si.doImport();
        si.getArchiveTransfer().getDataObjectPackage().regenerateContinuousIds();

        String testau = "{\r\n" +
                "  \"archiveUnitProfileXmlData\" : null,\r\n" +
                "  \"managementXmlData\" : null,\r\n" +
                "  \"contentXmlData\" : \"<Content>\\n                  <DescriptionLevel>Item</DescriptionLevel>\\n                  <Title>3059_KO_agencies_HTML.csv</Title>\\n                </Content>\",\r\n" +
                "  \"childrenAuList\" : {\r\n" +
                "    \"inDataObjectPackageIdList\" : [ ]\r\n" +
                "  },\r\n" +
                "  \"dataObjectRefList\" : {\r\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID46\" ]\r\n" +
                "  },\r\n" +
                "  \"inDataObjectPackageId\" : \"ID19\",\r\n" +
                "  \"onDiskPath\" : null\r\n" +
                "}";

        ArchiveUnit au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID19");
        String sau = mapper.writeValueAsString(au);
    //    System.out.println(sau);
        assertEquals(LineEndNormalize(testau), LineEndNormalize(sau));
    }
}
