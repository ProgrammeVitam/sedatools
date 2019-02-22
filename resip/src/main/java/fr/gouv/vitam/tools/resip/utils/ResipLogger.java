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

import java.nio.charset.Charset;

/**
 * A factory for creating ResipLogger objects.
 */
public class ResipLogger {

    //** ProgressLog level. */
    public static final int ERROR = 0;
    public static final Marker ERROR_MARKER = MarkerFactory.getMarker("ERROR");
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
     * Gets the app logger.
     *
     * @return the app logger
     */
    public static ResipLogger getGlobalLogger() {
        if (globalLogger == null)
            globalLogger = new ResipLogger(LoggerFactory.getLogger(ResipApp.class.getSimpleName()), GLOBAL);
        return globalLogger;
    }


    public ResipLogger(Logger logger, int progressLogLevel) {
        this.logger = logger;
        this.progressLogLevel = progressLogLevel;
     }

    public static void createGlobalLogger(String logPath, int progressLogLevel) {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(logCtx);
        consoleEncoder.setPattern("%d{HH:mm:ss.SSS} ["+ResipApp.class.getSimpleName()+"] %-5level %marker - %msg%n");
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            consoleEncoder.setCharset(Charset.forName("cp850"));
        else
            consoleEncoder.setCharset(Charset.forName("UTF-8"));
        consoleEncoder.start();

        ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
        logConsoleAppender.setContext(logCtx);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(consoleEncoder);
        logConsoleAppender.start();

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(logCtx);
        fileEncoder.setPattern("%d{HH:mm:ss.SSS} ["+ResipApp.class.getSimpleName()+"] %-5level %marker - %msg%n");
        fileEncoder.setCharset(Charset.forName("UTF-8"));
        fileEncoder.start();

        FileAppender<ch.qos.logback.classic.spi.ILoggingEvent> logFileAppender = new FileAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
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

        if (globalLogger!=null)
            globalLogger.close();
        globalLogger = new ResipLogger(log, progressLogLevel);
    }

    public static int getLevel(String levelName) throws ResipException {
        if (levelName.equals("OFF"))
            return -1;
        else if (levelName.equals(ERROR_MARKER.getName()))
            return ERROR;
        else if (levelName.equals(GLOBAL_MARKER.getName()))
            return GLOBAL;
        else if (levelName.equals(STEP_MARKER.getName()))
            return STEP;
        else if (levelName.equals(OBJECTS_GROUP_MARKER.getName()))
            return OBJECTS_GROUP;
        else if (levelName.equals(OBJECTS_MARKER.getName()))
            return OBJECTS;
        else if (levelName.equals(OBJECTS_WARNINGS_MARKER.getName()))
            return OBJECTS_WARNINGS;
        throw new ResipException("Niveau de log inconnu");
    }

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

    public void log(int level, String message) {
        if (level <= progressLogLevel) {
            if (logger != null) {
                if (level >= GLOBAL)
                    logger.info(getMarker(level), message);
                else logger.error(message);

            }
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public int getProgressLogLevel() {
        return progressLogLevel;
    }

    public void close(){
        if (logger instanceof ch.qos.logback.classic.Logger)
            ((ch.qos.logback.classic.Logger)logger).detachAndStopAllAppenders();
    }
}
