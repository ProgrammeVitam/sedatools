package fr.gouv.vitam.tools.mailextractlib.store;

import fr.gouv.vitam.tools.mailextractlib.AllTests;
import fr.gouv.vitam.tools.mailextractlib.core.*;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.Charset.defaultCharset;
import static org.assertj.core.api.Assertions.assertThat;

public class TestPstStoreExtractor implements AllTests {

    @Test
    public void testGlobalPstExtractor() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testGlobalPstExtractor");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
               true, true, 12, "windows-1252",
                true, true, true, true,
                true,2);
        MailExtractProgressLogger mepl= AllTests.initLogger("testGlobalPstExtractor");
        String urlString = StoreExtractor.composeStoreURL("pst", "", "", "",
                "src/test/resources/pst/Test.pst");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testGlobalPstExtractor", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        //global values
        assertThat(storeExtractor.getFolderTotalCount()).isEqualTo(25);
        assertThat(storeExtractor.getGlobalListCounter(StoreAppointment.class)).isEqualTo(25);
        assertThat(storeExtractor.getGlobalListCounter(StoreMessage.class)).isEqualTo(5);
        assertThat(storeExtractor.getGlobalListCounter(StoreContact.class)).isEqualTo(1);

        // mails extraction
        String mails=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/mails.csv"),defaultCharset());
        mails=mails.replaceAll("/","\\\\");
        String resultMails=FileUtils.readFileToString(new File("src/test/resources/pst/results/mails.csv"), StandardCharsets.UTF_8);
        resultMails=resultMails.replaceAll("/","\\\\");
        assertThat(mails).isEqualToNormalizingNewlines(resultMails);

        // appointments extraction
        String appointments=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/appointments.csv"),defaultCharset());
        appointments=appointments.replaceAll("/","\\\\");
        String resultAppointments=FileUtils.readFileToString(new File("src/test/resources/pst/results/appointments.csv"), StandardCharsets.UTF_8);
        resultAppointments=resultAppointments.replaceAll("/","\\\\");
        assertThat(appointments).isEqualToNormalizingNewlines(resultAppointments);

        // contacts extraction
        assertThat(new File("target/tmpJUnit/testGlobalPstExtractor/contacts.csv")).
                hasSameContentAs(new File("src/test/resources/pst/results/contacts.csv"),StandardCharsets.UTF_8);
        assertThat(new File("target/tmpJUnit/testGlobalPstExtractor/contacts/ContactPicture#1/__BinaryMaster_1__ContactPicture.jpg")).
                hasBinaryContent(FileUtils.readFileToByteArray(new File("src/test/resources/pst/results/ContactPicture.jpg")));

        // mail eml
        String mail=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/F#1-Début-du-fic/F#54-Éléments-env/M#58-Test-message/__BinaryMaster_1__-000c01d556d5-611cfc50-2356f4f0-.eml"),defaultCharset());
        mail=mail.replaceAll("----=_Part.*","");
        String resultMail=FileUtils.readFileToString(new File("src/test/resources/pst/results/mail.eml"), StandardCharsets.UTF_8);
        resultMail=resultMail.replaceAll("----=_Part.*","");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);
    }
}
