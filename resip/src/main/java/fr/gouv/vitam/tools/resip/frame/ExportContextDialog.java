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

import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;

import static fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter.*;
import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The class ExportContextDialog.
 * <p>
 * Class for export context specific definition dialog.
 */
public class ExportContextDialog extends JDialog {

	/**
	 * The actions components.
	 */
	private JTabbedPane tabbedPane;

	private JTextField messageIdentifierTextField;
	private JTextField dateTextField;
	private JCheckBox chckbxNowFlag;
	private JTextArea commentTextArea;
	private JTextField archivalAgreementTextField;
	private JTextField archivalAgencyIdentifierTextField;
	private JTextField transferringAgencyIdentifierTextField;

	private JTextArea clvTextArea;
	private JTextArea managementMetadataTextArea;
	private JTextField transferRequestReplyIdentifierTextField;
	private JTextArea archivalAgencyOrganizationDescriptiveMetadataTextArea;
	private JTextArea transferringAgencyOrganizationDescriptiveMetadataTextArea;

	private JRadioButton hierarchicalRadioButton;
	private JRadioButton indentedRadioButton;
	private JRadioButton reindexYesRadioButton;
	private JRadioButton firstUsageButton;
	private JRadioButton lastUsageButton;
	private JRadioButton allUsageButton;
	private JTextField nameMaxSizeTextField;
	private JTextArea metadataFilterTextArea;
	private JCheckBox metadataFilterCheckBox;

	/**
	 * The data.
	 */
	public ExportContext gmc;

	/**
	 * The return value.
	 */
	private int returnValue;

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
	public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		TestDialogWindow window = new TestDialogWindow(ExportContextDialog.class);
	}

	/**
	 * Instantiates a new ExportContextDialog for test.
	 *
	 * @param owner the owner
	 */
	public ExportContextDialog(JFrame owner) {
		this(owner, new ExportContext(Prefs.getInstance()));
	}

	/**
	 * Create the dialog.
	 *
	 * @param owner the owner
	 * @param exportContext the sec
	 */
	public ExportContextDialog(JFrame owner, ExportContext exportContext) {
		super(owner, "Edition des informations utiles à la création du manifest", true);
		GridBagConstraints gbc;

		this.setPreferredSize(new Dimension(800, 500));
		this.setMinimumSize(new Dimension(500, 300));

		Container contentPane = getContentPane();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{1.0, 0.1};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0};
		contentPane.setLayout(new GridBagLayout());

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx=1.0;
		gbc.weighty=1.0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		contentPane.add(tabbedPane, gbc);

		// header and footer simple fields
		JPanel headerFooterSimplePanel = new JPanel();
		tabbedPane.addTab("Métadonnées globales",  new ImageIcon(getClass().getResource("/icon/document-properties.png")), headerFooterSimplePanel, null);
		GridBagLayout gbl_headerFooterSimplePanel = new GridBagLayout();
		gbl_headerFooterSimplePanel.columnWidths = new int[]{0, 0, 0};
		gbl_headerFooterSimplePanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_headerFooterSimplePanel.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_headerFooterSimplePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
		headerFooterSimplePanel.setLayout(gbl_headerFooterSimplePanel);

		JLabel presentationLabel = new JLabel("Champs globaux du SIP");
		presentationLabel.setFont(MainWindow.BOLD_LABEL_FONT);
		gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.insets = new Insets(0, 0, 5, 0);
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);
		headerFooterSimplePanel.add(presentationLabel, gbc);

		JLabel messageIdentifierLabel = new JLabel("Identifiant du message :");
		messageIdentifierLabel.setToolTipText("MessageIdentifier");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 1;
		headerFooterSimplePanel.add(messageIdentifierLabel, gbc);

		messageIdentifierTextField = new JTextField();
		messageIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().messageIdentifier);
		messageIdentifierTextField.setFont(MainWindow.DETAILS_FONT);
		gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		headerFooterSimplePanel.add(messageIdentifierTextField, gbc);
		messageIdentifierTextField.setColumns(10);

		JLabel dateLabel = new JLabel("Date :");
		dateLabel.setToolTipText("ISO 8601");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 2;
		headerFooterSimplePanel.add(dateLabel, gbc);

		dateTextField = new JTextField();
		dateTextField.setText(exportContext.getArchiveTransferGlobalMetadata().date);
		dateTextField.setFont(MainWindow.DETAILS_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		headerFooterSimplePanel.add(dateTextField, gbc);
		dateTextField.setColumns(10);

		chckbxNowFlag = new JCheckBox("du jour");
		chckbxNowFlag.setSelected(exportContext.getArchiveTransferGlobalMetadata().isNowFlag());
		chckbxNowFlag.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 2;
		gbc.gridy = 2;
		headerFooterSimplePanel.add(chckbxNowFlag, gbc);

		JLabel lblComment = new JLabel("Commentaire :");
		lblComment.setToolTipText("Comment");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 3;
		headerFooterSimplePanel.add(lblComment, gbc);

		JScrollPane scrollPane = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 3;
		headerFooterSimplePanel.add(scrollPane, gbc);

		commentTextArea = new JTextArea();
		commentTextArea.setLineWrap(true);
		commentTextArea.setWrapStyleWord(true);
		commentTextArea.setCaretPosition(0);
		scrollPane.setViewportView(commentTextArea);
		commentTextArea.setText(exportContext.getArchiveTransferGlobalMetadata().comment);
		commentTextArea.setFont(MainWindow.DETAILS_FONT);

		JLabel lblArchivalagreement = new JLabel("Identifiant du contrat d'entrée :");
		lblArchivalagreement.setToolTipText("ArchivalAgreement");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 4;
		headerFooterSimplePanel.add(lblArchivalagreement, gbc);

		archivalAgreementTextField = new JTextField();
		archivalAgreementTextField.setText(exportContext.getArchiveTransferGlobalMetadata().archivalAgreement);
		archivalAgreementTextField.setFont(MainWindow.DETAILS_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 4;
		headerFooterSimplePanel.add(archivalAgreementTextField, gbc);
		archivalAgreementTextField.setColumns(10);

		JLabel lblArchivalAgencyIdentifier = new JLabel("Service d'archivage :");
		lblArchivalAgencyIdentifier.setToolTipText("ArchivalAgency.Identifier");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 5;
		headerFooterSimplePanel.add(lblArchivalAgencyIdentifier, gbc);

		archivalAgencyIdentifierTextField = new JTextField();
		archivalAgencyIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().archivalAgencyIdentifier);
		archivalAgencyIdentifierTextField.setFont(MainWindow.DETAILS_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 5;
		headerFooterSimplePanel.add(archivalAgencyIdentifierTextField, gbc);
		archivalAgencyIdentifierTextField.setColumns(10);

		JLabel lblTransferringAgencyIdentifier = new JLabel("Service versant :");
		lblTransferringAgencyIdentifier.setToolTipText("TransferringAgency.Identifier");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 6;
		headerFooterSimplePanel.add(lblTransferringAgencyIdentifier, gbc);

		transferringAgencyIdentifierTextField = new JTextField();
		transferringAgencyIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().transferringAgencyIdentifier);
		transferringAgencyIdentifierTextField.setFont(MainWindow.DETAILS_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 6;
		headerFooterSimplePanel.add(transferringAgencyIdentifierTextField, gbc);
		transferringAgencyIdentifierTextField.setColumns(10);

		// Header and footer complex fields
		JPanel headerFooterComplexPanel = new JPanel();
		tabbedPane.addTab("Métadonnées globales étendues",   new ImageIcon(getClass().getResource("/icon/text-x-generic.png")), headerFooterComplexPanel, null);
		GridBagLayout gbl_headerFooterComplexPanel = new GridBagLayout();
		gbl_headerFooterComplexPanel.columnWidths = new int[]{0, 0, 0};
		gbl_headerFooterComplexPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_headerFooterComplexPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
		gbl_headerFooterComplexPanel.rowWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.5, 0.5, 0.0};
		headerFooterComplexPanel.setLayout(gbl_headerFooterComplexPanel);

		JLabel presentationComplexLabel = new JLabel("Champs globaux étendus du SIP");
		presentationComplexLabel.setFont(MainWindow.BOLD_LABEL_FONT);
		gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		headerFooterComplexPanel.add(presentationComplexLabel, gbc);

		JLabel clvLabel = new JLabel("Liste des codes :");
		clvLabel.setToolTipText("Bloc XML CodeListVersions");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 1;
		headerFooterComplexPanel.add(clvLabel, gbc);

		JScrollPane scrollPane_1 = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 1;
		headerFooterComplexPanel.add(scrollPane_1, gbc);

		clvTextArea = new JTextArea();
		clvTextArea.setFont(MainWindow.DETAILS_FONT);
		scrollPane_1.setViewportView(clvTextArea);
		clvTextArea.setText(exportContext.getArchiveTransferGlobalMetadata().codeListVersionsXmlData);
		clvTextArea.setCaretPosition(0);

		JLabel managementMetadataLabel = new JLabel("Métadonnées de gestion globales :");
		managementMetadataLabel.setToolTipText("Bloc XML ManagementMetadata");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 2;
		headerFooterComplexPanel.add(managementMetadataLabel, gbc);

		JScrollPane scrollPane_2 = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 2;
		headerFooterComplexPanel.add(scrollPane_2, gbc);

		managementMetadataTextArea = new JTextArea();
		managementMetadataTextArea.setFont(MainWindow.DETAILS_FONT);
		scrollPane_2.setViewportView(managementMetadataTextArea);
		managementMetadataTextArea.setText(exportContext.getManagementMetadataXmlData());
		managementMetadataTextArea.setCaretPosition(0);

		JLabel lblTransferRequestReplyIdentifier = new JLabel("Identifiant de réponse de transfert :");
		lblTransferRequestReplyIdentifier.setToolTipText("Valeur de TransferRequestReplyIdentifier");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 3;
		headerFooterComplexPanel.add(lblTransferRequestReplyIdentifier, gbc);

		transferRequestReplyIdentifierTextField = new JTextField();
		transferRequestReplyIdentifierTextField.setFont(MainWindow.DETAILS_FONT);
		transferRequestReplyIdentifierTextField.setText(exportContext.getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 3;
		headerFooterComplexPanel.add(transferRequestReplyIdentifierTextField,
				gbc);
		transferRequestReplyIdentifierTextField.setColumns(10);

		JLabel archivalAgencyOrganizationDescriptiveMetadataLabel = new JLabel("Détails sur l'acteur d'archivage :");
		archivalAgencyOrganizationDescriptiveMetadataLabel.setToolTipText("Bloc XML OrganisationDescriptiveMetadata de la métadonnée ArchivalAgency ");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 4;
		headerFooterComplexPanel.add(archivalAgencyOrganizationDescriptiveMetadataLabel,
				gbc);

		JScrollPane scrollPane_3 = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 4;
		headerFooterComplexPanel.add(scrollPane_3, gbc);

		archivalAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
		archivalAgencyOrganizationDescriptiveMetadataTextArea.setFont(MainWindow.DETAILS_FONT);
		scrollPane_3.setViewportView(archivalAgencyOrganizationDescriptiveMetadataTextArea);
		archivalAgencyOrganizationDescriptiveMetadataTextArea
				.setText(exportContext.getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData);
		archivalAgencyOrganizationDescriptiveMetadataTextArea.setCaretPosition(0);

		JLabel transferringAgencyOrganizationDescriptiveMetadataLabel = new JLabel("Détails sur l'acteur de transfert :");
		transferringAgencyOrganizationDescriptiveMetadataLabel.setToolTipText("Bloc XML OrganisationDescriptiveMetadata de la métadonnée TransferringAgency ");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 5;
		headerFooterComplexPanel.add(transferringAgencyOrganizationDescriptiveMetadataLabel,
				gbc);

		JScrollPane scrollPane_4 = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 5;
		headerFooterComplexPanel.add(scrollPane_4, gbc);

		transferringAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
		transferringAgencyOrganizationDescriptiveMetadataTextArea.setFont(MainWindow.DETAILS_FONT);
		scrollPane_4.setViewportView(transferringAgencyOrganizationDescriptiveMetadataTextArea);
		transferringAgencyOrganizationDescriptiveMetadataTextArea
				.setText(exportContext.getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData);
		transferringAgencyOrganizationDescriptiveMetadataTextArea.setCaretPosition(0);

		// ExportParameters Panel

		JPanel exportParametersPanel = new JPanel();
		tabbedPane.addTab("Export", new ImageIcon(getClass().getResource("/icon/document-save.png")), exportParametersPanel, null);
		GridBagLayout gbl_exportParametersPanel = new GridBagLayout();
		gbl_exportParametersPanel.columnWeights = new double[]{0.1, 0.1, 0.5, 0.1};
		gbl_exportParametersPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_exportParametersPanel.rowWeights = new double[]{0, 0, 0, 0,0,0,0,0,1};
		exportParametersPanel.setLayout(gbl_exportParametersPanel);

		JLabel inSIPLabel = new JLabel("Options de formation du SIP");
		inSIPLabel.setFont(MainWindow.BOLD_LABEL_FONT);
		gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		exportParametersPanel.add(inSIPLabel, gbc);

		JLabel hierarchicalAULabel = new JLabel("Export des AU dans le SIP:");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 1;
		exportParametersPanel.add(hierarchicalAULabel, gbc);

		JRadioButton flatRadioButton = new JRadioButton("A plat");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 2;
		gbc.gridy = 1;
		exportParametersPanel.add(flatRadioButton, gbc);

		hierarchicalRadioButton = new JRadioButton("Imbriquées");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 1;
		exportParametersPanel.add(hierarchicalRadioButton, gbc);

		ButtonGroup hierarchicalAUButtonGroup = new ButtonGroup();
		hierarchicalAUButtonGroup.add(flatRadioButton);
		hierarchicalAUButtonGroup.add(hierarchicalRadioButton);
		hierarchicalAUButtonGroup.clearSelection();
		if (exportContext.isHierarchicalArchiveUnits())
			hierarchicalRadioButton.setSelected(true);
		else
			flatRadioButton.setSelected(true);

		JLabel xmlPresentationLabel = new JLabel("Présentation XML dans le SIP:");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 2;
		exportParametersPanel.add(xmlPresentationLabel, gbc);

		JRadioButton linearRadioButton = new JRadioButton("Linéaire");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 2;
		gbc.gridy = 2;
		exportParametersPanel.add(linearRadioButton, gbc);

		indentedRadioButton = new JRadioButton("Indentée");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 1;
		gbc.gridy = 2;
		exportParametersPanel.add(indentedRadioButton, gbc);

		ButtonGroup indentedButtonGroup = new ButtonGroup();
		indentedButtonGroup.add(linearRadioButton);
		indentedButtonGroup.add(indentedRadioButton);
		indentedButtonGroup.clearSelection();
		if (exportContext.isIndented())
			indentedRadioButton.setSelected(true);
		else
			linearRadioButton.setSelected(true);

		JLabel xmlReindexLabel = new JLabel("Renumérotation des éléments XML avant export:");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 3;
		exportParametersPanel.add(xmlReindexLabel, gbc);

		reindexYesRadioButton = new JRadioButton("Oui");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 1;
		gbc.gridy = 3;
		exportParametersPanel.add(reindexYesRadioButton, gbc);

		JRadioButton reindexNoRadioButton = new JRadioButton("Non");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = 2;
		gbc.gridy = 3;
		exportParametersPanel.add(reindexNoRadioButton, gbc);

		ButtonGroup reindexButtonGroup = new ButtonGroup();
		reindexButtonGroup.add(reindexYesRadioButton);
		reindexButtonGroup.add(reindexNoRadioButton);
		reindexButtonGroup.clearSelection();
		if (exportContext.isReindex())
			reindexYesRadioButton.setSelected(true);
		else
			reindexNoRadioButton.setSelected(true);

		JLabel inCSVLabel = new JLabel("Options d'export en hiérarchie simplifiée et fichier csv");
		inCSVLabel.setFont(MainWindow.BOLD_LABEL_FONT);
		gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 4;
		exportParametersPanel.add(inCSVLabel, gbc);

		JLabel exportModeChoiceLabel = new JLabel("Mode de choix des objets exportés:");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 5;
		exportParametersPanel.add(exportModeChoiceLabel, gbc);

		firstUsageButton = new JRadioButton("Premier");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 5;
		exportParametersPanel.add(firstUsageButton, gbc);

		lastUsageButton = new JRadioButton("Dernier");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 2;
		gbc.gridy = 5;
		exportParametersPanel.add(lastUsageButton, gbc);

		allUsageButton = new JRadioButton("Tous");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 3;
		gbc.gridy = 5;
		exportParametersPanel.add(allUsageButton, gbc);

		ButtonGroup exportModeChoiceButtonGroup = new ButtonGroup();
		exportModeChoiceButtonGroup.add(firstUsageButton);
		exportModeChoiceButtonGroup.add(lastUsageButton);
		exportModeChoiceButtonGroup.add(allUsageButton);
		exportModeChoiceButtonGroup.clearSelection();
		switch (exportContext.getUsageVersionSelectionMode()) {
			case FIRST_DATAOBJECT:
				firstUsageButton.setSelected(true);
				break;
			case LAST_DATAOBJECT:
			default:
				lastUsageButton.setSelected(true);
				break;
			case ALL_DATAOBJECTS:
				allUsageButton.setSelected(true);
				break;
		}

		JLabel lblNameMaxSize = new JLabel("Taille max des noms de répertoires :");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 6;
		exportParametersPanel.add(lblNameMaxSize, gbc);

		nameMaxSizeTextField = new JTextField();
		DocumentFilter filter = new NumericFilter();
		((AbstractDocument) nameMaxSizeTextField.getDocument()).setDocumentFilter(filter);
		nameMaxSizeTextField.setText(Integer.toString(exportContext.getMaxNameSize()));
		nameMaxSizeTextField.setFont(MainWindow.DETAILS_FONT);
		nameMaxSizeTextField.setColumns(10);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		exportParametersPanel.add(nameMaxSizeTextField, gbc);

		JLabel metadataFilterLabel = new JLabel("Filtrage des métadonnées");
		metadataFilterLabel.setFont(MainWindow.BOLD_LABEL_FONT);
		gbc = new GridBagConstraints();
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 7;
		exportParametersPanel.add(metadataFilterLabel, gbc);

		scrollPane = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 8;
		exportParametersPanel.add(scrollPane, gbc);

		metadataFilterTextArea = new JTextArea();
		metadataFilterTextArea.setFont(MainWindow.DETAILS_FONT);
		scrollPane.setViewportView(metadataFilterTextArea);
		if (exportContext.getKeptMetadataList() != null)
			metadataFilterTextArea.setText(String.join("\n", String.join("\n", exportContext.getKeptMetadataList())));
		metadataFilterTextArea.setCaretPosition(0);

		metadataFilterCheckBox = new JCheckBox("Seules métadonnées exportées :");
		metadataFilterCheckBox.setToolTipText("Liste des noms de métadonnées dans <Content>, utilisée comm filtre si coché ");
		metadataFilterCheckBox.setSelected(exportContext.isMetadataFilterFlag());
		metadataFilterTextArea.setEnabled(exportContext.isMetadataFilterFlag());
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.weighty = 1.0;
		exportParametersPanel.add(metadataFilterCheckBox, gbc);
		metadataFilterCheckBox.addItemListener(arg -> metadataFilterEvent(arg));

		// Buttons
		JButton cancelButton = new JButton("Annuler");
		cancelButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx=1.0;
		gbc.weighty=0.0;
		contentPane.add(cancelButton, gbc);
		cancelButton.addActionListener(arg -> buttonCancel());

		JButton okButton = new JButton("OK");
		okButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx=1.0;
		gbc.weighty=0.0;
		contentPane.add(okButton, gbc);
		okButton.addActionListener(arg -> buttonOk());

		pack();
		setLocationRelativeTo(owner);
	}

	// actions

	private void metadataFilterEvent(ItemEvent event) {
		if (event.getStateChange() == SELECTED) {
			metadataFilterTextArea.setEnabled(true);
		} else if (event.getStateChange() == DESELECTED) {
			metadataFilterTextArea.setEnabled(false);
		}
	}

	private void buttonCancel() {
		returnValue = ResipGraphicApp.KO_DIALOG;
		setVisible(false);
	}

	private void buttonOk() {
		if (!extractFromDialog())
			return;
		returnValue = ResipGraphicApp.OK_DIALOG;
		setVisible(false);
	}

	/**
	 * Extract the global metadata context from dialog.
	 */
	public boolean extractFromDialog() {
		int tmp=10;

		gmc=new ExportContext();
		gmc.setHierarchicalArchiveUnits(hierarchicalRadioButton.isSelected());
		gmc.setIndented(indentedRadioButton.isSelected());
		gmc.setReindex(reindexYesRadioButton.isSelected());
		if (firstUsageButton.isSelected())
			gmc.setUsageVersionSelectionMode(FIRST_DATAOBJECT);
		else  if (allUsageButton.isSelected())
			gmc.setUsageVersionSelectionMode(ALL_DATAOBJECTS);
		else
			gmc.setUsageVersionSelectionMode(LAST_DATAOBJECT);
		try {
			tmp = Integer.parseInt(nameMaxSizeTextField.getText());
			if (tmp <= 0)
				throw new NumberFormatException("Number not strictly positive");
		} catch (NumberFormatException e) {
			tabbedPane.setSelectedIndex(3);
			UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow,
					"La taille limite des noms de répertoires exportées doit être un nombre strictement supérieur à 0.",
					"Information", UserInteractionDialog.IMPORTANT_DIALOG,
					null);
			return false;
		}
		gmc.setMaxNameSize(tmp);
		gmc.setManagementMetadataXmlData(managementMetadataTextArea.getText());
		gmc.setMetadataFilterFlag(metadataFilterCheckBox.isSelected());
		gmc.setKeptMetadataList(Arrays.asList(metadataFilterTextArea.getText().split("\\s*\n\\s*"))
				.stream().map(String::trim).collect(Collectors.toList()));

		GlobalMetadata atgm=gmc.getArchiveTransferGlobalMetadata();
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
		return true;
	}

	/**
	 * Get return value int.
	 *
	 * @return the return value
	 */
	public int getReturnValue(){
		return returnValue;
	}
}
