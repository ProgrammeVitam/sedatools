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
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.service.DigestService;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.digest.DigestComputer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

/**
 * Recomputes the digests of the BinaryDataObjects of the work, or of a sub-tree of it, in a
 * dedicated thread.
 */
public class DigestThread extends SwingWorker<String, String> {

    //input
    private final Work work;
    private final ArchiveUnit from;
    private final String algorithm;
    private final InOutDialog inOutDialog;
    //run output
    private DigestService.DigestResult result;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Asks the digest algorithm, then recomputes the digests of the work or of a sub-tree of it, in a
     * dedicated thread.
     *
     * @param from the ArchiveUnit of the sub-tree, or null for the whole work
     */
    public static void launchDigestThread(ArchiveUnit from) {
        try {
            Work work = ResipGraphicApp.getTheApp().currentWork;
            if (work == null) return;
            String algorithm = askAlgorithm(work.getDataObjectPackage().getDigestAlgorithm(), from);
            if (algorithm == null) return;
            InOutDialog inOutDialog = new InOutDialog(ResipGraphicApp.mainWindow, "Calcul des empreintes");
            DigestThread digestThread = new DigestThread(work, from, algorithm, inOutDialog);
            digestThread.execute();
            inOutDialog.setVisible(true);
        } catch (Throwable e) {
            UserInteractionDialog.getUserAnswer(
                ResipGraphicApp.mainWindow,
                "Erreur fatale, impossible de calculer les empreintes \n->" + e.getMessage(),
                "Erreur",
                UserInteractionDialog.ERROR_DIALOG,
                null
            );
            ResipLogger.getGlobalLogger()
                .log(ResipLogger.ERROR, "Erreur fatale, impossible de calculer les empreintes", e);
        }
    }

    // asks the algorithm, the one of the work by default; any one Java can compute is accepted, with a
    // warning when Vitam doesn't accept it
    private static String askAlgorithm(String workAlgorithm, ArchiveUnit from) {
        List<String> algorithms = new ArrayList<>(DigestComputer.PROPOSED_ALGORITHMS);
        for (String vitamAlgorithm : DigestComputer.VITAM_ALGORITHMS) {
            if (!algorithms.contains(vitamAlgorithm)) algorithms.add(vitamAlgorithm);
        }
        JComboBox<String> comboBox = new JComboBox<>(algorithms.toArray(new String[0]));
        comboBox.setEditable(true);
        comboBox.setSelectedItem(workAlgorithm);
        JLabel warningLabel = new JLabel(" ");
        warningLabel.setForeground(new Color(0xB3, 0x5C, 0x00));
        Runnable updateWarning = () -> {
            List<String> warnings = DigestComputer.getAlgorithmWarnings(typedAlgorithm(comboBox));
            warningLabel.setText(warnings.isEmpty() ? " " : "Avertissement : " + String.join(" ; ", warnings));
        };
        updateWarning.run();
        comboBox.addActionListener(arg -> updateWarning.run());
        ((JTextField) comboBox.getEditor().getEditorComponent()).getDocument()
            .addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateWarning.run();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateWarning.run();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateWarning.run();
                    }
                }
            );

        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 5));
        panel.add(
            new JLabel(
                from == null
                    ? "Recalcul des empreintes de tous les objets du travail (algorithme actuel : " +
                    workAlgorithm +
                    ")."
                    : "Recalcul des empreintes des objets de l'ArchiveUnit " +
                    from.getInDataObjectPackageId() +
                    " et de ses descendants."
            )
        );
        panel.add(
            new JLabel(
                from == null
                    ? "Un autre algorithme devient celui du travail pour les empreintes calculées ensuite."
                    : "L'algorithme du travail reste " + workAlgorithm + "."
            )
        );
        panel.add(
            new JLabel("Seules les empreintes sont modifiées (ni taille, ni format, ni informations de fichier).")
        );
        panel.add(comboBox);
        panel.add(warningLabel);
        int answer = JOptionPane.showConfirmDialog(
            ResipGraphicApp.mainWindow,
            panel,
            "Calcul des empreintes",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (answer != JOptionPane.OK_OPTION) return null;
        String algorithm = typedAlgorithm(comboBox);
        return (algorithm == null) || algorithm.isEmpty() ? workAlgorithm : algorithm;
    }

    private static String typedAlgorithm(JComboBox<String> comboBox) {
        Object typed = comboBox.getEditor().getItem();
        return typed == null ? null : typed.toString().trim();
    }

    /**
     * Instantiates a new digest thread.
     *
     * @param work      the work
     * @param from      the ArchiveUnit of the sub-tree, or null for the whole work
     * @param algorithm the digest algorithm
     * @param dialog    the dialog
     */
    public DigestThread(Work work, ArchiveUnit from, String algorithm, InOutDialog dialog) {
        this.work = work;
        this.from = from;
        this.algorithm = algorithm;
        this.inOutDialog = dialog;
        this.exitThrowable = null;
        dialog.setThread(this);
    }

    @Override
    public String doInBackground() {
        while (ResipGraphicApp.getTheApp().addThreadRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                exitThrowable = e;
                Thread.currentThread().interrupt();
                return "KO";
            }
        }
        ResipGraphicApp.getTheApp().addThreadRunning = true;
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

            result = DigestService.computeDigests(work.getDataObjectPackage(), from, algorithm, spl);
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
            "Calcul des empreintes annulé, une partie des empreintes a pu être recalculée",
            null
        );
        else if (exitThrowable != null) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "Erreur durant le calcul des empreintes",
            exitThrowable
        );
        else {
            doProgressLogWithoutInterruption(spl, GLOBAL, "Calcul des empreintes terminé", null);
            List<String> warnings = DigestService.getDigestWarnings(work.getDataObjectPackage());
            if (!warnings.isEmpty()) doProgressLogWithoutInterruption(
                spl,
                GLOBAL,
                "Avertissement : " + warnings.size() + " anomalie(s) d'empreinte\n" + String.join("\n", warnings),
                null
            );
        }
        // even cancelled, some digests may have changed; the tree is unchanged, so the selection is kept
        if ((result != null) || isCancelled()) {
            ResipGraphicApp.getTheApp().setModifiedContext(true);
            ResipGraphicApp.mainWindow.treePane.refreshDisplayedArchiveUnit();
        }
        ResipGraphicApp.getTheApp().addThreadRunning = false;
    }
}
