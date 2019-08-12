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

import fr.gouv.vitam.tools.mailextractlib.formattools.HTMLTextExtractor;
import fr.gouv.vitam.tools.mailextractlib.formattools.TikaExtractor;
import fr.gouv.vitam.tools.mailextractlib.formattools.rtf.HTMLFromRTFExtractor;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.nodes.MetadataPerson;
import fr.gouv.vitam.tools.mailextractlib.utils.DateRange;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.mailextractlib.utils.RawDataSource;
import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLogIfStep;

/**
 * Abstract class for store element which is a mail box message.
 * <p>
 * It defines all information (descriptive metadata and objects) to collect from
 * a message and the method to generate directory/files structure from this
 * information. Each subclass has to be able to extract these informations from
 * a message.
 * <p>
 * It is able to generate a mime fake of the message, if not natively Mime.
 * <p>
 * Metadata information to collect in Vitam guidelines for mail extraction
 * <ul>
 * <li>Subject (Title metadata),</li>
 * <li>List of "From" addresses (Writer metadata),</li>
 * <li>List of "To" recipients addresses (Addressee metadata),</li>
 * <li>List of "Cc" and "Bcc" recipients addresses (Recipient metadata),</li>
 * <li>List of "Reply-To" addresses (ReplyTo metadata),</li>
 * <li>List of "Return-Path" addresses, more reliable information given by the
 * first mail relay server (ReturnPath metadata),</li>
 * <li>Message unique ID given by the sending server (OriginatingSystmId
 * metadata),</li>
 * <li>Sent date (SentDate metadata),</li>
 * <li>Received date (ReceivedDate metadata),</li>
 * <li>Message unique ID of the message replied to (and in some implementation
 * forwarded) by the current message (OriginatingSystemIdReplyTo metadata),</li>
 * <li>Message body textual content, if option set</li>
 * </ul>
 * In the descriptive metadata is also added the DescriptionLevel, which is Item for message and for Attachements.
 * <p>
 * Content information extracted
 * <ul>
 * <li>All Body Content, text, html and rtf extraction of the message body, when
 * it exists,</li>
 * <li>Attachments, content with filename,</li>
 * </ul>
 * <p>
 * All values can be null, it then express that the metadata is not defined for
 * this message.
 */
public abstract class StoreMessage extends StoreElement {

    // /** Message nature (MESSAGE, CALENDAR). */
    // protected int nature;
    //
    // /** The Constant MESSAGE. */
    // static public final int MESSAGE = 0;
    //
    // /** The Constant CALENDAR. */
    // static public final int CALENDAR = 1;
    //
    /**
     * Raw binary content of the message for mime sources, or of the mime fake
     * for others.
     */
    protected byte[] mimeContent;

    /**
     * Mime fake if any, or null for mime source.
     */
    protected MimeMessage mimeFake;

    /**
     * Different versions of the message body.
     */
    protected String[] bodyContent = new String[3];

    /**
     * The Constant TEXT_BODY.
     */
    static public final int TEXT_BODY = 0;

    /**
     * The Constant HTML_BODY.
     */
    static public final int HTML_BODY = 1;

    /**
     * The Constant RTF_BODY.
     */
    static public final int RTF_BODY = 2;

    /**
     * The Constant OUT_OF_BODY.
     */
    static public final int OUT_OF_BODY = 3;

    /**
     * Complete mail header from original smtp format, if any.
     */
    protected List<String> mailHeader;

    /**
     * Attachments list.
     */
    protected List<StoreAttachment> attachments;

    /**
     * Appointment information.
     */
    protected StoreMessageAppointment appointment;

    /**
     * Subject.
     */
    protected String subject;

    /**
     * "From" address.
     */
    protected String from;

    /**
     * List of "To" recipients addresses.
     */
    protected List<String> recipientTo;

    /**
     * List of "Cc"recipients addresses.
     */
    protected List<String> recipientCc;

    /**
     * List of "Bcc" recipients addresses.
     */
    protected List<String> recipientBcc;

    /**
     * List of "Reply-To" addresses.
     */
    protected List<String> replyTo;

    /**
     * "Return-Path" address.
     */
    protected String returnPath;

    /**
     * Sent date.
     */
    protected Date sentDate;

    /**
     * Received date.
     */
    protected Date receivedDate;

    /**
     * Message unique ID given by the sending server.
     */
    protected String messageID;

    /**
     * Message unique ID of the message replied to (and in some implementation
     * forwarded) by the current message.
     */
    protected String inReplyToUID;

    /**
     * List of message unique ID of the message in the same thread of forward
     * and reply.
     */
    protected List<String> references;

    /**
     * List of "Sender" addresses.
     */
    protected List<String> sender;

    /**
     * Message ArchiveUnit.
     */
    public ArchiveUnit messageNode;

    /**
     * The line id in csv list.
     */
    protected int listLineId;

    /**
     * Instantiates a new mail box message.
     *
     * @param storeFolder Mail box folder containing this message
     */
    protected StoreMessage(StoreFolder storeFolder) {
        super(storeFolder);
    }

    /**
     * Gets the sent date.
     *
     * <p>
     * Specific metadata getter used for folder date range computation.
     *
     * @return the sent date
     */
    public Date getSentDate() {
        return sentDate;
    }

    /**
     * Gets the message size.
     *
     * <p>
     * Specific information getter used for listing size statistic of folders.
     * Depend on sub class implementation.
     *
     * @return the message size
     * @throws InterruptedException the interrupted exception
     */
    public abstract long getMessageSize() throws InterruptedException;

    /**
     * Gets the message subject.
     *
     * @return the message subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets the message rfc822 form
     *
     * <p>
     * Either the original content if mime message or a fake mime message
     * generated form, generated during extraction operation.
     * <p>
     * <b>Important:</b> this is computed during message extraction
     *
     * @return the mime content
     */
    public byte[] getMimeContent() {
        return mimeContent;
    }

    @Override
    public String getLogDescription() {
        String result = "message "+getStoreExtractor().getGlobalListCounter(this.getClass());
        if (subject != null)
            result += " [" + subject + "]";
        else
            result += " [no subject]";
        return result;
    }

    /*
     * Header analysis methods to be implemented for each StoreMessage
     * implementation
     */

    /**
     * Get and generate the complete message mime header from original format,
     * if any, and all other information useful for analyzing.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void prepareAnalyze() throws InterruptedException;

    /**
     * Analyze message to get Subject metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeSubject() throws InterruptedException;

    /**
     * Analyze message to get Message-ID metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeMessageID() throws InterruptedException;

    /**
     * Analyze message to get From metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeFrom() throws InterruptedException;

    /**
     * Analyze message to get recipients (To, cc and bcc) metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeRecipients() throws InterruptedException;

    /**
     * Analyze message to get Reply-To metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeReplyTo() throws InterruptedException;

    /**
     * Analyze message to get Return-Path metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeReturnPath() throws InterruptedException;

    /**
     * Analyze message to get sent and received dates metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeDates() throws InterruptedException;

    /**
     * Analyze message to get In-Reply-To metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeInReplyToId() throws InterruptedException;

    /**
     * Analyze message to get References metadata.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeReferences() throws InterruptedException;

    /*
     * Content analysis methods to be implemented for each StoreMessage
     * implementation
     */

    /**
     * Analyze message to get the different bodies (text, html, rtf) if any.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeBodies() throws InterruptedException;

    /**
     * Analyze message to get the attachments, which can be other messages.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void analyzeAttachments() throws InterruptedException;

    /**
     * Detect embedded TNEF attachment and if exists process it to get the rtf content and attachments.
     *
     * @throws InterruptedException the interrupted exception
     */
    protected void detectTNEFAttachment() throws InterruptedException {
        String mimeType;

        if (attachments != null && !attachments.isEmpty()) {
            for (StoreAttachment a : attachments) {
                if ((a.attachmentType != StoreAttachment.STORE_ATTACHMENT)
                        && (a.attachmentContent instanceof byte[])
                        && ((a.mimeType.toLowerCase().equals("application/ms-tnef")
                        || (a.mimeType.toLowerCase().equals("application/vnd.ms-tnef"))))) {
                    try {
                        ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) a.attachmentContent);
                        HMEFMessage tnefPart = new HMEFMessage(bais);

                        String rtfBody = tnefPart.getBody();
                        List<Attachment> tnefAttachments = tnefPart.getAttachments();

                        attachments.remove(a);
                        if ((bodyContent[RTF_BODY] == null) || bodyContent[RTF_BODY].isEmpty())
                            bodyContent[RTF_BODY] = rtfBody;
                        else
                            logMessageWarning("mailextractlib: redondant rtf body extracted from winmail.dat droped", null);

                        for (Attachment tnefAttachment : tnefAttachments) {
                            StoreAttachment smAttachment = new StoreAttachment(this, tnefAttachment.getContents(),
                                    "file", tnefAttachment.getLongFilename(),
                                    null, tnefAttachment.getModifiedDate(),
                                    TikaExtractor.getInstance().getMimeType(tnefAttachment.getContents()),
                                    null, StoreAttachment.FILE_ATTACHMENT);
                            attachments.add(smAttachment);
                        }
                        break;
                    } catch (Exception e) {
                        logMessageWarning("mailextractlib: can't analyze winmail.dat content, it will be extracted as a file", e);
                    }
                }
            }
        }
    }

    /*
     * Global message
     */

    /**
     * Gets the native mime content, if any, or null.
     *
     * @return the native mime content
     * @throws InterruptedException the interrupted exception
     */
    protected abstract byte[] getNativeMimeContent() throws InterruptedException;

    /**
     * Analyze the appointment information if any in the message, or null.
     */
    protected abstract void analyzeAppointmentInformation();

    /**
     * Analyze message to collect metadata and content information (protocol
     * specific).
     *
     * <p>
     * This is the main method for sub classes, where all metadata and
     * information has to be extracted in standard representation out of the
     * inner representation of the message.
     * <p>
     * If needed a fake raw SMTP content (.eml) is generated with all the body
     * formats available but without the attachments, which are extracted too.
     *
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    public void analyzeMessage() throws MailExtractLibException, InterruptedException {
        // header metadata extraction
        // * special global
        analyzeSubject();
        if ((subject == null) || subject.isEmpty())
            subject = "[SubjectVide]";

        // header content extraction
        prepareAnalyze();

        // * messageID
        analyzeMessageID();
        if ((messageID == null) || messageID.isEmpty())
            messageID = "[MessageIDVide]";

        // * recipients and co
        analyzeFrom();
        analyzeRecipients();
        analyzeReplyTo();
        analyzeReturnPath();

        // * sent and received dates
        analyzeDates();

        // * immediate in-reply-to and references
        analyzeInReplyToId();
        analyzeReferences();

        // content extraction
        analyzeBodies();
        analyzeAttachments();
        detectTNEFAttachment();
        optimizeBodies();

        // try to get appointment information if any
        analyzeAppointmentInformation();

        // detect embedded store attachments not determine during parsing
        StoreAttachment.detectStoreAttachments(attachments);

        // no raw content, will be constructed at StoreMessage level
        mimeContent = getNativeMimeContent();
    }

    // get rid of useless beginning and ending spaces, carriage returns and
    // desencapsulate html and text from rtf
    private void optimizeBodies() {
        // get rid of useless beginning and ending spaces, carriage returns...
        if (bodyContent[TEXT_BODY] != null)
            bodyContent[TEXT_BODY] = bodyContent[TEXT_BODY].trim();
        if (bodyContent[HTML_BODY] != null)
            bodyContent[HTML_BODY] = bodyContent[HTML_BODY].trim();
        if (bodyContent[RTF_BODY] != null)
            bodyContent[RTF_BODY] = bodyContent[RTF_BODY].trim();

        try {
            // de-encapsulate TEXT and HTML from RTF if defined as encapsulated
            if (((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty())) {
                HTMLFromRTFExtractor htmlExtractor = new HTMLFromRTFExtractor(bodyContent[RTF_BODY]);
                if (htmlExtractor.isEncapsulatedTEXTinRTF()) {
                    String result;
                    result = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                    if ((result != null) && !result.isEmpty()) {
                        result = result.trim();
                        if ((bodyContent[TEXT_BODY] == null) || bodyContent[TEXT_BODY].isEmpty()) {
                            bodyContent[TEXT_BODY] = result;
                            bodyContent[RTF_BODY] = null;
                        } else {
                            if (bodyContent[TEXT_BODY].equals(result))
                                bodyContent[RTF_BODY] = null;
                        }
                    }
                } else if (htmlExtractor.isEncapsulatedHTMLinRTF()
                        && ((bodyContent[HTML_BODY] == null) || bodyContent[HTML_BODY].isEmpty())) {
                    String result = htmlExtractor.getDeEncapsulateHTMLFromRTF();
                    if ((result != null) && !result.isEmpty()) {
                        result = result.trim();
                        bodyContent[HTML_BODY] = result;
                        bodyContent[RTF_BODY] = null;
                    }
                }

            }
        } catch (MailExtractLibException e) {
            // forget bodies optimisation
        }
    }

    @Override
    public void processElement(boolean writeFlag) throws InterruptedException, MailExtractLibException {
        listLineId = storeFolder.getStoreExtractor().incGlobalListCounter(this.getClass());
        analyzeMessage();
        storeFolder.getDateRange().extendRange(sentDate);
        extractMessage(writeFlag);
        countMessage();
    }

    @Override
    public void listElement(boolean statsFlag) throws InterruptedException, MailExtractLibException {
        listLineId = storeFolder.getStoreExtractor().incGlobalListCounter(this.getClass());
        analyzeMessage();
        if (statsFlag)
            extractMessage(false);
        countMessage();
    }

    /**
     * Create the Archive Unit structures with all content and metadata needed,
     * and then write them on disk if writeFlag is true.
     * <p>
     * This is "the" method where the extraction structure and content is mainly
     * defined (see also {@link StoreFolder#extractFolder StoreFolder.extractFolder} and
     * {@link StoreExtractor#extractAllFolders StoreFolder.extractAllFolders}).
     *
     * @param writeFlag write or not flag (no write used for stats)
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    public final void extractMessage(boolean writeFlag) throws MailExtractLibException, InterruptedException {
        // String description = "[Vide]";
        String textContent = null;

        // create message unit
        if ((subject == null) || subject.trim().isEmpty())
            subject = "[Vide]";

        messageNode = new ArchiveUnit(storeFolder.storeExtractor, storeFolder.folderArchiveUnit, "Message", subject);

        // metadata in SEDA 2.0-ontology order
        messageNode.addMetadata("DescriptionLevel", "Item", true);
        messageNode.addMetadata("Title", subject, true);
        messageNode.addMetadata("OriginatingSystemId", messageID, false);

        // description = "Message extrait du compte " +
        // mailBoxFolder.storeExtractor.user;
        if (appointment != null) {
            messageNode.addMetadata("Description", "Rendez-vous", true);
        }
        messageNode.addPersonMetadata("Writer", from, false);
        messageNode.addPersonMetadataList("Addressee", recipientTo, false);
        messageNode.addPersonMetadataList("Recipient", recipientCc, false);
        messageNode.addPersonMetadataList("Recipient", recipientBcc, false);
        messageNode.addMetadata("SentDate", DateRange.getISODateString(sentDate), false);
        messageNode.addMetadata("ReceivedDate", DateRange.getISODateString(receivedDate), false);

        // put appointment information in metadata if any
        if (appointment != null) {
            if (appointment.identifier == null)
                appointment.identifier = "[IDVide]";
            if (appointment.location == null)
                appointment.location = "[LocalisationVide]";
            String bdString, edString;
            if (appointment.beginDate != null)
                bdString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.beginDate);
            else
                bdString = "[Date/HeureInconnues]";
            if (appointment.endDate != null)
                edString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.endDate);
            else
                edString = "[Date/HeureInconnues]";

            messageNode.addEventMetadata(appointment.identifier, "RDV Début", bdString,
                    "Localisation : " + appointment.location);
            messageNode.addEventMetadata(appointment.identifier, "RDV Fin", edString,
                    "Localisation : " + appointment.location);
        }

        // reply-to messageID
        if ((inReplyToUID != null) && !inReplyToUID.isEmpty())
            messageNode.addMetadata("OriginatingSystemIdReplyTo", inReplyToUID, false);

        // get textContent if TEXT_CONTENT not empty
        if ((bodyContent[TEXT_BODY] != null) && !bodyContent[TEXT_BODY].isEmpty())
            textContent = bodyContent[TEXT_BODY];

        // get text content from html if no textContent
        if ((textContent == null) && (bodyContent[HTML_BODY] != null))
            textContent = HTMLTextExtractor.getInstance().act(bodyContent[HTML_BODY]);

        // purify textContent and put in metadata
        if ((textContent != null) && (!textContent.trim().isEmpty())) {
            if (getStoreExtractor().options.extractMessageTextFile)
                messageNode.addObject(HTMLTextExtractor.getInstance().htmlStringtoString(textContent), messageID + ".txt",
                        "TextContent", 1);
            if (getStoreExtractor().options.extractMessageTextMetadata) {
                messageNode.addLongMetadata("TextContent", ArchiveUnit.purifyMetadataText(textContent), true);
            }
        }

        // extract all attachment and generate mimecontent of theese attachments

        // create all attachments subunits/object groups
        StoreAttachment.extractAttachments(attachments, messageNode, writeFlag);

        // generate mime fake if needed and associated mimeContent
        if (mimeContent == null) {
            mimeFake = getMimeFake();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mimeFake.writeTo(baos);
                mimeContent = baos.toByteArray();
            } catch (MessagingException | IOException e) {
                logMessageWarning("mailextractlib: can't extract raw content", e);
            }
        }
        if (mimeContent == null)
            mimeContent = "".getBytes();

        // add object binary master except if empty one
        messageNode.addObject(mimeContent, messageID + ".eml", "BinaryMaster", 1);

        if (writeFlag)
            messageNode.write();

        getStoreExtractor().incMessageCount();
        int logLevel;
        if (getStoreExtractor().isRoot()) {
            doProgressLogIfStep(getProgressLogger(), MailExtractProgressLogger.MESSAGE_GROUP, getStoreExtractor().getMessageCount(), "mailextractlib: " + getStoreExtractor().getMessageCount() + " extracted messages");
            logLevel=MailExtractProgressLogger.MESSAGE;
        } else
            logLevel=MailExtractProgressLogger.MESSAGE_DETAILS;

        doProgressLog(getProgressLogger(), logLevel, "mailextractlib: extracted "+getLogDescription()+
                        " with SentDate=" + (sentDate == null ? "Unknown sent date" : sentDate.toString()), null);

        // write in csv list if asked for
        writeToMailsList(writeFlag);
        if (Thread.interrupted())
            throw new InterruptedException("mailextractlib: interrupted");
    }

    /**
     * Gets mails global list name used for the csv file name construction.
     *
     * @return the global list name
     */
    static public String getGlobalListName() {
        return "mails";
    }

    /**
     * Print the header for mails list csv file
     *
     * @param ps the dedicated print stream
     */
    static public void printGlobalListCSVHeader(PrintStream ps) {
        ps.println("ID;SentDate;ReceivedDate;FromName;FromAddress;" +
                "ToList;Subject;MessageID;" +
                "AttachmentList;ReplyTo;Folder;Size;Attached;" +
                "AppointmentLocation;AppointmentBeginDate;AppointmentEndDate");
    }

    private void writeToMailsList(boolean writeFlag) throws InterruptedException {
        if (writeFlag && getStoreExtractor().options.extractObjectsLists && getStoreExtractor().canExtractObjectsLists()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            PrintStream ps = storeFolder.getStoreExtractor().getGlobalListPS(this.getClass());
            try {
                ps.format("\"%d\";", listLineId);
                ps.format("\"%s\";",
                        (sentDate == null ? "" : sdf.format(sentDate)));
                ps.format("\"%s\";",
                        (receivedDate == null ? "" : sdf.format(receivedDate)));
                if ((from != null) && !from.isEmpty()) {
                    MetadataPerson p = new MetadataPerson(from);
                    ps.format("\"%s\";\"%s\";", filterHyphen(p.fullName),
                            filterHyphen(p.identifier));
                } else
                    ps.print("\"\";\"\";");
                ps.format("\"%s\";",
                        filterHyphen(personStringListToIndentifierString(recipientTo)));
                ps.format("\"%s\";", filterHyphen(subject));
                ps.format("\"%s\";", filterHyphen(messageID));
                ps.format("\"%s\";", filterHyphen(attachmentsNamesList()));
                if ((replyTo == null) || replyTo.isEmpty())
                    ps.format("\"\";");
                else {
                    MetadataPerson p = new MetadataPerson(replyTo.get(0));
                    ps.format("\"%s\";", filterHyphen(p.identifier));
                }
                ps.format("\"%s\";", filterHyphen(storeFolder.getFullName()));
                ps.format("\"%d\";", this.getMessageSize());
                if (!storeFolder.getStoreExtractor().isRoot())
                    ps.format("\"Attached\"");
                if (appointment != null) {
                    String bdString, edString;
                    if (appointment.beginDate != null)
                        bdString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.beginDate);
                    else
                        bdString = "[Date/HeureInconnues]";
                    if (appointment.endDate != null)
                        edString = DateTimeFormatter.ISO_DATE_TIME.format(appointment.endDate);
                    else
                        edString = "[Date/HeureInconnues]";
                    ps.format(";\"%s\";\"%s\";\"%s\"",
                            filterHyphen(appointment.location), bdString, edString);
                } else {
                    ps.format(";;;");
                }
                ps.println();
                ps.flush();
            } catch (Exception e) {
                logMessageWarning("mailextractlib: can't write in mails csv list", e);
            }
        }
    }

    private String personStringListToIndentifierString(List<String> sList) {
        String result = "";
        MetadataPerson p;
        boolean first = true;

        if (sList != null) {
            for (String s : sList) {
                if (first)
                    first = false;
                else
                    result += ", ";
                p = new MetadataPerson(s);
                result += p.identifier;
            }
        }
        return result;
    }

    private String attachmentsNamesList() {
        String result = "";
        boolean first = true;

        if (attachments != null) {
            for (StoreAttachment a : attachments) {
                if (first)
                    first = false;
                else
                    result += ", ";
                result += a.getName();
            }
        }
        return result;
    }

    private String filterHyphen(String s) {
        return s.replace("\"", " ");
    }

    /**
     * Add this message in the folder accumulators for number of messages and
     * total raw size of messages.
     *
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major                             format problems...)
     * @throws InterruptedException    the interrupted exception
     */
    public void countMessage() throws MailExtractLibException, InterruptedException {
        // accumulate in folder statistics
        storeFolder.incFolderElementsCount();
        storeFolder.addFolderElementsRawSize(getMessageSize());
    }

    /**
     * Gets the mime fake.
     *
     * @return the mime fake
     * @throws InterruptedException the interrupted exception
     */
    public MimeMessage getMimeFake() throws InterruptedException {
        MimeMessage mime = new NoUpdateMimeMessage(Session.getDefaultInstance(new Properties()));
        try {
            buildMimeHeader(mime);
            buildMimePart(mime);
            mime.saveChanges();
        } catch (MessagingException | MailExtractLibException e) {
            logMessageWarning("mailextractlib: unable to generate mime fake", e);
            mime = null;
        }
        return mime;
    }

    private static void setAddressList(MimeMessage mime, String tag, List<String> addressList)
            throws MessagingException, UnsupportedEncodingException {
        if ((addressList != null) && (!addressList.isEmpty())) {
            String value = "";
            int countline = 0;
            for (String tmp : addressList) {
                // 80 characters lines
                tmp = MimeUtility.encodeText(tmp, "UTF-8", "Q");
                if (countline + tmp.length() > 80) {
                    value += "\n\t";
                    countline = 1;
                } else
                    countline += tmp.length();
                value += MimeUtility.encodeText(tmp, "UTF-8", "Q") + ",";
            }
            value = value.substring(0, value.length() - 1);
            mime.setHeader(tag, value);
        }
    }

    private void buildMimeHeader(MimeMessage mime) throws MailExtractLibException {
        try {
            // put all know headers, they will be change by the specific ones
            if ((mailHeader != null) && (mailHeader.size() > 0)) {
                String tag, value;
                for (String tmp : mailHeader) {
                    if (tmp.indexOf(':') < 0)
                        continue;
                    tag = tmp.substring(0, tmp.indexOf(':'));
                    value = tmp.substring(tmp.indexOf(':') + 1);
                    mime.setHeader(tag, value);
                }
            }

            // Return-Path
            if (returnPath != null)
                mime.setHeader("Return-Path", MimeUtility.encodeText(returnPath, "UTF-8", "Q"));
            // From
            if (from != null)
                mime.setHeader("From", MimeUtility.encodeText(from, "UTF-8", "Q"));
            // To
            if (recipientTo != null)
                setAddressList(mime, "To", recipientTo);
            // cc
            if (recipientCc != null)
                setAddressList(mime, "cc", recipientCc);
            // bcc
            if (recipientBcc != null)
                setAddressList(mime, "bcc", recipientBcc);
            // Reply-To
            if (replyTo != null)
                setAddressList(mime, "Reply-To", replyTo);
            // Date, if null Date is deleted
            mime.setSentDate(sentDate);
            // Subject
            if (subject != null)
                mime.setSubject(MimeUtility.encodeText(subject, "UTF-8", "Q"));
            // Message-ID
            if (messageID != null)
                mime.setHeader("Message-ID", MimeUtility.encodeText(messageID, "UTF-8", "Q"));
            // In-Reply-To
            if ((inReplyToUID != null) && (!inReplyToUID.isEmpty()))
                mime.setHeader("In-Reply-To", MimeUtility.encodeText(inReplyToUID, "UTF-8", "Q"));

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailExtractLibException("Unable to generate mime header of message " + subject, e);
        }
    }

    private void addAttachmentPart(MimeMultipart root, boolean isInline) throws MailExtractLibException {
        try {
            // build attach part
            for (StoreAttachment a : attachments) {
                boolean thisIsInline = (a.attachmentType == StoreAttachment.INLINE_ATTACHMENT);

                if ((thisIsInline && isInline) || ((!thisIsInline) && (!isInline))) {
                    MimeBodyPart attachPart = new MimeBodyPart();

                    // set Content-ID
                    String cidName;
                    if ((a.contentID != null) && !a.contentID.trim().isEmpty()) {
                        attachPart.setContentID("<" + a.contentID.trim() + ">");
                        if (a.contentID.indexOf('@') < 0)
                            cidName = a.contentID;
                        else
                            cidName = a.contentID.substring(0, a.contentID.indexOf('@'));
                    } else
                        cidName = "unknown";

                    // set object and Content-Type
                    String attachmentFilename = encodedFilename(a.name, a.mimeType, cidName);
                    if ((a.mimeType == null) || (a.mimeType.isEmpty()))
                        attachPart.setContent(a.getRawAttachmentContent(),
                                "application/octet-stream; name=\"" + attachmentFilename + "\"");
                    else {
                        if (a.mimeType.startsWith("text")) {
                            String s;
                            s = new String(a.getRawAttachmentContent(), "UTF-8");
                            attachPart.setContent(s, a.mimeType + "; name=\"" + attachmentFilename + "\"");
                        } else if (a.mimeType.startsWith("message")) {
                            // bypass datahandler as the rfc822 form is provided
                            RawDataSource rds = new RawDataSource(a.getRawAttachmentContent(), a.mimeType,
                                    attachmentFilename);
                            DataHandler dh = new DataHandler(rds);
                            attachPart.setDataHandler(dh);
                        } else if (a.mimeType.startsWith("multipart")) {
                            // wrong attachment type corrected to neutral application/octet-stream
                            attachPart.setContent(a.getRawAttachmentContent(),
                                    "application/octet-stream; name=\"" + attachmentFilename + "\"");
                        } else {
                            attachPart.setContent(a.getRawAttachmentContent(),
                                    a.mimeType + "; name=\"" + attachmentFilename + "\"");
                        }
                    }
                    // set Content-Disposition
                    if (a.attachmentType == StoreAttachment.INLINE_ATTACHMENT)
                        attachPart.setDisposition("inline; filename=\"" + attachmentFilename + "\"");
                    else
                        attachPart.setDisposition("attachment; filename=\"" + attachmentFilename + "\"");
                    root.addBodyPart(attachPart);
                }
            }
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailExtractLibException(
                    "Unable to generate " + (isInline ? "inlines" : "attachments") + " of message " + subject, e);
        }

    }

    private MimeMultipart newChild(MimeMultipart parent, String type) throws MessagingException {
        MimeMultipart child = new MimeMultipart(type);
        final MimeBodyPart mbp = new MimeBodyPart();
        parent.addBodyPart(mbp);
        mbp.setContent(child);
        return child;
    }

    // some extraction has no body only headers
    private boolean isEmptyBodies() {
        if ((bodyContent[TEXT_BODY] != null) && !bodyContent[TEXT_BODY].isEmpty())
            return false;
        if ((bodyContent[HTML_BODY] != null) && !bodyContent[HTML_BODY].isEmpty())
            return false;
        if ((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty())
            return false;
        // if (attachments.size() > 0)
        // return false;
        return true;
    }

    private void buildMimePart(MimeMessage mime) throws MailExtractLibException {
        boolean hasInline = false;
        int relatedPart = OUT_OF_BODY;

        MimeMultipart rootMp = new MimeMultipart("mixed");
        {
            try {
                // search if there are inlines
                for (StoreAttachment a : attachments) {
                    if (a.attachmentType == StoreAttachment.INLINE_ATTACHMENT) {
                        hasInline = true;
                        break;
                    }
                }

                // determine in which part to add related
                if ((bodyContent[HTML_BODY] != null) && !bodyContent[HTML_BODY].isEmpty())
                    relatedPart = HTML_BODY;
                else if ((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty())
                    relatedPart = RTF_BODY;

                // build message part
                MimeMultipart msgMp = newChild(rootMp, "alternative");
                {
                    if ((bodyContent[TEXT_BODY] != null) && !bodyContent[TEXT_BODY].isEmpty()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent(bodyContent[TEXT_BODY], "text/plain; charset=utf-8");
                        msgMp.addBodyPart(part);
                    }
                    // if empty message, construct a fake empty text part
                    if (isEmptyBodies()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent(" ", "text/plain; charset=utf-8");
                        msgMp.addBodyPart(part);
                    }

                    if ((bodyContent[HTML_BODY] != null) && !bodyContent[HTML_BODY].isEmpty()) {
                        MimeMultipart upperpart;
                        if (hasInline && (relatedPart == HTML_BODY)) {
                            upperpart = newChild(msgMp, "related");
                        } else
                            upperpart = msgMp;

                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent(bodyContent[HTML_BODY], "text/html; charset=utf-8");
                        upperpart.addBodyPart(part);

                        if (hasInline && (relatedPart == HTML_BODY))
                            addAttachmentPart(upperpart, true);
                    }
                    if ((bodyContent[RTF_BODY] != null) && !bodyContent[RTF_BODY].isEmpty()) {
                        MimeMultipart upperpart;
                        if (hasInline && (relatedPart == RTF_BODY)) {
                            upperpart = newChild(msgMp, "related");
                        } else
                            upperpart = msgMp;

                        MimeBodyPart part = new MimeBodyPart();
                        part.setContent(bodyContent[RTF_BODY], "text/rtf; charset=US-ASCII");// ;
                        // charset=utf-8");
                        upperpart.addBodyPart(part);

                        if (hasInline && (relatedPart == RTF_BODY))
                            addAttachmentPart(upperpart, true);

                    }
                }
            } catch (MessagingException e) {
                throw new MailExtractLibException("Unable to generate mime body part of message " + subject, e);
            }

            // add inline part of attachments if not added to HTML body
            if (relatedPart == OUT_OF_BODY)
                addAttachmentPart(rootMp, true);
            addAttachmentPart(rootMp, false);

            try {
                mime.setContent(rootMp);
            } catch (MessagingException e) {
                throw new MailExtractLibException("Unable to generate mime fake of message " + subject, e);
            }
        }
    }

    private static boolean isPureAscii(String v) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(v);
    }


    private String encodedFilename(String filename, String mimetype, String ifnone) {
        String tmp;
        if ((filename != null) && !filename.trim().isEmpty())
            tmp = filename;
        else
            tmp = ifnone;
        if ("message/rfc822".equals(mimetype) && (!tmp.endsWith(".eml")))
            tmp += ".eml";

        // prevent a bug when quotes are in ascii filename (encodeWord is then not encoding)
        if (isPureAscii(tmp) && tmp.contains("\""))
            tmp = tmp.replaceAll("\"", "'");

        try {
            return MimeUtility.encodeWord(tmp, "UTF-8", "Q");
        } catch (UnsupportedEncodingException e) {
            // forget it
        }
        return "Unknown";
    }

    /**
     * Prevent update of Message-ID and of Date header with now date
     */
    private class NoUpdateMimeMessage extends MimeMessage {

        /**
         * Instantiates a new No update mime message.
         *
         * @param session the session
         */
        public NoUpdateMimeMessage(Session session) {
            super(session);
        }

        @Override
        protected void updateMessageID() throws MessagingException {
            String[] ids = getHeader("Message-ID");
            if (ids == null || ids.length == 0 || ids[0] == null || ids[0].isEmpty()) {
                super.updateMessageID();
            }
        }

        @Override
        protected synchronized void updateHeaders() throws MessagingException {
            String[] date = getHeader("Date");
            super.updateHeaders();
            if (date == null)
                removeHeader("Date");
        }
    }
}
