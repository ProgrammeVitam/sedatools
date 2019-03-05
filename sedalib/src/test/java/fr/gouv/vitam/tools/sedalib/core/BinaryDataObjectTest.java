package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.LineEndNormalize;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class BinaryDataObjectTest {

    @Test
    void testJson() throws SEDALibException, InterruptedException, IOException {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/TestSip.zip", "target/tmpJunit/TestSIP.zip-tmpdir", null);
        si.doImport();
        BinaryDataObject bdo = si.getArchiveTransfer().getDataObjectPackage().getBdoInDataObjectPackageIdMap()
                .get("ID7");

        String bdoOut = mapper.writeValueAsString(bdo);
//        System.out.println("Value to verify=" + bdoOut);

        // When test read write in Json string format
        BinaryDataObject bdoNext = mapper.readValue(bdoOut, BinaryDataObject.class);
        String bdoNextOut = mapper.writeValueAsString(bdoNext);

        // Then
        String testOut = "{\n" +
                "  \"dataObjectSystemId\" : null,\n" +
                "  \"dataObjectGroupSystemId\" : null,\n" +
                "  \"relationshipsXmlData\" : [ ],\n" +
                "  \"dataObjectGroupReferenceId\" : null,\n" +
                "  \"dataObjectGroupId\" : null,\n" +
                "  \"dataObjectVersion\" : \"BinaryMaster_1\",\n" +
                "  \"uri\" : \"content/ID7.jpg\",\n" +
                "  \"messageDigest\" : \"e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3\",\n" +
                "  \"messageDigestAlgorithm\" : \"SHA-512\",\n" +
                "  \"size\" : 21232,\n" +
                "  \"compressed\" : null,\n" +
                "  \"formatIdentification\" : {\n" +
                "    \"formatLitteral\" : \"Exchangeable Image File Format (Compressed)\",\n" +
                "    \"mimeType\" : \"image/jpeg\",\n" +
                "    \"formatId\" : \"fmt/645\",\n" +
                "    \"encoding\" : null\n" +
                "  },\n" +
                "  \"fileInfo\" : {\n" +
                "    \"filename\" : \"image001.jpg\",\n" +
                "    \"creatingApplicationName\" : null,\n" +
                "    \"creatingApplicationVersion\" : null,\n" +
                "    \"dateCreatedByApplication\" : null,\n" +
                "    \"creatingOs\" : null,\n" +
                "    \"creatingOsVersion\" : null,\n" +
                "    \"lastModified\" : 1535484139000\n" +
                "  },\n" +
                "  \"metadataXmlData\" : null,\n" +
                "  \"otherMetadataXmlData\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID7\",\n" +
                "  \"onDiskPath\" : \"C:\\\\Users\\\\jean-severin.lair\\\\intelliJGit\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSip.zip-tmpdir\\\\content\\\\ID7.jpg\"\n" +
                "}";
        testOut = LineEndNormalize(testOut.replaceAll("\"onDiskPath\" : .*\"", ""));
        bdoNextOut = LineEndNormalize(bdoNextOut.replaceAll("\"onDiskPath\" : .*\"", ""));
        assertThat(bdoNextOut).isEqualTo(testOut);

    }

    @Test
    void testXMLFragment() throws SEDALibException, InterruptedException {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
                "src/test/resources/PacketSamples/TestSip.zip", "target/tmpJunit/TestSIP.zip-tmpdir", null);
        si.doImport();
        BinaryDataObject bdo = si.getArchiveTransfer().getDataObjectPackage().getBdoInDataObjectPackageIdMap()
                .get("ID7");


        // When test read write fragments in XML string format
        String bdoOut = bdo.toSedaXmlFragments();
        BinaryDataObject bdoNext = new BinaryDataObject(si.getArchiveTransfer().getDataObjectPackage());
        bdoNext.fromSedaXmlFragments(bdoOut);
        String bdoNextOut = bdoNext.toSedaXmlFragments();

        // Then
        String testOut = "<DataObjectVersion>BinaryMaster_1</DataObjectVersion>\n" +
                "  <Uri>content/ID52.jpg</Uri>\n" +
                "  <MessageDigest algorithm=\"SHA-512\">e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3</MessageDigest>\n" +
                "  <Size>21232</Size>\n" +
                "  <FormatIdentification>\n" +
                "    <FormatLitteral>Exchangeable Image File Format (Compressed)</FormatLitteral>\n" +
                "    <MimeType>image/jpeg</MimeType>\n" +
                "    <FormatId>fmt/645</FormatId>\n" +
                "  </FormatIdentification>\n" +
                "  <FileInfo>\n" +
                "    <Filename>image001.jpg</Filename>\n" +
                "    <LastModified>2018-08-28T19:22:19Z</LastModified>\n" +
                "  </FileInfo>";
        testOut = LineEndNormalize(testOut);
        bdoNextOut = LineEndNormalize(bdoNextOut);
        assertThat(bdoNextOut).isEqualTo(testOut);

    }
}
