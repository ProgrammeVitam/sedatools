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
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * The DigestType object editor class.
 */
public class DigestTypeEditor extends SEDAObjectEditor {

    /**
     * The editedObject attribute edition graphic component
     */
    private JTextField attributeTextField;

    /**
     * The editedObject edition graphic component
     */
    private JTextField valueTextField;

    /**
     * The graphic elements
     */
    private JLabel beforeLabel, innerLabel;
    private JButton algorithmButton;
    private GridBagLayout labelGBL;
    private int algorithmWidth;

    /**
     * Instantiates a new DigestType editor.
     *
     * @param metadata the DigestType editedObject
     * @param father   the father
     * @throws SEDALibException if not a DigestType editedObject
     */
    public DigestTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof DigestType))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private DigestType getDigestTypeMetadata() {
        return (DigestType) editedObject;
    }

    /**
     * Gets DigestType sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject sample
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        if (minimal)
            return new DigestType(elementName, "");
        else
            return new DigestType(elementName, "Hash", "SHA-512");
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        getDigestTypeMetadata().setValue(valueTextField.getText());
        String attr = attributeTextField.getText();
        if (attr.isEmpty()) attr = null;
        getDigestTypeMetadata().setAlgorithm(attr);
        return getDigestTypeMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        String result = "";
        if ((attributeTextField.getText() != null) && !attributeTextField.getText().isEmpty())
            result = "(" + attributeTextField.getText() + ")";
        return result + valueTextField.getText();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        JPanel labelPanel = new JPanel();
        GridBagLayout gbl;

        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
        algorithmWidth=(int) SEDAObjectEditor.LABEL_FONT.getStringBounds("SHA-512",frc).getWidth();

        labelGBL= new GridBagLayout();
        labelGBL.columnWidths = new int[]{0, 0, 0};
        labelGBL.columnWeights = new double[]{1.0, 0.0, 0.0};
        labelPanel.setLayout(labelGBL);

        beforeLabel = new JLabel(getName() + (getDigestTypeMetadata().getAlgorithm() == null ? "" : "("));
        beforeLabel.setToolTipText(getTag());
        beforeLabel.setFont(SEDAObjectEditor.LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(beforeLabel, gbc);

        algorithmButton = new JButton("+alg");
        algorithmButton.setMargin(new Insets(0, 0, 0, 0));
        algorithmButton.setFont(SEDAObjectEditor.MINI_EDIT_FONT);
        algorithmButton.setFocusable(false);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.gridx = 1;
        gbc.gridy = 0;
        algorithmButton.addActionListener(arg -> this.algorithmActivate());
        labelPanel.add(algorithmButton, gbc);

        attributeTextField = new JTextField(getDigestTypeMetadata().getAlgorithm(),5);
        attributeTextField.setFont(SEDAObjectEditor.MINI_EDIT_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 0;
        labelPanel.add(attributeTextField, gbc);
        if (getDigestTypeMetadata().getAlgorithm() == null) {
            algorithmButton.setVisible(true);
            attributeTextField.setVisible(false);
            labelGBL.columnWidths = new int[]{0, 0, 0};
        } else {
            algorithmButton.setVisible(false);
            attributeTextField.setVisible(true);
            labelGBL.columnWidths = new int[]{0,algorithmWidth, 0};
        }

        innerLabel = new JLabel((getDigestTypeMetadata().getAlgorithm() == null ? ":" : ") :"));
        innerLabel.setToolTipText(getTag());
        innerLabel.setFont(SEDAObjectEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 2;
        gbc.gridy = 0;
        labelPanel.add(innerLabel, gbc);

        JPanel editPanel = new JPanel();
        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        editPanel.setLayout(gbl);

        valueTextField = new JTextField();
        valueTextField.setText(getDigestTypeMetadata().getValue());
        valueTextField.setCaretPosition(0);
        valueTextField.setFont(SEDAObjectEditor.EDIT_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(valueTextField, gbc);

        this.sedaObjectEditorPanel = new SEDAObjectEditorSimplePanel(this, labelPanel, editPanel);
    }

    private void algorithmActivate() {
        algorithmButton.setVisible(false);
        attributeTextField.setVisible(true);
        beforeLabel.setText(getName() + "(");
        innerLabel.setText(") :");
        attributeTextField.grabFocus();
    }

    /**
     * Set hash and algorithm value.
     *
     * @param hash      the hash
     * @param algorithm the algorithm
     */
    public void setValue(String hash, String algorithm) {
        valueTextField.setText(hash);
        valueTextField.setCaretPosition(0);
        attributeTextField.setText(algorithm);
        if (algorithm == null) {
            algorithmButton.setVisible(true);
            attributeTextField.setVisible(false);
            labelGBL.columnWidths = new int[]{0,0, 0};
            beforeLabel.setText(getName());
            innerLabel.setText(" :");
        } else {
            algorithmButton.setVisible(false);
            attributeTextField.setVisible(true);
            labelGBL.columnWidths = new int[]{0,algorithmWidth, 0};
            beforeLabel.setText(getName() + "(");
            innerLabel.setText(") :");
        }
    }
}
