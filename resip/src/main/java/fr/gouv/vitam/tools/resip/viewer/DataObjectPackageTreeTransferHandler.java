/*
 * 
 */
package fr.gouv.vitam.tools.resip.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;

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
		else
			target = new TreePath(tree.getModel().getRoot());

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
				tree.addNewFiles(files, target);
			}
		} catch (UnsupportedFlavorException | java.io.IOException e) {
			// forget it
		}
		return true;
	}

}
