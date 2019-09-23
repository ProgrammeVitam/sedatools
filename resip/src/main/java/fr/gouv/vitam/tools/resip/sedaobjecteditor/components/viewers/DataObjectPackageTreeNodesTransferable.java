/*
 * 
 */
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

// TODO: Auto-generated Javadoc

/**
 * The Class DataObjectPackageTreeNodesTransferable.
 */
public class DataObjectPackageTreeNodesTransferable implements Transferable {

	/**
	 * The paths.
	 */
	TreePath[] paths;

	/**
	 * The flavors.
	 */
	static DataFlavor[] flavors;

	/**
	 * Gets the flavor.
	 *
	 * @return the flavor
	 */
	static public DataFlavor getFlavor(){
		if (flavors==null)
			try {
				flavors = new DataFlavor[] {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
						+ javax.swing.tree.DefaultMutableTreeNode[].class.getName() + "\"")};
			} catch (ClassNotFoundException e) {
				//impossible
			}
		return flavors[0];
	}

	/**
	 * Instantiates a new archive transfer tree nodes transferable.
	 *
	 * @param paths the paths
	 */
	public DataObjectPackageTreeNodesTransferable(TreePath[] paths) {
		this.paths = paths;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	@NotNull
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (!isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);
		return paths;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		if (flavors==null)
			try {
				flavors = new DataFlavor[] {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
						+ javax.swing.tree.DefaultMutableTreeNode[].class.getName() + "\"")};
			} catch (ClassNotFoundException e) {
				//impossible
			}
	return flavors;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return getFlavor().equals(flavor);
	}
}
