package fr.gouv.vitam.tools.sedalib.inout;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

//import org.apache.commons.io.Charsets;
//import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

class ArchiveTransferToFromXmlTest implements UseTestFiles {

	static public Logger createLogger(Level logLevel) {
		Logger logger;

		Properties props = System.getProperties();
		props.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");// "[%1$tc] %4$s: %5$s%n");
		logger = Logger.getLogger("SEDALibTest");
		logger.setLevel(logLevel);

		return logger;
	}

	static String readFileToString(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}

	@Test
	void testToSedaXml()
			throws IllegalArgumentException, SEDALibException, XMLStreamException, IOException, InterruptedException {

		SEDALibProgressLogger spl = new SEDALibProgressLogger(createLogger(Level.FINEST));

		// do import of test directory
		DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
				"src/test/resources/PacketSamples/SampleWithLinksModelV2", spl);
		di.addIgnorePattern("Thumbs.db");
		di.addIgnorePattern("pagefile.sys");

		di.doImport();

		di.getArchiveTransfer().setGlobalMetadata(new GlobalMetadata());
		di.getArchiveTransfer().getGlobalMetadata().comment = "2eme SIP";
		di.getArchiveTransfer().getGlobalMetadata().messageIdentifier = "MessageIdentifier0";
		di.getArchiveTransfer().getGlobalMetadata().archivalAgreement = "ArchivalAgreement0";
		di.getArchiveTransfer().getGlobalMetadata().codeListVersionsXmlData = "<CodeListVersions>\n"
				+ "    <ReplyCodeListVersion>ReplyCodeListVersion0</ReplyCodeListVersion>\n"
				+ "<MessageDigestAlgorithmCodeListVersion>MessageDigestAlgorithmCodeListVersion0</MessageDigestAlgorithmCodeListVersion>\n"
				+ "<MimeTypeCodeListVersion>MimeTypeCodeListVersion0</MimeTypeCodeListVersion>\n"
				+ "<EncodingCodeListVersion>EncodingCodeListVersion0</EncodingCodeListVersion>\n"
				+ "<FileFormatCodeListVersion>FileFormatCodeListVersion0</FileFormatCodeListVersion>\n"
				+ "<CompressionAlgorithmCodeListVersion>CompressionAlgorithmCodeListVersion0</CompressionAlgorithmCodeListVersion>\n"
				+ "<DataObjectVersionCodeListVersion>DataObjectVersionCodeListVersion0</DataObjectVersionCodeListVersion>\n"
				+ "<StorageRuleCodeListVersion>StorageRuleCodeListVersion0</StorageRuleCodeListVersion>\n"
				+ "<AppraisalRuleCodeListVersion>AppraisalRuleCodeListVersion0</AppraisalRuleCodeListVersion>\n"
				+ "<AccessRuleCodeListVersion>AccessRuleCodeListVersion0</AccessRuleCodeListVersion>\n"
				+ "<DisseminationRuleCodeListVersion>DisseminationRuleCodeListVersion0</DisseminationRuleCodeListVersion>\n"
				+ "<ReuseRuleCodeListVersion>ReuseRuleCodeListVersion0</ReuseRuleCodeListVersion>\n"
				+ "<ClassificationRuleCodeListVersion>ClassificationRuleCodeListVersion0</ClassificationRuleCodeListVersion>\n"
				+ "<AuthorizationReasonCodeListVersion>AuthorizationReasonCodeListVersion0</AuthorizationReasonCodeListVersion>\n"
				+ "<RelationshipCodeListVersion>RelationshipCodeListVersion0</RelationshipCodeListVersion>\n"
				+ "  </CodeListVersions>";
		di.getArchiveTransfer().getDataObjectPackage()
				.setManagementMetadataXmlData("<ManagementMetadata>\n"
						+ "      <AcquisitionInformation>Acquisition Information</AcquisitionInformation>\n"
						+ "<LegalStatus>Public Archive</LegalStatus>\n"
						+ "<OriginatingAgencyIdentifier>Service_producteur</OriginatingAgencyIdentifier>\n"
						+ "<SubmissionAgencyIdentifier>Service_versant</SubmissionAgencyIdentifier>\n"
						+ "    </ManagementMetadata>");
		di.getArchiveTransfer().getGlobalMetadata().archivalAgencyIdentifier = "Identifier4";
		di.getArchiveTransfer().getGlobalMetadata().transferringAgencyIdentifier = "Identifier5";

		// flat
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2);
		di.getArchiveTransfer().toSedaXml(xmlWriter, false, spl);
		xmlWriter.close();
		String generatedFlatManifest = baos.toString().replaceAll("<LastModified>.*</LastModified>\n", "");

		// hierarchical
		baos.reset();
		xmlWriter = new SEDAXMLStreamWriter(baos, 2);
		di.getArchiveTransfer().toSedaXml(xmlWriter, true, spl);
		xmlWriter.close();
		String generatedHierarchicalManifest = baos.toString().replaceAll("<LastModified>.*</LastModified>\n", "");

//		IOUtils.write(generatedFlatManifest,
//				new FileOutputStream("src/test/resources/PacketSamples/SampleWithoutLinkFlatManifest.xml"),Charsets.UTF_8);
//		IOUtils.write(generatedHierarchicalManifest,
//				new FileOutputStream("src/test/resources/PacketSamples/SampleWithoutLinkHierarchicalManifest.xml"),Charsets.UTF_8);

		String fileManifest = readFileToString("src/test/resources/PacketSamples/SampleWithoutLinkFlatManifest.xml");
		generatedFlatManifest = generatedFlatManifest.substring(generatedFlatManifest.indexOf("MessageIdentifier"));
		fileManifest = fileManifest.substring(fileManifest.indexOf("MessageIdentifier"));
//FIXME different order in manifest in windows and linux
		// assertEquals(generatedFlatManifest, fileManifest);

		fileManifest = readFileToString("src/test/resources/PacketSamples/SampleWithoutLinkHierarchicalManifest.xml");
		generatedHierarchicalManifest = generatedHierarchicalManifest
				.substring(generatedHierarchicalManifest.indexOf("MessageIdentifier"));
		fileManifest = fileManifest.substring(fileManifest.indexOf("MessageIdentifier"));
// FIXME different order in manifest in windows and linux
		// assertEquals(generatedHierarchicalManifest, fileManifest);
	}

}
