package fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst;

import fr.gouv.vitam.tools.mailextractlib.AllTests;
import fr.gouv.vitam.tools.mailextractlib.core.StoreContact;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPstStoreExtractor implements AllTests {

    @Test
    public void testContactCalendarPstExtractor() throws MailExtractLibException, InterruptedException, IOException {
        //given
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
               true, true, 12, "windows-1252",
                true, true, true, true,
                true,2);
        MailExtractProgressLogger mepl= AllTests.initLogger("testContactCalendarPstExtractor");
        String urlString = StoreExtractor.composeStoreURL("pst", "", "", "",
                "src/test/resources/pst/ContactCalendar-test.pst");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testContactCalendarPstExtractor", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then
        assertThat(storeExtractor.getFolderTotalCount()).isEqualTo(18);
        assertThat(storeExtractor.getGlobalListCounter(StoreMessage.class)).isEqualTo(19);
        assertThat(storeExtractor.getGlobalListCounter(StoreContact.class)).isEqualTo(1);
        assertThat(new File("target/tmpJUnit/testContactCalendarPstExtractor/mails.csv")).
                hasSameContentAs(new File("src/test/resources/pst/results/ContactCalendar-mails.csv"));
        assertThat(new File("target/tmpJUnit/testContactCalendarPstExtractor/contacts.csv")).
                hasSameContentAs(new File("src/test/resources/pst/results/ContactCalendar-contacts.csv"));
        assertThat(new File("target/tmpJUnit/testContactCalendarPstExtractor/contacts/ContactPicture#1/__BinaryMaster_1__ContactPicture.jpg")).
                hasBinaryContent(FileUtils.readFileToByteArray(new File("src/test/resources/pst/results/ContactCalendar-ContactPicture.jpg")));
    }
}
