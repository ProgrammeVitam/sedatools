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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.CompositeEditor;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Iterator;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The SEDAObjectEditor panel class for composite editedObject, containing elementary and composite editedObject.
 * <p>
 * This is the heart of the imbricated representation.
 */
public class SEDAObjectEditorCompositePanel extends SEDAObjectEditorPanel {

    /**
     * The Max index of SEDAObjectEditor, equivalent to y-2 in gridbaglayout.
     */
    protected int maxIndex;

    /**
     * The SEDAObjectEditor panel to GridBagConstraints in father map.
     */
    public HashMap<SEDAObjectEditorPanel, GridBagConstraints> objectEditorPanelGridBagConstraintsHashMap;

    /**
     * The graphic elements
     */
    private JTextField summary;
    private JSeparator separator;
    private JLabel editedObjectLabel;
    private JCheckBox arrowCheckBox;
    private ExtensionButton addMenu;

    /**
     * Instantiates a new CompositeEditor panel for a specified editedObject.
     *
     * @param objectEditor the SEDA object editor
     * @throws SEDALibException the seda lib exception
     */
    public SEDAObjectEditorCompositePanel(SEDAObjectEditor objectEditor) throws SEDALibException {
        this(objectEditor, null, false);
    }

    /**
     * Instantiates a new Composite editor panel.
     *
     * @param objectEditor      the SEDA object editor
     * @param moreMenuComponent the more menu component, to add behaviors in the editedObject label bar
     * @param topPanelFlag      the top panel flag, which determine different adaptations as if true the impossibility
     *                          to close ou delete, and change label font
     * @throws SEDALibException the seda lib exception
     */
    public SEDAObjectEditorCompositePanel(
        SEDAObjectEditor objectEditor,
        JComponent moreMenuComponent,
        boolean topPanelFlag
    ) throws SEDALibException {
        super(objectEditor);
        this.objectEditorPanelGridBagConstraintsHashMap = new HashMap<>();
        this.maxIndex = -1;
        GridBagLayout gbl;
        GridBagConstraints gbc;

        gbl = new GridBagLayout();
        if (topPanelFlag) gbl.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
        else gbl.columnWidths = new int[] { 16, SEDAObjectEditorConstants.computeLabelWidth() - 41, 10, 10, 0, 0, 0 };
        gbl.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
        gbl.rowHeights = new int[] { 20, 3 };
        setLayout(gbl);

        setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        setScrollableHeight(ScrollablePanel.ScrollableSizeHint.NONE);

        if (!topPanelFlag) {
            arrowCheckBox = new JCheckBox();
            arrowCheckBox.setEnabled(true);
            arrowCheckBox.setIcon(new ImageIcon(getClass().getResource("/icon/arrow-opened-small.png")));
            arrowCheckBox.setSelectedIcon(new ImageIcon(getClass().getResource("/icon/arrow-closed-small.png")));
            arrowCheckBox.setToolTipText("Bascule entre Version éditable et Version résumé");
            arrowCheckBox.setText("");
            arrowCheckBox.setMargin(new Insets(0, 0, 0, 0));
            arrowCheckBox.setFocusable(false);
            arrowCheckBox.addItemListener(this::arrowEvent);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(arrowCheckBox, gbc);
        } else {
            JLabel placeHolder = new JLabel("  ");
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(placeHolder, gbc);
        }

        editedObjectLabel = new JLabel(objectEditor.getName() + " : ");
        editedObjectLabel.setToolTipText(objectEditor.getTag());
        if (topPanelFlag) editedObjectLabel.setFont(SEDAObjectEditor.BOLD_LABEL_FONT);
        else {
            editedObjectLabel.setFont(SEDAObjectEditor.ITALIC_LABEL_FONT);
            editedObjectLabel.setForeground(SEDAObjectEditor.COMPOSITE_LABEL_COLOR);
        }
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(editedObjectLabel, gbc);

        if (!topPanelFlag) {
            JButton lessButton = new JButton();
            lessButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-remove-very-small.png")));
            lessButton.setToolTipText("Supprimer cet élément...");
            lessButton.setText("");
            lessButton.setMaximumSize(new Dimension(8, 8));
            lessButton.setMinimumSize(new Dimension(8, 8));
            lessButton.setPreferredSize(new Dimension(8, 8));
            lessButton.setBorderPainted(false);
            lessButton.setContentAreaFilled(false);
            lessButton.setFocusPainted(false);
            lessButton.setFocusable(false);
            lessButton.addActionListener(arg -> lessButton());
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 2;
            gbc.gridy = 0;
            add(lessButton, gbc);
        }

        if ((objectEditor.getFather() != null) && objectEditor.getFather().canContainsMultiple(objectEditor.getTag())) {
            JButton addButton = new JButton();
            addButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-add-very-small.png")));
            addButton.setToolTipText("Ajouter un élément de même type...");
            addButton.setText("");
            addButton.setMaximumSize(new Dimension(8, 8));
            addButton.setMinimumSize(new Dimension(8, 8));
            addButton.setPreferredSize(new Dimension(8, 8));
            addButton.setBorderPainted(false);
            addButton.setContentAreaFilled(false);
            addButton.setFocusPainted(false);
            addButton.setFocusable(false);
            addButton.addActionListener(arg -> addButton());
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 3;
            gbc.gridy = 0;
            add(addButton, gbc);
        }

        addMenu = new ExtensionButton(
            () -> {
                return ((CompositeEditor) objectEditor).getExtensionList();
            },
            (ActionEvent arg) -> doExtend(objectEditor, arg)
        );
        addMenu.setToolTipText("Ajouter un élément dans la liste du menu déroulant...");
        addMenu.setMargin(new Insets(0, 0, 0, 0));
        addMenu.setBorderPainted(false);
        addMenu.setContentAreaFilled(false);
        addMenu.setFocusPainted(false);
        addMenu.setFocusable(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx = 4;
        gbc.gridy = 0;
        add(addMenu, gbc);

        if (moreMenuComponent != null) {
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.insets = new Insets(0, 0, 0, 5);
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.gridx = 5;
            gbc.gridy = 0;
            add(moreMenuComponent, gbc);
        }

        summary = new JTextField();
        summary.setEditable(false);
        summary.setVisible(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 6;
        gbc.gridy = 0;
        add(summary, gbc);

        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setBackground(SEDAObjectEditor.GENERAL_BACKGROUND);
        separator.setForeground(SEDAObjectEditor.COMPOSITE_LABEL_SEPARATOR_COLOR);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 7;
        add(separator, gbc);
    }

    /**
     * Synchronize panels with the metadataeditor content.
     *
     * @throws SEDALibException the seda lib exception
     */
    public void synchronizePanels() throws SEDALibException {
        int i = -1;
        GridBagConstraints gbc;

        for (SEDAObjectEditor oe : ((CompositeEditor) objectEditor).objectEditorList) {
            i++;
            gbc = objectEditorPanelGridBagConstraintsHashMap.get(oe.getSEDAObjectEditorPanel());
            if (gbc == null) {
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.LINE_START;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0, 0, 0, 0);
                gbc.weightx = 1.0;
                gbc.weighty = 1.0;
                gbc.gridx = 1;
                gbc.gridy = i + 2;
                gbc.gridwidth = 6;
                oe
                    .getSEDAObjectEditorPanel()
                    .setBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, SEDAObjectEditor.COMPOSITE_LABEL_SEPARATOR_COLOR)
                    );
                add(oe.getSEDAObjectEditorPanel(), gbc);
                objectEditorPanelGridBagConstraintsHashMap.put(oe.getSEDAObjectEditorPanel(), gbc);
                continue;
            }
            if (gbc.gridy == i + 2) continue;
            remove(oe.getSEDAObjectEditorPanel());
            gbc.gridy = i + 2;
            add(oe.getSEDAObjectEditorPanel(), gbc);
        }

        Iterator<SEDAObjectEditorPanel> it = objectEditorPanelGridBagConstraintsHashMap.keySet().iterator();
        while (it.hasNext()) {
            SEDAObjectEditorPanel oep = it.next();
            if (!(((CompositeEditor) objectEditor).objectEditorList).contains(oep.objectEditor)) {
                remove(oep);
                it.remove();
            }
        }
    }

    /**
     * Refresh graphic load for empty or exiting content.
     *
     * @param emptyFlag the empty flag
     */
    public void refreshLoad(boolean emptyFlag) {
        addMenu.setVisible(!emptyFlag);
        editedObjectLabel.setText(objectEditor.getName() + " ");
        this.revalidate();
        this.repaint();
    }

    /**
     * Refresh editedObject label.
     */
    public void refreshEditedObjectLabel() {
        editedObjectLabel.setText(objectEditor.getName() + " : ");
    }

    /**
     * Set expanded state, forcing the action of expansion arrow.
     *
     * @param expandedFlag the expanded flag
     */
    public void setExpanded(boolean expandedFlag) {
        arrowCheckBox.setSelected(!expandedFlag);
    }

    /**
     * Get expanded state.
     *
     * @return the expanded state
     */
    public boolean isExpanded() {
        return !arrowCheckBox.isSelected();
    }

    private void arrowEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            summary.setVisible(true);
            addMenu.setVisible(false);
            try {
                summary.setText(objectEditor.getSummary());
            } catch (SEDALibException e) {
                summary.setText("Extraction de la métadonnée impossible");
            }
            summary.setCaretPosition(0);
            for (SEDAObjectEditorPanel mep : objectEditorPanelGridBagConstraintsHashMap.keySet()) mep.setVisible(false);
            separator.setVisible(false);
            editedObjectLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            this.validate();
        } else if (event.getStateChange() == DESELECTED) {
            if (
                ((CompositeEditor) objectEditor).hasSubeditorsCreatedWhenExpandedFlag()
            ) ((CompositeEditor) objectEditor).createSubEditors();
            summary.setVisible(false);
            addMenu.setVisible(true);
            for (SEDAObjectEditorPanel mep : objectEditorPanelGridBagConstraintsHashMap.keySet()) mep.setVisible(true);
            separator.setVisible(true);
            editedObjectLabel.setHorizontalAlignment(SwingConstants.LEADING);
            this.validate();
        }
    }

    /**
     * Do extend, adding an SEDAObjectEditor.
     *
     * @param objectEditor the SEDA object editor
     * @param event        the event
     */
    public static void doExtend(SEDAObjectEditor objectEditor, ActionEvent event) {
        try {
            ((CompositeEditor) objectEditor).addChild(event.getActionCommand());
            objectEditor.getSEDAObjectEditorPanelTopParent().revalidate();
            objectEditor.getSEDAObjectEditorPanelTopParent().repaint();
        } catch (SEDALibException ignored) {
            // no real case
        }
    }
}
