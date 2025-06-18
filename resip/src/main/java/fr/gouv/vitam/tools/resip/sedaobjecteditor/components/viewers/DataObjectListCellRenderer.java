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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;

import javax.swing.*;
import java.awt.*;

import static fr.gouv.vitam.tools.resip.frame.MainWindow.BOLD_LABEL_FONT;
import static fr.gouv.vitam.tools.resip.frame.MainWindow.GENERAL_BACKGROUND;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

// TODO: Auto-generated Javadoc

/**
 * The Class DataObjectListCellRenderer.
 */
public class DataObjectListCellRenderer extends JLabel implements ListCellRenderer<DataObject> {

	/**
	 * Instantiates a new data object list cell renderer.
	 */
	public DataObjectListCellRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends DataObject> list, DataObject value, int index,
			boolean isSelected, boolean cellHasFocus) {

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setFont(BOLD_LABEL_FONT);
		if (value instanceof BinaryDataObject) {
			BinaryDataObject bdo=(BinaryDataObject)value;
			StringType dataObjectVersion=bdo.getMetadataDataObjectVersion();
			setText(bdo.getInDataObjectPackageId()+"-"+(dataObjectVersion==null?translateTag("Unknown"):dataObjectVersion.getValue()));
		}
		else if (value instanceof PhysicalDataObject) {
			PhysicalDataObject pdo=(PhysicalDataObject)value;
			StringType dataObjectVersion=pdo.getMetadataDataObjectVersion();
			setText(pdo.getInDataObjectPackageId()+"-"+(dataObjectVersion==null?translateTag("Unknown"):dataObjectVersion.getValue()));
		}
		return this;
	}
}
