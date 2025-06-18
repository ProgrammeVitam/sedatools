package fr.gouv.vitam.tools.mailextractlib;

import fr.gouv.vitam.tools.mailextractlib.AllTests;
import fr.gouv.vitam.tools.mailextractlib.core.*;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;

public class TestMetadata implements AllTests {

    @Test
    public void testMetadata() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testMetadata");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
                true, true, 12, "windows-1252",
                true, true, true, false,
                true,2);
        MailExtractProgressLogger mepl= AllTests.initLogger("testMetadata");
        String urlString = StoreExtractor.composeStoreURL("eml", "", "", "",
                "src/test/resources/metadata/Test de métadonnées.eml");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testMetadata", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,false)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,false)).isEqualTo(0);

        // metadata extraction
        String metadata=FileUtils.readFileToString(new File("target/tmpJUnit/testMetadata/M#1-Test-de-méta/__ArchiveUnitMetadata.xml"),defaultCharset());
        String resultMetadata=FileUtils.readFileToString(new File("src/test/resources/metadata/results/__ArchiveUnitMetadata.xml"), StandardCharsets.UTF_8);
        assertThat(metadata).isEqualToNormalizingNewlines(resultMetadata);
    }
}
