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
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.utils;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.text.DecimalFormat;

/**
 * The Class SEDALibProgressLogger.
 * <p>
 * Class for logging in standard logger but also send events used to follow a long process advancement.
 * To do that it calls a lambda function when asked for on an event (method progressLog) or when a counter is a
 * multiple of the "step" value defined at the SEDALibProgressLogger creation (method progressLogIfStep).This can be used for example to actualise a
 * progress dialog.
 * <p>
 * The progress levels are defined with java.util.logging level:
 * <ul>
 * <li>GLOBAL: global information on the process</li>
 * <li>STEP: information on treatment steps of one uniq element (for exemple DataObjectPackage treatment steps when
 * imported) </li>
 * <li>OBJECTS_GROUPS: accumulation event of "step" value multiple elements treatment (for exemple each 1000 ArchiveUnits)</li>
 * <li>OBJECTS: information on the treatment of one of multiple elements (ArchiveUnits, DataObjects...)</li>
 * <li>OBJECTS_WARNINGS : alert issued from treatment of one of multiple elements (ArchiveUnits, DataObjects...)
 * </li>
 * </ul>
 */
public class SEDALibProgressLogger {

    //** ProgressLog level. */
    public static final int GLOBAL = 10;
    public static final Marker GLOBAL_MARKER = MarkerFactory.getMarker("GLOBAL");
    public static final int STEP = 20;
    public static final Marker STEP_MARKER = MarkerFactory.getMarker("STEP");
    public static final int OBJECTS_GROUP = 30;
    public static final Marker OBJECTS_GROUP_MARKER = MarkerFactory.getMarker("OBJECTS_GROUP");
    public static final int OBJECTS = 40;
    public static final Marker OBJECTS_MARKER = MarkerFactory.getMarker("OBJECTS");
    public static final int OBJECTS_WARNINGS = 50;
    public static final Marker OBJECTS_WARNINGS_MARKER = MarkerFactory.getMarker("OBJECTS_WARNINGS");

    /**
     * The Interface ProgressLogFunc.
     */
    @FunctionalInterface
    public interface ProgressLogFunc {

        /**
         * Do progress log.
         *
         * @param count the count
         * @param log   the log
         */
        void doprogressLog(int count, String log);
    }

    /**
     * The progress log func.
     */
    private ProgressLogFunc progressLogFunc;

    /**
     * The logger.
     */
    private Logger logger;

    /**
     * The number used to determine if an accumulation as to be a progress log or not.
     */
    private int step;

    /**
     * The progressLogLevel.
     */
    private int progressLogLevel;

    /**
     * Instantiates a new SEDA lib progress logger.
     *
     * @param logger           the logger
     * @param progressLogLevel the progress log level
     */
    public SEDALibProgressLogger(Logger logger, int progressLogLevel) {
        this.progressLogFunc = null;
        this.logger = logger;
        this.step = Integer.MAX_VALUE;
        this.progressLogLevel = progressLogLevel;
    }

    /**
     * Instantiates a new SEDA lib progress logger.
     *
     * @param logger           the standard logger
     * @param progressLogLevel the progress log level
     * @param progressConsumer the lambda function called to follow the progress
     * @param step             the step value
     */
    public SEDALibProgressLogger(Logger logger, int progressLogLevel, ProgressLogFunc progressConsumer, int step) {
        this.progressLogFunc = progressConsumer;
        this.logger = logger;
        this.step = step;
        this.progressLogLevel = progressLogLevel;
    }

    /**
     * Progress log if the counter is a step multiple.
     *
     * @param level the level
     * @param count the count
     * @param log   the log
     * @throws InterruptedException the interrupted exception
     */
    public void progressLogIfStep(int level, int count, String log) throws InterruptedException {
        if (level <= progressLogLevel) {
            if ((count == 0) || (count % step != 0))
                return;
            if (progressLogFunc != null) {
                progressLogFunc.doprogressLog(count, log);
            }
            log(level, log);
            Thread.sleep(1);
        }
    }

    /**
     * Progress log, and log.
     *
     * @param level the level
     * @param log   the log
     * @throws InterruptedException the interrupted exception
     */
    public void progressLog(int level, String log) throws InterruptedException {
        if (level <= progressLogLevel) {
            if (progressLogFunc != null) {
                progressLogFunc.doprogressLog(-1, log);
            }
            log(level, log);
            Thread.sleep(1);
        }
    }

    /**
     * Readable file size.
     *
     * @param size the size
     * @return the string
     */
    public static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private Marker getMarker(int level) {
        switch (level) {
            case GLOBAL:
                return GLOBAL_MARKER;
            case STEP:
                return STEP_MARKER;
            case OBJECTS_GROUP:
                return OBJECTS_GROUP_MARKER;
            case OBJECTS:
                return OBJECTS_MARKER;
            case OBJECTS_WARNINGS:
                return OBJECTS_WARNINGS_MARKER;
        }
        return GLOBAL_MARKER;
    }

    /**
     * Log only (no call to progress log function).
     *
     * @param level   the level
     * @param message the message
     */
    public void log(int level, String message) {
        if (level <= progressLogLevel) {
            if (logger != null)
                logger.info(getMarker(level), message);
        }
    }
}
