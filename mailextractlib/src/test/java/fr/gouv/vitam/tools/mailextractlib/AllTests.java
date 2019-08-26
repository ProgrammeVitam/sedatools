package fr.gouv.vitam.tools.mailextractlib;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.MESSAGE_DETAILS;

public interface AllTests {

    static void initializeTests(String testName) throws IOException {
        FileUtils.deleteDirectory(new File("target/tmpJUnit/"+testName));
        StoreExtractor.initDefaultExtractors();
    }

    static MailExtractProgressLogger initLogger(String loggerName) {
        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(logCtx);
        consoleEncoder.setPattern("[" + loggerName + "] %-5level %marker - %msg%n");
        consoleEncoder.setCharset(Charset.forName("UTF-8"));
        consoleEncoder.start();

        ConsoleAppender<ILoggingEvent> logConsoleAppender = new ConsoleAppender<>();
        logConsoleAppender.setContext(logCtx);
        logConsoleAppender.setName("console");
        logConsoleAppender.setEncoder(consoleEncoder);
        logConsoleAppender.start();

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(logCtx);
        fileEncoder.setPattern("[" + loggerName + "] %-5level %marker - %msg%n");
        fileEncoder.setCharset(Charset.forName("UTF-8"));
        fileEncoder.start();

        FileAppender<ILoggingEvent> logFileAppender = new FileAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        logFileAppender.setContext(logCtx);
        logFileAppender.setName("logFile");
        logFileAppender.setEncoder(fileEncoder);
        logFileAppender.setAppend(true);
        logFileAppender.setFile("target/tmpJUnit/logs/" + loggerName + ".log");
        logFileAppender.start();

        ch.qos.logback.classic.Logger log = logCtx.getLogger(loggerName);
        log.setLevel(ch.qos.logback.classic.Level.INFO);
        log.addAppender(logConsoleAppender);
        log.addAppender(logFileAppender);
        log.setAdditive(false);

        return new MailExtractProgressLogger(log, MESSAGE_DETAILS);
    }
}