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

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.apache.commons.cli.*;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.file.Paths;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLogWithoutInterruption;

/**
 * The MailExtractApp class launches the command-line or graphical interface for extracting
 * and processing email messages from multiple sources.
 *
 * <p>
 * It supports the extraction of email data from formats and protocols such as:
 * <ul>
 * <li>IMAP or IMAPS servers with user credentials</li>
 * <li>Thunderbird directories containing MBOX files and .sbd hierarchies</li>
 * <li>MBOX or EML files</li>
 * <li>Outlook PST or MSG files</li>
 * </ul>
 *
 * <p>
 * The application generates an organized directory/file structure, suitable
 * for SEDA archival (NF Z44-022 standard). See {@link StoreExtractor} for details.
 * Logging is supported at various levels, including errors, warnings, and progress.
 * By default:
 * <ul>
 * <li>INFO level is used for extractions</li>
 * <li>OFF level is used for listings</li>
 * </ul>
 *
 * <p>
 * Available command-line options:
 * </p>
 * <table>
 * <caption>Command options</caption>
 * <thead>
 * <tr><th>Option</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>--help</td><td>Shows help information</td></tr>
 * <tr><td>--type x</td><td>Extraction type (e.g., "thunderbird" or "imap")</td></tr>
 * <tr><td>--user x</td><td>Username for authentication</td></tr>
 * <tr><td>--password x</td><td>Password for authentication</td></tr>
 * <tr><td>--server HostName:port</td><td>Mail server to connect</td></tr>
 * <!-- Add other rows similarly -->
 * </tbody>
 * </table>
 *
 * <b>Warning:</b> Detailed listings from remote servers can be resource-intensive, as all messages
 * will be fully accessed (and downloaded if remote).
 ** <p>
 * See also: {@link fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor}
 **/
public class MailExtractApp {

    private static String noNullString(String string) {
        if (string == null) return "";
        return string;
    }

    private static Options createOptions() {
        Options options = new Options();

        Option help = new Option("h", "help", false, "help");
        options.addOption(help);

        Option type = new Option("t", "type", true,
                "type of local container to extract (thunderbird|pst|eml|mbox|msg) or protocol for server access (imap|imaps|pop3...) [default pst]");
        options.addOption(type);

        Option user = new Option("u", "username", true,
                "user account name (also used for destination extraction naming)");
        options.addOption(user);

        Option password = new Option("p", "password", true,
                "password");
        options.addOption(password);

        Option server = new Option("s", "server", true,
                "mail server [HostName|IP](:port)");
        options.addOption(server);

        Option container = new Option("c", "container", true,
                "local container directory or file to extract");
        options.addOption(container);

        Option folder = new Option("f", "folder", true, "specific mail folder to extract [default root]");
        options.addOption(folder);

        Option rootpath = new Option("r", "rootpath", true,
                "path for output directory [default current directory]");
        options.addOption(rootpath);

        Option outputname = new Option("o", "outputname", true,
                "name for output [default no name, extraction in rootpath directly, and log file is .log]");
        options.addOption(outputname);

        Option dropemptyfolders = new Option("d", "dropemptyfolders [default false]", false,
                "drop empty folders");
        options.addOption(dropemptyfolders);

        Option keeponlydeep = new Option("k", "keeponlydeep [default false]", false,
                "keep only empty folders not at root level");
        options.addOption(keeponlydeep);

        Option nameslength = new Option("n", "nameslength", true,
                "length limit for directories and files generated names [default 12]");
        options.addOption(nameslength);

        Option setchar = new Option("cs", "charset", true,
                "used charset in extraction [default UTF-8]");
        options.addOption(setchar);

        Option extractchoices = new Option("e", "extractchoices", true,
                "which elements are extracted with m for mails, c for contacts and a for appointment [default mca]");
        options.addOption(extractchoices);

        Option extractmode = new Option("ml", "extractmode", true,
                "which form of extraction with c for content and l for lists [default cl]");
        options.addOption(extractmode);

        Option extracttextmetadata = new Option("tm", "extracttextmetadata", true,
                "extract text and add to metadata with m for message and a for attachments [default nothing]");
        options.addOption(extracttextmetadata);

        Option extracttextfile = new Option("tf", "extracttextfile", true,
                "extract text and add a text file with m for message and a for attachments [default nothing]");
        options.addOption(extracttextfile);

        Option model = new Option("model", "model", true,
                "model of metadata extraction on disk 1 or 2 (default 2)");
        options.addOption(model);

        Option warning = new Option("w", "warning", false,
                "generate warning when there's a problem on a message whatever log level except OFF (otherwise log only at MESSAGE_DETAILS level) [default false]");
        options.addOption(warning);

        Option debug = new Option("b", "debug", false, "activate debug log [default false]");
        options.addOption(debug);

        Option verbatim = new Option("v", "verbatim", true,
                "event level to log (OFF|GLOBAL|WARNING|FOLDER|MESSAGE_GROUP|MESSAGE|MESSAGE_DETAILS) [default OFF]");
        options.addOption(verbatim);

        Option english = new Option("u", "english", true,
                "english version GUI");
        options.addOption(english);

        Option extract = new Option("x", "doextract", false, "extract all informations action");
        options.addOption(extract);

        Option list = new Option("l", "dolist", false,
                "process all informations and list folders action (no drop options)");
        options.addOption(list);

        Option stats = new Option("z", "dostats", false,
                "process all informations and list folders and there statistics action  (no drop options)");
        options.addOption(stats);

        return options;
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
            IllegalAccessException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        // params
        String type;
        String user, password, hostname;
        int port;
        String container, folder;
        String rootPath, outputName;
        boolean dropEmptyFolders, keepFirstLevelEmptyFolders;
        int namesLength;
        String charset;
        String extractChoices, extractMode, extractTextMetadata, extractTextFile;
        int model;
        boolean warning , debug ;
        String verbatim;
        boolean english;

        // prepare parsing
        Options options = createOptions();
        CommandLine cmd = null;
        CommandLineParser parser = new DefaultParser();
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();

                // Texte personnalisé pour l'introduction
                String header = "Mail extraction application (pst|thundebird|mbox|eml|msg files and distant mail servers)\n\nOptions :";

                // Texte personnalisé pour la conclusion
                String footer = "\nwhen no x, l or z action is given the GUI is launched";

                // Affichage de l'aide avec le header et footer
                formatter.printHelp("MailExtract", header, options, footer);
                System.exit(0);
            }

        } catch (ParseException e) {
            System.out.println("Can't analyze options : " + e.getMessage());
            System.exit(1);
        }

        // get all options and arguments verifying them when possible
        if (cmd.hasOption("type"))
            type = noNullString(cmd.getOptionValue("type"));
        else
            type = "pst";

        user = noNullString(cmd.getOptionValue("user"));

        password = noNullString(cmd.getOptionValue("password"));

        String server = cmd.getOptionValue("server");
        hostname = "";
        port = -1;
        if (server != null)
            try {
                if (server.indexOf(':') >= 0) {
                    hostname = server.substring(0, server.indexOf(':'));
                    port = Integer.parseInt(server.substring(server.indexOf(':') + 1));
                } else
                    hostname = server;
            } catch (NumberFormatException e) {
                System.out.println("Wrong server format must be <hostname[:port]>");
                System.exit(1);
            }

        container = noNullString(cmd.getOptionValue("container"));

        folder = noNullString(cmd.getOptionValue("folder"));

        if (cmd.hasOption("rootpath"))
            rootPath = noNullString(cmd.getOptionValue("rootpath"));
        else
            rootPath = System.getProperty("user.dir");

        outputName = noNullString(cmd.getOptionValue("outputname"));

        dropEmptyFolders = cmd.hasOption("dropemptyfolders");

        keepFirstLevelEmptyFolders = cmd.hasOption("keepfirstLlevelemptyfolders");

        namesLength = 12;
        if (cmd.hasOption("nameslength")) {
            try {
                namesLength = Integer.parseInt((String) cmd.getOptionValue("nameslength"));
            } catch (NumberFormatException e) {
                System.err.println("The names length argument must be numeric");
                System.exit(1);
            }
        }

        if (cmd.hasOption("charset")) {
            charset = noNullString(cmd.getOptionValue("charset"));
            try {
                if (!Charset.isSupported(charset))
                    throw new IllegalCharsetNameException(charset);
            } catch (IllegalCharsetNameException e) {
                System.err.println("The specified charset '" + charset + "' is not supported.");
                System.exit(1);
            }
        } else
            charset = "UTF-8";

        if (cmd.hasOption("extractchoices")) {
            extractChoices = noNullString(cmd.getOptionValue("extractchoices"));
            if (!extractChoices.matches("^[mca]{0,3}$")) {
                System.err.println("The 'extractchoices' argument must only contain the letters 'm', 'c', and 'a', each at most once and in any order.");
                System.exit(1);
            }
        } else
            extractChoices = "mca";

        if (cmd.hasOption("extractmode")) {
            extractMode = noNullString(cmd.getOptionValue("extractmode"));
            if (!extractMode.matches("^[cl]{0,2}$")) {
                System.err.println("The 'extractmode' argument must only contain the letters 'c' and 'l', each at most once and in any order.");
                System.exit(1);
            }
        } else
            extractMode = "cl";

        if (cmd.hasOption("extracttextmetadata")) {
            extractTextMetadata = noNullString(cmd.getOptionValue("extracttextmetadata"));
            if (!extractTextMetadata.matches("^[ma]{0,2}$")) {
                System.err.println("The 'extracttextmetadata' argument must only contain the letters 'm' and 'a', each at most once and in any order.");
                System.exit(1);
            }
        } else
            extractTextMetadata = "";

        if (cmd.hasOption("extracttextfile")) {
            extractTextFile = noNullString(cmd.getOptionValue("extracttextfile"));
            if (!extractTextFile.matches("^[ma]{0,2}$")) {
                System.err.println("The 'extracttextfile' argument must only contain the letters 'm' and 'a', each at most once and in any order.");
                System.exit(1);
            }
        } else
            extractTextFile = "";

        model = 2;
        if (cmd.hasOption("model")) {
            try {
                model = Integer.parseInt((String) cmd.getOptionValue("model"));

            } catch (NumberFormatException e) {
                System.err.println("The model argument must be numeric");
                System.exit(1);
            }
            if ((model != 1) && (model != 2)) {
                System.err.println("The model argument must 1 or 2");
                System.exit(1);
            }
        }

        warning = cmd.hasOption("warning");

        debug = cmd.hasOption("debug");

        verbatim = "OFF";
        if (cmd.hasOption("verbatim")) {
            verbatim = cmd.getOptionValue("verbatim");
            try {
                MailExtractLogger.getLevel(verbatim);
            } catch (MailExtractException iae) {
                System.err.println("Unknown log level must be in the list (OFF|GLOBAL|WARNING|FOLDER|MESSAGE_GROUP|MESSAGE|MESSAGE_DETAILS)");
                System.exit(1);
            }
        }

        english = cmd.hasOption("english");

        // get store extractor options
        StoreExtractorOptions storeExtractorOptions = new StoreExtractorOptions(
                keepFirstLevelEmptyFolders,
                dropEmptyFolders,
                warning,
                namesLength,
                charset,
                extractChoices.contains("m"), // extract messages
                extractChoices.contains("c"), // extract contacts
                extractChoices.contains("a"), // extract appointments
                extractMode.contains("c"),   // extract elements content
                extractMode.contains("l"),   // extract elements list
                extractTextFile.contains("m"), // extract message text file
                extractTextMetadata.contains("m"), // extract message text metadata
                extractTextFile.contains("a"), // extract file text file
                extractTextMetadata.contains("a"), // extract file text metadata
                model);


        // init default store extractors
        // Prevent running external tools during apache-tika text extraction (tesseract, ffmpeg...).
        StoreExtractor.initDefaultExtractors(false);

        // if no do option graphic version
        if (!cmd.hasOption("l") && !cmd.hasOption("z") && !cmd.hasOption("x")) {
            new MailExtractGraphicApp(type, user, password, hostname, port, container, folder,
                    rootPath, outputName,
                    storeExtractorOptions, debug, verbatim, english);
        } else {
            // specific option parsing for local type extraction
            switch (type) {
                case "thunderbird":
                case "pst":
                case "eml":
                case "mbox":
                case "msg":
                    if (!cmd.hasOption("container")) {
                        System.out.println("local " + type + " extraction need a container path");
                        System.exit(1);
                    }
                    if (cmd.hasOption("server")) {
                        System.err.println("no need a server for local " + type + " extraction");
                        System.exit(1);
                    }
                    break;
                default:
                    if (!cmd.hasOption("user")) {
                        System.err.println("need a username for distant access protocol");
                        System.exit(1);
                    }
                    if (!cmd.hasOption("server")) {
                        System.err.println("need a server (hostname or ip) for " + type + " access protocol");
                        System.exit(1);
                    }
                    if (cmd.hasOption("container")) {
                        System.err.println("no container for " + type + " access protocol");
                        System.exit(1);
                    }
                    break;
            }

            StoreExtractor storeExtractor = null;

            // do the job, creating a store extractor and running the extraction
            MailExtractLogger mel = null;
            MailExtractProgressLogger mepl = null;
            try {
                mel = new MailExtractLogger(rootPath + File.separator + outputName + ".log", MailExtractLogger.getLevel(verbatim));
                mepl = new MailExtractProgressLogger(mel.getProgressLogger(), MailExtractLogger.getLevel(verbatim));
                mepl.setDebugFlag(debug);

                String urlString = StoreExtractor.composeStoreURL(type, server, user, password, container);
                storeExtractor = StoreExtractor.createStoreExtractor(urlString, folder,
                        Paths.get(rootPath, outputName).toString(), storeExtractorOptions, mepl);
                if (cmd.hasOption("l") || cmd.hasOption("z")) {
                    storeExtractor.listAllFolders(cmd.hasOption("z"));
                } else {
                    storeExtractor.extractAllFolders();
                }
                storeExtractor.endStoreExtractor();
            } catch (MailExtractLibException ee) {
                doProgressLogWithoutInterruption(mepl, GLOBAL, "mailextract: extraction error", ee);
                System.exit(1);
            } catch (Exception e) {
                logFatalError(e, storeExtractor, mel);
                System.exit(1);
            }
        }
    }

    // try if possible to log in the store extractor logger all the information
    // about the fatal error
    private final static void logFatalError(Exception e, StoreExtractor storeExtractor, MailExtractLogger logger) {
        if (logger == null)
            logger = MailExtractLogger.getGlobalLogger();
        logger.log(GLOBAL, "Terminated with unrecoverable error");
        if (!e.getMessage().isEmpty())
            logger.log(GLOBAL, e.getMessage());
        logger.log(GLOBAL, getPrintStackTrace(e));
        if (storeExtractor == null
                || storeExtractor.getElementCounter(StoreFolder.class, false) + storeExtractor.getElementCounter(StoreMessage.class, false) == 0)
            logger.log(GLOBAL, "No writing done");
        else
            logger.log(GLOBAL, "Partial extraction done in " + storeExtractor.getSummary());

    }

    // make a String from the stack trace
    private final static String getPrintStackTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter p = new PrintWriter(baos);

        e.printStackTrace(p);
        p.close();
        return baos.toString();
    }

}
