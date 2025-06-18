package fr.gouv.vitam.tools.sedalib;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import mslinks.ShellLink;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtilities {

    private static final Logger log = LoggerFactory.getLogger(TestUtilities.class);
    public static boolean isPrepared = false;

    private static void createSymbolicLink(String link, String target) throws SEDALibException {
        Path linkpath = Paths.get(link);
        Path targetpath = Paths.get(target);
        try {
            Files.delete(linkpath);
        } catch (Exception ignored) {
        }
        try {
            Files.delete(Paths.get(linkpath + ".lnk"));
        } catch (Exception ignored) {
        }
        try {
            Files.createDirectories(linkpath.getParent());
            Files.createSymbolicLink(linkpath.toAbsolutePath(), linkpath.toAbsolutePath().getParent().relativize(targetpath.toAbsolutePath()));
        } catch (Exception e) {
            if (isWindowsOS()) {
                System.err.println(
                        "Link creation is impossible, Windows shortcut creation is tried");
                ShellLink sl = new ShellLink();
                sl.setTarget(target);
                try {
                    sl.saveTo(link + ".lnk");
                } catch (IOException e1) {
                    throw new SEDALibException(
                            "Link and Windows shortcut [" + link + "] creation impossible\n->" + e.getMessage());
                }
            } else
                throw new SEDALibException(
                        "Link [" + link + "] creation impossible\n->" + e.getMessage());
        }
    }

    public static void createOrEraseAll(String dirOrFile) {
        try {
            Files.createDirectories(Paths.get(dirOrFile));
        } catch (Exception ignored) {
        }
        try {
            Files.delete(Paths.get(dirOrFile));
        } catch (Exception ignored) {
        }
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception ignored) {
        }
    }

    private static void createShortcutIfWindows(String link, String target)
            throws IOException, SEDALibException {
        if (!isWindowsOS())
            createSymbolicLink(link, target);
        else {
            Path linkpath = Paths.get(link);
            try {
                Files.delete(linkpath);
            } catch (Exception ignored) {
            }
            ShellLink sl = new ShellLink();
            sl.setTarget(target);
            sl.saveTo(link);
        }
    }

    static void ContructTestFiles() throws IOException, SEDALibException {
        if (!isPrepared) {
            String prefix;

            prefix = "src/test/resources/PacketSamples/SampleWithLinksModelV2/Root/";
            // regenerate PacketSamples.SampleWithWindowsLinksAndShortcutsModelV2 links
            createSymbolicLink(prefix + "Link Node 1.2", prefix + "Node 1/Node 1.2");
            createSymbolicLink(prefix + "Link SmallContract.text",
                    prefix + "Node 2/Node 2.3 - Many/SmallContract.text");
            createShortcutIfWindows(prefix + "Shortcut Node 2.4 - OG Link.lnk", prefix + "Node 2/Node 2.4 - OG Link");
            createShortcutIfWindows(prefix + "ShortCut OK-RULES-MDRULES.zip.lnk",
                    prefix + "Node 2/Node 2.3 - Many/OK-RULES-MDRULES.zip");
            createSymbolicLink(prefix + "Node 2/Node 2.4 - OG Link/Link ##Test ObjectGroup##",
                    prefix + "Node 1/##Test ObjectGroup##");
            createShortcutIfWindows(prefix + "Node 2/Node 2.5 - OG Shortcut/Shortcut ##Test ObjectGroup##.lnk",
                    prefix + "Node 1/##Test ObjectGroup##");
            System.err.println("Test files with links in [" + prefix + "] prepared");

            prefix = "src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2/Root/";
            // regenerate PacketSamples.SampleWithWindowsLinksAndShortcutsModelV2 links
            createSymbolicLink(prefix + "Node 1.1", prefix + "Node 1/Node 1.1");
            createSymbolicLink(prefix + "SmallContract.text",
                    prefix + "Node 2/Node 2.3 - Many/SmallContract.text");
            createShortcutIfWindows(prefix + "OK-RULES-MDRULES.zip.lnk",
                    prefix + "Node 2/Node 2.3 - Many/OK-RULES-MDRULES.zip");
            System.err.println("Test files with links in [" + prefix + "] prepared");
            isPrepared = true;
        }
    }

    // Utility function to get rid of slacks in file names differences and enbaling cross-platform compilation
    public static String SlackNormalize(String csv) {
        if (csv == null) {
            return null;
        }
        // Remplace chaque '/' par un '\'
        return csv.replace('\\', '/');
    }

    public static void eraseAll(String dirOrFile) {
        try {
            Files.delete(Paths.get(dirOrFile));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static boolean isWindowsOS() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // Reads the content of a file into a String using the specified character set.
    public static String readFileToString(String filename) {
        try {
            return Files.readString(Path.of(filename), Charset.defaultCharset());
        } catch (IOException e) {
            return null;
        }
    }
}
