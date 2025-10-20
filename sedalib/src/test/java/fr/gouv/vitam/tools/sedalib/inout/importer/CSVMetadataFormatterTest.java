package fr.gouv.vitam.tools.sedalib.inout.importer;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

@ExtendWith(SedaContextExtension.class)
class CSVMetadataFormatterTest {

    @Test
    void newCSVMetadataFormatter_tests_bad_headers() {
        final String[] bad_row = {"One", "Two", "three"};
        Assertions.assertThrows(SEDALibException.class, () -> new CSVMetadataFormatter(bad_row, Paths.get("whatever")));

        String[] row_with_one_error = {"Id", "ParentId", "File", "ObjectFiles", "Content.DescriptionLevel", "Content.Title", "Content.Description", "Content.TransactedDate", "Content.StartDate", "CeaCategory"};
        Assertions.assertThrows(SEDALibException.class, () -> new CSVMetadataFormatter(row_with_one_error, Paths.get("whatever")));
    }

    @Test
    void newCSVMetadataFormatter_test_OK() throws FileNotFoundException, SEDALibException {
        String[] row = {"Id", "ParentId", "File", "ObjectFiles", "Content.DescriptionLevel", "Content.Title", "Content.Description", "Content.TransactedDate", "Content.StartDate"};
        CSVMetadataFormatter csvMetadataFormatter = new CSVMetadataFormatter(row, ResourceUtils.getResourcePath("metadata_OK.csv").getParent());
        Assertions.assertTrue(csvMetadataFormatter.isExtendedFormat());
    }

}
