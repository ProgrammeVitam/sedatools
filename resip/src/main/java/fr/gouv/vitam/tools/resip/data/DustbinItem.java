/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 *
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.resip.data;

import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.sedalib.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Dustbin item.
 */
public class DustbinItem {
    /**
     * The Former root.
     */
    DataObjectPackageTreeNode formerRoot;
    /**
     * The Node.
     */
    DataObjectPackageTreeNode node;
    /**
     * The Removed archive unit list.
     */
    List<ArchiveUnit> removedArchiveUnitList;
    /**
     * The Removed data object group list.
     */
    List<DataObjectGroup> removedDataObjectGroupList;
    /**
     * The Removed binary data object list.
     */
    List<BinaryDataObject> removedBinaryDataObjectList;
    /**
     * The Removed physical data object list.
     */
    List<PhysicalDataObject> removedPhysicalDataObjectList;

    /**
     * Instantiates a new Dustbin item.
     *
     * @param parentNode the parent node
     * @param node       the node
     */
    public DustbinItem(DataObjectPackageTreeNode parentNode, DataObjectPackageTreeNode node) {
		this.node=node;
		this.formerRoot = parentNode;
		this.removedArchiveUnitList = new ArrayList<ArchiveUnit>();
		this.removedDataObjectGroupList = new ArrayList<DataObjectGroup>();
		this.removedBinaryDataObjectList = new ArrayList<BinaryDataObject>();
		this.removedPhysicalDataObjectList = new ArrayList<PhysicalDataObject>();
	}

    /**
     * Add tree node.
     *
     * @param node the node
     */
    public void addTreeNode(DataObjectPackageTreeNode node) {
		if (node.getArchiveUnit()!=null)
			addArchiveUnit(node.getArchiveUnit());
		else if (node.getDataObject()!=null) {
			DataObject zod=node.getDataObject();
			if (zod instanceof DataObjectGroup)
				addDataObjectGroup((DataObjectGroup)zod);
			//FIXME ne traite les autres DataObject car à terme un TreeNode ne sera qu'un ArchiveUnit ou un DataObjectGroup
		}
	}

    /**
     * Add archive unit.
     *
     * @param archiveUnit the archive unit
     */
    public void addArchiveUnit(ArchiveUnit archiveUnit) {
		removedArchiveUnitList.add(archiveUnit);
	}

    /**
     * Add data object group.
     *
     * @param dataObjectGroup the data object group
     */
    public void addDataObjectGroup(DataObjectGroup dataObjectGroup) {
		removedDataObjectGroupList.add(dataObjectGroup);
		removedBinaryDataObjectList.addAll(dataObjectGroup.getBinaryDataObjectList());
		removedPhysicalDataObjectList.addAll(dataObjectGroup.getPhysicalDataObjectList());
	}

    /**
     * Remove content from data object package.
     *
     * @param at the at
     */
    public void removeContentFromDataObjectPackage(DataObjectPackage at) {
		for (ArchiveUnit au:removedArchiveUnitList)
			at.getAuInDataObjectPackageIdMap().remove(au.getInDataObjectPackageId());
		for (DataObjectGroup dog:removedDataObjectGroupList)
			at.getDogInDataObjectPackageIdMap().remove(dog.getInDataObjectPackageId());
		for (BinaryDataObject bdo:removedBinaryDataObjectList)
			at.getBdoInDataObjectPackageIdMap().remove(bdo.getInDataObjectPackageId());
		for (PhysicalDataObject pdo:removedPhysicalDataObjectList)
			at.getPdoInDataObjectPackageIdMap().remove(pdo.getInDataObjectPackageId());
	}
}
