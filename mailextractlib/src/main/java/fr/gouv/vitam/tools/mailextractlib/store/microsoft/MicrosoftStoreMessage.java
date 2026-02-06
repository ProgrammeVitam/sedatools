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
package fr.gouv.vitam.tools.mailextractlib.store.microsoft;

import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.core.StoreMessage;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.mailextractlib.utils.RFC822Headers;

import jakarta.mail.MessagingException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor.ISO_8601;
import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.mailextractlib.utils.RFC822Headers.decodeRfc2047Flexible;

/**
 * StoreMessage sub-class for Microsoft message format, abstraction for pst and msg messages.
 */
public abstract class MicrosoftStoreMessage extends StoreMessage implements MicrosoftStoreElement {

    /**
     * The RFC822 headers if any.
     */
    protected RFC822Headers rfc822Headers;

    /**
     * The attachments list.
     */
    protected MicrosoftStoreMessageAttachment[] nativeAttachments;

    /**
     * Instantiates a new LP store message.
     *
     * @param mBFolder Containing MailBoxFolder
     */
    public MicrosoftStoreMessage(StoreFolder mBFolder) {
        super(mBFolder);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxMessage#getMessageSize()
     */
    @Override
    public long getMessageSize() {
        long result;
        result = getNativeMessageSize();
        if ((result == 0) && (mimeContent != null))
            result = mimeContent.length;
        return result;
    }

    // Native message fields access functions

    abstract protected String getNativeSmtpTransportHeader() throws InterruptedException;

    abstract protected String getNativeSubject() throws InterruptedException;

    abstract protected String getNativeInternetMessageId() throws InterruptedException;

    abstract protected String getNativeSenderName() throws InterruptedException;

    abstract protected String getNativeSentRepresentingName() throws InterruptedException;

    abstract protected String getNativeSenderAddrType() throws InterruptedException;

    abstract protected String getNativeSenderEmailAddress() throws InterruptedException;

    abstract protected String getNativeSentRepresentingAddrType() throws InterruptedException;

    abstract protected String getNativeSentRepresentingEmailAddress() throws InterruptedException;

    abstract protected String getNativeReturnPath() throws InterruptedException;

    abstract protected Date getNativeMessageDeliveryTime();

    abstract protected Date getNativeClientSubmitTime();

    abstract protected String getNativeInReplyToId() throws InterruptedException;

    abstract protected long getNativeMessageSize();

    // Native message ConversationIndex access functions
    abstract protected boolean hasNativeConversationIndex();

    abstract protected Date getNativeCIDeliveryTime();

    abstract protected UUID getNativeCIGuid();

    abstract protected int getNativeCINumberOfResponseLevels();

    abstract protected short getNativeCIResponseLevelDeltaCode(int responseLevelNumber);

    abstract protected long getNativeCIResponseLevelTimeDelta(int responseLevelNumber);

    abstract protected short getNativeCIResponseLevelRandom(int responseLevelNumber);

    // Native message Recipients access functions
    abstract protected int getNativeNumberOfRecipients();

    abstract protected String getNativeRecipientsSmtpAddress(int recipientNumber);

    abstract protected String getNativeRecipientsEmailAddress(int recipientNumber);

    abstract protected String getNativeRecipientsDisplayName(int recipientNumber);

    abstract protected int getNativeRecipientsType(int recipientNumber);

    // Native message body access functions
    abstract protected String getNativeBodyText();

    abstract protected String getNativeBodyHTML();

    abstract protected String getNativeRTFBody();

    // Native message attachment access functions
    abstract protected MicrosoftStoreMessageAttachment[] getNativeAttachments();

    // General Headers function

    // test if there's a convenient analyzed rfc822Headers
    private boolean hasRFC822Headers() {
        return (rfc822Headers != null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#prepareHeaders()
     */
    // get the smtp transport header if any
    protected void prepareAnalyze() throws InterruptedException {
        String headerString;

        if (!hasRFC822Headers()) {
            headerString = getNativeSmtpTransportHeader();
            if ((headerString != null) && (!headerString.isEmpty()))
                try {
                    rfc822Headers = new RFC822Headers(headerString, this);
                    mailHeader = Collections.list(rfc822Headers.getAllHeaderLines());
                } catch (MessagingException e) {
                    logMessageWarning("mailextractlib.microsoft: can't decode smtp header", e);
                    rfc822Headers = null;
                    mailHeader = null;
                }
        }
        nativeAttachments = getNativeAttachments();
    }

    // Subject specific functions

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeSubject()
     */
    protected void analyzeSubject() throws InterruptedException {
        String result = null;

        if (hasRFC822Headers()) {
            // smtp header value
            String[] sList = rfc822Headers.getHeader("Subject");
            if (sList != null) {
                if (sList.length > 1)
                    logMessageWarning("mailextractlib.microsoft: multiple subjects, keep the first one in header", null);
                result = RFC822Headers.getHeaderValue(sList[0]);
                if ((result != null) && (result.contains("=?")))
                    result = decodeRfc2047Flexible(result);
            }
        } else {
            // pst file value
            result = getNativeSubject();

            // FIXME filtering in LibPST (aim 0x0101, 0105 and 0110 values)
            // to be verified why and if usefull in msg
            if ((result.length() >= 2) && result.charAt(0) == 0x01) {
                if (result.length() == 2) {
                    result = "";
                } else {
                    result = result.substring(2);
                }
            }
            if (result.isEmpty())
                result = null;
        }
        if (result == null)
            doProgressLog(getProgressLogger(), MailExtractProgressLogger.MESSAGE_DETAILS,
                    "mailextractlib.microsoft: no subject in header", null);

        subject = result;
    }

    // MessageID specific functions

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeMessageID()
     */
    protected void analyzeMessageID() throws InterruptedException {
        String result = null;

        if (hasRFC822Headers()) {
            // smtp header value
            String[] mList = rfc822Headers.getHeader("message-ID");
            if (mList != null) {
                if (mList.length > 1)
                    logMessageWarning("mailextractlib.microsoft: multiple message ID, keep the first one in header", null);
                result = RFC822Headers.getHeaderValue(mList[0]);
            }
        } else {
            // pst file value
            // generate a messageID from the conversationIndex
            try {
                result = getNativeInternetMessageId();
                if (result.isEmpty()) {
                    if (hasNativeConversationIndex()) {
                        Instant inst = getNativeCIDeliveryTime().toInstant();
                        ZonedDateTime zdt = ZonedDateTime.ofInstant(inst, ZoneOffset.UTC);
                        result = "<MIC:" + getNativeCIGuid() + "@" + zdt.format(ISO_8601);
                        int responseLevelNumber = getNativeCINumberOfResponseLevels();
                        for (int i = 0; i < responseLevelNumber; i += 1) {
                            result += "+" + Integer.toHexString(getNativeCIResponseLevelDeltaCode(i));
                            result += Long.toHexString(getNativeCIResponseLevelTimeDelta(i));
                            result += Integer.toHexString(getNativeCIResponseLevelRandom(i));
                        }
                        result += ">";
                    }
                }
            }
            // FIXME AN pst to test
            catch (Exception e) {
                logMessageWarning("mailextractlib.microsoft: error during Message ID extraction", e);
                result = "NoMessageID";
            }
        }
        if (result == null) {
            logMessageWarning("mailextractlib.microsoft: no Message ID address in header", null);
            result = "NoMessageID";
        }
        messageID = result;
    }

    // From specific functions

    // get sender name using all possible sources
    private String getSenderName() throws InterruptedException {
        String result;

        result = getNativeSenderName();
        if ((result == null) || result.isEmpty())
            result = getNativeSentRepresentingName();

        if (result.isEmpty())
            result = null;
        return result;
    }

    // get sender email address using all possible sources (sender and
    // SentRepresenting field), and using SMTP first
    private String getSenderEmailAddress() throws InterruptedException {
        String result = "";

        if (getNativeSenderAddrType().equalsIgnoreCase("SMTP"))
            result = getNativeSenderEmailAddress();
        if (result.isEmpty() && getNativeSentRepresentingAddrType().equalsIgnoreCase("SMTP"))
            result = getNativeSentRepresentingEmailAddress();
        if (result.isEmpty())
            result = getNativeSenderEmailAddress();
        if (result.isEmpty())
            result = getNativeSentRepresentingEmailAddress();

        if (result.isEmpty())
            result = null;
        return result;
    }

    static String getFormattedAddress(String name, String smtpAddress) {
        String result=null;

        if (name != null){
            if (smtpAddress != null)
                result = name + " <" + smtpAddress + ">";
            else
                result = name;
        }
        else {
            result = smtpAddress;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeFrom()
     */
    protected void analyzeFrom() throws InterruptedException {
        String result = null;

        if (hasRFC822Headers()) {
            // smtp header value
            String[] fromArray = rfc822Headers.getHeader("From");
            if (fromArray != null) {
                if (fromArray.length == 1)
                    result = RFC822Headers.getHeaderValue(fromArray[0]);
                else {
                    List<String> fromList = new ArrayList<>();
                    for (String header : fromArray) {
                        fromList.add(RFC822Headers.getHeaderValue(header));
                    }
                    fromList = RFC822Headers.removeInvalidAndDuplicatesFromAddressesList(fromList);
                    if (fromList.size() > 1) {
                        result = String.join(", ", fromList);
                        logMessageWarning(
                                "mailextractlib.microsoft: multiple From addresses [" + result + "]", null);
                    } else
                        result = fromList.get(0);
                }
                if ((result != null) && (result.contains("=?")))
                    result = decodeRfc2047Flexible(result);
            }
        } else {
            // pst file value
            String fromName = getSenderName();
            String fromAddress = getSenderEmailAddress();
            result=getFormattedAddress(fromName,fromAddress);
        }

        if (result == null)
            logMessageWarning("mailextractlib.microsoft: no From address in header", null);

        from = result;
    }

    // Recipients (To,cc,bcc) specific functions

    public static final int MAPI_TO = 1;
    public static final int MAPI_CC = 2;
    public static final int MAPI_BCC = 3;

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeRecipients()
     */
    protected void analyzeRecipients() throws InterruptedException {
        if (hasRFC822Headers()) {
            // smtp header value
            recipientTo = rfc822Headers.getAddressHeader("To");
            recipientCc = rfc822Headers.getAddressHeader("cc");
            recipientBcc = rfc822Headers.getAddressHeader("bcc");
        } else {
            // pst file values
            recipientTo = new ArrayList<String>();
            recipientCc = new ArrayList<String>();
            recipientBcc = new ArrayList<String>();

            int recipientNumber;
            String normAddress;
            try {
                recipientNumber = getNativeNumberOfRecipients();
            } catch (Exception e) {
                logMessageWarning("mailextractlib.microsoft: can't determine recipient list", e);
                recipientNumber = 0;
            }
            for (int i = 0; i < recipientNumber; i++) {
                try {
                    // prefer smtp address
                    String emailAddress = getNativeRecipientsSmtpAddress(i);
                    if ((emailAddress == null) || (emailAddress.isEmpty()))
                        emailAddress = getNativeRecipientsEmailAddress(i);
                    normAddress = getFormattedAddress(getNativeRecipientsDisplayName(i),emailAddress);
                    switch (getNativeRecipientsType(i)) {
                        case MAPI_TO:
                            recipientTo.add(normAddress);
                            break;
                        case MAPI_CC:
                            recipientCc.add(normAddress);
                            break;
                        case MAPI_BCC:
                            recipientBcc.add(normAddress);
                            break;
                    }
                } catch (Exception e) {
                    logMessageWarning("mailextractlib.microsoft: can't get recipient number " + Integer.toString(i), e);
                }
            }
        }
    }

// ReplyTo specific functions

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeReplyTo()
     */
    protected void analyzeReplyTo() throws InterruptedException {
        List<String> result = null;

        if (hasRFC822Headers()) {
            // smtp header value
            result = rfc822Headers.getAddressHeader("Reply-To");
        }
        // FIXME microsoft file value

        replyTo = result;
    }

// Return-Path specific functions

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeReturnPath()
     */
    protected void analyzeReturnPath() throws InterruptedException {
        String result = null;

        if (hasRFC822Headers()) {
            // smtp header value
            String[] rpArray = rfc822Headers.getHeader("Return-Path");
            if (rpArray != null) {
                if (rpArray.length == 1)
                    result = RFC822Headers.getHeaderValue(rpArray[0]);
                else {
                    List<String> rpList = new ArrayList<>();
                    for (String header : rpArray) {
                        rpList.add(RFC822Headers.getHeaderValue(header));
                    }
                    rpList = RFC822Headers.removeInvalidAndDuplicatesFromAddressesList(rpList);
                    if (rpList.size() > 1) {
                        result = String.join(", ", rpList);
                        logMessageWarning(
                                "mailextractlib.microsoft: multiple Return-Path addresses [" + result + "]", null);
                    } else
                        result = rpList.get(0);
                }
                if ((result != null) && (result.contains("=?")))
                    result = decodeRfc2047Flexible(result);
            }
        }
        // if not in the SMTP header there's no microsoft version

        returnPath = result;
    }

// Dates specific functions

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeDates()
     */
    protected void analyzeDates() {
        receivedDate = getNativeMessageDeliveryTime();
        sentDate = getNativeClientSubmitTime();
    }

// In-reply-to and References specific functions

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeInReplyToId(
     * )
     */
    protected void analyzeInReplyToId() throws InterruptedException {
        String result = null;

        if (hasRFC822Headers()) {
            // smtp header value
            String[] irtList = rfc822Headers.getHeader("In-Reply-To");
            if (irtList != null) {
                if (irtList.length > 1)
                    logMessageWarning(
                            "mailextractlib.microsoft: multiple In-Reply-To identifiers, keep the first one in header", null);
                result = RFC822Headers.getHeaderValue(irtList[0]);
            }
        } else {
            // pst file value
            result = getNativeInReplyToId();
            if (result.isEmpty()) {
                if (messageID == null)
                    analyzeMessageID();
                if ((messageID != null) && messageID.startsWith("<MIC:")) {
                    if (messageID.lastIndexOf('+') > messageID.lastIndexOf('@')) {
                        result = messageID.substring(0, messageID.lastIndexOf('+')) + ">";
                    } else
                        result = null;
                }
            }
        }

        inReplyToUID = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeReferences()
     */
    protected void analyzeReferences() {
        List<String> result = null;

        if (hasRFC822Headers()) {
            // smtp header value
            result = rfc822Headers.getReferences();
        }
        // FIXME pst file value with at least in-reply-to

        references = result;
    }

// Content analysis methods

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeBodies()
     */
    protected void analyzeBodies() {
        String result;

        // text
        result = getNativeBodyText();
        if (result.isEmpty())
            result = null;
        bodyContent[TEXT_BODY] = result;

        // html
        result = getNativeBodyHTML();
        if (result.isEmpty())
            result = null;
        bodyContent[HTML_BODY] = result;

        // rtf
        result = getNativeRTFBody();
        if (result.isEmpty())
            result = null;
        bodyContent[RTF_BODY] = result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeAttachments(
     * )
     */
    @Override
    protected void analyzeAttachments() throws InterruptedException {
        attachments = MicrosoftStoreElement.getAttachments(this, nativeAttachments);
    }
    // Global message

    /**
     * Gets the native mime content.
     *
     * @return the native mime content which is always null
     */
    protected byte[] getNativeMimeContent() {
        return null;
    }

}
