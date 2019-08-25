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

import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;

/**
 * StoreElement class for an element (fmessage, contact, appointment...) that can be extracted
 */
public abstract class StoreElement {

    /**
     * The line id in csv list.
     */
    protected int listLineId;

    /**
     * Store folder. containing this element.
     */
    protected StoreFolder storeFolder;

    /**
     * Instantiates a new store leaf.
     *
     * @param storeFolder the store folder
     */
    public StoreElement(StoreFolder storeFolder) {
        this.storeFolder = storeFolder;
        this.listLineId=-1;
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
        return storeFolder.getProgressLogger();
    }

    /**
     * Gets the current operation store extractor.
     *
     * @return storeExtractor store extractor
     */
    public StoreExtractor getStoreExtractor() {
        return storeFolder.getStoreExtractor();
    }

    /**
     * Log at warning or at finest level depending on store extractor options
     * <p>
     * To log a problem on a specific message.
     *
     * @param msg Message to log
     * @param t   the throwable cause
     * @throws InterruptedException the interrupted exception
     */
    public void logMessageWarning(String msg, Throwable t) throws InterruptedException {
        msg+= " for "+ getLogDescription();
        StoreElement fatherElement=getStoreExtractor().getFatherElement();
        while (fatherElement!=null) {
            msg+=" in "+ fatherElement.getLogDescription();
            fatherElement=fatherElement.getStoreExtractor().getFatherElement();
        }

        Exception ex = null;
        if (t instanceof Exception)
            ex = (Exception) t;

        if (storeFolder.getStoreExtractor().options.warningMsgProblem)
            doProgressLog(getProgressLogger(), MailExtractProgressLogger.WARNING, msg, ex);
        else
            doProgressLog(getProgressLogger(), MailExtractProgressLogger.MESSAGE_DETAILS, msg, ex);
    }

    /**
     * Give the best element description to use in logs
     *
     * @return the log description
     */
    abstract public String getLogDescription();

    /**
     * Whole process of extraction on one element (analysis, extraction, count...).
     *
     * @param writeFlag if true, extraction result is disk written
     * @throws InterruptedException    the interrupted exception
     * @throws MailExtractLibException the mail extract lib exception
     */
    abstract public void processElement(boolean writeFlag) throws InterruptedException, MailExtractLibException;

    /**
     * Limited process of listing on one element (analysis, count...).
     *
     * @param statsFlag if true, collect also element data (like size for message)
     * @throws InterruptedException    the interrupted exception
     * @throws MailExtractLibException the mail extract lib exception
     */
    abstract public void listElement(boolean statsFlag) throws InterruptedException, MailExtractLibException;

    /**
     * Filter hyphen for csv string.
     *
     * @param s the s
     * @return the string
     */
    public String filterHyphenForCsv(String s) {
        return s.replace("\"", " ");
    }
}
