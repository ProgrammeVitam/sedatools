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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObject;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * The Class DataObjectPackageTreeNode.
 */
public class DataObjectPackageTreeNode implements TreeNode {

    /** The archive unit. */
    private ArchiveUnit archiveUnit;

    /**
     * The data object.
     */
    DataObject dataObject;

    /** The au recursiv count. */
    private int auRecursivCount;

    /** The og recursiv count. */
    private int ogRecursivCount;

    /** The touched counter */
    private int touchedCounter;

    /** The title. */
    private String title;

    /** The parents. */
    private List<DataObjectPackageTreeNode> parents;

    /** The tree model. */
    private DataObjectPackageTreeModel treeModel;

    /**
     * Instantiates a new archive transfer tree node.
     *
     * @param treeModel the tree model
     * @param parent the parent
     */
    private DataObjectPackageTreeNode(DataObjectPackageTreeModel treeModel, DataObjectPackageTreeNode parent) {
        this.archiveUnit = null;
        this.dataObject = null;
        this.auRecursivCount = 0;
        this.ogRecursivCount = 0;
        this.touchedCounter = 0;
        this.parents = new ArrayList<DataObjectPackageTreeNode>();
        if (parent != null)
            this.parents.add(parent);
        this.treeModel = treeModel;
    }

    /**
     * Instantiates a new archive transfer tree node.
     *
     * @param treeModel   the tree model
     * @param archiveUnit the archive unit
     * @param parent      the parent
     */
    public DataObjectPackageTreeNode(DataObjectPackageTreeModel treeModel, ArchiveUnit archiveUnit, DataObjectPackageTreeNode parent) {
        this(treeModel, parent);
        this.archiveUnit = archiveUnit;
        treeModel.addIdElementTreeNode(archiveUnit, this);
    }

    /**
     * Instantiates a new archive transfer tree node.
     *
     * @param treeModel  the tree model
     * @param dataObject the data object
     * @param parent     the parent
     */
    public DataObjectPackageTreeNode(DataObjectPackageTreeModel treeModel, DataObject dataObject, DataObjectPackageTreeNode parent) {
        this(treeModel, parent);
        this.dataObject = dataObject;
        treeModel.addIdElementTreeNode(dataObject, this);
    }

    /**
     * Gets tree model.
     *
     * @return the tree model
     */
    public DataObjectPackageTreeModel getTreeModel() {
        return treeModel;
    }


    /**
     * Gets the data object.
     *
     * @return the data object
     */
    public DataObject getDataObject() {
        return dataObject;
    }

    /**
     * Sets the data object.
     *
     * @param dataObject the new data object
     */
    public void setDataObject(DataObject dataObject) {
        this.dataObject = dataObject;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildAt(int)
     */
    @Override
    @JsonIgnore
    public TreeNode getChildAt(int childIndex) {
        if ((archiveUnit == null) || (childIndex < 0))
            return null;
        int sonAuCount = 0;
        if (archiveUnit.getChildrenAuList() != null)
            sonAuCount = archiveUnit.getChildrenAuList().getArchiveUnitList().size();
        if ((sonAuCount > 0) && (childIndex < sonAuCount))
            return treeModel.findTreeNode(archiveUnit.getChildrenAuList().getArchiveUnitList().get(childIndex));
        else {
            try {
                return treeModel.findTreeNode(archiveUnit.getDataObjectRefList().getDataObjectList().get(childIndex - sonAuCount));
            } catch (Exception e) {
                return null;
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getChildCount()
     */
    @Override
    @JsonIgnore
    public int getChildCount() {
        if (archiveUnit == null)
            return 0;
        if (archiveUnit.getChildrenAuList() == null)
            return 0;
        return archiveUnit.getChildrenAuList().getArchiveUnitList().size()
                + archiveUnit.getDataObjectRefList().getCount();
    }

    /**
     * Gets the au child count.
     *
     * @return the au child count
     */
    @JsonIgnore
    public int getAuChildCount() {
        if (archiveUnit == null)
            return 0;
        if (archiveUnit.getChildrenAuList() == null)
            return 0;
        return archiveUnit.getChildrenAuList().getArchiveUnitList().size();
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getParent()
     */
    @Override
    @JsonIgnore
    public TreeNode getParent() {
        if (parents.isEmpty())
            return null;
        else
            return parents.get(0);
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
     */
    @Override
    @JsonIgnore
    public int getIndex(TreeNode node) {
        if ((archiveUnit == null) || (node == null))
            return -1;
        for (int i = 0; i < archiveUnit.getChildrenAuList().getCount(); i++) {
            if (treeModel.findTreeNode(archiveUnit.getChildrenAuList().getArchiveUnitList().get(i)) == node)
                return i;
        }
        for (int i = 0; i < archiveUnit.getDataObjectRefList().getCount(); i++) {
            if (treeModel.findTreeNode(archiveUnit.getDataObjectRefList().getDataObjectList().get(i)) == node)
                return i + archiveUnit.getChildrenAuList().getCount();
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#getAllowsChildren()
     */
    @Override
    @JsonIgnore
    public boolean getAllowsChildren() {
        return archiveUnit != null;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#isLeaf()
     */
    @Override
    @JsonIgnore
    public boolean isLeaf() {
        return dataObject != null;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeNode#children()
     */
    @Override
    public Enumeration<DataObjectPackageTreeNode> children() {
        if (archiveUnit == null)
            return null;

        List<DataObjectPackageTreeNode> lstn = new ArrayList<DataObjectPackageTreeNode>(
                archiveUnit.getChildrenAuList().getCount() + archiveUnit.getDataObjectRefList().getCount());
        for (int i = 0; i < archiveUnit.getChildrenAuList().getCount(); i++)
            lstn.add(treeModel.findTreeNode(archiveUnit.getChildrenAuList().getArchiveUnitList().get(i)));
        for (int i = 0; i < archiveUnit.getDataObjectRefList().getCount(); i++)
            lstn.add(treeModel.findTreeNode(archiveUnit.getDataObjectRefList().getDataObjectList().get(i)));
        return Collections.enumeration(lstn);
    }

    /**
     * Adds the parent.
     *
     * @param parent the parent
     */
    public void addParent(DataObjectPackageTreeNode parent) {
        if (parent != null)
            parents.add(parent);
    }

    /**
     * Removes the parent.
     *
     * @param parent the parent
     */
    public void removeParent(DataObjectPackageTreeNode parent) {
        if (parent != null)
            parents.remove(parent);
    }

    /**
     * Gets the au recursiv count.
     *
     * @return the au recursiv count
     */
    public int getAuRecursivCount() {
        return auRecursivCount;
    }

    /**
     * Sets the au recursiv count.
     *
     * @param auRecursivCount the new au recursiv count
     */
    public void setAuRecursivCount(int auRecursivCount) {
        this.auRecursivCount = auRecursivCount;
    }

    /**
     * Gets the og recursiv count.
     *
     * @return the og recursiv count
     */
    public int getOgRecursivCount() {
        return ogRecursivCount;
    }

    /**
     * Sets the og recursiv count.
     *
     * @param ogRecursivCount the new og recursiv count
     */
    public void setOgRecursivCount(int ogRecursivCount) {
        this.ogRecursivCount = ogRecursivCount;
    }

    /**
     * Gets the archive unit.
     *
     * @return the archive unit
     */
    public ArchiveUnit getArchiveUnit() {
        return archiveUnit;
    }

    /**
     * Sets the archive unit.
     *
     * @param archiveUnit the new archive unit
     */
    public void setArchiveUnit(ArchiveUnit archiveUnit) {
        this.archiveUnit = archiveUnit;
    }

    /**
     * Actualise recursiv counts.
     *
     * @param auCount the au count
     * @param ogCount the og count
     */
    public void actualiseRecursivCounts(int auCount, int ogCount) {
        setOgRecursivCount(getOgRecursivCount() + ogCount);
        setAuRecursivCount(getAuRecursivCount() + auCount);
        if (parents != null) {
            for (DataObjectPackageTreeNode parent : parents) {
                parent.actualiseRecursivCounts(auCount, ogCount);
            }
        }
    }

    /**
     * Adds the children node.
     *
     * @param childNode the child node
     */
    public void addChildrenNode(DataObjectPackageTreeNode childNode) {
        int auCount, ogCount;
        childNode.addParent(this);
        auCount = childNode.getAuRecursivCount();
        ogCount = childNode.getOgRecursivCount();
        if (childNode.getArchiveUnit() != null)
            auCount++;
        else
            ogCount++;
        actualiseRecursivCounts(auCount, ogCount);
    }

    /**
     * Removes the children node.
     *
     * @param childNode the child node
     */
    public void removeChildrenNode(DataObjectPackageTreeNode childNode) {
        int auCount, ogCount;
        childNode.removeParent(this);
        auCount = childNode.getAuRecursivCount();
        ogCount = childNode.getOgRecursivCount();
        if (childNode.getArchiveUnit() != null)
            auCount++;
        else
            ogCount++;
        actualiseRecursivCounts(-auCount, -ogCount);
    }

    /**
     * Checks if is descendant.
     *
     * @param aAttn the a attn
     * @return true, if is descendant
     */
// Determines if aAttn is a descendant of this TreeNode
    public boolean isDescendant(DataObjectPackageTreeNode aAttn) {
        if (aAttn == this)
            return true;
        for (DataObjectPackageTreeNode parent : aAttn.parents) {
            if (isDescendant(parent))
                return true;
        }
        return false;
    }

    /**
     * Checks for father.
     *
     * @param aAttn the a attn
     * @return true, if successful
     */
// Determines if aAttn is a father of this TreeNode
    public boolean hasFather(DataObjectPackageTreeNode aAttn) {
        for (DataObjectPackageTreeNode parent : parents) {
            if (aAttn.equals(parent))
                return true;
        }
        return false;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets touched counter.
     *
     * @return the touched counter
     */
    public int getTouchedCounter() {
        return touchedCounter;
    }

    /**
     * Sets touched counter.
     *
     * @param touchedCounter the touched counter
     */
    public void setTouchedCounter(int touchedCounter) {
        this.touchedCounter = touchedCounter;
    }

    /**
     * Inc touched counter.
     */
    public void incTouchedCounter() {
        touchedCounter++;
    }

    /**
     * Dec touched counter.
     */
    public void decTouchedCounter() {
        touchedCounter--;
    }

    /**
     * Gets parents.
     *
     * @return the parents
     */
    public List<DataObjectPackageTreeNode> getParents() {
        return parents;
    }

    /**
     * Sets parents.
     *
     * @param parents the parents
     */
    public void setParents(List<DataObjectPackageTreeNode> parents) {
        this.parents = parents;
    }
}
