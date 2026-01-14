/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.utils.digest;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.nio.file.Path;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

/**
 * Common logging logic for digest computation.
 */
public class DigestProgressLogger {

    private static final long LOGGING_THRESHOLD = BinaryUnit.MEBI.toBytes(0);
    private static final long PROGRESS_STEP_MILLISECONDS = 1000;

    private final SEDALibProgressLogger logger;
    private final String filename;
    private final long fileSize;
    private final boolean logProgress;
    private final String unitLabel;
    private final long unitFactor;
    private long lastLoggedTime;

    /**
     * Instantiates a new Digest progress logger.
     *
     * @param logger   the progress logger
     * @param path     the file path
     * @param fileSize the file size
     * @param context  the context (e.g., "NIO" or "parallèle")
     */
    public DigestProgressLogger(SEDALibProgressLogger logger, Path path, long fileSize, String context) {
        this.logger = logger;
        this.filename = (path != null && path.getFileName() != null) ? path.getFileName().toString() : "inconnu";
        this.fileSize = fileSize;

        if (fileSize >= BinaryUnit.GIBI.toBytes()) {
            unitLabel = "Go";
            unitFactor = BinaryUnit.GIBI.toBytes();
        } else if (fileSize >= BinaryUnit.MEBI.toBytes()) {
            unitLabel = "Mo";
            unitFactor = BinaryUnit.MEBI.toBytes();
        } else {
            unitLabel = "Ko";
            unitFactor = BinaryUnit.KIBI.toBytes();
        }

        this.logProgress = logger != null && fileSize > LOGGING_THRESHOLD;
        this.lastLoggedTime = System.currentTimeMillis();

        if (this.logProgress) {
            doProgressLogWithoutInterruption(
                    logger,
                    GLOBAL,
                    String.format("Calcul du digest (%s) : %s (%.2f %s)",
                            context, filename, (double) fileSize / unitFactor, unitLabel),
                    null);
        }
    }

    /**
     * Logs progress based on total bytes read.
     *
     * @param bytesReadTotal total bytes read
     */
    public void logProgress(long bytesReadTotal) {
        if (!logProgress) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLoggedTime >= PROGRESS_STEP_MILLISECONDS) {
            lastLoggedTime = currentTime;
            int percent = (int) ((bytesReadTotal * 100) / fileSize);
            doProgressLogWithoutInterruption(
                    logger,
                    GLOBAL,
                    String.format("Calcul du digest [%s] : %d%% (%.2f / %.2f %s)",
                            filename, percent,
                            (double) bytesReadTotal / unitFactor,
                            (double) fileSize / unitFactor, unitLabel),
                    null);
        }
    }

    /**
     * Logs the end of the digest computation.
     */
    public void logEnd() {
        if (!logProgress) {
            return;
        }

        doProgressLogWithoutInterruption(
                logger,
                GLOBAL,
                String.format("Calcul du digest [%s] : 100%% (%.2f / %.2f %s) - Terminé",
                        filename,
                        (double) fileSize / unitFactor,
                        (double) fileSize / unitFactor, unitLabel),
                null);
    }
}
