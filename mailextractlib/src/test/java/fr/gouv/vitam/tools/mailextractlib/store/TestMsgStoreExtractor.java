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

public class TestMsgStoreExtractor implements AllTests {

    @Test
    public void testGlobalMsgExtractor() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testGlobalMsgExtractor");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(
            false,
            true,
            true,
            12,
            "windows-1252",
            true,
            true,
            true,
            true,
            true,
            2
        );
        MailExtractProgressLogger mepl = AllTests.initLogger("testGlobalMsgExtractor");
        String urlString = StoreExtractor.composeStoreURL(
            "msg",
            "",
            "",
            "",
            "src/test/resources/msg/Test message 2.msg"
        );
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(
            urlString,
            "",
            "target/tmpJUnit/testGlobalMsgExtractor",
            storeExtractorOptions,
            mepl
        );

        //when
        storeExtractor.extractAllFolders();
        storeExtractor.endStoreExtractor();

        //then

        // element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class, false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class, false)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class, false)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class, false)).isEqualTo(0);

        // sub element counters
        assertThat(storeExtractor.getElementCounter(StoreFolder.class, true)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreMessage.class, true)).isEqualTo(1);
        assertThat(storeExtractor.getElementCounter(StoreAppointment.class, true)).isEqualTo(0);
        assertThat(storeExtractor.getElementCounter(StoreContact.class, true)).isEqualTo(0);

        // embedded mail eml
        String mail = FileUtils.readFileToString(
            new File(
                "target/tmpJUnit/testGlobalMsgExtractor/M#1-Test-message/M#2-Test-message/__BinaryMaster_1__-003e01d556d3-b3116ed0-19344c70-.eml"
            ),
            defaultCharset()
        );
        mail = mail.replaceAll("----=_Part.*", "");
        String resultMail = FileUtils.readFileToString(
            new File("src/test/resources/msg/results/embeddedMail.eml"),
            StandardCharsets.UTF_8
        );
        resultMail = resultMail.replaceAll("----=_Part.*", "");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);

        // mail eml
        mail = FileUtils.readFileToString(
            new File(
                "target/tmpJUnit/testGlobalMsgExtractor/M#1-Test-message/__BinaryMaster_1__-000c01d556d5-611cfc50-2356f4f0-.eml"
            ),
            defaultCharset()
        );
        mail = mail.replaceAll("----=_Part.*", "");
        resultMail = FileUtils.readFileToString(
            new File("src/test/resources/msg/results/mail.eml"),
            StandardCharsets.UTF_8
        );
        resultMail = resultMail.replaceAll("----=_Part.*", "");
        assertThat(mail).isEqualToNormalizingNewlines(resultMail);
    }
}
