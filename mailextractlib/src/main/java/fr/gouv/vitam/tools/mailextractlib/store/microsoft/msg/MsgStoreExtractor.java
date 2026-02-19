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
package fr.gouv.vitam.tools.mailextractlib.store.microsoft.msg;

import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.core.StoreElement;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import org.apache.poi.hsmf.MAPIMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * StoreExtractor sub-class for message file extracted through POI HSMF library.
 */
public class MsgStoreExtractor extends StoreExtractor {

    /**
     * Subscribes at StoreExtractor level all schemes treated by this specific store extractor.
     * <p>
     * This is in default list.
     */
    public static void subscribeStoreExtractor() {
        addExtractionRelation("application/vnd.ms-outlook", "x-fmt/430", "msg", false, MsgStoreExtractor.class);
        addExtractionRelation(null, null, "msg.embeddedmsg", false, MsgStoreExtractor.class);
    }

    // Attachment to complete with decoded form
    private StoreAttachment attachment;

    /**
     * Instantiates a new msg store extractor.
     *
     * @param urlString          the url string
     * @param folder             the folder
     * @param destPathString     the dest path string
     * @param options            the options
     * @param rootStoreExtractor the root store extractor
     * @param logger             the logger
     * @throws MailExtractLibException the extraction exception
     * @throws InterruptedException    the interrupted exception
     */
    public MsgStoreExtractor(
        String urlString,
        String folder,
        String destPathString,
        StoreExtractorOptions options,
        StoreExtractor rootStoreExtractor,
        MailExtractProgressLogger logger
    ) throws MailExtractLibException, InterruptedException {
        super(urlString, folder, destPathString, options, rootStoreExtractor, null, logger);
        MAPIMessage message;
        long size;

        try {
            File messageFile = new File(path);
            message = new MAPIMessage(messageFile);
            size = Files.size(messageFile.toPath());
        } catch (Exception e) {
            throw new MailExtractLibException(
                "mailextractlib.msg: can't open " + path + ", doesn't exist or is not a msg file",
                e
            );
        }

        ArchiveUnit rootNode = new ArchiveUnit(this, destRootPath, destName);
        setRootFolder(MsgStoreFolder.createRootFolder(this, message, size, rootNode));
    }

    /**
     * Instantiates a new embedded msg store extractor.
     *
     * @param attachment         the attachment
     * @param rootNode           the ArchiveUnit node representing this container
     * @param options            the options
     * @param rootStoreExtractor the root store extractor in nested extraction, or null if root one
     * @param fatherElement      the father element in nested extraction, or null if root one
     * @param logger             the logger
     * @throws MailExtractLibException the extraction exception
     * @throws InterruptedException    the interrupted exception
     */
    public MsgStoreExtractor(
        StoreAttachment attachment,
        ArchiveUnit rootNode,
        StoreExtractorOptions options,
        StoreExtractor rootStoreExtractor,
        StoreElement fatherElement,
        MailExtractProgressLogger logger
    ) throws MailExtractLibException, InterruptedException {
        super(
            "msg.embeddedmsg://localhost/",
            "",
            rootNode.getFullName(),
            options,
            rootStoreExtractor,
            fatherElement,
            logger
        );
        MAPIMessage message;

        this.attachment = attachment;
        if (attachment.getStoreContent() instanceof MAPIMessage) message = (MAPIMessage) attachment.getStoreContent();
        else if (attachment.getStoreContent() instanceof byte[]) {
            ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) attachment.getStoreContent());
            try {
                message = new MAPIMessage(bais);
            } catch (IOException e) {
                throw new MailExtractLibException("mailextractlib.msg: can't extract msg store", e);
            }
        } else throw new MailExtractLibException("mailextractlib.msg: can't extract msg store", null);

        setRootFolder(MsgStoreFolder.createRootFolder(this, message, 0, rootNode));
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
        return false;
    }

    /**
     * The Constant MSG_MN.
     */
    static final byte[] MSG_MN = new byte[] {
        (byte) 0xD0,
        (byte) 0xCF,
        0x11,
        (byte) 0xE0,
        (byte) 0xA1,
        (byte) 0xB1,
        0x1A,
        (byte) 0xE1,
    };

    /**
     * Gets the verified scheme.
     *
     * @param content the content
     * @return the verified scheme
     */
    public static String getVerifiedScheme(byte[] content) {
        if (hasMagicNumber(content, MSG_MN)) {
            return "msg";
        } else return null;
    }
}
