package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.DIPToArchiveDeliveryRequestReplyImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ArchiveDeliveryRequestReplyFromXmlTest {

	@Test
	void test() throws SEDALibException, InterruptedException, JsonProcessingException {
		// create jackson object mapper
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		DIPToArchiveDeliveryRequestReplyImporter si = new DIPToArchiveDeliveryRequestReplyImporter(
				"src/test/resources/PacketSamples/TestDIP.zip", "target/tmpJunit/TestDIP.zip-tmpdir", null);
		si.doImport();
		
		String testog = "{\n" +
				"  \"binaryDataObjectList\" : [ {\n" +
				"    \"dataObjectProfile\" : null,\n" +
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
				"    \"uri\" : {\n" +
				"      \"type\" : \"StringType\",\n" +
				"      \"elementName\" : \"Uri\",\n" +
				"      \"value\" : \"Content/aeaaaaaaaahph73oaa4eealgbuxxariaaaaq.pdf\"\n" +
				"    },\n" +
				"    \"messageDigest\" : {\n" +
				"      \"type\" : \"DigestType\",\n" +
				"      \"elementName\" : \"MessageDigest\",\n" +
				"      \"value\" : \"70ed5a707d8e7f421f7821131d40878de6d081d6961a0237dc7e1d18187d891fcc36abeeeed5b58a2caf249b1300d83b6bfd19afc462db3e4b152cf86bea545e\",\n" +
				"      \"algorithm\" : \"SHA-512\"\n" +
				"    },\n" +
				"    \"size\" : {\n" +
				"      \"type\" : \"IntegerType\",\n" +
				"      \"elementName\" : \"Size\",\n" +
				"      \"value\" : 33553\n" +
				"    },\n" +
				"    \"compressed\" : null,\n" +
				"    \"formatIdentification\" : {\n" +
				"      \"type\" : \"FormatIdentification\",\n" +
				"      \"elementName\" : \"FormatIdentification\",\n" +
				"      \"metadataList\" : [ {\n" +
				"        \"type\" : \"StringType\",\n" +
				"        \"elementName\" : \"FormatLitteral\",\n" +
				"        \"value\" : \"Acrobat PDF/A - Portable Document Format\"\n" +
				"      }, {\n" +
				"        \"type\" : \"StringType\",\n" +
				"        \"elementName\" : \"MimeType\",\n" +
				"        \"value\" : \"application/pdf\"\n" +
				"      }, {\n" +
				"        \"type\" : \"StringType\",\n" +
				"        \"elementName\" : \"FormatId\",\n" +
				"        \"value\" : \"fmt/354\"\n" +
				"      } ]\n" +
				"    },\n" +
				"    \"fileInfo\" : {\n" +
				"      \"type\" : \"FileInfo\",\n" +
				"      \"elementName\" : \"FileInfo\",\n" +
				"      \"metadataList\" : [ {\n" +
				"        \"type\" : \"StringType\",\n" +
				"        \"elementName\" : \"Filename\",\n" +
				"        \"value\" : \"009734_20130456_0001_20120117_DI_AN_CMP_lecture_retraite_magistrats.pdf.pdf\"\n" +
				"      }, {\n" +
				"        \"type\" : \"DateTimeType\",\n" +
				"        \"elementName\" : \"LastModified\",\n" +
				"        \"dateTimeString\" : \"2012-10-22T11:27:14Z\"\n" +
				"      } ]\n" +
				"    },\n" +
				"    \"metadata\" : null,\n" +
				"    \"inDataObjectPackageId\" : \"aeaaaaaaaahph73oaa4eealgbuxxariaaaaq\",\n" +
				"    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\IdeaProjects\\\\sedatools\\\\sedalib\\\\target\\\\tmpJunit\\\\TestDIP.zip-tmpdir\\\\Content\\\\aeaaaaaaaahph73oaa4eealgbuxxariaaaaq.pdf\"\n" +
				"  } ],\n" +
				"  \"physicalDataObjectList\" : [ ],\n" +
				"  \"logBook\" : null,\n" +
				"  \"inDataObjectPackageId\" : \"aebaaaaaa4hph73oaa4eealgbuxxariaaaba\",\n" +
				"  \"onDiskPath\" : null\n" +
				"}";
		DataObjectGroup og = si.getArchiveDeliveryRequestReply().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("aebaaaaaa4hph73oaa4eealgbuxxariaaaba");
		//System.out.println("Value to verify="+mapper.writeValueAsString(og));
		String sog = mapper.writeValueAsString(og);
		sog = sog.replaceAll("\"onDiskPath\" : .*\"", "");
		testog = testog.replaceAll("\"onDiskPath\" : .*\"", "");
		assertThat(sog).isEqualToNormalizingNewlines(testog);
	}

}
