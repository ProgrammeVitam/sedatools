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

import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.event.EventBus;
import fr.gouv.vitam.tools.resip.event.SedaVersionChangedEvent;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.service.ImportService;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog.*;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The type Import thread: runs the {@link ImportService} for the graphic application, and shows
 * its progress and result.
 */
public class ImportThread extends SwingWorker<String, String> {

    //input
    private final CreationContext creationContext;
    private InOutDialog inOutDialog;
    //run output
    private Work work;
    private int fileCounter;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * The SEDA version of the imported work, the one of the interface.
     */
    private final SedaVersion sedaVersion;

    /**
     * The import, which asks the user what to do with an already used temporary directory.
     */
    private final ImportService importService;

    /**
     * Instantiates a new Import thread.
     *
     * @param cc     the cc
     * @param dialog the dialog
     */
    public ImportThread(CreationContext cc, InOutDialog dialog) {
        this.creationContext = cc;
        this.work = null;
        this.sedaVersion = ResipGraphicApp.getTheApp().treatmentParameters.getSedaVersion();
        this.importService = new ImportService(Preferences.getInstance(), this::getTmpDirTarget);
        this.inOutDialog = dialog;
        this.exitThrowable = null;
        this.spl = null;
        dialog.setThread(this);
    }

    private void recursiveDelete(File inFile) throws InterruptedException {
        if (inFile.isDirectory()) {
            for (File f : inFile.listFiles()) recursiveDelete(f);
            inFile.delete(); //NOSONAR use the quickest method
        } else {
            inFile.delete(); //NOSONAR use the quickest method
            fileCounter++;
            doProgressLogIfStep(
                spl,
                SEDALibProgressLogger.OBJECTS_GROUP,
                fileCounter,
                fileCounter + " fichiers effacés"
            );
        }
    }

    private String getTmpDirTarget(String workDir, String subDir) throws ResipException, InterruptedException {
        String target = workDir + File.separator + subDir;
        if (Files.exists(Paths.get(target))) {
            UsedTmpDirDialog utdd = new UsedTmpDirDialog(ResipGraphicApp.getTheWindow(), workDir, subDir);
            utdd.setVisible(true);
            if (utdd.getReturnValue() == STATUS_CLEAN) {
                fileCounter = 0;
                recursiveDelete(new File(utdd.getResult()));
                doProgressLog(spl, SEDALibProgressLogger.STEP, fileCounter + " fichiers effacés au total", null);
                target = utdd.getResult();
            } else if ((utdd.getReturnValue() == STATUS_CONTINUE) || (utdd.getReturnValue() == STATUS_CHANGE)) {
                target = utdd.getResult();
            } else { // STATUS_CANCEL
                this.cancel(false);
                throw new ResipException("Opération annulée");
            }
        }
        return target;
    }

    private static String getImportDescription(CreationContext cc) {
        String source;
        if (cc instanceof ZipImportContext) source = "un fichier zip";
        else if (cc instanceof DiskImportContext) source = "une hiérarchie disque";
        else if (cc instanceof SIPImportContext) source = "un fichier SIP";
        else if (cc instanceof DIPImportContext) source = "un fichier DIP";
        else if (cc instanceof CSVTreeImportContext) source = "un csv d'arbre de classement";
        else if (cc instanceof CSVMetadataImportContext) source = "un csv de métadonnées";
        else if (cc instanceof MailImportContext) source = "un conteneur courriel";
        else source = "une source inconnue";
        return "Import depuis " + source + " en " + cc.getOnDiskInput() + "\n";
    }

    private MailExtractProgressLogger createMailExtractProgressLogger() {
        boolean debugFlag = ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag();
        MailExtractProgressLogger mepl = new MailExtractProgressLogger(
            ResipLogger.getGlobalLogger().getLogger(),
            debugFlag ? MailExtractProgressLogger.MESSAGE_DETAILS : MailExtractProgressLogger.MESSAGE_GROUP,
            (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            },
            debugFlag ? 1 : 1000,
            2,
            MailExtractProgressLogger.MESSAGE_GROUP,
            1000
        );
        mepl.setDebugFlag(debugFlag);
        return mepl;
    }

    @Override
    public String doInBackground() {
        ResipGraphicApp.getTheApp().importThreadRunning = true;
        try {
            int localLogLevel;
            int localLogStep;
            if (ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()) {
                localLogLevel = SEDALibProgressLogger.OBJECTS_WARNINGS;
                localLogStep = 1;
            } else {
                localLogLevel = SEDALibProgressLogger.OBJECTS_GROUP;
                localLogStep = 1000;
            }
            spl = new SEDALibProgressLogger(
                ResipLogger.getGlobalLogger().getLogger(),
                localLogLevel,
                (count, log) -> {
                    String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                    inOutDialog.extProgressTextArea.setText(newLog);
                    inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
                },
                localLogStep,
                2,
                SEDALibProgressLogger.OBJECTS_GROUP,
                1000
            );
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());
            inOutDialog.extProgressTextArea.setText(getImportDescription(creationContext));
            work = importService.importWork(
                creationContext,
                sedaVersion,
                spl,
                creationContext instanceof MailImportContext ? createMailExtractProgressLogger() : null
            );
        } catch (Throwable e) {
            exitThrowable = e;
            work = null;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        ResipGraphicApp theApp = ResipGraphicApp.getTheApp();

        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled()) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "resip: import annulé, les données n'ont pas été modifiées",
            null
        );
        else if (exitThrowable != null) {
            doProgressLogWithoutInterruption(
                spl,
                GLOBAL,
                "resip: erreur durant l'import, les données n'ont pas été modifiées",
                exitThrowable
            );
        } else {
            // a SIP or a DIP is read in the version of its manifest, the interface switches to it
            SedaVersion workSedaVersion = work.getDataObjectPackage().getSedaVersion();
            if (workSedaVersion != sedaVersion) {
                EventBus.publish(new SedaVersionChangedEvent(workSedaVersion));
                theApp.treatmentParameters.toPrefs(Preferences.getInstance());
                doProgressLogWithoutInterruption(
                    spl,
                    GLOBAL,
                    "resip: le manifest est en " +
                    workSedaVersion.displayString() +
                    ", l'interface passe dans cette version",
                    null
                );
            }
            theApp.currentWork = work;
            theApp.setFilenameWork(null);
            theApp.setModifiedContext(true);
            theApp.setContextLoaded(true);
            ResipGraphicApp.getTheWindow().load();
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: import terminé", null);
            doProgressLogWithoutInterruption(spl, GLOBAL, work.getCreationContext().getSummary(), null);
            try {
                Preferences.getInstance().setPrefsImportDirFromChild(work.getCreationContext().getOnDiskInput());
            } catch (ResipException e) {
                doProgressLogWithoutInterruption(
                    spl,
                    GLOBAL,
                    "resip: la localisation d'import par défaut n'a pu être actualisée dans les préférences",
                    e
                );
            }
        }
        theApp.importThreadRunning = false;
    }
}
