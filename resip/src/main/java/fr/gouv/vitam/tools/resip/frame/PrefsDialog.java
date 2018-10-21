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
package fr.gouv.vitam.tools.resip.frame;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.MailImportContext;
import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;

// TODO: Auto-generated Javadoc

/**
 * The Class PrefsDialog.
 */
public class PrefsDialog extends JDialog {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -4092514078236156033L;

    /**
     * The work dir text field.
     */
    private JTextField workDirTextField;

    /**
     * The ignore patterns text area.
     */
    private JTextArea ignorePatternsTextArea;

    /**
     * The hierarchical radio button.
     */
    private JRadioButton hierarchicalRadioButton;

    /**
     * The indented radio button.
     */
    private JRadioButton indentedRadioButton;

    /**
     * The reindex radio button.
     */
    private JRadioButton reindexYesRadioButton;

    /**
     * The message identifier text field.
     */
    private JTextField messageIdentifierTextField;

    /**
     * The date text field.
     */
    private JTextField dateTextField;

    /**
     * The chckbx now flag.
     */
    private JCheckBox chckbxNowFlag;

    /**
     * The comment text area.
     */
    private JTextArea commentTextArea;

    /**
     * The archival agreement text field.
     */
    private JTextField archivalAgreementTextField;

    /**
     * The clv text area.
     */
    private JTextArea clvTextArea;

    /**
     * The management metadata text area.
     */
    private JTextArea managementMetadataTextArea;

    /**
     * The transfer request reply identifier text field.
     */
    private JTextField transferRequestReplyIdentifierTextField;

    /**
     * The archival agency identifier text field.
     */
    private JTextField archivalAgencyIdentifierTextField;

    /**
     * The archival agency organization descriptive metadata text area.
     */
    private JTextArea archivalAgencyOrganizationDescriptiveMetadataTextArea;

    /**
     * The transferring agency identifier text field.
     */
    private JTextField transferringAgencyIdentifierTextField;

    /**
     * The transferring agency organization descriptive metadata text area.
     */
    private JTextArea transferringAgencyOrganizationDescriptiveMetadataTextArea;

    /**
     * The message file check box.
     */
    private JCheckBox messageFileCheckBox;

    /**
     * The attachement file check box.
     */
    private JCheckBox attachementFileCheckBox;

    /**
     * The message metadata check box.
     */
    private JCheckBox messageMetadataCheckBox;

    /**
     * The attachement metadata check box.
     */
    private JCheckBox attachementMetadataCheckBox;

    /**
     * The pst radio button.
     */
    private JRadioButton pstRadioButton;

    /**
     * The msg radio button.
     */
    private JRadioButton msgRadioButton;

    /**
     * The tdb radio button.
     */
    private JRadioButton tdbRadioButton;

    /**
     * The mbox radio button.
     */
    private JRadioButton mboxRadioButton;

    /**
     * The cc.
     */
    public CreationContext cc;

    /**
     * The dic.
     */
    public DiskImportContext dic;

    /**
     * The mic.
     */
    public MailImportContext mic;

    /**
     * The gmc.
     */
    public ExportContext gmc;

    /**
     * The return value.
     */
    public int returnValue;

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public PrefsDialog(JFrame owner) {
        super(owner, "Edition des paramètres par défaut", true);

        cc = new CreationContext(Prefs.getInstance().getPrefsContextNode());
        dic = new DiskImportContext(Prefs.getInstance().getPrefsContextNode());
        mic = new MailImportContext(Prefs.getInstance().getPrefsContextNode());
        gmc = new ExportContext(Prefs.getInstance().getPrefsContextNode());

        getContentPane().setPreferredSize(new Dimension(800, 500));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{1.0, 0.1};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0};
        getContentPane().setLayout(gridBagLayout);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
        gbc_tabbedPane.fill = GridBagConstraints.BOTH;
        gbc_tabbedPane.gridwidth = 2;
        gbc_tabbedPane.insets = new Insets(0, 0, 5, 5);
        gbc_tabbedPane.gridx = 0;
        gbc_tabbedPane.gridy = 0;
        getContentPane().add(tabbedPane, gbc_tabbedPane);

        // header and footer simple fields
        JPanel headerFooterSimplePanel = new JPanel();
        tabbedPane.addTab("Métadonnées globales", null, headerFooterSimplePanel, null);
        GridBagLayout gbl_headerFooterSimplePanel = new GridBagLayout();
        gbl_headerFooterSimplePanel.columnWidths = new int[]{0, 0, 0};
        gbl_headerFooterSimplePanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gbl_headerFooterSimplePanel.columnWeights = new double[]{0.0, 1.0, 0.0};
        gbl_headerFooterSimplePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
        headerFooterSimplePanel.setLayout(gbl_headerFooterSimplePanel);

        JLabel presentationLabel = new JLabel("Champs globaux du SIP");
        GridBagConstraints gbc_presentationLabel = new GridBagConstraints();
        gbc_presentationLabel.gridwidth = 3;
        gbc_presentationLabel.insets = new Insets(0, 0, 5, 0);
        gbc_presentationLabel.weightx = 1.0;
        gbc_presentationLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_presentationLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_presentationLabel.gridx = 0;
        gbc_presentationLabel.gridy = 0;
        headerFooterSimplePanel.add(presentationLabel, gbc_presentationLabel);

        JLabel messageIdentifierLabel = new JLabel("Identifiant du message (MessageIdentifier):");
        GridBagConstraints gbc_messageIdentifierLabel = new GridBagConstraints();
        gbc_messageIdentifierLabel.anchor = GridBagConstraints.EAST;
        gbc_messageIdentifierLabel.insets = new Insets(0, 0, 5, 5);
        gbc_messageIdentifierLabel.gridx = 0;
        gbc_messageIdentifierLabel.gridy = 1;
        headerFooterSimplePanel.add(messageIdentifierLabel, gbc_messageIdentifierLabel);

        messageIdentifierTextField = new JTextField();
        messageIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().messageIdentifier);
        GridBagConstraints gbc_messageIdentifierTextField = new GridBagConstraints();
        gbc_messageIdentifierTextField.weightx = 1.0;
        gbc_messageIdentifierTextField.insets = new Insets(0, 0, 5, 0);
        gbc_messageIdentifierTextField.gridwidth = 2;
        gbc_messageIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_messageIdentifierTextField.gridx = 1;
        gbc_messageIdentifierTextField.gridy = 1;
        headerFooterSimplePanel.add(messageIdentifierTextField, gbc_messageIdentifierTextField);
        messageIdentifierTextField.setColumns(10);

        JLabel dateLabel = new JLabel("Date (ISO 8601):");
        GridBagConstraints gbc_dateLabel = new GridBagConstraints();
        gbc_dateLabel.anchor = GridBagConstraints.EAST;
        gbc_dateLabel.insets = new Insets(0, 0, 5, 5);
        gbc_dateLabel.gridx = 0;
        gbc_dateLabel.gridy = 2;
        headerFooterSimplePanel.add(dateLabel, gbc_dateLabel);

        dateTextField = new JTextField();
        dateTextField.setText(gmc.getArchiveTransferGlobalMetadata().date);
        GridBagConstraints gbc_dateTextField = new GridBagConstraints();
        gbc_dateTextField.insets = new Insets(0, 0, 5, 5);
        gbc_dateTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_dateTextField.gridx = 1;
        gbc_dateTextField.gridy = 2;
        headerFooterSimplePanel.add(dateTextField, gbc_dateTextField);
        dateTextField.setColumns(10);

        chckbxNowFlag = new JCheckBox("du jour");
        chckbxNowFlag.setSelected(gmc.getArchiveTransferGlobalMetadata().isNowFlag());
        GridBagConstraints gbc_chckbxNowFlag = new GridBagConstraints();
        gbc_chckbxNowFlag.insets = new Insets(0, 0, 5, 0);
        gbc_chckbxNowFlag.gridx = 2;
        gbc_chckbxNowFlag.gridy = 2;
        headerFooterSimplePanel.add(chckbxNowFlag, gbc_chckbxNowFlag);

        JLabel lblComment = new JLabel("Commentaire (Comment):");
        GridBagConstraints gbc_lblComment = new GridBagConstraints();
        gbc_lblComment.anchor = GridBagConstraints.EAST;
        gbc_lblComment.insets = new Insets(0, 0, 5, 5);
        gbc_lblComment.gridx = 0;
        gbc_lblComment.gridy = 3;
        headerFooterSimplePanel.add(lblComment, gbc_lblComment);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridwidth = 2;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 3;
        headerFooterSimplePanel.add(scrollPane, gbc_scrollPane);

        commentTextArea = new JTextArea();
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.setCaretPosition(0);
        scrollPane.setViewportView(commentTextArea);
        commentTextArea.setText(gmc.getArchiveTransferGlobalMetadata().comment);

        JLabel lblArchivalagreement = new JLabel("Identifiant du contrat d'entrée (ArchivalAgreement):");
        GridBagConstraints gbc_lblArchivalagreement = new GridBagConstraints();
        gbc_lblArchivalagreement.anchor = GridBagConstraints.EAST;
        gbc_lblArchivalagreement.insets = new Insets(0, 0, 5, 5);
        gbc_lblArchivalagreement.gridx = 0;
        gbc_lblArchivalagreement.gridy = 4;
        headerFooterSimplePanel.add(lblArchivalagreement, gbc_lblArchivalagreement);

        archivalAgreementTextField = new JTextField();
        archivalAgreementTextField.setText(gmc.getArchiveTransferGlobalMetadata().archivalAgreement);
        GridBagConstraints gbc_archivalAgreementTextField = new GridBagConstraints();
        gbc_archivalAgreementTextField.insets = new Insets(0, 0, 5, 0);
        gbc_archivalAgreementTextField.gridwidth = 2;
        gbc_archivalAgreementTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_archivalAgreementTextField.gridx = 1;
        gbc_archivalAgreementTextField.gridy = 4;
        headerFooterSimplePanel.add(archivalAgreementTextField, gbc_archivalAgreementTextField);
        archivalAgreementTextField.setColumns(10);

        JLabel lblArchivalAgencyIdentifier = new JLabel("Service d'archivage (ArchivalAgency.Identifier):");
        GridBagConstraints gbc_lblArchivalAgencyIdentifier = new GridBagConstraints();
        gbc_lblArchivalAgencyIdentifier.anchor = GridBagConstraints.EAST;
        gbc_lblArchivalAgencyIdentifier.insets = new Insets(0, 0, 5, 5);
        gbc_lblArchivalAgencyIdentifier.gridx = 0;
        gbc_lblArchivalAgencyIdentifier.gridy = 5;
        headerFooterSimplePanel.add(lblArchivalAgencyIdentifier, gbc_lblArchivalAgencyIdentifier);

        archivalAgencyIdentifierTextField = new JTextField();
        archivalAgencyIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().archivalAgencyIdentifier);
        GridBagConstraints gbc_archivalAgencyIdentifierTextField = new GridBagConstraints();
        gbc_archivalAgencyIdentifierTextField.insets = new Insets(0, 0, 5, 0);
        gbc_archivalAgencyIdentifierTextField.gridwidth = 2;
        gbc_archivalAgencyIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_archivalAgencyIdentifierTextField.gridx = 1;
        gbc_archivalAgencyIdentifierTextField.gridy = 5;
        headerFooterSimplePanel.add(archivalAgencyIdentifierTextField, gbc_archivalAgencyIdentifierTextField);
        archivalAgencyIdentifierTextField.setColumns(10);

        JLabel lblTransferringAgencyIdentifier = new JLabel("Service versant (TransferringAgency.Identifier):");
        GridBagConstraints gbc_lblTransferringAgencyIdentifier = new GridBagConstraints();
        gbc_lblTransferringAgencyIdentifier.anchor = GridBagConstraints.EAST;
        gbc_lblTransferringAgencyIdentifier.insets = new Insets(0, 0, 5, 5);
        gbc_lblTransferringAgencyIdentifier.gridx = 0;
        gbc_lblTransferringAgencyIdentifier.gridy = 6;
        headerFooterSimplePanel.add(lblTransferringAgencyIdentifier, gbc_lblTransferringAgencyIdentifier);

        transferringAgencyIdentifierTextField = new JTextField();
        transferringAgencyIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().transferringAgencyIdentifier);
        GridBagConstraints gbc_transferringAgencyIdentifierTextField = new GridBagConstraints();
        gbc_transferringAgencyIdentifierTextField.insets = new Insets(0, 0, 5, 0);
        gbc_transferringAgencyIdentifierTextField.gridwidth = 2;
        gbc_transferringAgencyIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_transferringAgencyIdentifierTextField.gridx = 1;
        gbc_transferringAgencyIdentifierTextField.gridy = 6;
        headerFooterSimplePanel.add(transferringAgencyIdentifierTextField, gbc_transferringAgencyIdentifierTextField);
        transferringAgencyIdentifierTextField.setColumns(10);

        // Header and footer complex fields
        JPanel headerFooterComplexPanel = new JPanel();
        tabbedPane.addTab("Métadonnées globales étendues", null, headerFooterComplexPanel, null);
        GridBagLayout gbl_headerFooterComplexPanel = new GridBagLayout();
        gbl_headerFooterComplexPanel.columnWidths = new int[]{0, 0, 0};
        gbl_headerFooterComplexPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
        gbl_headerFooterComplexPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
        gbl_headerFooterComplexPanel.rowWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.5, 0.5, 0.0};
        headerFooterComplexPanel.setLayout(gbl_headerFooterComplexPanel);

        JLabel presentationComplexLabel = new JLabel("Champs globaux étendus du SIP");
        GridBagConstraints gbc_presentationComplexLabel = new GridBagConstraints();
        gbc_presentationComplexLabel.gridwidth = 3;
        gbc_presentationComplexLabel.insets = new Insets(0, 0, 5, 0);
        gbc_presentationComplexLabel.weightx = 1.0;
        gbc_presentationComplexLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_presentationComplexLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_presentationComplexLabel.gridx = 0;
        gbc_presentationComplexLabel.gridy = 0;
        headerFooterComplexPanel.add(presentationComplexLabel, gbc_presentationComplexLabel);

        JLabel clvLabel = new JLabel("CodeListVersions (XML):");
        GridBagConstraints gbc_clvLabel = new GridBagConstraints();
        gbc_clvLabel.anchor = GridBagConstraints.EAST;
        gbc_clvLabel.insets = new Insets(0, 0, 5, 5);
        gbc_clvLabel.gridx = 0;
        gbc_clvLabel.gridy = 1;
        headerFooterComplexPanel.add(clvLabel, gbc_clvLabel);

        JScrollPane scrollPane_1 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridwidth = 2;
        gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_1.gridx = 1;
        gbc_scrollPane_1.gridy = 1;
        headerFooterComplexPanel.add(scrollPane_1, gbc_scrollPane_1);

        clvTextArea = new JTextArea();
        scrollPane_1.setViewportView(clvTextArea);
        clvTextArea.setText(gmc.getArchiveTransferGlobalMetadata().codeListVersionsXmlData);
        clvTextArea.setCaretPosition(0);

        JLabel managementMetadataLabel = new JLabel("ManagementMetadata (XML):");
        GridBagConstraints gbc_managementMetadataLabel = new GridBagConstraints();
        gbc_managementMetadataLabel.anchor = GridBagConstraints.EAST;
        gbc_managementMetadataLabel.insets = new Insets(0, 0, 5, 5);
        gbc_managementMetadataLabel.gridx = 0;
        gbc_managementMetadataLabel.gridy = 2;
        headerFooterComplexPanel.add(managementMetadataLabel, gbc_managementMetadataLabel);

        JScrollPane scrollPane_2 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
        gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_2.gridwidth = 2;
        gbc_scrollPane_2.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_2.gridx = 1;
        gbc_scrollPane_2.gridy = 2;
        headerFooterComplexPanel.add(scrollPane_2, gbc_scrollPane_2);

        managementMetadataTextArea = new JTextArea();
        scrollPane_2.setViewportView(managementMetadataTextArea);
        managementMetadataTextArea.setText(gmc.getManagementMetadataXmlData());
        managementMetadataTextArea.setCaretPosition(0);

        JLabel lblTransferRequestReplyIdentifier = new JLabel("TransferRequestReplyIdentifier:");
        GridBagConstraints gbc_lblTransferRequestReplyIdentifier = new GridBagConstraints();
        gbc_lblTransferRequestReplyIdentifier.anchor = GridBagConstraints.EAST;
        gbc_lblTransferRequestReplyIdentifier.insets = new Insets(0, 0, 5, 5);
        gbc_lblTransferRequestReplyIdentifier.gridx = 0;
        gbc_lblTransferRequestReplyIdentifier.gridy = 3;
        headerFooterComplexPanel.add(lblTransferRequestReplyIdentifier, gbc_lblTransferRequestReplyIdentifier);

        transferRequestReplyIdentifierTextField = new JTextField();
        transferRequestReplyIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier);
        GridBagConstraints gbc_transferRequestReplyIdentifierTextField = new GridBagConstraints();
        gbc_transferRequestReplyIdentifierTextField.insets = new Insets(0, 0, 5, 0);
        gbc_transferRequestReplyIdentifierTextField.gridwidth = 2;
        gbc_transferRequestReplyIdentifierTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_transferRequestReplyIdentifierTextField.gridx = 1;
        gbc_transferRequestReplyIdentifierTextField.gridy = 3;
        headerFooterComplexPanel.add(transferRequestReplyIdentifierTextField,
                gbc_transferRequestReplyIdentifierTextField);
        transferRequestReplyIdentifierTextField.setColumns(10);

        JLabel archivalAgencyOrganizationDescriptiveMetadataLabel = new JLabel("ArchivalAgency.ODM (XML):");
        GridBagConstraints gbc_archivalAgencyOrganizationDescriptiveMetadataLabel = new GridBagConstraints();
        gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.anchor = GridBagConstraints.EAST;
        gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.insets = new Insets(0, 0, 5, 5);
        gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.gridx = 0;
        gbc_archivalAgencyOrganizationDescriptiveMetadataLabel.gridy = 4;
        headerFooterComplexPanel.add(archivalAgencyOrganizationDescriptiveMetadataLabel,
                gbc_archivalAgencyOrganizationDescriptiveMetadataLabel);

        JScrollPane scrollPane_3 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
        gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_3.gridwidth = 2;
        gbc_scrollPane_3.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane_3.gridx = 1;
        gbc_scrollPane_3.gridy = 4;
        headerFooterComplexPanel.add(scrollPane_3, gbc_scrollPane_3);

        archivalAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
        scrollPane_3.setViewportView(archivalAgencyOrganizationDescriptiveMetadataTextArea);
        archivalAgencyOrganizationDescriptiveMetadataTextArea
                .setText(gmc.getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData);
        archivalAgencyOrganizationDescriptiveMetadataTextArea.setCaretPosition(0);

        JLabel transferringAgencyOrganizationDescriptiveMetadataLabel = new JLabel("TransferringAgency.ODM (XML):");
        GridBagConstraints gbc_transferringAgencyOrganizationDescriptiveMetadataLabel = new GridBagConstraints();
        gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.anchor = GridBagConstraints.EAST;
        gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.insets = new Insets(0, 0, 0, 5);
        gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.gridx = 0;
        gbc_transferringAgencyOrganizationDescriptiveMetadataLabel.gridy = 5;
        headerFooterComplexPanel.add(transferringAgencyOrganizationDescriptiveMetadataLabel,
                gbc_transferringAgencyOrganizationDescriptiveMetadataLabel);

        JScrollPane scrollPane_4 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
        gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_4.gridwidth = 2;
        gbc_scrollPane_4.insets = new Insets(0, 0, 0, 5);
        gbc_scrollPane_4.gridx = 1;
        gbc_scrollPane_4.gridy = 5;
        headerFooterComplexPanel.add(scrollPane_4, gbc_scrollPane_4);

        transferringAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
        scrollPane_4.setViewportView(transferringAgencyOrganizationDescriptiveMetadataTextArea);
        transferringAgencyOrganizationDescriptiveMetadataTextArea
                .setText(gmc.getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData);
        transferringAgencyOrganizationDescriptiveMetadataTextArea.setCaretPosition(0);

        // Parameters Panel

        JPanel parametersPanel = new JPanel();
        tabbedPane.addTab("Import/Export", null, parametersPanel, null);
        GridBagLayout gbl_parametersPanel = new GridBagLayout();
        gbl_parametersPanel.columnWeights = new double[]{0.1, 0.1, 0.5, 0.1};
        parametersPanel.setLayout(gbl_parametersPanel);

        JLabel parametersLabel = new JLabel("Paramètres d'import/export");
        GridBagConstraints gbc_parametersLabel = new GridBagConstraints();
        gbc_parametersLabel.insets = new Insets(0, 0, 5, 5);
        gbc_parametersLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_parametersLabel.gridx = 0;
        gbc_parametersLabel.gridy = 0;
        gbc_parametersLabel.gridwidth = 2;
        parametersPanel.add(parametersLabel, gbc_parametersLabel);

        JLabel workDirLabel = new JLabel("Rép. de travail:");
        GridBagConstraints gbc_workDirLabel = new GridBagConstraints();
        gbc_workDirLabel.anchor = GridBagConstraints.EAST;
        gbc_workDirLabel.insets = new Insets(0, 0, 5, 5);
        gbc_workDirLabel.gridx = 0;
        gbc_workDirLabel.gridy = 1;
        parametersPanel.add(workDirLabel, gbc_workDirLabel);

        workDirTextField = new JTextField();
        workDirTextField.setText(cc.getWorkDir());
        GridBagConstraints gbc_workDirTextField = new GridBagConstraints();
        gbc_workDirTextField.weightx = 1.0;
        gbc_workDirTextField.gridwidth = 2;
        gbc_workDirTextField.insets = new Insets(0, 0, 5, 5);
        gbc_workDirTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_workDirTextField.gridx = 1;
        gbc_workDirTextField.gridy = 1;
        parametersPanel.add(workDirTextField, gbc_workDirTextField);
        workDirTextField.setColumns(10);

        JButton workDirButton = new JButton("Choisir...");
        GridBagConstraints gbc_workDirButton = new GridBagConstraints();
        gbc_workDirButton.insets = new Insets(0, 0, 5, 0);
        gbc_workDirButton.gridx = 3;
        gbc_workDirButton.gridy = 1;
        parametersPanel.add(workDirButton, gbc_workDirButton);
        workDirButton.addActionListener(arg0 -> buttonChooseWorkDir());

        JLabel hierarchicalAULabel = new JLabel("Export des AU dans le SIP:");
        GridBagConstraints gbc_hierarchicalAULabel = new GridBagConstraints();
        gbc_hierarchicalAULabel.anchor = GridBagConstraints.EAST;
        gbc_hierarchicalAULabel.insets = new Insets(0, 0, 5, 5);
        gbc_hierarchicalAULabel.gridx = 0;
        gbc_hierarchicalAULabel.gridy = 2;
        parametersPanel.add(hierarchicalAULabel, gbc_hierarchicalAULabel);

        JRadioButton flatRadioButton = new JRadioButton("A plat");
        GridBagConstraints gbc_flatRadioButton = new GridBagConstraints();
        gbc_flatRadioButton.anchor = GridBagConstraints.WEST;
        gbc_flatRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_flatRadioButton.gridx = 2;
        gbc_flatRadioButton.gridy = 2;
        parametersPanel.add(flatRadioButton, gbc_flatRadioButton);

        hierarchicalRadioButton = new JRadioButton("Imbriquées");
        GridBagConstraints gbc_hierarchicalRadioButton = new GridBagConstraints();
        gbc_hierarchicalRadioButton.anchor = GridBagConstraints.WEST;
        gbc_hierarchicalRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_hierarchicalRadioButton.gridx = 1;
        gbc_hierarchicalRadioButton.gridy = 2;
        parametersPanel.add(hierarchicalRadioButton, gbc_hierarchicalRadioButton);

        ButtonGroup hierarchicalAUButtonGroup = new ButtonGroup();
        hierarchicalAUButtonGroup.add(flatRadioButton);
        hierarchicalAUButtonGroup.add(hierarchicalRadioButton);
        hierarchicalAUButtonGroup.clearSelection();
        if (gmc.isHierarchicalArchiveUnits())
            hierarchicalRadioButton.setSelected(true);
        else
            flatRadioButton.setSelected(true);

        JLabel xmlPresentationLabel = new JLabel("Présentation XML dans le SIP:");
        GridBagConstraints gbc_xmlPresentationLabel = new GridBagConstraints();
        gbc_xmlPresentationLabel.anchor = GridBagConstraints.EAST;
        gbc_xmlPresentationLabel.insets = new Insets(0, 0, 0, 5);
        gbc_xmlPresentationLabel.gridx = 0;
        gbc_xmlPresentationLabel.gridy = 3;
        parametersPanel.add(xmlPresentationLabel, gbc_xmlPresentationLabel);

        JRadioButton linearRadioButton = new JRadioButton("Linéaire");
        GridBagConstraints gbc_linearRadioButton = new GridBagConstraints();
        gbc_linearRadioButton.anchor = GridBagConstraints.WEST;
        gbc_linearRadioButton.insets = new Insets(0, 0, 0, 5);
        gbc_linearRadioButton.gridx = 2;
        gbc_linearRadioButton.gridy = 3;
        parametersPanel.add(linearRadioButton, gbc_linearRadioButton);

        indentedRadioButton = new JRadioButton("Indentée");
        GridBagConstraints gbc_indentedRadioButton = new GridBagConstraints();
        gbc_indentedRadioButton.anchor = GridBagConstraints.WEST;
        gbc_indentedRadioButton.gridx = 1;
        gbc_indentedRadioButton.gridy = 3;
        parametersPanel.add(indentedRadioButton, gbc_indentedRadioButton);

        ButtonGroup indentedButtonGroup = new ButtonGroup();
        indentedButtonGroup.add(linearRadioButton);
        indentedButtonGroup.add(indentedRadioButton);
        indentedButtonGroup.clearSelection();
        if (gmc.isIndented())
            indentedRadioButton.setSelected(true);
        else
            linearRadioButton.setSelected(true);

        JLabel xmlReindexLabel = new JLabel("Renumérotation des éléments XML avant export:");
        GridBagConstraints gbc_xmlReindexLabel = new GridBagConstraints();
        gbc_xmlReindexLabel.anchor = GridBagConstraints.EAST;
        gbc_xmlReindexLabel.insets = new Insets(0, 0, 0, 5);
        gbc_xmlReindexLabel.gridx = 0;
        gbc_xmlReindexLabel.gridy = 4;
        parametersPanel.add(xmlReindexLabel, gbc_xmlReindexLabel);

        reindexYesRadioButton = new JRadioButton("Oui");
        GridBagConstraints gbc_reindexYesRadioButton = new GridBagConstraints();
        gbc_reindexYesRadioButton.anchor = GridBagConstraints.WEST;
        gbc_reindexYesRadioButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_reindexYesRadioButton.insets = new Insets(0, 0, 0, 5);
        gbc_reindexYesRadioButton.gridx = 1;
        gbc_reindexYesRadioButton.gridy = 4;
        parametersPanel.add(reindexYesRadioButton, gbc_reindexYesRadioButton);

        JRadioButton reindexNoRadioButton = new JRadioButton("Non");
        GridBagConstraints gbc_reindexNoRadioButton = new GridBagConstraints();
        gbc_reindexNoRadioButton.anchor = GridBagConstraints.WEST;
        gbc_reindexNoRadioButton.gridx = 2;
        gbc_reindexNoRadioButton.gridy = 4;
        parametersPanel.add(reindexNoRadioButton, gbc_reindexNoRadioButton);

        ButtonGroup reindexButtonGroup = new ButtonGroup();
        reindexButtonGroup.add(reindexYesRadioButton);
        reindexButtonGroup.add(reindexNoRadioButton);
        reindexButtonGroup.clearSelection();
        if (gmc.isReindex())
            reindexYesRadioButton.setSelected(true);
        else
            reindexNoRadioButton.setSelected(true);


        JLabel protocolLabel = new JLabel("Protocole d'extraction courriel:");
        GridBagConstraints gbc_protocolLabel = new GridBagConstraints();
        gbc_protocolLabel.anchor = GridBagConstraints.EAST;
        gbc_protocolLabel.insets = new Insets(0, 0, 0, 5);
        gbc_protocolLabel.gridx = 0;
        gbc_protocolLabel.gridy = 5;
        parametersPanel.add(protocolLabel, gbc_protocolLabel);

        pstRadioButton = new JRadioButton("Outlook-Pst");
        GridBagConstraints gbc_pstRadioButton = new GridBagConstraints();
        gbc_pstRadioButton.anchor = GridBagConstraints.WEST;
        gbc_pstRadioButton.insets = new Insets(0, 0, 0, 5);
        gbc_pstRadioButton.gridx = 1;
        gbc_pstRadioButton.gridy = 5;
        parametersPanel.add(pstRadioButton, gbc_pstRadioButton);

        msgRadioButton = new JRadioButton("Outlook-Msg");
        GridBagConstraints gbc_msgRadioButton = new GridBagConstraints();
        gbc_msgRadioButton.anchor = GridBagConstraints.WEST;
        gbc_msgRadioButton.insets = new Insets(0, 0, 0, 5);
        gbc_msgRadioButton.gridx = 2;
        gbc_msgRadioButton.gridy = 5;
        parametersPanel.add(msgRadioButton, gbc_msgRadioButton);

        tdbRadioButton = new JRadioButton("Thunderbird");
        GridBagConstraints gbc_tdbRadioButton = new GridBagConstraints();
        gbc_tdbRadioButton.anchor = GridBagConstraints.WEST;
        gbc_tdbRadioButton.insets = new Insets(0, 0, 0, 5);
        gbc_tdbRadioButton.gridx = 1;
        gbc_tdbRadioButton.gridy = 6;
        parametersPanel.add(tdbRadioButton, gbc_tdbRadioButton);

        mboxRadioButton = new JRadioButton("Mbox");
        GridBagConstraints gbc_mboxRadioButton = new GridBagConstraints();
        gbc_mboxRadioButton.anchor = GridBagConstraints.WEST;
        gbc_mboxRadioButton.insets = new Insets(0, 0, 0, 5);
        gbc_mboxRadioButton.gridx = 2;
        gbc_mboxRadioButton.gridy = 6;
        parametersPanel.add(mboxRadioButton, gbc_mboxRadioButton);

        ButtonGroup protocolButtonGroup = new ButtonGroup();
        protocolButtonGroup.add(pstRadioButton);
        protocolButtonGroup.add(msgRadioButton);
        protocolButtonGroup.add(tdbRadioButton);
        protocolButtonGroup.add(mboxRadioButton);
        pstRadioButton.setSelected("pst".equals(mic.getProtocol()));
        msgRadioButton.setSelected("msg".equals(mic.getProtocol()));
        tdbRadioButton.setSelected("thunderbird".equals(mic.getProtocol()));
        mboxRadioButton.setSelected("mbox".equals(mic.getProtocol()));

        JLabel mailFileTextExtractAULabel = new JLabel("Extraction des fichiers textes des courriels:");
        GridBagConstraints gbc_mailFileTextExtractAULabel = new GridBagConstraints();
        gbc_mailFileTextExtractAULabel.anchor = GridBagConstraints.EAST;
        gbc_mailFileTextExtractAULabel.insets = new Insets(0, 0, 5, 5);
        gbc_mailFileTextExtractAULabel.gridx = 0;
        gbc_mailFileTextExtractAULabel.gridy = 7;
        parametersPanel.add(mailFileTextExtractAULabel, gbc_mailFileTextExtractAULabel);

        messageFileCheckBox = new JCheckBox("des messages");
        GridBagConstraints gbc_messageFileCheckBox = new GridBagConstraints();
        gbc_messageFileCheckBox.anchor = GridBagConstraints.WEST;
        gbc_messageFileCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_messageFileCheckBox.gridx = 1;
        gbc_messageFileCheckBox.gridy = 7;
        parametersPanel.add(messageFileCheckBox, gbc_messageFileCheckBox);
        messageFileCheckBox.setSelected(mic.isExtractMessageTextFile());

        attachementFileCheckBox = new JCheckBox("des pièces jointes");
        GridBagConstraints gbc_attachementFileCheckBox = new GridBagConstraints();
        gbc_attachementFileCheckBox.anchor = GridBagConstraints.WEST;
        gbc_attachementFileCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_attachementFileCheckBox.gridx = 2;
        gbc_attachementFileCheckBox.gridy = 7;
        parametersPanel.add(attachementFileCheckBox, gbc_attachementFileCheckBox);
        attachementFileCheckBox.setSelected(mic.isExtractAttachmentTextFile());

        JLabel mailMetadataTextExtractAULabel = new JLabel("Extraction des métadonnées textuelles des courriels:");
        GridBagConstraints gbc_mailMetadataTextExtractAULabel = new GridBagConstraints();
        gbc_mailMetadataTextExtractAULabel.anchor = GridBagConstraints.EAST;
        gbc_mailMetadataTextExtractAULabel.insets = new Insets(0, 0, 5, 5);
        gbc_mailMetadataTextExtractAULabel.gridx = 0;
        gbc_mailMetadataTextExtractAULabel.gridy = 8;
        parametersPanel.add(mailMetadataTextExtractAULabel, gbc_mailMetadataTextExtractAULabel);

        messageMetadataCheckBox = new JCheckBox("des messages");
        GridBagConstraints gbc_messageMetadataCheckBox = new GridBagConstraints();
        gbc_messageMetadataCheckBox.anchor = GridBagConstraints.WEST;
        gbc_messageMetadataCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_messageMetadataCheckBox.gridx = 1;
        gbc_messageMetadataCheckBox.gridy = 8;
        parametersPanel.add(messageMetadataCheckBox, gbc_messageMetadataCheckBox);
        messageMetadataCheckBox.setSelected(mic.isExtractMessageTextMetadata());

        attachementMetadataCheckBox = new JCheckBox("des pièces jointes");
        GridBagConstraints gbc_attachementMetadataCheckBox = new GridBagConstraints();
        gbc_attachementMetadataCheckBox.anchor = GridBagConstraints.WEST;
        gbc_attachementMetadataCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_attachementMetadataCheckBox.gridx = 2;
        gbc_attachementMetadataCheckBox.gridy = 8;
        parametersPanel.add(attachementMetadataCheckBox, gbc_attachementMetadataCheckBox);
        attachementMetadataCheckBox.setSelected(mic.isExtractAttachmentTextMetadata());

        JScrollPane scrollPane_5 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_5 = new GridBagConstraints();
        gbc_scrollPane_5.weightx = 1.0;
        gbc_scrollPane_5.weighty = 1.0;
        gbc_scrollPane_5.gridwidth = 2;
        gbc_scrollPane_5.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_5.gridheight = 2;
        gbc_scrollPane_5.gridx = 1;
        gbc_scrollPane_5.gridy = 9;
        parametersPanel.add(scrollPane_5, gbc_scrollPane_5);

        ignorePatternsTextArea = new JTextArea();
        scrollPane_5.setViewportView(ignorePatternsTextArea);

        JLabel ignorePatternsLabel = new JLabel("Fichiers exclus des imports:");
        GridBagConstraints gbc_ignorePatternsLabel = new GridBagConstraints();
        gbc_ignorePatternsLabel.gridwidth = 1;
        gbc_ignorePatternsLabel.anchor = GridBagConstraints.EAST;
        gbc_ignorePatternsLabel.insets = new Insets(0, 0, 0, 5);
        gbc_ignorePatternsLabel.gridx = 0;
        gbc_ignorePatternsLabel.gridy = 9;
        parametersPanel.add(ignorePatternsLabel, gbc_ignorePatternsLabel);
        if (dic.getIgnorePatternList() != null) {
            StringBuilder sb = new StringBuilder();
            for (String p : dic.getIgnorePatternList()) {
                sb.append(p).append('\n');
            }
            ignorePatternsTextArea.setText(sb.toString().trim());
        }

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
    private void buttonCancel() {
        returnValue = JOptionPane.CANCEL_OPTION;
        setVisible(false);
    }

    /**
     * Button ok.
     */
    private void buttonOk() {
        extractFromDialog();
        returnValue = JOptionPane.OK_OPTION;
        setVisible(false);
    }

    /**
     * Choose a directory.
     *
     * @return the string
     */
    private String chooseDirectory() {
        JFileChooser fileChooser = new JFileChooser("/home/js/javaworkspace-resip/resip/tmpJunit/");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            try {
                return fileChooser.getSelectedFile().getCanonicalPath();
            } catch (IOException e) {
                return null;
            }
        return null;
    }

    /**
     * Button choose work dir.
     */
    private void buttonChooseWorkDir() {
        try {
            String dir = chooseDirectory();
            if (dir == null)
                return;

            workDirTextField.setText(dir);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur fatale, impossible de sélectionner sur le disque \n->" + e.getMessage());
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,
                    "Resip.GraphicApp: Erreur fatale, impossible de sélectionner sur le disque \n->" + e.getMessage());
        }
    }

    /**
     * Extract from dialog.
     */
    public void extractFromDialog() {
        cc.setWorkDir(workDirTextField.getText());

        dic.setIgnorePatternList(Arrays.asList(ignorePatternsTextArea.getText().split("\\s*\n\\s*")));

        mic.setExtractMessageTextMetadata(messageMetadataCheckBox.isSelected());
        mic.setExtractAttachmentTextMetadata(attachementMetadataCheckBox.isSelected());
        mic.setExtractMessageTextFile(messageFileCheckBox.isSelected());
        mic.setExtractAttachmentTextFile(attachementFileCheckBox.isSelected());
        if (pstRadioButton.isSelected())
            mic.setProtocol("pst");
        else if (msgRadioButton.isSelected())
            mic.setProtocol("msg");
        else if (tdbRadioButton.isSelected())
            mic.setProtocol("thunderbird");
        else if (mboxRadioButton.isSelected())
            mic.setProtocol("mbox");

        gmc.setHierarchicalArchiveUnits(hierarchicalRadioButton.isSelected());
        gmc.setIndented(indentedRadioButton.isSelected());
        gmc.setReindex(reindexYesRadioButton.isSelected());
        gmc.setManagementMetadataXmlData(managementMetadataTextArea.getText());

        gmc.getArchiveTransferGlobalMetadata().comment = commentTextArea.getText();
        gmc.getArchiveTransferGlobalMetadata().date = dateTextField.getText();
        gmc.getArchiveTransferGlobalMetadata().setNowFlag(chckbxNowFlag.isSelected());
        gmc.getArchiveTransferGlobalMetadata().messageIdentifier = messageIdentifierTextField.getText();
        gmc.getArchiveTransferGlobalMetadata().archivalAgreement = archivalAgreementTextField.getText();
        gmc.getArchiveTransferGlobalMetadata().codeListVersionsXmlData = clvTextArea.getText();
        gmc.getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier = transferRequestReplyIdentifierTextField.getText();
        gmc.getArchiveTransferGlobalMetadata().archivalAgencyIdentifier = archivalAgencyIdentifierTextField.getText();
        gmc.getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData =
                archivalAgencyOrganizationDescriptiveMetadataTextArea.getText();
        gmc.getArchiveTransferGlobalMetadata().transferringAgencyIdentifier = transferringAgencyIdentifierTextField.getText();
        gmc.getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData =
                transferringAgencyOrganizationDescriptiveMetadataTextArea.getText();
    }
}
