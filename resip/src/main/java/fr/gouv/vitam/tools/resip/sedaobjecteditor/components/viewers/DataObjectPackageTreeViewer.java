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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.DustbinItem;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.TreeDataObjectPackageEditorPanel;
import fr.gouv.vitam.tools.resip.threads.CompactThread;
import fr.gouv.vitam.tools.resip.threads.DeCompactThread;
import fr.gouv.vitam.tools.resip.threads.ExpandThread;
import fr.gouv.vitam.tools.resip.threads.MailExtractThread;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.inout.importer.CompressedFileToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DataObjectPackageTreeViewer.
 */
public class DataObjectPackageTreeViewer extends JTree implements ActionListener {

    private final TreeDataObjectPackageEditorPanel container;

    /**
     * The long archive transfer tree item name.
     */
    boolean longDataObjectPackageTreeItemName;

    /**
     * The popup STN.
     */
    private transient List<DataObjectPackageTreeNode> popupSTN;

    /**
     * The uncompressed DataObjectGroup tree node.
     */
    private transient DataObjectPackageTreeNode uncompressedDogTreeNode;

    private BinaryDataObject getCompressedBinaryDataObject(DataObjectPackageTreeNode treeNode) {
        DataObject dataObject = treeNode.getDataObject();
        if (dataObject instanceof DataObjectGroup) {
            DataObjectGroup dog = (DataObjectGroup) dataObject;
            if (
                (dog.getPhysicalDataObjectList() == null) ||
                ((dog.getPhysicalDataObjectList().isEmpty()) && (dog.getBinaryDataObjectList() != null))
            ) {
                for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
                    FormatIdentification fi = bdo.getMetadataFormatIdentification();
                    if (
                        (fi != null) &&
                        (CompressedFileToArchiveTransferImporter.isKnownCompressedDroidFormat(
                                fi.getSimpleMetadata("FormatId")
                            ))
                    ) {
                        return bdo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * The mailextract DataObjectGroup tree node.
     */
    private BinaryDataObject getMailBinaryDataObject(DataObjectPackageTreeNode treeNode) {
        DataObject dataObject = treeNode.getDataObject();
        if (dataObject instanceof DataObjectGroup) {
            DataObjectGroup dog = (DataObjectGroup) dataObject;
            if (
                (dog.getPhysicalDataObjectList() == null) ||
                ((dog.getPhysicalDataObjectList().isEmpty()) &&
                    (dog.getBinaryDataObjectList() != null) &&
                    (dog.getBinaryDataObjectList().size() == 1))
            ) {
                BinaryDataObject bdo = dog.getBinaryDataObjectList().get(0);
                FormatIdentification fi = bdo.getMetadataFormatIdentification();
                if (
                    (fi != null) &&
                    (StoreExtractor.getProtocolFromDroidFormat(fi.getSimpleMetadata("FormatId")) != null)
                ) {
                    return bdo;
                }
            }
        }
        return null;
    }

    /**
     * Instantiates a new archive transfer tree viewer.
     *
     * @param container the container
     * @param treeModel the tree model
     */
    public DataObjectPackageTreeViewer(
        TreeDataObjectPackageEditorPanel container,
        DataObjectPackageTreeModel treeModel
    ) {
        super(treeModel);
        this.container = container;
        final DataObjectPackageTreeViewer tree = this;
        DataObjectPackageTreeViewer thisSTV = this;

        MouseListener ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selPath != null) {
                    DataObjectPackageTreeNode stn = (DataObjectPackageTreeNode) (selPath.getLastPathComponent());
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        tree.container.selectTreePathItem(selPath);
                    } else if (SwingUtilities.isRightMouseButton(e) && (stn.getParents() != null)) {
                        JPopupMenu popup = new JPopupMenu();
                        popupSTN = new ArrayList<>(5);
                        popupSTN.add(stn);
                        JMenuItem mi;
                        boolean asParents = false;
                        if (stn.getParents().get(0) != getModel().getRoot()) {
                            int i = 0;
                            for (DataObjectPackageTreeNode pstn : stn.getParents()) {
                                popupSTN.add(pstn);
                                mi = new JMenuItem(
                                    "Voir " + pstn.getArchiveUnit().getInDataObjectPackageId() + "-" + pstn.getTitle()
                                );
                                mi.addActionListener(thisSTV);
                                mi.setActionCommand("Link-" + (i++));
                                popup.add(mi);
                            }
                            asParents = true;
                        }
                        if (stn.getArchiveUnit() != null) {
                            if (asParents) popup.addSeparator();
                            mi = new JMenuItem("Ajouter une sous-ArchiveUnit");
                            mi.addActionListener(thisSTV);
                            mi.setActionCommand("NewSubArchiveUnit");
                            popup.add(mi);
                            if (ResipGraphicApp.getTheApp().interfaceParameters.isExperimentalFlag()) {
                                boolean isDocumentContainer = false;
                                try {
                                    isDocumentContainer = (stn.getArchiveUnit() != null) &&
                                    (stn.getArchiveUnit().getContent() != null) &&
                                    (stn.getArchiveUnit().getContent().getFirstNamedMetadata("DocumentContainer") !=
                                        null);
                                } catch (SEDALibException ignored) {
                                    // no real case
                                }
                                if (isDocumentContainer) {
                                    mi = new JMenuItem("Décompacter l'ArchiveUnit");
                                    mi.addActionListener(thisSTV);
                                    mi.setActionCommand("DeCompactArchiveUnit");
                                    popup.add(mi);
                                } else {
                                    mi = new JMenuItem("Compacter l'ArchiveUnit");
                                    mi.addActionListener(thisSTV);
                                    mi.setActionCommand("CompactArchiveUnit");
                                    popup.add(mi);
                                }
                            }
                        }
                        BinaryDataObject bdo = getCompressedBinaryDataObject(stn);
                        if (bdo != null) {
                            popup.addSeparator();
                            mi = new JMenuItem("Remplacer par le décompressé");
                            mi.addActionListener(thisSTV);
                            mi.setActionCommand("Expand");
                            thisSTV.setAffectedDogTreeNode(stn);
                            popup.add(mi);
                        }
                        bdo = getMailBinaryDataObject(stn);
                        if (bdo != null) {
                            popup.addSeparator();
                            mi = new JMenuItem("Remplacer par l'extraction des messages");
                            mi.addActionListener(thisSTV);
                            mi.setActionCommand("MailExtract");
                            thisSTV.setAffectedDogTreeNode(stn);
                            popup.add(mi);
                        }
                        popup.show((Component) e.getSource(), e.getX(), e.getY());
                    }
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    clearSelection();
                    tree.container.reset();
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
            @Override
            public void keyPressed(KeyEvent e) {
                if (
                    ((e.getKeyCode() == KeyEvent.VK_DELETE) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) &&
                    (tree.getSelectionPaths() != null)
                ) {
                    if (
                        (tree.getSelectionPaths().length > 1) &&
                        (UserInteractionDialog.getUserAnswer(
                                ResipGraphicApp.getTheWindow(),
                                "Vous allez effacer " + tree.getSelectionPaths().length + " ArchiveUnit(s) ",
                                "Confirmation",
                                UserInteractionDialog.WARNING_DIALOG,
                                null
                            ) !=
                            ResipGraphicApp.OK_DIALOG)
                    ) return;
                    boolean confirmFlag = (tree.getSelectionPaths().length == 1);
                    for (TreePath path : tree.getSelectionPaths()) removeSubTree(path, confirmFlag);
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
        getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

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
                ((DefaultTreeModel) getModel()).getPathToRoot(popupSTN.get(Integer.parseInt(rank) + 1))
            );
            setExpandsSelectedPaths(true);
            setSelectionPath(path);
            expandPath(path);
            scrollPathToVisible(path);
        } else if (ae.getActionCommand().equals("NewSubArchiveUnit")) {
            addArchiveUnit(new TreePath(((DefaultTreeModel) getModel()).getPathToRoot(popupSTN.get(0))));
        } else if (ae.getActionCommand().equals("CompactArchiveUnit")) {
            CompactThread.launchCompactThread(popupSTN.get(0));
        } else if (ae.getActionCommand().equals("DeCompactArchiveUnit")) {
            DeCompactThread.launchDeCompactThread(popupSTN.get(0));
        } else if (ae.getActionCommand().equals("NewRootArchiveUnit")) {
            addArchiveUnit(null);
        } else if (ae.getActionCommand().equals("Expand")) {
            ExpandThread.launchExpandThread(
                (DataObjectPackageTreeNode) uncompressedDogTreeNode.getParent(),
                getCompressedBinaryDataObject(uncompressedDogTreeNode)
            );
        } else if (ae.getActionCommand().equals("MailExtract")) {
            MailExtractThread.launchMailExtractThread(
                (DataObjectPackageTreeNode) uncompressedDogTreeNode.getParent(),
                getMailBinaryDataObject(uncompressedDogTreeNode)
            );
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

    private void nodeLabelStructureChanged(DataObjectPackageTreeModel stm, TreeNode root) {
        if (root != null) {
            stm.nodeChanged(root);
            for (int i = 0; i < root.getChildCount(); i++) nodeLabelStructureChanged(stm, root.getChildAt(i));
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

    @Override
    public String convertValueToText(
        final Object value,
        final boolean selected,
        final boolean expanded,
        final boolean leaf,
        final int row,
        final boolean hasFocus
    ) {
        final DataObjectPackageTreeNode stn = (DataObjectPackageTreeNode) value;
        String result;

        if (stn.getArchiveUnit() != null) {
            final ArchiveUnit archiveUnit = stn.getArchiveUnit();
            result = stn.getTitle();
            if (longDataObjectPackageTreeItemName) {
                if (!leaf) result += " (" + stn.getAuChildCount() + "/" + stn.getAuRecursivCount() + ")";
                result += " - " + archiveUnit.getInDataObjectPackageId();
            }
        } else {
            result = stn.getDataObject().getClass().getSimpleName() +
            " " +
            stn.getDataObject().getInDataObjectPackageId();

            String algorithm = null;
            if (stn.getDataObject() instanceof DataObjectGroup) {
                DataObjectGroup dog = (DataObjectGroup) stn.getDataObject();
                for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
                    if (
                        bdo.getMetadataMessageDigest() != null && bdo.getMetadataMessageDigest().getAlgorithm() != null
                    ) {
                        algorithm = bdo.getMetadataMessageDigest().getAlgorithm();
                        break;
                    }
                }
            } else if (stn.getDataObject() instanceof BinaryDataObject) {
                BinaryDataObject bdo = (BinaryDataObject) stn.getDataObject();
                if (bdo.getMetadataMessageDigest() != null && bdo.getMetadataMessageDigest().getAlgorithm() != null) {
                    algorithm = bdo.getMetadataMessageDigest().getAlgorithm();
                }
            }
            if (algorithm != null) {
                result += " (" + algorithm + ")";
            }
        }
        return result;
    }

    // tree manipulation methods

    /**
     * Gets the path string.
     *
     * @param path the path
     * @return the path string
     */
    static String getPathString(TreePath path) {
        if (path == null) return "";
        DataObjectPackageTreeNode attn = (DataObjectPackageTreeNode) path.getLastPathComponent();
        if (attn.getArchiveUnit() != null) return getPathString(path.getParentPath()) + "->" + attn.getTitle();
        else return (
            getPathString(path.getParentPath()) + "->DataObject " + attn.getDataObject().getInDataObjectPackageId()
        );
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
        ArchiveUnit addAU;
        ArchiveUnit targetAU;
        DataObject addDo;
        DataObjectPackageTreeNode targetNode;
        DataObjectPackageTreeNode addNode;
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
        ResipGraphicApp.getTheApp().currentWork.getCreationContext().setStructureChanged(true);
        ResipGraphicApp.getTheApp().setContextLoaded(true);
    }

    private void resetTouchedFromNode(DataObjectPackageTreeNode node) {
        node.setTouchedCounter(0);
        for (int i = 0; i < node.getChildCount(); i++) resetTouchedFromNode(
            (DataObjectPackageTreeNode) node.getChildAt(i)
        );
    }

    private void countTouchedFromNode(DataObjectPackageTreeNode node) {
        node.incTouchedCounter();
        for (int i = 0; i < node.getChildCount(); i++) countTouchedFromNode(
            (DataObjectPackageTreeNode) node.getChildAt(i)
        );
    }

    private void removeNode(DataObjectPackageTreeNode parentNode, DataObjectPackageTreeNode node, DustbinItem de) {
        node.removeParent(parentNode);
        node.decTouchedCounter();
        if (node.getParents().size() > node.getTouchedCounter()) return;
        de.addTreeNode(node);
        for (int i = 0; i < node.getChildCount(); i++) removeNode(
            node,
            (DataObjectPackageTreeNode) node.getChildAt(i),
            de
        );
    }

    /**
     * Gets expansion state of the displayed tree.
     *
     * @return the expansion state
     */
    public Map<TreePath, Boolean> getExpansionState() {
        Map<TreePath, Boolean> expansionState = new HashMap<>();
        TreePath treePath;
        for (int i = 0; i < getRowCount(); i++) {
            try {
                treePath = getPathForRow(i);
            } catch (NullPointerException e) {
                continue;
            }
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
            if (Boolean.TRUE.equals(e.getValue())) expandPath(e.getKey());
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
        TreeExpansionListener[] expansionListeners = getTreeExpansionListeners();
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
        ArchiveUnit childAU;
        ArchiveUnit fatherAU;
        DataObject childDo;
        DataObjectPackageTreeNode childNode;
        DataObjectPackageTreeNode fatherNode;
        childNode = ((DataObjectPackageTreeNode) removePath.getLastPathComponent());
        fatherNode = ((DataObjectPackageTreeNode) removePath.getParentPath().getLastPathComponent());
        childAU = ((DataObjectPackageTreeNode) removePath.getLastPathComponent()).getArchiveUnit();
        fatherAU = ((DataObjectPackageTreeNode) removePath.getParentPath().getLastPathComponent()).getArchiveUnit();

        int auCount = childNode.getAuRecursivCount();
        int ogCount = childNode.getOgRecursivCount();
        if (childNode.getArchiveUnit() != null) auCount++;
        if (childNode.getDataObject() != null) ogCount++;
        if (
            confirmFlag &&
            (UserInteractionDialog.getUserAnswer(
                    ResipGraphicApp.getTheWindow(),
                    "Vous allez effacer " +
                    (auCount == 0 ? "" : auCount + " ArchiveUnits ") +
                    (auCount != 0 && ogCount != 0 ? "et " : "") +
                    (ogCount == 0 ? "" : ogCount + " DataObjectGroups "),
                    "Confirmation",
                    UserInteractionDialog.WARNING_DIALOG,
                    null
                ) !=
                ResipGraphicApp.OK_DIALOG)
        ) return;

        fatherNode.removeChildrenNode(childNode);

        DustbinItem be = new DustbinItem(fatherNode, childNode);
        resetTouchedFromNode(childNode);
        countTouchedFromNode(childNode);
        removeNode(fatherNode, childNode, be);
        // be.removeContentFromDataObjectPackage(fatherAU.getDataObjectPackage());

        if (childAU != null) {
            fatherAU.removeChildArchiveUnit(childAU);
        } else {
            childDo = ((DataObjectPackageTreeNode) removePath.getLastPathComponent()).getDataObject();
            fatherAU.removeDataObjectById(childDo.getInDataObjectPackageId());
        }

        fatherAU.getDataObjectPackage().actualiseIdMaps();
        ((DataObjectPackageTreeModel) getModel()).nodeStructureChanged(fatherNode);
        ResipGraphicApp.getTheApp().currentWork.getCreationContext().setStructureChanged(true);
        ResipGraphicApp.getTheApp().setContextLoaded(true);
        ResipGraphicApp.getTheApp().setModifiedContext(true);
        container.reset();
    }

    /**
     * Adds one new ArchiveUnit under target path.
     *
     * @param targetPath the target path
     */
    public void addArchiveUnit(TreePath targetPath) {
        DataObjectPackageTreeNode targetNode;
        if (targetPath == null) targetNode = (DataObjectPackageTreeNode) getModel().getRoot();
        else targetNode = (DataObjectPackageTreeNode) targetPath.getLastPathComponent();
        DataObjectPackageTreeModel treeModel = (DataObjectPackageTreeModel) getModel();

        ArchiveUnit au = new ArchiveUnit(targetNode.getArchiveUnit().getDataObjectPackage());
        try {
            au.setDefaultContent("Nouvelle ArchiveUnit", "RecordGrp");
        } catch (SEDALibException ignored) {
            // ignored
        }
        targetNode.getArchiveUnit().addChildArchiveUnit(au);

        treeModel.generateArchiveUnitNode(au, targetNode);
        targetNode.actualiseRecursivCounts(1, 0);

        treeModel.nodeStructureChanged(targetNode);
        ResipGraphicApp.getTheApp().currentWork.getCreationContext().setStructureChanged(true);

        ResipGraphicApp.getTheApp().setContextLoaded(true);
        ResipGraphicApp.getTheApp().setModifiedContext(true);
        container.reset();
    }
}
