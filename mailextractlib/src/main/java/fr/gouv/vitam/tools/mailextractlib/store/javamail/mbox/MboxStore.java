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

import fr.gouv.vitam.tools.mailextractlib.store.javamail.JMEmbeddedStore;

import jakarta.mail.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * JavaMail Store for mbox messages file.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class MboxStore extends Store implements JMEmbeddedStore {

    /** Path to the target mbox file */
    private String container;

    /** String eml content if embedded **/
    private byte[] objectContent;

    /**
     * Gets the container.
     *
     * @return the container
     */
    public String getContainer() {
        return container;
    }

    /**
     * Constructor, used by the JavaMail library.
     *
     * @param session
     *            the session
     * @param url
     *            the url supposed to be formed as mbox://localhost
     */
    public MboxStore(Session session, URLName url) {
        super(session, url);
    }

    /**
     * Override this service method to implement specific check, including url
     * and defined directory availability (not in params)
     *
     * <p>
     * Here control the params coherence mbox mail file mbox://localhost.
     *
     * @param host
     *            only localhost
     * @param port
     *            not used
     * @param user
     *            not used
     * @param passwd
     *            not used
     * @return true, if successful
     * @throws MessagingException
     *             the messaging exception
     */
    @Override
    protected boolean protocolConnect(String host, int port, String user, String passwd) throws MessagingException {
        // verify only if not embedded
        if (objectContent == null) {
            // verify params significance in ThunderMBox context
            if (!host.equals("localhost"))
                throw new MessagingException("mbox: only support localhost");
            if (!((passwd == null) || (passwd.isEmpty())))
                throw new MessagingException("mbox: does not allow passwords");
            if (port != -1)
                throw new MessagingException("mbox: does not allow port selection");

            // verify declared file for mbox availability
            try {
                container = URLDecoder.decode(url.getFile(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new MessagingException("mbox: Can't decode the container file name");
            }
            File test = new File(container);
            if (!test.isFile()) {
                throw new MessagingException("mbox: " + container + " is not an existing file");
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Store#getDefaultFolder()
     */
    @Override
    public Folder getDefaultFolder() throws MessagingException {
        return new MboxFolder(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Store#getFolder(java.lang.String)
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
        if ((name == null) || (name.isEmpty()))
            return new MboxFolder(this);
        else
            throw new MessagingException("mbox: only one root simulated folder, no " + name + " folder");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Store#getFolder(jakarta.mail.URLName)
     */
    @Override
    public Folder getFolder(URLName url) throws MessagingException {
        if ((url.getFile() == null) || (url.getFile().isEmpty()))
            return new MboxFolder(this);
        else
            throw new MessagingException("mbox: only one root simulated folder, no " + url.getFile() + " folder");
    }

    @Override
    public void setObjectContent(Object objectContent) {
        if (objectContent instanceof byte[])
            this.objectContent = (byte[]) objectContent;
    }

    @Override
    public Object getObjectContent() {
        return objectContent;
    }
}
