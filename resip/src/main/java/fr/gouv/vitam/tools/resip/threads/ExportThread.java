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
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.service.ExportService;
import fr.gouv.vitam.tools.resip.service.ExportService.ExportType;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The type Export thread.
 */
public class ExportThread extends SwingWorker<String, String> {

    /**
     * The Sip all export.
     */
    //input
    private Work work;
    private InOutDialog inOutDialog;
    private ExportType exportType;
    //run output
    private String summary;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Instantiates a new Export thread.
     *
     * @param work       the work
     * @param exportType the export type
     * @param dialog     the dialog
     */
    public ExportThread(Work work, ExportType exportType, InOutDialog dialog) {
        this.work = work;
        this.exportType = exportType;
        dialog.setThread(this);
        this.inOutDialog = dialog;
        this.exitThrowable = null;
    }

    private static String getExportDescription(ExportType exportType, Work work) {
        String output = work.getExportContext().getOnDiskOutput();
        switch (exportType) {
            case SIP_MANIFEST:
                return "Export du manifest SEDA en " + output + "\n";
            case SIP_ALL:
                return "Export du SIP SEDA en " + output + "\n";
            case DISK:
                return "Export en hiérarchie disque complète en " + output + "\n";
            case CSV_ALL_ZIP:
                return (
                    "Export en hiérarchie disque simplifiée dans un zip avec fichier csv des métadonnées " +
                    output +
                    "\n"
                );
            default:
                return "Export en hiérarchie disque simplifiée avec fichier csv des métadonnées " + output + "\n";
        }
    }

    @Override
    public String doInBackground() {
        spl = null;
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

            inOutDialog.extProgressTextArea.setText(getExportDescription(exportType, work));
            summary = new ExportService(Preferences.getInstance()).export(work, exportType, spl);
            if (work.getExportContext().isReindex()) ResipGraphicApp.mainWindow.treePane.allTreeChanged();
        } catch (Throwable e) {
            exitThrowable = e;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled()) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "resip: export annulé, les données seront partiellement sur le disque",
            null
        );
        else if (exitThrowable != null) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "resip: erreur durant l'export, les données seront partiellement sur le disque",
            exitThrowable
        );
        else {
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: export terminé", null);
            doProgressLogWithoutInterruption(spl, GLOBAL, summary, null);
            try {
                Preferences.getInstance().setPrefsExportDirFromChild(work.getExportContext().getOnDiskOutput());
            } catch (ResipException e) {
                doProgressLogWithoutInterruption(
                    spl,
                    GLOBAL,
                    "resip: la localisation d'export par défaut n'a pu être actualisée dans les préférences",
                    e
                );
            }
        }
    }
}
