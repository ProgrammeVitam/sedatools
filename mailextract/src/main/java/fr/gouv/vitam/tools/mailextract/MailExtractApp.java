/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.mailextract;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.utils.ExtractionException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.GLOBAL;

/**
 * MailExtractApp class for launching the command or the graphic application.
 *
 * <p>
 * Main class of mailextract toolfor launching the command or the graphic
 * application.
 * <p>
 * It performs extraction and structure listing of mail boxes from different
 * sources:
 * <ul>
 * <li>IMAP or IMAPS server with user/password login</li>
 * <li>Thunderbird directory containing mbox files and .sbd directory
 * hierarchy</li>
 * <li>Mbox or Eml file</li>
 * <li>Outlook pst or Msg file</li>
 * </ul>
 *
 * <p>
 * The extraction generate on disk a directories/files structure convenient for
 * SEDA archive packet (NF Z44-022). For detailed information see class
 * {@link StoreExtractor}.
 *
 * <p>
 * The operation, extraction or listing, can be logged on console and file
 * (root/username[-timestamp].log - cf args). At the different levels you can
 * have: extraction errors (SEVERE), warning about extraction problems and items
 * dropped (WARNING), information about global process (INFO), list of treated
 * folders (FINE), list of treated messages (FINER), problems with some expected
 * metadata (FINEST). <br>
 * The default level of log is INFO for extracting and OFF for listing.
 *
 * <p>
 * The arguments syntax is:
 * <table summary="command options">
 * <tr>
 * <td>--help</td>
 * <td>help</td>
 * </tr>
 * <tr>
 * <td>--type x</td>
 * <td>type of local container to extract (thunderbird|outlook|eml|mbox) or
 * protocol for server access (imap|imaps|pop3...)</td>
 * </tr>
 * <tr>
 * <td>--user x</td>
 * <td>user account name(also used for destination extraction naming)</td>
 * </tr>
 * <tr>
 * <td>--password x</td>
 * <td>password</td>
 * </tr>
 * <tr>
 * <td>--server [HostName|IP](:port)</td>
 * <td>mail server</td>
 * </tr>
 * <tr>
 * <td>--container x</td>
 * <td>mail container directory for mbox or file for pst</td>
 * </tr>
 * <tr>
 * <td>--folder x</td>
 * <td>specific mail folder used as root for extraction or listing</td>
 * </tr>
 * <tr>
 * <td>--rootdir x</td>
 * <td>root (default current directory) for output to root/username
 * directory</td>
 * </tr>
 * <tr>
 * <td>--addtimestamp</td>
 * <td>add a timestamp to output directory (root/username-timestamp)</td>
 * </tr>
 * <tr>
 * <td>--dropemptyfolders</td>
 * <td>drop empty folders</td>
 * </tr>
 * <tr>
 * <td>--keeponlydeep</td>
 * <td>keep only empty folders not at root level</td>
 * </tr>
 * <tr>
 * <td>--nameslength x</td>
 * <td>generate directories and files names with x characters max</td>
 * </tr>
 * <tr>
 * <td>--setchar</td>
 * <td>default charset</td>
 * </tr>
 * <tr>
 * <td>--extractlists</td>
 * <td>generate csv list of objects if any (mails, contacts...)</td>
 * </tr>
 * <tr>
 * <td>--extractmessagetextfile</td>
 * <td>extract a file with text version of messages</td>
 * </tr>
 * <tr>
 * <td>--extractmessagetextmetadata</td>
 * <td>put message text in metadata</td>
 * </tr>
 * <tr>
 * <td>--extractfiletextfile</td>
 * <td>extract a file with text version of attachment files</td>
 * </tr>
 * <tr>
 * <td>--extractfiletextmetadata</td>
 * <td>put attachment file text in metadata</td>
 * </tr>
 * <tr>
 * <td>--model x</td>
 * <td>model of extraction on disk 1 or 2 (default 2)</td>
 * </tr>
 * <tr>
 * <td>--verbatim x</td>
 * <td>event level to log</td>
 * </tr>
 * <tr>
 * <td>--warning</td>
 * <td>generate warning when there's a problem on a message (otherwise log at
 * FINEST level)</td>
 * </tr>
 * <tr>
 * <td>-x</td>
 * <td>generate extraction logs</td>
 * </tr>
 * <tr>
 * <td>-l</td>
 * <td>access account and list folders (no drop options)</td>
 * </tr>
 * <tr>
 * <td>-z</td>
 * <td>access account and list folders and there statistics (no drop
 * options)</td>
 * </tr>
 * </table>
 * Long options can be reduced to short ones (for example -h is equivalent to
 * --help)
 *
 * <p>
 * <b>Warning:</b> Listing with detailed information is a potentially expensive
 * operation, especially when accessing distant account, as all messages are
 * inspected (in the case of a distant account that mean also downloaded...).
 *
 * <p>
 * Note: For now it can't extract S/MIME (ciphered and/or signed) messages.
 * <p>
 * It implements the operating class {@link StoreExtractor}
 *
 * @author JSL
 **/
public class MailExtractApp {

    // define the jopt option parser
    private final static OptionParser createOptionParser() {
        OptionParser parser;

        parser = new OptionParser();
        parser.accepts("help").forHelp();
        parser.accepts("type",
                "type of local container to extract (thunderbird|pst|eml|mbox) or protocol for server access (imap|imaps|pop3...)")
                .withRequiredArg();
        parser.accepts("user", "user account name (also used for destination extraction naming)").withRequiredArg();
        parser.accepts("password", "password").withRequiredArg();
        parser.accepts("server", "mail server [HostName|IP](:port)").withRequiredArg();
        parser.accepts("container", "local container directory or file to extract").withRequiredArg();
        parser.accepts("folder", "specific mail folder").withRequiredArg();
        parser.accepts("rootdir", "root (default current directory) for output to root/username directory")
                .withRequiredArg();
        parser.accepts("dropemptyfolders", "drop empty folders");
        parser.accepts("keeponlydeep", "keep only empty folders not at root level");
        parser.accepts("verbatim", "event level to log (OFF|SEVERE|WARNING|INFO|FINE|FINER|FINEST)").withRequiredArg();
        parser.accepts("nameslength", "length limit for directories and files generated names").withRequiredArg();
        parser.accepts("setchar", "default charset").withRequiredArg();
        parser.accepts("extractlists",
                "generate csv list of objects if any (mails, contacts...)");
        parser.accepts("extractmessagetextfile", "extract a text file version of messages");
        parser.accepts("extractmessagetextmetadata", "put message text in metadata");
        parser.accepts("extractfiletextfile", "extract a text file version of attachment files");
        parser.accepts("extractfiletextmetadata", "put file text in metadata");
        parser.accepts("model", "model of extraction on disk 1 or 2 (default 2)").withRequiredArg();
        ;
        parser.accepts("warning",
                "generate warning when there's a problem on a message (otherwise log at FINEST level)");
        parser.accepts("x", "extract account");
        parser.accepts("l", "access account and list folders (no drop options)");
        parser.accepts("z", "access account and list folders and there statistics (no drop options)");

        return parser;
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
        String destRootPath = "", destName = "";
        String protocol = "", host = "localhost", user = "", password = "", container = "", folder = "";
        int port = -1;
        int namesLength = 12;
        int model = 2;
        StoreExtractorOptions storeExtractorOptions;
        boolean local = false;
        String logLevel, defaultCharset;

        // outputs
        MailExtractProgressLogger logger = null;

        // prepare parsing with jopt
        OptionParser parser = createOptionParser();
        OptionSet options = null;

        try {
            options = parser.parse(args);
        } catch (Exception e) {
            System.err.println("wrong arguments to know syntax use -h or --help option");
            System.exit(1);
        }

        // help
        try {
            if (options.has("help")) {
                parser.printHelpOn(System.out);
                System.exit(0);
            }
        } catch (Exception e) {
            System.exit(0);

        }

        // non protocol specific option parsing
        if (options.has("verbatim"))
            logLevel = (String) options.valueOf("verbatim");
        else if (options.has("l") || options.has("z") || (!options.has("l") && !options.has("z") && !options.has("x")))
            logLevel = "OFF";
        else
            logLevel = "GLOBAL";
        try {
            MailExtractLogger.getLevel(logLevel);
        } catch (MailExtractException iae) {
            System.err.println("Unknown log level");
            System.exit(1);
        }
        if (options.has("nameslength")) {
            try {
                namesLength = Integer.parseInt((String) options.valueOf("nameslength"));

            } catch (NumberFormatException e) {
                System.err.println("the names length argument must be numeric");
                System.exit(1);
            }
        }

        if (options.has("model")) {
            try {
                model = Integer.parseInt((String) options.valueOf("model"));

            } catch (NumberFormatException e) {
                System.err.println("the model argument must be numeric");
                System.exit(1);
            }
            if ((model != 1) && (model != 2))
                System.err.println("the model argument must 1 or 2");
            System.exit(1);
        }

        // identify protocol option
        if (options.has("type"))
            protocol = (String) options.valueOf("type");
        else
            protocol = "";

        // identify default charset
        if (options.has("setchar"))
            defaultCharset = (String) options.valueOf("setchar");
        else
            defaultCharset = Charset.defaultCharset().name();

        // get store extractor options
        storeExtractorOptions = new StoreExtractorOptions(options.has("keeponlydeep"), options.has("dropemptyfolders"),
                options.has("warning"), namesLength, defaultCharset, options.has("extractlists"), options.has("extractmessagetextfile"),
                options.has("extractmessagetextmetadata"), options.has("extractfiletextfile"),
                options.has("extractfiletextmetadata"), model);

        // specific option parsing for local type extraction
        switch (protocol) {
            case "thunderbird":
            case "pst":
            case "eml":
            case "mbox":
            case "msg":
                if (!options.has("container")) {
                    System.out.println("local " + protocol + " extraction need a container path");
                    System.exit(1);
                }
                if (options.has("server")) {
                    System.err.println("no need a server for local " + protocol + " extraction");
                    System.exit(1);
                }
            case "":
                local = true;
                break;
            default:
                if (!options.has("user")) {
                    System.err.println("need a username for distant access protocol");
                    System.exit(1);
                }
                if (!options.has("server")) {
                    System.err.println("need a server (hostname or ip) for " + protocol + " access protocol");
                    System.exit(1);
                }
                if (options.has("container")) {
                    System.err.println("no container for " + protocol + " access protocol");
                    System.exit(1);
                }
                break;
        }

        // collect or construct all store extractor variables
        user = (String) options.valueOf("user");
        destName = user;
        password = (String) options.valueOf("password");
        String server = (String) options.valueOf("server");
        if (server != null) {
            if (server.indexOf(':') >= 0) {
                host = server.substring(0, server.indexOf(':'));
                port = Integer.parseInt(server.substring(server.indexOf(':') + 1));
            } else
                host = server;
        }
        container = (String) options.valueOf("container");
        folder = (String) options.valueOf("folder");

        if (user == null)
            user = "";
        if (password == null)
            password = "";
        if (server == null)
            server = "";
        if (container == null)
            container = "";
        if (folder == null)
            folder = "";

        if (options.has("rootdir"))
            destRootPath = (String) options.valueOf("rootdir");
        else
            destRootPath = System.getProperty("user.dir");

        // init default store extractors
        StoreExtractor.initDefaultExtractors();

        // if no do option graphic version
        if (!options.has("l") && !options.has("z") && !options.has("x")) {
            new MailExtractGraphicApp(protocol, host, port, user, password, container, folder, destRootPath, destName,
                    storeExtractorOptions, logLevel, local);
        } else {
            StoreExtractor storeExtractor = null;

            if (protocol.isEmpty()) {
                System.err.println(
                        "only imap protocols, thunderbird mbox directory and outlook pst file extraction available");
                System.exit(1);
            }

            // do the job, creating a store extractor and running the extraction
            MailExtractLogger mel = null;
            try {
                mel = new MailExtractLogger(destRootPath + File.separator + destName + ".log", MailExtractLogger.getLevel(logLevel));
                logger = new MailExtractProgressLogger(mel.getProgressLogger(), MailExtractLogger.getLevel(logLevel));

                if (user == null || user.isEmpty())
                    destName = "unknown_extract";
                else
                    destName = user;
                String urlString = StoreExtractor.composeStoreURL(protocol, server, user, password, container);
                storeExtractor = StoreExtractor.createStoreExtractor(urlString, folder,
                        Paths.get(destRootPath, destName).toString(), storeExtractorOptions, logger);
                if (options.has("l") || options.has("z")) {
                    storeExtractor.listAllFolders(options.has("z"));
                } else {
                    storeExtractor.extractAllFolders();
                }
                storeExtractor.endStoreExtractor();
            } catch (ExtractionException ee) {
                logger.progressLogWithoutInterruption(GLOBAL, ee.getMessage());
                logger.logException(ee);
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
                || storeExtractor.getFolderTotalCount() + storeExtractor.getTotalElementsCount() == 0)
            logger.log(GLOBAL, "No writing done");
        else
            logger.log(GLOBAL, "Partial writing done " + Integer.toString(storeExtractor.getFolderTotalCount())
                    + " folders and " + Integer.toString(storeExtractor.getTotalElementsCount()) + " messages");

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
