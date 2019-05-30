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

class PhysicalDataObjectTest {

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
//        System.out.println("Value to verify=" + pdoOut);

        // When test read write in Json string format
        PhysicalDataObject pdoNext = mapper.readValue(pdoOut, PhysicalDataObject.class);
        String pdoNextOut = mapper.writeValueAsString(pdoNext);

        // Then
        String testOut = "{\n" +
                "\"dataObjectSystemId\":null,\n" +
                "\"dataObjectGroupSystemId\":null,\n" +
                "\"relationshipsXmlData\":[],\n" +
                "\"dataObjectVersion\":\"PhysicalMaster_1\",\n" +
                "\"physicalIdXmlData\":\"<PhysicalId>940 W</PhysicalId>\",\n" +
                "\"physicalDimensionsXmlData\":\"<PhysicalDimensions>          <Width unit=\"centimetre\">10</Width>          <Height unit=\"centimetre\">8</Height>          <Depth unit=\"centimetre\">1</Depth>          <Diameter unit=\"centimetre\">0</Diameter>          <Weight unit=\"gram\">59</Weight>        </PhysicalDimensions>\",\n" +
                "\"inDataObjectPackageId\":\"ID18\",\n" +
                "\"onDiskPath\":null\n" +
                "}";
        testOut = LineEndNormalize(testOut.replaceAll("\"onDiskPath\" : .*\"", ""));
        pdoNextOut = LineEndNormalize(pdoNextOut.replaceAll("\"onDiskPath\" : .*\"", ""));
        assertThat(pdoNextOut).isEqualTo(testOut);

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
        PhysicalDataObject pdo = si.getArchiveTransfer().getDataObjectPackage().getPdoInDataObjectPackageIdMap()
                .get("ID18");

        // When test read write fragments in XML string format
        String pdoOut = pdo.toSedaXmlFragments();
        PhysicalDataObject pdoNext = new PhysicalDataObject(si.getArchiveTransfer().getDataObjectPackage());
        pdoNext.fromSedaXmlFragments(pdoOut);
        String pdoNextOut = pdoNext.toSedaXmlFragments();

        // Then
        String testOut = "<DataObjectVersion>PhysicalMaster_1</DataObjectVersion>\n" +
                "<PhysicalId>940W</PhysicalId>\n" +
                "<PhysicalDimensions>\n" +
                "<Widthunit=\"centimetre\">10</Width>\n" +
                "<Heightunit=\"centimetre\">8</Height>\n" +
                "<Depthunit=\"centimetre\">1</Depth>\n" +
                "<Diameterunit=\"centimetre\">0</Diameter>\n" +
                "<Weightunit=\"gram\">59</Weight>\n" +
                "</PhysicalDimensions>";
        testOut = LineEndNormalize(testOut);
        pdoNextOut = LineEndNormalize(pdoNextOut);
        assertThat(pdoNextOut).isEqualTo(testOut);

    }
}
