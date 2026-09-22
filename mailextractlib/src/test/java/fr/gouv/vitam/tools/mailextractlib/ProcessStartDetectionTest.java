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

import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the guard.
 * <p>
 * TestTextExtraction asserts that no external process is started while tika extracts text, using the
 * jdk.ProcessStart flight recorder event. A detector that quietly stopped detecting would leave that
 * test green while watching nothing, so this test starts processes on purpose and checks they are seen.
 * The other-thread case is the one that matters: it is precisely what a mockito construction mock on
 * ProcessBuilder fails to catch, since such mocks are thread-local.
 */
class ProcessStartDetectionTest {

    @Test
    public void detectsProcessesStartedFromAnyThread() throws Exception {
        Recording recording = new Recording();
        recording.enable("jdk.ProcessStart");
        recording.start();

        new ProcessBuilder("echo", "same-thread").start().waitFor();

        Thread other = new Thread(() -> {
            try {
                new ProcessBuilder("echo", "other-thread").start().waitFor();
            } catch (IOException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
        other.start();
        other.join();

        Runtime.getRuntime().exec(new String[] { "echo", "runtime-exec" }).waitFor();

        recording.stop();
        Path dump = Files.createTempFile("processStartDetection", ".jfr");
        try {
            recording.dump(dump);
            assertThat(startedProcesses(dump))
                .as("commands seen by the jdk.ProcessStart detector")
                .contains("echo same-thread", "echo other-thread", "echo runtime-exec");
        } finally {
            recording.close();
            Files.deleteIfExists(dump);
        }
    }

    @Test
    public void reportsNothingWhenNoProcessIsStarted() throws Exception {
        Recording recording = new Recording();
        recording.enable("jdk.ProcessStart");
        recording.start();
        recording.stop();

        Path dump = Files.createTempFile("processStartDetection-empty", ".jfr");
        try {
            recording.dump(dump);
            assertThat(startedProcesses(dump)).isEmpty();
        } finally {
            recording.close();
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
}
