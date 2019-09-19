package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.metadataeditor.composite.CompositeEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

public class CompositeEditorPanel extends MetadataEditorPanel {
    int metadataPanelCount;

    JTextField summary;
    JSeparator separator;
    JLabel metadataLabel;
    JCheckBox arrowCheckBox;
    ExtensionButton addMenu;

    HashMap<MetadataEditorPanel, GridBagConstraints> metadataEditorPanelConstraints;

    public CompositeEditorPanel(MetadataEditor metadataEditor) throws SEDALibException {
        this(metadataEditor, null);
    }

    public CompositeEditorPanel(MetadataEditor metadataEditor, JComponent moreMenuComponent) throws SEDALibException {
        super(metadataEditor);
        final CompositeEditorPanel thisPanel = this;
        this.metadataPanelCount = 2;
        this.metadataEditorPanelConstraints = new HashMap<MetadataEditorPanel, GridBagConstraints>();
        GridBagLayout gbl = new GridBagLayout();
        if (metadataEditor == null)
            gbl.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
        else
            gbl.columnWidths = new int[]{16, MetadataEditorConstants.computeLabelWidth() - 41, 10, 10, 0, 0, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        setLayout(gbl);

        if (metadataEditor != null) {
            arrowCheckBox = new JCheckBox();
            arrowCheckBox.setEnabled(true);
            arrowCheckBox.setIcon(new ImageIcon(getClass().getResource("/icon/arrow-opened-small.png")));
            arrowCheckBox.setSelectedIcon(new ImageIcon(getClass().getResource("/icon/arrow-closed-small.png")));
            arrowCheckBox.setText("");
            arrowCheckBox.setMargin(new Insets(0,0,0,0));
            arrowCheckBox.setFocusable(false);
            arrowCheckBox.addItemListener(arg -> this.arrowEvent(arg));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(arrowCheckBox, gbc);
            metadataLabel = new JLabel(MetadataEditor.translate(metadataEditor.getName()) + " :");
            metadataLabel.setToolTipText(metadataEditor.getName());
            metadataLabel.setFont(MetadataEditor.ITALIC_LABEL_FONT);
            metadataLabel.setForeground(MetadataEditor.COMPOSITE_LABEL_COLOR);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 1;
            gbc.gridy = 0;
            add(metadataLabel, gbc);

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

            if ((metadataEditor.getFather()!=null ) &&
                    metadataEditor.getFather().containsMultiple(metadataEditor.getName())) {
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

            if (moreMenuComponent!=null){
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
            separator.setBackground(MetadataEditor.COMPOSITE_LABEL_SEPARATOR_COLOR);
            separator.setForeground(MetadataEditor.GENERAL_BACKGROUND);
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 7;
            add(separator, gbc);
        }
    }

    public void addMetadataEditorPanel(MetadataEditorPanel metadataPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = metadataPanelCount++;
        gbc.gridwidth = 6;
        metadataPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0,
                MetadataEditor.COMPOSITE_LABEL_SEPARATOR_COLOR));
        add(metadataPanel, gbc);
        metadataEditorPanelConstraints.put(metadataPanel, gbc);
    }

    public void addMetadataEditorPanel(int insertionIndex,MetadataEditorPanel metadataPanel) {
        int gridy = insertionIndex+1;
        for (Map.Entry<MetadataEditorPanel, GridBagConstraints> e : metadataEditorPanelConstraints.entrySet()) {
            if (e.getValue().gridy > gridy) {
                e.getValue().gridy++;
                remove(e.getKey());
                add(e.getKey(), e.getValue());
            }
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = gridy + 1;
        gbc.gridwidth = 5;
        metadataPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0,
                MetadataEditor.COMPOSITE_LABEL_SEPARATOR_COLOR));
        add(metadataPanel, gbc);
        metadataEditorPanelConstraints.put(metadataPanel, gbc);
        doLayout();
    }

    public void removeMetadataEditorPanel(MetadataEditorPanel mep) throws SEDALibException {
        remove(mep);
        metadataEditorPanelConstraints.remove(mep);
    }

    public void setExtended(boolean extended) {
        if (extended) {
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

    public static void doExtend(MetadataEditor metadataEditor, ActionEvent event) {
        try {
            ((CompositeEditor) metadataEditor).addChild(event.getActionCommand());
            metadataEditor.getMetadataEditorPanelTopParent().revalidate();
            metadataEditor.getMetadataEditorPanelTopParent().repaint();
        } catch (SEDALibException ignored) {
        }
    }
}
