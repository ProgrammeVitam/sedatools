package fr.gouv.vitam.tools.mailextractlib;

import fr.gouv.vitam.tools.mailextractlib.core.StoreAppointment;
import fr.gouv.vitam.tools.mailextractlib.core.StoreContact;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTextExtraction {

    private static final SoftAssertions assertNoProcessesRun = new SoftAssertions();
    private static SecurityManager initialSecurityManager;

    @BeforeAll
    public static void setup() {
        initialSecurityManager = System.getSecurityManager();

        // WARNING: Deprecated in Java 17. Not recommended for production, but still good for testing.
        class NoExecSecurityManager extends SecurityManager {
            @Override
            public void checkExec(String cmd) {
                assertNoProcessesRun.fail("Process run: '" + cmd + "'");
            }

            @Override
            public void checkPermission(java.security.Permission perm) {
                // Allow all other permissions
            }
        }

        // Set the custom SecurityManager
        System.setSecurityManager(new NoExecSecurityManager());
    }

    @AfterAll
    public static void tearDown() {
        // Reset initial security manager
        System.setSecurityManager(initialSecurityManager);

        // Asser that no external processes run under-the-hood by apache-tika (tesseract, ffmpeg...)
        assertNoProcessesRun.assertAll();
    }

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
