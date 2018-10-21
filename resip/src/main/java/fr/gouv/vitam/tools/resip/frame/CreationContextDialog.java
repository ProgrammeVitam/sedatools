/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.resip.frame;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.SIPImportContext;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;

/**
 * The Class CreationContextDialog.
 */
public class CreationContextDialog extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4092514078236156034L;

	/** The return value. */
	public int returnValue;

	/**
	 * Create the dialog.
	 *
	 * @param owner the owner
	 * @param creationContext the creation context
	 */
	public CreationContextDialog(JFrame owner, CreationContext creationContext, DataObjectPackage dataObjectPackage) {
		super(owner, "Visualisation du contexte d'import", true);
		setMinimumSize(new Dimension(600, 50));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 1.0, 0.1 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		getContentPane().setLayout(gridBagLayout);

		// Parameters Panel

		JPanel parametersPanel = new JPanel();
		parametersPanel.setBorder(null);
		GridBagLayout gbl_parametersPanel = new GridBagLayout();
		gbl_parametersPanel.columnWeights = new double[] { 0.1, 0.1, 1.0 };
		parametersPanel.setLayout(gbl_parametersPanel);
		GridBagConstraints gbc_parametersPanel = new GridBagConstraints();
		gbc_parametersPanel.anchor = GridBagConstraints.EAST;
		gbc_parametersPanel.fill = GridBagConstraints.BOTH;
		gbc_parametersPanel.insets = new Insets(5, 5, 5, 5);
		gbc_parametersPanel.gridwidth = 2;
		gbc_parametersPanel.gridx = 0;
		gbc_parametersPanel.gridy = 0;
		getContentPane().add(parametersPanel, gbc_parametersPanel);

		JLabel parametersLabel = new JLabel("Résumé:");
		GridBagConstraints gbc_parametersLabel = new GridBagConstraints();
		gbc_parametersLabel.insets = new Insets(0, 0, 5, 5);
		gbc_parametersLabel.anchor = GridBagConstraints.EAST;
		gbc_parametersLabel.gridx = 0;
		gbc_parametersLabel.gridy = 0;
		parametersPanel.add(parametersLabel, gbc_parametersLabel);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.weightx = 1.0;
		gbc_scrollPane_1.weighty = 1.0;
		gbc_scrollPane_1.gridwidth = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 0;
		parametersPanel.add(scrollPane_1, gbc_scrollPane_1);

		JTextArea importTypeTextArea = new JTextArea();
		scrollPane_1.setViewportView(importTypeTextArea);
		importTypeTextArea.setText(creationContext.getActualisedSummary(dataObjectPackage));
		importTypeTextArea.setCaretPosition(0);
			
		if ((creationContext instanceof DiskImportContext) || (creationContext instanceof SIPImportContext)) {

			JLabel workDirLabel = new JLabel("Répertoire de travail:");
			GridBagConstraints gbc_workDirLabel = new GridBagConstraints();
			gbc_workDirLabel.anchor = GridBagConstraints.EAST;
			gbc_workDirLabel.insets = new Insets(0, 0, 5, 5);
			gbc_workDirLabel.gridx = 0;
			gbc_workDirLabel.gridy = 1;
			parametersPanel.add(workDirLabel, gbc_workDirLabel);

			JTextArea workDirTextArea = new JTextArea(2, 60);
			workDirTextArea.setEditable(false);
			workDirTextArea.setText(creationContext.getWorkDir());
			workDirTextArea.setCaretPosition(0);
			workDirTextArea.setLineWrap(true);
			GridBagConstraints gbc_workDirTextArea = new GridBagConstraints();
			gbc_workDirTextArea.weightx = 1.0;
			gbc_workDirTextArea.gridwidth = 2;
			gbc_workDirTextArea.insets = new Insets(0, 0, 5, 5);
			gbc_workDirTextArea.fill = GridBagConstraints.HORIZONTAL;
			gbc_workDirTextArea.gridx = 1;
			gbc_workDirTextArea.gridy = 1;
			parametersPanel.add(workDirTextArea, gbc_workDirTextArea);
			workDirTextArea.setColumns(10);

			if (creationContext instanceof DiskImportContext) {
				JScrollPane ignorePatternsSrollPane = new JScrollPane();

				GridBagConstraints gbc_ignorePatternsSrollPane = new GridBagConstraints();
				gbc_ignorePatternsSrollPane.weightx = 1.0;
				gbc_ignorePatternsSrollPane.weighty = 1.0;
				gbc_ignorePatternsSrollPane.gridwidth = 2;
				gbc_ignorePatternsSrollPane.fill = GridBagConstraints.BOTH;
				gbc_ignorePatternsSrollPane.gridheight = 2;
				gbc_ignorePatternsSrollPane.gridx = 1;
				gbc_ignorePatternsSrollPane.gridy = 3;
				parametersPanel.add(ignorePatternsSrollPane, gbc_ignorePatternsSrollPane);

				JTextArea ignorePatternsTextArea = new JTextArea();
				ignorePatternsTextArea.setEditable(false);
				ignorePatternsTextArea.setRows(6);
				ignorePatternsSrollPane.setViewportView(ignorePatternsTextArea);

				JLabel ignorePatternsLabel = new JLabel("Fichiers exclus des imports:");
				GridBagConstraints gbc_ignorePatternsLabel = new GridBagConstraints();
				gbc_ignorePatternsLabel.gridwidth = 2;
				gbc_ignorePatternsLabel.anchor = GridBagConstraints.EAST;
				gbc_ignorePatternsLabel.insets = new Insets(0, 0, 0, 5);
				gbc_ignorePatternsLabel.gridx = 0;
				gbc_ignorePatternsLabel.gridy = 2;
				parametersPanel.add(ignorePatternsLabel, gbc_ignorePatternsLabel);

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
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 0;
		gbc_cancelButton.gridy = 1;
		getContentPane().add(cancelButton, gbc_cancelButton);
		cancelButton.addActionListener(arg0 -> buttonClose());

		pack();
		setLocationRelativeTo(owner);
	}

	/**
	 * Button close.
	 */
	public void buttonClose() {
		returnValue = JOptionPane.CANCEL_OPTION;
		setVisible(false);
	}

}
