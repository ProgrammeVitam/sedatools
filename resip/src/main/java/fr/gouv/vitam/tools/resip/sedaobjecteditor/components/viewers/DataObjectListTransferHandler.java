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