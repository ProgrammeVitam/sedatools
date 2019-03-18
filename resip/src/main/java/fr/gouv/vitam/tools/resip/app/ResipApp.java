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
package fr.gouv.vitam.tools.resip.app;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * ResipApp class for launching the command or the graphic application.
 *
 * <p>
 * Main class of Resip (Réalisation et Edition de SIP) tool for
 * launching the command or the graphic application.
 * <p>
 * It is used to generate and manipulate SEDA SIP
 * <p>
 * The operation can be logged on console and file. At the different levels you
 * can have: errors, information about global process, steps....
 * (OFF|ERROR|GLOBAL|STEP|OBJECTS_GROUP|OBJECTS|OBJECTS_WARNINGS). <br>
 * The default level of log is GLOBAL.
 *
 * <p>
 * The arguments syntax is:
 * <table summary="command options">
 * <tr>
 * <td>--help</td>
 * <td>help</td>
 * </tr>
 * <tr>
 * <td>--diskimport</td>
 * <td>import an ArchiveUnit hierarchy from a disk directories and files hierarchy, argument is root directory name</td>
 * </tr>
 * <tr>
 * <td>--exclude</td>
 * <td>exclude from the import by diskimport the files with names compliant with one of the regexp in a file, argument is file name</td>
 * </tr>
 * <tr>
 * <td>--listimport</td>
 * <td>import an ArchiveUnit hierarchy from a set of disk directories and files, the hierarchy and metadata defined in a csv file</td>
 * </tr>
 * <tr>
 * <td>--sipimport</td>
 * <td>import importe an ArchiveUnit hierarchy from a SEDA SIP, argument is SIP file name</td>
 * </tr>
 * <tr>
 * <td>--context</td>
 * <td>define the export context options and global metadatas for SIP generation(MessageIdentifier...), argument is context file name. Only used if not in graphic mode.</td>
 * </tr>
 * <tr>
 * <td>--generatesip</td>
 * <td>generate a SEDA SIP, argument is SIP file name</td>
 * </tr>
 * <tr>
 * <td>--manifest</td>
 * <td>generate a SEDA manifest, argument is manifest file name</td>
 * </tr>
 * <tr>
 * <td>--workdir</td>
 * <td>define the working directory for logs and temporary extractions</td>
 * </tr>
 * <tr>
 * <td>--xcommand</td>
 * <td>prevent graphic interface to be opened</td>
 * </tr>
 * <tr>
 * <td>--hierarchical</td>
 * <td>ArchiveUnits are generated in the hierchical model</td>
 * </tr>
 * <tr>
 * <td>--indented</td>
 * <td>the manifest is generated with indented XML</td>
 * </tr>
 * <tr>
 * <td>--verbatim</td>
 * <td>log level (OFF|ERROR|GLOBAL|STEP|OBJECTS_GROUP|OBJECTS|OBJECTS_WARNINGS)</td>
 * </tr>
 * </table>
 * <p>
 * Long options can be reduced to short ones (for example -h is equivalent to
 * --help)
 **/
public class ResipApp {

    private static Options createOptions() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "help");
        options.addOption(help);

        Option diskimport = new Option("d", "diskimport", true,
                "importe une hiérarchie d'AU depuis une hiérarchie de répertoires et fichiers avec en " +
                        "argument le répertoire racine");
        options.addOption(diskimport);

        Option exclude = new Option("e", "exclude", true, "de l'import par diskimport les fichiers" +
                " les fichiers dont le nom sont conformes aux expressions régulières contenue sur chaque ligne du fichier");
        options.addOption(exclude);

        Option listimport = new Option("l", "listimport", true,
                "importe une hiérarchie d'AU depuis un ensemble de répertoires et de fichiers dont " +
                        "la hiérarchie et les métadonnées sont décrits dans un csv");
        options.addOption(listimport);

        Option sipimport = new Option("s", "sipimport", true,
                "importe une hiérarchie d'AU depuis un SIP SEDA avec en argument le nom du fichier");
        options.addOption(sipimport);

        Option context = new Option("c", "context", true,
                "défini les informations globales utiles à la génération du SIP (MessageIdentifier...) dans le fichier indiqué");
        options.addOption(context);

        Option generatesip = new Option("g", "generatesip", true,
                "génère un paquet SEDA SIP de la structure importée avec en argument le nom du fichier à " +
                        "générer");
        options.addOption(generatesip);

        Option manifest = new Option("m", "manifest", true,
                "génère le manifest SEDA de la structure importée avec en argument le nom du fichier à " +
                        "générer");
        options.addOption(manifest);

        Option workdir = new Option("w", "workdir", true,
                "désigne le répertoire de travail pour les logs, les répertoires d'extraction temporaire");
        options.addOption(workdir);

        Option command = new Option("x", "xcommand", false, "ne lance pas l'interface " +
                "graphique");
        options.addOption(command);

        Option hierarchical = new Option("h", "hierarchical", false,
                "génère les ArchiveUnits en mode hiérarchique dans le manifest SEDA");
        options.addOption(hierarchical);

        Option indented = new Option("i", "indented", false, "génère le manifest SEDA en XML indenté");
        options.addOption(indented);

        Option verbatim = new Option("v", "verbatim", true,
                "niveau de log (OFF|ERROR|GLOBAL|STEP|OBJECTS_GROUP|OBJECTS|OBJECTS_WARNINGS)");
        options.addOption(verbatim);

        return options;
    }

    private static String[] importStringArray(String filename) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        FileInputStream fis = new FileInputStream(filename);
        JsonParser jsonParser = mapper.getFactory().createParser(fis);
        jsonParser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        return mapper.readValue(jsonParser, String[].class);
    }

    /**
     * The main method for both command and graphic version.
     *
     * @param args the arguments
     * @throws ClassNotFoundException          the class not found exception
     * @throws InstantiationException          the instantiation exception
     * @throws IllegalAccessException          the illegal access exception
     * @throws UnsupportedLookAndFeelException the unsupported look and feel
     *                                         exception
     */
    public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException, ResipException {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        String workdirString;
        int logLevel;
        CreationContext creationContext;
        ExportContext exportContext;

        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("Resip", options);
            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            formatter.printHelp("Resip", options);
            System.exit(0);
        }

        // treat all incompatible options
        List<String> inputDirList = cmd.getArgList();
        if (!inputDirList.isEmpty()) {
            System.err.print("Resip: Exécution annulée, arguments non reconnus [");
            for (String arg : inputDirList)
                System.err.print(arg + " ");
            System.err.println("]");
            System.exit(1);
        }

        if ((cmd.hasOption("sipimport") ? 1 : 0) + (cmd.hasOption("diskimport") ? 1 : 0) + (cmd.hasOption("listimport") ? 1 : 0) > 1) {
            System.err.println("Resip: Ne peux pas importer selon deux modes en même temps");
            System.exit(1);
        }

        if (cmd.hasOption("generatesip") && !cmd.hasOption("diskimport") &&
                !cmd.hasOption("listimport") && !cmd.hasOption("sipimport")) {
            System.err.println(
                    "Resip: Ne peux pas générer un SIP sans avoir importé une structure archiveUnit préalable");
            System.exit(1);
        }

        if (cmd.hasOption("generatesip") && !cmd.hasOption("xcommand")) {
            System.err.println(
                    "Resip: Ne peux pas générer en mode graphique, seulement en mode command (option --xcommand)");
            System.exit(1);
        }

        if (cmd.hasOption("context") && !cmd.hasOption("xcommand")) {
            System.err.println(
                    "Resip: Ne prend en compte un fichier de contexte qu'en mode command (option --xcommand)");
            System.exit(1);
        }

        // define workdir
        workdirString = cmd.getOptionValue("workdir");
        if (workdirString != null) {
            Path workdirPath = Paths.get(workdirString);
            if (Files.exists(workdirPath) && !Files.isDirectory(workdirPath)) {
                System.err.println("Resip: L'argument de --workdir doit être un répertoire");
                System.exit(1);
            }
            try {
                workdirString = workdirPath.toAbsolutePath().normalize().toString();
            } catch (Exception e) {
                System.err.println("Resip: L'argument de --workdir n'est pas correct\n->" + e.getMessage());
                System.exit(1);
            }
        } else {
            workdirString = new CreationContext(Prefs.getInstance().getPrefsContextNode()).getWorkDir();
        }
        try {
            Files.createDirectories(Paths.get(workdirString));
        } catch (Exception e) {
            System.err.println("Resip: La création de l'arborescence en --workdir n'est pas possible\n->" + e.getMessage());
            System.exit(1);
        }


        // define loglevel
        logLevel = -1;
        if (cmd.getOptionValue("verbatim") == null)
            logLevel = ResipLogger.GLOBAL;
        else {
            try {
                logLevel = ResipLogger.getLevel(cmd.getOptionValue("verbatim"));
            } catch (Exception e) {
                System.err.println(
                        "Resip: L'argument de niveau de log est non conforme, il doit être dans la liste " +
                                "(OFF|ERROR|GLOBAL|STEP|OBJECTS_GROUP|OBJECTS|OBJECTS_WARNINGS)\n->" + e.getMessage());
                System.exit(1);
            }
        }

        // define the convenient import context
        if (cmd.hasOption("diskimport")) {
            String[] excludePatterns = new String[0];
            if (cmd.hasOption("exclude")) {
                try {
                    excludePatterns = importStringArray(cmd.getOptionValue("exclude"));
                } catch (Exception e) {
                    System.err.println(
                            "Resip: Impossible de charger le fichier de configuration des exclusions d'import" +
                                    " [" + cmd.getOptionValue("context") + "]\n->" + e.getMessage());
                    System.exit(1);
                }
            }
            creationContext = new DiskImportContext(Arrays.asList(excludePatterns), cmd.getOptionValue("diskimport"), workdirString);
        } else if (cmd.hasOption("sipimport"))
            creationContext = new SIPImportContext(cmd.getOptionValue("sipimport"), workdirString);
        else if (cmd.hasOption("listimport"))
            creationContext = new CSVMetadataImportContext(cmd.getOptionValue("listimport"), workdirString);
        else
            creationContext = null;

        // define export context
        if (cmd.hasOption("context")) {
            try {
                exportContext = new ExportContext(cmd.getOptionValue("context"));
            } catch (Exception e) {
                System.err.println(
                        "Resip: Impossible de charger le fichier de configuration informations globales utiles à la " +
                                "génération du SIP [" + cmd.getOptionValue("context") + "]\n->" + e.getMessage());
                System.exit(1);
                return;
            }
        } else {
            try {
                exportContext = new ExportContext(Prefs.getInstance().getPrefsContextNode());
            } catch (Exception e) {
                exportContext = new ExportContext();
                exportContext.setDefaultPrefs();
            }
        }

        // define the global logger
        ResipLogger.createGlobalLogger(workdirString + File.separator + "log.txt", logLevel);
        ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL, "Début du journal au niveau=" +
                ResipLogger.getMarker(ResipLogger.getGlobalLogger().getProgressLogLevel()).getName());

        // graphic application
        if (!cmd.hasOption("xcommand")) {
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            else
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            new ResipGraphicApp(creationContext);
        } else {

            try {
                DroidIdentifier.getInstance();
                ArchiveTransfer packet = null;
                Instant start, end;

                start = Instant.now();
                SEDALibProgressLogger spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), logLevel);

                if (creationContext instanceof DiskImportContext) {
                    DiskImportContext diskImportContext = (DiskImportContext) creationContext;
                    DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(diskImportContext.getOnDiskInput(), spl);
                    for (String ip : diskImportContext.getIgnorePatternList())
                        di.addIgnorePattern(ip);
                    di.doImport();
                    packet = di.getArchiveTransfer();
                } else //noinspection ConstantConditions
                    if (creationContext instanceof SIPImportContext) {
                        SIPImportContext sipImportContext = (SIPImportContext) creationContext;
                        String target = sipImportContext.getWorkDir() + File.separator + Paths.get(sipImportContext.getOnDiskInput()).getFileName().toString() + "-tmpdir";
                        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(sipImportContext.getOnDiskInput(), target, spl);
                        si.doImport();
                        packet = si.getArchiveTransfer();
                    }

                if (((cmd.hasOption("generatesip")) || (cmd.hasOption("manifest"))) && (packet != null)) {
                    if (packet.getGlobalMetadata() == null)
                        packet.setGlobalMetadata(exportContext.getArchiveTransferGlobalMetadata());
                    if (packet.getDataObjectPackage().getManagementMetadataXmlData() == null)
                        packet.getDataObjectPackage().setManagementMetadataXmlData(exportContext.getManagementMetadataXmlData());
                    ArchiveTransferToSIPExporter se = new ArchiveTransferToSIPExporter(packet, spl);
                    if (cmd.hasOption("generatesip")) {
                        se.doExportToSEDASIP(cmd.getOptionValue("generatesip"), cmd.hasOption("hierarchical"),
                                cmd.hasOption("indented"));
                    } else if (cmd.hasOption("manifest")) {
                        se.doExportToSEDAXMLManifest(cmd.getOptionValue("manifest"), cmd.hasOption("hierarchical"),
                                cmd.hasOption("indented"));
                    }
                }

                end = Instant.now();
                ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL, " Toutes les opérations finies en =" + Duration.between(start, end).toString());
            } catch (Exception e) {
                System.err.println(
                        "Resip: Erreur fatale \n->" + e.getMessage());
                System.exit(1);
            }
        }
    }
}
