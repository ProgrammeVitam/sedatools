package fr.gouv.vitam.tools.mailextractlib.store;

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

public class TestMboxStoreExtractor implements AllTests {

    @Test
    public void testGlobalMboxExtractor() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testGlobalMboxExtractor");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
               true, true, 12, "windows-1252",
                true, true, true, true,
                true,2);
        MailExtractProgressLogger mepl= AllTests.initLogger("testGlobalPstExtractor");
        String urlString = StoreExtractor.composeStoreURL("mbox", "", "", "",
                "src/test/resources/mbox/Sent.mbox");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testGlobalMboxExtractor", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        //global values
        assertThat(storeExtractor.getFolderTotalCount()).isEqualTo(1);
        assertThat(storeExtractor.getGlobalListCounter(StoreAppointment.class)).isEqualTo(0);
        assertThat(storeExtractor.getGlobalListCounter(StoreMessage.class)).isEqualTo(4);
        assertThat(storeExtractor.getGlobalListCounter(StoreContact.class)).isEqualTo(0);

        // mails extraction
        String mails=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalMboxExtractor/mails.csv"),defaultCharset());
        mails=mails.replaceAll("/","\\\\");
        String resultMails=FileUtils.readFileToString(new File("src/test/resources/mbox/results/mails.csv"), StandardCharsets.UTF_8);
        resultMails=resultMails.replaceAll("/","\\\\");
        assertThat(mails).isEqualToNormalizingNewlines(resultMails);

        // mail eml
        String mail=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalMboxExtractor/M#4-Test-message/__BinaryMaster_1__-000c01d556d5-611cfc50-2356f4f0-.eml"),defaultCharset());
        mail=mail.replaceAll("----=_Part.*","");
        String resultMail=FileUtils.readFileToString(new File("src/test/resources/mbox/results/mail.eml"), StandardCharsets.UTF_8);
        resultMail=resultMail.replaceAll("----=_Part.*","");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);
    }
}
