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

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.parameters.MailImportContext;
import fr.gouv.vitam.tools.resip.parameters.Prefs;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * The class MailImportContextDialog.
 * <p>
 * Class for mail extraction and import context definition dialog.
 */
public class MailImportContextDialog extends JDialog {

	/**
	 * The actions components.
	 */
	private JCheckBox messageFileCheckBox;
	private JCheckBox attachementFileCheckBox;
	private JCheckBox messageMetadataCheckBox;
	private JCheckBox attachementMetadataCheckBox;
	private JRadioButton pstRadioButton;
	private JRadioButton msgRadioButton;
	private JRadioButton tdbRadioButton;
	private JRadioButton mboxRadioButton;
	private JRadioButton emlRadioButton;
	private JComboBox defaultCharsetCombobox;

    /**
     * The result.
     */
    public int returnValue;

	/**
	 * The proposed charsets.
	 */
	static private String[] charsetStrings = {"windows-1252", "ISO-8859-1", "UTF-8", "CESU-8", "IBM00858", "IBM437", "IBM775",
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
     */
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		TestDialogWindow window = new TestDialogWindow(MailImportContextDialog.class);
	}

    /**
     * Instantiates a new MailImportContextDialog for test.
     *
     * @param owner the owner
     */
    public MailImportContextDialog(JFrame owner) {
		this(owner, new MailImportContext(Prefs.getInstance()));
	}

    /**
     * Create the dialog.
     *
     * @param owner             the owner
     * @param mailImportContext the mail import context
     */
    public MailImportContextDialog(JFrame owner, MailImportContext mailImportContext) {
		super(owner, "Edition des paramètres d'extraction des courriels", true);
		GridBagConstraints gbc;

		//this.setPreferredSize(new Dimension(500, 300));
		this.setMinimumSize(new Dimension(550, 230));

		Container contentPane = getContentPane();
		GridBagLayout gridBagLayout = new GridBagLayout();
		contentPane.setLayout(new GridBagLayout());
		
		// Parameters Panel
		JPanel parametersPanel = new JPanel();
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		contentPane.add(parametersPanel, gbc);
		GridBagLayout gbl_parametersPanel = new GridBagLayout();
		gbl_parametersPanel.columnWeights = new double[] { 0.1, 0.5, 0.5 };
		parametersPanel.setLayout(gbl_parametersPanel);

		JLabel protocolLabel = new JLabel("Protocole d'extraction courriel :");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 1;
		parametersPanel.add(protocolLabel, gbc);

		pstRadioButton = new JRadioButton("Outlook-Pst");
		pstRadioButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 1;
		gbc.gridy = 1;
		parametersPanel.add(pstRadioButton, gbc);

		msgRadioButton = new JRadioButton("Outlook-Msg");
		msgRadioButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 2;
		gbc.gridy = 1;
		parametersPanel.add(msgRadioButton, gbc);

		tdbRadioButton = new JRadioButton("Thunderbird");
		tdbRadioButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 1;
		gbc.gridy = 2;
		parametersPanel.add(tdbRadioButton, gbc);

		mboxRadioButton = new JRadioButton("Mbox");
		mboxRadioButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 2;
		gbc.gridy =2;
		parametersPanel.add(mboxRadioButton, gbc);

		emlRadioButton = new JRadioButton("Eml");
		emlRadioButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 0, 5);
		gbc.gridx = 1;
		gbc.gridy =3;
		parametersPanel.add(emlRadioButton, gbc);

		ButtonGroup protocolButtonGroup = new ButtonGroup();
		protocolButtonGroup.add(pstRadioButton);
		protocolButtonGroup.add(msgRadioButton);
		protocolButtonGroup.add(tdbRadioButton);
		protocolButtonGroup.add(mboxRadioButton);
		protocolButtonGroup.add(emlRadioButton);
		pstRadioButton.setSelected("pst".equals(mailImportContext.getProtocol()));
		msgRadioButton.setSelected("msg".equals(mailImportContext.getProtocol()));
		tdbRadioButton.setSelected("thunderbird".equals(mailImportContext.getProtocol()));
		mboxRadioButton.setSelected("mbox".equals(mailImportContext.getProtocol()));
		emlRadioButton.setSelected("eml".equals(mailImportContext.getProtocol()));

		defaultCharsetCombobox = new JComboBox<String>(charsetStrings);
		defaultCharsetCombobox.setFont(MainWindow.LABEL_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 4;
		parametersPanel.add(defaultCharsetCombobox, gbc);
		defaultCharsetCombobox.setSelectedItem(mailImportContext.getDefaultCharsetName());

		JLabel charsetLabel = new JLabel("Encodage par défaut :");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 4;
		parametersPanel.add(charsetLabel, gbc);

		JLabel mailFileTextExtractAULabel = new JLabel("Extraction des fichiers textes des courriels :");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 5;
		parametersPanel.add(mailFileTextExtractAULabel, gbc);

		messageFileCheckBox = new JCheckBox("des messages");
		messageFileCheckBox.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 5;
		parametersPanel.add(messageFileCheckBox, gbc);
		messageFileCheckBox.setSelected(mailImportContext.isExtractMessageTextFile());

		attachementFileCheckBox = new JCheckBox("des pièces jointes");
		attachementFileCheckBox.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 2;
		gbc.gridy = 5;
		parametersPanel.add(attachementFileCheckBox, gbc);
		attachementFileCheckBox.setSelected(mailImportContext.isExtractAttachmentTextFile());

		JLabel mailMetadataTextExtractAULabel = new JLabel("Extraction des métadonnées textuelles des courriels :");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 6;
		parametersPanel.add(mailMetadataTextExtractAULabel, gbc);

		messageMetadataCheckBox = new JCheckBox("des messages");
		messageMetadataCheckBox.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 6;
		parametersPanel.add(messageMetadataCheckBox, gbc);
		messageMetadataCheckBox.setSelected(mailImportContext.isExtractMessageTextMetadata());

		attachementMetadataCheckBox = new JCheckBox("des pièces jointes");
		attachementMetadataCheckBox.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = 2;
		gbc.gridy = 6;
		parametersPanel.add(attachementMetadataCheckBox, gbc);
		attachementMetadataCheckBox.setSelected(mailImportContext.isExtractAttachmentTextMetadata());

		// Buttons
		JButton cancelButton = new JButton("Annuler");
		cancelButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor=GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		contentPane.add(cancelButton, gbc);
		cancelButton.addActionListener(arg -> buttonCancel());

		JButton okButton = new JButton("OK");
		okButton.setFont(MainWindow.CLICK_FONT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor=GridBagConstraints.CENTER;
		gbc.weightx = 1.0;
		contentPane.add(okButton, gbc);
		okButton.addActionListener(arg -> buttonOk());

		pack();
		setLocationRelativeTo(owner);
	}

	// actions

	private void buttonCancel() {
		returnValue = ResipGraphicApp.KO_DIALOG;
		setVisible(false);
	}

	private void buttonOk() {
		returnValue = ResipGraphicApp.OK_DIALOG;
		setVisible(false);
	}

    /**
     * Sets the mail import context from dialog.
     *
     * @param mailImportContext the mail import context
     */
    public void setMailImportContextFromDialog(MailImportContext mailImportContext) {
		mailImportContext.setExtractMessageTextFile(messageFileCheckBox.isSelected());
		mailImportContext.setExtractMessageTextMetadata(messageMetadataCheckBox.isSelected());
		mailImportContext.setExtractAttachmentTextFile(attachementFileCheckBox.isSelected());
		mailImportContext.setExtractAttachmentTextMetadata(attachementMetadataCheckBox.isSelected());
		mailImportContext.setExtractMessageTextFile(messageFileCheckBox.isSelected());
		if (pstRadioButton.isSelected())
			mailImportContext.setProtocol("pst");
		else if (msgRadioButton.isSelected())
			mailImportContext.setProtocol("msg");
		else if (tdbRadioButton.isSelected())
			mailImportContext.setProtocol("thunderbird");
		else if (mboxRadioButton.isSelected())
			mailImportContext.setProtocol("mbox");
		else if (emlRadioButton.isSelected())
			mailImportContext.setProtocol("eml");
		mailImportContext.setDefaultCharsetName((String) defaultCharsetCombobox.getSelectedItem());
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
