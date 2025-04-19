package fr.gouv.vitam.tools.mailextractlib;

import fr.gouv.vitam.tools.mailextractlib.core.*;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTextExtraction {
    @Test
    public void testTextExtraction() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testTextExtraction");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
                true, true, 12, "windows-1252",
                false, true, true, true,
                true, 2);
        MailExtractProgressLogger mepl = AllTests.initLogger("testTextExtraction");
        String urlString = StoreExtractor.composeStoreURL("eml", "", "", "",
                "src/test/resources/textextraction/Test text extraction.eml");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testTextExtraction", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class, false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class, false)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class, false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class, false)).isEqualTo(0);

        // compare txt and xml extracted
        AllTests.assertThatDirectoriesContainSameFilesWithExtensions("src/test/resources/textextraction/results/M#1-Test-extract",
                "target/tmpJUnit/testTextExtraction/M#1-Test-extract", new String[] {"txt", "xml"});
    }
}
