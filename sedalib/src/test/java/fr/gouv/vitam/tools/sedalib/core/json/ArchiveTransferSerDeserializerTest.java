package fr.gouv.vitam.tools.sedalib.core.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibJsonProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchiveTransferSerDeserializerTest implements UseTestFiles {

	@Test
	void TestSipSerializationDeserialization()
			throws SEDALibException, IOException, InterruptedException {
		// do import of test directory
		DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter("src/test/resources/PacketSamples" +
				"/SampleWithoutLinksModelV1", null);
		di.addIgnorePattern("Thumbs.db");
		di.addIgnorePattern("pagefile.sys");
		di.doImport();

		// assert macro results
		assertEquals(22,di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size());
		assertEquals(11,di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size());

		// create jackson object mapper
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		// assert archiveTransfer serialization/deserialization
		String ssip = mapper.writeValueAsString(di.getArchiveTransfer());
		ArchiveTransfer dssip = mapper.readValue(ssip, ArchiveTransfer.class);
		String sdssip = mapper.writeValueAsString(dssip);
//		mapper.writeValue(new FileOutputStream("target/tmpJunit/junit_sip.json"), di.getArchiveTransfer());
//		mapper.writeValue(new FileOutputStream("target/tmpJunit/junit_sip_after.json"), dssip);
		assertEquals(ssip, sdssip);

	}

	@Test
	void TestDeserializationKO()
			throws SEDALibException, IOException, InterruptedException {

		// create jackson object mapper
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		// assert archiveTransfer serialization/deserialization
		assertThatThrownBy(() -> mapper.readValue("{Toto}", ArchiveTransfer.class))
				.hasMessageContaining("Unexpected character");
	}

	@Test
	void TestSEDALibJsonProcessingException(){
		assertThatThrownBy(()->{throw new SEDALibJsonProcessingException("Test it");}).hasMessage("Test it");
	}
}
