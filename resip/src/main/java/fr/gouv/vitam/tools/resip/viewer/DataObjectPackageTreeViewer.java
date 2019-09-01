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
package fr.gouv.vitam.tools.resip.viewer;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.DustbinItem;
import fr.gouv.vitam.tools.resip.frame.MainWindow;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.threads.AddThread;
import fr.gouv.vitam.tools.resip.threads.ExpandThread;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.inout.importer.CompressedFileToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

/**
 * The Class DataObjectPackageTreeViewer.
 */
public class DataObjectPackageTreeViewer extends JTree implements ActionListener {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 8610503305899021755L;

    /**
     * The main.
     */
    MainWindow main;

    /**
     * The long archive transfer tree item name.
     */
    boolean longDataObjectPackageTreeItemName;

    /**
     * The popup STN.
     */
    private List<DataObjectPackageTreeNode> popupSTN;

    /**
     * The uncompressed DataObjectGroup tree node.
     */
    private DataObjectPackageTreeNode uncompressedDogTreeNode;

    private BinaryDataObject getCompressedBinaryDataObject(DataObjectPackageTreeNode treeNode) {
        DataObject dataObject = treeNode.getDataObject();
        if (dataObject != null) {
            if (dataObject instanceof DataObjectGroup) {
                DataObjectGroup dog = (DataObjectGroup) dataObject;
                if ((dog.getPhysicalDataObjectList() == null) || (dog.getPhysicalDataObjectList().isEmpty()) &&
                        (dog.getBinaryDataObjectList() != null) && (dog.getBinaryDataObjectList().size() == 1)) {
                    BinaryDataObject bdo = dog.getBinaryDataObjectList().get(0);
                    try {
                        if (CompressedFileToArchiveTransferImporter.isKnownCompressedMimeType(bdo.formatIdentification.mimeType)) {
                            return bdo;
                        }
                    } catch (SEDALibException ignored) {
                    }
                }
            }
        }
        return null;
    }

    /**
     * Instantiates a new archive transfer tree viewer.
     *
     * @param main      the main
     * @param treeModel the tree model
     */
    public DataObjectPackageTreeViewer(MainWindow main, DataObjectPackageTreeModel treeModel) {
        super(treeModel);
        this.main = main;
        final DataObjectPackageTreeViewer tree = this;
        DataObjectPackageTreeViewer thisSTV = this;

        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selPath != null) {
                    DataObjectPackageTreeNode stn = (DataObjectPackageTreeNode) (selPath.getLastPathComponent());
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        tree.main.dataObjectPackageTreeItemClick(selPath);
                    } else if (SwingUtilities.isRightMouseButton(e) && (stn.getParents() != null)) {

                        JPopupMenu popup = new JPopupMenu();
                        popupSTN = new ArrayList<DataObjectPackageTreeNode>(5);
                        popupSTN.add(stn);
                        JMenuItem mi;
                        if (stn.getParents().get(0) != getModel().getRoot()) {
                            int i = 0;
                            for (DataObjectPackageTreeNode pstn : stn.getParents()) {
                                popupSTN.add(pstn);
                                mi = new JMenuItem("Voir " + pstn.getArchiveUnit().getInDataObjectPackageId() + "-" + pstn.getTitle());
                                mi.addActionListener(thisSTV);
                                mi.setActionCommand("Link-" + (i++));
                                popup.add(mi);
                            }
                            popup.addSeparator();
                        }
                        mi = new JMenuItem("Ajouter une sous-ArchiveUnit");
                        mi.addActionListener(thisSTV);
                        mi.setActionCommand("NewSubArchiveUnit");
                        popup.add(mi);
                        BinaryDataObject bdo = getCompressedBinaryDataObject(stn);
                        if (bdo != null) {
                            popup.addSeparator();
                            mi = new JMenuItem("Remplacer par le décompressé");
                            mi.addActionListener(thisSTV);
                            mi.setActionCommand("Expand");
                            thisSTV.setAffectedDogTreeNode(stn);
                            popup.add(mi);
                        }
                        popup.show((Component) e.getSource(), e.getX(), e.getY());
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    clearSelection();
                    main.refreshInformations();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem mi;
                    mi = new JMenuItem("Ajouter une ArchiveUnit racine");
                    mi.addActionListener(thisSTV);
                    mi.setActionCommand("NewRootArchiveUnit");
                    popup.add(mi);

                    popup.show((Component) e.getSource(), e.getX(), e.getY());
                }
            }
        };
        this.addMouseListener(ml);

        KeyListener kl = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_DELETE) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
                    if (tree.getSelectionPaths() != null)
                        for (TreePath path : tree.getSelectionPaths())
                            removeSubTree(path, true);
                }
            }
        };
        this.addKeyListener(kl);

        setRootVisible(false);
        setLargeModel(true);
        DataObjectPackageTreeCellRenderer doptcr = new DataObjectPackageTreeCellRenderer();
        doptcr.setClosedIcon(new ImageIcon(getClass().getResource("/icon/folder.png")));
        doptcr.setOpenIcon(new ImageIcon(getClass().getResource("/icon/folder-open.png")));
        doptcr.setLeafIcon(new ImageIcon(getClass().getResource("/icon/text-x-generic.png")));
        setCellRenderer(doptcr);
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new DataObjectPackageTreeTransferHandler(this));
        getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);//.SINGLE_TREE_SELECTION);

        this.longDataObjectPackageTreeItemName = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().startsWith("Link-")) {
            String rank = ae.getActionCommand().substring(5);
            TreePath path = new TreePath(
                    ((DefaultTreeModel) getModel()).getPathToRoot(popupSTN.get(Integer.parseInt(rank) + 1)));
            setExpandsSelectedPaths(true);
            setSelectionPath(path);
            expandPath(path);
            scrollPathToVisible(path);
        } else if (ae.getActionCommand().equals("NewSubArchiveUnit")) {
            addArchiveUnit(new TreePath(
                    ((DefaultTreeModel) getModel()).getPathToRoot(popupSTN.get(0))));
        } else if (ae.getActionCommand().equals("NewRootArchiveUnit")) {
            addArchiveUnit(null);
        } else if (ae.getActionCommand().equals("Expand")) {
            ExpandThread.launchExpandThread((DataObjectPackageTreeNode) uncompressedDogTreeNode.getParent(),
                    getCompressedBinaryDataObject(uncompressedDogTreeNode));
        }
    }

    /**
     * Activate long archive transfer tree item name.
     *
     * @param value the value
     */
    public void activateLongDataObjectPackageTreeItemName(boolean value) {
        if (longDataObjectPackageTreeItemName != value) {
            longDataObjectPackageTreeItemName = value;
            DataObjectPackageTreeModel stm = (DataObjectPackageTreeModel) getModel();
            DataObjectPackageTreeNode root = (DataObjectPackageTreeNode) stm.getRoot();
            nodeLabelStructureChanged(stm, root);
        }
    }

    /**
     * Node label structure changed.
     *
     * @param stm  the stm
     * @param root the root
     */
    private void nodeLabelStructureChanged(DataObjectPackageTreeModel stm, TreeNode root) {
        if (root != null) {
            stm.nodeChanged(root);
            for (int i = 0; i < root.getChildCount(); i++)
                nodeLabelStructureChanged(stm, root.getChildAt(i));
        }
    }

    /**
     * Labels update.
     */
    public void labelsUpdate() {
        DataObjectPackageTreeModel stm = (DataObjectPackageTreeModel) getModel();
        DataObjectPackageTreeNode root = (DataObjectPackageTreeNode) stm.getRoot();
        nodeLabelStructureChanged(stm, root);
        repaint();
    }

    /**
     * Checks if is activated long archive transfer tree item name.
     *
     * @return true, if is activated long archive transfer tree item name
     */
    public boolean isActivatedLongDataObjectPackageTreeItemName() {
        return longDataObjectPackageTreeItemName;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JTree#convertValueToText(java.lang.Object, boolean, boolean,
     * boolean, int, boolean)
     */
    @Override
    public String convertValueToText(final Object value, final boolean selected, final boolean expanded,
                                     final boolean leaf, final int row, final boolean hasFocus) {
        final DataObjectPackageTreeNode stn = (DataObjectPackageTreeNode) value;
        String result;

        if (stn.getArchiveUnit() != null) {
            final ArchiveUnit archiveUnit = stn.getArchiveUnit();
            result = stn.getTitle();
            if (longDataObjectPackageTreeItemName) {
                if (!leaf)
                    result += " (" + stn.getAuChildCount() + "/" + stn.getAuRecursivCount() + ")";
                result += " - " + archiveUnit.getInDataObjectPackageId();
            }
        } else {
            result = stn.getDataObject().getClass().getSimpleName() + " "
                    + stn.getDataObject().getInDataObjectPackageId();
        }
        return result;
    }

    // tree manipulation methods

    /**
     * Gets the archive transfer.
     *
     * @return the archive transfer
     */
    private DataObjectPackage getDataObjectPackage() {
        return ((DataObjectPackageTreeNode) getModel().getRoot()).getArchiveUnit().getDataObjectPackage();
    }

    /**
     * Gets the path string.
     *
     * @param path the path
     * @return the path string
     */
    static String getPathString(TreePath path) {
        if (path == null)
            return "";
        DataObjectPackageTreeNode attn = (DataObjectPackageTreeNode) path.getLastPathComponent();
        if (attn.getArchiveUnit() != null)
            return getPathString(path.getParentPath()) + "->" + attn.getTitle();
        else
            return getPathString(path.getParentPath()) + "->DataObject "
                    + attn.getDataObject().getInDataObjectPackageId();

    }

    /**
     * Sets affected DataObjectGroup tree node.
     *
     * @param affectedDogTreeNode the affected tree node
     */
    public void setAffectedDogTreeNode(DataObjectPackageTreeNode affectedDogTreeNode) {
        this.uncompressedDogTreeNode = affectedDogTreeNode;
    }

    /**
     * Adds the link.
     *
     * @param addPath    the add path
     * @param targetPath the target path
     */
    public void addLink(TreePath addPath, TreePath targetPath) {
        ArchiveUnit addAU, targetAU;
        DataObject addDo;
        DataObjectPackageTreeNode targetNode, addNode;
        targetAU = ((DataObjectPackageTreeNode) targetPath.getLastPathComponent()).getArchiveUnit();
        targetNode = (DataObjectPackageTreeNode) targetPath.getLastPathComponent();
        addAU = ((DataObjectPackageTreeNode) addPath.getLastPathComponent()).getArchiveUnit();
        if (addAU != null) {
            targetAU.addChildArchiveUnit(addAU);
            addNode = ((DataObjectPackageTreeModel) getModel()).findTreeNode(addAU);
        } else {
            addDo = ((DataObjectPackageTreeNode) addPath.getLastPathComponent()).getDataObject();
            targetAU.addDataObjectById(addDo.getInDataObjectPackageId());
            addNode = ((DataObjectPackageTreeModel) getModel()).findTreeNode(addDo);
        }
        targetNode.addChildrenNode(addNode);
        ((DataObjectPackageTreeModel) getModel()).nodeStructureChanged(targetNode);
        main.getApp().currentWork.getCreationContext().setStructureChanged(true);
        main.getApp().setContextLoaded(true);
    }

    private void resetTouchedFromNode(DataObjectPackageTreeNode node) {
        node.setTouchedCounter(0);
        for (int i = 0; i < node.getChildCount(); i++)
            resetTouchedFromNode((DataObjectPackageTreeNode) node.getChildAt(i));
    }

    private void countTouchedFromNode(DataObjectPackageTreeNode node) {
        node.incTouchedCounter();
        for (int i = 0; i < node.getChildCount(); i++)
            countTouchedFromNode((DataObjectPackageTreeNode) node.getChildAt(i));
    }

    private void removeNode(DataObjectPackageTreeNode parentNode, DataObjectPackageTreeNode node, DustbinItem de) {
        node.removeParent(parentNode);
        node.decTouchedCounter();
        if (node.getParents().size() > node.getTouchedCounter())
            return;
        de.addTreeNode(node);
        for (int i = 0; i < node.getChildCount(); i++)
            removeNode(node, (DataObjectPackageTreeNode) node.getChildAt(i), de);
    }


    /**
     * Gets expansion state of the displayed tree.
     *
     * @return the expansion state
     */
    public Map<TreePath, Boolean> getExpansionState() {
        Map<TreePath, Boolean> expansionState = new HashMap<TreePath, Boolean>();
        for (int i = 0; i < getRowCount(); i++) {
            TreePath treePath = getPathForRow(i);
            expansionState.put(treePath, isExpanded(i));
        }
        return expansionState;
    }

    /**
     * Sets expansion state of the displayed tree.
     *
     * @param expansionState the expansion state
     */
    public void setExpansionState(Map<TreePath, Boolean> expansionState) {
        for (Map.Entry<TreePath, Boolean> e : expansionState.entrySet()) {
            if (e.getValue())
                expandPath(e.getKey());
        }
    }

    private void innerSetPathExpansionState(TreePath treePath, boolean state) {
        DataObjectPackageTreeNode node = (DataObjectPackageTreeNode) (treePath.getLastPathComponent());
        for (int i = 0; i < node.getChildCount(); i++) {
            innerSetPathExpansionState(treePath.pathByAddingChild(node.getChildAt(i)), state);
        }
        setExpandedState(treePath, state);
    }

    /**
     * Set the same expansion state for all nodes in the given path.
     *
     * @param treePath the tree path
     * @param state    the state
     */
    public void setPathExpansionState(TreePath treePath, boolean state) {
        // Temporarily remove all listeners that would otherwise
        // be flooded with TreeExpansionEvents
        List<TreeExpansionListener> expansionListeners =
                Arrays.asList(getTreeExpansionListeners());
        if (state) {
            for (TreeExpansionListener expansionListener : expansionListeners) {
                removeTreeExpansionListener(expansionListener);
            }
        }

        DataObjectPackageTreeNode node = (DataObjectPackageTreeNode) (treePath.getLastPathComponent());
        for (int i = 0; i < node.getChildCount(); i++) {
            innerSetPathExpansionState(treePath.pathByAddingChild(node.getChildAt(i)), state);
        }

        // Restore the listeners that the tree originally had
        if (state) {
            for (TreeExpansionListener expansionListener : expansionListeners) {
                addTreeExpansionListener(expansionListener);
            }
        }

        if (treePath.getParentPath() == null) {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (state) {
                    collapsePath(treePath.pathByAddingChild(node.getChildAt(i)));
                    expandPath(treePath.pathByAddingChild(node.getChildAt(i)));
                } else {
                    expandPath(treePath.pathByAddingChild(node.getChildAt(i)));
                    collapsePath(treePath.pathByAddingChild(node.getChildAt(i)));
                }
            }
        } else {
            if (state) {
                collapsePath(treePath);
                expandPath(treePath);
            } else {
                expandPath(treePath);
                collapsePath(treePath);
            }
        }
    }

    /**
     * Set the same expansion state for all nodes in the displayed tree.
     *
     * @param state the state
     */
    public void setAllExpansionState(boolean state) {
        DataObjectPackageTreeNode ghostRootNode = (DataObjectPackageTreeNode) (getModel().getRoot());
        if (ghostRootNode != null) {
            TreePath ghostRootPath = new TreePath(ghostRootNode);
            setPathExpansionState(ghostRootPath, state);
        }
    }

    /**
     * Removes the sub tree.
     *
     * @param removePath  the remove path
     * @param confirmFlag the confirm flag
     */
    public void removeSubTree(TreePath removePath, boolean confirmFlag) {
        ArchiveUnit childAU, fatherAU;
        DataObject childDo;
        DataObjectPackageTreeNode childNode, fatherNode;
        childNode = ((DataObjectPackageTreeNode) removePath.getLastPathComponent());
        fatherNode = ((DataObjectPackageTreeNode) removePath.getParentPath().getLastPathComponent());
        childAU = ((DataObjectPackageTreeNode) removePath.getLastPathComponent()).getArchiveUnit();
        fatherAU = ((DataObjectPackageTreeNode) removePath.getParentPath().getLastPathComponent()).getArchiveUnit();

        int auCount = childNode.getAuRecursivCount();
        int ogCount = childNode.getOgRecursivCount();
        if (childNode.getArchiveUnit() != null)
            auCount++;
        if (childNode.getDataObject() != null)
            ogCount++;
        if (confirmFlag && (UserInteractionDialog.getUserAnswer(main,
                "Vous allez effacer " +
                        (auCount == 0 ? "" : auCount + " ArchiveUnits ") +
                        (auCount != 0 && ogCount != 0 ? "et " : "") +
                        (ogCount == 0 ? "" : ogCount + " DataObjectGroups "),
                "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                null) != ResipGraphicApp.OK_DIALOG))
            return;

        fatherNode.removeChildrenNode(childNode);

        DustbinItem be = new DustbinItem(fatherNode, childNode);
        resetTouchedFromNode(childNode);
        countTouchedFromNode(childNode);
        removeNode(fatherNode, childNode, be);
        be.removeContentFromDataObjectPackage(fatherAU.getDataObjectPackage());

        if (childAU != null) {
            fatherAU.removeChildArchiveUnit(childAU);
        } else {
            childDo = ((DataObjectPackageTreeNode) removePath.getLastPathComponent()).getDataObject();
            fatherAU.removeDataObjectById(childDo.getInDataObjectPackageId());
        }
        ((DataObjectPackageTreeModel) getModel()).nodeStructureChanged(fatherNode);
        main.getApp().currentWork.getCreationContext().setStructureChanged(true);
        main.getApp().setContextLoaded(true);
        main.getApp().setModifiedContext(true);
        main.refreshInformations();
    }

    /**
     * Adds one new ArchiveUnit under target path.
     *
     * @param targetPath the target path
     */
    public void addArchiveUnit(TreePath targetPath) {
        ArchiveUnit targetAU;
        AddThread addThread;

        DataObjectPackageTreeNode targetNode;
        if (targetPath == null)
            targetNode = (DataObjectPackageTreeNode) getModel().getRoot();
        else
            targetNode = (DataObjectPackageTreeNode) targetPath.getLastPathComponent();
        DataObjectPackageTreeModel treeModel = (DataObjectPackageTreeModel) getModel();

        ArchiveUnit au = new ArchiveUnit(targetNode.getArchiveUnit().getDataObjectPackage());
        au.setDefaultContent("Nouvelle ArchiveUnit", "RecordGrp");
        targetNode.getArchiveUnit().addChildArchiveUnit(au);

        treeModel.generateArchiveUnitNode(au, targetNode);
        targetNode.actualiseRecursivCounts(1, 0);

        treeModel.nodeStructureChanged(targetNode);
        main.getApp().currentWork.getCreationContext().setStructureChanged(true);

        main.getApp().setContextLoaded(true);
        main.getApp().setModifiedContext(true);
        main.refreshInformations();
    }
}
