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
package fr.gouv.vitam.tools.resip;

import fr.gouv.vitam.tools.resip.utils.ResipException;
import mslinks.ShellLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The type Test utilities.
 */
public class TestUtilities {

    private static boolean isPrepared = false;
    private static boolean isWindows = false;

    private static void createSymbolicLink(String link, String target) throws ResipException {
        Path linkpath = Paths.get(link).toAbsolutePath();
        Path targetpath = Paths.get(target);
        try {
            Files.delete(linkpath);
        } catch (Exception ignored) {
        }
        try {
            Files.delete(Paths.get(linkpath.toString()+".lnk"));
        } catch (Exception ignored) {
        }
        try {
            Files.createSymbolicLink(linkpath.toAbsolutePath(), linkpath.toAbsolutePath().getParent().relativize(targetpath.toAbsolutePath()));
        } catch (Exception e) {
            if (isWindows) {
                System.err.println(
                        "Link creation is impossible, Windows shortcut creation is tried");
                ShellLink sl = new ShellLink();
                sl.setTarget(target);
                try {
                    sl.saveTo(link + ".lnk");
                } catch (IOException e1) {
                    throw new ResipException(
                            "Link and Windows shortcut [" + link + "] creation impossible\n->" + e.getMessage());
                }
            }
            else
                throw new ResipException(
                        "Link [" + link + "] creation impossible\n->" + e.getMessage());
        }
    }

    private static void createShortcutIfWindows(String link, String target)
            throws IOException, ResipException {
        if (!isWindows)
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

    /**
     * Contruct test files.
     *
     * @throws IOException    the io exception
     * @throws ResipException the resip exception
     */
    static void ContructTestFiles() throws IOException, ResipException {
        if (!isPrepared) {
            String prefix;
            isWindows=System.getProperty("os.name").toLowerCase().contains("win");


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
            System.err.println("Test files with links in ["+prefix+"] prepared");
            isPrepared = true;
        }
    }

    /**
     * Line end normalize string.
     *
     * @param text the text
     * @return the string
     */
// Utility function to get rid of line-ending differences and enbaling cross-platform compilation
    public static String LineEndNormalize(String text) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;

        char[] chars = text.toCharArray();
        for (int i = 0, n = chars.length; i < n; i++) {
            char c = chars[i];
            if (c == '"')
                inString = !inString;
            else if (c == '\\') {
                if ((inString) && (chars[i + 1] == '\\'))
                    i++;
                i++;
                continue;
            } else if (!inString && Character.isWhitespace(c) && c!='\n')
                continue;
            sb.append(c);
        }
        return sb.toString();
    }
}
