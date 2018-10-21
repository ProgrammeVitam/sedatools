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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;

/**
 * The Class ExportContextDialog.
 */
public class ExportContextDialog extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4092514078236156033L;

	/** The on disk text field. */
	public JTextField onDiskTextField;
	
	/** The work dir text field. */
	public JTextField workDirTextField;
	
	/** The message identifier text field. */
	private JTextField messageIdentifierTextField;
	
	/** The date text field. */
	private JTextField dateTextField;
	
	/** The chckbx now flag. */
	private JCheckBox chckbxNowFlag;
	
	/** The comment text area. */
	private JTextArea commentTextArea;
	
	/** The archival agreement text field. */
	private JTextField archivalAgreementTextField;
	
	/** The clv text area. */
	private JTextArea clvTextArea;
	
	/** The management metadata text area. */
	private JTextArea managementMetadataTextArea;
	
	/** The transfer request reply identifier text field. */
	private JTextField transferRequestReplyIdentifierTextField;
	
	/** The archival agency identifier text field. */
	private JTextField archivalAgencyIdentifierTextField;
	
	/** The archival agency organization descriptive metadata text area. */
	private JTextArea archivalAgencyOrganizationDescriptiveMetadataTextArea;
	
	/** The transferring agency identifier text field. */
	private JTextField transferringAgencyIdentifierTextField;
	
	/** The transferring agency organization descriptive metadata text area. */
	private JTextArea transferringAgencyOrganizationDescriptiveMetadataTextArea;

	/** The hierarchical radio button. */
	private JRadioButton hierarchicalRadioButton;
	
	/** The indented radio button. */
	private JRadioButton indentedRadioButton;
	
	/** The reindex radio button. */
	private JRadioButton reindexYesRadioButton;

	/** The return value. */
	public int returnValue;

	/**
	 * Create the dialog.
	 *
	 * @param owner the owner
	 * @param exportContext the sec
	 */
	public ExportContextDialog(JFrame owner, ExportContext exportContext) {
		super(owner, "Edition des informations utiles à la création du manifest", true);
		getContentPane().setPreferredSize(new Dimension(800, 500));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[] { 1.0, 0.1 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0 };
		getContentPane().setLayout(gridBagLayout);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridwidth = 2;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 5);
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		getContentPane().add(tabbedPane, gbc_tabbedPane);

		// metadata simple fields
		JPanel metadataSimplePanel = new JPanel();
		tabbedPane.addTab("Métadonnées essentielles", null, metadataSimplePanel, null);
		GridBagLayout gbl_metadataSimplePanel = new GridBagLayout();
		gbl_metadataSimplePanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_metadataSimplePanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_metadataSimplePanel.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gbl_metadataSimplePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0 };
		metadataSimplePanel.setLayout(gbl_metadataSimplePanel);

		JLabel presentationLabel = new JLabel("Champs globaux du SIP");
		GridBagConstraints gbc_presentationLabel = new GridBagConstraints();
		gbc_presentationLabel.gridwidth = 3;
		gbc_presentationLabel.insets = new Insets(0, 0, 5, 0);
		gbc_presentationLabel.weightx = 1.0;
		gbc_presentationLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_presentationLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_presentationLabel.gridx = 0;
		gbc_presentationLabel.gridy = 0;
		metadataSimplePanel.add(presentationLabel, gbc_presentationLabel);

		JLabel messageIdentifierLabel = new JLabel("Identifiant du message (MessageIdentifier):");
		GridBagConstraints gbc_messageIdentifierLabel = new GridBagConstraints();
		gbc_messageIdentifierLabel.anchor = GridBagConstraints.EAST;
		gbc_messageIdentifierLabel.insets = new Insets(0, 0, 5, 5);
		gbc_messageIdentifierLabel.gridx = 0;
		gbc_messageIdentifierLabel.gridy = 1;
		metadataSimplePanel.add(messageIdentifierLabel, gbc_messageIdentifierLabel);

		messageIdentifierTextField = new JTextField();
		messageIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().messageIdentifier);
		GridBagConstraints gbc_messageIdentifierTextField = new GridBagConstraints();
		gbc_messageIdentifierTextField.weightx = 1.0;
		gbc_messageIdentifierTextField.insets = new Insets(0, 0, 5, 0);
		gbc_messageIdentifierTextField.gridwidth = 2;
		gbc_messageIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_messageIdentifierTextField.gridx = 1;
		gbc_messageIdentifierTextField.gridy = 1;
		metadataSimplePanel.add(messageIdentifierTextField, gbc_messageIdentifierTextField);
		messageIdentifierTextField.setColumns(10);

		JLabel dateLabel = new JLabel("Date (ISO 8601):");
		GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.anchor = GridBagConstraints.EAST;
		gbc_dateLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dateLabel.gridx = 0;
		gbc_dateLabel.gridy = 2;
		metadataSimplePanel.add(dateLabel, gbc_dateLabel);

		dateTextField = new JTextField();
		dateTextField.setText(exportContext.getArchiveTransferGlobalMetadata().date);
		GridBagConstraints gbc_dateTextField = new GridBagConstraints();
		gbc_dateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_dateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateTextField.gridx = 1;
		gbc_dateTextField.gridy = 2;
		metadataSimplePanel.add(dateTextField, gbc_dateTextField);
		dateTextField.setColumns(10);

		chckbxNowFlag = new JCheckBox("du jour");
		chckbxNowFlag.setSelected(exportContext.getArchiveTransferGlobalMetadata().isNowFlag());
		GridBagConstraints gbc_chckbxNowFlag = new GridBagConstraints();
		gbc_chckbxNowFlag.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxNowFlag.gridx = 2;
		gbc_chckbxNowFlag.gridy = 2;
		metadataSimplePanel.add(chckbxNowFlag, gbc_chckbxNowFlag);

		JLabel lblComment = new JLabel("Commentaire (Comment):");
		GridBagConstraints gbc_lblComment = new GridBagConstraints();
		gbc_lblComment.anchor = GridBagConstraints.EAST;
		gbc_lblComment.insets = new Insets(0, 0, 5, 5);
		gbc_lblComment.gridx = 0;
		gbc_lblComment.gridy = 3;
		metadataSimplePanel.add(lblComment, gbc_lblComment);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 3;
		metadataSimplePanel.add(scrollPane, gbc_scrollPane);

		commentTextArea = new JTextArea();
		commentTextArea.setLineWrap(true);
		commentTextArea.setWrapStyleWord(true);
		commentTextArea.setCaretPosition(0);
		scrollPane.setViewportView(commentTextArea);
		commentTextArea.setText(exportContext.getArchiveTransferGlobalMetadata().comment);

		JLabel lblArchivalagreement = new JLabel("Identifiant du contrat d'entrée (ArchivalAgreement):");
		GridBagConstraints gbc_lblArchivalagreement = new GridBagConstraints();
		gbc_lblArchivalagreement.anchor = GridBagConstraints.EAST;
		gbc_lblArchivalagreement.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivalagreement.gridx = 0;
		gbc_lblArchivalagreement.gridy = 4;
		metadataSimplePanel.add(lblArchivalagreement, gbc_lblArchivalagreement);

		archivalAgreementTextField = new JTextField();
		archivalAgreementTextField.setText(exportContext.getArchiveTransferGlobalMetadata().archivalAgreement);
		GridBagConstraints gbc_archivalAgreementTextField = new GridBagConstraints();
		gbc_archivalAgreementTextField.insets = new Insets(0, 0, 5, 0);
		gbc_archivalAgreementTextField.gridwidth = 2;
		gbc_archivalAgreementTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_archivalAgreementTextField.gridx = 1;
		gbc_archivalAgreementTextField.gridy = 4;
		metadataSimplePanel.add(archivalAgreementTextField, gbc_archivalAgreementTextField);
		archivalAgreementTextField.setColumns(10);

		JLabel lblArchivalAgencyIdentifier = new JLabel("Service d'archivage (ArchivalAgency.Identifier):");
		GridBagConstraints gbc_lblArchivalAgencyIdentifier = new GridBagConstraints();
		gbc_lblArchivalAgencyIdentifier.anchor = GridBagConstraints.EAST;
		gbc_lblArchivalAgencyIdentifier.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivalAgencyIdentifier.gridx = 0;
		gbc_lblArchivalAgencyIdentifier.gridy = 5;
		metadataSimplePanel.add(lblArchivalAgencyIdentifier, gbc_lblArchivalAgencyIdentifier);

		archivalAgencyIdentifierTextField = new JTextField();
		archivalAgencyIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().archivalAgencyIdentifier);
		GridBagConstraints gbc_archivalAgencyIdentifierTextField = new GridBagConstraints();
		gbc_archivalAgencyIdentifierTextField.insets = new Insets(0, 0, 5, 0);
		gbc_archivalAgencyIdentifierTextField.gridwidth = 2;
		gbc_archivalAgencyIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_archivalAgencyIdentifierTextField.gridx = 1;
		gbc_archivalAgencyIdentifierTextField.gridy = 5;
		metadataSimplePanel.add(archivalAgencyIdentifierTextField, gbc_archivalAgencyIdentifierTextField);
		archivalAgencyIdentifierTextField.setColumns(10);

		JLabel lblTransferringAgencyIdentifier = new JLabel("Service versant (TransferringAgency.Identifier):");
		GridBagConstraints gbc_lblTransferringAgencyIdentifier = new GridBagConstraints();
		gbc_lblTransferringAgencyIdentifier.anchor = GridBagConstraints.EAST;
		gbc_lblTransferringAgencyIdentifier.insets = new Insets(0, 0, 5, 5);
		gbc_lblTransferringAgencyIdentifier.gridx = 0;
		gbc_lblTransferringAgencyIdentifier.gridy = 6;
		metadataSimplePanel.add(lblTransferringAgencyIdentifier, gbc_lblTransferringAgencyIdentifier);

		transferringAgencyIdentifierTextField = new JTextField();
		transferringAgencyIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().transferringAgencyIdentifier);
		GridBagConstraints gbc_transferringAgencyIdentifierTextField = new GridBagConstraints();
		gbc_transferringAgencyIdentifierTextField.insets = new Insets(0, 0, 5, 0);
		gbc_transferringAgencyIdentifierTextField.gridwidth = 2;
		gbc_transferringAgencyIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_transferringAgencyIdentifierTextField.gridx = 1;
		gbc_transferringAgencyIdentifierTextField.gridy = 6;
		metadataSimplePanel.add(transferringAgencyIdentifierTextField, gbc_transferringAgencyIdentifierTextField);
		transferringAgencyIdentifierTextField.setColumns(10);

		JLabel managementMetadataLabel = new JLabel("ManagementMetadata (XML):");
		GridBagConstraints gbc_managementMetadataLabel = new GridBagConstraints();
		gbc_managementMetadataLabel.anchor = GridBagConstraints.EAST;
		gbc_managementMetadataLabel.insets = new Insets(0, 0, 5, 5);
		gbc_managementMetadataLabel.gridx = 0;
		gbc_managementMetadataLabel.gridy = 7;
		metadataSimplePanel.add(managementMetadataLabel, gbc_managementMetadataLabel);

		JScrollPane scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridwidth = 2;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_2.gridx = 1;
		gbc_scrollPane_2.gridy = 7;
		metadataSimplePanel.add(scrollPane_2, gbc_scrollPane_2);

		managementMetadataTextArea = new JTextArea();
		scrollPane_2.setViewportView(managementMetadataTextArea);
		managementMetadataTextArea.setText(exportContext.getManagementMetadataXmlData());
		managementMetadataTextArea.setCaretPosition(0);

		// Metadata complex fields
		JPanel metadataComplexPanel = new JPanel();
		tabbedPane.addTab("Métadonnées étendues", null, metadataComplexPanel, null);
		GridBagLayout gbl_metadataComplexPanel = new GridBagLayout();
		gbl_metadataComplexPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_metadataComplexPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_metadataComplexPanel.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gbl_metadataComplexPanel.rowWeights = new double[] {0.0, 1.0,0.0,  1.0, 1.0 };
		metadataComplexPanel.setLayout(gbl_metadataComplexPanel);

		JLabel presentationComplexLabel = new JLabel("Champs globaux étendus du SIP");
		GridBagConstraints gbc_presentationComplexLabel = new GridBagConstraints();
		gbc_presentationComplexLabel.gridwidth = 3;
		gbc_presentationComplexLabel.insets = new Insets(0, 0, 5, 0);
		gbc_presentationComplexLabel.weightx = 1.0;
		gbc_presentationComplexLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_presentationComplexLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_presentationComplexLabel.gridx = 0;
		gbc_presentationComplexLabel.gridy = 0;
		metadataComplexPanel.add(presentationComplexLabel, gbc_presentationComplexLabel);

		JLabel clvLabel = new JLabel("CodeListVersions (XML):");
		GridBagConstraints gbc_clvLabel = new GridBagConstraints();
		gbc_clvLabel.anchor = GridBagConstraints.EAST;
		gbc_clvLabel.insets = new Insets(0, 0, 5, 5);
		gbc_clvLabel.gridx = 0;
		gbc_clvLabel.gridy = 1;
		metadataComplexPanel.add(clvLabel, gbc_clvLabel);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 1;
		metadataComplexPanel.add(scrollPane_1, gbc_scrollPane_1);

		clvTextArea = new JTextArea();
		scrollPane_1.setViewportView(clvTextArea);
		clvTextArea.setText(exportContext.getArchiveTransferGlobalMetadata().codeListVersionsXmlData);
		clvTextArea.setCaretPosition(0);

		JLabel lblTransferRequestReplyIdentifier = new JLabel("TransferRequestReplyIdentifier:");
		GridBagConstraints gbc_lblTransferRequestReplyIdentifier = new GridBagConstraints();
		gbc_lblTransferRequestReplyIdentifier.anchor = GridBagConstraints.EAST;
		gbc_lblTransferRequestReplyIdentifier.insets = new Insets(0, 0, 5, 5);
		gbc_lblTransferRequestReplyIdentifier.gridx = 0;
		gbc_lblTransferRequestReplyIdentifier.gridy = 2;
		metadataComplexPanel.add(lblTransferRequestReplyIdentifier, gbc_lblTransferRequestReplyIdentifier);

		transferRequestReplyIdentifierTextField = new JTextField();
		transferRequestReplyIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier);
		GridBagConstraints gbc_transferRequestReplyIdentifierTextField = new GridBagConstraints();
		gbc_transferRequestReplyIdentifierTextField.insets = new Insets(0, 0, 5, 0);
		gbc_transferRequestReplyIdentifierTextField.gridwidth = 2;
		gbc_transferRequestReplyIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_transferRequestReplyIdentifierTextField.gridx = 1;
		gbc_transferRequestReplyIdentifierTextField.gridy = 2;
		metadataComplexPanel.add(transferRequestReplyIdentifierTextField,
				gbc_transferRequestReplyIdentifierTextField);
		transferRequestReplyIdentifierTextField.setColumns(10);

		JLabel archivalAgencyOrganizationDescriptiveMetadataLabel = new JLabel("ArchivalAgency.ODM (XML):");
		GridBagConstraints gbc_archivalAgencyOrganizationDescriptiveMetadataLabel = new GridBagConstraints();
		gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.anchor = GridBagConstraints.EAST;
		gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.insets = new Insets(0, 0, 5, 5);
		gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.gridx = 0;
		gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.gridy = 3;
		metadataComplexPanel.add(archivalAgencyOrganizationDescriptiveMetadataLabel,
				gbc_archivalAgencyOrganizationDescriptiveMetadataLabel);

		JScrollPane scrollPane_3 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridwidth = 2;
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_3.gridx = 1;
		gbc_scrollPane_3.gridy = 3;
		metadataComplexPanel.add(scrollPane_3, gbc_scrollPane_3);

		archivalAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
		scrollPane_3.setViewportView(archivalAgencyOrganizationDescriptiveMetadataTextArea);
		archivalAgencyOrganizationDescriptiveMetadataTextArea
				.setText(exportContext.getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData);
		archivalAgencyOrganizationDescriptiveMetadataTextArea.setCaretPosition(0);

		JLabel transferringAgencyOrganizationDescriptiveMetadataLabel = new JLabel("TransferringAgency.ODM (XML):");
		GridBagConstraints gbc_transferringAgencyOrganizationDescriptiveMetadataLabel = new GridBagConstraints();
		gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.anchor = GridBagConstraints.EAST;
		gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.insets = new Insets(0, 0, 0, 5);
		gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.gridx = 0;
		gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.gridy = 4;
		metadataComplexPanel.add(transferringAgencyOrganizationDescriptiveMetadataLabel,
				gbc_transferringAgencyOrganizationDescriptiveMetadataLabel);

		JScrollPane scrollPane_4 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_4.gridwidth = 2;
		gbc_scrollPane_4.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane_4.gridx = 1;
		gbc_scrollPane_4.gridy = 4;
		metadataComplexPanel.add(scrollPane_4, gbc_scrollPane_4);

		transferringAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
		scrollPane_4.setViewportView(transferringAgencyOrganizationDescriptiveMetadataTextArea);
		transferringAgencyOrganizationDescriptiveMetadataTextArea
				.setText(exportContext.getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData);
		transferringAgencyOrganizationDescriptiveMetadataTextArea.setCaretPosition(0);

		// Manifest construction parameters
		JPanel manifestParametersPanel = new JPanel();
		tabbedPane.addTab("Paramètres de construction du manifest", null, manifestParametersPanel, null);
		GridBagLayout gbl_manifestParametersPanel = new GridBagLayout();
		gbl_manifestParametersPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_manifestParametersPanel.rowHeights = new int[] { 0, 0, 0};
		gbl_manifestParametersPanel.columnWeights = new double[] {};
		gbl_manifestParametersPanel.rowWeights = new double[] {};
		manifestParametersPanel.setLayout(gbl_manifestParametersPanel);

		JPanel parametersPanel = new JPanel();
		GridBagConstraints gbc_parametersPanel = new GridBagConstraints();
		gbc_parametersPanel.anchor = GridBagConstraints.EAST;
		gbc_parametersPanel.insets = new Insets(5, 5, 5, 5);
		gbc_parametersPanel.gridwidth = 2;
		gbc_parametersPanel.gridx = 0;
		gbc_parametersPanel.gridy = 0;
		getContentPane().add(parametersPanel, gbc_parametersPanel);
		GridBagLayout gbl_parametersPanel = new GridBagLayout();
		gbl_parametersPanel.columnWeights = new double[] { 0.1, 0.5, 0.5 };
		manifestParametersPanel.setLayout(gbl_parametersPanel);

		JLabel hierarchicalAULabel = new JLabel("Export des AU:");
		GridBagConstraints gbc_hierarchicalAULabel = new GridBagConstraints();
		gbc_hierarchicalAULabel.anchor = GridBagConstraints.EAST;
		gbc_hierarchicalAULabel.insets = new Insets(0, 0, 5, 5);
		gbc_hierarchicalAULabel.gridx = 0;
		gbc_hierarchicalAULabel.gridy = 0;
		manifestParametersPanel.add(hierarchicalAULabel, gbc_hierarchicalAULabel);

		JRadioButton flatRadioButton = new JRadioButton("A plat");
		GridBagConstraints gbc_flatRadioButton = new GridBagConstraints();
		gbc_flatRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_flatRadioButton.anchor = GridBagConstraints.WEST;
		gbc_flatRadioButton.insets = new Insets(0, 0, 5, 5);
		gbc_flatRadioButton.gridx = 2;
		gbc_flatRadioButton.gridy = 0;
		manifestParametersPanel.add(flatRadioButton, gbc_flatRadioButton);

		hierarchicalRadioButton = new JRadioButton("Imbriquées");
		GridBagConstraints gbc_hierarchicalRadioButton = new GridBagConstraints();
		gbc_hierarchicalRadioButton.anchor = GridBagConstraints.WEST;
		gbc_hierarchicalRadioButton.insets = new Insets(0, 0, 5, 0);
		gbc_hierarchicalRadioButton.gridx = 1;
		gbc_hierarchicalRadioButton.gridy = 0;
		manifestParametersPanel.add(hierarchicalRadioButton, gbc_hierarchicalRadioButton);

		ButtonGroup hierarchicalAUButtonGroup = new ButtonGroup();
		hierarchicalAUButtonGroup.add(flatRadioButton);
		hierarchicalAUButtonGroup.add(hierarchicalRadioButton);
		hierarchicalAUButtonGroup.clearSelection();
		if (exportContext.isHierarchicalArchiveUnits())
			hierarchicalRadioButton.setSelected(true);
		else
			flatRadioButton.setSelected(true);
		
		JLabel xmlPresentationLabel = new JLabel("Présentation XML:");
		GridBagConstraints gbc_xmlPresentationLabel = new GridBagConstraints();
		gbc_xmlPresentationLabel.anchor = GridBagConstraints.EAST;
		gbc_xmlPresentationLabel.insets = new Insets(0, 0, 0, 5);
		gbc_xmlPresentationLabel.gridx = 0;
		gbc_xmlPresentationLabel.gridy = 1;
		manifestParametersPanel.add(xmlPresentationLabel, gbc_xmlPresentationLabel);
		
		JRadioButton linearRadioButton = new JRadioButton("Linéaire");
		GridBagConstraints gbc_linearRadioButton = new GridBagConstraints();
		gbc_linearRadioButton.anchor = GridBagConstraints.WEST;
		gbc_linearRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_linearRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_linearRadioButton.gridx = 2;
		gbc_linearRadioButton.gridy = 1;
		manifestParametersPanel.add(linearRadioButton, gbc_linearRadioButton);
		
		indentedRadioButton = new JRadioButton("Indentée");
		GridBagConstraints gbc_indentedRadioButton = new GridBagConstraints();
		gbc_indentedRadioButton.anchor = GridBagConstraints.WEST;
		gbc_indentedRadioButton.gridx = 1;
		gbc_indentedRadioButton.gridy = 1;
		manifestParametersPanel.add(indentedRadioButton, gbc_indentedRadioButton);

		ButtonGroup indentedButtonGroup = new ButtonGroup();
		indentedButtonGroup.add(linearRadioButton);
		indentedButtonGroup.add(indentedRadioButton);
		indentedButtonGroup.clearSelection();
		if (exportContext.isIndented())
			indentedRadioButton.setSelected(true);
		else
			linearRadioButton.setSelected(true);
		
		
		JLabel xmlReindexLabel = new JLabel("Renumérotation des éléments XML avant export:");
		GridBagConstraints gbc_xmlReindexLabel = new GridBagConstraints();
		gbc_xmlReindexLabel.anchor = GridBagConstraints.EAST;
		gbc_xmlReindexLabel.insets = new Insets(0, 0, 0, 5);
		gbc_xmlReindexLabel.gridx = 0;
		gbc_xmlReindexLabel.gridy = 2;
		manifestParametersPanel.add(xmlReindexLabel, gbc_xmlReindexLabel);
		
		reindexYesRadioButton = new JRadioButton("Oui");
		GridBagConstraints gbc_reindexYesRadioButton = new GridBagConstraints();
		gbc_reindexYesRadioButton.anchor = GridBagConstraints.WEST;
		gbc_reindexYesRadioButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_reindexYesRadioButton.insets = new Insets(0, 0, 0, 5);
		gbc_reindexYesRadioButton.gridx = 1;
		gbc_reindexYesRadioButton.gridy = 2;
		manifestParametersPanel.add(reindexYesRadioButton, gbc_reindexYesRadioButton);
		
		JRadioButton reindexNoRadioButton = new JRadioButton("Non");
		GridBagConstraints gbc_reindexNoRadioButton = new GridBagConstraints();
		gbc_reindexNoRadioButton.anchor = GridBagConstraints.WEST;
		gbc_reindexNoRadioButton.gridx = 2;
		gbc_reindexNoRadioButton.gridy = 2;
		manifestParametersPanel.add(reindexNoRadioButton, gbc_reindexNoRadioButton);

		ButtonGroup reindexButtonGroup = new ButtonGroup();
		reindexButtonGroup.add(reindexYesRadioButton);
		reindexButtonGroup.add(reindexNoRadioButton);
		reindexButtonGroup.clearSelection();
		if (exportContext.isReindex())
			reindexYesRadioButton.setSelected(true);
		else
			reindexNoRadioButton.setSelected(true);
		
		
		// Buttons
		JButton cancelButton = new JButton("Annuler");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 0;
		gbc_cancelButton.gridy = 1;
		getContentPane().add(cancelButton, gbc_cancelButton);
		cancelButton.addActionListener(arg0 -> buttonCancel());

		JButton okButton = new JButton("OK");
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.gridx = 1;
		gbc_okButton.gridy = 1;
		getContentPane().add(okButton, gbc_okButton);
		okButton.addActionListener(arg0 -> buttonOk());

		pack();
		setLocationRelativeTo(owner);
	}

	/**
	 * Button cancel.
	 */
	public void buttonCancel() {
		returnValue = JOptionPane.CANCEL_OPTION;
		setVisible(false);
	}

	/**
	 * Button ok.
	 */
	public void buttonOk() {
		returnValue = JOptionPane.OK_OPTION;
		setVisible(false);
	}

	/**
	 * Sets the global metadata context from dialog.
	 *
	 * @param sec the sec
	 */
	public void setExportContextFromDialog(ExportContext sec) {
		sec.setHierarchicalArchiveUnits(hierarchicalRadioButton.isSelected());
		sec.setIndented(indentedRadioButton.isSelected());
		sec.setReindex(reindexYesRadioButton.isSelected());
		sec.setManagementMetadataXmlData(managementMetadataTextArea.getText());
		GlobalMetadata atgm=sec.getArchiveTransferGlobalMetadata();
		atgm.comment=commentTextArea.getText();
		atgm.date=dateTextField.getText();
		atgm.setNowFlag(chckbxNowFlag.isSelected());
		atgm.messageIdentifier=messageIdentifierTextField.getText();
		atgm.archivalAgreement=archivalAgreementTextField.getText();
		atgm.codeListVersionsXmlData=clvTextArea.getText();
		atgm.transferRequestReplyIdentifier=transferRequestReplyIdentifierTextField.getText();
		atgm.archivalAgencyIdentifier=archivalAgencyIdentifierTextField.getText();
		atgm.archivalAgencyOrganizationDescriptiveMetadataXmlData=
				archivalAgencyOrganizationDescriptiveMetadataTextArea.getText();
		atgm.transferringAgencyIdentifier=transferringAgencyIdentifierTextField.getText();
		atgm.transferringAgencyOrganizationDescriptiveMetadataXmlData=
				transferringAgencyOrganizationDescriptiveMetadataTextArea.getText();
	}
}
