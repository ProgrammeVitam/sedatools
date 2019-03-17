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

package fr.gouv.vitam.tools.mailextractlib.store.javamail.mbox;

import fr.gouv.vitam.tools.mailextractlib.store.javamail.JMMimeMessage;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import javax.mail.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaMail Folder for mbox file structure.
 * <p>
 * This is the main class for folder analysis and message slicing.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class MboxFolder extends Folder {

    private volatile boolean opened = false;
    private MboxStore mboxstore;
    private MboxReader mboxfilereader;
    private MailExtractProgressLogger logger;
    private List<MessageFork> messages;
    private int total; // total number of messages in mailbox

    private class MessageFork {
        long beg, end;

        MessageFork(long beg, long end) {
            this.beg = beg;
            this.end = end;
        }
    }

    /**
     * Sets the logger
     * <p>
     * This method is directly called from MailExtract library to enable this
     * class to log
     *
     * @param logger
     *            Store extractor logger
     */
    public void setLogger(MailExtractProgressLogger logger) {
        this.logger = logger;
    }

    /**
     * Instantiates a new mbox simulated folder.
     *
     * @param store
     *            Store
     * @throws MessagingException
     *             Messaging exception from inner JavaMail calls
     */
    // constructors
    public MboxFolder(MboxStore store) throws MessagingException {
        super(store);
        this.mboxstore = store;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getName()
     */
    @Override
    public String getName() {
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getFullName()
     */
    @Override
    public String getFullName() {
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getSeparator()
     */
    // implement inherited abstract method Folder.getSeparator()
    @Override
    public char getSeparator() {
        return File.separatorChar;
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public Folder[] list(String pattern) throws MessagingException {
        throw new MethodNotSupportedException("mbox: list with pattern not supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#list()
     */
    @Override
    public Folder[] list() throws MessagingException {
        Folder[] result = new MboxFolder[0];

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getParent()
     */
    @Override
    public Folder getParent() throws MessagingException {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#exists()
     */
    @Override
    public boolean exists() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getType()
     */
    @Override
    public int getType() {
        return HOLDS_MESSAGES;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getPermanentFlags()
     */
    @Override
    public Flags getPermanentFlags() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#hasNewMessages()
     */
    @Override
    public boolean hasNewMessages() {
        // only read static mbox file
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getFolder(java.lang.String)
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
        if ((name == null) || (name.isEmpty()))
            return new MboxFolder(mboxstore);
        else
            throw new MethodNotSupportedException("mbox: no folder supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean create(int type) throws MessagingException {
        throw new MethodNotSupportedException("mbox: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("mbox: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("mbox: no writing supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#isOpen()
     */
    @Override
    public boolean isOpen() {
        return opened;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#open(int)
     */
    @Override
    public void open(int mode) throws MessagingException {
        if (opened)
            throw new IllegalStateException("mbox: file " + mboxstore.getContainer() + " is already open");

        this.mode = mode;
        switch (mode) {
            case READ_WRITE:
                throw new MethodNotSupportedException("mbox: no writing supported");
            case READ_ONLY:
            default:
                break;
        }

        messages = new ArrayList<MessageFork>();
        MessageFork mf;

        try {
            if (mboxstore.getObjectContent() != null)
                mboxfilereader = new MboxReader(logger, (byte[]) mboxstore.getObjectContent());
            else
                mboxfilereader = new MboxReader(logger, new File(mboxstore.getContainer()));
            opened = true; // now really opened
            long beg, end;

            mboxfilereader.getNextFromLineBeg();
            beg = mboxfilereader.getLastFromLineEnd();
            while (beg != -1) {
                end = mboxfilereader.getNextFromLineBeg();
                mf = new MessageFork(beg, end);
                messages.add(mf);
                beg = mboxfilereader.getLastFromLineEnd();
            }
        } catch (IOException e) {
            throw new MessagingException("mbox: open failure, can't read: " + mboxstore.getContainer() + " file");
        }

        total = messages.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#close(boolean)
     */
    @Override
    public void close(boolean expunge) throws MessagingException {
        if (!opened)
            throw new IllegalStateException("mbox: file " + mboxstore.getContainer() + " is not open");
        messages = null;
        opened = false;
        try {
            mboxfilereader.close();
        } catch (IOException e) {
            // forget it
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getMessageCount()
     */
    @Override
    public int getMessageCount() throws MessagingException {
        if (!opened)
            return -1;

        return total;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getMessage(int)
     */
    @Override
    public Message getMessage(int msgno) throws MessagingException {
        if (msgno < 1) // message-numbers start at 1
            throw new IndexOutOfBoundsException("message number " + msgno + " < 1");
        else if (msgno > total) // Still out of range ? Throw up ...
            throw new IndexOutOfBoundsException("message number " + msgno + " > " + total);
        Message m;
        // each get regenerate a message with no strong link so that it can be
        // GC
        // optimal for the extraction usage with only one get by message
        m = new JMMimeMessage(this, mboxfilereader.newStream(messages.get(msgno - 1).beg, messages.get(msgno - 1).end),
                msgno);

        return m;
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("mbox: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("mbox: no writing supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Folder#getURLName()
     */
    @Override
    public URLName getURLName() {
        URLName storeURL = getStore().getURLName();

        return new URLName(storeURL.getProtocol(), storeURL.getHost(), storeURL.getPort(), mboxstore.getContainer(),
                storeURL.getUsername(), null /* no password */);
    }
}
