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
package fr.gouv.vitam.tools.resip.metadataeditor;

import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.MetadataEditorSimplePanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.TextType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;

/**
 * The TextType metadata editor class.
 */
public class TextTypeEditor extends MetadataEditor {

    /**
     * The metadata attribute edition graphic component
     */
    private JTextField attributeTextField;

    /**
     * The metadata edition graphic component
     */
    private JTextField valueTextField;
    private JTextArea valueTextArea;

    /**
     * The graphic elements
     */
    private JLabel beforeLabel,innerLabel;
    private JButton langButton;

    /**
     * Instantiates a new TextType editor.
     *
     * @param metadata the TextType metadata
     * @param father   the father
     * @throws SEDALibException if not a TextType metadata
     */
    public TextTypeEditor(SEDAMetadata metadata, MetadataEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof TextType))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private TextType getTextTypeMetadata() {
        return (TextType) metadata;
    }

    /**
     * Gets TextType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda metadata sample
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
        return getSEDAMetadata();
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
    public void createMetadataEditorPanel() throws SEDALibException {
        JPanel labelPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0, 0.0, 0.0};
        labelPanel.setLayout(gbl);

        beforeLabel = new JLabel(translateMetadataName(getName()) + (getTextTypeMetadata().getLang() == null ? "" : "("));
        beforeLabel.setToolTipText(getName());
        beforeLabel.setFont(MetadataEditor.LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(beforeLabel, gbc);

        langButton = new JButton("+lang");
        langButton.setMargin(new Insets(0, 0, 0, 0));
        langButton.setFont(MetadataEditor.MINI_EDIT_FONT);
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
        attributeTextField.setFont(MetadataEditor.MINI_EDIT_FONT);
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
        }
        else{
            langButton.setVisible(false);
            attributeTextField.setVisible(true);
        }

        innerLabel = new JLabel((getTextTypeMetadata().getLang() == null ? ":" : ") :"));
        innerLabel.setToolTipText(translateMetadataName("Lang attribute"));
        innerLabel.setFont(MetadataEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 2;
        gbc.gridy = 0;
        labelPanel.add(innerLabel, gbc);

        JPanel editPanel= new JPanel();
        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        editPanel.setLayout(gbl);

        if (MetadataEditorConstants.largeAreaTagList.contains(getName())){
            gbl.rowHeights = new int[]{100};
            valueTextArea = new JTextArea();
            valueTextArea.setText(getTextTypeMetadata().getValue());
            valueTextArea.setCaretPosition(0);
            valueTextArea.setFont(MetadataEditor.EDIT_FONT);
            valueTextArea.setRows(6);
            valueTextArea.setLineWrap(true);
            valueTextArea.setWrapStyleWord(true);
            JScrollPane scrollArea=new JScrollPane(valueTextArea);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(scrollArea, gbc);

        }
        else {
            valueTextField = new JTextField();
            valueTextField.setText(getTextTypeMetadata().getValue());
            valueTextField.setCaretPosition(0);
            valueTextField.setFont(MetadataEditor.EDIT_FONT);
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            editPanel.add(valueTextField, gbc);
        }

        this.metadataEditorPanel=new MetadataEditorSimplePanel(this,labelPanel,editPanel);
    }

    private void langActivate()
    {
        langButton.setVisible(false);
        attributeTextField.setVisible(true);
        beforeLabel.setText(translateMetadataName(getName()) + " (");
        innerLabel.setText(") :");
        attributeTextField.grabFocus();
    }
}
