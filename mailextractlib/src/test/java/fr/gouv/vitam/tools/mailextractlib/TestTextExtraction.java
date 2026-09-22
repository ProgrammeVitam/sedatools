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
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTextExtraction {

    /**
     * Records every process the JVM starts, so the test can assert that apache-tika did not shell out
     * to an external tool (tesseract, ffmpeg...) behind our back.
     * <p>
     * This used to be done with a SecurityManager, which Java 21 refuses to install and Java 24 removed
     * altogether. The jdk.ProcessStart flight recorder event is the equivalent that survives: like the
     * SecurityManager it is JVM-wide, so it also catches a process started from another thread, which a
     * mock on ProcessBuilder would miss since mockito's construction mocks are thread-local.
     */
    private static Recording processRecording;

    @BeforeAll
    public static void setup() {
        processRecording = new Recording();
        processRecording.enable("jdk.ProcessStart");
        processRecording.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        processRecording.stop();
        Path dump = Files.createTempFile("testTextExtraction-processes", ".jfr");
        try {
            processRecording.dump(dump);
            assertThat(startedProcesses(dump)).as("processes started while extracting text").isEmpty();
        } finally {
            processRecording.close();
            Files.deleteIfExists(dump);
        }
    }

    private static List<String> startedProcesses(Path dump) throws IOException {
        List<String> commands = new ArrayList<>();
        try (RecordingFile recordingFile = new RecordingFile(dump)) {
            while (recordingFile.hasMoreEvents()) {
                RecordedEvent event = recordingFile.readEvent();
                if ("jdk.ProcessStart".equals(event.getEventType().getName())) {
                    commands.add(event.getString("command"));
                }
            }
        }
        return commands;
    }

    @Test
    public void testTextExtraction() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testTextExtraction");
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(
            false,
            true,
            true,
            12,
            "windows-1252",
            false,
            true,
            true,
            true,
            true,
            2
        );
        MailExtractProgressLogger mepl = AllTests.initLogger("testTextExtraction");
        String urlString = StoreExtractor.composeStoreURL(
            "eml",
            "",
            "",
            "",
            "src/test/resources/textextraction/Test text extraction.eml"
        );
        StoreExtractor storeExtractor = StoreExtractor.createStoreExtractor(
            urlString,
            "",
            "target/tmpJUnit/testTextExtraction",
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

        // compare txt and xml extracted
        AllTests.assertThatDirectoriesContainSameFilesWithExtensions(
            "src/test/resources/textextraction/results/M#1-Test-extract",
            "target/tmpJUnit/testTextExtraction/M#1-Test-extract",
            new String[] { "txt", "xml" }
        );
    }
}
