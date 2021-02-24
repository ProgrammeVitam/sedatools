package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

public class CheckEndDateThread  extends SwingWorker<String, InOutDialog> {
    //input
    private String profileFileName;
    private InOutDialog inOutDialog;
    //run output
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Instantiates a new Check profile thread.
     *
     * @param profileFileName the profile file name
     * @param dialog          the dialog
     */
    public CheckEndDateThread(String profileFileName, InOutDialog dialog) {
        this.profileFileName = profileFileName;
        this.inOutDialog = dialog;
        this.exitThrowable = null;
        this.spl = null;
        dialog.setThread(this);
    }

    @Override
    public String doInBackground() {
        Work work = ResipGraphicApp.getTheApp().currentWork;
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
            }, localLogStep, 2);
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());

            if (work == null)
                throw new ResipException("Pas de contenu à valider");

            // first verify and reindex if neccesary
            if (work.getExportContext().isReindex()) {
                work.getDataObjectPackage().regenerateContinuousIds();
                ResipGraphicApp.getTheApp().mainWindow.treePane.allTreeChanged();
            }


            HashMap<String, ArchiveUnit> auInDataObjectPackageIdMap =
                work.getDataObjectPackage().getAuInDataObjectPackageIdMap();

            boolean isError = false;
            for (Map.Entry<String, ArchiveUnit> entry : auInDataObjectPackageIdMap.entrySet()) {
                ArchiveUnit archiveUnit = entry.getValue();
                String startDateString = archiveUnit.getContent().getSimpleMetadata("StartDate");
                String endDateString = archiveUnit.getContent().getSimpleMetadata("EndDate");
                if(endDateString != null && startDateString != null) {
                    Date startDate = ISODateTimeFormat.dateTimeParser().parseDateTime(startDateString).toDate();
                    Date endDate = ISODateTimeFormat.dateTimeParser().parseDateTime(endDateString).toDate();

                    if (startDate.after(endDate)) {
                        doProgressLogWithoutInterruption(spl, GLOBAL, "resip: l'unité archivistique " + entry.getKey() +
                            " contient une erreur : la date de fin est antérieure à la date de début", null);
                        isError = true;
                    }
                }
            }
            if (isError) {
                throw new Exception("Erreur durant la validation des dates de fin.");
            }
        } catch (Throwable e) {
            exitThrowable = e;
        }
        return "OK";
    }

    @Override
    protected void done() {
        JTextArea progressTextArea = inOutDialog.extProgressTextArea;
        ResipGraphicApp theApp = ResipGraphicApp.getTheApp();

        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled())
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: validation annulée", null);
        else if (exitThrowable != null)
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: erreur durant la validation", exitThrowable);
        else
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: validation OK", null);
    }
}
