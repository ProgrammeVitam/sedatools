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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.frame.ManifestWindow;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

/**
 * The type Statistic thread.
 */
public class SeeManifestThread extends SwingWorker<String, String> {

    //input
    private Work work;
    private ManifestWindow manifestWindow;
    private InOutDialog inOutDialog;
    //run output
    private String manifestString;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Expand a binary data object replacing it by the archive unit hierarchy, in a dedicated thread.
     *
     * @param work the work
     */
    public static void launchSeeManifestThread(Work work) {
        SeeManifestThread seeManifestThread;

        try {
            ManifestWindow manifestWindow = new ManifestWindow();

            InOutDialog inOutDialog = new InOutDialog(manifestWindow, "Génération du manifest");
            seeManifestThread = new SeeManifestThread(manifestWindow, work, inOutDialog);
            seeManifestThread.execute();
            inOutDialog.setVisible(true);
        } catch (Throwable e) {
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow,
                    "Erreur fatale, impossible de générer le manifest\n->" + e.getMessage(), "Erreur",
                    UserInteractionDialog.ERROR_DIALOG, null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de générer le manifest", e);
        }
    }

    /**
     * Instantiates a new SeeManifest thread.
     */
    public SeeManifestThread(ManifestWindow manifestWindow, Work work, InOutDialog inOutDialog) {
        this.manifestWindow = manifestWindow;
        this.work = work;
        this.inOutDialog = inOutDialog;
    }


    @Override
    public String doInBackground() {
        ArchiveTransfer archiveTransfer = new ArchiveTransfer();
        try {
            int localLogLevel, localLogStep;
            if (ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()) {
                localLogLevel = SEDALibProgressLogger.OBJECTS_WARNINGS;
                localLogStep = 1;
            } else {
                localLogLevel = SEDALibProgressLogger.OBJECTS_GROUP;
                localLogStep = 1000;
            }
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), localLogLevel, (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            }, localLogStep, 2,SEDALibProgressLogger.OBJECTS_GROUP,1000);
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());

            work.getDataObjectPackage().setManagementMetadataXmlData(work.getExportContext().getManagementMetadataXmlData());
            archiveTransfer.setDataObjectPackage(work.getDataObjectPackage());
            archiveTransfer.setGlobalMetadata(work.getExportContext().getArchiveTransferGlobalMetadata());
            if (work.getExportContext().isMetadataFilterFlag())
                work.getDataObjectPackage().setExportMetadataList(work.getExportContext().getKeptMetadataList());
            else
                work.getDataObjectPackage().setExportMetadataList(null);

            ArchiveTransferToSIPExporter sm = new ArchiveTransferToSIPExporter(archiveTransfer, spl);
            manifestString = sm.getSEDAXMLManifest(work.getExportContext().isHierarchicalArchiveUnits(),
                    work.getExportContext().isIndented());
        } catch (Throwable e) {
            exitThrowable = e;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        ResipGraphicApp theApp = ResipGraphicApp.getTheApp();

        if (isCancelled()) {
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: génération du manifest annulée", null);
            manifestWindow.setVisible(false);
            manifestWindow.dispose();
        } else if (exitThrowable != null)
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: erreur durant la génération du manifest", exitThrowable);
        else {
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: manifest généré", null);
            manifestWindow.setText(manifestString);
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: manifest chargé dans le visualisateur XML", null);
            inOutDialog.setVisible(false);
        }
    }
}
