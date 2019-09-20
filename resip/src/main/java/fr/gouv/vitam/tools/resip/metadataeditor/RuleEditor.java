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

import com.github.lgooddatepicker.components.DatePicker;
import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.MetadataEditorSimplePanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;

/**
 * The Rule metadata editor class.
 */
public class RuleEditor extends MetadataEditor {

    /**
     * The metadata edition graphic component
     */
    private JTextField ruleIDTextField;
    private DatePicker startDatePicker;

    /**
     * Instantiates a new Rule editor.
     *
     * @param metadata the Rule metadata
     * @param father   the father
     * @throws SEDALibException if not a Rule metadata
     */
    public RuleEditor(SEDAMetadata metadata, MetadataEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof Rule))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private Rule getRuleMetadata() {
        return (Rule) metadata;
    }

    /**
     * Gets Rule sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda metadata sample
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getSEDAMetadataSample(String elementName, boolean minimal) throws SEDALibException {
        if (minimal)
            return new Rule("", null);
        else
            return new Rule("Text", LocalDate.of(2000, 1, 1));
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        getRuleMetadata().setRuleID(ruleIDTextField.getText());
        getRuleMetadata().setStartDate(startDatePicker.getDate());
        return getSEDAMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        LocalDate tmp=startDatePicker.getDate();
        return ruleIDTextField.getText() + (tmp == null ? "" : ", " + tmp);
    }

    @Override
    public void createMetadataEditorPanel() throws SEDALibException {
        JPanel editPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        editPanel.setLayout(gbl);

        ruleIDTextField = new JTextField();
        ruleIDTextField.setText(getRuleMetadata().getRuleID());
        ruleIDTextField.setCaretPosition(0);
        ruleIDTextField.setFont(MetadataEditor.EDIT_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(ruleIDTextField, gbc);
        int height=MetadataEditor.EDIT_FONT.getSize();
        if (ruleIDTextField.getBorder()!=null)
            height+=ruleIDTextField.getBorder().getBorderInsets(ruleIDTextField).bottom+ruleIDTextField.getBorder().getBorderInsets(ruleIDTextField).top;

        startDatePicker = new DatePicker();
        startDatePicker.setDate(getRuleMetadata().getStartDate());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 1;
        editPanel.add(startDatePicker, gbc);

        JPanel labelPanel = new JPanel();
        gbl = new GridBagLayout();
        gbl.rowHeights = new int[]{height,height};
        gbl.columnWeights = new double[]{1.0};
        labelPanel.setLayout(gbl);

        JLabel label = new JLabel(translateMetadataName("Rule")+" :");
        label.setToolTipText("Rule");
        label.setFont(MetadataEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(label, gbc);

        JLabel dateLabel = new JLabel(translateMetadataName("StartDate")+" :");
        dateLabel.setToolTipText("StartDate");
        dateLabel.setFont(MetadataEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        labelPanel.add(dateLabel, gbc);

        this.metadataEditorPanel = new MetadataEditorSimplePanel(this, labelPanel, editPanel);
    }
}
