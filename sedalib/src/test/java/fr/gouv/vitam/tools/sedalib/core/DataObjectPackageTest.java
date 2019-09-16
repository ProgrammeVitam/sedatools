package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.LineEndNormalize;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class DataObjectPackageTest {

    @Test
    void testCycleDetectionOK() throws SEDALibException, InterruptedException {
        // Given
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/TestSip.zip", "target/tmpJunit/TestSIP.zip-tmpdir", null);
        si.doImport();
        SIPToArchiveTransferImporter wrongSi = new SIPToArchiveTransferImporter("src/test/resources/PacketSamples" +
                "/TestSipCyclic.zip", "target/tmpJunit/TestSIPCyclic.zip-tmpdir"
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
                "/TestSipCyclic.zip", "target/tmpJunit/TestSipCyclic.zip-tmpdir"
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
                "src/test/resources/PacketSamples/TestSipWrongDogReferences.zip",
                "target/tmpJunit/TestSipWrongDogReferences.zip-tmpdir", null);
        si.doImport();
        try {
            si.getArchiveTransfer().getDataObjectPackage().verifyDogUnicityCapacity();
            fail("Devrait détecter un problème de normalisation des DataObjectGroup");
        } catch (SEDALibException e) {
            System.err.println(e.getMessage());
            assert (e.getMessage().contains("impossible sur l'ArchiveUnit [ID21]"));
        }

        si = new SIPToArchiveTransferImporter("src/test/resources/PacketSamples/TestSipDogMerge.zip",
                "target/tmpJunit/TestSipDogMerge.zip-tmpdir", null);
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

        String testog = "{\n" +
                "  \"binaryDataObjectList\" : [ {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_1\",\n" +
                "    \"uri\" : \"content/ID17.ods\",\n" +
                "    \"messageDigest\" : \"ccc63de7306ced0b656f8f5bcb718304fefa93baed5bdb6e523146ff9ff9795ad22fff6077110fbd171df9553a24554fd5aa2b72cf76ffb4c24c7371be5f774e\",\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "    \"size\" : 50651,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"formatLitteral\" : \"OpenDocument Spreadsheet\",\n" +
                "      \"mimeType\" : \"application/vnd.oasis.opendocument.spreadsheet\",\n" +
                "      \"formatId\" : \"fmt/294\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"201609-TdB-suivi-des-a.ods\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID17\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID17.ods\"\n" +
                "  }, {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : \"TextContent_1\",\n" +
                "    \"uri\" : \"content/ID19.txt\",\n" +
                "    \"messageDigest\" : \"7040a2d9f0a4ba697fde735cbe12f462af609eda6e35a0f3ddbddddbdaf8ffdd394c37a59bbb8ea4238f13169e0d634fa75cf3b251c4607144010d3552a87dd2\",\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "    \"size\" : 3307,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"formatLitteral\" : \"Plain Text File\",\n" +
                "      \"mimeType\" : \"text/plain\",\n" +
                "      \"formatId\" : \"x-fmt/111\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"201609-TdB-suivi-des-a.txt\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID19\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID19.txt\"\n" +
                "  }, {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_1\",\n" +
                "    \"uri\" : \"content/ID23.pdf\",\n" +
                "    \"messageDigest\" : \"559dc14b4821f78aa138bb72923214c0f3635f0262f63999ba9c78d8df7833206f5e8310dedff60e9522c502ae3a5fe4e444c8e333efffac0f9c242b8f7a27f6\",\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "    \"size\" : 3868571,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"formatLitteral\" : \"Acrobat PDF 1.4 - Portable Document Format\",\n" +
                "      \"mimeType\" : \"application/pdf\",\n" +
                "      \"formatId\" : \"fmt/18\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"20160429-tuleap.pdf\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID23\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID23.pdf\"\n" +
                "  }, {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : \"TextContent_1\",\n" +
                "    \"uri\" : \"content/ID24.txt\",\n" +
                "    \"messageDigest\" : \"14a0a17426b8b356f7769faede46fd09391689c5362939b8b5d8559fda5908b8579072cb802a87856f172401ded5f8bcf3c0315340da415b71e6f86deef72545\",\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "    \"size\" : 5104,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"formatLitteral\" : \"Plain Text File\",\n" +
                "      \"mimeType\" : \"text/plain\",\n" +
                "      \"formatId\" : \"x-fmt/111\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"20160429-tuleap.pdf.txt\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID24\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID24.txt\"\n" +
                "  }, {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_2\",\n" +
                "    \"uri\" : \"content/ID200.json\",\n" +
                "    \"messageDigest\" : \"3e8c7ca5f7f0a742b8f424639b81ed9c5d9c6296ad22e5fdb90cb908e04a36c51f698beb0045931e6df4001214f4f49f7b0d6b8ba4461c7a188da10ac5586839\",\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "    \"size\" : 120,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"formatLitteral\" : \"JSON Data Interchange Format\",\n" +
                "      \"mimeType\" : \"application/json\",\n" +
                "      \"formatId\" : \"fmt/817\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"SmallContract2.json\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID200\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID200.json\"\n" +
                "  }, {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : \"BinaryMaster_3\",\n" +
                "    \"uri\" : \"content/ID201.json\",\n" +
                "    \"messageDigest\" : \"3e8c7ca5f7f0a742b8f424639b81ed9c5d9c6296ad22e5fdb90cb908e04a36c51f698beb0045931e6df4001214f4f49f7b0d6b8ba4461c7a188da10ac5586839\",\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "    \"size\" : 120,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"formatLitteral\" : \"JSON Data Interchange Format\",\n" +
                "      \"mimeType\" : \"application/json\",\n" +
                "      \"formatId\" : \"fmt/817\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"SmallContract3.json\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID201\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSipDogMerge.zip-tmpdir\\\\content\\\\ID201.json\"\n" +
                "  } ],\n" +
                "  \"physicalDataObjectList\" : [ {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectVersion\" : \"PhysicalMaster_1\",\n" +
                "    \"physicalIdXmlData\" : \"<PhysicalId>940 W</PhysicalId>\",\n" +
                "    \"physicalDimensionsXmlData\" : \"<PhysicalDimensions>\\n          <Width unit=\\\"centimetre\\\">10</Width>\\n          <Height unit=\\\"centimetre\\\">8</Height>\\n          <Depth unit=\\\"centimetre\\\">1</Depth>\\n          <Diameter unit=\\\"centimetre\\\">0</Diameter>\\n          <Weight unit=\\\"gram\\\">59</Weight>\\n        </PhysicalDimensions>\",\n" +
                "    \"inDataObjectPackageId\" : \"ID18\",\n" +
                "    \"onDiskPath\" : null\n" +
                "  } ],\n" +
                "  \"logBookXmlData\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID52\",\n" +
                "  \"onDiskPath\" : null\n" +
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
                ".zip", "target/tmpJunit/TestSip.zip-tmpdir", null);
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
