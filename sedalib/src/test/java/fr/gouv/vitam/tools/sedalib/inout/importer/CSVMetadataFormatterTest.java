package fr.gouv.vitam.tools.sedalib.inout.importer;

import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CSVMetadataFormatterTest {

    @Test
    void new_test_OK() throws FileNotFoundException, SEDALibException {
        String[] row = {"Id", "ParentId", "File", "ObjectFiles", "Content.DescriptionLevel", "Content.Title", "Content.Description", "Content.TransactedDate", "Content.StartDate"};
        CSVMetadataFormatter csvMetadataFormatter = new CSVMetadataFormatter(row, Paths.get("/baseDir/"));
        Assertions.assertTrue(csvMetadataFormatter.isExtendedFormat());
    }

    @Test
    void new_test_with_bad_headers() {
        final String[] bad_row = {"One", "Two", "three"};
        Assertions.assertThrows(SEDALibException.class, () -> new CSVMetadataFormatter(bad_row, Paths.get("whatever")));

        String[] row_with_one_error = {"Id", "ParentId", "File", "ObjectFiles", "Content.DescriptionLevel", "Content.Title", "Content.Description", "Content.TransactedDate", "Content.StartDate", "CeaCategory"};
        Assertions.assertThrows(SEDALibException.class, () -> new CSVMetadataFormatter(row_with_one_error, Paths.get("/baseDir/")));
    }

    @Test
    void doFormatAndExtractContentXML_test_OK() throws FileNotFoundException, SEDALibException {
        String[] row1 = {
                "File",
                "Content.DescriptionLevel",
                "Content.Title",
                "Content.SigningInformation.SigningRole.0",
                "Content.SigningInformation.SigningRole.1",
                "Content.SigningInformation.SigningRole.2",
                "Content.SigningInformation.DetachedSigningRole",
                "Content.SigningInformation.SignatureDescription.0.Signer.FullName",
                "Content.SigningInformation.SignatureDescription.0.Signer.SigningTime",
                "Content.SigningInformation.TimestampingInformation.TimeStamp",
                "Content.SigningInformation.AdditionalProof.0.AdditionalProofInformation"
        };
        String[] row2 = {
                "whatever.pdf",
                "Item",
                "whatever",
                "SignedDocument",
                "Signature",
                "Timestamp",
                "AdditionalProof",
                "Alexandre PARIS",
                "2024-12-25T12:34:56",
                "2023-06-22T11:36:49",
                "AdditionalProofInformation"
        };
        CSVMetadataFormatter metadataFormatter = new CSVMetadataFormatter(row1, Paths.get("/baseDir/"));
        metadataFormatter.isExtendedFormat();

        Assertions.assertEquals("/baseDir/whatever.pdf", metadataFormatter.getGUID(row2));
        Assertions.assertEquals("/baseDir", metadataFormatter.getParentGUID(row2));
        Assertions.assertEquals("/baseDir/whatever.pdf", metadataFormatter.getFile(row2));
        Assertions.assertEquals("", metadataFormatter.getObjectFiles(row2));
        assertThat(metadataFormatter.doFormatAndExtractContentXML(row2)).isEqualToIgnoringWhitespace(ResourceUtils.getResourceAsString("import/expected_content_01.xml"));
        Assertions.assertEquals("", metadataFormatter.extractManagementXML());
    }


}
