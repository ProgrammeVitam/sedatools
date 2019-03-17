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

package fr.gouv.vitam.tools.mailextractlib.store.microsoft.msg;

import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessageAttachment;
import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.datatypes.*;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * StoreMessage sub-class for mail boxes extracted through POI HSMF library.
 */
public class MsgStoreMessage extends MicrosoftStoreMessage {

    /**
     * The message.
     */
    MAPIMessage message;

    /**
     * The size.
     */
    long size;

    /**
     * The msg conversation index.
     */
    MsgConversationIndex msgConversationIndex;

    /**
     * The Constant EMBEDDED_MESSAGE.
     */
    static final String EMBEDDED_MESSAGE = "msg.embeddedmsg";

    /**
     * The Constant CONVERSATION_INDEX.
     */
    public static final int CONVERSATION_INDEX = 0x0071;

    /**
     * The Constant SMTP_TRANSPORT_HEADER.
     */
    public static final int SMTP_TRANSPORT_HEADER = 0x007d;

    /**
     * The Constant SUBJECT.
     */
    public static final int SUBJECT = 0x0037;

    /**
     * The Constant INTERNET_MESSAGE_ID.
     */
    public static final int INTERNET_MESSAGE_ID = 0x1035;

    /**
     * The Constant SENDER_NAME.
     */
    public static final int SENDER_NAME = 0x0c1a;

    /**
     * The Constant SENT_REPRESENTING_NAME.
     */
    public static final int SENT_REPRESENTING_NAME = 0x0042;

    /**
     * The Constant SENDER_ADDR_TYPE.
     */
    public static final int SENDER_ADDR_TYPE = 0x0c1e;

    /**
     * The Constant SENDER_EMAIL_ADDRESS.
     */
    public static final int SENDER_EMAIL_ADDRESS = 0x0c1f;

    /**
     * The Constant SENT_REPRESENTING_ADDR_TYPE.
     */
    public static final int SENT_REPRESENTING_ADDR_TYPE = 0x0064;

    /**
     * The Constant SENT_REPRESENTING_EMAIL_ADDRESS.
     */
    public static final int SENT_REPRESENTING_EMAIL_ADDRESS = 0x0065;

    /**
     * The Constant RETURN_PATH.
     */
    public static final int RETURN_PATH = 0x1046;

    /**
     * The Constant MESSAGE_DELIVERY_TIME.
     */
    public static final int MESSAGE_DELIVERY_TIME = 0x0e06;

    /**
     * The Constant CLIENT_SUBMIT_TIME.
     */
    public static final int CLIENT_SUBMIT_TIME = 0x0039;

    /**
     * The Constant IN_REPLY_TO_ID.
     */
    public static final int IN_REPLY_TO_ID = 0x1042;

    /**
     * The Constant MESSAGE_SIZE.
     */
    public static final int MESSAGE_SIZE = 0x0e08;

    /**
     * Instantiates a new msg store message.
     *
     * @param mBFolder the m B folder
     * @param message  the message
     * @param size     the size
     * @throws InterruptedException the interrupted exception
     */
    public MsgStoreMessage(StoreFolder mBFolder, MAPIMessage message, long size) throws InterruptedException {
        super(mBFolder);
        this.message = message;
        this.size = size;
        getConversationIndex();
    }

    private void getConversationIndex() throws InterruptedException {
        byte[] byteConversationIndex = getByteItem(CONVERSATION_INDEX);
        if (byteConversationIndex != null) {
            msgConversationIndex = new MsgConversationIndex(byteConversationIndex);
            if (msgConversationIndex.getGuid() == null)
                msgConversationIndex = null;
        }
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeMessageSize()
     */
    @Override
    protected long getNativeMessageSize() {
        return size;
    }

    private String getStringItem(int item) throws InterruptedException {
        String result = "";
        MAPIProperty prop = MAPIProperty.get(item);
        List<Chunk> lChunk = message.getMainChunks().getAll().get(prop);

        if (lChunk != null) {
            if (lChunk.size() > 1) {
                logMessageWarning("mailextract.msg: Multiple MSG object [" + prop.name + "], keep the first one");
            }
            if (lChunk.get(0) instanceof StringChunk) {
                StringChunk sChunk = (StringChunk) lChunk.get(0);
                result = sChunk.getValue();
            } else
                logMessageWarning("mailextract.msg: Unexpected MSG object type [" + prop.name + "]");
        }
        return result;
    }

    private byte[] getByteItem(int item) throws InterruptedException {
        byte[] result = null;
        MAPIProperty prop = MAPIProperty.get(item);
        List<Chunk> lChunk = message.getMainChunks().getAll().get(prop);

        if (lChunk != null) {
            if (lChunk.size() > 1) {
                logMessageWarning("mailextract.msg: Multiple MSG object [" + prop.name + "], keep the first one");
            }
            if (lChunk.get(0) instanceof ByteChunk) {
                ByteChunk bChunk = (ByteChunk) lChunk.get(0);
                result = bChunk.getValue();
            } else
                logMessageWarning("mailextract.msg: Unexpected MSG object type [" + prop.name + "]");
        }
        return result;
    }

    private Date getDateItem(int item) {
        Date result = null;
        MAPIProperty prop = MAPIProperty.get(item);
        List<PropertyValue> lVal = message.getMainChunks().getMessageProperties().getProperties().get(prop);

        if (lVal != null) {
            Calendar cal = (Calendar) lVal.get(0).getValue();
            if (cal != null)
                result = cal.getTime();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSmtpTransportHeader()
     */
    @Override
    protected String getNativeSmtpTransportHeader() throws InterruptedException {
        return getStringItem(SMTP_TRANSPORT_HEADER);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSubject()
     */
    @Override
    protected String getNativeSubject() throws InterruptedException {
        return getStringItem(SUBJECT);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeInternetMessageId()
     */
    @Override
    protected String getNativeInternetMessageId() throws InterruptedException {
        return getStringItem(INTERNET_MESSAGE_ID);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSenderName()
     */
    @Override
    protected String getNativeSenderName() throws InterruptedException {
        return getStringItem(SENDER_NAME);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSentRepresentingName()
     */
    @Override
    protected String getNativeSentRepresentingName() throws InterruptedException {
        return getStringItem(SENT_REPRESENTING_NAME);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSenderAddrType()
     */
    @Override
    protected String getNativeSenderAddrType() throws InterruptedException {
        return getStringItem(SENDER_ADDR_TYPE);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSenderEmailAddress()
     */
    @Override
    protected String getNativeSenderEmailAddress() throws InterruptedException {
        return getStringItem(SENDER_EMAIL_ADDRESS);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSentRepresentingAddrType()
     */
    @Override
    protected String getNativeSentRepresentingAddrType() throws InterruptedException {
        return getStringItem(SENT_REPRESENTING_ADDR_TYPE);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeSentRepresentingEmailAddress()
     */
    @Override
    protected String getNativeSentRepresentingEmailAddress() throws InterruptedException {
        return getStringItem(SENT_REPRESENTING_EMAIL_ADDRESS);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeReturnPath()
     */
    @Override
    protected String getNativeReturnPath() throws InterruptedException {
        return getStringItem(RETURN_PATH);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeMessageDeliveryTime()
     */
    @Override
    protected Date getNativeMessageDeliveryTime() {
        return getDateItem(MESSAGE_DELIVERY_TIME);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeClientSubmitTime()
     */
    @Override
    protected Date getNativeClientSubmitTime() {
        return getDateItem(CLIENT_SUBMIT_TIME);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeInReplyToId()
     */
    @Override
    protected String getNativeInReplyToId() throws InterruptedException {
        return getStringItem(IN_REPLY_TO_ID);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#hasNativeConversationIndex()
     */
    @Override
    protected boolean hasNativeConversationIndex() {
        return msgConversationIndex != null;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeCIDeliveryTime()
     */
    @Override
    protected Date getNativeCIDeliveryTime() {
        return msgConversationIndex.getDeliveryTime();
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeCIGuid()
     */
    @Override
    protected UUID getNativeCIGuid() {
        return msgConversationIndex.getGuid();
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeCINumberOfResponseLevels()
     */
    @Override
    protected int getNativeCINumberOfResponseLevels() {
        return msgConversationIndex.getResponseLevels().size();
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeCIResponseLevelDeltaCode(int)
     */
    @Override
    protected short getNativeCIResponseLevelDeltaCode(int responseLevelNumber) {
        return msgConversationIndex.getResponseLevels().get(responseLevelNumber).deltaCode;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeCIResponseLevelTimeDelta(int)
     */
    @Override
    protected long getNativeCIResponseLevelTimeDelta(int responseLevelNumber) {
        return msgConversationIndex.getResponseLevels().get(responseLevelNumber).timeDelta;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeCIResponseLevelRandom(int)
     */
    @Override
    protected short getNativeCIResponseLevelRandom(int responseLevelNumber) {
        return msgConversationIndex.getResponseLevels().get(responseLevelNumber).random;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeNumberOfRecipients()
     */
    @Override
    protected int getNativeNumberOfRecipients() {
        return message.getRecipientDetailsChunks().length;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsSmtpAddress(int)
     */
    @Override
    protected String getNativeRecipientsSmtpAddress(int recipientNumber) {
        return message.getRecipientDetailsChunks()[recipientNumber].recipientSMTPChunk.getValue();
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsEmailAddress(int)
     */
    @Override
    protected String getNativeRecipientsEmailAddress(int recipientNumber) {
        return message.getRecipientDetailsChunks()[recipientNumber].recipientEmailChunk.getValue();
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsDisplayName(int)
     */
    @Override
    protected String getNativeRecipientsDisplayName(int recipientNumber) {
        return message.getRecipientDetailsChunks()[recipientNumber].recipientNameChunk.getValue();
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeRecipientsType(int)
     */
    @Override
    protected int getNativeRecipientsType(int recipientNumber) {
        return (int) message.getRecipientDetailsChunks()[recipientNumber].getProperties()
                .get(MAPIProperty.RECIPIENT_TYPE).get(0).getValue();
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeBodyText()
     */
    @Override
    protected String getNativeBodyText() {
        String result;
        try {
            result = message.getTextBody();
        } catch (ChunkNotFoundException e) {
            result = "";
        }
        return result;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeBodyHTML()
     */
    @Override
    protected String getNativeBodyHTML() {
        String result;
        try {
            result = message.getHtmlBody();
        } catch (ChunkNotFoundException e) {
            result = "";
        }
        return result;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeRTFBody()
     */
    @Override
    protected String getNativeRTFBody() {
        String result;
        try {
            result = message.getRtfBody();
        } catch (ChunkNotFoundException e) {
            result = "";
        }
        return result;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getEmbeddedMessageScheme()
     */
    @Override
    protected String getEmbeddedMessageScheme() {
        return EMBEDDED_MESSAGE;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessage#getNativeAttachments()
     */
    @Override
    protected MicrosoftStoreMessageAttachment[] getNativeAttachments() {
        MsgStoreMessageAttachment[] msgAttachments;
        AttachmentChunks[] allAttachmentChunks = message.getAttachmentFiles();

        msgAttachments = new MsgStoreMessageAttachment[allAttachmentChunks.length];
        for (int i = 0; i < allAttachmentChunks.length; i++) {
            msgAttachments[i] = new MsgStoreMessageAttachment(allAttachmentChunks[i]);
        }
        return msgAttachments;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreMessage#analyzeAppointmentInformation()
     */
    @Override
    protected void analyzeAppointmentInformation() {
        // TODO Auto-generated method stub
        return;
    }

}
