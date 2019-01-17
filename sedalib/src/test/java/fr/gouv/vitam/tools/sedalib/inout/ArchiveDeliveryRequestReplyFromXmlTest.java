package fr.gouv.vitam.tools.sedalib.inout;

import static fr.gouv.vitam.tools.sedalib.TestUtilities.LineEndNormalize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
				"src/test/resources/PacketSamples/TestDIP.zip", "target/tmpJunit", null);
		si.doImport();
		
		String testog = "{\n" + 
				"  \"binaryDataObjectList\" : [ {\n" + 
				"    \"dataObjectSystemId\" : null,\n" + 
				"    \"dataObjectGroupSystemId\" : null,\n" + 
				"    \"relationshipsXmlData\" : [ ],\n" + 
				"    \"dataObjectGroupReferenceId\" : null,\n" + 
				"    \"dataObjectGroupId\" : null,\n" + 
				"    \"dataObjectVersion\" : \"BinaryMaster_1\",\n" + 
				"    \"uri\" : \"Content/aeaaaaaaaahph73oaa4eealgbuxxariaaaaq.pdf\",\n" + 
				"    \"messageDigest\" : \"70ed5a707d8e7f421f7821131d40878de6d081d6961a0237dc7e1d18187d891fcc36abeeeed5b58a2caf249b1300d83b6bfd19afc462db3e4b152cf86bea545e\",\n" + 
				"    \"messageDigestAlgorithm\" : \"SHA-512\",\n" + 
				"    \"size\" : 33553,\n" + 
				"    \"compressed\" : null,\n" + 
				"    \"formatIdentification\" : {\n" + 
				"      \"formatLitteral\" : \"Acrobat PDF/A - Portable Document Format\",\n" + 
				"      \"mimeType\" : \"application/pdf\",\n" + 
				"      \"formatId\" : \"fmt/354\",\n" + 
				"      \"encoding\" : null\n" + 
				"    },\n" + 
				"    \"fileInfo\" : {\n" + 
				"      \"filename\" : \"009734_20130456_0001_20120117_DI_AN_CMP_lecture_retraite_magistrats.pdf.pdf\",\n" + 
				"      \"creatingApplicationName\" : null,\n" + 
				"      \"creatingApplicationVersion\" : null,\n" +
				"      \"dateCreatedByApplication\" : null,\n" +
				"      \"creatingOs\" : null,\n" +
				"      \"creatingOsVersion\" : null,\n" + 
				"      \"lastModified\" : 1350905234000\n" + 
				"    },\n" + 
				"    \"metadataXmlData\" : null,\n" + 
				"    \"otherMetadataXmlData\" : null,\n" + 
				"    \"inDataObjectPackageId\" : \"aeaaaaaaaahph73oaa4eealgbuxxariaaaaq\",\n" + 
				"    \"onDiskPath\" : \"F:\\\\DocumentsPerso\\\\JS\\\\git\\\\sedalib\\\\tmpJunit\\\\TestDIP.zip-tmpdir\\\\Content\\\\aeaaaaaaaahph73oaa4eealgbuxxariaaaaq.pdf\"\n" + 
				"  } ],\n" + 
				"  \"physicalDataObjectList\" : [ ],\n" + 
				"  \"logBookXmlData\" : null,\n" + 
				"  \"inDataObjectPackageId\" : \"aebaaaaaa4hph73oaa4eealgbuxxariaaaba\",\n" + 
				"  \"onDiskPath\" : null\n" + 
				"}";
		DataObjectGroup og = si.getArchiveDeliveryRequestReply().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("aebaaaaaa4hph73oaa4eealgbuxxariaaaba");
//		System.out.println(mapper.writeValueAsString(og));
		String sog = mapper.writeValueAsString(og);
		sog = sog.replaceAll("\"onDiskPath\" : .*\"", "");
		testog = testog.replaceAll("\"onDiskPath\" : .*\"", "");
		assertEquals(LineEndNormalize(testog),LineEndNormalize(sog));
	}

}
