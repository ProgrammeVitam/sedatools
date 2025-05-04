/*
 *
 */
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The Class DataObjectListTransferHandler.
 */
class DataObjectListTransferHandler extends TransferHandler {

    /**
     * The DataObject list viewer associated with this TransferHandler.
     */
    private DataObjectListViewer list;

    /**
     * Instantiates a new data object list transfer handler.
     *
     * @param list the list
     */
    public DataObjectListTransferHandler(DataObjectListViewer list) {
        this.list = list;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean canImport(TransferSupport ts) {
        return ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && (list.container.getEditedArchiveUnit() != null);
    }

    @Override
    public boolean importData(TransferSupport ts) {
        if (!canImport(ts)) {
            return false;
        }
        try {
            List<File> files = (List<File>) ts.getTransferable().getTransferData(
                    DataFlavor.javaFileListFlavor);
            if (files.size() < 1) {
                return false;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow,
                            "Il n'est pas possible de mettre\nen objet un répertoire",
                            "Avertissement", UserInteractionDialog.IMPORTANT_DIALOG,
                            null);
                    return false;
                }
            }
            for (File file : files) {
                addFileDataObject(file.toPath());
            }
            return true;
        } catch (UnsupportedFlavorException | IOException e) {
            return false;
        }
    }

    private void addToArchiveUnitDataObjectGroup(DataObject dataObject) {
        DataObjectGroup dataObjectGroup = list.container.getEditedArchiveUnit().getTheDataObjectGroup();

        try {
            if (dataObjectGroup == null) {
                dataObjectGroup = new DataObjectGroup();
                list.container.getEditedArchiveUnit().getDataObjectPackage().addDataObjectGroup(dataObjectGroup);
                list.container.getEditedArchiveUnit().addDataObjectById(dataObjectGroup.getInDataObjectPackageId());
                ResipGraphicApp.getTheWindow().treePane.displayedTreeNodeChanged();
            }
            if (dataObject instanceof BinaryDataObject)
                list.container.getEditedArchiveUnit().getDataObjectPackage().addDataObjectPackageIdElement((BinaryDataObject) dataObject);
            else
                list.container.getEditedArchiveUnit().getDataObjectPackage().addDataObjectPackageIdElement((PhysicalDataObject) dataObject);
            dataObjectGroup.addDataObject(dataObject);
        } catch (SEDALibException ignored) {}
    }

    private void addFileDataObject(Path path) {
        ArchiveUnit archiveUnit = list.container.getEditedArchiveUnit();

        String filename = path.getFileName().toString();
        if (filename.matches("__\\w+(_[0-9]+)?__PhysicalDataObjectMetadata.xml")) {
            try {
                PhysicalDataObject pdo = new PhysicalDataObject(archiveUnit.getDataObjectPackage(), new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
                addToArchiveUnitDataObjectGroup(pdo);
                ((DefaultListModel<DataObject>) list.getModel()).addElement(pdo);
                list.selectDataObjectListItem(pdo);
            } catch (IOException | SEDALibException e) {
                UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                        "Impossible d'ouvrir le fichier " + path.toString()
                                + "\nLes données peuvent avoir été parteillement modifiées",
                        "Erreur", UserInteractionDialog.ERROR_DIALOG, null);
                return;
            }
        } else {
            String dataObjectVersion;
            if (filename.matches("__\\w+__.+")) {
                dataObjectVersion = DiskToDataObjectPackageImporter.extractDataObjectVersion(filename);
                filename = filename.substring(dataObjectVersion.length() + 4);
            } else {
                if (archiveUnit.getDataObjectRefList().getCount() == 0) {
                    dataObjectVersion = "BinaryMaster_1";
                } else {
                    dataObjectVersion = "Undefined_Undefined";
                }
            }
            BinaryDataObject bdo = new BinaryDataObject(archiveUnit.getDataObjectPackage(), path, filename, dataObjectVersion);
            try {
                bdo.extractTechnicalElements(null);
            } catch (SEDALibException ignored) {
            }
            addToArchiveUnitDataObjectGroup(bdo);
            ((DefaultListModel<DataObject>) list.getModel()).addElement(bdo);
            list.selectDataObjectListItem(bdo);
        }
        ResipGraphicApp.getTheApp().currentWork.getCreationContext().setStructureChanged(true);
        ResipGraphicApp.getTheWindow().treePane.refreshTreeLabel();
    }
}