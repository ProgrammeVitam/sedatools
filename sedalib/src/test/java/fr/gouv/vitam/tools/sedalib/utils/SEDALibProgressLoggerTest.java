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
package fr.gouv.vitam.tools.sedalib.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.OBJECTS;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.OBJECTS_GROUP;
import static org.junit.jupiter.api.Assertions.*;

class SEDALibProgressLoggerTest {

    private static final int FILTERED_OUT_CALLS = 20000;

    @AfterEach
    void clearInterruptedFlag() {
        Thread.interrupted();
    }

    /**
     * Non regression on the import duration: doProgressLog used to end with a Thread.sleep(1) placed
     * outside of the log level test, so every unzipped file, DataObjectGroup, BinaryDataObject and
     * ArchiveUnit cost at least one sleep even when the message was filtered out and displayed
     * nowhere. On Windows, where a 1ms sleep really lasts up to 15.6ms, that alone turned the import
     * of a large SIP into a half hour of pure sleeping, during which the Traiter and Export menus
     * stayed greyed out.
     */
    @Test
    void shouldNotWaitOnMessagesFilteredOutByLogLevel() throws InterruptedException {
        SEDALibProgressLogger spl = new SEDALibProgressLogger(null, OBJECTS_GROUP);

        long start = System.nanoTime();
        for (int i = 0; i < FILTERED_OUT_CALLS; i++) {
            SEDALibProgressLogger.doProgressLog(spl, OBJECTS, "sedalib: objet [" + i + "] importé", null);
        }
        long durationMs = (System.nanoTime() - start) / 1_000_000;

        // one sleep per call would be at least 20s here, and around 5mn on Windows
        assertTrue(
            durationMs < 2000,
            FILTERED_OUT_CALLS + " appels filtrés ont pris " + durationMs + "ms, un délai est réapparu par appel"
        );
    }

    @Test
    void shouldThrowWhenThreadIsInterrupted() {
        SEDALibProgressLogger spl = new SEDALibProgressLogger(null, OBJECTS_GROUP);

        Thread.currentThread().interrupt();

        assertThrows(
            InterruptedException.class,
            () -> SEDALibProgressLogger.doProgressLog(spl, OBJECTS, "sedalib: objet importé", null)
        );
        assertFalse(Thread.currentThread().isInterrupted(), "le drapeau d'interruption doit avoir été consommé");
    }

    @Test
    void shouldThrowFromStepLogWhenThreadIsInterrupted() {
        SEDALibProgressLogger spl = new SEDALibProgressLogger(null, OBJECTS_GROUP, (count, log) -> {}, 1);

        Thread.currentThread().interrupt();

        assertThrows(
            InterruptedException.class,
            () -> SEDALibProgressLogger.doProgressLogIfStep(spl, OBJECTS_GROUP, 1, "1 fichier extrait")
        );
        assertFalse(Thread.currentThread().isInterrupted(), "le drapeau d'interruption doit avoir été consommé");
    }

    @Test
    void shouldNotThrowWhenThreadIsNotInterrupted() {
        SEDALibProgressLogger spl = new SEDALibProgressLogger(null, OBJECTS_GROUP);

        assertDoesNotThrow(
            () -> SEDALibProgressLogger.doProgressLog(spl, OBJECTS_GROUP, "sedalib: import terminé", null)
        );
    }
}
