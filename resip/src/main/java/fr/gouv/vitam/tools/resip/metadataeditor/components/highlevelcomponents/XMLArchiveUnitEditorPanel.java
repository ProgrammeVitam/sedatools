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
package fr.gouv.vitam.tools.resip.metadataeditor.components.highlevelcomponents;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.MainWindow;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.frame.XmlEditDialog;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
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
import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;

public class XMLArchiveUnitEditorPanel extends JPanel implements ArchiveUnitEditorPanel {
    /**
     * The Add content metadata array.
     */
    static private String[] addContentMetadataArray = null;

    /**
     * The metadata.
     */
    private ArchiveUnit archiveUnit;

    /**
     * The graphic elements
     */
    private JLabel globalLabel;
    private RSyntaxTextArea xmlTextArea;
    private JButton editButton, addButton;
    private JComboBox<String> choiceComboBox;

    /**
     * Get add content metadata array string [ ], with all SEDAMetadata
     * that can be inserted in an ArchiveUnit.
     *
     * @return the string [ ]
     */
    static public String[] getAddContentMetadataArray() {
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

    /**
     * Instantiates a new Xml ArchiveUnit editor panel.
     */
    public XMLArchiveUnitEditorPanel() {
        this.archiveUnit = null;
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{0.0, 1.0, 0.0};
        gbl.rowWeights = new double[]{0.0, 1.0, 0.0};
        setLayout(gbl);

        globalLabel = new JLabel(translateMetadataName("ArchiveUnit") + " - "+ translateMetadataName("Unknown"));
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
        gbc.anchor = GridBagConstraints.LINE_END;
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
                title = translateMetadataName("Unknown");
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
            result = MetadataEditor.createSEDAMetadataSample("ArchiveUnitProfile", "ArchiveUnitProfile", false);
        } else {
            HashMap<String, ComplexListMetadataKind> metadataMap;
            if (macroMetadata.equals("[C]")) {
                metadataMap = Content.metadataMap;
            } else {
                metadataMap = Management.metadataMap;
            }
            if (elementName.equals("AnyOtherMetadata"))
                result = MetadataEditor.createSEDAMetadataSample("AnyXMLType", elementName, false);
            else
                result = MetadataEditor.createSEDAMetadataSample(metadataMap.get(elementName).metadataClass.getSimpleName(), elementName, false);
        }
        return result;
    }

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

    @Override
    public void editArchiveUnit(ArchiveUnit archiveUnit) throws SEDALibException {
        this.archiveUnit = archiveUnit;
        if (archiveUnit == null) {
            editButton.setEnabled(false);
            addButton.setEnabled(false);
            choiceComboBox.setEnabled(false);
            xmlTextArea.setText("");
            globalLabel.setText(translateMetadataName("ArchiveUnit") + " - pas de sélection");
        } else {
            editButton.setEnabled(true);
            addButton.setEnabled(true);
            choiceComboBox.setEnabled(true);
            xmlTextArea.setText(archiveUnit.toSedaXmlFragments());
            globalLabel.setText(translateMetadataName("ArchiveUnit") + " - " + archiveUnit.getInDataObjectPackageId());
        }
    }

    @Override
    public ArchiveUnit extractArchiveUnit() throws SEDALibException {
        return archiveUnit;
    }
}
