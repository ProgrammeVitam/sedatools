package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.CompressedFileToArchiveTransferImporter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompressedFileImportTest implements UseTestFiles {

    private void eraseAll(String dirOrFile) {
        try {
            Files.delete(Paths.get(dirOrFile));
        } catch (Exception ignored) {
        }
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception ignored) {
        }
    }

    static Function<String, String> replaced = s -> "Replaced";

    @Test
    void TestZipImport() throws Exception {

        // do import of test directory
        CompressedFileToArchiveTransferImporter zi = new CompressedFileToArchiveTransferImporter(
                "src/test/resources/zip/TestImport.zip", "target/tmpJunit/TestImport.zip-tmpdir", "UTF8", null, null);
        zi.addIgnorePattern("Thumbs.db");
        zi.addIgnorePattern("pagefile.sys");
        zi.doImport();

        // assert macro results
        assertEquals(22, zi.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size());
        assertEquals(11, zi.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size());

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // assert one dataObjectGroup using serialization
        String testog = "{\n" +
                "  \"binaryDataObjectList\" : [ {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"BinaryMaster_1\"\n" +
                "    },\n" +
                "    \"uri\" : null,\n" +
                "    \"messageDigest\" : {\n" +
                "      \"type\" : \"DigestType\",\n" +
                "      \"elementName\" : \"MessageDigest\",\n" +
                "      \"value\" : \"ccc63de7306ced0b656f8f5bcb718304fefa93baed5bdb6e523146ff9ff9795ad22fff6077110fbd171df9553a24554fd5aa2b72cf76ffb4c24c7371be5f774e\",\n" +
                "      \"algorithm\" : \"SHA-512\"\n" +
                "    },\n" +
                "    \"size\" : {\n" +
                "      \"type\" : \"IntegerType\",\n" +
                "      \"elementName\" : \"Size\",\n" +
                "      \"value\" : 50651\n" +
                "    },\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"elementName\" : \"FormatIdentification\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatLitteral\",\n" +
                "        \"value\" : \"OpenDocument Spreadsheet\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"MimeType\",\n" +
                "        \"value\" : \"application/vnd.oasis.opendocument.spreadsheet\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatId\",\n" +
                "        \"value\" : \"fmt/294\"\n" +
                "      } ]\n" +
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
                "        \"dateTimeString\" : \"2019-09-18T20:04:20.150050Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadata\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID17\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestImport.zip-tmpdir\\\\Root\\\\Node 1\\\\Node 1.1\\\\__BinaryMaster_1__201609-TdB-suivi-des-a.ods\"\n" +
                "  }, {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"TextContent_1\"\n" +
                "    },\n" +
                "    \"uri\" : null,\n" +
                "    \"messageDigest\" : {\n" +
                "      \"type\" : \"DigestType\",\n" +
                "      \"elementName\" : \"MessageDigest\",\n" +
                "      \"value\" : \"7040a2d9f0a4ba697fde735cbe12f462af609eda6e35a0f3ddbddddbdaf8ffdd394c37a59bbb8ea4238f13169e0d634fa75cf3b251c4607144010d3552a87dd2\",\n" +
                "      \"algorithm\" : \"SHA-512\"\n" +
                "    },\n" +
                "    \"size\" : {\n" +
                "      \"type\" : \"IntegerType\",\n" +
                "      \"elementName\" : \"Size\",\n" +
                "      \"value\" : 3307\n" +
                "    },\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"elementName\" : \"FormatIdentification\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatLitteral\",\n" +
                "        \"value\" : \"Plain Text File\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"MimeType\",\n" +
                "        \"value\" : \"text/plain\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatId\",\n" +
                "        \"value\" : \"x-fmt/111\"\n" +
                "      } ]\n" +
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
                "        \"dateTimeString\" : \"2019-09-18T20:04:20.154039Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadata\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID19\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestImport.zip-tmpdir\\\\Root\\\\Node 1\\\\Node 1.1\\\\__TextContent_1__201609-TdB-suivi-des-a.txt\"\n" +
                "  } ],\n" +
                "  \"physicalDataObjectList\" : [ {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"DataObjectVersion\",\n" +
                "      \"value\" : \"PhysicalMaster_1\"\n" +
                "    },\n" +
                "    \"physicalId\" : {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"PhysicalId\",\n" +
                "      \"value\" : \"940 W\"\n" +
                "    },\n" +
                "    \"physicalDimensions\" : {\n" +
                "      \"type\" : \"PhysicalDimensions\",\n" +
                "      \"elementName\" : \"PhysicalDimensions\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Width\",\n" +
                "        \"value\" : 10.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Height\",\n" +
                "        \"value\" : 8.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Depth\",\n" +
                "        \"value\" : 1.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"LinearDimensionType\",\n" +
                "        \"elementName\" : \"Diameter\",\n" +
                "        \"value\" : 0.0,\n" +
                "        \"unit\" : \"centimetre\"\n" +
                "      }, {\n" +
                "        \"type\" : \"Weight\",\n" +
                "        \"elementName\" : \"Weight\",\n" +
                "        \"value\" : 59.0,\n" +
                "        \"unit\" : \"gram\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"otherDimensionsAbstractXml\" : [ ],\n" +
                "    \"inDataObjectPackageId\" : \"ID18\",\n" +
                "    \"onDiskPath\" : null\n" +
                "  } ],\n" +
                "  \"logBook\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID16\",\n" +
                "  \"onDiskPath\" : null\n" +
                "}";
        DataObjectGroup og = zi.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("ID16");
		//System.out.println("Value to verify="+mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og);
        sog = sog.replaceAll("\"dateTimeString\" : .*", "").trim();
        Pattern pog = Pattern.compile("\"onDiskPath\" : .*Node 1.1");
        Matcher msog = pog.matcher(sog);
        boolean sogpath = msog.find();
        sog = TestUtilities.LineEndNormalize(sog.replaceAll("\"onDiskPath\" : .*\"", "")).trim();

        testog = testog.replaceAll("\"dateTimeString\" : .*", "").trim();
        Matcher mtestog = pog.matcher(testog);
        boolean testogpath = mtestog.find();
        testog = TestUtilities.LineEndNormalize(testog.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertTrue(sogpath & testogpath);
        assertThat(sog).isEqualTo(testog);

        // assert one archiveUnit using serialization
        String testau = "{\n" +
                "  \"archiveUnitProfileXmlData\" : null,\n" +
                "  \"managementXmlData\" : \"<Management>\\r\\n  <AccessRule>\\r\\n    <Rule>ACC-00002</Rule>\\r\\n    <StartDate>2015-11-19</StartDate>\\r\\n  </AccessRule>\\r\\n</Management>\",\n" +
                "  \"contentXmlData\" : \"<Content>\\r\\n    <DescriptionLevel>Item</DescriptionLevel>\\r\\n    <Title>CSIC Tech : points remarquables PMO</Title>\\r\\n    <OriginatingSystemId>&lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></OriginatingSystemId>\\r\\n    <Writer>\\r\\n        <FirstName>PLANCHOT Benjamin</FirstName>\\r\\n        <BirthName>PLANCHOT Benjamin</BirthName>\\r\\n        <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\\r\\n    </Writer>\\r\\n    <Addressee>\\r\\n        <FirstName>frederic.deguilhen@culture.gouv.fr</FirstName>\\r\\n        <BirthName>frederic.deguilhen@culture.gouv.fr</BirthName>\\r\\n        <Identifier>frederic.deguilhen@culture.gouv.fr</Identifier>\\r\\n    </Addressee>\\r\\n    <Addressee>\\r\\n        <FirstName>jean-severin.lair@culture.gouv.fr</FirstName>\\r\\n        <BirthName>jean-severin.lair@culture.gouv.fr</BirthName>\\r\\n        <Identifier>jean-severin.lair@culture.gouv.fr</Identifier>\\r\\n    </Addressee>\\r\\n    <Recipient>\\r\\n        <FirstName>PLANCHOT Benjamin</FirstName>\\r\\n        <BirthName>PLANCHOT Benjamin</BirthName>\\r\\n        <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\\r\\n    </Recipient>\\r\\n    <SentDate>2016-08-30T10:14:17Z</SentDate>\\r\\n    <ReceivedDate>2016-08-30T10:14:18Z</ReceivedDate>\\r\\n    <TextContent>Bonjour,\\r\\n\\r\\nVous trouverez ci-joint les éléments collectés au mois de juillet sous forme de tableur correspondant à l'avancement de vos activités. Afin de publier une mise à jour en CSIC Tech, merci de mettre à jour les éléments pour le jeudi 08 septembre au plus tard. Sans retour de votre part, je tiendrai compte de la dernière mise à jour.\\r\\n\\r\\nPour rappel :\\r\\n- L'objectif est de remonter l'état des activités (statut, livrable/jalon, points importants).\\r\\n- Les colonnes de N à V sont à mettre à jour si nécessaire (fond orange clair).\\r\\n\\r\\nMerci par avance.\\r\\n\\r\\nBien cordialement,\\r\\n\\r\\n\\r\\n[http://www.modernisation.gouv.fr/sites/default/files/bloc-sgmap-2.jpg]&lt; http://www.modernisation.gouv.fr/>\\r\\n\\r\\nBenjamin PLANCHOT | PMO\\r\\nService « performance des services numériques »\\r\\nDirection interministérielle du numérique et du système d'information et de communication de l'Etat\\r\\n01 40 15 71 50 | Tour Mirabeau - 39-43 Quai André Citroën, 75015 Paris - Bureau 4027\\r\\nmodernisation.gouv.fr&lt; http://www.modernisation.gouv.fr/></TextContent>\\r\\n</Content>\",\n" +
                "  \"childrenAuList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID15\", \"ID20\" ]\n" +
                "  },\n" +
                "  \"dataObjectRefList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID12\" ]\n" +
                "  },\n" +
                "  \"inDataObjectPackageId\" : \"ID11\",\n" +
                "  \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithoutLinksModelV1\\\\Root\\\\Node 1\"\n" +
                "}";

        Pattern pau = Pattern.compile("\"onDiskPath\" : .*Node 1\"");
        Matcher mtestau = pau.matcher(testau);
        boolean testaupath = mtestau.find();
        testau = TestUtilities.LineEndNormalize(testau.replaceAll("\"onDiskPath\" : .*\"", ""));

        ArchiveUnit au = zi.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID11");
        String sau = mapper.writeValueAsString(au);
        //System.out.println(sau);
        Matcher msau = pau.matcher(sau);
        boolean saupath = msau.find();
        sau = TestUtilities.LineEndNormalize(sau.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertThat(saupath).isEqualTo(testaupath);
        assertThat(sau).isEqualTo(testau);
    }
}
