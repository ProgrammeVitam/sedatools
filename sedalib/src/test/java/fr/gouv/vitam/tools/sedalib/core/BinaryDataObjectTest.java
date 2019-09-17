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
        //System.out.println("Value to verify=" + bdoOut);

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
                "  \"dataObjectVersion\" : {\n" +
                "    \"type\" : \"StringType\",\n" +
                "    \"elementName\" : \"DataObjectVersion\",\n" +
                "    \"value\" : \"BinaryMaster_1\"\n" +
                "  },\n" +
                "  \"uri\" : {\n" +
                "    \"type\" : \"StringType\",\n" +
                "    \"elementName\" : \"Uri\",\n" +
                "    \"value\" : \"content/ID7.jpg\"\n" +
                "  },\n" +
                "  \"messageDigest\" : {\n" +
                "    \"type\" : \"DigestType\",\n" +
                "    \"elementName\" : \"MessageDigest\",\n" +
                "    \"value\" : \"e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3\",\n" +
                "    \"algorithm\" : \"SHA-512\"\n" +
                "  },\n" +
                "  \"size\" : {\n" +
                "    \"type\" : \"IntegerType\",\n" +
                "    \"elementName\" : \"Size\",\n" +
                "    \"value\" : 21232\n" +
                "  },\n" +
                "  \"compressed\" : null,\n" +
                "  \"formatIdentification\" : {\n" +
                "    \"type\" : \"FormatIdentification\",\n" +
                "    \"elementName\" : \"FormatIdentification\",\n" +
                "    \"metadataList\" : [ {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"FormatLitteral\",\n" +
                "      \"value\" : \"Exchangeable Image File Format (Compressed)\"\n" +
                "    }, {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"MimeType\",\n" +
                "      \"value\" : \"image/jpeg\"\n" +
                "    }, {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"FormatId\",\n" +
                "      \"value\" : \"fmt/645\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"fileInfo\" : {\n" +
                "    \"type\" : \"FileInfo\",\n" +
                "    \"elementName\" : \"FileInfo\",\n" +
                "    \"metadataList\" : [ {\n" +
                "      \"type\" : \"StringType\",\n" +
                "      \"elementName\" : \"Filename\",\n" +
                "      \"value\" : \"image001.jpg\"\n" +
                "    }, {\n" +
                "      \"type\" : \"DateTimeType\",\n" +
                "      \"elementName\" : \"LastModified\",\n" +
                "      \"dateTimeString\" : \"2018-08-28T19:22:19Z\"\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"metadataXmlData\" : null,\n" +
                "  \"otherMetadataXmlData\" : null,\n" +
                "  \"inDataObjectPackageId\" : \"ID7\",\n" +
                "  \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestSIP.zip-tmpdir\\\\content\\\\ID7.jpg\"\n" +
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
                "    <LastModified>2018-08-28T19:22:19</LastModified>\n" +
                "  </FileInfo>";
        testOut = LineEndNormalize(testOut);
        bdoNextOut = LineEndNormalize(bdoNextOut);
        assertThat(bdoNextOut).isEqualTo(testOut);

    }
}
