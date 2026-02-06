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
import fr.gouv.vitam.tools.resip.frame.BigTextEditDialog;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorSimplePanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.AutomaticGrowingTextArea;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.TextType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The TextType object editor class.
 */
public class TextTypeEditor extends SEDAObjectEditor {

    /**
     * The editedObject attribute edition graphic component
     */
    private JTextField attributeTextField;

    /**
     * The editedObject edition graphic component
     */
    private JTextField valueTextField;
    private AutomaticGrowingTextArea valueTextArea;

    /**
     * The graphic elements
     */
    private JLabel beforeLabel,innerLabel;
    private JButton langButton;
    private GridBagLayout labelGBL;
    private int algorithmWidth;

    /**
     * Instantiates a new TextType editor.
     *
     * @param metadata the TextType editedObject
     * @param father   the father
     * @throws SEDALibException if not a TextType editedObject
     */
    public TextTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof TextType))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private TextType getTextTypeMetadata() {
        return (TextType) editedObject;
    }

    /**
     * Gets TextType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject sample
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        if (minimal)
            return new TextType(elementName, "");
        else
            return new TextType(elementName, "Text");
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        if (valueTextField!=null)
            getTextTypeMetadata().setValue(valueTextField.getText());
        else
            getTextTypeMetadata().setValue(valueTextArea.getText());
        String attr = attributeTextField.getText();
        if (attr.isEmpty()) attr = null;
        getTextTypeMetadata().setLang(attr);
        return getTextTypeMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        String tmp;
        String result="";
        if (valueTextField!=null)
            tmp= valueTextField.getText();
        else
            tmp= valueTextArea.getText();
        if ((attributeTextField.getText()!=null) && !attributeTextField.getText().isEmpty())
            result="("+ attributeTextField.getText()+")";
        return result+tmp;
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        JPanel labelPanel = new JPanel();
        GridBagLayout gbl;

        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
        algorithmWidth=(int) SEDAObjectEditor.LABEL_FONT.getStringBounds("ww",frc).getWidth();

        labelGBL= new GridBagLayout();
        labelGBL.columnWidths = new int[]{0, 0, 0};
        labelGBL.columnWeights = new double[]{1.0, 0.0, 0.0};
        labelPanel.setLayout(labelGBL);

        beforeLabel = new JLabel(getName() + (getTextTypeMetadata().getLang() == null ? "" : "("));
        beforeLabel.setToolTipText(getTag());
        beforeLabel.setFont(SEDAObjectEditor.LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(beforeLabel, gbc);

        langButton = new JButton("+lang");
        langButton.setMargin(new Insets(0, 0, 0, 0));
        langButton.setFont(SEDAObjectEditor.MINI_EDIT_FONT);
        langButton.setFocusable(false);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 1;
        gbc.gridy = 0;
        langButton.addActionListener(arg -> this.langActivate());
        labelPanel.add(langButton, gbc);

        attributeTextField = new JTextField();
        attributeTextField.setText(getTextTypeMetadata().getLang());
        attributeTextField.setFont(SEDAObjectEditor.MINI_EDIT_FONT);
        attributeTextField.setColumns(2);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 1;
        gbc.gridy = 0;
        labelPanel.add(attributeTextField, gbc);
        if (getTextTypeMetadata().getLang() == null) {
            langButton.setVisible(true);
            attributeTextField.setVisible(false);
            labelGBL.columnWidths = new int[]{0, 0, 0};
        }
        else{
            langButton.setVisible(false);
            attributeTextField.setVisible(true);
            labelGBL.columnWidths = new int[]{0,algorithmWidth, 0};
        }

        innerLabel = new JLabel((getTextTypeMetadata().getLang() == null ? ":" : ") :"));
        innerLabel.setToolTipText(translateTag("Lang attribute"));
        innerLabel.setFont(SEDAObjectEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 2;
        gbc.gridy = 0;
        labelPanel.add(innerLabel, gbc);

        JPanel editPanel= new JPanel();
        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0,0.0};
        gbl.rowWeights = new double[]{1.0};
        editPanel.setLayout(gbl);

        if (SEDAObjectEditorConstants.largeAreaTagList.contains(getTag())){
            valueTextArea = new AutomaticGrowingTextArea(6);
            valueTextArea.setText(getTextTypeMetadata().getValue());
            valueTextArea.setCaretPosition(0);
            valueTextArea.setFont(SEDAObjectEditor.EDIT_FONT);
            valueTextArea.setLineWrap(true);
            valueTextArea.setWrapStyleWord(true);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(valueTextArea.getScrollPane(), gbc);
            JButton editButton=new JButton();
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
        }
        else {
            valueTextField = new JTextField();
            valueTextField.setText(getTextTypeMetadata().getValue());
            valueTextField.setCaretPosition(0);
            valueTextField.setFont(SEDAObjectEditor.EDIT_FONT);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(valueTextField, gbc);
        }

        this.sedaObjectEditorPanel =new SEDAObjectEditorSimplePanel(this,labelPanel,editPanel);
    }

    private void langActivate()
    {
        langButton.setVisible(false);
        attributeTextField.setVisible(true);
        labelGBL.columnWidths = new int[]{0,algorithmWidth, 0};
        beforeLabel.setText(getName() + " (");
        innerLabel.setText(") :");
        attributeTextField.grabFocus();
    }

    private void editButton()
    {
        BigTextEditDialog bigTextEditDialog = new BigTextEditDialog(ResipGraphicApp.getTheWindow(), valueTextArea.getText(), getName());
        bigTextEditDialog.setVisible(true);
        if (bigTextEditDialog.getReturnValue())
            valueTextArea.setText(bigTextEditDialog.getResult());
    }
}
