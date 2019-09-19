package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor.translate;

public class InnerStructuredArchiveUnitEditorPanel extends ScrollablePanel {
    ArchiveUnit archiveUnit;

    JLabel globalLabel;
    ExtensionButton globalExtensionButton;
    HashMap<MetadataEditorPanel, GridBagConstraints> metadataEditorPanelConstraints;

    public InnerStructuredArchiveUnitEditorPanel() {
        super();
        this.metadataEditorPanelConstraints = new HashMap<MetadataEditorPanel, GridBagConstraints>();
        this.archiveUnit = null;

        // define the ScrollablePanel context avoiding horizontal expansion
        GridBagLayout gbl = new GridBagLayout();
        gbl.columnWidths = new int[]{10, MetadataEditorConstants.computeLabelWidth() - 25, 0, 0};
        gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0};
        gbl.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
        gbl.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        setLayout(gbl);
        setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        setScrollableHeight(ScrollablePanel.ScrollableSizeHint.NONE);

        globalLabel = new JLabel(translate("ArchiveUnit") + " - pas de sélection");
        globalLabel.setFont(MetadataEditor.BOLD_LABEL_FONT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(globalLabel, gbc);

        globalExtensionButton = new ExtensionButton(() -> {
            return getExtensionList(metadataEditorPanelConstraints);
        }, (arg) -> {
            doExtend(archiveUnit, arg);
        });
        globalExtensionButton.setMargin(new Insets(0, 0, 0, 0));
        globalExtensionButton.setBorderPainted(false);
        globalExtensionButton.setContentAreaFilled(false);
        globalExtensionButton.setFocusPainted(false);
        globalExtensionButton.setFocusable(false);
        globalExtensionButton.setVisible(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 2;
        gbc.gridy = 0;
        add(globalExtensionButton, gbc);

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        add(separator, gbc);
    }

    private void addMetadataEditorPanel(MetadataEditorPanel metadataPanel, int rank) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        gbc.gridy = rank + 2;
        gbc.gridwidth = 5;
        metadataPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
        add(metadataPanel, gbc);
        metadataEditorPanelConstraints.put(metadataPanel, gbc);
    }

    public void removeChild(MetadataEditor metadataEditor) throws SEDALibException {
        remove(metadataEditor.getMetadataEditorPanel());
        metadataEditorPanelConstraints.remove(metadataEditor.getMetadataEditorPanel());
        this.revalidate();
        this.repaint();
    }

    public ArchiveUnit getArchivUnit() {
        return archiveUnit;
    }

    public void editArchiveUnit(ArchiveUnit archiveUnit) throws SEDALibException {
        for (MetadataEditorPanel mep : metadataEditorPanelConstraints.keySet()) {
            remove(mep);
        }
        this.metadataEditorPanelConstraints = new HashMap<MetadataEditorPanel, GridBagConstraints>();
        this.archiveUnit = archiveUnit;
        if (archiveUnit != null) {
            if (archiveUnit.getContent() != null) {
                MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(archiveUnit.getContent(), null);
                addMetadataEditorPanel(metadataEditor.getMetadataEditorPanel(), 0);
            }
            if (archiveUnit.getManagement() != null) {
                MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(archiveUnit.getManagement(), null);
                addMetadataEditorPanel(metadataEditor.getMetadataEditorPanel(), 1);
            }
            if (archiveUnit.getArchiveUnitProfile() != null) {
                MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(archiveUnit.getArchiveUnitProfile(), null);
                addMetadataEditorPanel(metadataEditor.getMetadataEditorPanel(), 2);
            }
            globalExtensionButton.setVisible(true);
            globalLabel.setText(translate("ArchiveUnit") + " - " + archiveUnit.getInDataObjectPackageId());
        }
        else {
            globalExtensionButton.setVisible(false);
            globalLabel.setText(translate("ArchiveUnit") + " - pas de sélection");
        }

        this.revalidate();
        this.repaint();
    }

    public ArchiveUnit extractArchiveUnit() throws SEDALibException {
        for (MetadataEditorPanel mep : metadataEditorPanelConstraints.keySet()) {
            SEDAMetadata sedaMetadata = mep.metadataEditor.extractMetadata();
            switch (sedaMetadata.getXmlElementName()) {
                case "ArchiveUnitProfile":
                    archiveUnit.setArchiveUnitProfile((ArchiveUnitProfile) sedaMetadata);
                    break;
                case "Content":
                    archiveUnit.setContent((Content) sedaMetadata);
                    break;
                case "Management":
                    archiveUnit.setManagement((Management) sedaMetadata);
                    break;
            }
        }
        return archiveUnit;
    }

    private List<Pair<String, String>> getExtensionList(HashMap<MetadataEditorPanel, GridBagConstraints> metadataEditorPanelConstraints) {
        if (archiveUnit == null)
            return new ArrayList<Pair<String, String>>();

        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(Arrays.asList(
                Pair.of("ArchiveUnitProfile", translate("ArchiveUnitProfile")),
                Pair.of("Content", translate("Content")),
                Pair.of("Management", translate("Management"))));

        for (MetadataEditorPanel mep : metadataEditorPanelConstraints.keySet()) {
            String name=mep.metadataEditor.getName();
            extensionList.remove(Pair.of(name,translate(name)));
        }
        return extensionList;
    }

    private void doExtend(ArchiveUnit archiveUnit, ActionEvent event) {
        SEDAMetadata sedaMetadata = null;
        try {
            sedaMetadata = MetadataEditor.createMetadataSample(event.getActionCommand(), event.getActionCommand(), true);
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(sedaMetadata, null);
            switch (event.getActionCommand()) {
                case "Content":
                    addMetadataEditorPanel(metadataEditor.getMetadataEditorPanel(), 0);
                    break;
                case "Management":
                    addMetadataEditorPanel(metadataEditor.getMetadataEditorPanel(), 1);
                    break;
                case "ArchiveUnitProfile":
                    addMetadataEditorPanel(metadataEditor.getMetadataEditorPanel(), 2);
                    break;
            }
            getParent().validate();
        } catch (SEDALibException ignored) {
        }
    }
}
