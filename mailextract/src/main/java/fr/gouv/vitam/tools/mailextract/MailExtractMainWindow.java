/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.mailextract;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * MailExtractMainWindow class for the main window with all parameters field and
 * console text area.
 */
public class MailExtractMainWindow extends JFrame {

    private static final long serialVersionUID = 4607177374736676766L;

    private MailExtractGraphicApp app;

    /**
     * The folder field.
     */
    JTextField folderField;

    /**
     * The savedir field.
     */
    JTextField savedirField;

    /**
     * The local radio button.
     */
    JRadioButton localRadioButton;

    /**
     * The thunderbird radio button.
     */
    JRadioButton thunderbirdRadioButton;

    /**
     * The outlook radio button.
     */
    JRadioButton pstRadioButton;

    /**
     * The mbox radio button.
     */
    JRadioButton mboxRadioButton;

    /**
     * The eml radio button.
     */
    JRadioButton emlRadioButton;

    /**
     * The eml radio button.
     */
    JRadioButton msgRadioButton;

    /**
     * The name label.
     */
    JLabel nameLabel;

    /**
     * The name field.
     */
    JTextField nameField;

    /**
     * The container label.
     */
    JLabel containerLabel;

    /**
     * The container field.
     */
    JTextField containerField;

    /**
     * The container button.
     */
    JButton containerButton;

    /**
     * The protocole radio button.
     */
    JRadioButton protocoleRadioButton;

    /**
     * The imap radio button.
     */
    JRadioButton imapRadioButton;

    /**
     * The imaps radio button.
     */
    JRadioButton imapsRadioButton;

    /**
     * The server label.
     */
    JLabel serverLabel;

    /**
     * The server field.
     */
    JTextField serverField;

    /**
     * The user label.
     */
    JLabel userLabel;

    /**
     * The user field.
     */
    JTextField userField;

    /**
     * The password label.
     */
    JLabel passwordLabel;

    /**
     * The password field.
     */
    JTextField passwordField;

    /**
     * The loglevel combo box.
     */
    @SuppressWarnings("rawtypes")
    JComboBox loglevelComboBox;

    /**
     * The warning check box.
     */
    JCheckBox warningCheckBox;

    /**
     * The keeponlydeep check box.
     */
    JCheckBox keeponlydeepCheckBox;

    /**
     * The dropemptyfolders check box.
     */
    JCheckBox dropemptyfoldersCheckBox;

    /**
     * The names length field.
     */
    JTextField namesLengthField;

    /**
     * The extractlist check box.
     */
    JCheckBox extractlistCheckBox;

    /**
     * The loglevel combo box.
     */
    JComboBox charsetComboBox;

    /**
     * The text extraction flags.
     */
    JCheckBox extractmessagetextfileCheckBox;

    /**
     * The extractmessagetextmetadata check box.
     */
    JCheckBox extractmessagetextmetadataCheckBox;

    /**
     * The extractfiletextfile check box.
     */
    JCheckBox extractfiletextfileCheckBox;

    /**
     * The extractfiletextmetadata check box.
     */
    JCheckBox extractfiletextmetadataCheckBox;

    /**
     * The console text area.
     */
    JTextArea consoleTextArea;

    /**
     * The warning check box.
     */
    JCheckBox debugCheckBox;


    /**
     * The english check box.
     */
    JCheckBox englishCheckBox;

    /**
     *  Other labels
     */
    JLabel lblNewLabel, loglevelLabel, namesLengthLabel, savedirLabel, folderLabel, charsetLabel;

    /**
     *  Other buttons
     */
    JButton extractButton, listButton, statButton, emptyButton, savedirButton;

    /**
     * The proposed log level.
     */
    String[] loglevelGraphicStrings = {"OFF", "INFO GLOBALE", "AVERTISSEMENT", "DOSSIERS", "LOT DE MESSAGES",
            "MESSAGES", "DETAIL MESSAGES"};

    /**
     * The proposed charsets.
     */
    String[] charsetGraphicStrings = {"windows-1252", "ISO-8859-1", "UTF-8", "CESU-8", "IBM00858", "IBM437", "IBM775",
            "IBM850", "IBM852", "IBM855", "IBM857", "IBM862", "IBM866", "ISO-8859-2", "ISO-8859-4", "ISO-8859-5",
            "ISO-8859-7", "ISO-8859-9", "ISO-8859-13", "ISO-8859-15", "KOI8-R", "KOI8-U", "US-ASCII", "UTF-16",
            "UTF-16BE", "UTF-16LE", "UTF-32", "UTF-32BE", "UTF-32LE", "x-UTF-32BE-BOM", "x-UTF-32LE-BOM",
            "windows-1250", "windows-1251", "windows-1253", "windows-1254", "windows-1257"};

    private JPanel panel;

    private final ResourceBundle MESSAGES, MESSAGES_EN;

    /**
     * Gets the global graphic app.
     *
     * @return the app
     */
    public MailExtractGraphicApp getApp() {
        return app;
    }

    /**
     * Create the main window and initialize all the frames.
     *
     * @param app the app
     */
    public MailExtractMainWindow(MailExtractGraphicApp app) {
        super();
        this.app = app;
        MESSAGES = ResourceBundle.getBundle("Labels",Locale.FRENCH);;
        MESSAGES_EN = ResourceBundle.getBundle("Labels", Locale.ENGLISH);;
        initialize();
    }

    // Initialize the contents of the frame.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initialize() {
        URL imageURL = getClass().getClassLoader().getResource("VitamIcon96.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            setIconImage(icon.getImage());
        }

        this.setTitle(MESSAGES.getString("mainWindow.title"));

        getContentPane().setPreferredSize(new Dimension(800, 800));
        setBounds(0, 0, 800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[]{1.0, 1, 1, 1};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        getContentPane().setLayout(gridBagLayout);

        // Panel d'extraction du texte
        JPanel extractTextPanel = new JPanel();
        extractTextPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        GridBagConstraints gbc_extractTextPanel = new GridBagConstraints();
        gbc_extractTextPanel.gridwidth = 4;
        gbc_extractTextPanel.weightx = 1.0;
        gbc_extractTextPanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_extractTextPanel.insets = new Insets(0, 10, 10, 10);
        gbc_extractTextPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_extractTextPanel.gridx = 0;
        gbc_extractTextPanel.gridy = 9;
        getContentPane().add(extractTextPanel, gbc_extractTextPanel);
        GridBagLayout gbl_extractTextPanel = new GridBagLayout();
        gbl_extractTextPanel.rowWeights = new double[]{1.0, 1.0};
        gbl_extractTextPanel.columnWeights = new double[]{1.0, 1, 1};
        extractTextPanel.setLayout(gbl_extractTextPanel);

        lblNewLabel = new JLabel(MESSAGES.getString("label.extractText"));
        GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
        gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel.gridx = 0;
        gbc_lblNewLabel.gridy = 0;
        extractTextPanel.add(lblNewLabel, gbc_lblNewLabel);

        extractmessagetextfileCheckBox = new JCheckBox(MESSAGES.getString("checkbox.extractMessageToFile"));
        GridBagConstraints gbc_chckbxMessageTextFileBox = new GridBagConstraints();
        gbc_chckbxMessageTextFileBox.anchor = GridBagConstraints.WEST;
        gbc_chckbxMessageTextFileBox.gridx = 1;
        gbc_chckbxMessageTextFileBox.gridy = 0;
        extractTextPanel.add(extractmessagetextfileCheckBox, gbc_chckbxMessageTextFileBox);

        extractmessagetextmetadataCheckBox = new JCheckBox(MESSAGES.getString("checkbox.extractMessageToMetadata"));
        GridBagConstraints gbc_chckbxMessageTextMetadata = new GridBagConstraints();
        gbc_chckbxMessageTextMetadata.anchor = GridBagConstraints.WEST;
        gbc_chckbxMessageTextMetadata.gridx = 1;
        gbc_chckbxMessageTextMetadata.gridy = 1;
        extractTextPanel.add(extractmessagetextmetadataCheckBox, gbc_chckbxMessageTextMetadata);

        extractfiletextfileCheckBox = new JCheckBox(MESSAGES.getString("checkbox.extractAttachmentToFile"));
        GridBagConstraints gbc_chckbxFileTextFileBox = new GridBagConstraints();
        gbc_chckbxFileTextFileBox.anchor = GridBagConstraints.WEST;
        gbc_chckbxFileTextFileBox.gridx = 2;
        gbc_chckbxFileTextFileBox.gridy = 0;
        extractTextPanel.add(extractfiletextfileCheckBox, gbc_chckbxFileTextFileBox);

        extractfiletextmetadataCheckBox = new JCheckBox(MESSAGES.getString("checkbox.extractAttachmentToMetadata"));
        GridBagConstraints gbc_chckbxFileTextMetadata = new GridBagConstraints();
        gbc_chckbxFileTextMetadata.anchor = GridBagConstraints.WEST;
        gbc_chckbxFileTextMetadata.gridx = 2;
        gbc_chckbxFileTextMetadata.gridy = 1;
        extractTextPanel.add(extractfiletextmetadataCheckBox, gbc_chckbxFileTextMetadata);

        panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.anchor = GridBagConstraints.WEST;
        gbc_panel.insets = new Insets(0, 0, 5, 5);
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 0;
        gbc_panel.gridwidth = 4;
        gbc_panel.gridy = 11;
        getContentPane().add(panel, gbc_panel);

        consoleTextArea = new JTextArea();
        consoleTextArea.setFont(new Font("Courier 10 Pitch", Font.BOLD, 12));
        consoleTextArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 4;
        gbc_scrollPane.weightx = 1.0;
        gbc_scrollPane.weighty = 1.0;
        gbc_scrollPane.insets = new Insets(5, 5, 0, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 13;
        getContentPane().add(scrollPane, gbc_scrollPane);

        extractButton = new JButton(MESSAGES.getString("button.extract"));
        GridBagConstraints gbc_extractButton = new GridBagConstraints();
        gbc_extractButton.gridwidth = 1;
        gbc_extractButton.insets = new Insets(0, 0, 10, 10);
        gbc_extractButton.gridx = 0;
        gbc_extractButton.gridy = 12;
        getContentPane().add(extractButton, gbc_extractButton);
        extractButton.setActionCommand("extract");
        extractButton.addActionListener(app);

        listButton = new JButton(MESSAGES.getString("button.listFolders"));
        GridBagConstraints gbc_listButton = new GridBagConstraints();
        gbc_listButton.anchor = GridBagConstraints.EAST;
        gbc_listButton.insets = new Insets(0, 0, 10, 10);
        gbc_listButton.gridx = 1;
        gbc_listButton.gridy = 12;
        getContentPane().add(listButton, gbc_listButton);
        listButton.setActionCommand("list");
        listButton.addActionListener(app);

        statButton = new JButton(MESSAGES.getString("button.listStats"));
        GridBagConstraints gbc_statButton = new GridBagConstraints();
        gbc_statButton.insets = new Insets(0, 0, 10, 10);
        gbc_statButton.gridx = 2;
        gbc_statButton.gridy = 12;
        getContentPane().add(statButton, gbc_statButton);
        statButton.setActionCommand("stat");
        statButton.addActionListener(app);

        emptyButton = new JButton(MESSAGES.getString("button.clearLog"));
        GridBagConstraints gbc_emptyButton = new GridBagConstraints();
        gbc_emptyButton.insets = new Insets(0, 0, 10, 10);
        gbc_emptyButton.gridx = 3;
        gbc_emptyButton.gridy = 12;
        getContentPane().add(emptyButton, gbc_emptyButton);
        emptyButton.setActionCommand("empty");
        emptyButton.addActionListener(app);

        warningCheckBox = new JCheckBox(MESSAGES.getString("checkbox.warning"));
        GridBagConstraints gbc_warningCheckBox = new GridBagConstraints();
        gbc_warningCheckBox.anchor = GridBagConstraints.WEST;
        gbc_warningCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_warningCheckBox.gridx = 2;
        gbc_warningCheckBox.gridy = 10;
        getContentPane().add(warningCheckBox, gbc_warningCheckBox);

        loglevelComboBox = new JComboBox(loglevelGraphicStrings);
        GridBagConstraints gbc_loglevelComboBox = new GridBagConstraints();
        gbc_loglevelComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_loglevelComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_loglevelComboBox.gridx = 1;
        gbc_loglevelComboBox.gridy = 10;
        getContentPane().add(loglevelComboBox, gbc_loglevelComboBox);

        loglevelLabel = new JLabel(MESSAGES.getString("label.logLevel"));
        GridBagConstraints gbc_loglevelLabel = new GridBagConstraints();
        gbc_loglevelLabel.anchor = GridBagConstraints.EAST;
        gbc_loglevelLabel.insets = new Insets(0, 0, 5, 5);
        gbc_loglevelLabel.gridx = 0;
        gbc_loglevelLabel.gridy = 10;
        getContentPane().add(loglevelLabel, gbc_loglevelLabel);

        keeponlydeepCheckBox = new JCheckBox(MESSAGES.getString("checkbox.keepOnlyDeep"));
        GridBagConstraints gbc_keeponlydeepRadioButton = new GridBagConstraints();
        gbc_keeponlydeepRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_keeponlydeepRadioButton.gridx = 1;
        gbc_keeponlydeepRadioButton.gridy = 6;
        getContentPane().add(keeponlydeepCheckBox, gbc_keeponlydeepRadioButton);

        namesLengthLabel = new JLabel(MESSAGES.getString("label.namesLength"));
        GridBagConstraints gbc_namesLengthLabel = new GridBagConstraints();
        gbc_namesLengthLabel.anchor = GridBagConstraints.EAST;
        gbc_namesLengthLabel.insets = new Insets(0, 0, 5, 5);
        gbc_namesLengthLabel.gridx = 2;
        gbc_namesLengthLabel.gridy = 6;
        getContentPane().add(namesLengthLabel, gbc_namesLengthLabel);

        namesLengthField = new JTextField();
        GridBagConstraints gbc_namesLengthField = new GridBagConstraints();
        gbc_namesLengthField.weightx = 0.5;
        gbc_namesLengthField.insets = new Insets(0, 0, 5, 10);
        gbc_namesLengthField.fill = GridBagConstraints.HORIZONTAL;
        gbc_namesLengthField.gridx = 3;
        gbc_namesLengthField.gridy = 6;
        getContentPane().add(namesLengthField, gbc_namesLengthField);
        namesLengthField.setColumns(128);

        extractlistCheckBox = new JCheckBox(MESSAGES.getString("checkbox.extractList"));
        GridBagConstraints gbc_extractlistCheckBox = new GridBagConstraints();
        gbc_extractlistCheckBox.anchor = GridBagConstraints.WEST;
        gbc_extractlistCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_extractlistCheckBox.gridx = 2;
        gbc_extractlistCheckBox.gridy = 8;
        getContentPane().add(extractlistCheckBox, gbc_extractlistCheckBox);

        savedirButton = new JButton(MESSAGES.getString("button.savedir"));
        GridBagConstraints gbc_savedirButton = new GridBagConstraints();
        gbc_savedirButton.insets = new Insets(0, 0, 5, 0);
        gbc_savedirButton.gridx = 3;
        gbc_savedirButton.gridy = 7;
        getContentPane().add(savedirButton, gbc_savedirButton);
        savedirButton.setActionCommand("savedir");
        savedirButton.addActionListener(app);

        savedirField = new JTextField();
        GridBagConstraints gbc_savedirField = new GridBagConstraints();
        gbc_savedirField.gridwidth = 2;
        gbc_savedirField.insets = new Insets(0, 0, 5, 5);
        gbc_savedirField.fill = GridBagConstraints.HORIZONTAL;
        gbc_savedirField.gridx = 1;
        gbc_savedirField.gridy = 7;
        gbc_savedirField.weightx = 0.5;
        getContentPane().add(savedirField, gbc_savedirField);

        dropemptyfoldersCheckBox = new JCheckBox(MESSAGES.getString("checkbox.dropEmptyFolders"));
        GridBagConstraints gbc_dropemptyfoldersCheckBox = new GridBagConstraints();
        gbc_dropemptyfoldersCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_dropemptyfoldersCheckBox.gridx = 0;
        gbc_dropemptyfoldersCheckBox.gridy = 6;
        getContentPane().add(dropemptyfoldersCheckBox, gbc_dropemptyfoldersCheckBox);

        savedirLabel = new JLabel(MESSAGES.getString("label.savedir"));
        GridBagConstraints gbc_savedirLabel = new GridBagConstraints();
        gbc_savedirLabel.anchor = GridBagConstraints.EAST;
        gbc_savedirLabel.insets = new Insets(0, 0, 5, 5);
        gbc_savedirLabel.gridx = 0;
        gbc_savedirLabel.gridy = 7;
        getContentPane().add(savedirLabel, gbc_savedirLabel);

        folderField = new JTextField();
        GridBagConstraints gbc_folderField = new GridBagConstraints();
        gbc_folderField.gridwidth = 3;
        gbc_folderField.weightx = 0.5;
        gbc_folderField.insets = new Insets(0, 0, 5, 10);
        gbc_folderField.fill = GridBagConstraints.HORIZONTAL;
        gbc_folderField.gridx = 1;
        gbc_folderField.gridy = 4;
        getContentPane().add(folderField, gbc_folderField);
        folderField.setColumns(128);

        folderLabel = new JLabel(MESSAGES.getString("label.folderRoot"));
        GridBagConstraints gbc_folderLabel = new GridBagConstraints();
        gbc_folderLabel.anchor = GridBagConstraints.EAST;
        gbc_folderLabel.insets = new Insets(0, 0, 5, 5);
        gbc_folderLabel.gridx = 0;
        gbc_folderLabel.gridy = 4;
        getContentPane().add(folderLabel, gbc_folderLabel);

        charsetComboBox = new JComboBox(charsetGraphicStrings);
        GridBagConstraints gbc_charsetComboBox = new GridBagConstraints();
        gbc_charsetComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_charsetComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_charsetComboBox.gridx = 1;
        gbc_charsetComboBox.gridy = 5;
        getContentPane().add(charsetComboBox, gbc_charsetComboBox);

        charsetLabel = new JLabel(MESSAGES.getString("label.charset"));
        GridBagConstraints gbc_charsetLabel = new GridBagConstraints();
        gbc_charsetLabel.anchor = GridBagConstraints.EAST;
        gbc_charsetLabel.insets = new Insets(0, 0, 5, 5);
        gbc_charsetLabel.gridx = 0;
        gbc_charsetLabel.gridy = 5;
        getContentPane().add(charsetLabel, gbc_charsetLabel);

        localRadioButton = new JRadioButton(MESSAGES.getString("radio.localExtraction"));
        localRadioButton.setSelected(true);
        GridBagConstraints gbc_localRadioButton = new GridBagConstraints();
        gbc_localRadioButton.gridwidth = 2;
        gbc_localRadioButton.weightx = 0.5;
        gbc_localRadioButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_localRadioButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_localRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_localRadioButton.gridx = 0;
        gbc_localRadioButton.gridy = 0;
        getContentPane().add(localRadioButton, gbc_localRadioButton);
        localRadioButton.setActionCommand("local");
        localRadioButton.addActionListener(app);

        protocoleRadioButton = new JRadioButton(MESSAGES.getString("radio.serverExtraction"));
        GridBagConstraints gbc_protocoleRadioButton = new GridBagConstraints();
        gbc_protocoleRadioButton.gridwidth = 2;
        gbc_protocoleRadioButton.weightx = 0.5;
        gbc_protocoleRadioButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_protocoleRadioButton.anchor = GridBagConstraints.NORTHWEST;
        gbc_protocoleRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_protocoleRadioButton.gridx = 0;
        gbc_protocoleRadioButton.gridy = 2;
        getContentPane().add(protocoleRadioButton, gbc_protocoleRadioButton);
        protocoleRadioButton.setActionCommand("protocole");
        protocoleRadioButton.addActionListener(app);

        JPanel serverPanel = new JPanel();
        serverPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        GridBagConstraints gbc_serverPanel = new GridBagConstraints();
        gbc_serverPanel.gridwidth = 4;
        gbc_serverPanel.weightx = 1.0;
        gbc_serverPanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_serverPanel.insets = new Insets(0, 10, 10, 10);
        gbc_serverPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_serverPanel.gridx = 0;
        gbc_serverPanel.gridy = 3;
        getContentPane().add(serverPanel, gbc_serverPanel);
        GridBagLayout gbl_serverPanel = new GridBagLayout();
        serverPanel.setLayout(gbl_serverPanel);
        serverPanel.setEnabled(false);

        imapRadioButton = new JRadioButton("IMAP");
        imapRadioButton.setEnabled(false);
        GridBagConstraints gbc_imapRadioButton = new GridBagConstraints();
        gbc_imapRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_imapRadioButton.gridx = 1;
        gbc_imapRadioButton.gridy = 0;
        serverPanel.add(imapRadioButton, gbc_imapRadioButton);

        imapsRadioButton = new JRadioButton("IMAPS");
        imapsRadioButton.setEnabled(false);
        imapsRadioButton.setSelected(true);
        GridBagConstraints gbc_imapsRadioButton = new GridBagConstraints();
        gbc_imapsRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_imapsRadioButton.gridx = 0;
        gbc_imapsRadioButton.gridy = 0;
        serverPanel.add(imapsRadioButton, gbc_imapsRadioButton);

        serverLabel = new JLabel(MESSAGES.getString("label.server"));
        serverLabel.setEnabled(false);
        GridBagConstraints gbc_serverLabel = new GridBagConstraints();
        gbc_serverLabel.anchor = GridBagConstraints.EAST;
        gbc_serverLabel.insets = new Insets(0, 0, 5, 5);
        gbc_serverLabel.gridx = 0;
        gbc_serverLabel.gridy = 3;
        serverPanel.add(serverLabel, gbc_serverLabel);

        userLabel = new JLabel(MESSAGES.getString("label.user"));
        userLabel.setEnabled(false);
        GridBagConstraints gbc_userLabel = new GridBagConstraints();
        gbc_userLabel.anchor = GridBagConstraints.EAST;
        gbc_userLabel.insets = new Insets(0, 0, 5, 5);
        gbc_userLabel.gridx = 0;
        gbc_userLabel.gridy = 1;
        serverPanel.add(userLabel, gbc_userLabel);

        userField = new JTextField();
        userField.setEnabled(false);
        GridBagConstraints gbc_userField = new GridBagConstraints();
        gbc_userField.weightx = 0.5;
        gbc_userField.fill = GridBagConstraints.HORIZONTAL;
        gbc_userField.insets = new Insets(0, 0, 5, 5);
        gbc_userField.anchor = GridBagConstraints.NORTHWEST;
        gbc_userField.gridwidth = 2;
        gbc_userField.gridx = 1;
        gbc_userField.gridy = 1;
        serverPanel.add(userField, gbc_userField);
        userField.setColumns(128);

        passwordLabel = new JLabel(MESSAGES.getString("label.password"));
        passwordLabel.setEnabled(false);
        GridBagConstraints gbc_passwordLabel = new GridBagConstraints();
        gbc_passwordLabel.anchor = GridBagConstraints.EAST;
        gbc_passwordLabel.insets = new Insets(0, 0, 5, 5);
        gbc_passwordLabel.gridx = 0;
        gbc_passwordLabel.gridy = 2;
        serverPanel.add(passwordLabel, gbc_passwordLabel);

        passwordField = new JTextField();
        passwordField.setEnabled(false);
        GridBagConstraints gbc_passwordField = new GridBagConstraints();
        gbc_passwordField.weightx = 0.5;
        gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
        gbc_passwordField.insets = new Insets(0, 0, 5, 5);
        gbc_passwordField.anchor = GridBagConstraints.NORTHWEST;
        gbc_passwordField.gridwidth = 2;
        gbc_passwordField.gridx = 1;
        gbc_passwordField.gridy = 2;
        serverPanel.add(passwordField, gbc_passwordField);
        passwordField.setColumns(128);

        serverField = new JTextField();
        serverField.setEnabled(false);
        GridBagConstraints gbc_serverField = new GridBagConstraints();
        gbc_serverField.weightx = 0.5;
        gbc_serverField.gridwidth = 2;
        gbc_serverField.anchor = GridBagConstraints.NORTHWEST;
        gbc_serverField.insets = new Insets(0, 0, 5, 5);
        gbc_serverField.fill = GridBagConstraints.HORIZONTAL;
        gbc_serverField.gridx = 1;
        gbc_serverField.gridy = 3;
        serverPanel.add(serverField, gbc_serverField);
        serverField.setColumns(128);

        JPanel localPanel = new JPanel();
        localPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        GridBagConstraints gbc_localPanel = new GridBagConstraints();
        gbc_localPanel.fill = GridBagConstraints.HORIZONTAL;
        gbc_localPanel.gridwidth = 4;
        gbc_localPanel.insets = new Insets(0, 10, 10, 10);
        gbc_localPanel.weightx = 0.5;
        gbc_localPanel.anchor = GridBagConstraints.NORTHWEST;
        gbc_localPanel.gridx = 0;
        gbc_localPanel.gridy = 1;
        getContentPane().add(localPanel, gbc_localPanel);
        GridBagLayout gbl_containerPanel = new GridBagLayout();
        localPanel.setLayout(gbl_containerPanel);

        pstRadioButton = new JRadioButton("Outlook PST");
        pstRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
        pstRadioButton.setSelected(true);
        GridBagConstraints gbc_pstRadioButton = new GridBagConstraints();
        gbc_pstRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_pstRadioButton.gridx = 0;
        gbc_pstRadioButton.gridy = 0;
        localPanel.add(pstRadioButton, gbc_pstRadioButton);

        thunderbirdRadioButton = new JRadioButton("ThunderBird");
        thunderbirdRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_thunderbirdRadioButton = new GridBagConstraints();
        gbc_thunderbirdRadioButton.weightx = 0.5;
        gbc_thunderbirdRadioButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_thunderbirdRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_thunderbirdRadioButton.gridx = 1;
        gbc_thunderbirdRadioButton.gridy = 0;
        localPanel.add(thunderbirdRadioButton, gbc_thunderbirdRadioButton);

        nameLabel = new JLabel(MESSAGES.getString("label.nameExtraction"));
        GridBagConstraints gbc_nameLabel = new GridBagConstraints();
        gbc_nameLabel.anchor = GridBagConstraints.EAST;
        gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_nameLabel.gridx = 0;
        gbc_nameLabel.gridy = 8;
        getContentPane().add(nameLabel, gbc_nameLabel);

        nameField = new JTextField();
        nameField.setText("");
        GridBagConstraints gbc_nameField = new GridBagConstraints();
        gbc_nameField.insets = new Insets(0, 0, 5, 5);
        gbc_nameField.anchor = GridBagConstraints.NORTHWEST;
        gbc_nameField.weightx = 0.5;
        gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_nameField.gridx = 1;
        gbc_nameField.gridy = 8;
        getContentPane().add(nameField, gbc_nameField);
        nameField.setColumns(128);

        mboxRadioButton = new JRadioButton("Mbox");
        mboxRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_mboxRadioButton = new GridBagConstraints();
        gbc_mboxRadioButton.weightx = 0.5;
        gbc_mboxRadioButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_mboxRadioButton.insets = new Insets(0, 0, 5, 5);
        gbc_mboxRadioButton.gridx = 2;
        gbc_mboxRadioButton.gridy = 0;
        localPanel.add(mboxRadioButton, gbc_mboxRadioButton);

        emlRadioButton = new JRadioButton("EML");
        emlRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_emlRadioButton = new GridBagConstraints();
        gbc_emlRadioButton.weightx = 0.5;
        gbc_emlRadioButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_emlRadioButton.insets = new Insets(0, 0, 5, 0);
        gbc_emlRadioButton.gridx = 3;
        gbc_emlRadioButton.gridy = 0;
        localPanel.add(emlRadioButton, gbc_emlRadioButton);

        msgRadioButton = new JRadioButton("Outlook msg");
        msgRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbc_msgRadioButton = new GridBagConstraints();
        gbc_msgRadioButton.weightx = 0;
        gbc_msgRadioButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_msgRadioButton.insets = new Insets(0, 0, 5, 0);
        gbc_msgRadioButton.gridx = 4;
        gbc_msgRadioButton.gridy = 0;
        localPanel.add(msgRadioButton, gbc_msgRadioButton);

        containerLabel = new JLabel(MESSAGES.getString("label.path"));
        GridBagConstraints gbc_containerLabel = new GridBagConstraints();
        gbc_containerLabel.anchor = GridBagConstraints.EAST;
        gbc_containerLabel.insets = new Insets(0, 0, 0, 5);
        gbc_containerLabel.gridx = 0;
        gbc_containerLabel.gridy = 1;
        localPanel.add(containerLabel, gbc_containerLabel);

        containerField = new JTextField();
        containerField.setText("");
        GridBagConstraints gbc_containerField = new GridBagConstraints();
        gbc_containerField.gridwidth = 3;
        gbc_containerField.insets = new Insets(0, 0, 0, 5);
        gbc_containerField.anchor = GridBagConstraints.NORTHWEST;
        gbc_containerField.weightx = 1.0;
        gbc_containerField.fill = GridBagConstraints.HORIZONTAL;
        gbc_containerField.gridx = 1;
        gbc_containerField.gridy = 1;
        localPanel.add(containerField, gbc_containerField);
        containerField.setColumns(128);

        containerButton = new JButton(MESSAGES.getString("button.path"));
        GridBagConstraints gbc_containerButton = new GridBagConstraints();
        gbc_containerButton.gridx = 4;
        gbc_containerButton.gridy = 1;
        localPanel.add(containerButton, gbc_containerButton);
        containerButton.setActionCommand("container");
        containerButton.addActionListener(app);

        // Case à cocher debug, avec gestion via un lambda
        debugCheckBox = new JCheckBox(MESSAGES.getString("checkbox.debug"));
        GridBagConstraints gbc_debugCheckBox = new GridBagConstraints();
        gbc_debugCheckBox.anchor = GridBagConstraints.WEST;
        gbc_debugCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_debugCheckBox.gridx = 3;
        gbc_debugCheckBox.gridy = 14;
        getContentPane().add(debugCheckBox, gbc_debugCheckBox);

        englishCheckBox = new JCheckBox("English");
        GridBagConstraints gbc_englishCheckBox = new GridBagConstraints();
        gbc_englishCheckBox.anchor = GridBagConstraints.WEST;
        gbc_englishCheckBox.insets = new Insets(0, 0, 5, 5);
        gbc_englishCheckBox.gridx = 0;
        gbc_englishCheckBox.gridy = 14;
        getContentPane().add(englishCheckBox, gbc_englishCheckBox);
        englishCheckBox.addActionListener(e -> {
            if (englishCheckBox.isSelected()) SetLabels(MESSAGES_EN);
            else SetLabels(MESSAGES);
        });

        // RadioButtons
        ButtonGroup groupLocalProtocol = new ButtonGroup();
        groupLocalProtocol.add(localRadioButton);
        groupLocalProtocol.add(protocoleRadioButton);

        ButtonGroup groupLocal = new ButtonGroup();
        groupLocal.add(thunderbirdRadioButton);
        groupLocal.add(pstRadioButton);
        groupLocal.add(emlRadioButton);
        groupLocal.add(msgRadioButton);
        groupLocal.add(mboxRadioButton);

        ButtonGroup groupProtocol = new ButtonGroup();
        groupProtocol.add(imapRadioButton);
        groupProtocol.add(imapsRadioButton);

        pack();
    }

    private void SetLabels(ResourceBundle messages)
    {
        this.setTitle(messages.getString("mainWindow.title"));
        lblNewLabel.setText(messages.getString("label.extractText"));
        extractmessagetextfileCheckBox.setText(messages.getString("checkbox.extractMessageToFile"));
        extractmessagetextmetadataCheckBox.setText(messages.getString("checkbox.extractMessageToMetadata"));
        extractfiletextfileCheckBox.setText(messages.getString("checkbox.extractAttachmentToFile"));
        extractfiletextmetadataCheckBox.setText(messages.getString("checkbox.extractAttachmentToMetadata"));
        extractButton.setText(messages.getString("button.extract"));
        listButton.setText(messages.getString("button.listFolders"));
        statButton.setText(messages.getString("button.listStats"));
        emptyButton.setText(messages.getString("button.clearLog"));
        warningCheckBox.setText(messages.getString("checkbox.warning"));
        loglevelLabel.setText(messages.getString("label.logLevel"));
        keeponlydeepCheckBox.setText(messages.getString("checkbox.keepOnlyDeep"));
        namesLengthLabel.setText(messages.getString("label.namesLength"));
        extractlistCheckBox.setText(messages.getString("checkbox.extractList"));
        savedirButton.setText(messages.getString("button.savedir"));
        dropemptyfoldersCheckBox.setText(messages.getString("checkbox.dropEmptyFolders"));
        savedirLabel.setText(messages.getString("label.savedir"));
        folderLabel.setText(messages.getString("label.folderRoot"));
        charsetLabel.setText(messages.getString("label.charset"));
        localRadioButton.setText(messages.getString("radio.localExtraction"));
        protocoleRadioButton.setText(messages.getString("radio.serverExtraction"));
        serverLabel.setText(messages.getString("label.server"));
        userLabel.setText(messages.getString("label.user"));
        passwordLabel.setText(messages.getString("label.password"));
        nameLabel.setText(messages.getString("label.nameExtraction"));
        containerLabel.setText(messages.getString("label.path"));
        containerButton.setText(messages.getString("button.path"));
        debugCheckBox.setText(messages.getString("checkbox.debug"));
        pack();
        revalidate();
        repaint();
    }
}
