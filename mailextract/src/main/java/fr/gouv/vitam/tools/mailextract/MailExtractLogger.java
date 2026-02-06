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
package fr.gouv.vitam.tools.mailextract;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.nio.charset.Charset;

/**
 * A factory for creating MailExtractLogger objects.
 */
public class MailExtractLogger {

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
     * The global logger.
     */
    private static MailExtractLogger globalLogger = null;

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
    public static MailExtractLogger getGlobalLogger() {
        if (globalLogger == null) globalLogger = new MailExtractLogger(
            LoggerFactory.getLogger(MailExtractApp.class.getSimpleName()),
            GLOBAL
        );
        return globalLogger;
    }

    private MailExtractLogger(Logger logger, int progressLogLevel) {
        this.logger = logger;
        this.progressLogLevel = progressLogLevel;
    }

    public MailExtractLogger(String logPath, int progressLogLevel) {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(logCtx);
        consoleEncoder.setPattern(
            "%d{HH:mm:ss.SSS} [" + MailExtractApp.class.getSimpleName() + "] %-5level %marker - %msg%n"
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
        fileEncoder.setPattern(
            "%d{HH:mm:ss.SSS} [" + MailExtractApp.class.getSimpleName() + "] %-5level %marker - %msg%n"
        );
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

        ch.qos.logback.classic.Logger log = logCtx.getLogger(logPath);
        log.setLevel(ch.qos.logback.classic.Level.INFO);
        log.addAppender(logConsoleAppender);
        log.addAppender(logFileAppender);
        log.setAdditive(false);

        this.logger = log;
        this.progressLogLevel = progressLogLevel;
    }

    public static int getLevel(String levelName) throws MailExtractException {
        if (levelName.equals("OFF")) return -1;
        else if (levelName.equals(GLOBAL_MARKER.getName())) return GLOBAL;
        else if (levelName.equals(WARNING_MARKER.getName())) return WARNING;
        else if (levelName.equals(FOLDER_MARKER.getName())) return FOLDER;
        else if (levelName.equals(MESSAGE_GROUP_MARKER.getName())) return MESSAGE_GROUP;
        else if (levelName.equals(MESSAGE_MARKER.getName())) return MESSAGE;
        else if (levelName.equals(MESSAGE_DETAILS_MARKER.getName())) return MESSAGE_DETAILS;
        throw new MailExtractException("Unknown log level");
    }

    public static Marker getMarker(int level) {
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

    public void log(int level, String message) {
        if (level <= progressLogLevel) {
            if (logger != null) {
                if (level >= GLOBAL) logger.info(getMarker(level), message);
                else logger.error(message);
            }
        }
    }

    public Logger getProgressLogger() {
        return logger;
    }

    public int getProgressLogLevel() {
        return progressLogLevel;
    }

    public void close() {
        if (
            logger instanceof ch.qos.logback.classic.Logger
        ) ((ch.qos.logback.classic.Logger) logger).detachAndStopAllAppenders();
    }
}
