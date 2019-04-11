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
package fr.gouv.vitam.tools.resip.app;

import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;

public class CleanThread extends SwingWorker<String, String> {

    private String workDir;
    Exception exitException;
    JTextArea logTextArea;
    SEDALibProgressLogger spl;
    InOutDialog inOutDialog;

    CleanThread(String workDir, InOutDialog dialog) {
        this.workDir = workDir;
        this.logTextArea = dialog.extProgressTextArea;
        dialog.setThread(this);
        this.inOutDialog = dialog;
    }

    static void delete(File inFile) throws IOException, InterruptedException {
        if (inFile.isDirectory()) {
            for (File f : inFile.listFiles())
                delete(f);
            inFile.delete();
        } else {
            inFile.delete();
            Thread.sleep(1);
        }
    }

    @Override
    public String doInBackground() {
        SEDALibProgressLogger spl = null;
        try {
            if (logTextArea != null)
                spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP, (count, log) -> {
                    String newLog = logTextArea.getText() + "\n" + log;
                    logTextArea.setText(newLog);
                    logTextArea.setCaretPosition(newLog.length());
                }, 100);
            else
                spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP);

            spl.progressLog(GLOBAL, "Nettoyage du répertoire: " + workDir);
            for (File f : new File(workDir).listFiles()) {
                if (f.isDirectory() && f.toString().endsWith("-tmpdir")) {
                    spl.progressLog(GLOBAL, "  Sous-répertoire pris en compte: " + f.toString());
                    delete(f);
                    spl.progressLog(GLOBAL, "       -> terminé");
                }
            }
            spl.progressLog(GLOBAL, "    -> nettoyage terminé");
            return "OK";
        } catch (Exception e) {
            if (spl != null) {
                try {
                    spl.progressLog(GLOBAL, "Erreur pendant le nettoyage du " +
                            "répertoire de travail, les fichiers sont partiellement effacés.");
                } catch (InterruptedException ignored) {
                }
            }
            exitException = e;
            e.printStackTrace();
            return "KO";
        }
    }

    @Override
    protected void done() {
        JTextArea loadText = inOutDialog.extProgressTextArea;
        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled())
            loadText.setText(loadText.getText() + "\n-> " + "Nettoyage annulé, les fichiers sont partiellement effacées.");
    }
}
