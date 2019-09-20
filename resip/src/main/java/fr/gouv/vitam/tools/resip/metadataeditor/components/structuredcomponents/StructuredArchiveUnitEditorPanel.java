package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.components.ArchiveUnitEditorPanel;
import fr.gouv.vitam.tools.resip.metadataeditor.composite.ArchiveUnitEditor;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor.translate;

public class StructuredArchiveUnitEditorPanel extends JPanel implements ArchiveUnitEditorPanel {

    ArchiveUnitEditor archiveUnitEditor;
    JButton revertButton, saveButton;
    JScrollPane scrollPane;
    JPanel warningPane;
    JTextArea warningText;

    public StructuredArchiveUnitEditorPanel() {
        super();
        GridBagLayout gbl;
        GridBagConstraints gbc;

        this.archiveUnitEditor = new ArchiveUnitEditor(null, null);

        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0, 1.0, 1.0};
        gbl.rowHeights = new int[]{0, 0};
        gbl.rowWeights = new double[]{1.0, 0.0};
        setLayout(gbl);

        MetadataEditorPanel mep = null;
        try {
            mep = archiveUnitEditor.getMetadataEditorPanel();
        } catch (SEDALibException ignored) {
        }

        scrollPane = new JScrollPane(mep);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(scrollPane, gbc);

        gbl = new GridBagLayout();
        warningPane = new JPanel(gbl);
        warningPane.setVisible(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(warningPane, gbc);

        JLabel warningLabel = new JLabel("", new ImageIcon(getClass().getResource("/icon/large-dialog-warning.png")), SwingConstants.CENTER);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        warningPane.add(warningLabel, gbc);

        warningText = new JTextArea("Problème d'analyse");
        warningText.setBackground(MetadataEditor.GENERAL_BACKGROUND);
        warningText.setFont(MetadataEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 0;
        warningPane.add(warningText, gbc);

        revertButton = new JButton("Recharger");
        revertButton.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        revertButton.addActionListener(arg -> {
            revertButton(arg);
        });
        add(revertButton, gbc);

        saveButton = new JButton("Sauver");
        saveButton.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 1;
        saveButton.addActionListener(arg -> {
            saveButton(arg);
        });
        add(saveButton, gbc);
    }

    private void revertButton(ActionEvent event) {
        try {
            editArchiveUnit((ArchiveUnit)archiveUnitEditor.getEditedObject());
        } catch (SEDALibException ignored) {
        }
    }

    private void saveButton(ActionEvent event) {
        try {
            ArchiveUnit archiveUnit = archiveUnitEditor.extractEditedObject();
            ((DataObjectPackageTreeModel) ResipGraphicApp.getTheWindow().getDataObjectPackageTreePaneViewer().getModel())
                    .nodeChanged(ResipGraphicApp.getTheWindow().dataObjectPackageTreeItemDisplayed);
            String title = null;
            try {
                title = archiveUnit.getContent().getSimpleMetadata("Title");
                if (title == null)
                    title = SEDAXMLEventReader.extractNamedElement("Title", archiveUnit.toSedaXmlFragments());
                if (title == null)
                    title = "Inconnu";
            } catch (SEDALibException e) {
                title = "Inconnu";
            }
            ResipGraphicApp.getTheWindow().dataObjectPackageTreeItemDisplayed.setTitle(title);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        } catch (SEDALibException ignored) {
        }
    }

    public void editArchiveUnit(ArchiveUnit archiveUnit) throws SEDALibException {
        if (archiveUnit == null) {
            archiveUnitEditor.editArchiveUnit(archiveUnit);
            scrollPane.setVisible(true);
            saveButton.setEnabled(false);
            revertButton.setEnabled(false);
        } else {
            try {
                archiveUnit.getContent();
                archiveUnit.getManagement();
                archiveUnit.getArchiveUnitProfile();
            } catch (SEDALibException e) {
                String title = SEDAXMLEventReader.extractNamedElement("Title", archiveUnit.getContentXmlData());
                warningText.setText(translate("ArchiveUnit") + " - " + (title != null ? title + " - " : "") + archiveUnit.getInDataObjectPackageId()
                        + " a un problème de construction qui empêche de l'éditer de manière structurée.\n"
                        + SEDALibProgressLogger.getMessagesStackString(e));
                scrollPane.setVisible(false);
                warningPane.setVisible(true);
                return;
            }
            archiveUnitEditor.editArchiveUnit(archiveUnit);
            scrollPane.setVisible(true);
            warningPane.setVisible(false);
            saveButton.setEnabled(true);
            revertButton.setEnabled(true);
        }
    }

    public ArchiveUnit extractArchiveUnit() throws SEDALibException {
        return (ArchiveUnit)archiveUnitEditor.extractEditedObject();
    }
}
