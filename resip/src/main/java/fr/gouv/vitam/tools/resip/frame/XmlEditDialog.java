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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.AddMetadataItem;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.UIManager;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.ScrollPaneConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class XmlEditDialog.
 */
public class XmlEditDialog extends JDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4092514078236156033L;

	/** The xml text area. */
	//private XmlTextPane editTextPane;
	private RSyntaxTextArea xmlTextArea;
	
	/** The information text area. */
	private JTextArea informationTextArea;
	
	/** The xml object. */
	private Object xmlObject;

	/** The indented xml data. */
	public String indentedXmlData;

	/**
	 * Create the dialog.
	 *
	 * @param owner the owner
	 * @param xmlObject the xml object
	 */
	
	
	public XmlEditDialog(JFrame owner, Object xmlObject) {
		super(owner, "", true);
		this.xmlObject = xmlObject;
		String title, presentation, xmlData="";

		if (xmlObject instanceof DataObjectPackageTreeNode) {
			DataObjectPackageTreeNode node=(DataObjectPackageTreeNode) xmlObject;
			ArchiveUnit au = node.getArchiveUnit();
			title = "Edition ArchiveUnit";
			presentation = "ArchiveUnit:" + node.getTitle() + " - " + au.getInDataObjectPackageId();
			try {
				xmlData = au.toSedaXmlFragments();
				xmlData = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
						.indentString(xmlData);
			} catch (Exception e) {
				ResipLogger.getGlobalLogger().log(ResipLogger.STEP,
						"Resip.InOut: Erreur à l'indentation de l'ArchiveUnit [" + au.getInDataObjectPackageId() + "]");
			}
		} else if (xmlObject instanceof DataObject) {
			DataObject dataObject = (DataObject) xmlObject;
			title = "Edition DataObject";
			presentation = "DataObject:" + dataObject.getInDataObjectPackageId();
			try {
				xmlData = dataObject.toSedaXmlFragments();
				xmlData = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
						.indentString(xmlData);
			} catch (Exception e) {
				ResipLogger.getGlobalLogger().log(ResipLogger.STEP,"Resip.InOut: Erreur à l'indentation du DataObject ["
						+ dataObject.getInDataObjectPackageId() + "]");
			}

		} else if (xmlObject instanceof AddMetadataItem) {
			AddMetadataItem addMetadataItem = (AddMetadataItem) xmlObject;
			title = "Edition partielle de métadonnées";
			presentation = addMetadataItem.elementName+": "+ addMetadataItem.extraInformation;
			try {
				xmlData = addMetadataItem.skeleton.toString();
			} catch (Exception e) {
				ResipLogger.getGlobalLogger().log(ResipLogger.STEP,"Resip.InOut: Erreur à l'indentation de la métadonnée ["
						+ addMetadataItem.elementName + "]");
			}

		} else {
			dispose();
			return;
		}

		setTitle(title);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0 };
		getContentPane().setLayout(gridBagLayout);

		JTextArea presentationTextArea = new JTextArea();
		presentationTextArea.setText(presentation);
		presentationTextArea.setEditable(false);
		presentationTextArea.setFont(UIManager.getFont("OptionPane.font"));
		presentationTextArea.setBackground(UIManager.getColor("Label.background"));
		presentationTextArea.setLineWrap(true);
		presentationTextArea.setWrapStyleWord(true);
		GridBagConstraints gbc_presentationTextArea = new GridBagConstraints();
		gbc_presentationTextArea.insets = new Insets(0, 0, 5, 5);
		gbc_presentationTextArea.weightx = 1.0;
		gbc_presentationTextArea.fill = GridBagConstraints.BOTH;
		gbc_presentationTextArea.gridx = 0;
		gbc_presentationTextArea.gridy = 0;
		getContentPane().add(presentationTextArea, gbc_presentationTextArea);

		xmlTextArea = new RSyntaxTextArea(20,120);
		xmlTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		xmlTextArea.setCodeFoldingEnabled(true);
		xmlTextArea.setText(xmlData);
		xmlTextArea.setCaretPosition(0);

		JScrollPane scrollPane = new RTextScrollPane(xmlTextArea);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(600, 400));
		scrollPane.setMinimumSize(new Dimension(200, 100));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.weightx = 1.0;
		gbc_scrollPane.anchor = GridBagConstraints.NORTHWEST;
		gbc_scrollPane.weighty = 1.0;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		getContentPane().add(scrollPane, gbc_scrollPane);

		informationTextArea = new JTextArea("");
		informationTextArea.setFont(UIManager.getFont("OptionPane.font"));
		informationTextArea.setEditable(false);
		informationTextArea.setLineWrap(true);
		informationTextArea.setWrapStyleWord(true);
		informationTextArea.setBackground(UIManager.getColor("Label.background"));
		informationTextArea.setForeground(Color.BLACK);
		GridBagConstraints gbc_informationLabel = new GridBagConstraints();
		gbc_informationLabel.fill = GridBagConstraints.BOTH;
		gbc_informationLabel.weightx = 1.0;
		gbc_informationLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_informationLabel.insets = new Insets(0, 0, 5, 5);
		gbc_informationLabel.gridx = 0;
		gbc_informationLabel.gridy = 2;
		getContentPane().add(informationTextArea, gbc_informationLabel);

		JPanel buttonPane = new JPanel();
		GridBagLayout gbl_buttonPane = new GridBagLayout();
		buttonPane.setLayout(gbl_buttonPane);
		GridBagConstraints gbc_buttonPane = new GridBagConstraints();
		gbc_buttonPane.weighty = 0.1;
		gbc_buttonPane.insets = new Insets(0, 0, 5, 0);
		gbc_buttonPane.weightx = 1.0;
		gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_buttonPane.gridx = 0;
		gbc_buttonPane.gridy = 3;
		getContentPane().add(buttonPane, gbc_buttonPane);

		final JButton indentButton = new JButton("Indenter");
		indentButton.addActionListener(arg0 -> buttonIndent());
		GridBagConstraints gbc_indentButton = new GridBagConstraints();
		gbc_indentButton.insets = new Insets(0, 0, 5, 5);
		gbc_indentButton.weightx = 1.0;
		gbc_indentButton.gridy = 0;
		gbc_indentButton.gridx = 0;
		buttonPane.add(indentButton, gbc_indentButton);

		final JButton saveButton = new JButton("Sauver");
		saveButton.addActionListener(arg0 -> buttonSaveXmlEdit());
		GridBagConstraints gbc_validateButton = new GridBagConstraints();
		gbc_validateButton.insets = new Insets(0, 0, 5, 5);
		gbc_validateButton.weightx = 1.0;
		gbc_validateButton.gridy = 0;
		gbc_validateButton.gridx = 1;
		buttonPane.add(saveButton, gbc_validateButton);

		final JButton cancelButton = new JButton("Annuler");
		cancelButton.addActionListener(arg0 -> buttonCancel());
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.insets = new Insets(0, 0, 5, 0);
		gbc_cancelButton.weightx = 1.0;
		gbc_cancelButton.gridy = 0;
		gbc_cancelButton.gridx = 2;
		buttonPane.add(cancelButton, gbc_cancelButton);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancelButton.doClick();
			}
		});

		pack();
		setLocationRelativeTo(owner);
	}

	// buttons

	/**
	 * Button indent.
	 */
	public void buttonIndent() {
		try {
			String xml = getXmlTextArea().getText();
			String indentedString = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT).indentString(xml);
			getXmlTextArea().setText(indentedString);
			getInformationTextArea().setForeground(Color.BLACK);
			getInformationTextArea().setText("XML indenté");
		} catch (Exception e) {
			getXmlTextArea().revalidate();
			getInformationTextArea().setForeground(Color.RED);
			getInformationTextArea().setText("XML mal formé (" + e.getMessage() + ")");
		}
	}

	/**
	 * Button cancel.
	 */
	public void buttonCancel() {
		indentedXmlData = null;
		setVisible(false);
	}

	/**
	 * Button save xml edit.
	 */
	private void buttonSaveXmlEdit() {
		try {
			indentedXmlData = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
					.indentString(getXmlTextArea().getText());
			if (xmlObject instanceof DataObjectPackageTreeNode) {
				DataObjectPackageTreeNode node=(DataObjectPackageTreeNode)xmlObject;
				ArchiveUnit au = node.getArchiveUnit();
				au.fromSedaXmlFragments(indentedXmlData);
				node.setTitle(SEDAXMLEventReader.extractNamedElement("Title", indentedXmlData));
				ResipGraphicApp.getTheApp().mainWindow.updateAUMetadata(node);
			} else if (xmlObject instanceof DataObject) {
				DataObject dataObject = (DataObject) xmlObject;
				dataObject.fromSedaXmlFragments(indentedXmlData);
			}
			else if (xmlObject instanceof AddMetadataItem) {
				AddMetadataItem addMetadataItem = (AddMetadataItem) xmlObject;
				addMetadataItem.skeleton = SEDAMetadata.fromString(indentedXmlData, addMetadataItem.skeleton.getClass());
				indentedXmlData=addMetadataItem.skeleton.toString();
			}
			getInformationTextArea().setForeground(Color.BLACK);
			getInformationTextArea().setText("");
			setVisible(false);
		} catch (Exception e) {
			getInformationTextArea().setForeground(Color.RED);
			getInformationTextArea().setText(e.getMessage());
		}
	}

	/**
	 * Gets the xml text area.
	 *
	 * @return the xml text area
	 */
	public RSyntaxTextArea getXmlTextArea() {
		return xmlTextArea;
	}

	/**
	 * Gets the information text area.
	 *
	 * @return the information text area
	 */
	public JTextArea getInformationTextArea() {
		return informationTextArea;
	}
}
