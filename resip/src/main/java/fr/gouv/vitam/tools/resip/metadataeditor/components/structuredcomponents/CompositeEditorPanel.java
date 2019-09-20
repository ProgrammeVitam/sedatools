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
package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants;
import fr.gouv.vitam.tools.resip.metadataeditor.composite.CompositeEditor;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;
import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The MetadataEditor panel class for composite metadata, containing elementary and composite metadata.
 * <p>
 * This is the heart of the imbricated representation.
 */
public class CompositeEditorPanel extends MetadataEditorPanel {

    /**
     * The Max index of MetadataEditor, equivalent to y-2 in gridbaglayout.
     */
    int maxIndex;

    /**
     * The MetadataEditor panel to GridBagConstraints in father map.
     */
    HashMap<MetadataEditorPanel, GridBagConstraints> metadataEditorPanelConstraints;

    /**
     * The graphic elements
     */
    private JTextField summary;
    private JSeparator separator;
    private JLabel metadataLabel;
    private JCheckBox arrowCheckBox;
    private ExtensionButton addMenu;

    /**
     * Instantiates a new CompositeEditor panel for a specified metadata.
     *
     * @param metadataEditor the metadata editor
     * @throws SEDALibException the seda lib exception
     */
    public CompositeEditorPanel(MetadataEditor metadataEditor) throws SEDALibException {
        this(metadataEditor, null, false);
    }

    /**
     * Instantiates a new Composite editor panel.
     *
     * @param metadataEditor    the metadata editor
     * @param moreMenuComponent the more menu component, to add behaviors in the metadata label bar
     * @param topPanelFlag      the top panel flag, which determine different adaptations as if true the impossibility
     *                          to close ou delete, and change label font
     * @throws SEDALibException the seda lib exception
     */
    public CompositeEditorPanel(MetadataEditor metadataEditor, JComponent moreMenuComponent, boolean topPanelFlag) throws SEDALibException {
        super(metadataEditor);
        this.metadataEditorPanelConstraints = new HashMap<MetadataEditorPanel, GridBagConstraints>();
        this.maxIndex = -1;
        GridBagLayout gbl;
        GridBagConstraints gbc;

        gbl = new GridBagLayout();
        if (topPanelFlag)
            gbl.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        else
            gbl.columnWidths = new int[]{16, MetadataEditorConstants.computeLabelWidth() - 41, 10, 10, 0, 0, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl.rowHeights = new int[]{20, 3};
        setLayout(gbl);

        setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        setScrollableHeight(ScrollablePanel.ScrollableSizeHint.NONE);

        if (!topPanelFlag) {
            arrowCheckBox = new JCheckBox();
            arrowCheckBox.setEnabled(true);
            arrowCheckBox.setIcon(new ImageIcon(getClass().getResource("/icon/arrow-opened-small.png")));
            arrowCheckBox.setSelectedIcon(new ImageIcon(getClass().getResource("/icon/arrow-closed-small.png")));
            arrowCheckBox.setText("");
            arrowCheckBox.setMargin(new Insets(0, 0, 0, 0));
            arrowCheckBox.setFocusable(false);
            arrowCheckBox.addItemListener(arg -> this.arrowEvent(arg));
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

        metadataLabel = new JLabel(translateMetadataName(metadataEditor.getName()) + " : ");
        metadataLabel.setToolTipText(metadataEditor.getName());
        if (topPanelFlag)
            metadataLabel.setFont(MetadataEditor.BOLD_LABEL_FONT);
        else {
            metadataLabel.setFont(MetadataEditor.ITALIC_LABEL_FONT);
            metadataLabel.setForeground(MetadataEditor.COMPOSITE_LABEL_COLOR);
        }
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(metadataLabel, gbc);

        if (!topPanelFlag) {
            JButton lessButton = new JButton();
            lessButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-remove-very-small.png")));
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

        if ((metadataEditor.getFather() != null) &&
                metadataEditor.getFather().canContainsMultiple(metadataEditor.getName())) {
            JButton addButton = new JButton();
            addButton.setIcon(new ImageIcon(getClass().getResource("/icon/list-add-very-small.png")));
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

        addMenu = new ExtensionButton(() -> {
            return ((CompositeEditor) metadataEditor).getExtensionList();
        }, (ActionEvent arg) -> {
            doExtend(metadataEditor, arg);
        });
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

        separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setBackground(MetadataEditor.GENERAL_BACKGROUND);
        separator.setForeground(MetadataEditor.COMPOSITE_LABEL_SEPARATOR_COLOR);
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
     * Add a child MetadataEditor panel before a specified place (begins at 0). All MetadataEditors with index equal
     * or bigger will be shifted.
     *
     * @param insertionIndex the insertion index
     * @param metadataPanel  the metadata panel
     */
    public void addMetadataEditorPanel(int insertionIndex, MetadataEditorPanel metadataPanel) {
        int gridy = insertionIndex + 1;
        if (insertionIndex <= maxIndex) {
            for (Map.Entry<MetadataEditorPanel, GridBagConstraints> e : metadataEditorPanelConstraints.entrySet()) {
                if (e.getValue().gridy > gridy) {
                    e.getValue().gridy++;
                    remove(e.getKey());
                    add(e.getKey(), e.getValue());
                }
            }
            maxIndex++;
        }
        else
            maxIndex=insertionIndex;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weighty = 1.0;
        gbc.gridx = 1;
        gbc.gridy = gridy + 1;
        gbc.gridwidth = 6;
        metadataPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0,
                MetadataEditor.COMPOSITE_LABEL_SEPARATOR_COLOR));
        add(metadataPanel, gbc);
        metadataEditorPanelConstraints.put(metadataPanel, gbc);
        doLayout();
    }

    /**
     * Remove a childs MetadataEditor panel.
     *
     * @param metadataEditorPanel the MetadataEditor panel
     * @throws SEDALibException the seda lib exception
     */
    public void removeMetadataEditorPanel(MetadataEditorPanel metadataEditorPanel) throws SEDALibException {
        if (metadataEditorPanelConstraints.get(metadataEditorPanel).gridy-2==maxIndex)
            maxIndex--;
        remove(metadataEditorPanel);
        metadataEditorPanelConstraints.remove(metadataEditorPanel);
    }

    /**
     * Refresh graphic load for empty or exiting content.
     *
     * @param emptyFlag the empty flag
     */
    public void refreshLoad(boolean emptyFlag) {
        addMenu.setVisible(!emptyFlag);
        metadataLabel.setText(metadataEditor.getName() + " ");
        this.revalidate();
        this.repaint();
    }

    /**
     * Set expanded state, forcing the action of expansion arrow.
     *
     * @param expandedFlag the expanded flag
     */
    public void setExpanded(boolean expandedFlag) {
        if (expandedFlag) {
            arrowCheckBox.setSelected(false);
        } else {
            arrowCheckBox.setSelected(true);
        }
    }

    private void arrowEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            summary.setVisible(true);
            addMenu.setVisible(false);
            try {
                summary.setText(metadataEditor.getSummary());
            } catch (SEDALibException e) {
                summary.setText("Extraction de la métadonnée impossible");
            }
            summary.setCaretPosition(0);
            for (MetadataEditorPanel mep : metadataEditorPanelConstraints.keySet())
                mep.setVisible(false);
            separator.setVisible(false);
            metadataLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            this.validate();
        } else if (event.getStateChange() == DESELECTED) {
            summary.setVisible(false);
            addMenu.setVisible(true);
            for (MetadataEditorPanel mep : metadataEditorPanelConstraints.keySet())
                mep.setVisible(true);
            separator.setVisible(true);
            metadataLabel.setHorizontalAlignment(SwingConstants.LEADING);
            this.validate();
        }
    }

    /**
     * Do extend, adding a MetadataEditor.
     *
     * @param metadataEditor the metadata editor
     * @param event          the event
     */
    public static void doExtend(MetadataEditor metadataEditor, ActionEvent event) {
        try {
            ((CompositeEditor) metadataEditor).addChild(event.getActionCommand());
            metadataEditor.getMetadataEditorPanelTopParent().revalidate();
            metadataEditor.getMetadataEditorPanelTopParent().repaint();
        } catch (SEDALibException ignored) {
        }
    }
}
