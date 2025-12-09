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

package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.store.javamail.JMStoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.msg.MsgStoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst.PstStoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst.embeddedmsg.PstEmbeddedStoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.utils.DateRange;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.apache.poi.util.IOUtils;

import jakarta.mail.URLName;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.*;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

/**
 * Abstract factory class for operation context on a defined mailbox or mail
 * file.
 *
 * <p>
 * The {@link #createStoreExtractor createStoreExtractor} call create a
 * StoreExtractor subclass convenient for the declared protocol. This creation
 * specify the target of extraction, the different options of extraction, or
 * listing, and the log level. It makes a first level of connection and
 * compliance check to warranty other methods calls a viable mail box access.
 * <p>
 * The StoreExtractor use sub class extractors to manage different protocols or
 * formats (called schemes). There are defaults sub class for:
 * <ul>
 * <li>IMAP/IMAPS/POP3 (GIMAP experimental) server with user/password login</li>
 * <li>Thunderbird directory containing mbox files and .sbd directory
 * hierarchy</li>
 * <li>mbox file</li>
 * <li>eml file</li>
 * <li>Outlook pst file</li>
 * <li>Msg file</li>
 * </ul>
 * Notice: you have to call {@link #initDefaultExtractors initDefaultExtractors}
 * method before any usage to benefit from these existing extractors, and you
 * can add new ones with {@link #addExtractionRelation addExtractionRelation},
 * and they will be automatically used when needed.
 *
 * <p>
 * The extraction generate on disk a directories/files structure convenient for
 * SEDA archive packet (NF Z44-022), which is:
 * <ul>
 * <li>each folder is extracted as a directory named "Folder#'UniqID': 'name'",
 * with uniqID being a unique ID innerly generated and name being the n first
 * characters of the folder name UTF-8 encoded (n is defined by the option
 * namesLength, default being 12). The folder descriptive metadata is in the
 * file named ArchiveUnitContent.xml or, in V2 model, __ArchiveUnitMetadata (XML/UTF-8 encoded) in its directory. It
 * represent the folder ArchiveUnit with no objects</li>
 * <li>each message is extracted in a directory named "Message#'UniqID':
 * 'name'", with name being the n first characters of the message title UTF-8
 * encoded. It represent the message ArchiveUnit. In this message directory,
 * there's:
 * <ul>
 * <li>the message descriptive metadata file (ArchiveUnitContent.xml or,
 * in V2 model, __ArchiveUnitMetadata),</li>
 * <li>one directory for each special attachments being extractible stores
 * (attached messages or attached files formatted in eml, msg, pst...) (if any)
 * <ul>
 * <li>if simple message, it's named "Message#'UniqID': 'name'", with name being
 * the n first characters of the message title UTF-8 encoded, and contents the
 * message extraction,</li>
 * <li>if complex extraction, it's named "Container#'UniqID': 'name'", with name
 * being the n first characters of the attachment name (if non default to
 * 'infile') UTF-8 encoded, and contents the container descriptive metadata file
 * (ArchiveUnitContent.xml or, in V2 model, __ArchiveUnitMetadata) and
 * the hierarchy of extracted folders, messages, attachments...</li>
 * </ul>
 * <li>one directory by attachment (if any) named "Attachment#'UniqID': 'name'"
 * with the name being the n first characters of the attachment filename. In
 * this folder are :
 * <ul>
 * <li>the attachment descriptive metadata file (ArchiveUnitContent.xml
 * or, in V2 model, __ArchiveUnitMetadata),
 * and</li>
 * <li>the attachment binary file, being a final object, named according the
 * format "__'ObjectType'_'Version'_'filename'" or, in V2 model,
 * "__'ObjectType'_'Version'__'filename'", with in this case ObjectType
 * being "BinaryMaster", version being "1", filename being the attachment file
 * name with extension if any.</li>
 * <li>the text version of attachment binary file, if option is set, named
 * according the format "__'ObjectType'_'Version'_'filename'" or, in V2 model,
 * * "__'ObjectType'_'Version'__'filename'", with in this case
 * ObjectType being "TextContent", version being "1", filename being the
 * attachment file name with extension if any.</li>
 * </ul></li>
 * <li>the message body in eml format file, being a final object, named
 * "__BinaryMaster_1_'messageID'", or in V2 model, "__BinaryMaster_1__'messageID'"
 * with the messageID being the n first characters
 * of the uniq messageID , and</li>
 * <li>the message body in text format file, if option is set, being a final
 * object, named "__TextContent_1_'messageID'", or in V2 model,
 * "__TextContent_1__'messageID'" with the messageID being the n
 * first characters of the uniq messageID.</li>
 * </ul>
 * </ul>
 * Note: any folder's name is modified with starting and ending "__" when the
 * ArchiveUnit, the folder represents, contains an object.
 * <p>
 * For detailed information on descritptive metadata collected see
 * {@link StoreFolder} and {@link StoreMessage}.
 * <p>
 * The extraction or listing operation is logged on console and file
 * (root/username[-timestamp].log - cf args). At the different levels (using
 * {@link MailExtractProgressLogger}) you can have: information
 * about global process and extraction errors (GLOBAL),
 * warning about extraction problems and items dropped (WARNING), list of
 * treated folders (FOLDER), accumulated count of treated messages (MESSAGE_GROUP),
 * list of treated messages (MESSAGE), problems with some expected metadata (MESSAGE_DETAILS).
 * <p>
 * It's also possible (ruled by option) to generate a csv file with one line by
 * extracted message with a selection of metadata, including appointment details
 * <p>
 * Note: Default or generated metadata values are, for now, hardcoded in french
 */
public abstract class StoreExtractor {

    /**
     * The formatter used for zoned date conversion to String
     */
    public static final DateTimeFormatter ISO_8601 = new DateTimeFormatterBuilder()
            .append(ISO_LOCAL_DATE_TIME)
            .optionalStart()
            .appendOffsetId()
            .optionalStart()
            .toFormatter();

    /**
     * The map of mimetypes/scheme known relations.
     */
    static HashMap<String, String> mimeTypeSchemeMap = new HashMap<>();

    /**
     * The map of droidformat/scheme known relations.
     */
    static HashMap<String, String> droidFormatSchemeMap = new HashMap<>();

    /**
     * The map of scheme/extractor class known relations.
     */
    @SuppressWarnings("rawtypes")
    static HashMap<String, Class> schemeStoreExtractorClassMap = new HashMap<>();

    /**
     * The map of scheme/container extraction (vs single file extraction) known
     * relations.
     */
    static HashMap<String, Boolean> schemeContainerMap = new HashMap<>();

    /**
     * Initializes default extractors and sets relevant system properties for handling email
     * and document extraction scenarios. This method configures several components to ensure
     * compatibility with specific formats and address common issues in data extraction processes.
     *
     * The following initializations and configurations are applied:
     *
     * 1. Prevent external tika tools execution on host (tesseract, ffmpeg...) if configured
     *
     * 2. Subscribes default store extractors:
     *    - JMStoreExtractor
     *    - MsgStoreExtractor
     *    - PstStoreExtractor
     *    - PstEmbeddedStoreExtractor
     *
     * 3. Sets the maximum allowed size for byte arrays in Apache POI to prevent extraction 
     *    failures when handling large attachments in TNEF files. This change overrides 
     *    the default 1 MB limit with a maximum value of 2 GB.
     *
     * 4. Configures Jakarta Mail properties to handle malformed MIME messages:
     *    - Enables fallback for unknown content-transfer-encoding values, which allows
     *      the parsing of messages with invalid encoding values (e.g., "iso-8859-1").
     *    - Disables strict decoding of MIME-encoded headers, allowing the library 
     *      to tolerate malformed headers and return raw, undecoded text instead of throwing exceptions.
     *    - Enables lenient Base64 decoding to tolerate malformed or corrupted Base64 encoded data,
     *      avoiding exceptions during data extraction.
     *
     * 4. Sets the default time zone for the application to UTC.
     */
    public static void initDefaultExtractors(boolean allowsExternalToolsForTextExtraction) {

        if (!allowsExternalToolsForTextExtraction) {
            preventExternalToolsForTextExtractionInTika();
        }

        JMStoreExtractor.subscribeStoreExtractor();
        MsgStoreExtractor.subscribeStoreExtractor();
        PstStoreExtractor.subscribeStoreExtractor();
        PstEmbeddedStoreExtractor.subscribeStoreExtractor();

        // HMEF calls when extracting TNEF can generate an exception if an attachment extraction is bigger than 1Mo,
        // this change the limit to Integer.MAX_VALUE that is to say 2Go
        // This is also a bypass to a bug in POI 4.0 and 4.1 version when opening msg
        IOUtils.setByteArrayMaxOverride(Integer.MAX_VALUE);

        /*
         * Enable fallback for unknown Content-Transfer-Encoding values.
         *
         * Some malformed MIME messages incorrectly specify a charset name (e.g., "iso-8859-1")
         * as the Content-Transfer-Encoding value instead of using valid encodings like "7bit",
         * "8bit", "base64" or "quoted-printable".
         *
         * By setting the "mail.mime.ignoreunknownencoding" system property to true,
         * Jakarta Mail will silently ignore unknown encodings and treat the content as "8bit".
         * This avoids exceptions (e.g., IOException: Unknown encoding) during message parsing.
         *
         * Limitations:
         * - If the actual encoding is "base64" or "quoted-printable" but declared incorrectly,
         *   the message content may not be decoded properly and appear corrupted or unreadable.
         * - This is a best-effort fallback intended for resilience, not full correctness.
         */
        System.setProperty("mail.mime.ignoreunknownencoding", "true");

        /*
         * Disable strict decoding of MIME-encoded headers (RFC 2047).
         *
         * By default, Jakarta Mail enforces strict parsing of encoded words
         * (=?charset?encoding?encoded-text?=) in headers such as "Subject" or "From".
         * If a header is malformed — for example, using an invalid charset name or
         * broken Base64/Quoted-Printable content — it throws an exception.
         *
         * Setting "mail.mime.decodetext.strict" to "false" allows Jakarta Mail
         * to tolerate such malformed headers by skipping the faulty encoding and
         * returning the raw, undecoded text instead.
         *
         * ⚠ Note: this does not fix the content — it just avoids an exception and
         * preserves the original text block for manual or partial decoding if needed.
         */
        System.setProperty("mail.mime.decodetext.strict", "false");

        /*
         * Enable lenient Base64 decoding of MIME parts.
         *
         * By default, Jakarta Mail enforces strict Base64 decoding and will throw an exception
         * if it encounters errors (e.g., malformed or corrupted Base64 encoded data). By setting
         * "mail.mime.base64.ignoreerrors" to "true", you instruct Jakarta Mail to ignore such
         * errors and continue processing the data.
         *
         * This configuration is particularly useful in scenarios where you expect non-standard
         * or imperfectly encoded data and prefer to bypass decoding exceptions in favor of
         * processing the available content, albeit with a potential risk of data corruption.
         *
         * ⚠ Note: While lenient decoding prevents your application from stopping due to decoding
         * errors, it could lead to silent data loss or misinterpretation if the encoded data is critical.
         */
        System.setProperty("mail.mime.base64.ignoreerrors", "true");
    }

    private static void preventExternalToolsForTextExtractionInTika() {
        // Apache Tika may use external tools (ffmpeg, exiftool, sox, tesseract...)
        // Manually instantiating the TikaConfig class does not work because some parsers still use internally the
        // TikaConfig.getDefaultConfig() method, which by default allows external tools.
        // Setting the "tika.config" system property is the only solution that seems to work.
        URL url = StoreExtractor.class.getClassLoader().getResource("tika-without-externals.config");
        try {
            System.setProperty("tika.config", Objects.requireNonNull(url).toURI().toString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // StoreExtractor definition parameters

    /**
     * Scheme defining specific store extractor (imap| imaps| pop3| thunderbird|
     * mbox| eml| pst| msg| experimental gimap)...)
     */
    protected final String scheme;

    /**
     * Hostname of target store in ((hostname|ip)[:port]) *.
     */
    protected final String host;

    /**
     * Port of target store in ((hostname|ip)[:port]) *.
     */
    protected final int port;

    /**
     * User account name, can be null if not used *.
     */
    protected final String user;

    /**
     * Password, can be null if not used *.
     */
    protected final String password;

    /**
     * Path of ressource to extract, can be null if not used  *.
     */
    protected final String path;

    /**
     * Path of the folder in the store used as root for extraction, can be null
     * if default root folder.
     */
    protected final String rootStoreFolderName;

    /**
     * Path of the directory where will be the extraction directory.
     */
    protected final String destRootPath;

    /**
     * Name of the extraction directory.
     */
    protected final String destName;

    /**
     * Extractor options, flags coded on an int, defined thru constants *.
     */
    protected final StoreExtractorOptions options;

    /**
     * Extractor context description.
     */
    protected String description;

    // private field for global statictics
    private final AtomicLong totalRawSize;

    // private field for time statistics
    private Instant start;
    private Instant end;

    // private object extraction root folder in store
    private StoreFolder rootStoreFolder;

    // private father storeExtractor for nested extraction, null if root
    private final StoreExtractor fatherStoreExtractor;

    // private root storeExtractor for nested extraction, this if root
    private final StoreExtractor rootStoreExtractor;

    // private father element for nested extraction, null if root
    private final StoreElement fatherElement;

    // private logger
    private MailExtractProgressLogger logger;

    /**
     * The Global lists ps map.
     * private map of printstreams for global lists extraction
     * (messages, contacts, appointments...)
     */
    protected Map<String, PrintStream> globalListsPSMap;

    /**
     * The accumulated elements classes
     */
    static Class[] accumulatedElements = {StoreFolder.class, StoreMessage.class,
            StoreAppointment.class, StoreContact.class};

    /**
     * The extracted elements counters map.
     * private map of counters for extracted elements
     * (messages, contacts, appointments...)
     * It is final to enable synchronization on folder content changes during parallel processing of elements.
     */
    private Map<String, Integer> elementsCounterMap;

    /**
     * The sub-extracted elements counters map.
     * private map of counters for extracted elements from inner containers
     * (messages, contacts, appointments...)
     */
    private Map<String, Integer> subElementsCounterMap;

    /**
     * UniqID for archive unit identification must be thread safe
     */
    private AtomicInteger uniqID;

    /**
     * The maximum number of parallel threads, automatically defined.
     * Useful only for testing purposes.
     */
    private static final double THREAD_MULTIPLIER = Double.parseDouble(System.getProperty("thread.factor", "1.5"));
    private int maxParallelThreads = (int) Math.round(
            Runtime.getRuntime().availableProcessors() * THREAD_MULTIPLIER);

    /**
     * Add mimetypes, scheme, isContainer, store extractor known relation.
     * <p>
     * This is used by store extractor sub classes to subscribe. When the
     * relation is known it can be used for processing automatically the
     * mimetype files with appropriate store extractors, in code.
     * <p>
     * Warning: the mime type has to be the code returned by tika!
     *
     * @param mimeType    the mime type
     * @param scheme      the scheme
     * @param isContainer the is container
     * @param extractor   the extractor
     */
    @SuppressWarnings("rawtypes")
    public static void addExtractionRelation(String mimeType, String droidFormat, String scheme, boolean isContainer, Class extractor) {
        // if there is a file mimetype for this scheme
        if (mimeType != null)
            mimeTypeSchemeMap.put(mimeType, scheme);
        if (droidFormat != null)
            droidFormatSchemeMap.put(droidFormat, scheme);
        schemeStoreExtractorClassMap.put(scheme, extractor);
        schemeContainerMap.put(scheme, isContainer);
    }

    /**
     * Get protocol if droid format is a mail format ready for extraction.
     *
     * @param droidFormat the Droid format
     * @return the protocol
     */
    public static String getProtocolFromDroidFormat(String droidFormat) {
        return droidFormatSchemeMap.get(droidFormat);
    }

    /**
     * Compose an URL String.
     *
     * @param scheme    Type of local store to extract (thunderbird|pst|eml|mbox) or                  protocol for server access (imap|imaps|pop3...)
     * @param authority Server:port of target account ((hostname|ip)[:port
     * @param user      User account name, can be null if not used
     * @param password  Password, can be null if not used
     * @param path      Path to the ressource
     * @return the string
     */
    public static String composeStoreURL(String scheme, String authority, String user, String password, String path) {
        String result = null;

        try {
            result = scheme + "://";
            if (user != null && !user.isEmpty()) {
                result += URLEncoder.encode(user, "UTF-8");
                if (password != null && !password.isEmpty())
                    result += ":" + URLEncoder.encode(password, "UTF-8");
                result += "@";
            }
            if (authority != null && !authority.isEmpty())
                result += authority;
            else
                result += "localhost";
            if (path != null && !path.isEmpty())
                result += "/" + URLEncoder.encode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // impossible case with UTF-8
        }
        return result;
    }

    /**
     * Init the PrintStream for a global list generated for a certain type of element (message, folder, appointment, contact...),
     * if not already done with the header in csv format, and return the printstream
     * <p>This method is thread-safe, enabling Elements of non-abstract Extractors to get, and may be initialize, PrintStreams in parallel.
     *
     * @param listClass the list class
     * @return the initialized global list ps
     */
    @SuppressWarnings("unchecked")
    synchronized public PrintStream getGlobalListPS(Class listClass) {
        String globalListName = null;
        PrintStream result = null;
        try {
            globalListName = (String) listClass.getMethod("getElementName").invoke(null);
            result = globalListsPSMap.get(globalListName);
            if (result == null) {
                String dirname = this.destRootPath
                        + File.separator + this.destName + File.separator;
                Files.createDirectories(Paths.get(dirname));
                result = new PrintStream(dirname + listClass.getMethod("getElementName").invoke(null) + ".csv");
                globalListsPSMap.put(globalListName, result);
                listClass.getMethod("printGlobalListCSVHeader", PrintStream.class).invoke(null, result);
            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException e) {
            doProgressLogWithoutInterruption(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: can't create global list for [" + globalListName + "] csv file", e);
        } catch (InvocationTargetException te) {
            doProgressLogWithoutInterruption(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: can't create global list for [" + globalListName + "] csv file", te.getTargetException());
        }
        return result;
    }

    /**
     * Return the counter for a certain type of extracted element (message, folder, appointment, contact...) if it exists.
     * <p>If not init the counter to 0, and return the counter.
     * <p>If subFlag is true, it actually act on the sub-extracted elements from inner containers counter.
     * <p>This method is thread-safe, enabling non-abstract Extractors to use counters in parallel.
     *
     * @param listClass the list class
     * @param subFlag   the sub extracted flag
     * @return the initialized global list counter
     */
    @SuppressWarnings("unchecked")
    synchronized public int getElementCounter(Class listClass, boolean subFlag) {
        String elementName = null;
        Integer result = 0;
        try {
            elementName = (String) listClass.getMethod("getElementName").invoke(null);
            result = (subFlag ? subElementsCounterMap : elementsCounterMap).get(elementName);
            if (result == null) {
                (subFlag ? subElementsCounterMap : elementsCounterMap).put(elementName, 0);
                result = 0;
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            doProgressLogWithoutInterruption(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: can't create counter for " + (subFlag ? "sub " : "") + "[" + elementName + "] csv file", e);
        } catch (InvocationTargetException te) {
            doProgressLogWithoutInterruption(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: can't create counter for " + (subFlag ? "sub " : "") + "[" + elementName + "] csv file", te.getTargetException());
        }
        return result;
    }


    /**
     * Adds a value to the counter for a specific type of extracted element (e.g., message, folder, appointment, contact...) and returns the updated counter.
     * <p>If the counter does not exist, it is initialized with the provided value and then returned.
     * <p>If {@code subFlag} is true, the operation is applied to the counters of sub-extracted elements from inner containers.
     * <p>This method is thread-safe, enabling non-abstract Extractors to use counters in parallel.
     *
     * @param value     the value to add to the counter
     * @param listClass the class representing the type of the extracted element
     * @param subFlag   a flag indicating if the operation should target sub-extracted elements
     * @return the updated counter value
     */
    @SuppressWarnings("unchecked")
    synchronized public int addElementCounter(int value, Class listClass, boolean subFlag) {
        String elementName = null;
        Integer result = 0;
        try {
            elementName = (String) listClass.getMethod("getElementName").invoke(null);
            result = (subFlag ? subElementsCounterMap : elementsCounterMap).get(elementName);
            if (result == null)
                result = value;
            else
                result += value;
            (subFlag ? subElementsCounterMap : elementsCounterMap).put(elementName, result);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            doProgressLogWithoutInterruption(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: can't create counter for " + (subFlag ? "sub " : "") + "[" + elementName + "] csv file", e);
        } catch (InvocationTargetException te) {
            doProgressLogWithoutInterruption(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: can't create counter for " + (subFlag ? "sub " : "") + "[" + elementName + "] csv file", te.getTargetException());
        }
        return result;
    }


    /**
     * Increment the counter for a certain type of extracted element (message, folder, appointment, contact...), and return the counter if it exists.
     * <p>If not init the counter to 1, and return the counter.
     *
     * @param listClass the list class
     * @return the int
     */
    @SuppressWarnings("unchecked")
    public int incElementCounter(Class listClass) {
        return addElementCounter(1, listClass, false);
    }

    /**
     * Accumulate elements of a container extractor in sub element counters.
     * <p>This method is thread-safe, enabling non-abstract Extractors to use counters in parallel.
     *
     * @param subExtractor the sub extractor
     */
    synchronized public void accumulateSubElements(StoreExtractor subExtractor) {
        for (Class c : accumulatedElements) {
            int value = subExtractor.getElementCounter(c, false) +
                    subExtractor.getElementCounter(c, true);
            if (value > 0)
                addElementCounter(value, c, true);
        }
    }

    /**
     * Close the PrintStream in map. Can be override if necessary
     */
    protected void closeGlobalListsPSMap() {
        for (String psName : globalListsPSMap.keySet()) {
            globalListsPSMap.get(psName).close();
        }
    }

    /**
     * Instantiates a new store extractor.
     *
     * @param urlString            the url string
     * @param rootStoreFolderName  Path of the extracted folder in the store box, can be null if default root folder
     * @param destPathString       the dest path string
     * @param options              Extractor options
     * @param fatherStoreExtractor the creating store extractor in nested extraction, or null if root one
     * @param fatherElement        the father element in nested extraction, or null if root one
     * @param logger               logger used
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     */
    protected StoreExtractor(String urlString, String rootStoreFolderName, String destPathString, StoreExtractorOptions options,
                             StoreExtractor fatherStoreExtractor, StoreElement fatherElement, MailExtractProgressLogger logger) throws MailExtractLibException {

        URLName url;
        url = new URLName(urlString);

        this.scheme = url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort();

        String tempUser = null;
        String tempPassword = null;
        String tempPath = null;
        try {
            if (url.getUsername() != null)
                tempUser = URLDecoder.decode(url.getUsername(), "UTF-8");
            if (url.getPassword() != null)
                tempPassword = URLDecoder.decode(url.getPassword(), "UTF-8");
            if (url.getFile() != null)
                tempPath = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // not possible
        }
        this.user = tempUser;
        this.password = tempPassword;
        this.path = tempPath;

        this.rootStoreFolderName = rootStoreFolderName;
        this.destRootPath = Paths.get(destPathString).toAbsolutePath().normalize().getParent().toString();
        this.destName = Paths.get(destPathString).toAbsolutePath().normalize().getFileName().toString();
        if (options == null)
            this.options = new StoreExtractorOptions();
        else
            this.options = options;

        this.totalRawSize = new AtomicLong(0);

        this.fatherStoreExtractor = fatherStoreExtractor;
        this.fatherElement = fatherElement;
        if (fatherStoreExtractor == null)
            this.rootStoreExtractor = this;
        else
            this.rootStoreExtractor = fatherStoreExtractor.rootStoreExtractor;
        this.logger = logger;

        this.description = ":p:" + scheme + ":u:" + user;

        globalListsPSMap = new ConcurrentHashMap<>();
        elementsCounterMap = new ConcurrentHashMap<>();
        subElementsCounterMap = new ConcurrentHashMap<>();

        uniqID = new AtomicInteger(0);

        doProgressLogIfDebug(logger, "StoreExtractor [" + this + "] created with url=" + urlString +
                " rootFolder=" + rootStoreFolderName + " destPath=" + destPathString + " rootExtractor=" + fatherStoreExtractor, null);
    }

    /**
     * Log the context of the StoreExtractor.
     *
     * @throws InterruptedException the interrupted exception
     */
    public void writeTargetLog() throws InterruptedException {

        // if root extractor log extraction context
        if (fatherStoreExtractor == null) {
            doProgressLog(logger, MailExtractProgressLogger.GLOBAL,
                    "mailextract :target store with scheme=" + scheme + (host == null || host.isEmpty() ? "" : "  server=" + host)
                            + (port == -1 ? "" : ":" + Integer.toString(port))
                            + (user == null || user.isEmpty() ? "" : " user=" + user)
                            + (password == null || password.isEmpty() ? "" : " password=" + password)
                            + (path == null || path.isEmpty() ? "" : " path=" + path)
                            + (rootStoreFolderName == null || rootStoreFolderName.isEmpty() ? "" : " store folder=" + rootStoreFolderName), null);
            doProgressLog(logger, MailExtractProgressLogger.GLOBAL, "to " + destRootPath + " in " + destName + " directory", null);
            if ((logger != null) && logger.getDebugFlag())
                doProgressLog(logger, MailExtractProgressLogger.GLOBAL, "DEBUG MODE", null);

            boolean first = true;
            String optionsLog = "";
            if (options.keepOnlyDeepEmptyFolders) {
                optionsLog += "keeping all empty folders except root level ones";
                first = false;
            }
            if (options.dropEmptyFolders) {
                if (!first)
                    optionsLog += ", ";
                optionsLog += "droping all empty folders";
                first = false;
            }
            if (options.warningMsgProblem) {
                if (!first)
                    optionsLog += ", ";
                optionsLog += "generate warning when there's a problem on a message (otherwise log at FINEST level)";
                first = false;
            }
            if (!first)
                optionsLog += ", ";
            optionsLog += "with names length=" + Integer.toString(options.namesLength);
            optionsLog += ", ";
            optionsLog += "with log level " + getProgressLogger().getLevelName();

            doProgressLog(logger, MailExtractProgressLogger.GLOBAL, optionsLog, null);
        }
        // if internal extractor give attachment context
    }

    /**
     * Gets the logger created during the store extractor construction, and used
     * in all mailextract classes.
     *
     * <p>
     * For convenience each class which may have some log actions has it's own
     * getProgressLogger method always returning this store extractor logger.
     *
     * @return logger progress logger
     */
    public MailExtractProgressLogger getProgressLogger() {
        return logger;
    }

    /**
     * Checks for dest name.
     *
     * @return true, if successful
     */
    public boolean hasDestName() {
        return !((destName == null) || destName.isEmpty());
    }

    /**
     * Gets a unique ID within the store extractor context.
     * <p>
     * The sequence is incremented with each call in the root store extractor context,
     * ensuring uniqueness throughout the entire extraction process, including nested extractions.
     * This method is thread-safe.
     *
     * @return a unique ID
     */

    synchronized public int getNewUniqID() {
        int id;
        if (fatherStoreExtractor == null)
            id = uniqID.addAndGet(1);
        else
            id = rootStoreExtractor.getNewUniqID();
        return id;
    }

    /**
     * Add to total raw size.
     * <p>This method is thread-safe, enabling non-abstract Extractors to use size accumulator in parallel.
     *
     * @param elementSize the element size
     */
    public void addTotalRawSize(long elementSize) {
        totalRawSize.addAndGet(elementSize);
    }

    /**
     * Gets the total raw size of all analyzed elements.
     * <p>
     * The "raw" size is the sum of the size of elements as in the store, the
     * extraction will be larger (up to x2)
     *
     * @return the total raw size
     */
    public long getTotalRawSize() {
        return totalRawSize.get();
    }

    /**
     * Gets the extraction context description.
     *
     * @return the description String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Is the store extractor the root one in nested extraction.
     *
     * @return the description String
     */
    public boolean isRoot() {
        return fatherStoreExtractor == null;
    }

    /**
     * Gets the extraction root folder in store.
     *
     * @return the root StoreFolder
     */
    public StoreFolder getRootFolder() {
        return rootStoreFolder;
    }

    /**
     * Sets the extraction root folder in store.
     *
     * @param rootFolder the new root folder
     */
    public void setRootFolder(StoreFolder rootFolder) {
        rootStoreFolder = rootFolder;
    }

    /**
     * Gets the store extractor options.
     *
     * @return the store extractor options
     */
    public StoreExtractorOptions getOptions() {
        return options;
    }

    /**
     * Gets father element in nested extraction, null if root.
     *
     * @return the father element
     */
    public StoreElement getFatherElement() {
        return fatherElement;
    }

    /**
     * Create a store extractor for the declared scheme in url as a factory
     * creator.
     *
     * @param urlString           the url string
     * @param rootStoreFolderName Path of the extracted folder in the account mail box, can be                       null if default root folder
     * @param destPathString      the dest path string
     * @param options             Options (flag composition of CONST_)
     * @param logger              logger used
     * @return the store extractor, constructed as a non abstract subclass
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     */
    public static StoreExtractor createStoreExtractor(String urlString, String rootStoreFolderName, String destPathString,
                                                      StoreExtractorOptions options, MailExtractProgressLogger logger) throws MailExtractLibException {
        StoreExtractor storeExtractor;

        storeExtractor = createInternalStoreExtractor(urlString, rootStoreFolderName, destPathString, options, null, logger);

        return storeExtractor;
    }

    /**
     * Create an internal depth store extractor as a factory creator.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static StoreExtractor createInternalStoreExtractor(String urlString, String rootStoreFolderName,
                                                               String destPathString, StoreExtractorOptions options, StoreExtractor
                                                                       rootStoreExtractor, MailExtractProgressLogger logger
    ) throws MailExtractLibException {

        StoreExtractor store;
        URLName url;

        url = new URLName(urlString);

        // get read of leading file separator in folder
        if ((rootStoreFolderName != null) && (!rootStoreFolderName.isEmpty()) && (rootStoreFolderName.substring(0, 1).equals(File.separator)))
            rootStoreFolderName = rootStoreFolderName.substring(1);

        // find the store extractor constructor for scheme in URL
        Class storeExtractorClass = StoreExtractor.schemeStoreExtractorClassMap.get(url.getProtocol());
        if (storeExtractorClass == null) {
            throw new MailExtractLibException("mailextractlib: unknown store type=" + url.getProtocol(), null);
        } else {
            try {
                store = (StoreExtractor) storeExtractorClass.getConstructor(String.class, String.class, String.class,
                                StoreExtractorOptions.class, StoreExtractor.class, MailExtractProgressLogger.class)
                        .newInstance(urlString, rootStoreFolderName, destPathString, options, rootStoreExtractor, logger);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
                     | SecurityException e) {
                throw new MailExtractLibException("mailextractlib: dysfonctional store type=" + url.getProtocol(), e);
            } catch (InvocationTargetException e) {
                Throwable te = e.getCause();
                throw new MailExtractLibException("mailextractlib: dysfonctional store type=" + url.getProtocol(), te);
            }
        }
        return store;
    }

    private static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Get summary string with time and statistics.
     *
     * @return the string
     */
    public String getSummary() {
        String summary = Duration.between(start, end).toString();
        String elementSummary = "";
        for (Class elementClass : accumulatedElements) {
            String elementName = null;
            try {
                elementName = (String) elementClass.getMethod("getElementName").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                doProgressLogIfDebug(logger, "GetSummary error", e);
            }
            Integer count = elementsCounterMap.get(elementName);
            if ((count != null) && (count > 0)) {
                if (!elementSummary.isEmpty())
                    elementSummary += ",";
                elementSummary += " " + count + " " + elementName;
            }
        }
        if (elementSummary.isEmpty())
            summary += " empty extraction";
        else
            summary += " writing" + elementSummary;
        String subElementSummary = "";
        for (Class elementClass : accumulatedElements) {
            String elementName = null;
            try {
                elementName = (String) elementClass.getMethod("getElementName").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                doProgressLogIfDebug(logger, "GetSummary error", e);
            }
            Integer count = subElementsCounterMap.get(elementName);
            if ((count != null) && (count > 0)) {
                if (!subElementSummary.isEmpty())
                    subElementSummary += ",";
                subElementSummary += " " + count + " " + elementName;
            }
        }
        if (subElementSummary.isEmpty())
            summary += " without embedded elements";
        else
            summary += " with" + subElementSummary + " embedded";
        summary += " for a total size of " + readableFileSize(getTotalRawSize());
        return summary;
    }

    /**
     * Extract all folders from the defined root folder (considering drop
     * options).
     *
     * <p>
     * This is a method where the extraction structure and content is partially
     * defined (see also {@link StoreMessage#extractMessage extractMessage} and
     * {@link StoreFolder#extractFolder extractFolder}).
     *
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    public void extractAllFolders() throws MailExtractLibException, InterruptedException {
        String title;

        start = Instant.now();

        writeTargetLog();
        doProgressLog(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: extraction begin", null);

        rootStoreFolder.extractFolderAsRoot(true);

        ArchiveUnit rootNode = rootStoreFolder.getArchiveUnit();
        rootNode.addMetadata("DescriptionLevel", "RecordGrp", true);

        // title generation from context
        if ((user != null) && (!user.isEmpty()))
            title = "Ensemble des messages électroniques et informations associées (contacts, rendez-vous...) envoyés et reçus par le compte " + user;
        else if ((path != null) && (!path.isEmpty()))
            title = "Ensemble des messages électroniques et informations associées (contacts, rendez-vous...) du container " + path;
        else
            title = "Ensemble de messages électroniques et informations associées (contacts, rendez-vous...)";
        if ((host != null) && (!host.isEmpty()) && (!host.equals("localhost")))
            title += " sur le serveur " + host + (port == -1 ? "" : ":" + Integer.toString(port));
        title += " à la date du " + start;
        rootNode.addMetadata("Title", title, true);
        if (rootStoreFolder.getDateRange().isDefined()) {
            rootNode.addMetadata("StartDate", DateRange.getISODateString(rootStoreFolder.getDateRange().getStart()),
                    true);
            rootNode.addMetadata("EndDate", DateRange.getISODateString(rootStoreFolder.getDateRange().getEnd()), true);
        }
        rootNode.write();

        end = Instant.now();
        String summary = "Terminated in " + getSummary();
        doProgressLog(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: " + summary, null);
        System.out.println(summary);
    }

    /**
     * List all folders from the defined root folder (no drop options).
     *
     * <p>
     * Warning: listing with detailed information is a potentially expensive
     * operation, especially when accessing distant account, as all elements are
     * inspected (in the case of a distant account that mean also
     * downloaded...).
     *
     * @param stats true if detailed information (number and raw size of elements              in each folder) is asked for
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    public void listAllFolders(boolean stats) throws MailExtractLibException, InterruptedException {
        String time;
        String tmp;
        Duration d;

        start = Instant.now();

        int memProgressLogLevel=logger.getProgressLogLevel();
        logger.setProgressLogLevel(GLOBAL);
        writeTargetLog();
        doProgressLog(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: listing begin", null);

        rootStoreFolder.listFolder(stats);

        end = Instant.now();
        String summary = "Terminated in " + getSummary();
        doProgressLog(logger, MailExtractProgressLogger.GLOBAL, "mailextractlib: " + summary, null);
        System.out.println(summary);
        logger.setProgressLogLevel(memProgressLogLevel);
    }

    /**
     * Do all end tasks for the StoreExtractor, like deleting temporary files.
     *
     * @throws MailExtractLibException the extraction exception
     */
    public void endStoreExtractor() throws MailExtractLibException {
        closeGlobalListsPSMap();
    }

    /**
     * Utility function to detect if the four first bytes are a defined magic number.
     *
     * @param content     the content
     * @param magicNumber the magic number
     * @return true, if successful
     */
    public static boolean hasMagicNumber(byte[] content, byte[] magicNumber) {
        return hasMagicNumber(content, magicNumber, 0);
    }

    /**
     * Utility function to detect if the bytes at offset are a defined magic number.
     *
     * @param content     the content
     * @param magicNumber the magic number
     * @param offset      the offset
     * @return true, if successful
     */
    public static boolean hasMagicNumber(byte[] content, byte[] magicNumber, int offset) {
        if (content.length < magicNumber.length + offset)
            return false;
        for (int i = 0; i < magicNumber.length; i++) {
            if (content[i + offset] != magicNumber[i])
                return false;
        }
        return true;
    }

    /**
     * Gets the attachment in which is the embedded object.
     *
     * @return the attachment
     */
    public abstract StoreAttachment getAttachment();

    /**
     * Tests if this store extractor can generate objects lists
     * (mails, contacts, appointments...).
     *
     * @return the flag true or false
     */
    public abstract boolean canExtractObjectsLists();

    /**
     * Gets the scheme if this content can be managed by this StoreExtractor, or
     * null
     *
     * @param content the content
     * @return the scheme
     */
    public static String getVerifiedScheme(byte[] content) {
        return null;
    }

    /**
     * Gets the maximum number of parallel threads used for processing.
     * This value is automatically defined and is useful only for testing purposes.
     *
     * @return the maximum number of parallel threads.
     */
    public int getMaxParallelThreads() {
        return maxParallelThreads;
    }

    /**
     * Sets the maximum number of parallel threads used for processing.
     *
     * @param maxParallelThreads the maximum number of parallel threads.
     */
    public void setMaxParallelThreads(int maxParallelThreads) {
        this.maxParallelThreads = maxParallelThreads;
    }

    /**
     * Gets the root store extractor in nested extractions.
     *
     * @return the root extractor.
     */
    public StoreExtractor getRootStoreExtractor() {
        return rootStoreExtractor;
    }

}