package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListInterface;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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
                "  \"metadataList\" : [ {\n" +
                "    \"type\" : \"StringType\",\n" +
                "    \"elementName\" : \"DataObjectVersion\",\n" +
                "    \"value\" : \"BinaryMaster_1\"\n" +
                "  }, {\n" +
                "    \"type\" : \"StringType\",\n" +
                "    \"elementName\" : \"Uri\",\n" +
                "    \"value\" : \"content/ID7.jpg\"\n" +
                "  }, {\n" +
                "    \"type\" : \"DigestType\",\n" +
                "    \"elementName\" : \"MessageDigest\",\n" +
                "    \"value\" : \"e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3\",\n" +
                "    \"algorithm\" : \"SHA-512\"\n" +
                "  }, {\n" +
                "    \"type\" : \"IntegerType\",\n" +
                "    \"elementName\" : \"Size\",\n" +
                "    \"value\" : 21232\n" +
                "  }, {\n" +
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
                "  }, {\n" +
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
                "  }, {\n" +
                "    \"type\" : \"Metadata\",\n" +
                "    \"elementName\" : \"Metadata\",\n" +
                "    \"metadataList\" : [ {\n" +
                "      \"type\" : \"AnyXMLListType\",\n" +
                "      \"elementName\" : \"Image\",\n" +
                "      \"metadataList\" : [ {\n" +
                "        \"type\" : \"AnyXMLType\",\n" +
                "        \"elementName\" : \"Dimensions\",\n" +
                "        \"rawXml\" : \"<Dimensions>117x76</Dimensions>\"\n" +
                "      }, {\n" +
                "        \"type\" : \"AnyXMLType\",\n" +
                "        \"elementName\" : \"Width\",\n" +
                "        \"rawXml\" : \"<Width>117px</Width>\"\n" +
                "      }, {\n" +
                "        \"type\" : \"AnyXMLType\",\n" +
                "        \"elementName\" : \"Height\",\n" +
                "        \"rawXml\" : \"<Height>76px</Height>\"\n" +
                "      }, {\n" +
                "        \"type\" : \"AnyXMLType\",\n" +
                "        \"elementName\" : \"VerticalResolution\",\n" +
                "        \"rawXml\" : \"<VerticalResolution>96ppp</VerticalResolution>\"\n" +
                "      }, {\n" +
                "        \"type\" : \"AnyXMLType\",\n" +
                "        \"elementName\" : \"HorizontalResolution\",\n" +
                "        \"rawXml\" : \"<HorizontalResolution>96ppp</HorizontalResolution>\"\n" +
                "      }, {\n" +
                "        \"type\" : \"AnyXMLType\",\n" +
                "        \"elementName\" : \"ColorDepth\",\n" +
                "        \"rawXml\" : \"<ColorDepth>24</ColorDepth>\"\n" +
                "      } ]\n" +
                "    } ]\n" +
                "  } ],\n" +
                "  \"inDataObjectPackageId\" : \"ID7\",\n" +
                "  \n" +
                "}";
        testOut = TestUtilities.SlackNormalize(testOut.replaceAll("\"onDiskPath\" : .*\"", ""));
        bdoNextOut = TestUtilities.SlackNormalize(bdoNextOut.replaceAll("\"onDiskPath\" : .*\"", ""));
        assertThat(bdoNextOut).isEqualToNormalizingNewlines(testOut);

    }

    @Test
    void testXMLFragment() throws SEDALibException, InterruptedException, FileNotFoundException {
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

        // When dataObjectProfile is defined in SEDA 2.1 can't add
        assertThatThrownBy(() ->
                bdo.addMetadata(new StringType("DataObjectProfile", "Test"))).isInstanceOf(SEDALibException.class)
                .hasMessageContaining("Impossible d'étendre le schéma");
        bdo.removeFirstNamedMetadata("DataObjectProfile");

        // When test read write fragments in XML string format
        String bdoOut = bdo.toSedaXmlFragments();
        BinaryDataObject bdoNext = new BinaryDataObject(si.getArchiveTransfer().getDataObjectPackage());
        bdoNext.fromSedaXmlFragments(bdoOut);
        String bdoNextOut = bdoNext.toSedaXmlFragments();

        // Then
        assertThat(bdoNextOut).isEqualToIgnoringWhitespace(ResourceUtils.getResourceAsString("import/binary_data_object_ID7.xml"));
    }

    @Test
    void testXMLFragmentForSedaVersion2() throws SEDALibException, InterruptedException, FileNotFoundException {

        // Given
        SEDA2Version.setSeda2Version(2);
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
        bdo.addMetadata(new StringType("DataObjectProfile", "Test"));

        // When test read write fragments in XML string format
        String bdoOut = bdo.toSedaXmlFragments();
        BinaryDataObject bdoNext = new BinaryDataObject(si.getArchiveTransfer().getDataObjectPackage());
        bdoNext.fromSedaXmlFragments(bdoOut);
        String bdoNextOut = bdoNext.toSedaXmlFragments();

        // Then
        assertThat(bdoNextOut).isEqualToIgnoringWhitespace(ResourceUtils.getResourceAsString("import/binary_data_object_ID7_seda2.2.xml"));
        SEDA2Version.setSeda2Version(1);
    }

    @Test
    void testXMLFragmentForSedaVersion3() throws SEDALibException, InterruptedException, FileNotFoundException {

        // Given
        SEDA2Version.setSeda2Version(3);
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
        bdo.addMetadata(new StringType("DataObjectProfile", "Test"));
        bdo.addMetadata(new PersistentIdentifier("PType","POrigin", "PReference", "PContent"));
        bdo.addMetadata(new StringType("DataObjectUse","BinaryMaster"));
        bdo.addMetadata(new IntegerType("DataObjectNumber",1));

        // When test read write fragments in XML string format
        String bdoOut = bdo.toSedaXmlFragments();
        BinaryDataObject bdoNext = new BinaryDataObject(si.getArchiveTransfer().getDataObjectPackage());
        bdoNext.fromSedaXmlFragments(bdoOut);
        String bdoNextOut = bdoNext.toSedaXmlFragments();

        // Then
        assertThat(bdoNextOut).isEqualToIgnoringWhitespace(ResourceUtils.getResourceAsString("import/binary_data_object_ID7_seda2.3.xml"));
        SEDA2Version.setSeda2Version(1);
    }
}
