package fr.gouv.vitam.tools.sedalib.inout;

import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

//import java.io.FileOutputStream;
//import org.apache.commons.io.Charsets;
//import org.apache.commons.io.IOUtils;

class SEDAValidationTest implements UseTestFiles {

	private static String readFileToString(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, "UTF-8");
	}

	@Test
	void testSedaXmlValidationOK()
			throws IllegalArgumentException, SEDALibException, XMLStreamException, IOException, InterruptedException {

		// do import of test directory
		DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
				"src/test/resources/PacketSamples/SampleWithLinksModelV2", null);
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

		// validation
		assertAll(() -> di.getArchiveTransfer().seda21Validate(null));
	}

	@Test
	void testSedaXmlValidationKO()
			throws IllegalArgumentException, SEDALibException, XMLStreamException, IOException, InterruptedException {

		// do import of test directory
		DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
				"src/test/resources/PacketSamples/SampleWithLinksModelV2", null);
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

		ArchiveUnit au = di.getArchiveTransfer().getDataObjectPackage().getArchiveUnitById("ID38");
		au.setContentXmlData("");

		// validation
		assertThatThrownBy(() -> di.getArchiveTransfer().seda21Validate(null))
				.hasMessageContaining("The content of element 'ArchiveUnit' is not complete");
	}

	@Test
	void testSedaRNGProfileValidationOK()
			throws IllegalArgumentException, SEDALibException, XMLStreamException, IOException, InterruptedException {
		// given
		TestUtilities.eraseAll("target/tmpJunit/OK_468.zip-tmpdir");
		SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
				"src/test/resources/PacketSamples/OK_468.zip", "target/tmpJunit/OK_468.zip-tmpdir", null);
		si.doImport();

		// when validate then no exception
		assertAll(() -> si.getArchiveTransfer()
				.sedaProfileValidate("src/test/resources/PacketSamples/profile.rng",null));
	}

	@Test
	void testSedaRNGProfileValidationKO()
			throws IllegalArgumentException, SEDALibException, XMLStreamException, IOException, InterruptedException {
		// given
		TestUtilities.eraseAll("target/tmpJunit/KO_468.zip-tmpdir");
		SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
				"src/test/resources/PacketSamples/KO_468.zip", "target/tmpJunit/KO_468.zip-tmpdir", null);
		si.doImport();

		// when validate then no exception
		assertThatThrownBy(() -> si.getArchiveTransfer()
				.sedaProfileValidate("src/test/resources/PacketSamples/profile.rng",null))
				.hasMessageContaining("\"Title\" invalid; must be equal to \"Versement de la matrice cadastrale numérique\"");
	}

	// TODO testWithXSD
}