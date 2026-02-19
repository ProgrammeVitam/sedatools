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
package fr.gouv.vitam.tools.mailextractlib.store.javamail.eml;

import fr.gouv.vitam.tools.mailextractlib.store.javamail.JMEmbeddedStore;
import jakarta.mail.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * JavaMail Store for eml uniq message file.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class EmlStore extends Store implements JMEmbeddedStore {

    /** Path to the target eml file **/
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
     *            the url supposed to be formed as eml://localhost
     */
    public EmlStore(Session session, URLName url) {
        super(session, url);
    }

    /**
     * Override this service method to implement specific check, including url
     * and defined directory availability (not in params)
     *
     * <p>
     * Here control the params coherence eml single mail eml://localhost.
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
            if (!host.equals("localhost")) throw new MessagingException("eml: only support localhost");
            if (!((passwd == null) || (passwd.isEmpty()))) throw new MessagingException(
                "eml: does not allow passwords"
            );
            if (port != -1) throw new MessagingException("eml: does not allow port selection");

            // verify declared file for eml availability
            try {
                container = URLDecoder.decode(url.getFile(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new MessagingException("Eml: Can't decode the container file name");
            }
            File test = new File(container);
            if (!test.isFile()) {
                throw new MessagingException("Eml: " + container + " is not an existing file");
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
        return new EmlFolder(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Store#getFolder(java.lang.String)
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
        if ((name == null) || (name.isEmpty())) return new EmlFolder(this);
        else throw new MessagingException("eml: only one root simulated folder, no " + name + " folder");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Store#getFolder(jakarta.mail.URLName)
     */
    @Override
    public Folder getFolder(URLName url) throws MessagingException {
        // verify that the root directory in store is respected
        if ((url.getFile() == null) || (url.getFile().isEmpty())) return new EmlFolder(this);
        else throw new MessagingException("eml: only one root simulated folder, no " + url.getFile() + " folder");
    }

    public void setObjectContent(Object objectContent) {
        if (objectContent instanceof byte[]) this.objectContent = (byte[]) objectContent;
    }

    public Object getObjectContent() {
        return objectContent;
    }
}
