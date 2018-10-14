package fr.gouv.vitam.tools.sedalib.core.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

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
		assertEquals(di.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap().size(), 22);
		assertEquals(di.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap().size(), 11);

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
		mapper.writeValue(new FileOutputStream("target/tmpJunit/junit_sip.json"), di.getArchiveTransfer());
		mapper.writeValue(new FileOutputStream("target/tmpJunit/junit_sip_after.json"), dssip);
		assertEquals(ssip, sdssip);

	}

}
