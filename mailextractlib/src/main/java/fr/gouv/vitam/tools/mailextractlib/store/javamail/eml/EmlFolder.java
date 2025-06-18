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

package fr.gouv.vitam.tools.mailextractlib.store.javamail.eml;

import fr.gouv.vitam.tools.mailextractlib.store.javamail.JMMimeMessage;

import jakarta.mail.*;
import jakarta.mail.util.SharedByteArrayInputStream;
import jakarta.mail.util.SharedFileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * JavaMail Folder simulated for Eml uniq message file.
 * <p>
 * This is the main class for folder analysis and message slicing.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class EmlFolder extends Folder {

    private volatile boolean opened = false;
    private EmlStore emlstore;
    private InputStream emlInputStream;

    /**
     * Instantiates a new Eml simulated folder.
     *
     * @param store
     *            Store
     * @throws MessagingException
     *             Messaging exception from inner JavaMail calls
     */
    // constructors
    public EmlFolder(EmlStore store) throws MessagingException {
        super(store);
        this.emlstore = store;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getName()
     */
    @Override
    public String getName() {
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getFullName()
     */
    @Override
    public String getFullName() {
        return "";
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getSeparator()
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
        throw new MethodNotSupportedException("eml: list with pattern not supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#list()
     */
    @Override
    public Folder[] list() throws MessagingException {
        Folder[] result = new EmlFolder[0];

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getParent()
     */
    @Override
    public Folder getParent() throws MessagingException {
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#exists()
     */
    @Override
    public boolean exists() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getType()
     */
    @Override
    public int getType() {
        return HOLDS_MESSAGES;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getPermanentFlags()
     */
    @Override
    public Flags getPermanentFlags() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#hasNewMessages()
     */
    @Override
    public boolean hasNewMessages() {
        // a simulated folder in memory with one rfc822 mail never changed
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getFolder(java.lang.String)
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
        if ((name == null) || (name.isEmpty()))
            return new EmlFolder(emlstore);
        else
            throw new MethodNotSupportedException("eml: no folder supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean create(int type) throws MessagingException {
        throw new MethodNotSupportedException("eml: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("eml: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("eml: no writing supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#isOpen()
     */
    @Override
    public boolean isOpen() {
        return opened;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#open(int)
     */
    @Override
    public void open(int mode) throws MessagingException {
        if (opened)
            throw new IllegalStateException("eml: simulated folder is already open");

        this.mode = mode;
        switch (mode) {
            case READ_WRITE:
                throw new MethodNotSupportedException("eml: no writing supported");
            case READ_ONLY:
            default:
                break;
        }

        // create input stream from embedded content
        if (emlstore.getObjectContent() != null) {
            emlInputStream = new SharedByteArrayInputStream((byte[]) emlstore.getObjectContent());
        } else {
            // create input stream from file
            try {

                emlInputStream = new SharedFileInputStream(new File(emlstore.getContainer()));
            } catch (IOException e) {
                throw new MessagingException("eml: open failure, can't read: " + emlstore.getContainer());
            }
        }
        opened = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#close(boolean)
     */
    @Override
    public void close(boolean expunge) throws MessagingException {
        if (!opened)
            throw new IllegalStateException("eml: simulated folder is not open");
        opened = false;
        try {
            emlInputStream.close();
        } catch (IOException e) {
            // forget it
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getMessageCount()
     */
    @Override
    public int getMessageCount() throws MessagingException {
        return 1;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getMessage(int)
     */
    @Override
    public Message getMessage(int msgno) throws MessagingException {
        if (msgno != 1)
            throw new IndexOutOfBoundsException("Eml: only message 1, no message number " + msgno);
        Message m;

        m = new JMMimeMessage(this, emlInputStream, msgno);

        return m;
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("Eml: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("Eml: no writing supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getURLName()
     */
    @Override
    public URLName getURLName() {
        URLName storeURL = getStore().getURLName();

        return new URLName(storeURL.getProtocol(), storeURL.getHost(), storeURL.getPort(), emlstore.getContainer(),
                storeURL.getUsername(), null /* no password */);
    }
}
