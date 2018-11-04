package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.CSVTreeToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.LineEndNormalize;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CSVTreeToDataObjectPackageImporterTest {

	@Test
	void importOKCSV() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVTreeToDataObjectPackageImporter cti;

		// When loaded with the csv OK test file
		cti= new CSVTreeToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/TestOK.csv", "Cp1252",';',null);
		cti.doImport();

		// Then
		String testAu = "{\n" +
				"  \"archiveUnitProfileXmlData\" : null,\n" +
				"  \"managementXmlData\" : null,\n" +
				"  \"contentXmlData\" : \"<Content>\\n  <DescriptionLevel>Subseries</DescriptionLevel>\\n  <Title>Justice</Title>\\n  <OriginatingAgencyArchiveUnitIdentifier>REP.7.</OriginatingAgencyArchiveUnitIdentifier>\\n</Content>\",\n" +
				"  \"childrenAuList\" : {\n" +
				"    \"inDataObjectPackageIdList\" : [ \"ID36\", \"ID37\", \"ID38\", \"ID39\", \"ID40\", \"ID41\", \"ID42\", \"ID43\", \"ID44\", \"ID45\", \"ID46\", \"ID47\", \"ID48\", \"ID49\" ]\n" +
				"  },\n" +
				"  \"dataObjectRefList\" : {\n" +
				"    \"inDataObjectPackageIdList\" : [ ]\n" +
				"  },\n" +
				"  \"inDataObjectPackageId\" : \"ID35\",\n" +
				"  \"onDiskPath\" : null\n" +
				"}";
		ArchiveUnit au = cti.getDataObjectPackage().getArchiveUnitById("ID35");
		String sau = mapper.writeValueAsString(au);
		assertThat(TestUtilities.LineEndNormalize(sau)).isEqualTo(TestUtilities.LineEndNormalize(testAu));
	}

	@Test
	void importKOCSV() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVTreeToDataObjectPackageImporter cti;

		// When loaded with the csv KO test file with a line out of all defined series
		cti= new CSVTreeToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/TestKO1.csv", "Cp1252",';',null);

		// Test message identify the wrong line
		assertThatThrownBy(() -> cti.doImport())
				.hasMessageContaining("2b"); // for StringType;
	}
}
