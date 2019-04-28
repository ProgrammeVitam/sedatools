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
import fr.gouv.vitam.tools.resip.data.AddMetadataItem;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AgentType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

/**
 * The Class XmlEditDialog.
 * <p>
 * Class for editing XML dialog acting on different sources AU, GOT and part of AU.
 */
public class XmlEditDialog extends JDialog {

    /**
     * The actions components.
     */
    private RSyntaxTextArea xmlTextArea;
    private JTextArea informationTextArea;
    private JPanel informationPanel;

    /**
     * The data.
     */
    private Object xmlObject;

    /**
     * The result.
     */
    private String xmlDataString;

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
        TestDialogWindow window = new TestDialogWindow(XmlEditDialog.class);
    }

    /**
     * Instantiates a new XmlEditDialog for test.
     *
     * @param owner the owner
     */
    public XmlEditDialog(JFrame owner) throws SEDALibException {
        this(owner, new AddMetadataItem(AgentType.class, "Writer"));
    }

    /**
     * Create the dialog.
     *
     * @param owner     the owner
     * @param xmlObject the xml object
     */
    public XmlEditDialog(JFrame owner, Object xmlObject) {
        super(owner, "", true);
        this.xmlObject = xmlObject;
        String title, presentationName, presentationText, xmlData = "";
        GridBagConstraints gbc;

        if (xmlObject instanceof DataObjectPackageTreeNode) {
            DataObjectPackageTreeNode node = (DataObjectPackageTreeNode) xmlObject;
            ArchiveUnit au = node.getArchiveUnit();
            title = "Edition ArchiveUnit";
            presentationName = "ArchiveUnit :";
            presentationText = node.getTitle() + " - " + au.getInDataObjectPackageId();
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
            presentationName = "DataObject :";
            presentationText = dataObject.getInDataObjectPackageId();
            try {
                xmlData = dataObject.toSedaXmlFragments();
                xmlData = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
                        .indentString(xmlData);
            } catch (Exception e) {
                ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Resip.InOut: Erreur à l'indentation du DataObject ["
                        + dataObject.getInDataObjectPackageId() + "]");
            }

        } else if (xmlObject instanceof AddMetadataItem) {
            AddMetadataItem addMetadataItem = (AddMetadataItem) xmlObject;
            title = "Edition partielle de métadonnées";
            presentationName = addMetadataItem.elementName + " :";
            presentationText = addMetadataItem.extraInformation;
            try {
                xmlData = addMetadataItem.skeleton.toString();
            } catch (Exception e) {
                ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Resip.InOut: Erreur à l'indentation de la métadonnée ["
                        + addMetadataItem.elementName + "]");
            }

        } else {
            dispose();
            return;
        }

        setTitle(title);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(500, 500));

        final JPanel presentationPanel = new JPanel();
        presentationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(presentationPanel, gbc);
        JLabel presentationLabel = new JLabel();
        presentationLabel.setText(presentationName);
        presentationLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        presentationPanel.add(presentationLabel, gbc);
        JTextArea presentationTextArea = new JTextArea();
        presentationTextArea.setText(presentationText);
        presentationTextArea.setEditable(false);
        presentationTextArea.setFont(MainWindow.LABEL_FONT);
        presentationTextArea.setBackground(MainWindow.GENERAL_BACKGROUND);
        presentationTextArea.setLineWrap(true);
        presentationTextArea.setWrapStyleWord(true);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        presentationPanel.add(presentationTextArea, gbc);

        xmlTextArea = new RSyntaxTextArea(20, 120);
        xmlTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        xmlTextArea.setCodeFoldingEnabled(true);
        xmlTextArea.setFont(MainWindow.DETAILS_FONT);
        xmlTextArea.setText(xmlData);
        xmlTextArea.setCaretPosition(0);
        JScrollPane editScrollPane = new RTextScrollPane(xmlTextArea);
        editScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        editScrollPane.setMinimumSize(new Dimension(200, 100));
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPane.add(editScrollPane, gbc);

        informationPanel = new JPanel();
        informationPanel.setLayout(new GridBagLayout());
        informationPanel.setVisible(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(informationPanel, gbc);
        JLabel informationLabel = new JLabel();
        informationLabel.setText("");
        informationLabel.setIcon(new ImageIcon(getClass().getResource("/icon/dialog-warning.png")));
        informationLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        informationPanel.add(informationLabel, gbc);
        informationTextArea = new JTextArea("");
        informationTextArea.setFont(MainWindow.LABEL_FONT);
        informationTextArea.setEditable(false);
        informationTextArea.setLineWrap(true);
        informationTextArea.setWrapStyleWord(true);
        informationTextArea.setBackground(MainWindow.GENERAL_BACKGROUND);
        informationTextArea.setForeground(Color.RED);
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        informationPanel.add(informationTextArea, gbc);

        JPanel actionPanel = new JPanel();
        GridBagLayout gbl_buttonPane = new GridBagLayout();
        actionPanel.setLayout(gbl_buttonPane);
        gbc = new GridBagConstraints();
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 3;
        getContentPane().add(actionPanel, gbc);
        final JButton indentButton = new JButton("Indenter");
        indentButton.addActionListener(arg -> buttonIndent());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.weightx = 1.0;
        gbc.gridy = 0;
        gbc.gridx = 0;
        actionPanel.add(indentButton, gbc);
        final JButton saveButton = new JButton("Sauver");
        saveButton.addActionListener(arg -> buttonSaveXmlEdit());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.weightx = 1.0;
        gbc.gridy = 0;
        gbc.gridx = 1;
        actionPanel.add(saveButton, gbc);
        if (xmlObject instanceof DataObjectPackageTreeNode) {
            final JButton canonizeButton = new JButton("Ordonner");
            canonizeButton.addActionListener(arg -> buttonCanonizeXmlEdit());
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 5, 5);
            gbc.weightx = 1.0;
            gbc.gridy = 0;
            gbc.gridx = 2;
            actionPanel.add(canonizeButton, gbc);
        }
        final JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(arg -> buttonCancel());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.gridy = 0;
        gbc.gridx = 3;
        actionPanel.add(cancelButton, gbc);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelButton.doClick();
            }
        });

        pack();
        informationPanel.setVisible(false);
        pack();
        setLocationRelativeTo(owner);
    }

    // actions

    private void showWarning(String text) {
        informationTextArea.setText(text);
        if (!informationPanel.isVisible()) {
            Dimension dim = this.getSize();
            informationPanel.setVisible(true);
            pack();
            dim.height = dim.height + informationPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        }
    }

    private void hideWarning() {
        if (informationPanel.isVisible()) {
            informationPanel.setVisible(false);
            Dimension dim = this.getSize();
            dim.height = dim.height - informationPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        }
    }

    public void buttonIndent() {
        try {
            String xml = xmlTextArea.getText();
            String indentedString = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT).indentString(xml);
            xmlTextArea.setText(indentedString);
            hideWarning();
            xmlTextArea.setCaretPosition(0);
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    private void buttonCancel() {
        xmlDataString = null;
        setVisible(false);
    }

    private void buttonSaveXmlEdit() {
        try {
            xmlDataString = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
                    .indentString(xmlTextArea.getText());
            if (xmlObject instanceof DataObjectPackageTreeNode) {
                DataObjectPackageTreeNode node = (DataObjectPackageTreeNode) xmlObject;
                ArchiveUnit au = node.getArchiveUnit();
                au.fromSedaXmlFragments(xmlDataString);
                node.setTitle(SEDAXMLEventReader.extractNamedElement("Title", xmlDataString));
                ResipGraphicApp.getTheApp().mainWindow.updateAUMetadata(node);
            } else if (xmlObject instanceof DataObject) {
                DataObject dataObject = (DataObject) xmlObject;
                dataObject.fromSedaXmlFragments(xmlDataString);
            } else if (xmlObject instanceof AddMetadataItem) {
                AddMetadataItem addMetadataItem = (AddMetadataItem) xmlObject;
                addMetadataItem.skeleton = SEDAMetadata.fromString(xmlDataString, addMetadataItem.skeleton.getClass());
                xmlDataString = addMetadataItem.skeleton.toString();
            }
            informationTextArea.setForeground(Color.BLACK);
            informationTextArea.setText("");
            setVisible(false);
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    private void buttonCanonizeXmlEdit() {
        try {
            xmlDataString = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
                    .indentString(xmlTextArea.getText());
            ArchiveUnit au = new ArchiveUnit();
            au.fromSedaXmlFragments(xmlDataString);
            au.getContent();
            au.getManagement();
            au.getArchiveUnitProfile();
            String xmlData = au.toSedaXmlFragments();
            xmlData = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
                    .indentString(xmlData);
            xmlTextArea.setText(xmlData);
            informationTextArea.setForeground(Color.BLACK);
            informationTextArea.setText("");
        } catch (Exception e) {
            showWarning(e.getMessage());
        }
    }

    /**
     * Get the dialog result xml string.
     *
     * @return the xml string
     */
    public String getResult() {
        return xmlDataString;
    }

    /**
     * Get the dialog return value.
     *
     * @return the return value
     */
    public boolean getReturnValue() {
        return !(xmlDataString == null);
    }
}
