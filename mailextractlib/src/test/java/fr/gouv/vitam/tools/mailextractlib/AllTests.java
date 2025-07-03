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
import java.nio.file.Files;
import java.util.*;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.MESSAGE_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;

public interface AllTests {

    static void initializeTests(String testName) throws IOException {
        File testDir=new File("target/tmpJUnit/"+testName);
        if (testDir.isDirectory())
            FileUtils.deleteDirectory(testDir);
        StoreExtractor.initDefaultExtractors(false);
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
     * Reads the content of a file into a String using the specified character set.
     *
     * @param file the file to be read
     * @param charset the character set to use for decoding the file
     * @return the file content as a String
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static String readFileToString(File file, Charset charset) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        return Files.readString(file.toPath(), charset);
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
            String[] columns = lines[i].split("\";\"");
            StringBuilder newLine = new StringBuilder();
            for (int j = 0; j < columns.length; j++) {
                if (!columnsToRemoveAsSet.contains(j)) {
                    if (newLine.length() > 0) {
                        newLine.append("\";\"");
                    }
                    newLine.append(columns[j]);
                }
            }
            result[i] = newLine.toString().strip(); // Trim extra spaces, if any.
        }
        return result;
    }

    static public void assertThatDirectoriesContainSameFilesWithExtensions(String expectedPath, String resultPath, String[] extensions) throws IOException {
        // Test data: paths of directories to compare
        File expectedDir = new File(expectedPath); // Expected directory
        File actualDir = new File(resultPath); // Produced directory

        // Initial check: both directories must exist
        assertThat(expectedDir).exists().isDirectory();
        assertThat(actualDir).exists().isDirectory();

        // Recursive verification of files in directories
        compareDirectories(expectedDir, actualDir, extensions);
    }

    static private void compareDirectories(File dir1, File dir2, String[] extensions) throws IOException {
        // List the files in each directory
        File[] dir1Files = dir1.listFiles();
        File[] dir2Files = dir2.listFiles();

        // Filter with extensions
        if (extensions != null) {
            dir1Files = Arrays.stream(dir1Files)
                    .filter(file -> file.isDirectory() || Arrays.stream(extensions)
                            .anyMatch(ext -> file.getName().toLowerCase(Locale.ROOT).endsWith("." + ext.toLowerCase(Locale.ROOT))))
                    .toArray(File[]::new);

            dir2Files = Arrays.stream(dir2Files)
                    .filter(file -> file.isDirectory() || Arrays.stream(extensions)
                            .anyMatch(ext -> file.getName().toLowerCase(Locale.ROOT).endsWith("." + ext.toLowerCase(Locale.ROOT))))
                    .toArray(File[]::new);
        }

        // Ensure both directories are not null
        assertThat(dir1Files)
                .as("Expected directory files (%s) have to be not null", dir1.getAbsolutePath())
                .isNotNull();
        assertThat(dir2Files)
                .as("Actual directory files (%s) have to be not null", dir2.getAbsolutePath())
                .isNotNull();

        // Sort files to ensure consistent order (avoiding dependence on file system order)
        Arrays.sort(dir1Files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });
        Arrays.sort(dir2Files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        // Verify both directories contain the same number of files
        assertThat(dir2Files)
                .as("Directories do not contain the same number of files: %s and %s", dir1.getAbsolutePath(), dir2.getAbsolutePath())
                .hasSameSizeAs(dir1Files);

        // Compare files one by one
        for (int i = 0; i < dir1Files.length; i++) {
            File file1 = dir1Files[i];
            File file2 = dir2Files[i];

            // Verify file names are identical
            assertThat(file2.getName())
                    .as("File names differ at position %d: %s vs %s", i, file1.getName(), file2.getName())
                    .isEqualTo(file1.getName());

            if (file1.isDirectory() && file2.isDirectory()) {
                // Recursive comparison of sub-directories
                compareDirectories(file1, file2, extensions);
            } else if (file1.isFile() && file2.isFile()) {
                // Verify file contents only if file1 has a .xml or .txt extension
                if (file1.getName().endsWith(".xml") || file1.getName().endsWith(".txt")) {
                    String content1 = FileUtils.readFileToString(file1, "UTF-8");
                    String content2 = FileUtils.readFileToString(file2, "UTF-8");
                    assertThat(content2)
                            .as("Content of .xml/.txt files differs: %s and %s", file1.getAbsolutePath(), file2.getAbsolutePath())
                            .isEqualToNormalizingNewlines(content1);
                } else {
                    byte[] content1 = Files.readAllBytes(file1.toPath());
                    byte[] content2 = Files.readAllBytes(file2.toPath());
                    assertThat(content2)
                            .as("Content of binary files differs: %s and %s", file1.getAbsolutePath(), file2.getAbsolutePath())
                            .isEqualTo(content1);
                }
            } else {
                // One is a file, the other is a directory (error)
                throw new AssertionError("Elements do not match in directories: " +
                        file1.getAbsolutePath() + " and " + file2.getAbsolutePath());
            }
        }
    }
}
