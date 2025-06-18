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

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.threads.ChangeSeda2VersionThread;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static fr.gouv.vitam.tools.resip.app.ResipGraphicApp.OK_DIALOG;
import static fr.gouv.vitam.tools.resip.utils.ResipLogger.getGlobalLogger;
import static fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter.*;
import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;
import static javax.swing.SwingConstants.TOP;

/**
 * The class PrefsDialog.
 * <p>
 * Class for prefs definition dialog.
 */
public class PrefsDialog extends JDialog {

    /**
     * The actions components.
     */
    private final JTabbedPane tabbedPane;

    private final JTextField messageIdentifierTextField;
    private final JTextField dateTextField;
    private final JCheckBox chckbxNowFlag;
    private final JTextArea commentTextArea;
    private final JTextField archivalAgreementTextField;
    private final JTextField archivalAgencyIdentifierTextField;
    private final JTextField transferringAgencyIdentifierTextField;

    private final JTextArea clvTextArea;
    private final JTextArea managementMetadataTextArea;
    private final JTextField transferRequestReplyIdentifierTextField;
    private final JTextArea archivalAgencyOrganizationDescriptiveMetadataTextArea;
    private final JTextArea transferringAgencyOrganizationDescriptiveMetadataTextArea;

    private final JComboBox<String> defaultMailCharsetCombobox;
    private final JCheckBox messageFileCheckBox;
    private final JCheckBox attachementFileCheckBox;
    private final JCheckBox messageMetadataCheckBox;
    private final JCheckBox attachementMetadataCheckBox;
    private final JTextArea ignorePatternsTextArea;
    private final JCheckBox ignoreLinksChexBox;
    private final JComboBox<String> csvCharsetCombobox;
    private final JTextField csvDelimiterTextField;

    private final JTextField workDirTextField;
    private final JRadioButton hierarchicalRadioButton;
    private final JRadioButton indentedRadioButton;
    private final JRadioButton firstUsageButton;
    private final JRadioButton lastUsageButton;
    private final JRadioButton allUsageButton;
    private final JTextField nameMaxSizeTextField;
    private final JCheckBox csvExtendedFormatChexBox;
    private final JRadioButton reindexYesRadioButton;
    private final JTextArea metadataFilterTextArea;
    private final JCheckBox metadataFilterCheckBox;

    private JTextField maxMetadataSizeTextField;
    private JTextField maxDocumentNumberTextField;
    private JRadioButton zipRadioButton;
    private JCheckBox compactMetadataFilterCheckBox;
    private JTextArea compactMetadataFilterTextArea;
    private JTextArea compactDocumentDataObjectVersionFilterTextArea;
    private JTextArea compactSubDocumentDataObjectVersionFilterTextArea;

    private final JRadioButton seda2Version1RadioButton;
    private final JRadioButton seda2Version2RadioButton;
    private final JRadioButton seda2Version3RadioButton;
    private final JTextField dupMaxTextField;
    private final JRadioButton structuredInterfaceRadioButton;
    private final JCheckBox debugModeCheckBox;
    private final JCheckBox experimentalModeCheckBox;

    private final JFrame owner;

    /**
     * The data.
     */
    public final transient CreationContext cc;
    /**
     * The Coc.
     */
    public final transient CompactContext coc;
    /**
     * The Dic.
     */
    public final transient DiskImportContext dic;
    /**
     * The Mic.
     */
    public final transient MailImportContext mic;
    /**
     * The Gmc.
     */
    public final transient ExportContext gmc;
    /**
     * The Cic.
     */
    public final transient CSVImportContext cic;
    /**
     * The Tp.
     */
    public final transient TreatmentParameters tp;
    /**
     * The Ip.
     */
    public final transient InterfaceParameters ip;

    /**
     * The return value.
     */
    private int returnValue;

    /**
     * The proposed charsets.
     */
    private static final String[] charsetStrings = {"windows-1252", "ISO-8859-1", "UTF-8", "CESU-8", "IBM00858", "IBM437", "IBM775",
            "IBM850", "IBM852", "IBM855", "IBM857", "IBM862", "IBM866", "ISO-8859-2", "ISO-8859-4", "ISO-8859-5",
            "ISO-8859-7", "ISO-8859-9", "ISO-8859-13", "ISO-8859-15", "KOI8-R", "KOI8-U", "US-ASCII", "UTF-16",
            "UTF-16BE", "UTF-16LE", "UTF-32", "UTF-32BE", "UTF-32LE", "x-UTF-32BE-BOM", "x-UTF-32LE-BOM",
            "windows-1250", "windows-1251", "windows-1253", "windows-1254", "windows-1257"};

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
     * @throws ResipException                  the resip exception
     * @throws InterruptedException            the interrupted exception
     */
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ResipException, InterruptedException {
        ResipGraphicApp rga = new ResipGraphicApp(null);//NOSONAR used for debug run
        Thread.sleep(1000);
        TestDialogWindow window = new TestDialogWindow(PrefsDialog.class);//NOSONAR used for debug run
    }

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public PrefsDialog(JFrame owner) {
        super(owner, "Edition des paramètres par défaut", true);
        GridBagConstraints gbc;

        cc = new CreationContext(Prefs.getInstance());
        coc = new CompactContext(Prefs.getInstance());
        dic = new DiskImportContext(Prefs.getInstance());
        mic = new MailImportContext(Prefs.getInstance());
        gmc = new ExportContext(Prefs.getInstance());
        cic = new CSVImportContext(Prefs.getInstance());
        tp = new TreatmentParameters(Prefs.getInstance());
        ip = new InterfaceParameters(Prefs.getInstance());

        this.owner = owner;
        this.setPreferredSize(new Dimension(800, 500));
        this.setMinimumSize(new Dimension(500, 300));

        Container contentPane = getContentPane();
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{1.0, 0.1};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0};
        contentPane.setLayout(new GridBagLayout());

        tabbedPane = new JTabbedPane(TOP);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPane.add(tabbedPane, gbc);

        // header and footer simple fields
        JPanel headerFooterSimplePanel = new JPanel();
        tabbedPane.addTab("Métadonnées globales", new ImageIcon(getClass().getResource("/icon/document-properties.png")), headerFooterSimplePanel, null);
        GridBagLayout gblHeaderFooterSimplePanel = new GridBagLayout();
        gblHeaderFooterSimplePanel.columnWidths = new int[]{0, 0, 0};
        gblHeaderFooterSimplePanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gblHeaderFooterSimplePanel.columnWeights = new double[]{0.0, 1.0, 0.0};
        gblHeaderFooterSimplePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
        headerFooterSimplePanel.setLayout(gblHeaderFooterSimplePanel);

        JLabel presentationLabel = new JLabel("Champs globaux du SIP");
        presentationLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
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
        messageIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().messageIdentifier);
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
        dateTextField.setText(gmc.getArchiveTransferGlobalMetadata().date);
        dateTextField.setFont(MainWindow.DETAILS_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 2;
        headerFooterSimplePanel.add(dateTextField, gbc);
        dateTextField.setColumns(10);

        chckbxNowFlag = new JCheckBox("du jour");
        chckbxNowFlag.setSelected(gmc.getArchiveTransferGlobalMetadata().isNowFlag());
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
        commentTextArea.setText(gmc.getArchiveTransferGlobalMetadata().comment);
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
        archivalAgreementTextField.setText(gmc.getArchiveTransferGlobalMetadata().archivalAgreement);
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
        archivalAgencyIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().archivalAgencyIdentifier);
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
        transferringAgencyIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().transferringAgencyIdentifier);
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
        tabbedPane.addTab("Métadonnées globales étendues", new ImageIcon(getClass().getResource("/icon/text-x-generic.png")), headerFooterComplexPanel, null);
        GridBagLayout gblHeaderFooterComplexPanel = new GridBagLayout();
        gblHeaderFooterComplexPanel.columnWidths = new int[]{0, 0, 0};
        gblHeaderFooterComplexPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
        gblHeaderFooterComplexPanel.columnWeights = new double[]{0.0, 1.0, 0.0};
        gblHeaderFooterComplexPanel.rowWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.5, 0.5, 0.0};
        headerFooterComplexPanel.setLayout(gblHeaderFooterComplexPanel);

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

        JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 1;
        headerFooterComplexPanel.add(scrollPane1, gbc);

        clvTextArea = new JTextArea();
        clvTextArea.setFont(MainWindow.DETAILS_FONT);
        scrollPane1.setViewportView(clvTextArea);
        clvTextArea.setText(gmc.getArchiveTransferGlobalMetadata().codeListVersionsXmlData);
        clvTextArea.setCaretPosition(0);

        JLabel managementMetadataLabel = new JLabel("Métadonnées de gestion globales :");
        managementMetadataLabel.setToolTipText("Bloc XML ManagementMetadata");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 2;
        headerFooterComplexPanel.add(managementMetadataLabel, gbc);

        JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 2;
        headerFooterComplexPanel.add(scrollPane2, gbc);

        managementMetadataTextArea = new JTextArea();
        managementMetadataTextArea.setFont(MainWindow.DETAILS_FONT);
        scrollPane2.setViewportView(managementMetadataTextArea);
        managementMetadataTextArea.setText(gmc.getManagementMetadataXmlData());
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
        transferRequestReplyIdentifierTextField.setText(gmc.getArchiveTransferGlobalMetadata().transferRequestReplyIdentifier);
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

        JScrollPane scrollPane3 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 4;
        headerFooterComplexPanel.add(scrollPane3, gbc);

        archivalAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
        archivalAgencyOrganizationDescriptiveMetadataTextArea.setFont(MainWindow.DETAILS_FONT);
        scrollPane3.setViewportView(archivalAgencyOrganizationDescriptiveMetadataTextArea);
        archivalAgencyOrganizationDescriptiveMetadataTextArea
                .setText(gmc.getArchiveTransferGlobalMetadata().archivalAgencyOrganizationDescriptiveMetadataXmlData);
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

        JScrollPane scrollPane4 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 5;
        headerFooterComplexPanel.add(scrollPane4, gbc);

        transferringAgencyOrganizationDescriptiveMetadataTextArea = new JTextArea();
        transferringAgencyOrganizationDescriptiveMetadataTextArea.setFont(MainWindow.DETAILS_FONT);
        scrollPane4.setViewportView(transferringAgencyOrganizationDescriptiveMetadataTextArea);
        transferringAgencyOrganizationDescriptiveMetadataTextArea
                .setText(gmc.getArchiveTransferGlobalMetadata().transferringAgencyOrganizationDescriptiveMetadataXmlData);
        transferringAgencyOrganizationDescriptiveMetadataTextArea.setCaretPosition(0);

        // ExportParameters Panel
        JPanel exportParametersPanel = new JPanel();
        tabbedPane.addTab("Export", new ImageIcon(getClass().getResource("/icon/document-save.png")), exportParametersPanel, null);
        GridBagLayout gblExportParametersPanel = new GridBagLayout();
        gblExportParametersPanel.columnWeights = new double[]{0, 0.5, 0.5, 0};
        gblExportParametersPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gblExportParametersPanel.rowWeights = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0};
        exportParametersPanel.setLayout(gblExportParametersPanel);

        JLabel inSIPLabel = new JLabel("Options de formation du SIP");
        inSIPLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        exportParametersPanel.add(inSIPLabel, gbc);

        JLabel hierarchicalAULabel = new JLabel("Export des AU dans le SIP:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 3;
        exportParametersPanel.add(hierarchicalAULabel, gbc);

        JRadioButton flatRadioButton = new JRadioButton("A plat");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 3;
        exportParametersPanel.add(flatRadioButton, gbc);

        hierarchicalRadioButton = new JRadioButton("Imbriquées");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 3;
        exportParametersPanel.add(hierarchicalRadioButton, gbc);

        ButtonGroup hierarchicalAUButtonGroup = new ButtonGroup();
        hierarchicalAUButtonGroup.add(flatRadioButton);
        hierarchicalAUButtonGroup.add(hierarchicalRadioButton);
        hierarchicalAUButtonGroup.clearSelection();
        if (gmc.isHierarchicalArchiveUnits())
            hierarchicalRadioButton.setSelected(true);
        else
            flatRadioButton.setSelected(true);

        JLabel xmlPresentationLabel = new JLabel("Présentation XML dans le SIP:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx = 0;
        gbc.gridy = 4;
        exportParametersPanel.add(xmlPresentationLabel, gbc);

        JRadioButton linearRadioButton = new JRadioButton("Linéaire");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx = 2;
        gbc.gridy = 4;
        exportParametersPanel.add(linearRadioButton, gbc);

        indentedRadioButton = new JRadioButton("Indentée");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = 4;
        exportParametersPanel.add(indentedRadioButton, gbc);

        ButtonGroup indentedButtonGroup = new ButtonGroup();
        indentedButtonGroup.add(linearRadioButton);
        indentedButtonGroup.add(indentedRadioButton);
        indentedButtonGroup.clearSelection();
        if (gmc.isIndented())
            indentedRadioButton.setSelected(true);
        else
            linearRadioButton.setSelected(true);

        JLabel xmlReindexLabel = new JLabel("Renumérotation des éléments XML avant export:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx = 0;
        gbc.gridy = 5;
        exportParametersPanel.add(xmlReindexLabel, gbc);

        reindexYesRadioButton = new JRadioButton("Oui");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx = 1;
        gbc.gridy = 5;
        exportParametersPanel.add(reindexYesRadioButton, gbc);

        JRadioButton reindexNoRadioButton = new JRadioButton("Non");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 2;
        gbc.gridy = 5;
        exportParametersPanel.add(reindexNoRadioButton, gbc);

        ButtonGroup reindexButtonGroup = new ButtonGroup();
        reindexButtonGroup.add(reindexYesRadioButton);
        reindexButtonGroup.add(reindexNoRadioButton);
        reindexButtonGroup.clearSelection();
        if (gmc.isReindex())
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
        gbc.gridy = 6;
        exportParametersPanel.add(inCSVLabel, gbc);

        JLabel exportModeChoiceLabel = new JLabel("Mode de choix des objets exportés:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 7;
        exportParametersPanel.add(exportModeChoiceLabel, gbc);

        firstUsageButton = new JRadioButton("Premier");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 7;
        exportParametersPanel.add(firstUsageButton, gbc);

        lastUsageButton = new JRadioButton("Dernier");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 7;
        exportParametersPanel.add(lastUsageButton, gbc);

        allUsageButton = new JRadioButton("Tous");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 3;
        gbc.gridy = 7;
        exportParametersPanel.add(allUsageButton, gbc);

        ButtonGroup exportModeChoiceButtonGroup = new ButtonGroup();
        exportModeChoiceButtonGroup.add(firstUsageButton);
        exportModeChoiceButtonGroup.add(lastUsageButton);
        exportModeChoiceButtonGroup.add(allUsageButton);
        exportModeChoiceButtonGroup.clearSelection();
        switch (gmc.getUsageVersionSelectionMode()) {
            case FIRST_DATAOBJECT:
                firstUsageButton.setSelected(true);
                break;
            case ALL_DATAOBJECTS:
                allUsageButton.setSelected(true);
                break;
            case LAST_DATAOBJECT:
            default:
                lastUsageButton.setSelected(true);
                break;
        }

        JLabel lblNameMaxSize = new JLabel("Taille max des noms de répertoires :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 8;
        exportParametersPanel.add(lblNameMaxSize, gbc);

        nameMaxSizeTextField = new JTextField();
        DocumentFilter filter = new NumericFilter();
        ((AbstractDocument) nameMaxSizeTextField.getDocument()).setDocumentFilter(filter);
        nameMaxSizeTextField.setText(Integer.toString(gmc.getMaxNameSize()));
        nameMaxSizeTextField.setFont(MainWindow.DETAILS_FONT);
        nameMaxSizeTextField.setColumns(10);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        exportParametersPanel.add(nameMaxSizeTextField, gbc);

        JLabel lblCsvExtendedFormat = new JLabel("Format csv :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 9;
        exportParametersPanel.add(lblCsvExtendedFormat, gbc);

        csvExtendedFormatChexBox = new JCheckBox("étendu");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 9;
        exportParametersPanel.add(csvExtendedFormatChexBox, gbc);
        csvExtendedFormatChexBox.setSelected(gmc.isCsvExtendedFormat());

        JLabel metadataFilterLabel = new JLabel("Filtrage des métadonnées");
        metadataFilterLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 10;
        exportParametersPanel.add(metadataFilterLabel, gbc);

        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 11;
        exportParametersPanel.add(scrollPane, gbc);

        metadataFilterTextArea = new JTextArea();
        metadataFilterTextArea.setFont(MainWindow.DETAILS_FONT);
        scrollPane.setViewportView(metadataFilterTextArea);
        if (gmc.getKeptMetadataList() != null)
            metadataFilterTextArea.setText(String.join("\n", String.join("\n", gmc.getKeptMetadataList())));
        metadataFilterTextArea.setCaretPosition(0);

        metadataFilterCheckBox = new JCheckBox("Seules métadonnées exportées :");
        metadataFilterCheckBox.setToolTipText("Liste des noms de métadonnées dans <Content>, utilisée comm filtre si coché ");
        metadataFilterCheckBox.setSelected(gmc.isMetadataFilterFlag());
        metadataFilterTextArea.setEnabled(gmc.isMetadataFilterFlag());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        exportParametersPanel.add(metadataFilterCheckBox, gbc);
        metadataFilterCheckBox.addItemListener(this::metadataFilterEvent);

        // ImportParameters Panel
        JPanel importParametersPanel = new JPanel();
        tabbedPane.addTab("Import", new ImageIcon(getClass().getResource("/icon/document-open.png")), importParametersPanel, null);
        GridBagLayout gblImportParametersPanel = new GridBagLayout();
        gblImportParametersPanel.columnWeights = new double[]{0.1, 0.1, 0.5, 0.1};
        importParametersPanel.setLayout(gblImportParametersPanel);

        JLabel mailImportLabel = new JLabel("Import des messageries");
        mailImportLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        importParametersPanel.add(mailImportLabel, gbc);

        JLabel mailCharsetLabel = new JLabel("Encodage par défaut des messageries :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        importParametersPanel.add(mailCharsetLabel, gbc);

        defaultMailCharsetCombobox = new JComboBox<>(charsetStrings);
        defaultMailCharsetCombobox.setFont(MainWindow.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 1;
        importParametersPanel.add(defaultMailCharsetCombobox, gbc);
        defaultMailCharsetCombobox.setSelectedItem(mic.getDefaultCharsetName());

        JLabel mailFileTextExtractAULabel = new JLabel("Extraction des fichiers textes des courriels :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 2;
        importParametersPanel.add(mailFileTextExtractAULabel, gbc);

        messageFileCheckBox = new JCheckBox("des messages");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 2;
        importParametersPanel.add(messageFileCheckBox, gbc);
        messageFileCheckBox.setSelected(mic.isExtractMessageTextFile());

        attachementFileCheckBox = new JCheckBox("des pièces jointes");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 2;
        importParametersPanel.add(attachementFileCheckBox, gbc);
        attachementFileCheckBox.setSelected(mic.isExtractAttachmentTextFile());

        JLabel mailMetadataTextExtractAULabel = new JLabel("Extraction des métadonnées textuelles des courriels :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 3;
        importParametersPanel.add(mailMetadataTextExtractAULabel, gbc);

        messageMetadataCheckBox = new JCheckBox("des messages");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 3;
        importParametersPanel.add(messageMetadataCheckBox, gbc);
        messageMetadataCheckBox.setSelected(mic.isExtractMessageTextMetadata());

        attachementMetadataCheckBox = new JCheckBox("des pièces jointes");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 3;
        importParametersPanel.add(attachementMetadataCheckBox, gbc);
        attachementMetadataCheckBox.setSelected(mic.isExtractAttachmentTextMetadata());

        JLabel diskImportLabel = new JLabel("Import des hiérarchies sur disque");
        diskImportLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 4;
        importParametersPanel.add(diskImportLabel, gbc);

        JScrollPane scrollPane5 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 5;
        importParametersPanel.add(scrollPane5, gbc);

        ignorePatternsTextArea = new JTextArea();
        ignorePatternsTextArea.setFont(MainWindow.DETAILS_FONT);
        scrollPane5.setViewportView(ignorePatternsTextArea);
        if (dic.getIgnorePatternList() != null)
            ignorePatternsTextArea.setText(String.join("\n", String.join("\n", dic.getIgnorePatternList())));

        JLabel ignorePatternsLabel = new JLabel("Fichiers exclus des imports :");
        ignorePatternsLabel.setToolTipText("Liste d'expressions régulières définissant les noms de fichiers à ne pas prendre en compte");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        importParametersPanel.add(ignorePatternsLabel, gbc);

        ignoreLinksChexBox = new JCheckBox("ignorer les liens symboliques et rassourcis");
        ignoreLinksChexBox.setSelected(dic.isNoLinkFlag());
        gbc = new GridBagConstraints();
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 6;
        importParametersPanel.add(ignoreLinksChexBox, gbc);

        JLabel csvImportLabel = new JLabel("Import/export des csv");
        csvImportLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 7;
        importParametersPanel.add(csvImportLabel, gbc);

        JLabel csvCharsetLabel = new JLabel("Encodage des csv :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 8;
        importParametersPanel.add(csvCharsetLabel, gbc);

        csvCharsetCombobox = new JComboBox<>(charsetStrings);
        csvCharsetCombobox.setFont(MainWindow.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 8;
        importParametersPanel.add(csvCharsetCombobox, gbc);
        csvCharsetCombobox.setSelectedItem(cic.getCsvCharsetName());

        JLabel lblCsvDelimiter = new JLabel("Séparateur :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 9;
        importParametersPanel.add(lblCsvDelimiter, gbc);

        csvDelimiterTextField = new JTextField();
        csvDelimiterTextField.setText(Character.toString(cic.getDelimiter()));
        csvDelimiterTextField.setFont(MainWindow.DETAILS_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridwidth = 2;
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.WEST;
        importParametersPanel.add(csvDelimiterTextField, gbc);
        csvDelimiterTextField.setColumns(1);

        if (ResipGraphicApp.getTheApp().interfaceParameters.isExperimentalFlag()) {
            // CompactParameters Panel
            JPanel compactParametersPanel = new JPanel();
            tabbedPane.addTab("Compact", new ImageIcon(getClass().getResource("/icon/package-x-generic.png")), compactParametersPanel, null);
            GridBagLayout gblCompactParametersPanel = new GridBagLayout();
            gblCompactParametersPanel.columnWeights = new double[]{0.20, 0.15, 0.25, 0.15, 0.25};
            gblCompactParametersPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
            gblCompactParametersPanel.rowWeights = new double[]{0, 0, 0, 0.4, 0, 0, 0, 1};
            compactParametersPanel.setLayout(gblCompactParametersPanel);

            JLabel compactLabel = new JLabel("Limites des paquets de documents");
            compactLabel.setFont(MainWindow.BOLD_LABEL_FONT);
            gbc = new GridBagConstraints();
            gbc.gridwidth = 3;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            compactParametersPanel.add(compactLabel, gbc);

            compactLabel = new JLabel("Taille max des métadonnées :");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 1;
            compactParametersPanel.add(compactLabel, gbc);

            maxMetadataSizeTextField = new JTextField();
            ((AbstractDocument) maxMetadataSizeTextField.getDocument()).setDocumentFilter(filter);
            maxMetadataSizeTextField.setText(Integer.toString(coc.getMaxMetadataSize()));
            maxMetadataSizeTextField.setFont(MainWindow.DETAILS_FONT);
            maxMetadataSizeTextField.setColumns(10);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            compactParametersPanel.add(maxMetadataSizeTextField, gbc);

            compactLabel = new JLabel("Nombre max de documents :");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 2;
            gbc.gridy = 1;
            compactParametersPanel.add(compactLabel, gbc);

            maxDocumentNumberTextField = new JTextField();
            ((AbstractDocument) maxDocumentNumberTextField.getDocument()).setDocumentFilter(filter);
            maxDocumentNumberTextField.setText(Integer.toString(coc.getMaxDocumentNumber()));
            maxDocumentNumberTextField.setFont(MainWindow.DETAILS_FONT);
            maxDocumentNumberTextField.setColumns(10);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            compactParametersPanel.add(maxDocumentNumberTextField, gbc);

            compactLabel = new JLabel("Filtrage des version d'objet");
            compactLabel.setFont(MainWindow.BOLD_LABEL_FONT);
            gbc = new GridBagConstraints();
            gbc.gridwidth = 3;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 2;
            compactParametersPanel.add(compactLabel, gbc);

            compactLabel = new JLabel("Documents:");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 3;
            compactParametersPanel.add(compactLabel, gbc);

            scrollPane = new JScrollPane();
            gbc = new GridBagConstraints();
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 1;
            gbc.gridy = 3;
            compactParametersPanel.add(scrollPane, gbc);

            compactDocumentDataObjectVersionFilterTextArea = new JTextArea();
            compactDocumentDataObjectVersionFilterTextArea.setFont(MainWindow.DETAILS_FONT);
            scrollPane.setViewportView(compactDocumentDataObjectVersionFilterTextArea);
            if (coc.getDocumentKeptDataObjectVersionList() != null)
                compactDocumentDataObjectVersionFilterTextArea.setText(String.join("\n", String.join("\n", coc.getDocumentKeptDataObjectVersionList())));
            compactDocumentDataObjectVersionFilterTextArea.setCaretPosition(0);

            compactLabel = new JLabel("Sous-documents:");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 2;
            gbc.gridy = 3;
            compactParametersPanel.add(compactLabel, gbc);

            scrollPane = new JScrollPane();
            gbc = new GridBagConstraints();
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 3;
            gbc.gridy = 3;
            compactParametersPanel.add(scrollPane, gbc);

            compactSubDocumentDataObjectVersionFilterTextArea = new JTextArea();
            compactSubDocumentDataObjectVersionFilterTextArea.setFont(MainWindow.DETAILS_FONT);
            scrollPane.setViewportView(compactSubDocumentDataObjectVersionFilterTextArea);
            if (coc.getSubDocumentKeptDataObjectVersionList() != null)
                compactSubDocumentDataObjectVersionFilterTextArea.setText(String.join("\n", String.join("\n", coc.getSubDocumentKeptDataObjectVersionList())));
            compactSubDocumentDataObjectVersionFilterTextArea.setCaretPosition(0);

            compactLabel = new JLabel("Mode de construction des paquets de documents");
            compactLabel.setFont(MainWindow.BOLD_LABEL_FONT);
            gbc = new GridBagConstraints();
            gbc.gridwidth = 3;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 4;
            compactParametersPanel.add(compactLabel, gbc);

            zipRadioButton = new JRadioButton("Compressé(.zip)");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 5, 5);
            gbc.gridx = 1;
            gbc.gridy = 5;
            compactParametersPanel.add(zipRadioButton, gbc);

            JRadioButton tarRadioButton = new JRadioButton("Sans compression(.tar)");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 5, 5);
            gbc.gridx = 2;
            gbc.gridy = 5;
            compactParametersPanel.add(tarRadioButton, gbc);

            ButtonGroup arcMethodButtonGroup = new ButtonGroup();
            arcMethodButtonGroup.add(zipRadioButton);
            arcMethodButtonGroup.add(tarRadioButton);
            arcMethodButtonGroup.clearSelection();
            if (coc.isDeflatedFlag())
                zipRadioButton.setSelected(true);
            else
                tarRadioButton.setSelected(true);

            compactLabel = new JLabel("Filtrage des métadonnées");
            compactLabel.setFont(MainWindow.BOLD_LABEL_FONT);
            gbc = new GridBagConstraints();
            gbc.gridwidth = 3;
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 6;
            compactParametersPanel.add(compactLabel, gbc);

            scrollPane = new JScrollPane();
            gbc = new GridBagConstraints();
            gbc.weightx = 1.0;
            gbc.gridwidth = 3;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 1;
            gbc.gridy = 7;
            compactParametersPanel.add(scrollPane, gbc);

            compactMetadataFilterTextArea = new JTextArea();
            compactMetadataFilterTextArea.setFont(MainWindow.DETAILS_FONT);
            scrollPane.setViewportView(compactMetadataFilterTextArea);
            if (coc.getKeptMetadataList() != null)
                compactMetadataFilterTextArea.setText(String.join("\n", String.join("\n", coc.getKeptMetadataList())));
            compactMetadataFilterTextArea.setCaretPosition(0);

            compactMetadataFilterCheckBox = new JCheckBox("Seules métadonnées compactées :");
            compactMetadataFilterCheckBox.setToolTipText("Liste des noms de métadonnées dans <Content>, utilisée comm filtre si coché ");
            compactMetadataFilterCheckBox.setSelected(coc.isMetadataFilterFlag());
            compactMetadataFilterTextArea.setEnabled(coc.isMetadataFilterFlag());
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(0, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.weighty = 1.0;
            compactParametersPanel.add(compactMetadataFilterCheckBox, gbc);
            compactMetadataFilterCheckBox.addItemListener(this::compactMetadataFilterEvent);
        }

        // TreatmentParameters Panel
        JPanel treatmentParametersPanel = new JPanel();
        tabbedPane.addTab("Traitement/Interface", new ImageIcon(getClass().getResource("/icon/edit-find-replace.png")),
                treatmentParametersPanel, null);
        GridBagLayout gblTreatmentParametersPanel = new GridBagLayout();
        gblTreatmentParametersPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        gblTreatmentParametersPanel.rowWeights = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 1.0};
        treatmentParametersPanel.setLayout(gblTreatmentParametersPanel);

        JLabel workDirLabel = new JLabel("Répertoire de travail");
        workDirLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        treatmentParametersPanel.add(workDirLabel, gbc);

        JLabel dirLabel = new JLabel("Dossier:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        treatmentParametersPanel.add(dirLabel, gbc);

        workDirTextField = new JTextField();
        workDirTextField.setText(cc.getWorkDir());
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 1;
        treatmentParametersPanel.add(workDirTextField, gbc);
        workDirTextField.setColumns(10);

        JButton workDirButton = new JButton("Choisir...");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.gridx = 3;
        gbc.gridy = 1;
        treatmentParametersPanel.add(workDirButton, gbc);
        workDirButton.addActionListener(arg0 -> buttonChooseWorkDir());

        JLabel dupTreatmentLabel = new JLabel("Traitement des doublons");
        dupTreatmentLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 2;
        treatmentParametersPanel.add(dupTreatmentLabel, gbc);

        JLabel lblDupMax = new JLabel("Limite d'aggrégation des doublons :");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 3;
        treatmentParametersPanel.add(lblDupMax, gbc);

        dupMaxTextField = new JTextField();
        DocumentFilter dupMaxFilter = new NumericFilter();
        ((AbstractDocument) dupMaxTextField.getDocument()).setDocumentFilter(dupMaxFilter);
        dupMaxTextField.setText(Integer.toString(tp.getDupMax()));
        dupMaxTextField.setFont(MainWindow.DETAILS_FONT);
        dupMaxTextField.setColumns(10);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridwidth = 2;
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        treatmentParametersPanel.add(dupMaxTextField, gbc);

        JLabel sedaVersionLabel = new JLabel("Version du Standard d'Echange utilisé (SEDA)");
        sedaVersionLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 4;
        treatmentParametersPanel.add(sedaVersionLabel, gbc);

        seda2Version1RadioButton = new JRadioButton("SEDA 2.1");
        seda2Version2RadioButton = new JRadioButton("SEDA 2.2");
        seda2Version3RadioButton = new JRadioButton("SEDA 2.3");
        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0)); // Alignement serré à gauche.
        buttonGroupPanel.add(seda2Version1RadioButton);
        buttonGroupPanel.add(seda2Version2RadioButton);
        buttonGroupPanel.add(seda2Version3RadioButton);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1; // Position du groupe.
        gbc.gridy = 5;
        treatmentParametersPanel.add(buttonGroupPanel, gbc);

        ButtonGroup seda2VersionButtonGroup = new ButtonGroup();
        seda2VersionButtonGroup.add(seda2Version1RadioButton);
        seda2VersionButtonGroup.add(seda2Version2RadioButton);
        seda2VersionButtonGroup.add(seda2Version3RadioButton);
        seda2VersionButtonGroup.clearSelection();
        if (tp.getSeda2Version() == 1)
            seda2Version1RadioButton.setSelected(true);
        else if (tp.getSeda2Version() == 2)
            seda2Version2RadioButton.setSelected(true);
        else
          seda2Version3RadioButton.setSelected(true);


        JLabel interfaceLabel = new JLabel("Interface");
        interfaceLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 6;
        treatmentParametersPanel.add(interfaceLabel, gbc);

        JLabel interfaceTypeLabel = new JLabel("Interface par défaut:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 7;
        treatmentParametersPanel.add(interfaceTypeLabel, gbc);

        structuredInterfaceRadioButton = new JRadioButton("Structurée");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 7;
        treatmentParametersPanel.add(structuredInterfaceRadioButton, gbc);

        JRadioButton classicInterfaceRadioButton = new JRadioButton("XML-expert");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 7;
        treatmentParametersPanel.add(classicInterfaceRadioButton, gbc);

        ButtonGroup interfaceTypeButtonGroup = new ButtonGroup();
        interfaceTypeButtonGroup.add(classicInterfaceRadioButton);
        interfaceTypeButtonGroup.add(structuredInterfaceRadioButton);
        interfaceTypeButtonGroup.clearSelection();
        if (ip.isStructuredMetadataEditionFlag())
            structuredInterfaceRadioButton.setSelected(true);
        else
            classicInterfaceRadioButton.setSelected(true);

        JLabel debugModeLabel = new JLabel("Modes spécifiques:");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 8;
        treatmentParametersPanel.add(debugModeLabel, gbc);

        debugModeCheckBox = new JCheckBox("debug");
        debugModeCheckBox.setSelected(ip.isDebugFlag());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 8;
        treatmentParametersPanel.add(debugModeCheckBox, gbc);

        experimentalModeCheckBox = new JCheckBox("experimental");
        experimentalModeCheckBox.setSelected(ip.isExperimentalFlag());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 8;
        treatmentParametersPanel.add(experimentalModeCheckBox, gbc);

        // Buttons
        JButton cancelButton = new JButton("Annuler");
        cancelButton.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        contentPane.add(cancelButton, gbc);
        cancelButton.addActionListener(arg -> buttonCancel());

        JButton okButton = new JButton("OK");
        okButton.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
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

    private void compactMetadataFilterEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            compactMetadataFilterTextArea.setEnabled(true);
        } else if (event.getStateChange() == DESELECTED) {
            compactMetadataFilterTextArea.setEnabled(false);
        }
    }

    private void buttonCancel() {
        returnValue = ResipGraphicApp.KO_DIALOG;
        setVisible(false);
    }

    private void buttonOk() {
        if (!extractFromDialog())
            return;
        returnValue = OK_DIALOG;
        setVisible(false);
    }

    private String chooseDirectory() {
        JFileChooser fileChooser = new JFileChooser(cc.getWorkDir());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            try {
                return fileChooser.getSelectedFile().getCanonicalPath();
            } catch (IOException e) {
                return null;
            }
        return null;
    }

    private void buttonChooseWorkDir() {
        try {
            String dir = chooseDirectory();
            if (dir == null)
                return;

            workDirTextField.setText(dir);
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(this.owner,
                    "Erreur fatale, impossible de sélectionner sur le disque \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,
                    "Resip.GraphicApp: Erreur fatale, impossible de sélectionner sur le disque", e);
        }
    }

    private boolean tryToConvertCurrentWorkSedaVersion(int toSeda2Version) {
        DataObjectPackage dop = ResipGraphicApp.getTheApp().currentWork.getDataObjectPackage();
        InOutDialog inOutDialog = new InOutDialog(this.owner, "Conversion vers le schéma SEDA 2." + toSeda2Version);
        ChangeSeda2VersionThread changeSeda2VersionThread = new ChangeSeda2VersionThread(toSeda2Version, dop, inOutDialog);
        try {
            changeSeda2VersionThread.execute();
            inOutDialog.setVisible(true);
            while (!changeSeda2VersionThread.isDone()) Thread.sleep(100);
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(this.owner,
                    "Impossible de faire la conversion en SEDA 2." +
                            toSeda2Version + "\n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire la " +
                    "conversion en SEDA 2." + toSeda2Version, changeSeda2VersionThread.getError());
            return false;
        }
        if (changeSeda2VersionThread.getResult() == null) {
            UserInteractionDialog.getUserAnswer(this.owner,
                    "Impossible de faire la conversion en SEDA 2." +
                            toSeda2Version + "\n" +
                            "Fermez cet objet avant de changer la version du SEDA utilisé\n->"
                            + changeSeda2VersionThread.getError(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur, impossible de faire la " +
                    "conversion en SEDA 2." + toSeda2Version, changeSeda2VersionThread.getError());
            return false;
        }
        ResipGraphicApp.getTheApp().currentWork.setDataObjectPackage(changeSeda2VersionThread.getResult());
        ResipGraphicApp.getTheWindow().load();
        return true;
    }

    private int getPositiveInt(String numberString){
        int result = Integer.parseInt(numberString);
        if (result <= 0)
            throw new NumberFormatException("Number not strictly positive");
        return result;
    }

    private boolean extractFromDialog() {
        int tmp;

        cc.setWorkDir(workDirTextField.getText());

        dic.setIgnorePatternList(Arrays.asList(ignorePatternsTextArea.getText().split("\\s*\n\\s*")));
        dic.setNoLinkFlag(ignoreLinksChexBox.isSelected());

        mic.setExtractMessageTextMetadata(messageMetadataCheckBox.isSelected());
        mic.setExtractAttachmentTextMetadata(attachementMetadataCheckBox.isSelected());
        mic.setExtractMessageTextFile(messageFileCheckBox.isSelected());
        mic.setExtractAttachmentTextFile(attachementFileCheckBox.isSelected());
        mic.setDefaultCharsetName((String) defaultMailCharsetCombobox.getSelectedItem());

        gmc.setHierarchicalArchiveUnits(hierarchicalRadioButton.isSelected());
        gmc.setIndented(indentedRadioButton.isSelected());
        gmc.setReindex(reindexYesRadioButton.isSelected());
        if (firstUsageButton.isSelected())
            gmc.setUsageVersionSelectionMode(FIRST_DATAOBJECT);
        else if (allUsageButton.isSelected())
            gmc.setUsageVersionSelectionMode(ALL_DATAOBJECTS);
        else if (lastUsageButton.isSelected())
            gmc.setUsageVersionSelectionMode(LAST_DATAOBJECT);
        try {
            tmp = getPositiveInt(nameMaxSizeTextField.getText());
        } catch (NumberFormatException e) {
            tabbedPane.setSelectedIndex(3);
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                    "La taille limite des noms de répertoires exportées doit être un nombre strictement supérieur à 0.",
                    "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                    null);
            return false;
        }
        gmc.setMaxNameSize(tmp);
        gmc.setCsvExtendedFormat(csvExtendedFormatChexBox.isSelected());
        gmc.setManagementMetadataXmlData(managementMetadataTextArea.getText());
        gmc.setMetadataFilterFlag(metadataFilterCheckBox.isSelected());
        gmc.setKeptMetadataList(Arrays.asList(metadataFilterTextArea.getText().split("\\s*\n\\s*"))
                .stream().map(String::trim).collect(Collectors.toList()));

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

        cic.setDelimiter((csvDelimiterTextField.getText().isEmpty() ? ';' : csvDelimiterTextField.getText().charAt(0)));
        cic.setCsvCharsetName((String) csvCharsetCombobox.getSelectedItem());

        if (ResipGraphicApp.getTheApp().interfaceParameters.isExperimentalFlag()) {
            try {
                tmp = Integer.parseInt(maxMetadataSizeTextField.getText());
                if (tmp < 0)
                    throw new NumberFormatException("Number not strictly positive");
            } catch (NumberFormatException e) {
                tabbedPane.setSelectedIndex(3);
                UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                        "La taille limite des métadonnées exportées doit être un nombre supérieur à 0. A noter, 0 veut dire sans limite.",
                        "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                        null);
                return false;
            }
            coc.setMaxMetadataSize(tmp);
            try {
                tmp = Integer.parseInt(maxDocumentNumberTextField.getText());
                if (tmp < 0)
                    throw new NumberFormatException("Number not strictly positive");
            } catch (NumberFormatException e) {
                tabbedPane.setSelectedIndex(3);
                UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                        "Le nombre limite de documents dans un paquet doit être un nombre supérieur à 0. A noter, 0 veut dire sans limite.",
                        "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                        null);
                return false;
            }
            coc.setMaxDocumentNumber(tmp);
            coc.setMetadataFilterFlag(compactMetadataFilterCheckBox.isSelected());
            coc.setDocumentKeptDataObjectVersionList(Arrays.asList(compactDocumentDataObjectVersionFilterTextArea.getText().split("\\s*\n\\s*"))
                    .stream().map(String::trim).collect(Collectors.toList()));
            coc.setSubDocumentKeptDataObjectVersionList(Arrays.asList(compactSubDocumentDataObjectVersionFilterTextArea.getText().split("\\s*\n\\s*"))
                    .stream().map(String::trim).collect(Collectors.toList()));
            coc.setKeptMetadataList(Arrays.asList(compactMetadataFilterTextArea.getText().split("\\s*\n\\s*"))
                    .stream().map(String::trim).collect(Collectors.toList()));
            coc.setDeflatedFlag(zipRadioButton.isSelected());
        }

        try {
            tmp = getPositiveInt(dupMaxTextField.getText());
        } catch (NumberFormatException e) {
            tabbedPane.setSelectedIndex(4);
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                    "La valeur limite d'aggrégation des doublons doit être un nombre strictement supérieur à 0.",
                    "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                    null);
            return false;
        }
        tp.setDupMax(tmp);

        int toSeda2Version = (seda2Version1RadioButton.isSelected() ? 1 : 2);
        if ((tp.getSeda2Version() != toSeda2Version) && (ResipGraphicApp.getTheApp().currentWork != null)) {
            if (UserInteractionDialog.getUserAnswer(this.owner,
                    "Attention, un SIP est ouvert et vous changez de version de SEDA2.x\n" +
                            "Voulez-vous essayer de le convertir?",
                    "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                    null) != OK_DIALOG)
                return false;
            if (!tryToConvertCurrentWorkSedaVersion(toSeda2Version))
                return false;
        }
        if (seda2Version1RadioButton.isSelected())
            tp.setSeda2Version(1);
        else if (seda2Version2RadioButton.isSelected())
            tp.setSeda2Version(2);
        else
            tp.setSeda2Version(3);
        try {
            SEDA2Version.setSeda2Version(tp.getSeda2Version());
        } catch (SEDALibException ignored) {
            // no real case
        }

        ip.setStructuredMetadataEditionFlag(structuredInterfaceRadioButton.isSelected());
        ip.setDebugFlag(debugModeCheckBox.isSelected());

        ip.setExperimentalFlag(experimentalModeCheckBox.isSelected());
        return true;
    }

    /**
     * Get return value int.
     *
     * @return the return value
     */
    public int getReturnValue() {
        return returnValue;
    }
}
