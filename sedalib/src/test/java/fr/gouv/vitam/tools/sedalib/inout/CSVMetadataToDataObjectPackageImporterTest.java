package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.CSVMetadataToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CSVMetadataToDataObjectPackageImporterTest {

	@Test
	void importOKCSV1column() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVMetadataToDataObjectPackageImporter cmi;

		// When loaded with the csv OK test file
		cmi= new CSVMetadataToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/MetadataTestOK1col.csv", "windows-1252",';',null);
		cmi.doImport();

		// Then
		String testAuID10 = "{\n" +
				"\"archiveUnitProfileXmlData\":null,\n" +
				"\"managementXmlData\":null,\n" +
				"\"contentXmlData\":\"<Content>  <DescriptionLevel>RecordGrp</DescriptionLevel>  <Title>Root2</Title></Content>\",\n" +
				"\"childrenAuList\":{\n" +
				"\"inDataObjectPackageIdList\":[]\n" +
				"},\n" +
				"\"dataObjectRefList\":{\n" +
				"\"inDataObjectPackageIdList\":[]\n" +
				"},\n" +
				"\"inDataObjectPackageId\":\"ID10\",\n" +
				"\"onDiskPath\":null\n" +
				"}";
		String testAuID17 = "{\n" +
				"\"archiveUnitProfileXmlData\":null,\n" +
				"\"managementXmlData\":null,\n" +
				"\"contentXmlData\":\"<Content>  <DescriptionLevel>RecordGrp</DescriptionLevel>  <Title>Root2</Title></Content>\",\n" +
				"\"childrenAuList\":{\n" +
				"\"inDataObjectPackageIdList\":[]\n" +
				"},\n" +
				"\"dataObjectRefList\":{\n" +
				"\"inDataObjectPackageIdList\":[]\n" +
				"},\n" +
				"\"inDataObjectPackageId\":\"ID17\",\n" +
				"\"onDiskPath\":null\n" +
				"}";
		// Root2 is either the first or last AU (random order due to hashmap use)
		ArchiveUnit au = cmi.getDataObjectPackage().getArchiveUnitById("ID17");
		String sau = mapper.writeValueAsString(au);
		if (sau.contains("Root2"))
			assertThat(TestUtilities.LineEndNormalize(sau)).isEqualTo(TestUtilities.LineEndNormalize(testAuID17));
		else {
			au = cmi.getDataObjectPackage().getArchiveUnitById("ID10");
			sau = mapper.writeValueAsString(au);
			assertThat(TestUtilities.LineEndNormalize(sau)).isEqualTo(TestUtilities.LineEndNormalize(testAuID10));
		}
	}

	@Test
	void importOKCSV3column() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVMetadataToDataObjectPackageImporter cmi;

		// When loaded with the csv OK test file
		cmi= new CSVMetadataToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/MetadataTestOK3col.csv", "windows-1252",';',null);
		cmi.doImport();

		// Then
		String testAu = "{\n" +
				"\"archiveUnitProfileXmlData\":null,\n" +
				"\"managementXmlData\":null,\n" +
				"\"contentXmlData\":\"<Content>  <DescriptionLevel>Item</DescriptionLevel>  <Title>image001.jpg</Title>  <Description xml:lang=\"fr\">Document \"image001.jpg\" joint au message &lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></Description>  <CreatedDate>2016-08-30T10:14:17</CreatedDate></Content>\",\n" +
				"\"childrenAuList\":{\n" +
				"\"inDataObjectPackageIdList\":[]\n" +
				"},\n" +
				"\"dataObjectRefList\":{\n" +
				"\"inDataObjectPackageIdList\":[\"ID13\"]\n" +
				"},\n" +
				"\"inDataObjectPackageId\":\"Import-6\",\n" +
				"\"onDiskPath\":null\n" +
				"}";
		ArchiveUnit au = cmi.getDataObjectPackage().getArchiveUnitById("Import-6");
		String sau = mapper.writeValueAsString(au);
		assertThat(TestUtilities.LineEndNormalize(sau)).isEqualTo(TestUtilities.LineEndNormalize(testAu));
	}

	@Test
	void importOKCSV3columnWithManagement() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVMetadataToDataObjectPackageImporter cmi;

		// When loaded with the csv OK test file
		cmi= new CSVMetadataToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/MetadataTestManagementOK3col.csv", "windows-1252",';',null);
		cmi.doImport();

		// Then
		String testAu = "{\n" +
				"\"archiveUnitProfileXmlData\":null,\n" +
				"\"managementXmlData\":\"<Management>  <AccessRule>    <Rule>ACC-00001</Rule>    <StartDate>2015-11-19</StartDate>    <Rule>ACC-00002</Rule>    <PreventInheritance>true</PreventInheritance>    <RefNonRuleId>ACC-00003</RefNonRuleId>    <RefNonRuleId>ACC-00004</RefNonRuleId>  </AccessRule>  " +
				"<ClassificationRule>    <Rule>CLAS-00001</Rule>    <StartDate>2015-11-19</StartDate>    <ClassificationOwner>Autorité</ClassificationOwner>    <NeedReassessingAuthorization>true</NeedReassessingAuthorization>  </ClassificationRule></Management>\",\n" +
				"\"contentXmlData\":\"<Content>  <DescriptionLevel>Item</DescriptionLevel>  <Title>image001.jpg</Title>  <Description xml:lang=\"fr\">Document \"image001.jpg\" joint au message &lt;79980C36BA239C449A9575FE17591F3D0C237AD1@prd-exch-b01.solano.alize></Description>  <CreatedDate>2016-08-30T10:14:17</CreatedDate></Content>\",\n" +
				"\"childrenAuList\":{\n" +
				"\"inDataObjectPackageIdList\":[]\n" +
				"},\n" +
				"\"dataObjectRefList\":{\n" +
				"\"inDataObjectPackageIdList\":[\"ID13\"]\n" +
				"},\n" +
				"\"inDataObjectPackageId\":\"Import-6\",\n" +
				"\"onDiskPath\":null\n" +
				"}";
		ArchiveUnit au = cmi.getDataObjectPackage().getArchiveUnitById("Import-6");
		String sau = mapper.writeValueAsString(au);
		assertThat(TestUtilities.LineEndNormalize(sau)).isEqualTo(TestUtilities.LineEndNormalize(testAu));
	}

	@Test
	void importTagKOCSV() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVMetadataToDataObjectPackageImporter cmi;

		// When loaded with the csv OK test file
		cmi= new CSVMetadataToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/MetadataTestTagKO.csv", "windows-1252",';',null);

		assertThatThrownBy(() -> cmi.doImport())
				.hasMessageContaining("Caractère interdit"); // for StringType;
	}

	@Test
	void importLineKOCSV() throws SEDALibException, InterruptedException, JsonProcessingException {
		// Given
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		CSVMetadataToDataObjectPackageImporter cmi;

		// When loaded with the csv OK test file
		cmi= new CSVMetadataToDataObjectPackageImporter(
				"src/test/resources/PacketSamples/MetadataTestLineKO.csv", "windows-1252",';',null);

		assertThatThrownBy(() -> cmi.doImport())
				.hasMessageContaining("ligne 4"); // for StringType;
	}
}
