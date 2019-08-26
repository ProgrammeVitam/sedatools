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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.time.Instant;

/**
 * The Class MailExtractProgressLogger.
 * <p>
 * Class for logging in standard logger but also send events used to follow a long process advancement.
 * To do that it calls a lambda function when asked for on an event (method progressLog), when a counter is a
 * multiple of the "step" value defined at the SEDALibProgressLogger creation or when the time past since previous
 * "step" log is more than the specified "stepDuration" (method progressLogIfStep).This can be
 * used for example to actualise a progress dialog.
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
    public MailExtractProgressLogger(Logger logger, int progressLogLevel, ProgressLogFunc progressConsumer, int step) {
        this.progressLogFunc = progressConsumer;
        this.logger = logger;
        this.step = step;
        this.progressLogLevel = progressLogLevel;
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
     * @param stepDuration     the step duration
     */
    public MailExtractProgressLogger(Logger logger, int progressLogLevel, ProgressLogFunc progressConsumer, int step,int stepDuration) {
        this.progressLogFunc = progressConsumer;
        this.logger = logger;
        this.step = step;
        this.progressLogLevel = progressLogLevel;
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
    static public String getMessagesStackString(Throwable e) {
        String result;
        result = "-> " + e.getMessage();
        if (e.getCause() instanceof Exception)
            result += "\n" + getMessagesStackString((Exception) e.getCause());
        return result;
    }

    static private String getJavaStackString(Throwable e) {
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
    static public String getAllJavaStackString(Throwable e) {
        String result;
        result = getJavaStackString(e);
        if (e.getCause() instanceof Exception)
            result += "\n------------------------------------\n" + getJavaStackString((Exception) e.getCause());
        return result;
    }

    /**
     * Do progress log, and log with exception detail if any.
     *
     * @param mepl   the SEDALib progress logger
     * @param level the level
     * @param log   the log
     * @param e     the exception
     */
    static public void doProgressLogWithoutInterruption(MailExtractProgressLogger mepl, int level, String log, Throwable e) {
        if (mepl!=null) {
            if (level <= mepl.progressLogLevel) {
                if (e != null)
                    log += "\n" + getMessagesStackString(e);
                if (mepl.progressLogFunc != null) {
                    mepl.progressLogFunc.doProgressLog(-1, log);
                }
                if ((e != null) && mepl.debugFlag)
                    log += "\n" + getAllJavaStackString(e);
                mepl.log(level, log);
            }
        }
    }

    /**
     * Do progress log, and log with exception detail if any, and wait 1ms to allow interruption
     *
     * @param mepl   the MailExtract progress logger
     * @param level the level
     * @param log   the log
     * @param e     the exception
     * @throws InterruptedException the interrupted exception
     */
    static public void doProgressLog(MailExtractProgressLogger mepl, int level, String log, Throwable e) throws InterruptedException {
        if (mepl != null) {
            doProgressLogWithoutInterruption(mepl, level, log, e);
            Thread.sleep(1);
        }
    }

    /**
     * Do progress log if the counter is a step multiple.
     *
     * @param mepl  the MailExtract progress logger
     * @param level the level
     * @param count the count
     * @param log   the log
     * @throws InterruptedException the interrupted exception
     */
    static public void doProgressLogIfStep(MailExtractProgressLogger mepl, int level, int count, String log) throws InterruptedException {
        if (mepl!=null) {
            if (level <= mepl.progressLogLevel) {
                long nowEpochSeconds = Instant.now().getEpochSecond();
                int mod = count % mepl.step;
                if ((mod == 0) || (mepl.stepDuration < nowEpochSeconds - mepl.previousStepEpochSeconds)) {
                    if (mepl.progressLogFunc != null)
                        mepl.progressLogFunc.doProgressLog(count, (mod == 0 ? "" : "* ") + log);
                    mepl.log(level, log);
                    Thread.sleep(1);
                    mepl.previousStepEpochSeconds = nowEpochSeconds;
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
}
