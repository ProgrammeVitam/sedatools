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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.XMLDataObjectGroupEditorPanel;
import fr.gouv.vitam.tools.resip.threads.ExpandThread;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.inout.importer.CompressedFileToArchiveTransferImporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The Class DataObjectListViewer.
 */
public class DataObjectListViewer extends JList<DataObject> implements ActionListener {

    /**
     * The XMLDataObjectGroupEditorPanel container
     */
    protected XMLDataObjectGroupEditorPanel container;

    /**
     * The uncompress BinaryDataObject.
     */
    private BinaryDataObject uncompressedBdo;


    /**
     * Instantiates a new data object list viewer.
     *
     * @param container the XMLDataObjectGroupEditorPanel container
     * @param listModel the list model
     */
    public DataObjectListViewer(XMLDataObjectGroupEditorPanel container, DefaultListModel<DataObject> listModel) {
        super(listModel);
        this.container = container;
        DataObjectListViewer list = this;

        MouseListener ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index >= 0) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        list.container.selectDataObject(list.getModel().getElementAt(index));
                        if (e.getClickCount() == 2)
                            list.container.buttonOpenObject();
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        DataObject dataObject = list.getModel().getElementAt(index);
                        if (dataObject instanceof BinaryDataObject) {
                            BinaryDataObject bdo = (BinaryDataObject) dataObject;
                            if ((bdo.formatIdentification!=null) &&
                                    (CompressedFileToArchiveTransferImporter.isKnownCompressedDroidFormat(bdo.formatIdentification.getSimpleMetadata("FormatId")))) {
                                JPopupMenu popup = new JPopupMenu();
                                JMenuItem mi;
                                mi = new JMenuItem("Remplacer par le décompressé");
                                mi.addActionListener(list);
                                mi.setActionCommand("Expand");
                                list.uncompressedBdo=bdo;
                                popup.add(mi);
                                popup.show((Component) e.getSource(), e.getX(), e.getY());
                            }
                        }
                    }
                }
            }
        };
        this.addMouseListener(ml);

        KeyListener kl = new KeyAdapter() {
            @Override
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

    /**
     * Init the list viewer with a data object group.
     *
     * @param dataObjectGroup the data object group
     */
    public void initDataObjectGroup(DataObjectGroup dataObjectGroup){
        DefaultListModel<DataObject> model = (DefaultListModel<DataObject>) getModel();
        model.removeAllElements();
        if (dataObjectGroup != null) {
            for (BinaryDataObject bdo : dataObjectGroup.getBinaryDataObjectList())
                model.addElement(bdo);
            for (PhysicalDataObject pdo : dataObjectGroup.getPhysicalDataObjectList())
                model.addElement(pdo);
        }
        if (model.isEmpty())
            container.selectDataObject(null);
        else
            container.selectDataObject(model.elementAt(0));
    }

    /**
     * Select the data object.
     *
     * @param dataObject the data object
     */
    public void selectDataObjectListItem(DataObject dataObject) {
        for (int i = 0; i < getModel().getSize(); i++) {
            if (getModel().getElementAt(i).equals(dataObject)) {
                this.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Removes the data object.
     *
     * @param dataObject the data object
     */
    public void removeDataObject(DataObject dataObject) {
        container.getEditedArchiveUnit().getTheDataObjectGroup().removeDataObject(dataObject);
        ((DefaultListModel<DataObject>) getModel()).removeElement(dataObject);
        if (((DefaultListModel<DataObject>) getModel()).isEmpty())
            container.selectDataObject(null);
        else
            container.selectDataObject(getModel().getElementAt(0));
        ResipGraphicApp.getTheApp().currentWork.getCreationContext().setStructureChanged(true);
        ResipGraphicApp.getTheWindow().treePane.refreshTreeLabel();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("Expand")) {
            ExpandThread.launchExpandThread(ResipGraphicApp.getTheWindow().treePane.getDisplayedTreeNode(), uncompressedBdo);
        }
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        if (getModel().getSize() == 0)
            return new Dimension(200, 128);
        else
            return super.getPreferredScrollableViewportSize();
    }
}
