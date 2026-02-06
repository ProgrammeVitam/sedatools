/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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

public class TestPstStoreExtractor implements AllTests {

    @Test
    public void testGlobalPstExtractorSequentialTreament() throws MailExtractLibException, InterruptedException, IOException {
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
        storeExtractor.setMaxParallelThreads(1);

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,false)).isEqualTo(4);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,false)).isEqualTo(5);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,false)).isEqualTo(25);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,false)).isEqualTo(1);

        // sub element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,true)).isEqualTo(7);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,true)).isEqualTo(14);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,true)).isEqualTo(28);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,true)).isEqualTo(2);

        // mails extraction comparison
        String mails=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/messages.csv"),defaultCharset());
        mails=mails.replaceAll("/","\\\\");
        String resultMails=FileUtils.readFileToString(new File("src/test/resources/pst/results/messages.csv"), StandardCharsets.UTF_8);
        resultMails=resultMails.replaceAll("/","\\\\");
        assertThat(mails).isEqualToNormalizingNewlines(resultMails);

        // appointments extraction comparison except UniqID and ExceptionToID which may differ in parallel treatment
        String appointments=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/appointments.csv"),defaultCharset());
        appointments=AllTests.removeColumnsAndSortLines(appointments.replaceAll("/","\\\\"), new int[]{10,19});
        String resultAppointments=FileUtils.readFileToString(new File("src/test/resources/pst/results/appointments.csv"), StandardCharsets.UTF_8);
        resultAppointments=AllTests.removeColumnsAndSortLines(resultAppointments.replaceAll("/","\\\\"), new int[]{10,19});
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

    @Test
    public void testGlobalPstExtractorParallelTreament() throws MailExtractLibException, InterruptedException, IOException {
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

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,false)).isEqualTo(4);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,false)).isEqualTo(5);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,false)).isEqualTo(25);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,false)).isEqualTo(1);

        // sub element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class,true)).isEqualTo(7);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class,true)).isEqualTo(14);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class,true)).isEqualTo(28);
        assertThat(storeExtractor.getElementCounter(StoreContact.class,true)).isEqualTo(2);

        // mails extraction comparison except ID which may differ in parallel treatment
        String mails=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/messages.csv"),defaultCharset());
        mails=AllTests.removeColumnsAndSortLines(mails.replaceAll("/","\\\\"), new int[]{1});
        String resultMails=FileUtils.readFileToString(new File("src/test/resources/pst/results/messages.csv"), StandardCharsets.UTF_8);
        resultMails=AllTests.removeColumnsAndSortLines(resultMails.replaceAll("/","\\\\"), new int[]{1});
        assertThat(mails).isEqualToNormalizingNewlines(resultMails);

        // appointments extraction comparison except ID, UniqID and ExceptionToID which may differ in parallel treatment
        String appointments=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/appointments.csv"),defaultCharset());
        appointments=AllTests.removeColumnsAndSortLines(appointments.replaceAll("/","\\\\"), new int[]{1,10,19});
        String resultAppointments=FileUtils.readFileToString(new File("src/test/resources/pst/results/appointments.csv"), StandardCharsets.UTF_8);
        resultAppointments=AllTests.removeColumnsAndSortLines(resultAppointments.replaceAll("/","\\\\"), new int[]{1,10,19});
        assertThat(appointments).isEqualToNormalizingNewlines(resultAppointments);

        // contacts extraction
        String contacts=FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/contacts.csv"),defaultCharset());
        String resultContacts=FileUtils.readFileToString(new File("src/test/resources/pst/results/contacts.csv"), StandardCharsets.UTF_8);
        assertThat(contacts).isEqualToNormalizingNewlines(resultContacts);

        assertThat(new File("target/tmpJUnit/testGlobalPstExtractor/contacts/ContactPicture#1/__BinaryMaster_1__ContactPicture.jpg")).
                hasBinaryContent(FileUtils.readFileToByteArray(new File("src/test/resources/pst/results/ContactPicture.jpg")));

        // mail eml (first search for the good file name as the number is not predictable in parallel treatment)
        String mail=null;
        for (int i=55;i<=62;i++) {
            try {
                mail = FileUtils.readFileToString(new File("target/tmpJUnit/testGlobalPstExtractor/F#1-Début-du-fic/F#54-Éléments-env/M#"+i+"-Test-message/__BinaryMaster_1__-000c01d556d5-611cfc50-2356f4f0-.eml"), defaultCharset());
                break;
            } catch (IOException ignored) {
            }
        }
        assertThat(mail).isNotNull();
        mail=mail.replaceAll("----=_Part.*","");
        String resultMail=FileUtils.readFileToString(new File("src/test/resources/pst/results/mail.eml"), StandardCharsets.UTF_8);
        resultMail=resultMail.replaceAll("----=_Part.*","");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);
    }

}
