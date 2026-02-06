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

import fr.gouv.vitam.tools.resip.threads.AddThread;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class DataObjectPackageTreeTransferHandler.
 */
public class DataObjectPackageTreeTransferHandler extends TransferHandler {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6282321332303448872L;

	/** The tree. */
	private DataObjectPackageTreeViewer tree;

    /**
     * Instantiates a new archive transfer tree transfer handler.
     *
     * @param tree the tree
     */
    public DataObjectPackageTreeTransferHandler(DataObjectPackageTreeViewer tree) {
		this.tree = tree;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
	 */
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		
		support.setShowDropLocation(true);
		if (!support.isDataFlavorSupported(DataObjectPackageTreeNodesTransferable.getFlavor())
				&& !support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			return false;
		}

		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		TreePath dropPath = dl.getPath();
		DataObjectPackageTreeNode targetNode;
		if (dl.getPath() != null)
			targetNode = (DataObjectPackageTreeNode) dl.getPath().getLastPathComponent();
		else
			targetNode = (DataObjectPackageTreeNode) tree.getModel().getRoot();

		// if drag and drop treenodes
		if (support.isDataFlavorSupported(DataObjectPackageTreeNodesTransferable.getFlavor())) {
			TreePath[] dragPaths;
			try {
				dragPaths = (TreePath[]) support.getTransferable()
						.getTransferData(DataObjectPackageTreeNodesTransferable.getFlavor());
			} catch (UnsupportedFlavorException | IOException e) {
				dragPaths = new TreePath[0];
			}
			// Do not allow a drop on one of drag source father
			for (TreePath dragPath : dragPaths) {
				if (((DataObjectPackageTreeNode) dragPath.getLastPathComponent())
						.hasFather(targetNode))
					return false;
			}		
			// On empty space ok only if not a leaf to drop and one of drag source is not already at rootlevel
			if (dropPath == null) {
				for (TreePath dragPath : dragPaths)
					if (((DataObjectPackageTreeNode) dragPath.getLastPathComponent()).isLeaf())
						return false;
				
				return true;
			}
			// Do not allow drop on leaf
			if (((DataObjectPackageTreeNode) dropPath.getLastPathComponent()).isLeaf())
				return false;
			// Do not allow a drop on a drag source child not only in Jtree structure but in
			// ArchiveUnit DAG structure
			for (TreePath dragPath1 : dragPaths) {
				if (((DataObjectPackageTreeNode) dragPath1.getLastPathComponent())
						.isDescendant((DataObjectPackageTreeNode) dropPath.getLastPathComponent()))
					return false;
			}
			// Do not allow a drop of a leaf on a node which already has a leaf
			ArchiveUnit dropAU = targetNode.getArchiveUnit();
			if ((dropAU != null) && (dropAU.getDataObjectRefList().getCount() > 0)) {
				for (TreePath dragPath : dragPaths) {
					if (((DataObjectPackageTreeNode) dragPath.getLastPathComponent()).isLeaf())
						return false;
				}
			}
		}
		// if drag and drop files
		else {
			// On empty space ok
			if (dropPath == null)
				return true;
			// Do not allow a drop on leaf
			return !((DataObjectPackageTreeNode) dropPath.getLastPathComponent()).isLeaf();
		}
		return true;

	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
	 */
	protected Transferable createTransferable(JComponent c) {
		JTree tree = (JTree) c;
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null)
			return new DataObjectPackageTreeNodesTransferable(paths);
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
	 */
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
	 */
	@SuppressWarnings("unchecked")
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		// Get drop location info.
		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		TreePath target;
		if (dl.getPath() != null)
			target = dl.getPath();
		else {
			Object root=tree.getModel().getRoot();
			if (root==null)
				// tree is empty and must be created by AddThread
				target=null;
			else
				target = new TreePath(tree.getModel().getRoot());
		}

		// Extract transfer data and action
		List<File> files;
		try {
			Transferable t = support.getTransferable();
			if (t.isDataFlavorSupported(DataObjectPackageTreeNodesTransferable.getFlavor())) {
				TreePath[] paths = (TreePath[]) t.getTransferData(DataObjectPackageTreeNodesTransferable.getFlavor());
				for (TreePath path : paths) {
					tree.addLink(path, target);
					if ((support.getDropAction() & MOVE) == MOVE)
						tree.removeSubTree(path,false);
				}
			} else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
				AddThread.launchAddThread(files, target);
			}
		} catch (UnsupportedFlavorException | java.io.IOException e) {
			// forget it
		}
		return true;
	}

}
