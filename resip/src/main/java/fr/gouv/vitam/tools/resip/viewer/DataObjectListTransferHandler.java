/*
 * 
 */
package fr.gouv.vitam.tools.resip.viewer;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

// TODO: Auto-generated Javadoc

/**
 * The Class DataObjectListTransferHandler.
 */
class DataObjectListTransferHandler extends TransferHandler {
	 
 	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5040874596496847186L;
	
	/** The list. */
	private DataObjectListViewer list;

    /**
     * Instantiates a new data object list transfer handler.
     *
     * @param list the list
     */
    public DataObjectListTransferHandler(DataObjectListViewer list) {
	      this.list = list;
	   }

	   /* (non-Javadoc)
   	 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
   	 */
   	public int getSourceActions(JComponent c) {
	      return COPY_OR_MOVE;
	   }

	   /* (non-Javadoc)
   	 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
   	 */
   	public boolean canImport(TransferSupport ts) {
	      return ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor) && (ResipGraphicApp.getTheApp().mainWindow.dataObjectPackageTreeItemDisplayed!=null);
	   }

	   /* (non-Javadoc)
   	 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
   	 */
   	@SuppressWarnings("unchecked")
	public boolean importData(TransferSupport ts) {
			if (!canImport(ts)) {
				return false;
			}

	      try {
	         List<File> files = (List<File>) ts.getTransferable().getTransferData(
	               DataFlavor.javaFileListFlavor);
	         if (files.size() < 1) {
	            return false;
	         }

	         for (File file : files)  {
	        	 if (file.isDirectory()) {
	        		 UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow,
							 "Il n'est pas possible de mettre\nen objet un répertoire",
							 "Avertissement", UserInteractionDialog.IMPORTANT_DIALOG,
							 null);
	        		 return false;
	        	 }
	         }
	         
	         for (File file : files) {
	            list.addFileDataObject(file.toPath());
	         }

	         return true;

	      } catch (UnsupportedFlavorException | IOException e) {
	         return false;
	      }
	   }
	}