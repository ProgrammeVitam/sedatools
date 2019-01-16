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
 * circulated by CEA, CNRS and INRIA dataObjectPackage the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.resip.viewer;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

import fr.gouv.vitam.tools.resip.frame.MainWindow;
import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

/**
 * The Class DataObjectListViewer.
 */
public class DataObjectListViewer extends JList<DataObject> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8610503305899021755L;

    /** The main. */
    MainWindow main;

    /** The long archiveTransfer tree item name. */
    boolean longSipTreeItemName;

    /**
     * Instantiates a new data object list viewer.
     *
     * @param main the main
     * @param listModel the list model
     */
    public DataObjectListViewer(MainWindow main, DefaultListModel<DataObject> listModel) {
        super(listModel);
        this.main = main;
        final DataObjectListViewer list = this;

        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0) {
                    list.main.dataObjectListItemClick(list.getModel().getElementAt(index));
                }
            }
        };
        this.addMouseListener(ml);

        KeyListener kl = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_DELETE) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
                    removeDataObject(list.getSelectedValue());
                }
            }
        };
        this.addKeyListener(kl);

        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setTransferHandler(new DataObjectListTransferHandler(this));
    }

    /* (non-Javadoc)
     * @see javax.swing.JList#getPreferredScrollableViewportSize()
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        if (getModel().getSize() == 0)
            return new Dimension(150, 128);
        else
            return super.getPreferredScrollableViewportSize();
    }

    /**
     * Adds the to archive unit group or not.
     *
     * @param au the au
     * @param dataObject the data object
     */
    void addToArchiveUnitDataObjectGroup(ArchiveUnit au, DataObject dataObject) {
        DataObjectGroup og;
        if (au.getDataObjectRefList().getCount() == 0) {
            og = new DataObjectGroup(au.getDataObjectPackage(), null);
            au.addDataObjectById(og.getInDataObjectPackageId());
            if (dataObject instanceof BinaryDataObject) {
                og.addDataObject((BinaryDataObject) dataObject);
            } else if (dataObject instanceof PhysicalDataObject) {
                og.addDataObject((PhysicalDataObject) dataObject);
            }
            DataObjectPackageTreeModel attm = (DataObjectPackageTreeModel) ResipGraphicApp.getTheApp().mainWindow.getDataObjectPackageTreePaneViewer().getModel();
            attm.generateDataObjectNode(og, attm.findTreeNode(au));
            attm.nodeStructureChanged(attm.findTreeNode(au));
        } else {
            og = (DataObjectGroup) au.getDataObjectRefList().getDataObjectList().get(0);
            if (dataObject instanceof BinaryDataObject) {
                og.addDataObject((BinaryDataObject) dataObject);
            } else if (dataObject instanceof PhysicalDataObject) {
                og.addDataObject((PhysicalDataObject) dataObject);
            }
        }
    }

    /**
     * Removes the data object from archive unit.
     *
     * @param au the archive unit
     * @param dataObject the data object
     */
    void removeFromArchiveUnit(ArchiveUnit au, DataObject dataObject) {
        DataObjectGroup og;
        if ((au.getDataObjectRefList().getCount() == 1)
                && (au.getDataObjectRefList().getDataObjectList().get(0) instanceof DataObjectGroup)) {
            og = (DataObjectGroup) au.getDataObjectRefList().getDataObjectList().get(0);
            og.removeDataObject(dataObject);
        } else {
            au.removeDataObjectById(dataObject.getInDataObjectPackageId());
        }
        if (dataObject instanceof BinaryDataObject) {
            au.getDataObjectPackage().getBdoInDataObjectPackageIdMap().remove(dataObject.getInDataObjectPackageId());
        } else if (dataObject instanceof PhysicalDataObject) {
            au.getDataObjectPackage().getPdoInDataObjectPackageIdMap().remove(dataObject.getInDataObjectPackageId());
        }
    }

    /**
     * Adds the file data object.
     *
     * @param path the path
     */
    public void addFileDataObject(Path path) {
        ArchiveUnit targetAU = ResipGraphicApp.getTheApp().mainWindow.dataObjectPackageTreeItemDisplayed.getArchiveUnit();

        if (targetAU == null)
            return;

        String filename = path.getFileName().toString();
        if (filename.matches("__\\w+(_[0-9]+)?__PhysicalDataObjectMetadata.xml")) {
            try {
                PhysicalDataObject pdo = new PhysicalDataObject(targetAU.getDataObjectPackage(), new String(Files.readAllBytes(path), "UTF-8"));
                addToArchiveUnitDataObjectGroup(targetAU, pdo);
                ((DefaultListModel<DataObject>) getModel()).addElement(pdo);
            } catch (IOException | SEDALibException e) {
                JOptionPane.showMessageDialog(ResipGraphicApp.getTheApp().mainWindow,
                        "Impossible d'ouvrir le fichier " + path.toString()
                                + "\nLes données peuvent avoir été parteillement modifiées",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            String dataObjectVersion;
            if (filename.matches("__\\w+__.+")) {
                dataObjectVersion = DiskToDataObjectPackageImporter.extractDataObjectVersion(filename);
                filename= filename.substring(dataObjectVersion.length()+4);
            } else {
                if (targetAU.getDataObjectRefList().getCount() == 0) {
                    dataObjectVersion = "BinaryMaster_1";
                } else {
                    dataObjectVersion = "Undefined_Undefined";
                }
            }
            BinaryDataObject bdo = new BinaryDataObject(targetAU.getDataObjectPackage(), path, filename, dataObjectVersion);
            try {
                bdo.extractTechnicalElements(null);
            } catch (SEDALibException ignored) {
            }
            addToArchiveUnitDataObjectGroup(targetAU, bdo);
            ((DefaultListModel<DataObject>) getModel()).addElement(bdo);
        }
        main.getApp().currentWork.getCreationContext().setStructureChanged(true);
        main.refreshTreePaneLabel();
    }

    /**
     * Removes the data object.
     *
     * @param dataObject the data object
     */
    public void removeDataObject(DataObject dataObject) {
        ArchiveUnit targetAU = ResipGraphicApp.getTheApp().mainWindow.dataObjectPackageTreeItemDisplayed.getArchiveUnit();

        if (targetAU == null)
            return;

        removeFromArchiveUnit(targetAU, dataObject);
        ((DefaultListModel<DataObject>) getModel()).removeElement(dataObject);
        main.dataObjectListItemClick(null);
        main.getApp().currentWork.getCreationContext().setStructureChanged(true);
        main.refreshTreePaneLabel();
    }
}
