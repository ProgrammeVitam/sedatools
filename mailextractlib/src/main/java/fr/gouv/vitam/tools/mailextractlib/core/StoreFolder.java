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

package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.DateRange;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;

/**
 * Abstract class for a store content folder.
 * <p>
 * It defines high-level methods for extracting and listing recursively folders
 * using low-level methods from the non-abstract sub-classes, according to
 * folder inner organisation. Each subclass has to be able to run through
 * folders and to get leafs.
 * <p>
 * The descriptive metadata kept for a folder are:
 * <ul>
 * <li>folder name (Title metadata),</li>
 * <li>the minimum of all the SentDate of leafs in this folder or in one of it's
 * descendant, (StartDate metadata),</li>
 * <li>the maximum of all the SentDate of leafs in this folder or in one of it's
 * descendant, (EndDate metadata),</li>
 * </ul> There's also a DescriptionLevel defined as RecordGrp.
 */
public abstract class StoreFolder {

    /**
     * Operation store extractor.
     * <p>
     * Context for operation on a defined store.
     */
    protected StoreExtractor storeExtractor;

    /**
     * Folder Archive Unit.
     * <p>
     * Context for disk writing this folder metadata.
     */
    protected ArchiveUnit folderArchiveUnit;

    /**
     * Folder date range.
     * <p>
     * It is computed as the min and max of all the dates of leafs in the folder
     * sub-hierarchy.
     */
    protected DateRange dateRange;

    // private fields for folder statistics
    private int folderElementsCount;
    private int folderSubFoldersCount;
    private long folderElementsRawSize;

    /**
     * Instantiates a new store folder.
     *
     * @param storeExtractor the store extractor
     */
    protected StoreFolder(StoreExtractor storeExtractor) {
        this.storeExtractor = storeExtractor;
        this.folderArchiveUnit = null;
        this.dateRange = new DateRange();
    }

    /**
     * Gets element name used for the csv file name construction.
     *
     * @return the element name
     */
    static public String getElementName() {
        return "folders";
    }

    /**
     * Finalize store folder.
     * <p>
     * Called at the end of the subclass StoreFolder construction to add the
     * folder {@link ArchiveUnit} with the name depending on father and on the
     * subclass folder inner name.
     *
     * @param father the father
     */
    protected void finalizeStoreFolder(StoreFolder father) {
        folderArchiveUnit = new ArchiveUnit(storeExtractor, father.folderArchiveUnit, "Folder", getName());

    }

    /**
     * Gets the logger created during the store extractor construction, and used
     * in all mailextract classes.
     *
     * <p>
     * For convenience each class which may have some log actions has it's own
     * getProgressLogger method always returning the store extractor logger.
     *
     * @return logger progress logger
     */
    public MailExtractProgressLogger getProgressLogger() {
        return storeExtractor.getProgressLogger();
    }

    // log at the folder level considering storeExtractor depth
    private void logFolder(String msg) throws InterruptedException {
        if (storeExtractor.isRoot())
            doProgressLog(storeExtractor.getProgressLogger(), MailExtractProgressLogger.FOLDER, msg, null);
        else
            doProgressLog(storeExtractor.getProgressLogger(), MailExtractProgressLogger.MESSAGE_DETAILS, msg, null);
    }

    /**
     * Gets the current operation store extractor.
     *
     * @return storeExtractor store extractor
     */
    public StoreExtractor getStoreExtractor() {
        return storeExtractor;
    }

    /**
     * Gets the date range.
     *
     * @return the date range
     */
    public DateRange getDateRange() {
        return dateRange;
    }

    /**
     * Gets the unit node.
     *
     * @return the unit node
     */
    public ArchiveUnit getArchiveUnit() {
        return folderArchiveUnit;
    }

    /**
     * Gets the full name.
     * <p>
     * It depends on the inner representation of folder in subclasses, so
     * abstract.
     *
     * @return the full name, is empty if operation root folder
     */
    public abstract String getFullName();

    /**
     * Gets the name.
     * <p>
     * It depends on the inner representation of folder in subclasses, so
     * abstract.
     *
     * @return the name, is empty if operation root folder
     */
    public abstract String getName();

    /**
     * Increment folder elements count.
     */
    public void incFolderElementsCount() {
        folderElementsCount++;
    }

    /**
     * Gets the folder elements count.
     *
     * @return the folder elements count
     */
    public int getFolderElementsCount() {
        return folderElementsCount;
    }

    /**
     * Increment the folder subfolders count.
     */
    public void incFolderSubFoldersCount() {
        folderSubFoldersCount++;
    }

    /**
     * Gets the folder subfolders count.
     *
     * @return the folder sub folders count
     */
    public int getFolderSubFoldersCount() {
        return folderSubFoldersCount;
    }

    /**
     * Adds to the folder elements raw size.
     *
     * @param rawSize the elements raw size
     */
    public void addFolderElementsRawSize(long rawSize) {
        folderElementsRawSize += rawSize;
    }

    /**
     * Gets the folder elements raw size.
     * <p>
     * The "raw" size is the sum of the size of elements (messages or files) in
     * this specific folder, not in subfolders.
     *
     * @return the folder elements raw size
     */
    public long getFolderElementsRawSize() {
        return folderElementsRawSize;
    }

    /**
     * Checks if this folder contains elements (messages or files).
     *
     * @return true, if successful
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major             format problems...)
     */
    public abstract boolean hasElements() throws MailExtractLibException;

    /**
     * Checks if this folder contains subfolders.
     *
     * @return true, if successful
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major             format problems...)
     */
    public abstract boolean hasSubfolders() throws MailExtractLibException;

    /**
     * Extract all elements and subfolders as {@link #extractFolder
     * extractFolder}** for the root folder.
     * <p>
     * There's a special treatment of the Unit Node for root metadata,
     * completion and writing being done by the store extractor. This folder is
     * never dropped, .
     *
     * @param writeFlag the write flag
     * @throws MailExtractLibException  Any unrecoverable extraction exception (access trouble, major             format problems...)
     * @throws InterruptedException the interrupted exception
     */
    public void extractFolderAsRoot(boolean writeFlag) throws MailExtractLibException, InterruptedException {
        // log process on folder
        logFolder("mailextractlib: extract folder /");
        // extract all elements in the folder to the unit directory
        extractFolderElements(writeFlag);
        // extract all subfolders in the folder to the unit directory
        extractSubFolders(0, writeFlag);
        if (folderElementsCount + folderSubFoldersCount != 0) {
            // accumulate in store extractor statistics out of recursion
            // if the scheme is not a container one but an embedded message folder counter is not used
            if (StoreExtractor.schemeContainerMap.get(storeExtractor.scheme))
                storeExtractor.incElementCounter(this.getClass());
            storeExtractor.addTotalRawSize(getFolderElementsRawSize());
        } else
            logFolder("mailextractlib: empty extraction");
    }

    /**
     * Extract all elements and subfolders.
     * <p>
     * This is a method where the extraction structure and content is partially
     * defined (see also {@link StoreMessage#extractMessage extractMessage} and
     * {@link StoreExtractor#extractAllFolders extractAllFolders})
     * <p>
     * It writes on the disk, recursively, all the elements and subfolders in
     * this folder. For the detailed structure of extraction see class
     * {@link StoreExtractor}.
     *
     * @param level     distance from the root folder (used for drop options)
     * @param writeFlag the write flag
     * @return true, if not empty
     * @throws MailExtractLibException  Any unrecoverable extraction exception (access trouble, major             format problems...)
     * @throws InterruptedException the interrupted exception
     */
    public boolean extractFolder(int level, boolean writeFlag) throws MailExtractLibException, InterruptedException {
        boolean result = false;

        // log process on folder
        logFolder("mailextractlib: extract folder /" + getFullName());

        // extract all elements in the folder to the unit directory
        extractFolderElements(writeFlag);
        // extract all subfolders in the folder to the unit directory
        extractSubFolders(level, writeFlag);
        if ((folderElementsCount + folderSubFoldersCount != 0) || ((!storeExtractor.options.dropEmptyFolders)
                && !(level == 1 && storeExtractor.options.keepOnlyDeepEmptyFolders))) {
            // get specific folder metadata to the unit
            // compute and add to the folder ArchiveUnit the expected folder
            // metadata
            folderArchiveUnit.addMetadata("DescriptionLevel", "RecordGrp", true);
            folderArchiveUnit.addMetadata("Title", getName(), true);
            if (dateRange.isDefined()) {
                folderArchiveUnit.addMetadata("StartDate", DateRange.getISODateString(dateRange.getStart()), true);
                folderArchiveUnit.addMetadata("EndDate", DateRange.getISODateString(dateRange.getEnd()), true);
            }
            if (writeFlag)
                folderArchiveUnit.write();
            result = true;
            storeExtractor.incElementCounter(this.getClass());
            storeExtractor.addTotalRawSize(getFolderElementsRawSize());
        } else
            logFolder("mailextractlib: empty folder " + getFullName() + " is droped");
        // accumulate in store extractor statistics out of recursion

        return result;
    }

    // encapsulate the subclasses real processing method
    private void extractFolderElements(boolean writeFlag) throws MailExtractLibException, InterruptedException {
        folderElementsCount = 0;
        folderElementsRawSize = 0;
        if (hasElements())
            doExtractFolderElements(writeFlag);
    }

    /**
     * Extract folder elements (extractor specific).
     * <p>
     * It extracts folder elements, count these elements with
     * {@link #incFolderElementsCount incFolderElementsCount}, and accumulate their
     * raw size with {@link #addFolderElementsRawSize addFolderElementsRawSize}.
     *
     * @param writeFlag the write flag
     * @throws MailExtractLibException  Any unrecoverable extraction exception (access trouble, major             format problems...)
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void doExtractFolderElements(boolean writeFlag) throws MailExtractLibException, InterruptedException;

    // encapsulate the subclasses real processing method
    private void extractSubFolders(int level, boolean writeFlag) throws MailExtractLibException, InterruptedException {
        folderSubFoldersCount = 0;
        if (hasSubfolders())
            doExtractSubFolders(level, writeFlag);
    }

    /**
     * Extract subfolders (protocol specific).
     * <p>
     * It recursively call {@link #extractFolder extractFolder} and count
     * extracted sub folders with {@link #incFolderSubFoldersCount
     * incFolderSubFoldersCount}**. Must implement drop options.
     *
     * @param level     distance from the root folder (used for drop options)
     * @param writeFlag the write flag
     * @throws MailExtractLibException  Any unrecoverable extraction exception (access trouble, major             format problems...)
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void doExtractSubFolders(int level, boolean writeFlag) throws MailExtractLibException, InterruptedException;

    /**
     * List all folders with or without statistics.
     * <p>
     * It lists on the console, one by line, the complete list of folders from
     * the root folder defined in store extractor. If statistics are required,
     * at the beginning of each line is added the number of elements directly in
     * the folder and the raw size of theese elements.
     *
     * @param stats if true, add folder statistics
     * @throws MailExtractLibException  Any unrecoverable extraction exception (access trouble, major             format problems...)
     * @throws InterruptedException the interrupted exception
     */
    public void listFolder(boolean stats) throws MailExtractLibException, InterruptedException {
        // define a specific name "/" for the root folder
        String fullName, tmp;

        fullName = getFullName();
        if (fullName == null || fullName.isEmpty())
            fullName = "";
        // log process on folder
        logFolder("mailextractlib: list folder /" + fullName);
        if (stats) {
            // inspect all elements in the folder for statistics
            listFolderElements(stats);
            // expose this folder statistics
            tmp = String.format("%5d éléments   %7.2f MBytes    /%s", folderElementsCount,
                    ((double) folderElementsRawSize) / (1024.0 * 1024.0), fullName);
            System.out.println(tmp);
            logFolder("mailextractlib: " + tmp);
        } else {
            System.out.println("/" + fullName);
        }
        // extract all subfolders in the folder to the unit directory
        listSubFolders(stats);

        // accumulate in store extractor statistics out of recursion
        storeExtractor.incElementCounter(this.getClass());
        if (stats) {
            storeExtractor.addTotalRawSize(getFolderElementsRawSize());
        }
    }

    // encapsulate the subclasses real processing method
    private void listFolderElements(boolean stats) throws MailExtractLibException, InterruptedException {
        folderElementsCount = 0;
        folderElementsRawSize = 0;
        if (hasElements())
            doListFolderElements(stats);
    }

    /**
     * List folder elements (protocol specific).
     * <p>
     * If stats where asked for, this method is invoked for counting elements
     * with {@link #incFolderElementsCount incFolderElementsCount}, and
     * accumulating raw size with {@link #addFolderElementsRawSize
     * addFolderElementsRawSize}**.
     *
     * @param stats the stats
     * @throws MailExtractLibException  Any unrecoverable extraction exception (access trouble, major             format problems...)
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void doListFolderElements(boolean stats) throws MailExtractLibException, InterruptedException;

    // encapsulate the subclasses real processing method
    private void listSubFolders(boolean stats) throws MailExtractLibException, InterruptedException {
        folderSubFoldersCount = 0;
        if (hasSubfolders())
            doListSubFolders(stats);
    }

    /**
     * List subfolders (protocol specific).
     * <p>
     * It recursively call {@link #listFolder listFolder} and count sub folders
     * with {@link #incFolderSubFoldersCount incFolderSubFoldersCount}.
     *
     * @param stats the stats
     * @throws MailExtractLibException  Any unrecoverable extraction exception (access trouble, major             format problems...)
     * @throws InterruptedException the interrupted exception
     */
    protected abstract void doListSubFolders(boolean stats) throws MailExtractLibException, InterruptedException;

}
