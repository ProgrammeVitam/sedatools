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

package fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst;

import com.pff.*;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreFolder;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;

/**
 * StoreFolder sub-class for mail boxes extracted through libpst library.
 */
public class PstStoreFolder extends StoreFolder {

    /** Native libpst folder **/
    protected PSTFolder pstFolder;

    // name and fullName computed from constructors
    private String fullName;
    private String name;

    // for the root folder
    private PstStoreFolder(StoreExtractor storeExtractor, PSTFolder pstFolder) {
        super(storeExtractor);
        this.pstFolder = pstFolder;
        this.fullName = "";
        this.name = "";
    }

    // for a folder with a father
    private PstStoreFolder(StoreExtractor storeExtractor, PSTFolder pstFolder, PstStoreFolder father) {
        super(storeExtractor);
        this.pstFolder = pstFolder;
        this.name = pstFolder.getDisplayName();
        if (this.name == null)
            this.name = "";
        if (father.getFullName().isEmpty())
            this.fullName = this.name;
        else
            this.fullName = father.fullName + File.separator + this.name;
        finalizeStoreFolder(father);
    }

    /**
     * Creates the root folder.
     *
     * @param storeExtractor
     *            Operation store extractor
     * @param pstFolder
     *            Root native libpst folder
     * @param rootArchiveUnit
     *            Root ArchiveUnit
     * @return the LP store folder
     */
    public static PstStoreFolder createRootFolder(PstStoreExtractor storeExtractor, PSTFolder pstFolder,
                                                  ArchiveUnit rootArchiveUnit) {
        PstStoreFolder result = new PstStoreFolder(storeExtractor, pstFolder);
        result.folderArchiveUnit = rootArchiveUnit;

        return result;
    }

    private void logMessageWarning(String msg, Exception e) throws InterruptedException {
        if (getStoreExtractor().getOptions().warningMsgProblem)
            doProgressLog(getProgressLogger(), MailExtractProgressLogger.WARNING, msg, e);
        else
            doProgressLog(getProgressLogger(), MailExtractProgressLogger.MESSAGE_DETAILS, msg, e);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#
     * doExtractFolderMessages()
     */
    @Override
    protected void doExtractFolderElements(boolean writeFlag) throws MailExtractLibException, InterruptedException {
        PSTMessage message;
        PSTObject po = null;
        int mes = 0;
        while (true) {
            boolean error;
            do {
                try {
                    mes++;
                    po = pstFolder.getNextChild();
                    error = false;
                } catch (IOException e) {
                    throw new MailExtractLibException("mailextract.pst: can't use pst file", e);
                } catch (PSTException e) {
                    throw new MailExtractLibException("mailextract.pst: can't get messages from folder " + getFullName(), e);
                } catch (Exception e) {
                    logMessageWarning("mailextract.pst: wrongly formatted message " + mes + " in folder " + this.getName(), e);
                    error = true;
                }
            }
            while (error);
            if (po == null)
                break;
            message = (PSTMessage) po;
            if (message instanceof PSTContact) {
                PstStoreContact lPStoreContact = new PstStoreContact(this, (PSTContact) message);
                lPStoreContact.writeToContactsList(writeFlag);
            } else {
                PstStoreMessage lPStoreMessage = new PstStoreMessage(this, message);
                lPStoreMessage.processElement(writeFlag);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doExtractSubFolders(
     * int)
     */
    @Override
    protected void doExtractSubFolders(int level, boolean writeFlag) throws MailExtractLibException, InterruptedException {
        PstStoreFolder lPMailBoxSubFolder;

        try {
            final Vector<PSTFolder> subfolders = pstFolder.getSubFolders();
            for (final PSTFolder subfolder : subfolders) {
                lPMailBoxSubFolder = new PstStoreFolder(storeExtractor, subfolder, this);
                if (lPMailBoxSubFolder.extractFolder(level + 1, writeFlag))
                    incFolderSubFoldersCount();
                dateRange.extendRange(lPMailBoxSubFolder.getDateRange());
            }
        } catch (IOException e) {
            throw new MailExtractLibException("mailextract.pst: can't use pst file", e);
        } catch (PSTException e) {
            throw new MailExtractLibException("mailextract.pst: can't get sub folders from folder " + getFullName(), e);
        }
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
        try {
            return (pstFolder.getEmailCount() > 0) || (pstFolder.getContentCount() > 0);
        } catch (IOException e) {
            throw new MailExtractLibException("mailextract.pst: can't use pst file", e);
        } catch (PSTException e) {
            throw new MailExtractLibException("mailextract.pst: can't get messages from folder " + getFullName(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#hasSubfolders()
     */
    @Override
    public boolean hasSubfolders() throws MailExtractLibException {
        return pstFolder.hasSubfolders();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListFolderMessages()
     */
    @Override
    protected void doListFolderElements(boolean stats) throws MailExtractLibException, InterruptedException {
        PSTMessage message;

        try {
            message = (PSTMessage) pstFolder.getNextChild();
            while (message != null) {
                PstStoreMessage lPStoreMessage = new PstStoreMessage(this, message);
                lPStoreMessage.listElement(stats);
                message = (PSTMessage) pstFolder.getNextChild();
            }
        } catch (IOException e) {
            throw new MailExtractLibException("mailExtract.pst: can't Can't use pst file", e);
        } catch (PSTException e) {
            throw new MailExtractLibException("mailExtract.pst: can't get messages from folder " + getFullName(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MailBoxFolder#doListSubFolders(
     * boolean)
     */
    @Override
    protected void doListSubFolders(boolean stats) throws MailExtractLibException, InterruptedException {
        PstStoreFolder lPMailBoxSubFolder;

        try {
            final Vector<PSTFolder> subfolders = pstFolder.getSubFolders();
            for (final PSTFolder subfolder : subfolders) {
                lPMailBoxSubFolder = new PstStoreFolder(storeExtractor, subfolder, this);
                lPMailBoxSubFolder.listFolder(stats);
                incFolderSubFoldersCount();
            }
        } catch (IOException e) {
            throw new MailExtractLibException("mailextract.pst: can't use pst file", e);
        } catch (PSTException e) {
            throw new MailExtractLibException("mailextract.pst: can't get sub folders from folder " + getFullName(), e);
        }
    }
}
