package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.frame.VerifyDateDialog;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.joda.time.format.ISODateTimeFormat;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

public class CheckEndDateThread  extends SwingWorker<String, InOutDialog> {
    private final VerifyDateDialog verifyDateDialog;
    //run output
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Instantiates a new Check profile thread.
     *
     * @param dialog          the dialog
     */
    public CheckEndDateThread(VerifyDateDialog dialog) {
        //input
        this.verifyDateDialog = dialog;
        this.exitThrowable = null;
        this.spl = null;
    }

    @Override
    public String doInBackground() {
        verifyDateDialog.setSearchArchiveUnitResult(new ArrayList<>());
        verifyDateDialog.getExtProgressTextArea().setText("");
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
                String newLog = verifyDateDialog.getExtProgressTextArea().getText() + "\n" + log;
                verifyDateDialog.getExtProgressTextArea().setText(newLog);
                verifyDateDialog.getExtProgressTextArea().setCaretPosition(newLog.length());
            }, localLogStep, 2,SEDALibProgressLogger.OBJECTS_GROUP,1000);
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());

            if (work == null)
                throw new ResipException("Pas de contenu à valider");

            // first verify and reindex if neccesary
            if (work.getExportContext().isReindex()) {
                work.getDataObjectPackage().regenerateContinuousIds();
                ResipGraphicApp.mainWindow.treePane.allTreeChanged();
            }


            HashMap<String, ArchiveUnit> auInDataObjectPackageIdMap =
                work.getDataObjectPackage().getAuInDataObjectPackageIdMap();

            List<ArchiveUnit> searchArchiveUnitResult = new ArrayList<>();
            boolean isError = false;
            for (Map.Entry<String, ArchiveUnit> entry : auInDataObjectPackageIdMap.entrySet()) {
                ArchiveUnit archiveUnit = entry.getValue();
                String startDateString = archiveUnit.getContent().getSimpleMetadata("StartDate");
                String endDateString = archiveUnit.getContent().getSimpleMetadata("EndDate");
                if(endDateString != null && startDateString != null) {
                    Date startDate = ISODateTimeFormat.dateTimeParser().parseDateTime(startDateString).toDate();
                    Date endDate = ISODateTimeFormat.dateTimeParser().parseDateTime(endDateString).toDate();

                    if (startDate.after(endDate)) {
                        searchArchiveUnitResult.add(archiveUnit);
                        doProgressLogWithoutInterruption(spl, GLOBAL, "resip: l'unité archivistique " + entry.getKey() +
                            " contient une erreur : la date de fin est antérieure à la date de début", null);
                        isError = true;
                    }
                }
            }
            verifyDateDialog.setSearchArchiveUnitResult(searchArchiveUnitResult);
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
        if (isCancelled())
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: validation annulée", null);
        else if (exitThrowable != null)
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: erreur durant la validation", exitThrowable);
        else
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: validation OK", null);
    }
}
