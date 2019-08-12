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

package fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst.embeddedmsg;

import com.pff.PSTMessage;
import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.core.StoreElement;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

/**
 * StoreExtractor sub-class for embedded messages extracted through libpst
 * library.
 */
public class PstEmbeddedStoreExtractor extends StoreExtractor {

    /**
     * Subscribes at StoreExtractor level all schemes treated by this specific store extractor.
     * <p>
     * This is in default list.
     */
    static public void subscribeStoreExtractor() {
        addExtractionRelation(null, "pst.embeddedmsg", false, PstEmbeddedStoreExtractor.class);
    }

    // Attachment to complete with decoded form
    private StoreAttachment attachment;

    /**
     * Instantiates a new LP embedded message store extractor.
     *
     * @param attachment         the attachment
     * @param rootNode           the ArchiveUnit node representing this container
     * @param options            Options (flag composition of CONST_)
     * @param rootStoreExtractor the creating store extractor in nested extraction, or null if root one
     * @param fatherElement      the father element in nested extraction, or null if root one
     * @param logger             logger used
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major format problems...)
     */
    public PstEmbeddedStoreExtractor(StoreAttachment attachment, ArchiveUnit rootNode,
                                     StoreExtractorOptions options, StoreExtractor rootStoreExtractor, StoreElement fatherElement, MailExtractProgressLogger logger)
            throws MailExtractLibException {
        super("pst.embeddedmsg://localhost/", "", rootNode.getFullName(), options, rootStoreExtractor, fatherElement, logger);

        this.attachment = attachment;
        setRootFolder(PstEmbeddedStoreFolder.createRootFolder((PSTMessage) attachment.getStoreContent(), this, rootNode));
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

    ;
}
