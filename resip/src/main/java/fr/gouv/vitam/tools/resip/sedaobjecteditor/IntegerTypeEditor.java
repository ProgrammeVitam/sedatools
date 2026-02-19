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

import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorSimplePanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import java.awt.*;

/**
 * The IntegerType object editor class.
 */
public class IntegerTypeEditor extends SEDAObjectEditor {

    /**
     * The editedObject edition graphic component
     */
    private JTextField valueTextField;

    /**
     * Instantiates a new IntegerType editor.
     *
     * @param metadata the IntegerType editedObject
     * @param father   the father
     * @throws SEDALibException if not a IntegerType editedObject
     */
    public IntegerTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof IntegerType)) throw new SEDALibException(
            "La métadonnée à éditer n'est pas du bon type"
        );
    }

    private IntegerType getIntegerTypeMetadata() {
        return (IntegerType) editedObject;
    }

    /**
     * Gets TexType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject sample
     * @throws SEDALibException the seda lib exception
     */
    public static SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        if (minimal) return new IntegerType(elementName);
        else return new IntegerType(elementName, 42);
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        if (valueTextField.getText().isEmpty()) getIntegerTypeMetadata().setValue(null);
        else getIntegerTypeMetadata().setValue(Long.parseLong(valueTextField.getText()));
        return getIntegerTypeMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        return valueTextField.getText();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        JPanel labelPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[] { 1.0 };
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
        gbl.columnWidths = new int[] { 100, 0 };
        gbl.columnWeights = new double[] { 0.0, 1.0 };
        editPanel.setLayout(gbl);

        valueTextField = new JTextField();
        DocumentFilter filter = new IntegerFilter();
        ((AbstractDocument) valueTextField.getDocument()).setDocumentFilter(filter);
        valueTextField.setText(
            (getIntegerTypeMetadata().getValue() == null ? "" : Long.toString(getIntegerTypeMetadata().getValue()))
        );
        valueTextField.setFont(SEDAObjectEditor.EDIT_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(valueTextField, gbc);

        this.sedaObjectEditorPanel = new SEDAObjectEditorSimplePanel(this, labelPanel, editPanel);
    }

    /**
     * Set long value.
     *
     * @param value the value
     */
    public void setValue(long value) {
        valueTextField.setText(Long.toString(value));
        valueTextField.setCaretPosition(0);
    }
}
