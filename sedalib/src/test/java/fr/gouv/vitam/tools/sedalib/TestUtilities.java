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
        } catch (Exception ignored) {}
        try {
            Files.delete(Paths.get(linkpath + ".lnk"));
        } catch (Exception ignored) {}
        try {
            Files.createDirectories(linkpath.getParent());
            Files.createSymbolicLink(
                linkpath.toAbsolutePath(),
                linkpath.toAbsolutePath().getParent().relativize(targetpath.toAbsolutePath())
            );
        } catch (Exception e) {
            if (isWindowsOS()) {
                System.err.println("Link creation is impossible, Windows shortcut creation is tried");
                ShellLink sl = new ShellLink();
                sl.setTarget(target);
                try {
                    sl.saveTo(link + ".lnk");
                } catch (IOException e1) {
                    throw new SEDALibException(
                        "Link and Windows shortcut [" + link + "] creation impossible\n->" + e.getMessage()
                    );
                }
            } else throw new SEDALibException("Link [" + link + "] creation impossible\n->" + e.getMessage());
        }
    }

    public static void createOrEraseAll(String dirOrFile) {
        try {
            Files.createDirectories(Paths.get(dirOrFile));
        } catch (Exception ignored) {}
        try {
            Files.delete(Paths.get(dirOrFile));
        } catch (Exception ignored) {}
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception ignored) {}
    }

    private static void createShortcutIfWindows(String link, String target) throws IOException, SEDALibException {
        if (!isWindowsOS()) createSymbolicLink(link, target);
        else {
            Path linkpath = Paths.get(link);
            try {
                Files.delete(linkpath);
            } catch (Exception ignored) {}
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
            createSymbolicLink(
                prefix + "Link SmallContract.text",
                prefix + "Node 2/Node 2.3 - Many/SmallContract.text"
            );
            createShortcutIfWindows(prefix + "Shortcut Node 2.4 - OG Link.lnk", prefix + "Node 2/Node 2.4 - OG Link");
            createShortcutIfWindows(
                prefix + "ShortCut OK-RULES-MDRULES.zip.lnk",
                prefix + "Node 2/Node 2.3 - Many/OK-RULES-MDRULES.zip"
            );
            createSymbolicLink(
                prefix + "Node 2/Node 2.4 - OG Link/Link ##Test ObjectGroup##",
                prefix + "Node 1/##Test ObjectGroup##"
            );
            createShortcutIfWindows(
                prefix + "Node 2/Node 2.5 - OG Shortcut/Shortcut ##Test ObjectGroup##.lnk",
                prefix + "Node 1/##Test ObjectGroup##"
            );
            System.err.println("Test files with links in [" + prefix + "] prepared");

            prefix = "src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2/Root/";
            // regenerate PacketSamples.SampleWithWindowsLinksAndShortcutsModelV2 links
            createSymbolicLink(prefix + "Node 1.1", prefix + "Node 1/Node 1.1");
            createSymbolicLink(prefix + "SmallContract.text", prefix + "Node 2/Node 2.3 - Many/SmallContract.text");
            createShortcutIfWindows(
                prefix + "OK-RULES-MDRULES.zip.lnk",
                prefix + "Node 2/Node 2.3 - Many/OK-RULES-MDRULES.zip"
            );
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
