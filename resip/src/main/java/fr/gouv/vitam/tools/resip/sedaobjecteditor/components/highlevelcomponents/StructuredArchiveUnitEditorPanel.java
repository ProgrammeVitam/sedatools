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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorPanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.ArchiveUnitEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The ArchiveUnit object editor class for structured edition.
 */
public class StructuredArchiveUnitEditorPanel extends JPanel implements ArchiveUnitEditorPanel {

    /**
     * The graphic elements
     */
    private ArchiveUnitEditor archiveUnitEditor;
    private JButton revertButton, saveButton;
    private JScrollPane scrollPane;
    private JPanel warningPane;
    private JTextArea warningText;

    /**
     * Instantiates a new structured ArchiveUnit editor panel.
     */
    public StructuredArchiveUnitEditorPanel() {
        super();
        GridBagLayout gbl;
        GridBagConstraints gbc;

        this.archiveUnitEditor = new ArchiveUnitEditor(null, null);

        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{0.0, 1.0, 0.0};
        gbl.rowWeights = new double[]{1.0, 0.0};
        setLayout(gbl);

        SEDAObjectEditorPanel mep = null;
        try {
            mep = archiveUnitEditor.getSEDAObjectEditorPanel();
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
        warningText.setBackground(SEDAObjectEditor.GENERAL_BACKGROUND);
        warningText.setFont(SEDAObjectEditor.LABEL_FONT);
        warningText.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 0;
        warningPane.add(warningText, gbc);

        revertButton = new JButton("Recharger "+translateTag("ArchiveUnit").toLowerCase());
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

        saveButton = new JButton("Sauver "+translateTag("ArchiveUnit").toLowerCase());
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
            archiveUnitEditor.extractEditedObject();
            ResipGraphicApp.getTheWindow().treePane.resetDisplayedTreeNodeTitle();
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        } catch (SEDALibException ignored) {
            System.out.println("error : "+ ignored.getMessage());
        }
    }

    @Override
    public void editArchiveUnit(ArchiveUnit archiveUnit) throws SEDALibException {
        if (archiveUnit == null) {
            archiveUnitEditor.editArchiveUnit(null);
            scrollPane.setVisible(true);
            saveButton.setEnabled(false);
            revertButton.setEnabled(false);
            ResipGraphicApp.getTheWindow().dogMetadataPane.editDataObjectGroup(null);
        } else {
            try {
                archiveUnit.getContent();
                archiveUnit.getManagement();
                archiveUnit.getArchiveUnitProfile();
            } catch (SEDALibException e) {
                String title = SEDAXMLEventReader.extractNamedElement("Title", archiveUnit.getContentXmlData());
                warningText.setText(translateTag("ArchiveUnit") + " - " + (title != null ? title + " - " : "") + archiveUnit.getInDataObjectPackageId()
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
            ResipGraphicApp.getTheWindow().dogMetadataPane.editDataObjectGroup(archiveUnit);
        }
    }

    @Override
    public ArchiveUnit extractArchiveUnit() throws SEDALibException {
        return archiveUnitEditor.extractEditedObject();
    }
}
