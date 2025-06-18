/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

/**
 * The change SEDA2 version thread.
 */
public class ChangeSeda2VersionThread extends SwingWorker<String, String> {
    //input
    private int toSeda2Version;
    private DataObjectPackage dop;
    private InOutDialog inOutDialog;

    //run output
    private DataObjectPackage convertedDop;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Instantiates a new Check profile thread.
     *
     * @param toSeda2Version the seda 2 version to change to
     * @param dop            the data object package
     * @param dialog         the dialog
     */
    public ChangeSeda2VersionThread(int toSeda2Version, DataObjectPackage dop, InOutDialog dialog) {
        this.toSeda2Version = toSeda2Version;
        this.dop = dop;
        this.inOutDialog = dialog;
        this.exitThrowable = null;
        this.spl = null;
        dialog.setThread(this);
    }

    @Override
    public String doInBackground() {
        Work work = ResipGraphicApp.getTheApp().currentWork;
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
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), localLogLevel, (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            }, localLogStep, 2,SEDALibProgressLogger.OBJECTS_GROUP,1000);
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());

            if (work == null)
                throw new ResipException("Pas de contenu à transformer");

            convertedDop = SEDA2Version.convertToSeda2Version(dop, toSeda2Version, spl);
        } catch (Throwable e) { //NOSONAR
            exitThrowable = e;
        }
        return "OK";
    }

    @Override
    protected void done() {
        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);

        if (isCancelled())
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: conversion annulée", null);
        else if (exitThrowable != null)
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: erreur durant la conversion", exitThrowable);
        else
            doProgressLogWithoutInterruption(spl, GLOBAL, "resip: conversion OK", null);
    }

    public DataObjectPackage getResult() {
        return convertedDop;
    }

    public Throwable getError() {
        return exitThrowable;
    }
}
