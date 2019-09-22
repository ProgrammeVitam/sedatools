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
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.CompositeEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.DataObjectGroupEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The ArchiveUnit object editor class for structured edition.
 */
public class StructuredDataObjectGroupEditorPanel extends JPanel implements DataObjectGroupEditorPanel {

    /**
     * The original edited DataObjectGroup and ArchiveUnit
     */
    private DataObjectGroup editedDataObjectGroup;
    private ArchiveUnit editedArchiveUnit;

    /**
     * The graphic elements
     */
    private DataObjectGroupEditor dataObjectGroupEditor;
    private JButton revertButton, saveButton;
    private JScrollPane scrollPane;
    private JPanel addPane;

    /**
     * Instantiates a new structured ArchiveUnit editor panel.
     */
    public StructuredDataObjectGroupEditorPanel() {
        super();
        GridBagLayout gbl;
        GridBagConstraints gbc;

        this.dataObjectGroupEditor = new DataObjectGroupEditor(null, null);
        this.editedArchiveUnit = null;

        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{0.0, 1.0, 0.0};
        gbl.rowWeights = new double[]{1.0, 0.0};
        setLayout(gbl);

        SEDAObjectEditorPanel mep = null;
        try {
            mep = dataObjectGroupEditor.getSEDAObjectEditorPanel();
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
        addPane = new JPanel(gbl);
        addPane.setVisible(false);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(addPane, gbc);

        JLabel addText = new JLabel(translateTag("ArchiveUnit") + " sans " + translateTag("DataObjectGroup").toLowerCase());
        addText.setFont(SEDAObjectEditor.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        addPane.add(addText, gbc);

        JButton addButton = new JButton("Ajouter un groupe d'objets");
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        addButton.addActionListener(arg -> {
            addDataObjectGroupButton(arg);
        });
        addPane.add(addButton, gbc);

        revertButton = new JButton("Recharger "+translateTag("DataObjectGroup").toLowerCase());
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

        saveButton = new JButton("Sauver "+translateTag("DataObjectGroup").toLowerCase());
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
            editDataObjectGroup(editedArchiveUnit);
        } catch (SEDALibException ignored) {
        }
    }

    private void saveButton(ActionEvent event) {
        try {
            // when a new DataObjectGroup has been created
            if ((dataObjectGroupEditor.getEditedObject()!=null) && (((DataObjectGroup)dataObjectGroupEditor.getEditedObject()).getInDataObjectPackageId() == null)) {
                DataObjectPackageTreeModel model=(DataObjectPackageTreeModel) ResipGraphicApp.getTheWindow().treePane.dataObjectPackageTreeViewer.getModel();
                editedDataObjectGroup = (DataObjectGroup)dataObjectGroupEditor.getEditedObject();
                editedArchiveUnit.getDataObjectPackage().addDataObjectGroup((DataObjectGroup)dataObjectGroupEditor.getEditedObject());
                editedArchiveUnit.addDataObjectById(((DataObjectGroup)dataObjectGroupEditor.getEditedObject()).getInDataObjectPackageId());
                model.generateDataObjectNode(editedDataObjectGroup, ResipGraphicApp.getTheWindow().treePane.displayedTreeNode);
                ((DataObjectPackageTreeModel) ResipGraphicApp.getTheWindow().treePane.dataObjectPackageTreeViewer.getModel())
                        .nodeChanged(ResipGraphicApp.getTheWindow().treePane.displayedTreeNode);
                ((CompositeEditor) dataObjectGroupEditor).refreshEditedObjectLabel();
            }
            DataObjectGroup dog = dataObjectGroupEditor.extractEditedObject();
            for (SEDAObjectEditor me : dataObjectGroupEditor.objectEditorList) {
                ((CompositeEditor) me).refreshEditedObjectLabel();
            }
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        } catch (SEDALibException e) {
            e.printStackTrace();
        }
    }

    private void addDataObjectGroupButton(ActionEvent event) {
        DataObjectGroup dataObjectGroup = new DataObjectGroup();
        try {
            dataObjectGroupEditor.editDataObjectGroup(dataObjectGroup);
            scrollPane.setVisible(true);
            addPane.setVisible(false);
            saveButton.setEnabled(true);
            revertButton.setEnabled(true);
        } catch (SEDALibException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void editDataObjectGroup(ArchiveUnit archiveUnit) throws SEDALibException {
        this.editedArchiveUnit = archiveUnit;
        if (editedArchiveUnit==null){
            dataObjectGroupEditor.editDataObjectGroup(null);
            scrollPane.setVisible(true);
            saveButton.setEnabled(false);
            revertButton.setEnabled(false);
            revalidate();
            repaint();
       } else if (editedArchiveUnit.getTheDataObjectGroup() == null) {
            dataObjectGroupEditor.editDataObjectGroup(null);
            scrollPane.setVisible(false);
            addPane.setVisible(true);
            saveButton.setEnabled(false);
            revertButton.setEnabled(false);
            revalidate();
            repaint();
        } else {
            dataObjectGroupEditor.editDataObjectGroup(editedArchiveUnit.getTheDataObjectGroup());
            scrollPane.setVisible(true);
            addPane.setVisible(false);
            saveButton.setEnabled(true);
            revertButton.setEnabled(true);
        }
    }

    @Override
    public DataObjectGroup extractDataObjectGroup() throws SEDALibException {
        return dataObjectGroupEditor.extractEditedObject();
    }

    @Override
    public void selectDataObject(DataObject dataObject) throws SEDALibException{
        //FIXME
    }
}
