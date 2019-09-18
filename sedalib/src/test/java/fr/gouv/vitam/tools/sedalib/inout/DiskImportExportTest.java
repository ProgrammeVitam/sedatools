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
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
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

public class DiskImportExportTest implements UseTestFiles {

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

    @Test
    public void TestDiskImportWithoutLink() throws Exception {

        // do import of test directory
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
                "src/test/resources/PacketSamples/SampleWithoutLinksModelV1", null);
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // assert macro results
        assertEquals(22, di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size());
        assertEquals(11, di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size());

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
                "        \"dateTimeString\" : \"2019-01-16T20:03:35.841171Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID14\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithoutLinksModelV1\\\\Root\\\\Node 1\\\\Node 1.1\\\\__BinaryMaster_1_201609-TdB-suivi-des-a.ods\"\n" +
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
                "        \"dateTimeString\" : \"2019-01-16T20:03:35.842169Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID15\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithoutLinksModelV1\\\\Root\\\\Node 1\\\\Node 1.1\\\\__TextContent_1_201609-TdB-suivi-des-a.txt\"\n" +
                "  } ],\n" +
                "  \"physicalDataObjectList\" : [ ],\n" +
                "  \"logBook\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID13\",\n" +
                "  \"onDiskPath\" : null\n" +
                "}";
        DataObjectGroup og = di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("ID13");
		//System.out.println("Value to verify="+mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og).replaceAll("\"dateTimeString\" : .*", "");
        Pattern pog = Pattern.compile("\"onDiskPath\" : .*Node 1.1");
        Matcher msog = pog.matcher(sog);
        boolean sogpath = msog.find();
        sog = TestUtilities.LineEndNormalize(sog.replaceAll("\"onDiskPath\" : .*\"", ""));

        testog = testog.replaceAll("\"dateTimeString\" : .*", "");
        Matcher mtestog = pog.matcher(testog);
        boolean testogpath = mtestog.find();
        testog = TestUtilities.LineEndNormalize(testog.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertTrue(sogpath & testogpath);
        assertThat(sog).isEqualTo(testog);

        // assert one archiveUnit using serialization
        String testau = "{\n" +
                "  \"archiveUnitProfileXmlData\" : null,\n" +
                "  \"managementXmlData\" : \"<Management>\\r\\n  <AccessRule>\\r\\n    <Rule>ACC-00002</Rule>\\r\\n    <StartDate>2015-11-19</StartDate>\\r\\n  </AccessRule>\\r\\n</Management>\",\n" +
                "  \"contentXmlData\" : \"<Content>\\r\\n    <DescriptionLevel>Item</DescriptionLevel>\\r\\n    <Title>CSIC Tech : points remarquables PMO</Title>\\r\\n    <OriginatingSystemId>&lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize&gt;</OriginatingSystemId>\\r\\n    <Writer>\\r\\n        <FirstName>PLANCHOT Benjamin</FirstName>\\r\\n        <BirthName>PLANCHOT Benjamin</BirthName>\\r\\n        <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\\r\\n    </Writer>\\r\\n    <Addressee>\\r\\n        <FirstName>frederic.deguilhen@culture.gouv.fr</FirstName>\\r\\n        <BirthName>frederic.deguilhen@culture.gouv.fr</BirthName>\\r\\n        <Identifier>frederic.deguilhen@culture.gouv.fr</Identifier>\\r\\n    </Addressee>\\r\\n    <Addressee>\\r\\n        <FirstName>jean-severin.lair@culture.gouv.fr</FirstName>\\r\\n        <BirthName>jean-severin.lair@culture.gouv.fr</BirthName>\\r\\n        <Identifier>jean-severin.lair@culture.gouv.fr</Identifier>\\r\\n    </Addressee>\\r\\n    <Recipient>\\r\\n        <FirstName>PLANCHOT Benjamin</FirstName>\\r\\n        <BirthName>PLANCHOT Benjamin</BirthName>\\r\\n        <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>\\r\\n    </Recipient>\\r\\n    <SentDate>2016-08-30T10:14:17Z</SentDate>\\r\\n    <ReceivedDate>2016-08-30T10:14:18Z</ReceivedDate>\\r\\n    <TextContent>Bonjour,\\r\\n\\r\\nVous trouverez ci-joint les éléments collectés au mois de juillet sous forme de tableur correspondant à l&apos;avancement de vos activités. Afin de publier une mise à jour en CSIC Tech, merci de mettre à jour les éléments pour le jeudi 08 septembre au plus tard. Sans retour de votre part, je tiendrai compte de la dernière mise à jour.\\r\\n\\r\\nPour rappel :\\r\\n- L&apos;objectif est de remonter l&apos;état des activités (statut, livrable/jalon, points importants).\\r\\n- Les colonnes de N à V sont à mettre à jour si nécessaire (fond orange clair).\\r\\n\\r\\nMerci par avance.\\r\\n\\r\\nBien cordialement,\\r\\n\\r\\n\\r\\n[http://www.modernisation.gouv.fr/sites/default/files/bloc-sgmap-2.jpg]&lt; http://www.modernisation.gouv.fr/&gt;\\r\\n\\r\\nBenjamin PLANCHOT | PMO\\r\\nService « performance des services numériques »\\r\\nDirection interministérielle du numérique et du système d&apos;information et de communication de l&apos;Etat\\r\\n01 40 15 71 50 | Tour Mirabeau - 39-43 Quai André Citroën, 75015 Paris - Bureau 4027\\r\\nmodernisation.gouv.fr&lt; http://www.modernisation.gouv.fr/&gt;</TextContent>\\r\\n</Content>\",\n" +
                "  \"childrenAuList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID12\", \"ID16\" ]\n" +
                "  },\n" +
                "  \"dataObjectRefList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID19\" ]\n" +
                "  },\n" +
                "  \"inDataObjectPackageId\" : \"ID11\",\n" +
                "  \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithoutLinksModelV1\\\\Root\\\\Node 1\"\n" +
                "}";

        Pattern pau = Pattern.compile("\"onDiskPath\" : .*Node 1\"");
        Matcher mtestau = pau.matcher(testau);
        boolean testaupath = mtestau.find();
        testau = TestUtilities.LineEndNormalize(testau.replaceAll("\"onDiskPath\" : .*\"", ""));

        ArchiveUnit au = di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID11");
        String sau = mapper.writeValueAsString(au);
        //System.out.println(sau);
        Matcher msau = pau.matcher(sau);
        boolean saupath = msau.find();
        sau = TestUtilities.LineEndNormalize(sau.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertThat(saupath).isEqualTo(testaupath);
        assertThat(sau).isEqualTo(testau);
    }

    @Test
    public void TestDiskImportWithLink() throws Exception {

        // do import of test directory
        DiskToArchiveTransferImporter di;
        di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithLinksModelV2", null);

        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // assert macro results
        assertEquals(di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size(), 22);
        assertEquals(di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size(), 11);

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
                "      \"value\" : \"e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3\",\n" +
                "      \"algorithm\" : \"SHA-512\"\n" +
                "    },\n" +
                "    \"size\" : {\n" +
                "      \"type\" : \"IntegerType\",\n" +
                "      \"elementName\" : \"Size\",\n" +
                "      \"value\" : 21232\n" +
                "    },\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"type\" : \"FormatIdentification\",\n" +
                "      \"elementName\" : \"FormatIdentification\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatLitteral\",\n" +
                "        \"value\" : \"Exchangeable Image File Format (Compressed)\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"MimeType\",\n" +
                "        \"value\" : \"image/jpeg\"\n" +
                "      }, {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"FormatId\",\n" +
                "        \"value\" : \"fmt/645\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"type\" : \"FileInfo\",\n" +
                "      \"elementName\" : \"FileInfo\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"StringType\",\n" +
                "        \"elementName\" : \"Filename\",\n" +
                "        \"value\" : \"image001.jpg\"\n" +
                "      }, {\n" +
                "        \"type\" : \"DateTimeType\",\n" +
                "        \"elementName\" : \"LastModified\",\n" +
                "        \"dateTimeString\" : \"2019-01-16T20:03:35.698553Z\"\n" +
                "      } ]\n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID13\",\n" +
                "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithLinksModelV2\\\\Root\\\\Node 1\\\\Node 1.2\\\\__BinaryMaster_1__image001.jpg\"\n" +
                "  } ],\n" +
                "  \"physicalDataObjectList\" : [ ],\n" +
                "  \"logBook\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID12\",\n" +
                "  \"onDiskPath\" : null\n" +
                "}";

        DataObjectGroup og = di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("ID12");
		//System.out.println("Value to verify="+mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og);
        Pattern pog = Pattern.compile("\"onDiskPath\" : .*Node 1.2");
        Matcher msog = pog.matcher(sog);
        boolean sogpath = msog.find();
        sog = sog.replaceAll("\"onDiskPath\" : .*", "");
        sog = TestUtilities.LineEndNormalize(sog.replaceAll("\"dateTimeString\" : .*\"", ""));

        Matcher mtestog = pog.matcher(testog);
        boolean testogpath = mtestog.find();
        testog = testog.replaceAll("\"onDiskPath\" : .*", "");
        testog = TestUtilities.LineEndNormalize(testog.replaceAll("\"dateTimeString\" : .*\"", ""));

        assertThat(sog).isEqualTo(testog);
        assertTrue(sogpath & testogpath);

        // assert one archiveUnit using serialization
        String testau = "{\n" + "  \"archiveUnitProfileXmlData\" : null,\n" + "  \"managementXmlData\" : null,\n"
                + "  \"contentXmlData\" : \"<Content>\\n  <DescriptionLevel>RecordGrp</DescriptionLevel>\\n  <Title>Node 2.3 - Many</Title>\\n</Content>\",\n"
                + "  \"childrenAuList\" : {\n"
                + "    \"inDataObjectPackageIdList\" : [\"ID43\",\"ID46\",\"ID49\",\"ID16\",\"ID52\",\"ID55\"]\n"
                + "  },\n" + "  \"dataObjectRefList\" : {\n" + "    \"inDataObjectPackageIdList\" : [ ]\n" + "  },\n"
                + "  \"inDataObjectPackageId\" : \"ID40\",\n"
                + "  \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\src\\\\test\\\\ressources\\\\PacketSamples\\\\SampleWithWindowsLinksAndShortcutsModelV2\\\\Root\\\\Node 2\\\\Node 2.3 - Many\"\n"
                + "}";

        Pattern pau = Pattern.compile("\"onDiskPath\" : .*Node 2.3 - Many\"");
        Matcher mtestau = pau.matcher(testau);
        boolean testaupath = mtestau.find();
        testau = TestUtilities.LineEndNormalize(testau.replaceAll("\"onDiskPath\" : .*\"", ""));

        ArchiveUnit au = di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID40");
        String sau = mapper.writeValueAsString(au);
//		System.out.println(sau);
        Matcher msau = pau.matcher(sau);
        boolean saupath = msau.find();
        sau = TestUtilities.LineEndNormalize(sau.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertTrue(saupath & testaupath);
        assertEquals(sau, testau);
    }

    static Function<String,String> replaced = s->"Replaced";

    @Test
    public void TestDiskImportWithLinkIgnoringLinksAndExtractingTitle() throws Exception {

        // do import of test directory
        DiskToArchiveTransferImporter di;
        di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples/SampleWithLinksModelV2",
                true,replaced,null);

        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // assert macro results
        assertEquals(di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size(), 22);
        assertEquals(di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size(), 11);

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // assert one archiveUnit using serialization
        String testau = "{\n" +
                "\"archiveUnitProfileXmlData\":null,\n" +
                "\"managementXmlData\":null,\n" +
                "\"contentXmlData\":\"<Content>  <DescriptionLevel>RecordGrp</DescriptionLevel>  <Title>Replaced</Title></Content>\",\n" +
                "\"childrenAuList\":{\n" +
                "\"inDataObjectPackageIdList\":[]\n" +
                "},\n" +
                "\"dataObjectRefList\":{\n" +
                "\"inDataObjectPackageIdList\":[]\n" +
                "},\n" +
                "\"inDataObjectPackageId\":\"ID56\",\n" +
                "\n" +
                "}";

        testau = TestUtilities.LineEndNormalize(testau.replaceAll("\"onDiskPath\" : .*\"", ""));

        ArchiveUnit au = di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().get("ID56");
        String sau = mapper.writeValueAsString(au);
        sau = TestUtilities.LineEndNormalize(sau.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertEquals(sau, testau);
    }
}
