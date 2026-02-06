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
package fr.gouv.vitam.tools.mailextractlib.store.javamail;

import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.core.StoreElement;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.store.javamail.eml.EmlStore;
import fr.gouv.vitam.tools.mailextractlib.store.javamail.mbox.MboxStore;
import fr.gouv.vitam.tools.mailextractlib.store.javamail.thunderbird.ThunderbirdStore;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import jakarta.mail.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * StoreExtractor sub-class for mail boxes extracted through JavaMail library.
 * <p>
 * For now, IMAP and Thunderbird mbox, eml structure through MailExtract application,
 * could also be used for POP3 and Gmail, via StoreExtractor (not tested).
 */
public class JMStoreExtractor extends StoreExtractor {
    private Store store;

    /**
     * Subscribes at StoreExtractor level all schemes treated by this specific store extractor.
     * <p>
     * This is in default list.
     */
    public static void subscribeStoreExtractor() {
        addExtractionRelation("message/rfc822", "fmt/278","eml", false, JMStoreExtractor.class);
        addExtractionRelation("message/rfc822", "fmt/950","eml", false, JMStoreExtractor.class);
        addExtractionRelation("application/mbox", "fmt/720","mbox", true, JMStoreExtractor.class);
        addExtractionRelation(null, null,"thunderbird", true, JMStoreExtractor.class);
        addExtractionRelation(null, null,"imap", true, JMStoreExtractor.class);
        addExtractionRelation(null, null,"imaps", true, JMStoreExtractor.class);
        addExtractionRelation(null, null,"gimap", true, JMStoreExtractor.class);
        addExtractionRelation(null, null,"pop3", true, JMStoreExtractor.class);
    }

    // Attachment to complete with decoded form
    private StoreAttachment attachment;

    /**
     * Instantiates a new JavaMail StoreExtractor for protcole or complex
     * containers.
     *
     * @param urlString
     *            the url string
     * @param folder
     *            Path of the extracted folder in the account mail box, can be
     *            null if default root folder
     * @param destPathString
     *            the dest path string
     * @param options
     *            Options (flag composition of CONST_)
     * @param rootStoreExtractor
     *            the creating store extractor in nested extraction, or null if
     *            root one
     * @param logger
     *            logger used
     * @throws MailExtractLibException
     *             Any unrecoverable extraction exception (access trouble, major
     *             format problems...)
     */
    public JMStoreExtractor(String urlString, String folder, String destPathString, StoreExtractorOptions options,
                            StoreExtractor rootStoreExtractor, MailExtractProgressLogger logger) throws MailExtractLibException {
        super(urlString, folder, destPathString, options, rootStoreExtractor, null, logger);

        String url;

        url = urlString;
        if (folder != null && !folder.isEmpty())
            url += "/" + folder;

        try {
            // Connect to the store
            Properties props = System.getProperties();
            props.setProperty("mail.imaps.ssl.trust", "*");
            props.setProperty("mail.imap.ssl.trust", "*");
            setSessionProperties(props);
            Session session = Session.getDefaultInstance(props, null);

            // add thunderbird provider
            session.addProvider(new Provider(Provider.Type.STORE, "thunderbird",
                    ThunderbirdStore.class.getName(),
                    "fr.gouv.vitam", getClass().getPackage().getImplementationVersion()));
            // add eml provider
            session.addProvider(new Provider(Provider.Type.STORE, "eml",
                    EmlStore.class.getName(), "fr.gouv.vitam",
                    getClass().getPackage().getImplementationVersion()));
            // add mbox provider
            session.addProvider(new Provider(Provider.Type.STORE, "mbox",
                    MboxStore.class.getName(), "fr.gouv.vitam",
                    getClass().getPackage().getImplementationVersion()));

            URLName urlName = new URLName(url);
            store = session.getStore(urlName);
            store.connect();
        } catch (MessagingException e) {
            throw new MailExtractLibException("mailextractlib.javamail: can't get store for " + getDecodedURL(url), e);
        }

        ArchiveUnit rootNode = new ArchiveUnit(this, destRootPath, destName);
        JMStoreFolder jMRootMailBoxFolder;

        try {
            if ((folder == null) || folder.isEmpty())

                jMRootMailBoxFolder = JMStoreFolder.createRootFolder(this, store.getDefaultFolder(), rootNode);
            else
                jMRootMailBoxFolder = JMStoreFolder.createRootFolder(this, store.getFolder(folder), rootNode);

            if (!jMRootMailBoxFolder.folder.exists()) {
                throw new MailExtractLibException("mailextractlib.javamail: can't find extraction root folder " + folder, null);
            }
            setRootFolder(jMRootMailBoxFolder);
        } catch (MessagingException e) {
            throw new MailExtractLibException("mailextractlib.javamail: can't find extraction root folder " + folder, e);
        }
    }

    /**
     * Instantiates a new JavaMail StoreExtractor for embedded container.
     *
     * @param attachment         the attachment
     * @param rootNode           the root node
     * @param options            Options
     * @param rootStoreExtractor the creating store extractor in nested extraction, or null if root one
     * @param fatherElement      the father element in nested extraction, or null if root one
     * @param logger             logger used
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major format problems...)
     */
    public JMStoreExtractor(StoreAttachment attachment, ArchiveUnit rootNode, StoreExtractorOptions options,
                            StoreExtractor rootStoreExtractor, StoreElement fatherElement, MailExtractProgressLogger logger) throws MailExtractLibException {
        super(attachment.getScheme() + "://localhost/", "", rootNode.getFullName(), options, rootStoreExtractor, fatherElement, logger);
        String url;

        url = attachment.getScheme() + ":";
        this.attachment = attachment;
        try {
            // Connect to the store
            Properties props = System.getProperties();
            props.setProperty("mail.imaps.ssl.trust", "*");
            props.setProperty("mail.imap.ssl.trust", "*");
            setSessionProperties(props);
            Session session = Session.getDefaultInstance(props, null);

            // add eml provider
            session.addProvider(new Provider(Provider.Type.STORE, "eml",
                    EmlStore.class.getName(), "fr.gouv.vitam",
                    getClass().getPackage().getImplementationVersion()));
            // add mbox provider
            session.addProvider(new Provider(Provider.Type.STORE, "mbox",
                    MboxStore.class.getName(), "fr.gouv.vitam",
                    getClass().getPackage().getImplementationVersion()));

            URLName urlName = new URLName(url);
            store = session.getStore(urlName);
            if (!(store instanceof JMEmbeddedStore)) {
                throw new MailExtractLibException(
                        "mailextractlib.javamail: can't extract embedded store for scheme [" + scheme + "]", null);
            }
            JMEmbeddedStore ejs = (JMEmbeddedStore) store;
            ejs.setObjectContent(attachment.getStoreContent());
            store.connect();
        } catch (MessagingException e) {
            throw new MailExtractLibException(
                    "mailextractlib.javamail: can't get store for " + url, e);
        }

        JMStoreFolder jMRootMailBoxFolder;

        try {
            jMRootMailBoxFolder = JMStoreFolder.createRootFolder(this, store.getDefaultFolder(), rootNode);
            setRootFolder(jMRootMailBoxFolder);
        } catch (MessagingException e) {
            throw new MailExtractLibException("mailextractlib.javamail: can't find extraction root folder ", e);
        }
    }

    // decode URL encoding to UTF-8
    private static String getDecodedURL(String url) {
        String decodedUrl = "";
        try {
            decodedUrl = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // non sense for UTF-8
        }
        return decodedUrl;
    }

    // set properties to have the most tolerant reader...
    private void setSessionProperties(Properties props) {
        props.setProperty("mail.mime.decodetext.strict", "false"); // not
        // default
        props.setProperty("mail.mime.encodeeol.strict", "false");
        props.setProperty("mail.mime.decodefilename", "true"); // not default
        props.setProperty("mail.mime.decodeparameters", "true");
        props.setProperty("mail.mime.multipart.ignoremissingendboundary", "true");
        props.setProperty("mail.mime.multipart.ignoremissingboundaryparameter", "true");
        props.setProperty("mail.mime.parameters.strict", "false"); // not
        // default
        props.setProperty("mail.mime.windowsfilenames", "true"); // not default
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor#getAttachment()
     */
    @Override
    public StoreAttachment getAttachment() {
        return attachment;
    }


    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor#canExtractObjectsLists()
     */
    @Override
    public boolean canExtractObjectsLists() {
        return ! scheme.equals("eml");
    }
}
