package fr.gouv.vitam.tools.resip.metadataeditor.components.xmlcomponents;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.MainWindow;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.frame.XmlEditDialog;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.components.ArchiveUnitEditorPanel;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor.*;

public class XMLArchiveUnitEditorPanel extends JPanel implements ArchiveUnitEditorPanel {
    /**
     * The Add content metadata array.
     */
    static String[] addContentMetadataArray = null;

    ArchiveUnit archiveUnit;

    JLabel globalLabel;
    RSyntaxTextArea xmlTextArea;
    JButton editButton, addButton;
    JComboBox<String> choiceComboBox;

    /**
     * Get add content metadata array string [ ].
     *
     * @return the string [ ]
     */
    public static String[] getAddContentMetadataArray() {
        if (addContentMetadataArray == null) {
            try {
                List<String> tmp = new ArrayList<String>();
                tmp.add("[A]ArchiveUnitProfile ");
                Content c = new Content();
                List<String> contentMetadataList = new ArrayList<String>();
                contentMetadataList.add("[C]AnyOtherMetadata ");
                for (String metadataName : c.getMetadataOrderedList()) {
                    ComplexListMetadataKind complexListMetadataKind = c.getMetadataMap().get(metadataName);
                    contentMetadataList.add("[C]" + metadataName + (complexListMetadataKind.many ? " *" : " "));
                }
                contentMetadataList.sort(String::compareTo);
                tmp.addAll(contentMetadataList);
                Management m = new Management();
                List<String> managementMetadataList = new ArrayList<String>();
                managementMetadataList.add("[C]AnyOtherMetadata ");
                for (String metadataName : m.getMetadataOrderedList()) {
                    ComplexListMetadataKind complexListMetadataKind = m.getMetadataMap().get(metadataName);
                    managementMetadataList.add("[M]" + metadataName + (complexListMetadataKind.many ? " *" : " "));
                }
                managementMetadataList.sort(String::compareTo);
                tmp.addAll(managementMetadataList);
                addContentMetadataArray = tmp.toArray(new String[0]);
            } catch (SEDALibException e) {
                addContentMetadataArray = new String[0];
            }
        }
        return addContentMetadataArray;
    }

    public XMLArchiveUnitEditorPanel() {
        this.archiveUnit = null;
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{0.0, 1.0, 0.0};
        gbl.rowWeights = new double[]{0.0, 1.0, 0.0};
        setLayout(gbl);

        globalLabel = new JLabel(translate("ArchiveUnit") + " - pas de sélection");
        globalLabel.setFont(MetadataEditor.BOLD_LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 5, 5, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(globalLabel, gbc);


        xmlTextArea = new RSyntaxTextArea(20, 80);
        xmlTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        SyntaxScheme scheme = xmlTextArea.getSyntaxScheme();
        scheme.getStyle(Token.MARKUP_TAG_DELIMITER).foreground = COMPOSITE_LABEL_MARKUP_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_NAME).foreground = COMPOSITE_LABEL_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE).foreground = COMPOSITE_LABEL_MARKUP_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE_VALUE).foreground = COMPOSITE_LABEL_ATTRIBUTE_COLOR;
        xmlTextArea.setCodeFoldingEnabled(true);
        xmlTextArea.setFont(MetadataEditor.EDIT_FONT);
        xmlTextArea.setEditable(false);
        JScrollPane auMetadataPaneScrollPane = new RTextScrollPane(xmlTextArea);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        add(auMetadataPaneScrollPane, gbc);

        editButton = new JButton("Editer");
        editButton.setFont(MainWindow.CLICK_FONT);
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editButton(e));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(editButton, gbc);

        addButton = new JButton("Ajouter...");
        addButton.setFont(MainWindow.CLICK_FONT);
        addButton.setEnabled(false);
        addButton.addActionListener(e -> addButton(e));
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(addButton, gbc);

        choiceComboBox = new JComboBox<String>(getAddContentMetadataArray());
        choiceComboBox.setFont(MetadataEditor.LABEL_FONT);
        choiceComboBox.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 2;
        add(choiceComboBox, gbc);

    }

    private void editButton(ActionEvent event) {
        XmlEditDialog xmlEditDialog = new XmlEditDialog(ResipGraphicApp.getTheWindow(), archiveUnit);
        xmlEditDialog.setVisible(true);
        if (xmlEditDialog.getReturnValue()) {
            String xmlDataString = archiveUnit.toSedaXmlFragments();
            ((DataObjectPackageTreeModel) ResipGraphicApp.getTheWindow().getDataObjectPackageTreePaneViewer().getModel())
                    .nodeChanged(ResipGraphicApp.getTheWindow().dataObjectPackageTreeItemDisplayed);
            xmlTextArea.setText(xmlDataString);
            String title = null;
            try {
                title = archiveUnit.getContent().getSimpleMetadata("Title");
            } catch (SEDALibException ignored) {
            }
            if (title == null)
                title = SEDAXMLEventReader.extractNamedElement("Title", xmlDataString);
            if (title == null)
                title = "Inconnu";
            ResipGraphicApp.getTheWindow().dataObjectPackageTreeItemDisplayed.setTitle(title);
            xmlTextArea.setCaretPosition(0);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        }
    }

    private SEDAMetadata getAddedMetadataSample(String choiceName) throws SEDALibException {
        SEDAMetadata result;
        String macroMetadata = choiceName.substring(0, 3);
        String elementName = choiceName.substring(3, choiceName.indexOf(' '));
        if (macroMetadata.equals("[A]")) {
            result = MetadataEditor.createMetadataSample("ArchiveUnitProfile", "ArchiveUnitProfile", false);
        } else {
            HashMap<String, ComplexListMetadataKind> metadataMap;
            if (macroMetadata.equals("[C]")) {
                metadataMap = Content.metadataMap;
            } else {
                metadataMap = Management.metadataMap;
            }
            if (elementName.equals("AnyOtherMetadata"))
                result = MetadataEditor.createMetadataSample("AnyXMLType", elementName, false);
            else
                result = MetadataEditor.createMetadataSample(metadataMap.get(elementName).metadataClass.getSimpleName(), elementName, false);
        }
        return result;
    }

    /**
     * Button add metadata.
     */
    private void addButton(ActionEvent event) {
        try {
            SEDAMetadata sm = getAddedMetadataSample((String) choiceComboBox.getSelectedItem());
            XmlEditDialog xmlEditDialog = new XmlEditDialog(ResipGraphicApp.getTheWindow(), sm);
            xmlEditDialog.setVisible(true);
            if (xmlEditDialog.getReturnValue()) {
                sm = (SEDAMetadata) xmlEditDialog.getResult();
                String macroMetadata = ((String) choiceComboBox.getSelectedItem()).substring(0, 3);
                switch (macroMetadata) {
                    case "[A]":
                        archiveUnit.setArchiveUnitProfile((ArchiveUnitProfile) sm);
                        break;
                    case "[C]":
                        archiveUnit.getContent().addMetadata(sm);
                        break;
                    case "[M]":
                        archiveUnit.getManagement().addMetadata(sm);
                        break;
                }
                ((DataObjectPackageTreeModel) ResipGraphicApp.getTheWindow().getDataObjectPackageTreePaneViewer().getModel())
                        .nodeChanged(ResipGraphicApp.getTheWindow().dataObjectPackageTreeItemDisplayed);

                xmlTextArea.setText(archiveUnit.toSedaXmlFragments());
                xmlTextArea.setCaretPosition(0);
                ResipGraphicApp.getTheApp().setModifiedContext(true);
            }
        } catch (SEDALibException e) {
            UserInteractionDialog.getUserAnswer((JFrame) (SwingUtilities.windowForComponent(this)),
                    "L'édition des métadonnées de l'ArchiveUnit n'a pas été possible.\n->"
                            + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "L'édition des métadonnées de l'ArchiveUnit n'a pas été possible.\n->"
                    + e.getMessage());
        }
    }

    public void editArchiveUnit(ArchiveUnit archiveUnit) throws SEDALibException {
        this.archiveUnit = archiveUnit;
        if (archiveUnit == null) {
            editButton.setEnabled(false);
            addButton.setEnabled(false);
            choiceComboBox.setEnabled(false);
            xmlTextArea.setText("");
            globalLabel.setText(translate("ArchiveUnit") + " - pas de sélection");
        } else {
            editButton.setEnabled(true);
            addButton.setEnabled(true);
            choiceComboBox.setEnabled(true);
            xmlTextArea.setText(archiveUnit.toSedaXmlFragments());
            globalLabel.setText(translate("ArchiveUnit") + " - " + archiveUnit.getInDataObjectPackageId());
        }
    }

    public ArchiveUnit extractArchiveUnit() throws SEDALibException {
        return archiveUnit;
    }
}
