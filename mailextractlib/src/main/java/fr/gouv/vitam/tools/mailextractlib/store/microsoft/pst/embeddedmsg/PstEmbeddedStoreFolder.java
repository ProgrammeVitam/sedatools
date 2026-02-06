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
package fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst.embeddedmsg;

import fr.gouv.vitam.tools.javalibpst.PSTMessage;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst.PstStoreMessage;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

/**
 * StoreFolder sub-class for mail boxes extracted through libpst library.
 */
public class PstEmbeddedStoreFolder extends StoreFolder {

    // Embedded message
    private PstStoreMessage lpStoreMessage;

    // name and fullName computed from constructors
    private String fullName;
    private String name;

    // for the root folder
    private PstEmbeddedStoreFolder(PSTMessage content, StoreExtractor storeExtractor) {
        super(storeExtractor);

        this.lpStoreMessage = new PstStoreMessage(this, content);
        this.fullName = "";
        this.name = "";
    }

    /**
     * Creates the root folder.
     *
     * @param content
     *            the embedded message
     * @param storeExtractor
     *            the store extractor
     * @param rootArchiveUnit
     *            Root ArchiveUnit
     * @return the LP store folder
     */
    public static PstEmbeddedStoreFolder createRootFolder(PSTMessage content, PstEmbeddedStoreExtractor storeExtractor,
                                                          ArchiveUnit rootArchiveUnit) {
        PstEmbeddedStoreFolder result = new PstEmbeddedStoreFolder(content, storeExtractor);
        result.folderArchiveUnit = rootArchiveUnit;

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#
     * doExtractFolderMessages()
     */
    @Override
    protected void doExtractFolderElements(boolean writeFlag) throws MailExtractLibException, InterruptedException {
        lpStoreMessage.processElement(writeFlag);

        // return to attachment the binary form
        StoreAttachment attachment = ((PstEmbeddedStoreExtractor) storeExtractor).getAttachment();
        attachment.setStoreContent(lpStoreMessage.getMimeContent());
        attachment.setMimeType("message/rfc822");
        if ((attachment.getName() == null) || attachment.getName().isEmpty())
            attachment.setName(lpStoreMessage.getSubject() + ".eml");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doExtractSubFolders(
     * int)
     */
    @Override
    protected void doExtractSubFolders(int level, boolean writeFlag) throws MailExtractLibException {
        // no subfolders
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#getFullName()
     */
    @Override
    public String getFullName() {
        return fullName;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#hasMessages()
     */
    @Override
    public boolean hasElements() throws MailExtractLibException {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#hasSubfolders()
     */
    @Override
    public boolean hasSubfolders() throws MailExtractLibException {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListFolderMessages()
     */
    @Override
    protected void doListFolderElements(boolean stats) throws MailExtractLibException, InterruptedException {
        lpStoreMessage.listElement(stats);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListSubFolders(
     * boolean)
     */
    @Override
    protected void doListSubFolders(boolean stats) throws MailExtractLibException {
        // no subfolder
    }
}
