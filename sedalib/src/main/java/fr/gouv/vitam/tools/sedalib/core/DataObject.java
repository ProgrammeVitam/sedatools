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
package fr.gouv.vitam.tools.sedalib.core;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

/**
 * The Interface DataObject.
 * <p>
 * Interface for SEDA elements BinaryDataObject, PhysicalDataObject and
 * DataObjectGroup that can be managed the same way in export and import for
 * edition, and linked to ArchiveUnit.
 */
public interface DataObject {

	// SEDA XML exporter

	/**
	 * Export in XML expected form for the SEDA Manifest.
	 *
	 * @param xmlWriter the SEDAXMLStreamWriter generating the SEDA manifest
	 * @param progressLogger the progress logger
	 * @throws InterruptedException if export process is interrupted
	 * @throws SEDALibException     if the XML can't be written
	 */
	void toSedaXml(SEDAXMLStreamWriter xmlWriter, SEDALibProgressLogger progressLogger)
			throws InterruptedException, SEDALibException;

	/**
	 * Export the elements that can be edited without changing the structure. This
	 * is in XML expected form for the SEDA Manifest but in String.
	 * 
	 * @return the XML elements in String format
	 * @throws SEDALibException if the XML can't be written
	 */
	String toSedaXmlFragments() throws SEDALibException;

	// SEDA XML importer

	/**
	 * Import the elements that can be edited without changing the structure. This
	 * is in XML expected form for the SEDA Manifest but in String.
	 * 
	 * @param fragments the XML elements in String format
	 * @throws SEDALibException if the XML can't be read or don't have expected
	 *                          mandatory field - Content
	 */
	void fromSedaXmlFragments(String fragments) throws SEDALibException;

	// Getters and setters

	/**
	 * Gets the id in DataObjectPackage.
	 *
	 * @return the id in DataObjectPackage
	 */
	String getInDataObjectPackageId();

	/**
	 * Gets the DataObjectGroup.
	 *
	 * @return the DataObjectGroup
	 */
	DataObjectGroup getDataObjectGroup();
}
