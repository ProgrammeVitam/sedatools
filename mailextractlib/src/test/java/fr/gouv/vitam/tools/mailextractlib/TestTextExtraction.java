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
