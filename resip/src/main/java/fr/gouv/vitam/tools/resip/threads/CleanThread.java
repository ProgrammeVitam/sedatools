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
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The type Clean thread.
 */
public class CleanThread extends SwingWorker<String, String> {

    //input
    private String workDir;
    private InOutDialog inOutDialog;
    //run output
    private int fileCounter;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Instantiates a new Clean thread.
     *
     * @param workDir the work dir
     * @param dialog  the dialog
     */
    public CleanThread(String workDir, InOutDialog dialog) {
        this.workDir = workDir;
        dialog.setThread(this);
        this.inOutDialog = dialog;
        this.spl = new ThreadLoggerFactory(
            inOutDialog.extProgressTextArea,
            ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()
        ).getLogger();
    }

    private void recursiveDelete(File inFile) throws IOException, InterruptedException {
        if (inFile.isDirectory()) {
            for (File f : inFile.listFiles()) recursiveDelete(f);
            inFile.delete();
        } else {
            inFile.delete();
            fileCounter++;
            doProgressLogIfStep(spl, OBJECTS_GROUP, fileCounter, fileCounter + " fichiers effacés");
        }
    }

    @Override
    public String doInBackground() {
        fileCounter = 0;
        try {
            doProgressLog(spl, GLOBAL, "Nettoyage du répertoire: " + workDir, null);
            for (File f : new File(workDir).listFiles()) {
                if (f.isDirectory() && f.toString().endsWith("-tmpdir")) {
                    fileCounter = 0;
                    doProgressLog(spl, STEP, "  - Sous-répertoire pris en compte: " + f.toString(), null);
                    recursiveDelete(f);
                    doProgressLog(spl, STEP, "    Terminé, " + fileCounter + " effacés", null);
                }
            }
        } catch (Throwable e) {
            exitThrowable = e;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        JTextArea loadText = inOutDialog.extProgressTextArea;
        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled()) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "Nettoyage annulé, les fichiers sont partiellement effacées",
            null
        );
        else if (exitThrowable != null) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "Erreur durant le nettoyage du " + "répertoire de travail, les fichiers sont partiellement effacés",
            exitThrowable
        );
        else doProgressLogWithoutInterruption(spl, GLOBAL, "Nettoyage terminé", null);
    }
}
