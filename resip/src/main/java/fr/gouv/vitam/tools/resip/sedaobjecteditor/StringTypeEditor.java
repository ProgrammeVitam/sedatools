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
package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.BigTextEditDialog;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.AutomaticGrowingTextArea;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorSimplePanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;

/**
 * The StringType object editor class.
 */
public class StringTypeEditor extends SEDAObjectEditor {

    /**
     * The editedObject edition graphic component
     */
    private JTextField valueTextField;
    private AutomaticGrowingTextArea valueTextArea;

    /**
     * Instantiates a new StringType editor.
     *
     * @param metadata the StringType editedObject
     * @param father   the father
     * @throws SEDALibException if not a StringType editedObject
     */
    public StringTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof StringType))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private StringType getStringTypeMetadata() {
        return (StringType) editedObject;
    }

    /**
     * Gets StringType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject sample
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        if (minimal)
            return new StringType(elementName, "");
        else
            return new StringType(elementName, "Text");
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        if (valueTextField!=null)
            getStringTypeMetadata().setValue(valueTextField.getText());
        else
            getStringTypeMetadata().setValue(valueTextArea.getText());
        return getStringTypeMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        if (valueTextField!=null)
            return valueTextField.getText();
        else
            return valueTextArea.getText();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        JPanel labelPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        labelPanel.setLayout(gbl);

        JLabel label = new JLabel(getName() + " :");
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
        gbl.columnWeights = new double[]{1.0};
        gbl.rowWeights = new double[]{1.0};
        editPanel.setLayout(gbl);

        if (SEDAObjectEditorConstants.largeAreaTagList.contains(getTag())){
            valueTextArea = new AutomaticGrowingTextArea(6);
            valueTextArea.setText(getStringTypeMetadata().getValue());
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
            valueTextField.setText(getStringTypeMetadata().getValue());
            valueTextField.setCaretPosition(0);
            valueTextField.setFont(SEDAObjectEditor.EDIT_FONT);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(valueTextField, gbc);
        }

        this.sedaObjectEditorPanel = new SEDAObjectEditorSimplePanel(this, labelPanel, editPanel);
    }

    private void editButton()
    {
        BigTextEditDialog bigTextEditDialog = new BigTextEditDialog(ResipGraphicApp.getTheWindow(), valueTextArea.getText(), getName());
        bigTextEditDialog.setVisible(true);
        if (bigTextEditDialog.getReturnValue())
            valueTextArea.setText(bigTextEditDialog.getResult());
    }

    /**
     * Set String value.
     *
     * @param value the value
     */
    public void setValue(String value){
        if (valueTextField!=null) {
            valueTextField.setText(value);
            valueTextField.setCaretPosition(0);
        }
        else {
            valueTextArea.setText(value);
            valueTextArea.setCaretPosition(0);
        }
    }
}
