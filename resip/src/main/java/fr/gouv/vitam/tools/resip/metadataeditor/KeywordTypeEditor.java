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
import fr.gouv.vitam.tools.sedalib.metadata.content.KeywordType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;

/**
 * The KeywordType metadata editor class.
 */
public class KeywordTypeEditor extends MetadataEditor{

    /**
     * The metadata edition graphic component
     */
    private JComboBox<String> valueComboBox;

    /**
     * Instantiates a new KeywordType editor.
     *
     * @param metadata the KeywordType metadata
     * @param father   the father
     * @throws SEDALibException if not a KeywordType metadata
     */
    public KeywordTypeEditor(SEDAMetadata metadata, MetadataEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof KeywordType))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private KeywordType getKeywordTypeMetadata() {
        return (KeywordType) metadata;
    }

    /**
     * Gets KeywordType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda metadata sample
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        return new KeywordType("subject");
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException{
        getKeywordTypeMetadata().setValue((String)(valueComboBox.getSelectedItem()));
        return getSEDAMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        return (String)(valueComboBox.getSelectedItem());
    }

    @Override
    public void createMetadataEditorPanel() throws SEDALibException {
        JPanel labelPanel= new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        labelPanel.setLayout(gbl);

        JLabel label = new JLabel(translateMetadataName(getName())+" :");
        label.setToolTipText(getName());
        label.setFont(MetadataEditor.LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(label, gbc);

        JPanel editPanel= new JPanel();
        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        editPanel.setLayout(gbl);

        valueComboBox=new JComboBox<String>((String[])KeywordType.enumValues.toArray());
        valueComboBox.setEditable(true);
        valueComboBox.getEditor().getEditorComponent().setFocusable(false);
        valueComboBox.setFont(MetadataEditor.EDIT_FONT);
        valueComboBox.setSelectedItem(getKeywordTypeMetadata().getValue());
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(valueComboBox, gbc);

        this.metadataEditorPanel=new MetadataEditorSimplePanel(this,labelPanel,editPanel);
    }
}
