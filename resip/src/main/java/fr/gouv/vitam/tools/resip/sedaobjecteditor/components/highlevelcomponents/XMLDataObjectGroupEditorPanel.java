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
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.frame.XmlEditDialog;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectListCellRenderer;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectListViewer;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fr.gouv.vitam.tools.resip.frame.MainWindow.CLICK_FONT;
import static fr.gouv.vitam.tools.resip.frame.MainWindow.DETAILS_FONT;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor.BOLD_LABEL_FONT;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

public class XMLDataObjectGroupEditorPanel extends JPanel implements DataObjectGroupEditorPanel {
    /**
     * The ArchiveUnit owning the original edited DataObjectGroup
     */
    public ArchiveUnit editedArchiveUnit;

    /**
     * The displayed element
     */
    private DataObject displayedDataObject;

    /**
     * The graphic elements
     */
    private DataObjectListViewer dataObjectListViewer;
    private JTextPane dataObjectDetailText;
    private JButton openObjectButton, changeObjectButton, editObjectButton;

    /**
     * Instantiates a new Xml ArchiveUnit editor panel.
     */
    public XMLDataObjectGroupEditorPanel() {
        GridBagConstraints gbc;
        GridBagLayout gbl;

        this.editedArchiveUnit = null;
        this.displayedDataObject = null;

        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        gbl.rowWeights = new double[]{1.0};
        setLayout(gbl);

        JSplitPane dataObjectGroupSplitPane = new JSplitPane();
        dataObjectGroupSplitPane.setResizeWeight(0.2);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(dataObjectGroupSplitPane, gbc);

        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0};
        gbl.rowWeights = new double[]{0.0, 1.0};
        JPanel dataObjectListPane = new JPanel(gbl);

        JLabel dataObjectListPaneLabel = new JLabel(translateTag("DataObjectGroup"));
        dataObjectListPaneLabel.setFont(BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        dataObjectListPane.add(dataObjectListPaneLabel, gbc);

        DefaultListModel<DataObject> dataObjectsListModel = new DefaultListModel<DataObject>();
        dataObjectListViewer = new DataObjectListViewer(this, dataObjectsListModel);
        dataObjectListViewer.setFont(DETAILS_FONT);
        dataObjectListViewer.setLayoutOrientation(JList.VERTICAL);
        dataObjectListViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DataObjectListCellRenderer dataObjectListCellRenderer = new DataObjectListCellRenderer();
        dataObjectListViewer.setCellRenderer(dataObjectListCellRenderer);
        JScrollPane binaryObjectsListViewerScrollPane = new JScrollPane(dataObjectListViewer);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        dataObjectListPane.add(binaryObjectsListViewerScrollPane, gbc);

        openObjectButton = new JButton("Ouvrir l'objet");
        openObjectButton.setFont(CLICK_FONT);
        openObjectButton.setMinimumSize(new Dimension(20, 25));
        openObjectButton.setEnabled(false);
        openObjectButton.addActionListener(e -> buttonOpenObject());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 2;
        dataObjectListPane.add(openObjectButton, gbc);

        changeObjectButton = new JButton("Changer l'objet");
        changeObjectButton.setFont(CLICK_FONT);
        changeObjectButton.setMinimumSize(new Dimension(20, 25));
        changeObjectButton.setEnabled(false);
        changeObjectButton.addActionListener(e -> buttonChangeObject());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 3;
        dataObjectListPane.add(changeObjectButton, gbc);

        dataObjectGroupSplitPane.setLeftComponent(dataObjectListPane);

        gbl = new GridBagLayout();
        gbl.rowWeights = new double[]{0.0, 1.0, 0.0};
        gbl.columnWeights = new double[]{1.0};
        JPanel dataObjectDetailPane = new JPanel(gbl);

        JLabel dataObjectDetailPaneLabel = new JLabel("Détails");
        dataObjectDetailPaneLabel.setFont(BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        dataObjectDetailPane.add(dataObjectDetailPaneLabel, gbc);

        dataObjectDetailText = new JTextPane();
        dataObjectDetailText.setFont(DETAILS_FONT);
        JScrollPane dataObjectDetailScrollPane = new JScrollPane(dataObjectDetailText);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridy = 1;
        gbc.gridx = 0;
        dataObjectDetailPane.add(dataObjectDetailScrollPane, gbc);

        editObjectButton = new JButton("Editer le DataObject");
        editObjectButton.setFont(CLICK_FONT);
        editObjectButton.setEnabled(false);
        editObjectButton.addActionListener(e -> buttonEditDataObject());
        GridBagConstraints gbc_editObjectButton = new GridBagConstraints();
        gbc_editObjectButton.insets = new Insets(5, 5, 5, 0);
        gbc_editObjectButton.gridwidth = 1;
        gbc_editObjectButton.gridx = 0;
        gbc_editObjectButton.gridy = 2;
        dataObjectDetailPane.add(editObjectButton, gbc_editObjectButton);

        dataObjectGroupSplitPane.setRightComponent(dataObjectDetailPane);
    }

    public void buttonOpenObject() {
        Path path;
        try {
            if ((displayedDataObject instanceof BinaryDataObject)) {
                path = ((BinaryDataObject) displayedDataObject).getOnDiskPath();
                if (path != null)
                    Desktop.getDesktop().open(path.toFile());
            }
        } catch (IOException ignored) {
        }
    }

    private String chooseNewObject() {
        File tmp = null;
        try {
            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tmp = fileChooser.getSelectedFile();
                if (!tmp.exists() || !tmp.isFile()) {
                    throw new ResipException("Le nom choisi ne correspond pas à un fichier existant");
                }
                return tmp.toString();
            } else
                return null;
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                    "Le fichier choisi " + (tmp != null ? "[" + tmp.getAbsolutePath() + "]" : "") + " ne peut être pris en compte",
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de choisir le fichier "
                    + (tmp != null ? "[" + tmp.getAbsolutePath() + "]" : "") + "\n->" + e.getMessage());
            return null;
        }
    }

    private void buttonChangeObject() {
        String newBinary = chooseNewObject();
        if (newBinary != null) {
            if ((displayedDataObject instanceof BinaryDataObject)) {
                BinaryDataObject bdo = (BinaryDataObject) displayedDataObject;
                bdo.setOnDiskPath(Paths.get(newBinary));
                try {
                    bdo.fileInfo = null;
                    bdo.extractTechnicalElements(null);
                } catch (SEDALibException e) {
                    UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                            "Les informations techniques du fichier choisi [" + newBinary
                                    + "] n'ont pas pu être toutes extraites, la mise à jour est partielle.",
                            "Erreur", UserInteractionDialog.ERROR_DIALOG,
                            null);
                    ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Les informations techniques du fichier choisi [" + newBinary
                            + "] n'ont pas pu être toutes extraites, la mise à jour est partielle.\n->"
                            + e.getMessage());
                }
                selectDataObject(displayedDataObject);
                ResipGraphicApp.getTheApp().setModifiedContext(true);
            }
        }
    }

    private void buttonEditDataObject() {
        XmlEditDialog xmlEditDialog = new XmlEditDialog(ResipGraphicApp.getTheWindow(), displayedDataObject);
        xmlEditDialog.setVisible(true);
        if (xmlEditDialog.getReturnValue()) {
            ((DefaultListModel<DataObject>) dataObjectListViewer.getModel()).set(0,
                    ((DefaultListModel<DataObject>) dataObjectListViewer.getModel()).get(0));
            selectDataObject(displayedDataObject);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        }
    }

    @Override
    public void editDataObjectGroup(ArchiveUnit archiveUnit) throws SEDALibException {
        this.editedArchiveUnit = archiveUnit;
        this.displayedDataObject = null;

        if (editedArchiveUnit == null)
            dataObjectListViewer.initDataObjectGroup(null);
        else
            dataObjectListViewer.initDataObjectGroup(editedArchiveUnit.getTheDataObjectGroup());

        openObjectButton.setEnabled(false);
        changeObjectButton.setEnabled(false);
    }

    @Override
    public DataObjectGroup extractDataObjectGroup() throws SEDALibException {
        return editedArchiveUnit.getTheDataObjectGroup();
    }

    @Override
    public void selectDataObject(DataObject dataObject) {
        if (dataObject == null) {
            dataObjectDetailText.setText("");
            openObjectButton.setEnabled(false);
            changeObjectButton.setEnabled(false);
            displayedDataObject = null;
            editObjectButton.setEnabled(false);
        } else {
            editObjectButton.setEnabled(true);
            displayedDataObject = dataObject;
            dataObjectListViewer.selectDataObjectListItem(dataObject);
            if (dataObject instanceof BinaryDataObject) {
                BinaryDataObject bdo = (BinaryDataObject) dataObject;
                String tmp = "";
                try {
                    tmp = bdo.toSedaXmlFragments();
                    //tmp = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT).indentString(tmp);
                } catch (SEDALibException e) {
                    ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Resip.InOut: Erreur à l'indentation du BinaryDataObject ["
                            + bdo.getInDataObjectPackageId() + "]");
                }
                dataObjectDetailText.setText(tmp);
                openObjectButton.setEnabled(true);
                changeObjectButton.setEnabled(true);
            } else {
                PhysicalDataObject pdo = (PhysicalDataObject) dataObject;
                String tmp = "";
                try {
                    tmp = pdo.toSedaXmlFragments();
                    //tmp = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT).indentString(tmp);
                } catch (SEDALibException e) {
                    ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Resip.InOut: Erreur à l'indentation du PhysicalDataObject ["
                            + pdo.getInDataObjectPackageId() + "]");
                }
                dataObjectDetailText.setText(tmp);
                openObjectButton.setEnabled(false);
                changeObjectButton.setEnabled(false);
            }
            dataObjectDetailText.setCaretPosition(0);
        }
    }
}
