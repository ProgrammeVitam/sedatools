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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackageIdElement;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

// TODO: Auto-generated Javadoc

/**
 * The Class DataObjectPackageTreeModel.
 */
public class DataObjectPackageTreeModel extends DefaultTreeModel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 140500100384184333L;

//	/** The in archive transfer ID tree node map. */
//	private HashMap<String, DataObjectPackageTreeNode> inArchiveTransferIDTreeNodeMap;
//
    /** The DataObjectPackageIdElement Treenode map. */
    private HashMap<DataObjectPackageIdElement, DataObjectPackageTreeNode> idElementTreeNodeMap;

    /**
     * Instantiates a new archive transfer tree model.
     *
     * @param root the root
     */
    public DataObjectPackageTreeModel(TreeNode root) {
        super(root, false);
        idElementTreeNodeMap = new HashMap<DataObjectPackageIdElement, DataObjectPackageTreeNode>();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.DefaultTreeModel#getChild(java.lang.Object, int)
     */
    @Override
    public Object getChild(Object parent, int index) {
        return ((DataObjectPackageTreeNode) parent).getChildAt(index);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.DefaultTreeModel#getChildCount(java.lang.Object)
     */
    @Override
    public int getChildCount(Object parent) {
        return ((DataObjectPackageTreeNode) parent).getChildCount();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.tree.DefaultTreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf(Object node) {
        return ((DataObjectPackageTreeNode) node).isLeaf();
    }

    /**
     * Notifies all listeners that have registered interest for notification on this
     * event type. The event instance is lazily created using the parameters passed
     * into the fire method.
     *
     * @param source the source of the {@code TreeModelEvent}; typically
     *               {@code this}
     * @param path   the path to the parent of the structure that has changed; use
     *               {@code null} to identify the root has changed
     */
    public void fireTreeStructureChanged(Object source, Object[] path) {
        super.fireTreeStructureChanged(source, path, null, null);
    }

    /**
     * Generate all JTree nodes for an ArchiveTransfer.
     *
     * @param archiveTransfer the archive transfer
     * @return the archive transfer tree node
     */
    public DataObjectPackageTreeNode generateDataObjectPackageNodes(DataObjectPackage archiveTransfer) {
        DataObjectPackageTreeNode top, node;

        int auRecursivCount = 0;
        int ogRecursivCount = 0;
        top = new DataObjectPackageTreeNode(this, archiveTransfer.getGhostRootAu(), null);
        idElementTreeNodeMap = new HashMap<DataObjectPackageIdElement, DataObjectPackageTreeNode>();
        idElementTreeNodeMap.put(archiveTransfer.getGhostRootAu(), top);

        for (ArchiveUnit au : archiveTransfer.getGhostRootAu().getChildrenAuList().getArchiveUnitList()) {
            node = generateArchiveUnitNode(au, top);
            auRecursivCount += node.getAuRecursivCount() + 1;
            ogRecursivCount += node.getOgRecursivCount() + au.getDataObjectRefList().getCount();
        }
        top.setAuRecursivCount(auRecursivCount);
        top.setOgRecursivCount(ogRecursivCount);

        return top;
    }

    /**
     * Generate JTree Node for an ArchiveUnit.
     *
     * @param archiveUnit the ArchiveUnit
     * @param parent      the parent
     * @return the archive transfer tree node
     */
    public DataObjectPackageTreeNode generateArchiveUnitNode(ArchiveUnit archiveUnit,
                                                             DataObjectPackageTreeNode parent) {
        DataObjectPackageTreeNode node, childNode;
        int auRecursivCount = 0;
        int ogRecursivCount = 0;

        node = findTreeNode(archiveUnit);
        if (node != null) {
            node.addParent(parent);
            parent.actualiseRecursivCounts(node.getAuRecursivCount() + 1,
                    node.getOgRecursivCount() + archiveUnit.getDataObjectRefList().getCount());
        } else {
            node = new DataObjectPackageTreeNode(this, archiveUnit, parent);
            idElementTreeNodeMap.put(archiveUnit, node);
            if (archiveUnit.getChildrenAuList() != null) {
                for (ArchiveUnit au : archiveUnit.getChildrenAuList().getArchiveUnitList()) {
                    childNode = generateArchiveUnitNode(au, node);
                    auRecursivCount += childNode.getAuRecursivCount() + 1;
                    ogRecursivCount += childNode.getOgRecursivCount() + au.getDataObjectRefList().getCount();
                }
            }
            node.setAuRecursivCount(auRecursivCount);
            node.setOgRecursivCount(ogRecursivCount + archiveUnit.getDataObjectRefList().getCount());
            for (DataObject dataObject : archiveUnit.getDataObjectRefList().getDataObjectList())
                generateDataObjectNode(dataObject, node);
            try {
                if (archiveUnit.getContent() != null)
                    node.setTitle(archiveUnit.getContent().getSimpleMetadata("Title"));
            } catch (SEDALibException ignored) {
            }
            if (node.getTitle() == null)
                node.setTitle("Can't interpret Content");
        }
        return node;
    }

    /**
     * Generate JTree Node for a DataObject.
     *
     * @param dataObject the DataObject
     * @param parent     the parent
     */
    public void generateDataObjectNode(DataObject dataObject, DataObjectPackageTreeNode parent) {
        DataObjectPackageTreeNode node;

        //noinspection SuspiciousMethodCalls
        node = idElementTreeNodeMap.get(dataObject);
        if (node != null) {
            node.addParent(parent);
        } else {
            node = new DataObjectPackageTreeNode(this, dataObject, parent);
            if (dataObject instanceof DataObjectPackageIdElement)
                idElementTreeNodeMap.put((DataObjectPackageIdElement) dataObject, node);
        }
    }

    private void oneStepBeyond(DataObjectPackageTreeNode node, List<DataObjectPackageTreeNode> subPath,
                               List<DataObjectPackageTreeNode[]> allPathsList) {
        subPath.add(node);
        if (node == getRoot()) {
            Collections.reverse(subPath);
            allPathsList.add(subPath.toArray(new DataObjectPackageTreeNode[0]));
            Collections.reverse(subPath);
        } else {
            for (DataObjectPackageTreeNode parent : node.getParents())
                oneStepBeyond(parent, subPath, allPathsList);
        }
        subPath.remove(node);
    }

    private List<DataObjectPackageTreeNode[]> getAllPathsToRoot(DataObjectPackageTreeNode node) {
        List<DataObjectPackageTreeNode[]> result = new ArrayList<DataObjectPackageTreeNode[]>();
        List<DataObjectPackageTreeNode> subPath = new ArrayList<DataObjectPackageTreeNode>();
        oneStepBeyond(node, subPath, result);

        return result;
    }

    /**
     * Invoke this method if you've totally changed the children of node and its
     * children's children... This will post a treeStructureChanged event. But as
     * the Tree can represent a DAG the event is fired on all the paths to root
     */
    @Override
    public void nodeStructureChanged(TreeNode node) {
        if (node != null) {
            List<DataObjectPackageTreeNode[]> pathsToRoot = getAllPathsToRoot((DataObjectPackageTreeNode) node);
            for (DataObjectPackageTreeNode[] path : pathsToRoot) {
                fireTreeStructureChanged(this, path);
            }
        }
    }

    /**
     * Reset in archive transfer ID tree node map.
     */
    public void resetInArchiveTransferIDTreeNodeMap() {
        idElementTreeNodeMap = new HashMap<DataObjectPackageIdElement, DataObjectPackageTreeNode>();
    }

    /**
     * Find tree node.
     *
     * @param au the au
     * @return the archive transfer tree node
     */
    public DataObjectPackageTreeNode findTreeNode(ArchiveUnit au) {
        return idElementTreeNodeMap.get(au);
    }

    /**
     * Find tree node.
     *
     * @param dataObject the data object
     * @return the archive transfer tree node
     */
    public DataObjectPackageTreeNode findTreeNode(DataObject dataObject) {
        if (dataObject instanceof DataObjectPackageIdElement)
            return idElementTreeNodeMap.get(dataObject);
        return null;
    }

    /**
     * Adds the id element tree node.
     *
     * @param au       the au
     * @param treeNode the tree node
     */
    public void addIdElementTreeNode(ArchiveUnit au, DataObjectPackageTreeNode treeNode) {
        idElementTreeNodeMap.put(au, treeNode);
    }

    /**
     * Adds the id element tree node.
     *
     * @param dataObject the data object
     * @param treeNode   the tree node
     */
    public void addIdElementTreeNode(DataObject dataObject, DataObjectPackageTreeNode treeNode) {
        if (dataObject instanceof DataObjectPackageIdElement)
            idElementTreeNodeMap.put((DataObjectPackageIdElement) dataObject, treeNode);
    }

    /**
     * Removes the id element tree node.
     *
     * @param au the au
     */
    public void removeIdElementTreeNode(ArchiveUnit au) {
        idElementTreeNodeMap.remove(au);
    }

    /**
     * Removes the id element tree node.
     *
     * @param dataObject the data object
     */
    public void removeIdElementTreeNode(DataObject dataObject) {
        if (dataObject instanceof DataObjectPackageIdElement)
            idElementTreeNodeMap.remove(dataObject);
    }
}
