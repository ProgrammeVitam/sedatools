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
package fr.gouv.vitam.tools.resip.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import fr.gouv.vitam.tools.resip.app.ResipApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * A factory for creating ResipLogger objects.
 */
public class ResipLogger {

    /**
     * The constant ERROR.
     */
    //** ProgressLog level. */
    public static final int ERROR = 0;
    /**
     * The constant ERROR_MARKER.
     */
    public static final Marker ERROR_MARKER = MarkerFactory.getMarker("ERROR");
    /**
     * The constant GLOBAL.
     */
    public static final int GLOBAL = 10;
    /**
     * The constant GLOBAL_MARKER.
     */
    public static final Marker GLOBAL_MARKER = MarkerFactory.getMarker("GLOBAL");
    /**
     * The constant STEP.
     */
    public static final int STEP = 20;
    /**
     * The constant STEP_MARKER.
     */
    public static final Marker STEP_MARKER = MarkerFactory.getMarker("STEP");
    /**
     * The constant OBJECTS_GROUP.
     */
    public static final int OBJECTS_GROUP = 30;
    /**
     * The constant OBJECTS_GROUP_MARKER.
     */
    public static final Marker OBJECTS_GROUP_MARKER = MarkerFactory.getMarker("OBJECTS_GROUP");
    /**
     * The constant OBJECTS.
     */
    public static final int OBJECTS = 40;
    /**
     * The constant OBJECTS_MARKER.
     */
    public static final Marker OBJECTS_MARKER = MarkerFactory.getMarker("OBJECTS");
    /**
     * The constant OBJECTS_WARNINGS.
     */
    public static final int OBJECTS_WARNINGS = 50;
    /**
     * The constant OBJECTS_WARNINGS_MARKER.
     */
    public static final Marker OBJECTS_WARNINGS_MARKER = MarkerFactory.getMarker("OBJECTS_WARNINGS");

    /**
     * The global logger.
     */
    private static ResipLogger globalLogger = null;

    /**
     * The logger.
     */
    private Logger logger;

    /**
     * The progressLogLevel.
     */
    private int progressLogLevel;

    /**
     * The debugFlag flag
     */
    private boolean debugFlag;

    /**
     * Gets the app logger.
     *
     * @return the app logger
     */
    public static ResipLogger getGlobalLogger() {
        if (globalLogger == null) globalLogger = new ResipLogger(
            LoggerFactory.getLogger(ResipApp.class.getSimpleName()),
            GLOBAL
        );
        return globalLogger;
    }

    /**
     * Instantiates a new Resip logger.
     *
     * @param logger           the logger
     * @param progressLogLevel the progress log level
     */
    public ResipLogger(Logger logger, int progressLogLevel) {
        this.logger = logger;
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
     * Create global logger.
     *
     * @param logPath          the log path
     * @param progressLogLevel the progress log level
     */
    public static void createGlobalLogger(String logPath, int progressLogLevel) {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(logCtx);
        consoleEncoder.setPattern(
            "%d{HH:mm:ss.SSS} [" + ResipApp.class.getSimpleName() + "] %-5level %marker - %msg%n"
        );
        if (System.getProperty("os.name").toLowerCase().contains("win")) consoleEncoder.setCharset(
            Charset.forName("cp850")
        );
        else consoleEncoder.setCharset(Charset.forName("UTF-8"));
        consoleEncoder.start();

        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
        logConsoleAppender.setContext(logCtx);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(consoleEncoder);
        logConsoleAppender.start();

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(logCtx);
        fileEncoder.setPattern("%d{HH:mm:ss.SSS} [" + ResipApp.class.getSimpleName() + "] %-5level %marker - %msg%n");
        fileEncoder.setCharset(Charset.forName("UTF-8"));
        fileEncoder.start();

        FileAppender<ch.qos.logback.classic.spi.ILoggingEvent> logFileAppender = new FileAppender<
            ch.qos.logback.classic.spi.ILoggingEvent
        >();
        logFileAppender.setContext(logCtx);
        logFileAppender.setName("logFile");
        logFileAppender.setEncoder(fileEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile(logPath);
        logFileAppender.start();

        ch.qos.logback.classic.Logger log = logCtx.getLogger("Global");
        log.setLevel(ch.qos.logback.classic.Level.INFO);
        log.addAppender(logConsoleAppender);
        log.addAppender(logFileAppender);

        if (globalLogger != null) globalLogger.close();
        globalLogger = new ResipLogger(log, progressLogLevel);
    }

    /**
     * Gets level.
     *
     * @param levelName the level name
     * @return the level
     * @throws ResipException the resip exception
     */
    public static int getLevel(String levelName) throws ResipException {
        if (levelName.equals("OFF")) return -1;
        else if (levelName.equals(ERROR_MARKER.getName())) return ERROR;
        else if (levelName.equals(GLOBAL_MARKER.getName())) return GLOBAL;
        else if (levelName.equals(STEP_MARKER.getName())) return STEP;
        else if (levelName.equals(OBJECTS_GROUP_MARKER.getName())) return OBJECTS_GROUP;
        else if (levelName.equals(OBJECTS_MARKER.getName())) return OBJECTS;
        else if (levelName.equals(OBJECTS_WARNINGS_MARKER.getName())) return OBJECTS_WARNINGS;
        throw new ResipException("Niveau de log inconnu");
    }

    /**
     * Gets marker.
     *
     * @param level the level
     * @return the marker
     */
    public static Marker getMarker(int level) {
        switch (level) {
            case ERROR:
                return ERROR_MARKER;
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
     * Gets messages from the exception, and recursively from all causes, in a string.
     *
     * @param e the exception
     * @return the messages stack string
     */
    public static String getMessagesStackString(Throwable e) {
        String result;
        result = "-> " + e.getMessage();
        if (e.getCause() instanceof Exception) result += "\n" + getMessagesStackString((Exception) e.getCause());
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
        "\n------------------------------------\n" + getJavaStackString((Exception) e.getCause());
        return result;
    }

    /**
     * Log.
     *
     * @param level   the level
     * @param message the message
     * @param e       the exception
     */
    public void log(int level, String message, Throwable e) {
        if (level <= progressLogLevel) {
            if (logger != null) {
                if (level >= GLOBAL) logger.info(getMarker(level), message);
                else {
                    if (e != null) {
                        message += "\n" + getMessagesStackString(e);
                        message += "\n" + getAllJavaStackString(e);
                    }
                    logger.error(ERROR_MARKER, message);
                }
            }
        }
    }

    /**
     * Log if debug flag set.
     *
     * @param message the message
     * @param e       the exception
     */
    public void logIfDebug(String message, Throwable e) {
        if (debugFlag) {
            if (e != null) {
                message += "\n" + getMessagesStackString(e);
                message += "\n" + getAllJavaStackString(e);
                logger.error(ERROR_MARKER, message);
            } else logger.info(message);
        }
    }

    /**
     * Gets logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets progress log level.
     *
     * @return the progress log level
     */
    public int getProgressLogLevel() {
        return progressLogLevel;
    }

    /**
     * Close.
     */
    public void close() {
        if (
            logger instanceof ch.qos.logback.classic.Logger
        ) ((ch.qos.logback.classic.Logger) logger).detachAndStopAllAppenders();
    }
}
