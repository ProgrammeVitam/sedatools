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
package fr.gouv.vitam.tools.mailextract;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLogWithoutInterruption;

/**
 * MailExtractThread class for the real extraction command
 * <p>
 * Thread for one extraction treatment writing to stdout in the console text area.
 */
public class MailExtractThread extends Thread {

    /**
     * The main window.
     */
    private MailExtractMainWindow mainWindow;

    /**
     * The action number.
     */
    private int actionNumber;

    /**
     * The store extractor.
     */
    private StoreExtractor storeExtractor;

    /**
     * The debug flag.
     */
    private boolean debugFlag;

    /**
     * The thread logger.
     */
    private MailExtractLogger mel;

    /**
     * The mailextract library logger.
     */
    private MailExtractProgressLogger logger;

    /**
     * Instantiates a new mail extract thread.
     *
     * @param mainWindow            the main window
     * @param actionNumber          the action number (LIST, STAT, EXTRACT)
     * @param protocol              the protocol
     * @param host                  the host
     * @param port                  the port
     * @param user                  the user
     * @param password              the password
     * @param container             the container
     * @param folder                the folder
     * @param destRootPath          the dest root path
     * @param destName              the dest name
     * @param storeExtractorOptions the store extractor options
     * @param logLevel              the log level
     * @param debugFlag             the debug flag
     */
    public MailExtractThread(MailExtractMainWindow mainWindow, int actionNumber, String protocol, String host, int port, String user, String password,
                             String container, String folder, String destRootPath, String destName,
                             StoreExtractorOptions storeExtractorOptions, String logLevel, boolean debugFlag) {
        this.mainWindow = mainWindow;
        try {
            mel = new MailExtractLogger(destRootPath + File.separator + destName + ".log", MailExtractLogger.getLevel(logLevel));
            logger = new MailExtractProgressLogger(mel.getProgressLogger(), MailExtractLogger.getLevel(logLevel), (count, log) -> {
                String newLog = mainWindow.consoleTextArea.getText() + "\n" + log;
                mainWindow.consoleTextArea.setText(newLog);
                mainWindow.consoleTextArea.setCaretPosition(newLog.length());
            }, 1000,2,MailExtractProgressLogger.MESSAGE_GROUP,1000);
            logger.setDebugFlag(debugFlag);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // do the job, creating a store extractor and running the extraction
        try {
            String urlString = StoreExtractor.composeStoreURL(protocol, host, user, password, container);
            this.storeExtractor = StoreExtractor.createStoreExtractor(urlString, folder,
                    Paths.get(destRootPath, destName).toString(), storeExtractorOptions, logger);
            this.actionNumber = actionNumber;
        } catch (MailExtractLibException ee) {
            doProgressLogWithoutInterruption(logger, GLOBAL, "mailextract: erreur d'extraction", ee);
        } catch (Exception e) {
            this.actionNumber = 0;
            doProgressLogWithoutInterruption(logger, GLOBAL, "mailextract: erreur générale", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    public void run() {
        try {
            switch (actionNumber) {
                case MailExtractGraphicApp.LIST_ACTION:
                    storeExtractor.listAllFolders(false);
                    break;
                case MailExtractGraphicApp.STAT_ACTION:
                    storeExtractor.listAllFolders(true);
                    break;
                case MailExtractGraphicApp.EXTRACT_ACTION:
                    if (storeExtractor.hasDestName())
                        storeExtractor.extractAllFolders();
                    else
                        throw new MailExtractLibException("mailextract: no destination name for extraction", null);
                    break;

            }
        } catch (MailExtractLibException ee) {
            doProgressLogWithoutInterruption(logger, GLOBAL, "mailextract: extraction error", ee);
        } catch (InterruptedException ie) {
            doProgressLogWithoutInterruption(logger, GLOBAL, "mailextract: extraction process interrupted", ie);
        } catch (Exception e) {
            doProgressLogWithoutInterruption(logger, GLOBAL, "mailextract: error", e);
            System.out.println(getPrintStackTrace(e));
        } finally {
            try {
                if (storeExtractor != null)
                    storeExtractor.endStoreExtractor();
            } catch (MailExtractLibException e) {
                doProgressLogWithoutInterruption(logger, GLOBAL, "mailextract: closing error", e);
            }
            if (logger != null)
                logger.close();
            if (mel != null)
                mel.close();
        }
    }

    // make a String from the stack trace
    private final static String getPrintStackTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter p = new PrintWriter(baos);

        e.printStackTrace(p);
        p.close();
        return baos.toString();
    }

}
