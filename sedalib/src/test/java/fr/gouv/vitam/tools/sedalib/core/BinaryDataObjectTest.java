package fr.gouv.vitam.tools.sedalib.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

class BinaryDataObjectTest {

	static public Logger createLogger(Level logLevel) {
		Logger logger;

		Properties props = System.getProperties();
		props.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");// "[%1$tc] %4$s: %5$s%n");
		logger = Logger.getLogger("SEDALibTest");
		logger.setLevel(logLevel);

		return logger;
	}

	@Test
	void test() throws SEDALibException, InterruptedException {
		SEDALibProgressLogger spl = new SEDALibProgressLogger(createLogger(Level.FINEST));

		// create jackson object mapper
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
		module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
		mapper.registerModule(module);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
				"src/test/resources/PacketSamples/TestSip.zip", "target/tmpJunit", spl);
		si.doImport();

		BinaryDataObject bdo = si.getArchiveTransfer().getDataObjectPackage().getBdoInDataObjectPackageIdMap()
				.get("ID7");
//		System.out.println(bdo.toString());
		String stest = "DataObjectVersion: BinaryMaster_1\n" + 
				"Nom: image001.jpg\n" + 
				"Modifié le:2018-08-28T19:22:19Z\n" + 
				"Taille: 21232 octets\n" + 
				"SIP Id: ID7\n" + 
				"URI: content/ID7.jpg\n" + 
				"MimeType: image/jpeg\n" + 
				"PUID: fmt/645\n" + 
				"Format: Exchangeable Image File Format (Compressed)\n" + 
				"Digest: SHA-512 - e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3\n";
		String sbdo = bdo.toString().replaceAll("Path: .*", "");

		assertEquals(sbdo, stest);

	}

}
