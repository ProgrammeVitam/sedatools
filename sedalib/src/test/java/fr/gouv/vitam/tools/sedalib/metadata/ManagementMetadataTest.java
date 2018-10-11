package fr.gouv.vitam.tools.sedalib.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

class ManagementMetadataTest {

	@Test
	void test() throws SEDALibException {
		ManagementMetadata mm = new ManagementMetadata();

		mm.addNewMetadata("ArchivalProfile", "TestArchivalProfile", "TestSchemeAgencyID", "TestSchemeAgencyName",
				"TestSchemeDataURI", "TestSchemeID", "TestSchemeName", "TestSchemeURI", "TestSchemeVersionID");

		mm.addNewMetadata("OriginatingAgencyIdentifier", "TestOriginatingAgencyIdentifier");
		mm.addNewMetadata("SubmissionAgencyIdentifier", "TestSubmissionAgencyIdentifier");

		String mmOut = mm.toString();

		mm = (ManagementMetadata) SEDAMetadata.fromString(mmOut, ManagementMetadata.class);
		System.out.println(mm.toString());

		String testOut = "<ManagementMetadata>\n"
				+ "  <ArchivalProfile schemeAgencyID=\"TestSchemeAgencyID\" schemeAgencyName=\"TestSchemeAgencyName\" schemeDataURI=\"TestSchemeDataURI\" schemeID=\"TestSchemeID\" schemeName=\"TestSchemeName\" schemeURI=\"TestSchemeURI\" schemeVersionID=\"TestSchemeVersionID\">TestArchivalProfile</ArchivalProfile>\n"
				+ "  <OriginatingAgencyIdentifier>TestOriginatingAgencyIdentifier</OriginatingAgencyIdentifier>\n"
				+ "  <SubmissionAgencyIdentifier>TestSubmissionAgencyIdentifier</SubmissionAgencyIdentifier>\n"
				+ "</ManagementMetadata>";

		assertEquals(testOut, mm.toString());
	}
}
