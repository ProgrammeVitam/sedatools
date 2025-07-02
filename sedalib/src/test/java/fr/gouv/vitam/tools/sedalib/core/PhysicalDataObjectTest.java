package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Execution(value = ExecutionMode.SAME_THREAD,reason= "Can't execute different SedaVersion treatment in parallel")
class PhysicalDataObjectTest {

    @BeforeEach
    void setUp() throws SEDALibException {
        // Reset default seda version
        SEDA2Version.setSeda2Version(1);
    }

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
        PhysicalDataObject pdo = si.getArchiveTransfer().getDataObjectPackage().getPdoInDataObjectPackageIdMap()
                .get("ID18");

        String pdoOut = mapper.writeValueAsString(pdo);
        //System.out.println("Value to verify=" + pdoOut);

        // When test read write in Json string format
        PhysicalDataObject pdoNext = mapper.readValue(pdoOut, PhysicalDataObject.class);
        String pdoNextOut = mapper.writeValueAsString(pdoNext);

        // Then
        String testOut = "{\n" +
                "  \"metadataList\" : [ {\n" +
                "    \"type\" : \"StringType\",\n" +
                "    \"elementName\" : \"DataObjectVersion\",\n" +
                "    \"value\" : \"PhysicalMaster_1\"\n" +
                "  }, {\n" +
                "    \"type\" : \"StringType\",\n" +
                "    \"elementName\" : \"PhysicalId\",\n" +
                "    \"value\" : \"940 W\"\n" +
                "  }, {\n" +
                "    \"type\" : \"PhysicalDimensions\",\n" +
                "    \"elementName\" : \"PhysicalDimensions\",\n" +
                "    \"metadataList\" : [ {\n" +
                "      \"type\" : \"LinearDimensionType\",\n" +
                "      \"elementName\" : \"Width\",\n" +
                "      \"value\" : 10.0,\n" +
                "      \"unit\" : \"centimetre\"\n" +
                "    }, {\n" +
                "      \"type\" : \"LinearDimensionType\",\n" +
                "      \"elementName\" : \"Height\",\n" +
                "      \"value\" : 8.0,\n" +
                "      \"unit\" : \"centimetre\"\n" +
                "    }, {\n" +
                "      \"type\" : \"LinearDimensionType\",\n" +
                "      \"elementName\" : \"Depth\",\n" +
                "      \"value\" : 1.0,\n" +
                "      \"unit\" : \"centimetre\"\n" +
                "    }, {\n" +
                "      \"type\" : \"LinearDimensionType\",\n" +
                "      \"elementName\" : \"Diameter\",\n" +
                "      \"value\" : 0.0,\n" +
                "      \"unit\" : \"centimetre\"\n" +
                "    }, {\n" +
                "      \"type\" : \"Weight\",\n" +
                "      \"elementName\" : \"Weight\",\n" +
                "      \"value\" : 59.0,\n" +
                "      \"unit\" : \"gram\"\n" +
                "    } ]\n" +
                "  }, {\n" +
                "    \"type\" : \"AnyXMLType\",\n" +
                "    \"elementName\" : \"Extent\",\n" +
                "    \"rawXml\" : \"<Extent>1 carte imprimée</Extent>\"\n" +
                "  }, {\n" +
                "    \"type\" : \"AnyXMLType\",\n" +
                "    \"elementName\" : \"Dimensions\",\n" +
                "    \"rawXml\" : \"<Dimensions>10,5cm x 14,8cm</Dimensions>\"\n" +
                "  }, {\n" +
                "    \"type\" : \"AnyXMLType\",\n" +
                "    \"elementName\" : \"Color\",\n" +
                "    \"rawXml\" : \"<Color>Noir et blanc</Color>\"\n" +
                "  }, {\n" +
                "    \"type\" : \"AnyXMLType\",\n" +
                "    \"elementName\" : \"Framing\",\n" +
                "    \"rawXml\" : \"<Framing>Paysage</Framing>\"\n" +
                "  }, {\n" +
                "    \"type\" : \"AnyXMLType\",\n" +
                "    \"elementName\" : \"Technique\",\n" +
                "    \"rawXml\" : \"<Technique>Phototypie</Technique>\"\n" +
                "  } ],\n" +
                "  \"inDataObjectPackageId\" : \"ID18\",\n" +
                "  \"onDiskPath\" : null\n" +
                "}";
        testOut = testOut.replaceAll("\"onDiskPath\" : .*\"", "");
        pdoNextOut = pdoNextOut.replaceAll("\"onDiskPath\" : .*\"", "");
        assertThat(pdoNextOut).isEqualToNormalizingNewlines(testOut);

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

        SEDA2Version.setSeda2Version(1);
        si.doImport();
        PhysicalDataObject pdo = si.getArchiveTransfer().getDataObjectPackage().getPdoInDataObjectPackageIdMap()
                .get("ID18");

        // When dataObjectProfile defined in SEDA 2.1 it's at the end as an extension
        pdo.addSedaXmlFragments("<DataObjectProfile>Test</DataObjectProfile>");
        String pdoOut = pdo.toSedaXmlFragments();

        // Then
        String testOut1 = "  <DataObjectVersion>PhysicalMaster_1</DataObjectVersion>\n" +
                "  <PhysicalId>940 W</PhysicalId>\n" +
                "  <PhysicalDimensions>\n" +
                "    <Width unit=\"centimetre\">10.0</Width>\n" +
                "    <Height unit=\"centimetre\">8.0</Height>\n" +
                "    <Depth unit=\"centimetre\">1.0</Depth>\n" +
                "    <Diameter unit=\"centimetre\">0.0</Diameter>\n" +
                "    <Weight unit=\"gram\">59.0</Weight>\n" +
                "  </PhysicalDimensions>\n" +
                "  <Extent>1 carte imprimée</Extent>\n" +
                "  <Dimensions>10,5cm x 14,8cm</Dimensions>\n" +
                "  <Color>Noir et blanc</Color>\n" +
                "  <Framing>Paysage</Framing>\n" +
                "  <Technique>Phototypie</Technique>\n" +
                "  <DataObjectProfile>Test</DataObjectProfile>";
        assertThat(pdoOut).isEqualToNormalizingNewlines(testOut1);
        pdo.removeFirstNamedMetadata("DataObjectProfile");


        // When test read write fragments in XML string format
        pdoOut = pdo.toSedaXmlFragments();
        PhysicalDataObject pdoNext = new PhysicalDataObject(si.getArchiveTransfer().getDataObjectPackage());
        pdoNext.fromSedaXmlFragments(pdoOut);
        String pdoNextOut = pdoNext.toSedaXmlFragments();

        // Then
        String testOut2 = "  <DataObjectVersion>PhysicalMaster_1</DataObjectVersion>\n" +
                "  <PhysicalId>940 W</PhysicalId>\n" +
                "  <PhysicalDimensions>\n" +
                "    <Width unit=\"centimetre\">10.0</Width>\n" +
                "    <Height unit=\"centimetre\">8.0</Height>\n" +
                "    <Depth unit=\"centimetre\">1.0</Depth>\n" +
                "    <Diameter unit=\"centimetre\">0.0</Diameter>\n" +
                "    <Weight unit=\"gram\">59.0</Weight>\n" +
                "  </PhysicalDimensions>\n" +
                "  <Extent>1 carte imprimée</Extent>\n" +
                "  <Dimensions>10,5cm x 14,8cm</Dimensions>\n" +
                "  <Color>Noir et blanc</Color>\n" +
                "  <Framing>Paysage</Framing>\n" +
                "  <Technique>Phototypie</Technique>";
        assertThat(pdoNextOut).isEqualToNormalizingNewlines(testOut2);
    }

    @Test
    void testXMLFragmentForSedaVersion2() throws SEDALibException, InterruptedException {
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
        PhysicalDataObject pdo = si.getArchiveTransfer().getDataObjectPackage().getPdoInDataObjectPackageIdMap()
                .get("ID18");
        pdo.addNewMetadata("DataObjectProfile", "Test");

        // When test read write fragments in XML string format
        String pdoOut = pdo.toSedaXmlFragments();
        PhysicalDataObject pdoNext = new PhysicalDataObject(si.getArchiveTransfer().getDataObjectPackage());
        pdoNext.fromSedaXmlFragments(pdoOut);
        String pdoNextOut = pdoNext.toSedaXmlFragments();

        // Then
        String testOut = "  <DataObjectProfile>Test</DataObjectProfile>\n" +
                "  <DataObjectVersion>PhysicalMaster_1</DataObjectVersion>\n" +
                "  <PhysicalId>940 W</PhysicalId>\n" +
                "  <PhysicalDimensions>\n" +
                "    <Width unit=\"centimetre\">10.0</Width>\n" +
                "    <Height unit=\"centimetre\">8.0</Height>\n" +
                "    <Depth unit=\"centimetre\">1.0</Depth>\n" +
                "    <Diameter unit=\"centimetre\">0.0</Diameter>\n" +
                "    <Weight unit=\"gram\">59.0</Weight>\n" +
                "  </PhysicalDimensions>\n" +
                "  <Extent>1 carte imprimée</Extent>\n" +
                "  <Dimensions>10,5cm x 14,8cm</Dimensions>\n" +
                "  <Color>Noir et blanc</Color>\n" +
                "  <Framing>Paysage</Framing>\n" +
                "  <Technique>Phototypie</Technique>";
        assertThat(pdoNextOut).isEqualToNormalizingNewlines(testOut);
        SEDA2Version.setSeda2Version(1);
    }

    @Test
    void testXMLFragmentForSedaVersion3() throws SEDALibException, InterruptedException {
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
        PhysicalDataObject pdo = si.getArchiveTransfer().getDataObjectPackage().getPdoInDataObjectPackageIdMap()
                .get("ID18");
        pdo.addNewMetadata("DataObjectProfile", "Test");
        pdo.addMetadata(new PersistentIdentifier("PType", "POrigin", "PReference", "PContent"));
        pdo.addMetadata(new StringType("DataObjectUse", "PhysicalMaster"));
        pdo.addMetadata(new IntegerType("DataObjectNumber", 1));

        // When test read write fragments in XML string format
        String pdoOut = pdo.toSedaXmlFragments();
        PhysicalDataObject pdoNext = new PhysicalDataObject(si.getArchiveTransfer().getDataObjectPackage());
        pdoNext.fromSedaXmlFragments(pdoOut);
        String pdoNextOut = pdoNext.toSedaXmlFragments();

        // Then
        String testOut = "  <DataObjectProfile>Test</DataObjectProfile>\n" +
                "  <DataObjectVersion>PhysicalMaster_1</DataObjectVersion>\n" +
                "  <PersistentIdentifier>\n" +
                "    <PersistentIdentifierType>PType</PersistentIdentifierType>\n" +
                "    <PersistentIdentifierOrigin>POrigin</PersistentIdentifierOrigin>\n" +
                "    <PersistentIdentifierReference>PReference</PersistentIdentifierReference>\n" +
                "    <PersistentIdentifierContent>PContent</PersistentIdentifierContent>\n" +
                "  </PersistentIdentifier>\n" +
                "  <DataObjectUse>PhysicalMaster</DataObjectUse>\n" +
                "  <DataObjectNumber>1</DataObjectNumber>\n" +
                "  <PhysicalId>940 W</PhysicalId>\n" +
                "  <PhysicalDimensions>\n" +
                "    <Width unit=\"centimetre\">10.0</Width>\n" +
                "    <Height unit=\"centimetre\">8.0</Height>\n" +
                "    <Depth unit=\"centimetre\">1.0</Depth>\n" +
                "    <Diameter unit=\"centimetre\">0.0</Diameter>\n" +
                "    <Weight unit=\"gram\">59.0</Weight>\n" +
                "  </PhysicalDimensions>\n" +
                "  <Extent>1 carte imprimée</Extent>\n" +
                "  <Dimensions>10,5cm x 14,8cm</Dimensions>\n" +
                "  <Color>Noir et blanc</Color>\n" +
                "  <Framing>Paysage</Framing>\n" +
                "  <Technique>Phototypie</Technique>";
        assertThat(pdoNextOut).isEqualToNormalizingNewlines(testOut);
        SEDA2Version.setSeda2Version(1);
    }
}
