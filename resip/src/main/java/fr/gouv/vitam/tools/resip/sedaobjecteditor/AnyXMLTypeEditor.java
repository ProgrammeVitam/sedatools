/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.XmlEditDialog;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorSimplePanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The AnyXMLType object editor class.
 */
public class AnyXMLTypeEditor extends SEDAObjectEditor {

    /**
     * The editedObject edition graphic component
     */
    private RSyntaxTextArea valueTextArea;

    /**
     * Instantiates a new AnyXMLType editor.
     *
     * @param metadata the AnyXMLType editedObject
     * @param father   the father
     * @throws SEDALibException if not a AnyXMLType editedObject
     */
    public AnyXMLTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof AnyXMLType)) throw new SEDALibException(
            "La métadonnée à éditer n'est pas du bon type"
        );
    }

    private AnyXMLType getAnyXMLTypeMetadata() {
        return (AnyXMLType) editedObject;
    }

    /**
     * Gets AnyXMLType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject sample
     * @throws SEDALibException the seda lib exception
     */
    public static SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        if (minimal) return new AnyXMLType("AnyXMLType", "");
        else return new AnyXMLType(
            "AnyXMLType",
            "<" + elementName + "><BlockTag1>Text1</BlockTag1><BlockTag2>Text2</BlockTag2></" + elementName + ">"
        );
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        getAnyXMLTypeMetadata().setRawXml(valueTextArea.getText());
        return getAnyXMLTypeMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        return valueTextArea.getText();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        JPanel labelPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[] { 1.0 };
        labelPanel.setLayout(gbl);

        JLabel label = new JLabel(translateTag("AnyXMLType") + " :");
        label.setToolTipText(getTag());
        label.setFont(SEDAObjectEditor.LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(label, gbc);

        JPanel editPanel = new JPanel();
        gbl = new GridBagLayout();
        gbl.rowHeights = new int[] { 100 };
        gbl.columnWeights = new double[] { 1.0 };
        editPanel.setLayout(gbl);

        valueTextArea = new RSyntaxTextArea(4, 80);
        valueTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        SyntaxScheme scheme = valueTextArea.getSyntaxScheme();
        scheme.getStyle(Token.MARKUP_TAG_DELIMITER).foreground = COMPOSITE_LABEL_MARKUP_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_NAME).foreground = COMPOSITE_LABEL_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE).foreground = COMPOSITE_LABEL_MARKUP_COLOR;
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE_VALUE).foreground = COMPOSITE_LABEL_ATTRIBUTE_COLOR;
        valueTextArea.setCodeFoldingEnabled(true);
        valueTextArea.setFont(SEDAObjectEditor.EDIT_FONT);
        JScrollPane scrollArea = new RTextScrollPane(valueTextArea);
        String xmlData;
        try {
            xmlData = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT).indentString(
                getAnyXMLTypeMetadata().getRawXml()
            );
        } catch (SEDALibException e) {
            xmlData = getAnyXMLTypeMetadata().getRawXml();
        }
        valueTextArea.setText(xmlData);
        valueTextArea.setCaretPosition(0);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(scrollArea, gbc);
        JButton editButton = new JButton();
        editButton.setIcon(new ImageIcon(getClass().getResource("/icon/text.png")));
        editButton.setToolTipText("Ouvrir pour édition...");
        editButton.setText("");
        editButton.setMaximumSize(new Dimension(16, 16));
        editButton.setMinimumSize(new Dimension(16, 16));
        editButton.setPreferredSize(new Dimension(16, 16));
        editButton.setBorderPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setFocusPainted(false);
        editButton.setFocusable(false);
        editButton.addActionListener(arg -> editButton());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.gridx = 1;
        gbc.gridy = 0;
        editPanel.add(editButton, gbc);

        this.sedaObjectEditorPanel = new SEDAObjectEditorSimplePanel(this, labelPanel, editPanel);
    }

    private void editButton() {
        XmlEditDialog xmlEditDialog = new XmlEditDialog(ResipGraphicApp.getTheWindow(), valueTextArea.getText());
        xmlEditDialog.setVisible(true);
        if (xmlEditDialog.getReturnValue()) valueTextArea.setText((String) xmlEditDialog.getResult());
    }
}
