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

import com.github.lgooddatepicker.components.DatePicker;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorSimplePanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.CompositeEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.RuleTypeEditor;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.NamedTypeMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.getAllJavaStackString;

/**
 * The Rule object editor class.
 */
public class RuleEditor extends SEDAObjectEditor {

    /**
     * The editedObject edition graphic component
     */
    private JTextField ruleIDTextField;
    private DatePicker startDatePicker;

    /**
     * Instantiates a new Rule editor.
     *
     * @param metadata the Rule editedObject
     * @param father   the father
     * @throws SEDALibException if not a Rule editedObject
     */
    public RuleEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof Rule))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private Rule getRuleMetadata() {
        return (Rule) editedObject;
    }

    /**
     * Gets Rule sample.
     *
     * @param elementName the element name, corresponding to the XML tag in SEDA
     * @param minimal     the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject sample
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
        return getRuleMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        LocalDate tmp=startDatePicker.getDate();
        return ruleIDTextField.getText() + (tmp == null ? "" : ", " + tmp);
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        JPanel editPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        editPanel.setLayout(gbl);

        ruleIDTextField = new JTextField();
        ruleIDTextField.setText(getRuleMetadata().getRuleID());
        ruleIDTextField.setCaretPosition(0);
        ruleIDTextField.setFont(SEDAObjectEditor.EDIT_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(ruleIDTextField, gbc);
        int height= SEDAObjectEditor.EDIT_FONT.getSize();
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

        JLabel label = new JLabel(translateTag("Rule")+" :");
        label.setToolTipText("Rule");
        label.setFont(SEDAObjectEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        labelPanel.add(label, gbc);

        JLabel dateLabel = new JLabel(translateTag("StartDate")+" :");
        dateLabel.setToolTipText("StartDate");
        dateLabel.setFont(SEDAObjectEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        labelPanel.add(dateLabel, gbc);

        this.sedaObjectEditorPanel = new RuleEditorPanel(this, labelPanel, editPanel);
    }

    private static class RuleEditorPanel extends SEDAObjectEditorSimplePanel {

        public RuleEditorPanel(RuleEditor ruleEditor,
            JPanel labelPanel, JPanel editPanel) throws SEDALibException {
            super(ruleEditor, labelPanel, editPanel);
        }

        @Override
        public void lessButton() {
            try {
                if (objectEditor.getFather() != null) {
                    List<SEDAObjectEditor> rulesEditorList =
                        ((RuleTypeEditor) objectEditor.getFather()).objectEditorList;
                    List<String> ruleMetadataKindList =
                        ((RuleType) objectEditor.getFather().getEditedObject()).getRuleMetadataKindList();

                    List<SEDAObjectEditor> itemsToDelete =
                        prepareEditorListToDelete(rulesEditorList, ruleMetadataKindList);

                    for (SEDAObjectEditor sedaObjectEditor : itemsToDelete) {
                        ((CompositeEditor) objectEditor.getFather()).removeChild(sedaObjectEditor);
                    }

                    super.lessButton();
                }
            } catch (SEDALibException ignored) {
            }
        }

        private List<SEDAObjectEditor> prepareEditorListToDelete(List<SEDAObjectEditor> rulesEditorList,
            List<String> ruleMetadataKindList) {
            List<SEDAObjectEditor> itemsToDelete = new ArrayList<>();
            int ruleIndexToDelete = retrieveRuleIndex();
            for (SEDAObjectEditor ruleObjectEditor : rulesEditorList
                .subList(ruleIndexToDelete + 1, rulesEditorList.size())) {
                if (ruleMetadataKindList
                    .contains(((NamedTypeMetadata) ruleObjectEditor.getEditedObject()).elementName)) {
                    itemsToDelete.add(ruleObjectEditor);
                } else {
                    break;
                }
            }
            return itemsToDelete;
        }

        private int retrieveRuleIndex() {
            int index = ((RuleTypeEditor) objectEditor.getFather()).objectEditorList.stream()
                .map(SEDAObjectEditor::getEditedObject).collect(Collectors.toList())
                .indexOf(objectEditor.getEditedObject());
            if(index == -1) {
                throw new IllegalStateException("Cannot find rule to delete index");
            }
            return index;
        }

        @Override
        public void addButton() {
            JPopupMenu popupMenu = new JPopupMenu();
            List<Pair<String, String>> extensionList;
            try {
                List<String> ruleMetadataKindList =
                    ((RuleType) objectEditor.getFather().getEditedObject()).getRuleMetadataKindList();
                extensionList = ruleMetadataKindList.stream().map(e -> new ImmutablePair<>(e, translateTag(e))).collect(
                    Collectors.toList());
            } catch (SEDALibException e) {
                extensionList = null;
            }

            if ((extensionList != null) && !extensionList.isEmpty()) {
                for (Pair<String,String> names : extensionList) {
                    JMenuItem mi = new JMenuItem(names.getValue());
                    mi.addActionListener((ev) -> {
                        if (objectEditor.getFather() != null) {
                            try {
                                ((RuleTypeEditor) objectEditor.getFather()).addChildTo(ev.getActionCommand(), (Rule) objectEditor.getEditedObject());
                                objectEditor.getFather().getSEDAObjectEditorPanelTopParent().validate();
                            } catch (SEDALibException e) {
                                System.err.println(getAllJavaStackString(e));
                            }
                        }
                    });
                    mi.setActionCommand(names.getKey());
                    popupMenu.add(mi);
                }
                popupMenu.show(this, this.getBounds().width / 2, this.getBounds().height);
            }
        }
    }
}
