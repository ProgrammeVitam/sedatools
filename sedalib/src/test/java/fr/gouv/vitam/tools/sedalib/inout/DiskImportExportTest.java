package fr.gouv.vitam.tools.sedalib.inout;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.vitam.tools.sedalib.TestUtilities;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToDiskExporter;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;

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
                "    \"dataObjectVersion\" : \"BinaryMaster_1\",\n" +
                "    \"uri\" : null,\n" +
                "    \"messageDigest\" : \"ccc63de7306ced0b656f8f5bcb718304fefa93baed5bdb6e523146ff9ff9795ad22fff6077110fbd171df9553a24554fd5aa2b72cf76ffb4c24c7371be5f774e\",\n" +
                "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "    \"size\" : 50651,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"formatLitteral\" : \"OpenDocument Spreadsheet\",\n" +
                "      \"mimeType\" : \"application/vnd.oasis.opendocument.spreadsheet\",\n" +
                "      \"formatId\" : \"fmt/294\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"filename\" : \"201609-TdB-suivi-des-a.ods\",\n" +
                "      \"creatingApplicationName\" : null,\n" +
                "      \"creatingApplicationVersion\" : null,\n" +
                "      \"creatingOs\" : null,\n" +
                "      \"creatingOsVersion\" : null,\n" +
                "      \n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID14\",\n" +
                "    \"onDiskPath\" : \"C:\\\\Users\\\\jean-severin.lair\\\\intelliJGit\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithoutLinksModelV1\\\\Root\\\\Node 1\\\\Node 1.1\\\\__BinaryMaster_1_201609-TdB-suivi-des-a.ods\"\n" +
                "  }, {\n" +
                "    \"dataObjectSystemId\" : null,\n" +
                "    \"dataObjectGroupSystemId\" : null,\n" +
                "    \"relationshipsXmlData\" : [ ],\n" +
                "    \"dataObjectGroupReferenceId\" : null,\n" +
                "    \"dataObjectGroupId\" : null,\n" +
                "    \"dataObjectVersion\" : \"TextContent_1\",\n" +
                "    \"uri\" : null,\n" +
                "    \"messageDigest\":\"7040a2d9f0a4ba697fde735cbe12f462af609eda6e35a0f3ddbddddbdaf8ffdd394c37a59bbb8ea4238f13169e0d634fa75cf3b251c4607144010d3552a87dd2\",\n" +
                "    \"messageDigestAlgorithm\":\"SHA-512\",\n" +
                "    \"size\":3307,\n" +
                "    \"compressed\" : null,\n" +
                "    \"formatIdentification\" : {\n" +
                "      \"formatLitteral\" : \"Plain Text File\",\n" +
                "      \"mimeType\" : \"text/plain\",\n" +
                "      \"formatId\" : \"x-fmt/111\",\n" +
                "      \"encoding\" : null\n" +
                "    },\n" +
                "    \"fileInfo\" : {\n" +
                "      \"filename\" : \"201609-TdB-suivi-des-a.txt\",\n" +
                "      \"creatingApplicationName\" : null,\n" +
                "      \"creatingApplicationVersion\" : null,\n" +
                "      \"creatingOs\" : null,\n" +
                "      \"creatingOsVersion\" : null,\n" +
                "      \n" +
                "    },\n" +
                "    \"metadataXmlData\" : null,\n" +
                "    \"otherMetadataXmlData\" : null,\n" +
                "    \"inDataObjectPackageId\" : \"ID15\",\n" +
                "    \"onDiskPath\" : \"C:\\\\Users\\\\jean-severin.lair\\\\intelliJGit\\\\sedatools\\\\sedalib\\\\src\\\\test\\\\resources\\\\PacketSamples\\\\SampleWithoutLinksModelV1\\\\Root\\\\Node 1\\\\Node 1.1\\\\__TextContent_1_201609-TdB-suivi-des-a.txt\"\n" +
                "  } ],\n" +
                "  \"physicalDataObjectList\" : [ ],\n" +
                "  \"logBookXmlData\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID13\",\n" +
                "  \"onDiskPath\" : null\n" +
                "}";
        DataObjectGroup og = di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("ID13");
//		System.out.println(mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og).replaceAll("\"lastModified\" : .*", "");
        Pattern pog = Pattern.compile("\"onDiskPath\" : .*Node 1.1");
        Matcher msog = pog.matcher(sog);
        boolean sogpath = msog.find();
        sog = TestUtilities.LineEndNormalize(sog.replaceAll("\"onDiskPath\" : .*\"", ""));

        testog = testog.replaceAll("\"lastModified\" : .*", "");
        Matcher mtestog = pog.matcher(testog);
        boolean testogpath = mtestog.find();
        testog = TestUtilities.LineEndNormalize(testog.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertTrue(sogpath & testogpath);
        assertEquals(sog, testog);

        // assert one archiveUnit using serialization
        String testau = "{\n" +
                "  \"archiveUnitProfileXmlData\" : null,\n" +
                "  \"managementXmlData\" : \"<Management>\\r\\n  <AccessRule>\\r\\n    <Rule>ACC-00002</Rule>\\r\\n    <StartDate>2015-11-19</StartDate>\\r\\n  </AccessRule>\\r\\n</Management>\",\n" +
                "  \"contentXmlData\" : \"<Content>  <DescriptionLevel>Item</DescriptionLevel>  <Title>CSIC Tech : points remarquables PMO</Title>  <OriginatingSystemId>&lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></OriginatingSystemId>  <Writer>    <FirstName>PLANCHOT Benjamin</FirstName>    <BirthName>PLANCHOT Benjamin</BirthName>    <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>  </Writer>  <Addressee>    <FirstName>frederic.deguilhen@culture.gouv.fr</FirstName>    <BirthName>frederic.deguilhen@culture.gouv.fr</BirthName>    <Identifier>frederic.deguilhen@culture.gouv.fr</Identifier>  </Addressee>  <Addressee>    <FirstName>jean-severin.lair@culture.gouv.fr</FirstName>    <BirthName>jean-severin.lair@culture.gouv.fr</BirthName>    <Identifier>jean-severin.lair@culture.gouv.fr</Identifier>  </Addressee>  <Recipient>    <FirstName>PLANCHOT Benjamin</FirstName>    <BirthName>PLANCHOT Benjamin</BirthName>    <Identifier>benjamin.planchot@modernisation.gouv.fr</Identifier>  </Recipient>  <SentDate>2016-08-30T10:14:17</SentDate>  <ReceivedDate>2016-08-30T10:14:18</ReceivedDate>  <TextContent>Bonjour,Vous trouverez ci-joint les éléments collectés au mois de juillet sous forme de tableur correspondant à l'avancement de vos activités. Afin de publier une mise à jour en CSIC Tech, merci de mettre à jour les éléments pour le jeudi 08 septembre au plus tard. Sans retour de votre part, je tiendrai compte de la dernière mise à jour.Pour rappel :- L'objectif est de remonter l'état des activités (statut, livrable/jalon, points importants).- Les colonnes de N à V sont à mettre à jour si nécessaire (fond orange clair).Merci par avance.Bien cordialement,[http://www.modernisation.gouv.fr/sites/default/files/bloc-sgmap-2.jpg]&lt; http://www.modernisation.gouv.fr/&gt;Benjamin PLANCHOT | PMOService « performance des services numériques »Direction interministérielle du numérique et du système d'information et de communication de l'Etat01 40 15 71 50 | Tour Mirabeau - 39-43 Quai André Citroën, 75015 Paris - Bureau 4027modernisation.gouv.fr&lt; http://www.modernisation.gouv.fr/&gt;</TextContent></Content>\",\n" +
                "  \"childrenAuList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID12\", \"ID16\" ]\n" +
                "  },\n" +
                "  \"dataObjectRefList\" : {\n" +
                "    \"inDataObjectPackageIdList\" : [ \"ID19\" ]\n" +
                "  },\n" +
                "  \"inDataObjectPackageId\" : \"ID11\",\n" +
                "  \"onDiskPath\" : \"/home/js/GitTemp/sedalib/src/test/ressources/PacketSamples/SampleWithoutLinksModelV1/Root/Node 1\"\n" +
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

        assertTrue(saupath & testaupath);
        assertEquals(testau, sau);
    }

    @Test
    public void TestDiskImportWithLink() throws Exception {

        // do import of test directory
        DiskToArchiveTransferImporter di;
//		if (System.getProperty("os.name").toLowerCase().contains("win"))
//			di = new DiskToArchiveTransferImporter("src/test/ressources/PacketSamples/SampleWithWindowsLinksAndShortcutsModelV2",spl);
//		else
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
        String testog = "{\n" + "  \"binaryDataObjectList\" : [ {\n" + "    \"dataObjectSystemId\" : null,\n"
                + "    \"dataObjectGroupSystemId\" : null,\n" + "    \"relationshipsXmlData\" : [ ],\n"
                + "    \"dataObjectGroupReferenceId\" : null,\n" + "    \"dataObjectGroupId\" : null,\n"
                + "    \"dataObjectVersion\" : \"BinaryMaster_1\",\n" + "    \"uri\" : null,\n"
                + "    \"messageDigest\" : \"e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3\",\n"
                + "    \"messageDigestAlgorithm\" : \"SHA-512\",\n" + "    \"size\" : 21232,\n"
                + "    \"compressed\" : null,\n" + "    \"formatIdentification\" : {\n"
                + "      \"formatLitteral\" : \"Exchangeable Image File Format (Compressed)\",\n"
                + "      \"mimeType\" : \"image/jpeg\",\n" + "      \"formatId\" : \"fmt/645\",\n"
                + "      \"encoding\" : null\n" + "    },\n" + "    \"fileInfo\" : {\n"
                + "      \"filename\" : \"image001.jpg\",\n" + "      \"creatingApplicationName\" : null,\n"
                + "      \"creatingApplicationVersion\" : null,\n" + "      \"creatingOs\" : null,\n"
                + "      \"creatingOsVersion\" : null,\n" + "      \"lastModified\" : 1535484139000\n" + "    },\n"
                + "    \"metadataXmlData\" : null,\n" + "    \"otherMetadataXmlData\" : null,\n"
                + "    \"inDataObjectPackageId\" : \"ID13\",\n"
                + "    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\src\\\\test\\\\ressources\\\\PacketSamples\\\\SampleWithWindowsLinksAndShortcutsModelV2\\\\Root\\\\Node 1\\\\Node 1.2\\\\__BinaryMaster_1_image001.jpg\"\n"
                + "  } ],\n" + "  \"physicalDataObjectList\" : [ ],\n" + "  \"logBookXmlData\" : null,\n"
                + "  \"inDataObjectPackageId\" : \"ID12\",\n" + "  \"onDiskPath\" : null\n" + "}";

        DataObjectGroup og = di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("ID12");
//		System.out.println(mapper.writeValueAsString(og));
        String sog = mapper.writeValueAsString(og).replaceAll("\"lastModified\" : .*", "");
        Pattern pog = Pattern.compile("\"onDiskPath\" : .*Node 1.2");
        Matcher msog = pog.matcher(sog);
        boolean sogpath = msog.find();
        sog = TestUtilities.LineEndNormalize(sog.replaceAll("\"onDiskPath\" : .*\"", ""));

        testog = testog.replaceAll("\"lastModified\" : .*", "");
        Matcher mtestog = pog.matcher(testog);
        boolean testogpath = mtestog.find();
        testog = TestUtilities.LineEndNormalize(testog.replaceAll("\"onDiskPath\" : .*\"", ""));

        assertEquals(sog, testog);
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

    @Test
    public void TestDiskImportExport() throws Exception {

        // create jackson object mapper
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/TestSipWrongDogReferences.zip", "target/tmpJunit", null);
        si.doImport();

        ArchiveTransferToDiskExporter atde = new ArchiveTransferToDiskExporter(si.getArchiveTransfer(), null);
        eraseAll("target/tmpJunit/SWLMV2");
        atde.doExport("target/tmpJunit/SWLMV2");

        // do import of test directory
        DiskToArchiveTransferImporter dai1;
        dai1 = new DiskToArchiveTransferImporter("target/tmpJunit/SWLMV2", null);

        dai1.addIgnorePattern("Thumbs.db");
        dai1.addIgnorePattern("pagefile.sys");
        dai1.doImport();

        //fix date for future test
        dai1.getArchiveTransfer().getGlobalMetadata().date = "2018-09-30T14:33:24";
        dai1.getArchiveTransfer().getGlobalMetadata().setNowFlag(false);

        ArchiveTransferToSIPExporter attse;
        attse = new ArchiveTransferToSIPExporter(dai1.getArchiveTransfer(), null);
        attse.doExportToSEDAXMLManifest("target/tmpJunit/SWLMV2.xml", true, true);

        // assert macro results
        assertEquals(dai1.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size(), 22);
        assertEquals(dai1.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size(), 11);

        // do export of test directory
        ArchiveTransferToDiskExporter adi;
        adi = new ArchiveTransferToDiskExporter(dai1.getArchiveTransfer(), null);
        eraseAll("target/tmpJunit/SWLMV2.1");
        adi.doExport("target/tmpJunit/SWLMV2.1");

        // do reimport of test directory
        DiskToArchiveTransferImporter dai2;
        dai2 = new DiskToArchiveTransferImporter("target/tmpJunit/SWLMV2.1", null);

        dai2.addIgnorePattern("Thumbs.db");
        dai2.addIgnorePattern("pagefile.sys");
        dai2.doImport();

        attse = new ArchiveTransferToSIPExporter(dai2.getArchiveTransfer(), null);
        attse.doExportToSEDAXMLManifest("target/tmpJunit/SWLMV2.1.xml", true, true);

        String gm1 = dai1.getArchiveTransfer().getGlobalMetadata().toSedaXmlFragments();
        String gm2 = dai2.getArchiveTransfer().getGlobalMetadata().toSedaXmlFragments();

        assertEquals(gm1, gm2);

        assertTrue(FileUtils.contentEquals(new File("target/tmpJunit/SWLMV2.1.xml"), new File("target/tmpJunit/SWLMV2.xml")));
    }
}