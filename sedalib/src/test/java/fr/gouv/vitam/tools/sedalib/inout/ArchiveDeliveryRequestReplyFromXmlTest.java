/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.inout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.DIPToArchiveDeliveryRequestReplyImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
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
				"    \"metadataList\" : [ {\n" +
				"      \"type\" : \"StringType\",\n" +
				"      \"elementName\" : \"DataObjectVersion\",\n" +
				"      \"value\" : \"BinaryMaster_1\"\n" +
				"    }, {\n" +
				"      \"type\" : \"StringType\",\n" +
				"      \"elementName\" : \"Uri\",\n" +
				"      \"value\" : \"Content/aeaaaaaaaahph73oaa4eealgbuxxariaaaaq.pdf\"\n" +
				"    }, {\n" +
				"      \"type\" : \"DigestType\",\n" +
				"      \"elementName\" : \"MessageDigest\",\n" +
				"      \"value\" : \"70ed5a707d8e7f421f7821131d40878de6d081d6961a0237dc7e1d18187d891fcc36abeeeed5b58a2caf249b1300d83b6bfd19afc462db3e4b152cf86bea545e\",\n" +
				"      \"algorithm\" : \"SHA-512\"\n" +
				"    }, {\n" +
				"      \"type\" : \"IntegerType\",\n" +
				"      \"elementName\" : \"Size\",\n" +
				"      \"value\" : 33553\n" +
				"    }, {\n" +
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
				"    }, {\n" +
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
				"    } ],\n" +
				"    \"inDataObjectPackageId\" : \"aeaaaaaaaahph73oaa4eealgbuxxariaaaaq\",\n" +
				"    \n" +
				"  } ],\n" +
				"  \"physicalDataObjectList\" : [ ],\n" +
				"  \"logBook\" : null,\n" +
				"  \"inDataObjectPackageId\" : \"aebaaaaaa4hph73oaa4eealgbuxxariaaaba\",\n" +
				"  \"onDiskPath\" : null\n" +
				"}";
		DataObjectGroup og = si.getArchiveDeliveryRequestReply().getDataObjectPackage().getDogInDataObjectPackageIdMap().get("aebaaaaaa4hph73oaa4eealgbuxxariaaaba");
		String sog = mapper.writeValueAsString(og);
		sog = sog.replaceAll("\"onDiskPath\" : .*\"", "");
		testog = testog.replaceAll("\"onDiskPath\" : .*\"", "");
		assertThat(sog).isEqualToNormalizingNewlines(testog);
	}

}
