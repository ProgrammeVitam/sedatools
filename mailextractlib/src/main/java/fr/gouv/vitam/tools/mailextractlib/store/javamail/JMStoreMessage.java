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

package fr.gouv.vitam.tools.mailextractlib.store.javamail;

import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.mailextractlib.utils.RFC822Headers;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.eclipse.angus.mail.util.QPDecoderStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;

/**
 * StoreMessage sub-class for mail boxes extracted through JavaMail library.
 * <p>
 * For now, IMAP and Thunderbird mbox, eml structure through MailExtract application,
 * could also be used for POP3 and Gmail, via StoreExtractor (not tested).
 */
public class JMStoreMessage extends StoreMessage {

    /**
     * Native JavaMail message.
     */
    protected MimeMessage message;

    // format to parse dates in Receive header
    static private MailDateFormat mailDateFormat = new MailDateFormat();

    /**
     * Instantiates a new JM mail box message.
     *
     * @param mBFolder Containing MailBoxFolder
     * @param message  Native JavaMail message
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major
     *                             format problems...)
     */
    public JMStoreMessage(StoreFolder mBFolder, MimeMessage message) throws MailExtractLibException {
        super(mBFolder);
        this.message = message;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#getMessageSize()
     */
    @Override
    public long getMessageSize() throws InterruptedException {
        // geMessageSize of JavaMail is quite approximative...
        long result;

        if (mimeContent != null)
            result = mimeContent.length;
        else {
            mimeContent = getNativeMimeContent();
            result = mimeContent.length;
        }
        return result;
    }

    // utilities

    // simple InternetAddress to metadata String
    static private String getElementalStringAddress(InternetAddress address) {
        String result = "";
        String s;

        if (address != null) {
            s = address.getPersonal();
            if (s != null)
                result = s + " ";
            s = address.getAddress();
            if (s != null)
                result += "<" + s + ">";
        }
        return result;
    }

    // any (simple or group) InternetAddress to metadata String
    static private String getStringAddress(InternetAddress address) {
        String result = "";

        if (address != null) {
            result = getElementalStringAddress(address);
            // special case of group address (RFC 2822)
            if (address.isGroup()) {
                try {
                    InternetAddress[] group = address.getGroup(false);
                    result += ":";
                    for (int k = 0; k < group.length; k++) {
                        if (k > 0)
                            result += ",";
                        result += getElementalStringAddress(group[k]);
                    }
                } catch (AddressException e) {
                    // too bad
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#prepareHeaders()
     */
    protected void prepareAnalyze() throws InterruptedException {
        List<String> result = null;
        String line, value;
        Header header;

        try {
            Enumeration<Header> headers = message.getAllHeaders();
            if ((headers != null) && headers.hasMoreElements()) {
                result = new ArrayList<String>();
                while (headers.hasMoreElements()) {
                    header = headers.nextElement();
                    line = header.getName() + ": ";
                    try {
                        value = MimeUtility.decodeText(header.getValue());
                    } catch (UnsupportedEncodingException e) {
                        value = header.getValue(); // use raw value
                    }
                    // value = innerTrim(value);
                    line += value;
                    result.add(line);
                }
            }
        } catch (MessagingException e) {
            logMessageWarning("mailextractlib.javamail: can't extract complete mail header", e);
        }
        mailHeader = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeSubject()
     */
    protected void analyzeSubject() throws InterruptedException {
        String result = null;

        try {
            result = message.getSubject();
        } catch (MessagingException e) {
            doProgressLog(getProgressLogger(), MailExtractProgressLogger.MESSAGE_DETAILS, "mailextractlib.javamail: can't get message subject", e);
        }
        subject = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeMessageID()
     */
    protected void analyzeMessageID() throws InterruptedException {
        String result = null;

        try {
            result = message.getMessageID();
        } catch (MessagingException e) {
            logMessageWarning("mailextractlib.javamail: can't extract message ID", e);
        }
        messageID = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeFrom()
     */
    protected void analyzeFrom() throws InterruptedException {
        String result = null;
        List<String> aList = getAddressHeader("From");

        if ((aList == null) || (aList.size() == 0)) {
            logMessageWarning("mailextractlib.javamail: no From address in header", null);
        } else {
            if (aList.size() > 1)
                logMessageWarning("mailextractlib.javamail: multiple From addresses, keep the first one in header", null);
            result = aList.get(0);
        }
        from = result;
    }

    // get addresses in header with parsing control relaxed
    private List<String> getAddressHeader(String name) throws InterruptedException {
        List<String> result = null;
        String addressHeaderString = null;

        try {
            addressHeaderString = message.getHeader(name, ", ");
        } catch (MessagingException me) {
            logMessageWarning("mailextractlib.javamail: can't access to [" + name + "] address header", me);
        }

        if (addressHeaderString != null) {
            result = new ArrayList<String>();
            InternetAddress[] iAddressArray;
            try {
                iAddressArray = InternetAddress.parseHeader(addressHeaderString, false);
            } catch (AddressException e) {
                try {
                    // try at least to Mime decode
                    addressHeaderString = MimeUtility.decodeText(addressHeaderString);
                } catch (UnsupportedEncodingException ignored) {
                }
                logMessageWarning("mailextractlib.javamail: wrongly formatted address " + addressHeaderString
                        + ", keep raw address list in metadata in header " + name, e);
                result.add(addressHeaderString);
                return result;
            }
            for (InternetAddress ia : iAddressArray) {
                result.add(getStringAddress(ia));
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeRecipients()
     */
    protected void analyzeRecipients() throws InterruptedException {
        recipientTo = getAddressHeader("To");
        recipientCc = getAddressHeader("Cc");
        recipientBcc = getAddressHeader("Bcc");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeReplyTo()
     */
    protected void analyzeReplyTo() throws InterruptedException {
        replyTo = getAddressHeader("Reply-To");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeReturnPath()
     */
    protected void analyzeReturnPath() throws InterruptedException {
        String result = null;
        List<String> aList = getAddressHeader("Return-Path");

        if (!((aList == null) || (aList.size() == 0))) {
            if (aList.size() > 1)
                logMessageWarning("mailextractlib.javamail: multiple Return-Path, keep the first one addresses in header", null);
            result = aList.get(0);
        }

        returnPath = result;
    }

    // Received date, either in a specific header field either determined from
    // information about smtp relaying (Recieved header field in smtp).
    private Date getReceivedDate() throws MessagingException {
        Date result;

        result = message.getReceivedDate();
        if (result == null) {
            String receivedHeader = message.getHeader("Received", ",");
            if (receivedHeader != null) {
                int i = receivedHeader.indexOf(';');
                if (i != -1) // supposed to always be
                {
                    receivedHeader = receivedHeader.substring(i + 1);
                    try {
                        result = mailDateFormat.parse(receivedHeader);
                    } catch (ParseException e) {
                        // too bad no date
                    }
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeDates()
     */
    protected void analyzeDates() throws InterruptedException {
        try {
            sentDate = message.getSentDate();
            receivedDate = getReceivedDate();
        } catch (MessagingException e) {
            logMessageWarning("mailextractlib.javamail: can't extract dates", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeInReplyToId(
     * )
     */
    protected void analyzeInReplyToId() throws InterruptedException {
        String result = null;
        try {
            String[] irtList = message.getHeader("In-Reply-To");

            if (irtList != null) {
                if (irtList.length > 1)
                    logMessageWarning(
                            "mailextractlib.javamail: multiple In-Reply-To identifiers, keep the first one in header", null);
                result = RFC822Headers.getHeaderValue(irtList[0]);
            }
        } catch (MessagingException me) {
            logMessageWarning("mailextractlib.javamail: can't access to In-Reply-To header", null);
        }

        inReplyToUID = result;
    }

    // utility function to get the value part of an header string
    private static String getHeaderValue(String line) {
        int i = line.indexOf(':');
        if (i < 0)
            return line;
        // skip whitespace after ':'
        int j;
        for (j = i + 1; j < line.length(); j++) {
            char c = line.charAt(j);
            if (!(c == ' ' || c == '\t' || c == '\r' || c == '\n'))
                break;
        }
        return line.substring(j);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeReferences()
     */
    protected void analyzeReferences() throws InterruptedException {
        List<String> result = null;
        try {
            String refHeader = message.getHeader("References", " ");

            if (refHeader != null) {
                result = new ArrayList<String>();
                String[] refList = getHeaderValue(refHeader).split(" ");
                for (String tmp : refList)
                    try {
                        result.add(MimeUtility.decodeText(tmp));
                    } catch (UnsupportedEncodingException ignored) {
                    }
            }
        } catch (MessagingException me) {
            logMessageWarning("mailextractlib.javamail: can't access to In-Reply-To header", me);
        }

        references = result;
    }

    /*
     * Content analysis methods to be implemented for each StoreMessage
     * implementation
     */

    // append to bodyContent after creating it if needed
    private void appendBodyContent(int type, String s) {
        if (s != null) {
            if (bodyContent[type] == null)
                bodyContent[type] = s;
            else {
                bodyContent[type] += "/n" + s;
            }
        }
    }

    private static String getInputStreamContent(InputStream is) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (IOException e) {
            return null;
        }
    }

    // recursively search in MimeParts of the message de body contents in
    // different versions
    private void getPartBodyContents(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            if (p.isMimeType("text/plain")
                    && ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition()))) {
                if (p.getContent() instanceof InputStream)
                    appendBodyContent(TEXT_BODY, getInputStreamContent((InputStream) p.getContent()));
                else if (p.getContent() instanceof String)
                    appendBodyContent(TEXT_BODY, (String) p.getContent());
            } else if (p.isMimeType("text/html")
                    && ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition()))) {
                if (p.getContent() instanceof InputStream)
                    appendBodyContent(HTML_BODY, getInputStreamContent((InputStream) p.getContent()));
                else if (p.getContent() instanceof String)
                    appendBodyContent(HTML_BODY, (String) p.getContent());
            } else if (p.isMimeType("text/rtf")
                    && ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition()))) {
                if (p.getContent() instanceof InputStream)
                    appendBodyContent(RTF_BODY, getInputStreamContent((InputStream) p.getContent()));
                else if (p.getContent() instanceof String)
                    appendBodyContent(RTF_BODY, (String) p.getContent());
            }
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                getPartBodyContents(mp.getBodyPart(i));
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeBodies()
     */
    protected void analyzeBodies() throws InterruptedException {
        try {
            getPartBodyContents(message);
        } catch (Exception e) {
            logMessageWarning("mailextractlib.javamail: badly formatted mime message, can't extract body contents", e);
        }
    }

    // recursively search in MimeParts all attachments
    private void getAttachments(List<StoreAttachment> lStoreMessageAttachment, BodyPart p)
            throws MessagingException, IOException, InterruptedException {

        if ((p.isMimeType("text/plain") || p.isMimeType("text/html") || p.isMimeType("text/rtf"))
                && ((p.getDisposition() == null) || Part.INLINE.equalsIgnoreCase(p.getDisposition())))
            // test if it's a bodyContent then not an attachment
        {
        }
        else if (!p.isMimeType("multipart/*")) {
            // any other non multipart is an attachment

            try {
                addAttachment(lStoreMessageAttachment, p);
            } catch (IOException | MessagingException | ParseException e) {
                logMessageWarning("mailextractlib.javamail: can't extract a badly formatted attachement", e);
            }

        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                getAttachments(lStoreMessageAttachment, mp.getBodyPart(i));
            }
        }
    }

    // rawcontent of a part
    private byte[] getPartRawContent(BodyPart bp) throws IOException, MessagingException {
        InputStream is = bp.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buf)) != -1) {
            baos.write(buf, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    // rawcontent of a part, replacing LF by CRLF in quoted-printable encoded parts (used for windows TNEF fixing)
    private byte[] getPartLFFixedRawContent(BodyPart bp) throws IOException, MessagingException, InterruptedException {
        InputStream is = bp.getInputStream();
        if (is instanceof QPDecoderStream) {
            DataHandler dh = bp.getDataHandler();
            bp.setDataHandler(null);
            is = new LFFixingQPDecoderStream(bp.getInputStream());
            bp.setDataHandler(dh);
            logMessageWarning("mailextractlib.javamail: using LFFixing quoted-printable decoding", null);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buf)) != -1) {
            baos.write(buf, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    // replace illegal characters in a filename with "_"
    // illegal characters : \ / * ? | < >
    private static String sanitizeFilename(String name) {
        return name.replaceAll("[:\\\\/*?|<>]", "_");
    }

    // add one attachment
    private void addAttachment(List<StoreAttachment> lStoreMessageAttachment, BodyPart bodyPart)
            throws IOException, MessagingException, ParseException, InterruptedException {
        String[] headers;
        ContentDisposition disposition;
        ContentType contenttype;
        String date;

        // all attachment definition vars
        String aName = null;
        Date aCreationDate = null;
        Date aModificationDate = null;
        String aMimeType;
        String aContentID = null;
        int aType;

        // by default is an attachment
        aType = StoreAttachment.FILE_ATTACHMENT;

        // get all we can from disposition
        headers = bodyPart.getHeader("Content-Disposition");
        if ((headers != null) && (headers.length > 0)) {
            disposition = new ContentDisposition(headers[0]);
            if (Part.INLINE.equalsIgnoreCase(disposition.getDisposition()))
                aType = StoreAttachment.INLINE_ATTACHMENT;
            date = disposition.getParameter("creation-date");
            if ((date != null) && (!date.isEmpty()))
                aCreationDate = mailDateFormat.parse(date);
            date = disposition.getParameter("modification-date");
            if ((date != null) && (!date.isEmpty()))
                aModificationDate = mailDateFormat.parse(date);
            aName = disposition.getParameter("filename");
        }

        // get all we can from content-type if any
        headers = bodyPart.getHeader("Content-Type");
        if ((headers != null) && (headers.length > 0)) {
            // some kind of mimeType normalization
            try {
                contenttype = new ContentType(headers[0]);
                if (contenttype.getSubType().equalsIgnoreCase("RFC822"))
                    aType = StoreAttachment.STORE_ATTACHMENT;
                aMimeType = contenttype.getBaseType();
                if (aName == null)
                    aName = contenttype.getParameter("name");
            } catch (Exception e) {
                aMimeType = headers[0];
                if (aMimeType.indexOf(';') != -1)
                    aMimeType = aMimeType.substring(0, aMimeType.indexOf(';'));
                int j = aMimeType.lastIndexOf('/');
                if ((j != -1) && (j < aMimeType.length()))
                    aMimeType = "application/" + aMimeType.substring(j + 1);
                else
                    aMimeType = "application/octet-stream";
            }
        } else {
            // if no mimetype force to general case
            aMimeType = "application/octet-stream";
        }

        // get contentId for inline attachment
        headers = bodyPart.getHeader("Content-ID");
        if ((headers != null) && (headers.length != 0))
            aContentID = headers[0];

        // define a filename if not defined in headers, encode and sanitize it
        if (aName == null)
            aName = "noname";
        else
            try {
                aName = MimeUtility.decodeText(aName);
            } catch (UnsupportedEncodingException ignored) {
            }
        aName = sanitizeFilename(aName);

        if (aType == StoreAttachment.STORE_ATTACHMENT)
            lStoreMessageAttachment.add(new StoreAttachment(this,getPartRawContent(bodyPart), "eml",
                    MimeUtility.decodeText(aName), aCreationDate, aModificationDate, aMimeType, aContentID, aType));
        else {
            if (aMimeType.toLowerCase().equals("application/ms-tnef")
                    || aMimeType.toLowerCase().equals("application/vnd.ms-tnef"))
                lStoreMessageAttachment.add(new StoreAttachment(this,getPartLFFixedRawContent(bodyPart), "file",
                        MimeUtility.decodeText(aName), aCreationDate, aModificationDate, aMimeType, aContentID, aType));
            else
                lStoreMessageAttachment.add(new StoreAttachment(this,getPartRawContent(bodyPart), "file",
                        MimeUtility.decodeText(aName), aCreationDate, aModificationDate, aMimeType, aContentID, aType));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeAttachments(
     * )
     */
    protected void analyzeAttachments() throws InterruptedException {
        List<StoreAttachment> result = new ArrayList<StoreAttachment>();

        try {
            Object contentObject = message.getContent();

            if (contentObject instanceof Multipart) {

                Multipart multipart = (Multipart) contentObject;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    getAttachments(result, bodyPart);

                    // filename = bodyPart.getFileName();
                    // // skip not attachment part
                    // if
                    // (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
                    // && (filename == null)) {
                    // continue; // dealing with attachments only
                    // }
                    //
                    // addAttachment(result, bodyPart);
                }
            }
        } catch (Exception e) {
            logMessageWarning("mailextractlib.javamail: badly formatted mime message, can't extract all attachments", e);
        }

        if (result.size() == 0)
            result = null;

        attachments = result;
    }

    /*
     * Global message
     */

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#
     * getNativeMimeContent()
     */
    protected byte[] getNativeMimeContent() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            message.writeTo(baos);
        } catch (Exception e) {
            logMessageWarning("mailextractlib.javamail: can't extract raw mime content", e);
        }

        return baos.toByteArray();
    }
}
