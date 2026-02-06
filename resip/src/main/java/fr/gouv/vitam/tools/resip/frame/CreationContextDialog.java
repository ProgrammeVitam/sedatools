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
package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.parameters.SIPImportContext;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * The class CreationContextDialog.
 * <p>
 * Class for creation context informations visualisation dialog.
 */
public class CreationContextDialog extends JDialog {

    // Dialog test context

    /**
     * The entry point of dialog test.
     *
     * @param args the input arguments
     * @throws ClassNotFoundException          the class not found exception
     * @throws UnsupportedLookAndFeelException the unsupported look and feel exception
     * @throws InstantiationException          the instantiation exception
     * @throws IllegalAccessException          the illegal access exception
     * @throws NoSuchMethodException           the no such method exception
     * @throws InvocationTargetException       the invocation target exception
     */
    public static void main(String[] args)
        throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        TestDialogWindow window = new TestDialogWindow(CreationContextDialog.class);
    }

    /**
     * Instantiates a new CreationContextDialog for test.
     *
     * @param owner the owner
     */
    public CreationContextDialog(JFrame owner) {
        this(owner, new DiskImportContext(Preferences.getInstance()), new DataObjectPackage());
    }

    /**
     * Create the dialog.
     *
     * @param owner             the owner
     * @param creationContext   the creation context
     * @param dataObjectPackage the data object package
     */
    public CreationContextDialog(JFrame owner, CreationContext creationContext, DataObjectPackage dataObjectPackage) {
        super(owner, "Visualisation du contexte d'import", true);
        GridBagConstraints gbc;
        setMinimumSize(new Dimension(400, 200));
        setPreferredSize(new Dimension(700, 400));

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[] { 1.0, 0.0 };
        gridBagLayout.columnWeights = new double[] { 1.0 };
        contentPane.setLayout(gridBagLayout);

        // Parameters Panel
        JPanel parametersPanel = new JPanel();
        GridBagLayout gbl_parametersPanel = new GridBagLayout();
        parametersPanel.setLayout(gbl_parametersPanel);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPane.add(parametersPanel, gbc);

        JLabel parametersLabel = new JLabel("Résumé :");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        parametersPanel.add(parametersLabel, gbc);

        JScrollPane scrollPane_1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        parametersPanel.add(scrollPane_1, gbc);

        JTextArea importTypeTextArea = new JTextArea();
        importTypeTextArea.setFont(MainWindow.LABEL_FONT);
        importTypeTextArea.setEditable(false);
        scrollPane_1.setViewportView(importTypeTextArea);
        importTypeTextArea.setText(creationContext.getActualisedSummary(dataObjectPackage));
        importTypeTextArea.setCaretPosition(0);

        if ((creationContext instanceof DiskImportContext) || (creationContext instanceof SIPImportContext)) {
            JLabel workDirLabel = new JLabel("Répertoire de travail :");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(0, 0, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 1;
            parametersPanel.add(workDirLabel, gbc);

            JTextArea workDirTextArea = new JTextArea(2, 60);
            workDirTextArea.setFont(MainWindow.LABEL_FONT);
            workDirTextArea.setEditable(false);
            workDirTextArea.setBorder(scrollPane_1.getBorder());
            workDirTextArea.setText(creationContext.getWorkDir());
            workDirTextArea.setCaretPosition(0);
            workDirTextArea.setLineWrap(true);
            gbc = new GridBagConstraints();
            gbc.weightx = 1.0;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(0, 0, 5, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 1;
            gbc.gridy = 1;
            parametersPanel.add(workDirTextArea, gbc);
            workDirTextArea.setColumns(10);

            if (creationContext instanceof DiskImportContext) {
                JScrollPane ignorePatternsSrollPane = new JScrollPane();
                gbc = new GridBagConstraints();
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.gridheight = 2;
                gbc.gridx = 1;
                gbc.gridy = 2;
                parametersPanel.add(ignorePatternsSrollPane, gbc);

                JTextArea ignorePatternsTextArea = new JTextArea();
                ignorePatternsTextArea.setFont(MainWindow.LABEL_FONT);
                ignorePatternsTextArea.setEditable(false);
                ignorePatternsTextArea.setCaretPosition(0);
                ignorePatternsTextArea.setLineWrap(true);
                ignorePatternsSrollPane.setViewportView(ignorePatternsTextArea);

                JLabel ignorePatternsLabel = new JLabel("Fichiers exclus des imports :");
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.EAST;
                gbc.insets = new Insets(0, 0, 0, 5);
                gbc.gridx = 0;
                gbc.gridy = 2;
                parametersPanel.add(ignorePatternsLabel, gbc);

                if (((DiskImportContext) creationContext).getIgnorePatternList() != null) {
                    StringBuilder sb = new StringBuilder();
                    for (String p : ((DiskImportContext) creationContext).getIgnorePatternList()) {
                        sb.append(p).append('\n');
                    }
                    ignorePatternsTextArea.setText(sb.toString().trim());
                }
            }
        }
        // Buttons
        JButton cancelButton = new JButton("Fermer");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPane.add(cancelButton, gbc);
        cancelButton.addActionListener(arg -> buttonClose());

        pack();
        setLocationRelativeTo(owner);
    }

    // actions

    private void buttonClose() {
        setVisible(false);
    }
}
