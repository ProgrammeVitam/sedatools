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

package fr.gouv.vitam.tools.mailextractlib.store.javamail.thunderbird;

import javax.mail.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * JavaMail Store for Thunderbird mbox directory/file structure.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class ThunderbirdStore extends Store {

    /** Path to the target Thunderbird mbox directory/file structure **/
    private String container;

    /**
     * Gets the container.
     *
     * @return the container
     */
    public String getContainer() {
        return container;
    }

    /**
     * Constructor.
     *
     * @param session
     *            the session
     * @param url
     *            the url formed as protocol://user@host/container
     *            <ul>
     *            <li>user is the user (declarative only, not mandatory and not
     *            used in processing)</li>
     *            <li>host for now only localhost
     *            <li>container is the directory where is the thunderbird mbox
     *            hierarchy (default current directory)</li>
     *            </ul>ex: /home/me/.thunderbird/hag0y625.default/ImapMail/localhost
     */
    public ThunderbirdStore(Session session, URLName url) {
        super(session, url);

        try {
            container = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // not possible
        }
    }

    /**
     * Override this service method to implement specific check, including url
     * and defined directory availability (not in params)
     *
     * <p>
     * Here control the params coherence in ThunderMBox context and that the
     * container directory exists
     *
     * @param host
     *            for now only localhost
     * @param port
     *            not used
     * @param user
     *            the user declarative only, not used in processing
     * @param passwd
     *            not used
     * @return true, if successful
     * @throws MessagingException
     *             the messaging exception
     */
    @Override
    protected boolean protocolConnect(String host, int port, String user, String passwd) throws MessagingException {
        // verify params significance in ThunderMBox context
        if (!((passwd == null) || (passwd.isEmpty())))
            throw new MessagingException("ThunderMBox: does not allow passwords");
        if (port != -1)
            throw new MessagingException("ThunderMBox: does not allow port selection");

        // verify declared directory for thunderbird mbox hierarchy availability
        File test = new File(container);
        if (!test.isDirectory()) {
            throw new MessagingException("ThunderMBox: " + container + " is not an existing directory");
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Store#getDefaultFolder()
     */
    @Override
    public Folder getDefaultFolder() throws MessagingException {
        return new ThunderbirdFolder(this, null, Folder.HOLDS_FOLDERS);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Store#getFolder(java.lang.String)
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
        if (name.equals(container))
            name = null;
        else if (name.startsWith(container))
            name = name.substring(container.length() + 1);
        return new ThunderbirdFolder(this, name);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.mail.Store#getFolder(javax.mail.URLName)
     */
    @Override
    public Folder getFolder(URLName url) throws MessagingException {
        // verify that the root directory in store is respected
        String filename = "";
        try {
            filename = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // not possible
        }
        if (!filename.startsWith(container))
            throw new MessagingException("ThunderMBox: folder must be in directory declared for the store");
        return getFolder(url.getFile().substring(container.length()));
    }

}
