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
package fr.gouv.vitam.tools.testsipgenerator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.inout.SIPBuilder;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

/**
 * TestSipGeneratorApp class for launching the command.
 *
 * <p>
 * Main class of TestSipGeneratorApp tool for launching the command.
 */

public class TestSipGeneratorApp {

    final static int ZERO_CONTENT = 0;
    final static int TEXT_CONTENT = 1;
    final static int RANDOM_CONTENT = 2;

    static Options options;
    static CommandLineParser parser = new DefaultParser();
    static CommandLine cmd = null;

    static int uniqNodeID = 0;

    static int contentType = ZERO_CONTENT;
    static int depth = 1;
    static int number = 0;
    static int size = 100;
    static int bigNumber = 0;
    static int bigSize = 1024;
    static String out = "out.zip";
    static String word = "Titre";

    static Path onDiskStandardPath;
    static Path onDiskBigPath;
    static String standardFileDigest;
    static String bigFileDigest;

    /**
     * Create the Options object
     *
     * @return
     */
    static Options createOptions() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "help");
        options.addOption(help);

        Option depth = new Option("d", "depth", true,
                "profondeur de l'arbre des ArchiveUnits (1 par défaut, min 1, max 128)");
        depth.setArgName("num");
        options.addOption(depth);

        Option number = new Option("n", "number", true,
                "nombre d'objets de taille standard (0 par défaut, min 0, max 1000000)");
        number.setArgName("num");
        options.addOption(number);

        Option size = new Option("s", "size", true,
                "taille des objets standards en ko (100ko par défaut, min 1ko, max 1Go)");
        size.setArgName("num");
        options.addOption(size);

        Option bigNumber = new Option("N", "Number", true,
                "nombre d'objets de grande taille (0 par défaut, min 0, max 100)");
        bigNumber.setArgName("num");
        options.addOption(bigNumber);

        Option bigSize = new Option("S", "Size", true,
                "taille des gros objets en Mo (1Go par défaut, min 1Mo, max 1TO)");
        bigSize.setArgName("num");
        options.addOption(bigSize);

        Option random = new Option("r", "random", false,
                "génère des contenus aléatoires (donc peu compressibles) dans les fichiers d'objets");
        options.addOption(random);

        Option text = new Option("t", "text", false,
                "génère des contenus textuels (donc compressibles) dans les fichiers d'objets");
        options.addOption(text);

        Option zero = new Option("z", "zero", false,
                "met uniquement des zéros dans les fichiers d'objets (contenu par défaut)");
        options.addOption(zero);

        Option out = new Option("o", "out", true,
                "nom du fichier de sortie (out.sip par défaut)");
        out.setArgName("FILE");
        options.addOption(out);

        Option word = new Option("w", "word", true,
                "mot utilisé dans le titre des ArchiveUnits");
        word.setArgName("WORD");
        options.addOption(word);

        return options;
    }

    /**
     * Exit with a status code after printing the help
     *
     * @param exitCode the process exit code
     */
    static void exitHelp(int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("TestSipGenerator", "Generate populated test SIP", options, "", true);
        System.exit(exitCode);
    }

    /**
     * Get the int value for option or exit with error status
     *
     * @param option the option string
     * @return
     */
    static int getIntOrExit(String option) {
        int result = 0;
        try {
            result = Integer.parseInt(cmd.getOptionValue(option));
        } catch (NumberFormatException e) {
            System.out.println("Argument entier attendu pour l'option " + option);
            exitHelp(1);
        }
        return result;
    }

    /**
     * Extract all options values from args
     */
    private static void extractOptionsOrExit(String[] args) {
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            exitHelp(1);
        }
        if (cmd.hasOption("help"))
            exitHelp(0);
        if (cmd.hasOption("depth")) {
            depth = getIntOrExit("depth");
            if ((depth < 1) || (depth > 128)) {
                System.out.println("Argument hors des limites pour l'option depth");
                exitHelp(1);
            }
        }
        if (cmd.hasOption("number")) {
            number = getIntOrExit("number");
            if ((number < 0) || (number > 1000000)) {
                System.out.println("Argument hors des limites pour l'option number");
                exitHelp(1);
            }
        }
        if (cmd.hasOption("size")) {
            size = getIntOrExit("size");
            if ((size < 1) || (size > 1024 * 1024 * 1024)) {
                System.out.println("Argument hors des limites pour l'option size");
                exitHelp(1);
            }
        }
        if (cmd.hasOption("Number")) {
            bigNumber = getIntOrExit("Number");
            if ((bigNumber < 0) || (bigNumber > 100)) {
                System.out.println("Argument hors des limites pour l'option Number");
                exitHelp(1);
            }
        }
        if (cmd.hasOption("Size")) {
            bigSize = getIntOrExit("Size");
            if ((bigSize < 1) || (bigSize > (long) 1024 * 1024 * 1024)) {
                System.out.println("Argument hors des limites pour l'option Size");
                exitHelp(1);
            }
        }
        if (cmd.hasOption("out")) {
            out = cmd.getOptionValue("out");
            if (out == null)
                exitHelp(1);
        }
        if (cmd.hasOption("word")) {
            word = cmd.getOptionValue("word");
            if (word == null)
                exitHelp(1);
        }
        if (cmd.hasOption("text"))
            contentType = TEXT_CONTENT;
        if (cmd.hasOption("random"))
            contentType = RANDOM_CONTENT;
    }

    /**
     * Generate a test file with chosen size and content type
     * @param onDiskPath        the generated file path
     * @param blockNumber       the number of blocks
     * @param blockSize         the block size
     * @param contentType       the content type (zero, text or random)
     * @return the file SHA-512 digest
     */
    static String generateFile(Path onDiskPath, int blockNumber, int blockSize, int contentType) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException var33) {
            System.out.println("Impossible de mobiliser l'algorithme de hashage SHA-512");
            System.exit(1);
        }
        try (FileOutputStream fos = new FileOutputStream(onDiskPath.toFile())) {
            byte[] content = new byte[blockSize];
            for (int i = 0; i < blockSize; i++) {
                switch (contentType) {
                    case ZERO_CONTENT:
                        content[i] = 0;
                        break;
                    case TEXT_CONTENT:
                        int letter = (int) (Math.random() * 31.0);
                        if (letter > 25) letter = ' ';
                        else letter = 'a' + letter;
                        content[i] = (byte) (letter);
                        break;
                    case RANDOM_CONTENT:
                        content[i] = (byte) (Math.random() * 256.0);
                        break;
                }
            }
            for (int i = 0; i < blockNumber; i++) {
                fos.write(content);
                messageDigest.update(content, 0, blockSize);
            }
        } catch (IOException e) {
            System.out.println("Impossible de créer le fichier temporaire " + onDiskPath.toString());
        }
        // Convert the byte to hex format
        try (Formatter formatter = new Formatter()) {
            for (final byte b : messageDigest.digest()) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } catch (Exception e) {
            System.out.println("Impossible d'encoder le hash du fichier [" + onDiskPath.toString() + "]->" + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    /**
     * Gets a uniq ID for tree nodes
     * @return uniq ID
     */
    static int getUniqNodeID() {
        return ++uniqNodeID;
    }

    /**
     * Generate an ArchiveUnit binary tree distributing standard and big objects
     *
     * @param sb        the SIPBuilder
     * @param auName    the root ArchiveUnit under which two nodes are created
     * @param number    the number of standard objects to distribute
     * @param bigNumber the number of big objects to distribute
     * @param depth     the tree depth to generate
     * @throws SEDALibException
     */
    static void generateTree(SIPBuilder sb, String auName, int number, int bigNumber, int depth) throws SEDALibException {
        String childAuName;
        ArchiveUnit au;

        if ((number == 0) && (bigNumber == 0))
            return;

        if (depth == 0) {
            for (int i = 0; i < number; i++) {
                childAuName = "Leaf" + Integer.toString(getUniqNodeID());
                au = sb.addNewSubArchiveUnit(auName, childAuName, "Item", word + " " + childAuName,
                        "Description " + childAuName);
                addKnownFileToArchiveUnit(au, onDiskStandardPath, standardFileDigest);
            }
            for (int i = 0; i < bigNumber; i++) {
                childAuName = "BigLeaf" + Integer.toString(getUniqNodeID());
                au = sb.addNewSubArchiveUnit(auName, childAuName, "Item", word + " " + childAuName,
                        "Description " + childAuName);
                addKnownFileToArchiveUnit(au, onDiskBigPath, bigFileDigest);
            }
            return;
        }

        if ((Math.floor(number / 2) > 0) || (Math.floor(bigNumber / 2) > 0)) {
            childAuName = "Node" + Integer.toString(getUniqNodeID());
            sb.addNewSubArchiveUnit(auName, childAuName, "RecordGrp", word + " " + childAuName,
                    "Description " + childAuName);
            generateTree(sb, childAuName, (int) Math.floor(number / 2), (int) Math.floor(bigNumber / 2), depth - 1);
        }
        if ((number - Math.floor(number / 2) > 0) || (bigNumber - Math.floor(bigNumber / 2) > 0)) {
            childAuName = "Node" + Integer.toString(getUniqNodeID());
            sb.addNewSubArchiveUnit(auName, childAuName, "RecordGrp", word + " " + childAuName,
                    "Description " + childAuName);
            generateTree(sb, childAuName, (int) (number - Math.floor(number / 2)), (int) (bigNumber - Math.floor(bigNumber / 2)), depth - 1);
        }
    }

    /**
     * Adds a file, with known digest, to an archive unit as BinaryMaster_1.
     *
     * @param au         the archive unit
     * @param onDiskPath the on disk path
     * @param digest     the file digest
     * @throws SEDALibException if no identified ArchiveUnit, file access problem , or when there is already an
     *                          ArchiveUnit with the same UniqID
     */
    static void addKnownFileToArchiveUnit(ArchiveUnit au, Path onDiskPath, String digest) throws SEDALibException {
        BinaryDataObject bdo = new BinaryDataObject(au.getDataObjectPackage(), onDiskPath, onDiskPath.getFileName().toString(),
                "BinaryMaster_1");
        putKnownTechnicalElements(bdo, digest);
        au.addDataObjectById(bdo.getInDataObjectPackageId());
    }

    /**
     * Put technical metadata in an object, with known digest
     *
     * @param bdo    the binary data object
     * @param digest the known digest
     * @throws SEDALibException
     */
    static void putKnownTechnicalElements(BinaryDataObject bdo, String digest) throws SEDALibException {
        IdentificationResult ir = null;
        String lfilename = null;

        long lsize;
        FileTime llastModified;
        try {
            lsize = Files.size(bdo.getOnDiskPath());
            if (lfilename == null) {
                lfilename = bdo.getOnDiskPath().getFileName().toString();
            }
            llastModified = Files.getLastModifiedTime(bdo.getOnDiskPath());
        } catch (IOException e) {
            throw new SEDALibException("Impossible de générer les infos techniques pour le fichier [" + bdo.getOnDiskPath().toString() + "]\n->" + e.getMessage());
        }

        bdo.messageDigestAlgorithm = "SHA-512";
        bdo.messageDigest = digest;
        bdo.size = lsize;
        if (contentType == TEXT_CONTENT)
            bdo.formatIdentification = new FormatIdentification("Plain Text File", "text/plain", "x-fmt/111", null);
        else
            bdo.formatIdentification = new FormatIdentification("Unknown", (String) null, (String) null, (String) null);

        bdo.fileInfo = new FileInfo();
        bdo.fileInfo.filename = lfilename;
        bdo.fileInfo.lastModified = llastModified;
    }

    /**
     * The main method for command execution.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        options = createOptions();

        extractOptionsOrExit(args);

        Path outPath = Paths.get(out).normalize().toAbsolutePath();
        Path dirPath = outPath.getParent();
        if (dirPath == null)
            dirPath = Paths.get("/");
        onDiskStandardPath = dirPath.resolve("standardfile.tmp");
        onDiskBigPath = dirPath.resolve("bigfile.tmp");

        standardFileDigest = generateFile(onDiskStandardPath, size, 1024, contentType);
        bigFileDigest = generateFile(onDiskBigPath, bigSize, 1024 * 1024, contentType);

        SEDALibProgressLogger spl = new SEDALibProgressLogger(LoggerFactory.getLogger("fr/gouv/vitam/tools/testsipgenerator"), SEDALibProgressLogger.OBJECTS_GROUP, null, 100);
        try (SIPBuilder sb = new SIPBuilder(outPath.toString(), spl)) {
            sb.setAgencies("FRAN_NP_000001", "FRAN_NP_000010", "FRAN_NP_000015", "FRAN_NP_000019");
            sb.setArchivalAgreement("Accepte_les_objets_non_identifies");
            sb.createRootArchiveUnit("Root", "Subseries", "TestSIPRoot-" + word,
                    "Racine du SIP de test généré avec les arguments [" + String.join(" ", args) + "]");
            generateTree(sb, "Root", number, bigNumber, depth - 1);
            sb.generateSIP();
        } catch (SEDALibException e) {
            System.out.println("Erreur de traitement du SIP");
            e.printStackTrace();
            System.exit(1);
        }
        finally {
            try {
                Files.delete(onDiskStandardPath);
                Files.delete(onDiskBigPath);
            } catch (IOException e) {
                System.out.println("Impossible d'effacer les fichiers intermédiaires");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
