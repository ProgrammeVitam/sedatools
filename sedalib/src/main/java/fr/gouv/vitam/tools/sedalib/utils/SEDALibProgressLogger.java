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

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.time.Instant;

/**
 * The Class SEDALibProgressLogger.
 * <p>
 * Class for logging in standard logger but also send events used to follow a long process advancement.
 * To do that it calls a lambda function when asked for on an event (method progressLog), when a counter is a
 * multiple of the "step" value defined at the SEDALibProgressLogger creation or when the time past since previous
 * "step" log is more than the specified "stepDuration" (method progressLogIfStep).This can be
 * used for example to actualise a progress dialog.
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
        void doProgressLog(int count, String log);
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
     * The number of seconds expected between to "step" log publication.
     */
    private int stepDuration;

    /**
     * The last "step" log epoch seconds.
     */
    private long previousStepEpochSeconds;

    /**
     * The progressLogLevel.
     */
    private int progressLogLevel;

    /**
     * The progressLogLevel for progressFunc
     */
    private int progressFuncLogLevel;

    /**
     * The number used to determine if an accumulation as to be a progress log or not in progressFunc
     */
    private int progressFuncStep;

    /**
     * The debugFlag flag
     */
    private boolean debugFlag;

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
        this.progressFuncLogLevel = progressLogLevel;
        this.progressFuncStep = step;
        this.stepDuration = Integer.MAX_VALUE;
        this.previousStepEpochSeconds = Instant.now().getEpochSecond();
        this.debugFlag = false;
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
        this.progressFuncLogLevel = progressLogLevel;
        this.progressFuncStep = step;
        this.stepDuration = Integer.MAX_VALUE;
        this.previousStepEpochSeconds = Instant.now().getEpochSecond();
        this.debugFlag = false;
    }

    /**
     * Instantiates a new SEDA lib progress logger.
     *
     * @param logger           the standard logger
     * @param progressLogLevel the progress log level
     * @param progressConsumer the lambda function called to follow the progress
     * @param step             the step value
     * @param stepDuration     the step duration in seconds
     */
    public SEDALibProgressLogger(
        Logger logger,
        int progressLogLevel,
        ProgressLogFunc progressConsumer,
        int step,
        int stepDuration
    ) {
        this.progressLogFunc = progressConsumer;
        this.logger = logger;
        this.step = step;
        this.progressLogLevel = progressLogLevel;
        this.progressFuncLogLevel = progressLogLevel;
        this.progressFuncStep = step;
        this.stepDuration = stepDuration;
        this.previousStepEpochSeconds = Instant.now().getEpochSecond();
        this.debugFlag = false;
    }

    /**
     * Instantiates a new SEDA lib progress logger with all parameters.
     *
     * @param logger               the standard logger
     * @param progressLogLevel     the progress log level, controls which log messages are displayed
     * @param progressConsumer     the lambda function called to indicate progress updates
     * @param step                 the interval count for triggering progress logs
     * @param stepDuration         the duration threshold in seconds for triggering progress logs
     * @param progressFuncLogLevel the log level for the progress consumer function
     */
    public SEDALibProgressLogger(
        Logger logger,
        int progressLogLevel,
        ProgressLogFunc progressConsumer,
        int step,
        int stepDuration,
        int progressFuncLogLevel,
        int progressFuncStep
    ) {
        this.progressLogFunc = progressConsumer;
        this.logger = logger;
        this.step = step;
        this.progressLogLevel = progressLogLevel;
        this.progressFuncLogLevel = progressFuncLogLevel;
        this.progressFuncStep = progressFuncStep;
        this.stepDuration = stepDuration;
        this.previousStepEpochSeconds = Instant.now().getEpochSecond();
        this.debugFlag = false;
    }

    /**
     * Set debug flag.
     *
     * @param debugFlag the debug flag
     */
    public void setDebugFlag(boolean debugFlag) {
        this.debugFlag = debugFlag;
    }

    /**
     * Gets debug flag.
     *
     * @return the debug flag
     */
    public boolean getDebugFlag() {
        return debugFlag;
    }

    /**
     * Gets messages from the exception, and recursively from all causes, in a string.
     *
     * @param e the exception
     * @return the messages stack string
     */
    public static String getMessagesStackString(Throwable e) {
        String result;
        result = "-> " + e.getMessage();
        if (e.getCause() instanceof Exception) result += "\n" + getMessagesStackString(e.getCause());
        return result;
    }

    private static String getJavaStackString(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        return baos.toString();
    }

    /**
     * Gets java stacks from the exception, and recursively from all causes, in a string.
     *
     * @param e the exception
     * @return the all java stack string
     */
    public static String getAllJavaStackString(Throwable e) {
        String result;
        result = getJavaStackString(e);
        if (e.getCause() instanceof Exception) result +=
        "\n------------------------------------\n" + getJavaStackString(e.getCause());
        return result;
    }

    /**
     * Do progress log, and log with exception detail if any.
     *
     * @param spl   the SEDALib progress logger
     * @param level the level
     * @param log   the log
     * @param e     the exception
     */
    public static void doProgressLogWithoutInterruption(SEDALibProgressLogger spl, int level, String log, Throwable e) {
        if (spl != null) {
            if (level <= spl.progressLogLevel) {
                if (e != null) log += "\n" + getMessagesStackString(e);
                if ((spl.progressLogFunc != null) && (level <= spl.progressFuncLogLevel)) {
                    spl.progressLogFunc.doProgressLog(-1, log);
                }
                if ((e != null) && spl.debugFlag) log += "\n" + getAllJavaStackString(e);
                spl.log(level, log);
            }
        }
    }

    /**
     * Do progress log, and log with exception if debug flag set
     *
     * @param spl the SEDALib progress logger
     * @param log the log
     * @param e   the exception
     */
    public static void doProgressLogIfDebug(SEDALibProgressLogger spl, String log, Throwable e) {
        if ((spl != null) && spl.debugFlag) {
            doProgressLogWithoutInterruption(spl, GLOBAL, log, e);
        }
    }

    /**
     * Do progress log, and log with exception detail if any, and wait 1ms to allow interruption
     *
     * @param spl   the SEDALib progress logger
     * @param level the level
     * @param log   the log
     * @param e     the exception
     * @throws InterruptedException the interrupted exception
     */
    public static void doProgressLog(SEDALibProgressLogger spl, int level, String log, Exception e)
        throws InterruptedException {
        if (spl != null) {
            doProgressLogWithoutInterruption(spl, level, log, e);
            Thread.sleep(1);
        }
    }

    /**
     * Do progress log if the counter is a step multiple.
     *
     * @param spl   the SEDALib progress logger
     * @param level the level
     * @param count the count
     * @param log   the log
     * @throws InterruptedException the interrupted exception
     */
    public static void doProgressLogIfStep(SEDALibProgressLogger spl, int level, int count, String log)
        throws InterruptedException {
        if (spl != null) {
            if (level <= spl.progressLogLevel) {
                long nowEpochSeconds = Instant.now().getEpochSecond();
                if (spl.stepDuration < nowEpochSeconds - spl.previousStepEpochSeconds) {
                    if (
                        (spl.progressLogFunc != null) && (level <= spl.progressFuncLogLevel)
                    ) spl.progressLogFunc.doProgressLog(count, (count % spl.progressFuncStep == 0 ? "" : " * ") + log);
                    spl.log(level, log);
                    Thread.sleep(1);
                    spl.previousStepEpochSeconds = nowEpochSeconds;
                    return;
                }
                if ((count % spl.step) == 0) {
                    spl.log(level, log);
                }
                if ((spl.progressLogFunc != null) && (count % spl.progressFuncStep) == 0) {
                    spl.progressLogFunc.doProgressLog(count, log);
                    Thread.sleep(1);
                }
            }
        }
    }

    /**
     * Readable file size.
     *
     * @param size the size
     * @return the string
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
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

    private void log(int level, String message) {
        if (level <= progressLogLevel) {
            if (logger != null) logger.info(getMarker(level), message);
        }
    }
}
