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
package fr.gouv.vitam.tools.mailextractlib.utils;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * The Class MailExtractProgressLogger.
 * <p>
 * Class for logging in standard logger but also send events used to follow a long process advancement.
 * To do that it calls a lambda function when asked for on an event (method progressLog) or when a counter is a
 * multiple of the "step" value defined at the MailExtractProgressLogger creation (method progressLogIfStep).This can be used for example to actualise a
 * progress dialog.
 * <p>
 * The progress levels are defined with java.util.logging level:
 * <ul>
 * <li>GLOBAL: all global process information and severe errors</li>
 * <li>WARNING: alert issued from treatment of one of elements </li>
 * <li>FOLDERS: information on the treatment of one extracted folder</li>
 * <li>MESSAGE_GROUPS: accumulation event of "step" value multiple message treatment (for exemple each 100 Messages)</li>
 * <li>MESSAGE: information on the treatment of one extracted message</li>
 * <li>MESSAGE_DETAILS : details, if any, on message extraction</li>
 * </ul>
 */
public class MailExtractProgressLogger {

    //** ProgressLog level. */
    public static final int GLOBAL = 10;
    public static final Marker GLOBAL_MARKER = MarkerFactory.getMarker("GLOBAL");
    public static final int WARNING = 20;
    public static final Marker WARNING_MARKER = MarkerFactory.getMarker("WARNING");
    public static final int FOLDER = 30;
    public static final Marker FOLDER_MARKER = MarkerFactory.getMarker("FOLDER");
    public static final int MESSAGE_GROUP = 40;
    public static final Marker MESSAGE_GROUP_MARKER = MarkerFactory.getMarker("MESSAGE_GROUP");
    public static final int MESSAGE = 50;
    public static final Marker MESSAGE_MARKER = MarkerFactory.getMarker("MESSAGE");
    public static final int MESSAGE_DETAILS = 60;
    public static final Marker MESSAGE_DETAILS_MARKER = MarkerFactory.getMarker("MESSAGE_DETAILS");

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
     * The debugFlag flag
     */
    private boolean debugFlag;

    /**
     * Instantiates a new SEDA lib progress logger.
     *
     * @param logger           the logger
     * @param progressLogLevel the progress log level
     */
    public MailExtractProgressLogger(Logger logger, int progressLogLevel) {
        this.progressLogFunc = null;
        this.logger = logger;
        this.step = Integer.MAX_VALUE;
        this.progressLogLevel = progressLogLevel;
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
    public MailExtractProgressLogger(Logger logger, int progressLogLevel, ProgressLogFunc progressConsumer, int step) {
        this.progressLogFunc = progressConsumer;
        this.logger = logger;
        this.step = step;
        this.progressLogLevel = progressLogLevel;
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
     * Log an exception.
     *
     * @param e the exception
     */
    public void logException(Exception e) {
        if (debugFlag)
            log(GLOBAL, getPrintStackTrace(e));
    }

    /**
     * Progress log, and log but with no interruption allowed.
     *
     * @param level the level
     * @param log   the log
     */
    public void progressLogWithoutInterruption(int level, String log) {
        if (level <= progressLogLevel) {
            if (progressLogFunc != null) {
                progressLogFunc.doprogressLog(-1, log);
            }
            log(level, log);
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
            case WARNING:
                return WARNING_MARKER;
            case FOLDER:
                return FOLDER_MARKER;
            case MESSAGE_GROUP:
                return MESSAGE_GROUP_MARKER;
            case MESSAGE:
                return MESSAGE_MARKER;
            case MESSAGE_DETAILS:
                return MESSAGE_DETAILS_MARKER;
        }
        return GLOBAL_MARKER;
    }

    /**
     * Gets level name.
     *
     * @return the level name
     */
    public String getLevelName() {
        return getMarker(progressLogLevel).getName();
    }

    /**
     * Log.
     *
     * @param level   the level
     * @param message the message
     */
    private void log(int level, String message) {
        if (level <= progressLogLevel) {
            if (logger != null)
                logger.info(getMarker(level), message);
        }
    }

    /**
     * Close.
     */
    public void close() {
    }

    // make a String from the stack trace
    private final static String getPrintStackTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter p = new PrintWriter(baos);

        e.printStackTrace(p);
        p.close();
        return baos.toString();
    }
}
