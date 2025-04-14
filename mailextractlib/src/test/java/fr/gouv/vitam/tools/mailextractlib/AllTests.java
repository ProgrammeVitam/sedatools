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
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.MESSAGE_DETAILS;

public interface AllTests {

    static void initializeTests(String testName) throws IOException {
        File testDir=new File("target/tmpJUnit/"+testName);
        if (testDir.isDirectory())
            FileUtils.deleteDirectory(testDir);
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

    /**
     * Sorts the lines in the given input string in alphabetical order after removing the specified columns
     * from each line.
     * <p>This is particularly useful for comparing CSV lists generated in parallel, where some columns with IDs
     * might differ across executions.</p>
     *
     * @param input           The input string containing multiple lines separated by "\n".
     * @param columnsToRemove An array of 1-based indices of the columns to remove.
     * @return A new string with the lines sorted alphabetically after removing the specified columns.
     */
    public static String removeColumnsAndSortLines(String input, int[] columnsToRemove) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Split the input string into lines
        String[] lines = input.replace("\r", "").split("\n");
        lines = removeColumnsFromLines(lines, columnsToRemove);

        // Convert to a list for sorting
        List<String> lineList = Arrays.asList(lines);

        // Sort the list in alphabetical order
        Collections.sort(lineList);

        // Rejoin the sorted lines into a single string
        return String.join("\n", lineList);
    }


    /**
     * Removes the specified columns from each line of a given array of CSV strings.
     *
     * @param lines           An array of strings containing the CSV lines to process.
     * @param columnsToRemove An array of 1-based indices of the columns to remove.
     * @return A new array containing the CSV lines without the specified columns.
     */
    static String[] removeColumnsFromLines(String[] lines, int[] columnsToRemove) {
        String[] result = new String[lines.length];
        HashSet<Integer> columnsToRemoveAsSet = new HashSet<>();

        // Convert 1-based column indices to 0-based for internal processing.
        for (int column : columnsToRemove) {
            columnsToRemoveAsSet.add(column - 1);
        }

        for (int i = 0; i < lines.length; i++) {
            String[] columns = lines[i].split(";");
            StringBuilder newLine = new StringBuilder();
            for (int j = 0; j < columns.length; j++) {
                if (!columnsToRemoveAsSet.contains(j)) {
                    if (newLine.length() > 0) {
                        newLine.append(";");
                    }
                    newLine.append(columns[j]);
                }
            }
            result[i] = newLine.toString().strip(); // Trim extra spaces, if any.
        }
        return result;
    }
}