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

public class TestEmlStoreExtractor implements AllTests {

    @Test
    public void testGlobalEmlExtractor() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testGlobalEmlExtractor");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
                true, true, 12, "windows-1252",
                true, true, true, true,
                true,2);
        MailExtractProgressLogger mepl= AllTests.initLogger("testGlobalEmlExtractor");
        String urlString = StoreExtractor.composeStoreURL("eml", "", "", "",
                "src/test/resources/eml/Test message 2.eml");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testGlobalEmlExtractor", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,false)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,false)).isEqualTo(0);

        // sub element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,true)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,true)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,true)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,true)).isEqualTo(0);

        // embedded mail eml
        String mail=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalEmlExtractor/M#1-Test-message/M#2-Test-message/__BinaryMaster_1__-003e01d556d3-b3116ed0-19344c70-.eml"),defaultCharset());
        mail=mail.replaceAll("----=_Part.*","");
        String resultMail=FileUtils.readFileToString(new File("src/test/resources/eml/results/embeddedMail.eml"), StandardCharsets.UTF_8);
        resultMail=resultMail.replaceAll("----=_Part.*","");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);

        // mail eml
        mail=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalEmlExtractor/M#1-Test-message/__BinaryMaster_1__-000c01d556d5-611cfc50-2356f4f0-.eml"),defaultCharset());
        mail=mail.replaceAll("----=_Part.*","");
        resultMail=FileUtils.readFileToString(new File("src/test/resources/eml/results/mail.eml"), StandardCharsets.UTF_8);
        resultMail=resultMail.replaceAll("----=_Part.*","");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);
    }

    @Test
    public void testPgpEmlExtractor() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testPgpEmlExtractor");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
                true, true, 12, "windows-1252",
                true, true, true, true,
                true,2);
        MailExtractProgressLogger mepl= AllTests.initLogger("testPgpEmlExtractor");
        String urlString = StoreExtractor.composeStoreURL("eml", "", "", "",
                "src/test/resources/eml/TestPgp.eml");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testPgpEmlExtractor", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,false)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,false)).isEqualTo(0);

        // mail eml
        String mail=FileUtils.readFileToString(new File("target/tmpJUnit/testPgpEmlExtractor/M#1--membres--No/__BinaryMaster_1__-200505092127.23834.pierre.habou.eml"),defaultCharset());
        mail=mail.replaceAll("----=_Part.*","");
        String

                resultMail=FileUtils.readFileToString(new File("src/test/resources/eml/results/pgpmail.eml"), StandardCharsets.UTF_8);
        resultMail=resultMail.replaceAll("----=_Part.*","");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);
    }

    @Test
    public void testPkcs7EmlExtractor() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testPkcs7EmlExtractor");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(false,
                true, true, 12, "windows-1252",
                true, true, true, true,
                true,2);
        MailExtractProgressLogger mepl= AllTests.initLogger("testPkcs7EmlExtractor");
        String urlString = StoreExtractor.composeStoreURL("eml", "", "", "",
                "src/test/resources/eml/TestPkcs7.eml");
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(urlString, "",
                "target/tmpJUnit/testPkcs7EmlExtractor", storeExtractorOptions, mepl);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,false)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,false)).isEqualTo(0);

        // mail eml
        String mail=FileUtils.readFileToString(new File("target/tmpJUnit/testPkcs7EmlExtractor/M#1-Re--Présenta/__BinaryMaster_1__-53878A4F.5080807-agriculture.go.eml"),defaultCharset());
        mail=mail.replaceAll("----=_Part.*","");
        String resultMail=FileUtils.readFileToString(new File("src/test/resources/eml/results/pkcs7mail.eml"), StandardCharsets.UTF_8);
        resultMail=resultMail.replaceAll("----=_Part.*","");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);
    }
}
