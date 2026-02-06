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
    public static DataFlavor getFlavor() {
        if (flavors == null) try {
            flavors = new DataFlavor[] {
                new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType +
                    ";class=\"" +
                    javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                    "\""
                ),
            };
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
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
        return paths;
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors() {
        if (flavors == null) try {
            flavors = new DataFlavor[] {
                new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType +
                    ";class=\"" +
                    javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                    "\""
                ),
            };
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
