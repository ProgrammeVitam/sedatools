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
package fr.gouv.vitam.tools.sedalib.metadata;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

/**
 * The Class UpdateOperation.
 * <p>
 * Class for SEDA element UpdateOperation.
 * <p>
 * A Management metadata.
 * <p>
 * Standard quote: "Pointeur vers un ArchiveUnit existant dans le système."
 */
public class UpdateOperation extends SEDAMetadata {

	// SEDA elements

	/** The systemId. */
	public String systemId;

	/** The metadata name. */
	public String metadataName;

	/** The metadata value. */
	public String metadataValue;

	// Constructors

	/**
	 * Instantiates a new file info.
	 */
	public UpdateOperation() {
		this.systemId = null;
		this.metadataName = null;
		this.metadataValue = null;
	}

	/**
	 * Instantiates a new update operation with a systemId link.
	 *
	 * @param systemId the system id
	 */
	public UpdateOperation(String systemId) {
		this.systemId = systemId;
		this.metadataName = null;
		this.metadataValue = null;
	}

	/**
	 * Instantiates a new update operation with a metadata name and value link.
	 *
	 * @param metadataName  the metadata name
	 * @param metadataValue the metadata value
	 */
	public UpdateOperation(String metadataName, String metadataValue) {
		this.systemId = null;
		this.metadataName = metadataName;
		this.metadataValue = metadataValue;
	}

	/**
	 * Instantiates a new update from args.
	 *
	 * @param elementName the XML element name (here "UpdateOperation")
	 * @param args        the generic args for UpdateOperation construction
	 * @throws SEDALibException if args are not suitable for constructor
	 */
	public UpdateOperation(String elementName,Object[] args) throws SEDALibException {
		if ((args.length == 1) && (args[0] instanceof String)) {
			this.systemId = (String) args[0];
			this.metadataName = null;
			this.metadataValue = null;
		} else if ((args.length == 2) && (args[0] instanceof String) && (args[1] instanceof String)) {
			this.systemId = null;
			this.metadataName = (String) args[0];
			this.metadataValue = (String) args[1];
		} else
			throw new SEDALibException("Mauvais arguments pour le constructeur de l'élément UpdateOperation");
	}

	/**
	 * Sets the system id.
	 *
	 * @param systemId the new system id
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
		this.metadataName = null;
		this.metadataValue = null;
	}

	/**
	 * Sets the metadata name and value.
	 *
	 * @param metadataName  the metadata name
	 * @param metadataValue the metadata value
	 */
	public void setMetadata(String metadataName, String metadataValue) {
		this.systemId = null;
		this.metadataName = metadataName;
		this.metadataValue = metadataValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#getXmlElementName()
	 */
	@Override
	public String getXmlElementName() {
		return "UpdateOperation";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toSedaXml(fr.gouv.vitam.
	 * tools.sedalib.xml.SEDAXMLStreamWriter)
	 */
	@Override
	public void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
		try {
			xmlWriter.writeStartElement("UpdateOperation");
			if (systemId != null) {
				xmlWriter.writeElementValueIfNotEmpty("SystemId", systemId);
			} else {
				xmlWriter.writeStartElement("ArchiveUnitIdentifierKey");
				xmlWriter.writeElementValueIfNotEmpty("MetadataName", metadataName);
				xmlWriter.writeElementValueIfNotEmpty("MetadataValue", metadataValue);
				xmlWriter.writeEndElement();
			}
			xmlWriter.writeEndElement();
		} catch (XMLStreamException e) {
			throw new SEDALibException("Erreur d'écriture XML dans un élément UpdateOperation\n->" + e.getMessage());
		}
	}

	/**
	 * Import the UpdateOperation in XML expected form for the SEDA Manifest.
	 *
	 * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
	 * @return the read UpdateOperation
	 * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
	 *                          respected
	 */
	public static UpdateOperation fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
		UpdateOperation fi = null;
		try {
			if (xmlReader.nextBlockIfNamed("UpdateOperation")) {
				fi = new UpdateOperation();
				fi.systemId = xmlReader.nextValueIfNamed("SystemId");
				if (fi.systemId == null) {
					if (xmlReader.nextBlockIfNamed("ArchiveUnitIdentifierKey")) {
						fi.metadataName = xmlReader.nextMandatoryValue("MetadataName");
						fi.metadataValue = xmlReader.nextMandatoryValue("MetadataValue");
						xmlReader.endBlockNamed("ArchiveUnitIdentifierKey");
					}
				}
				xmlReader.endBlockNamed("UpdateOperation");
			}
		} catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
			throw new SEDALibException("Erreur de lecture XML dans un élément UpdateOperation\n->" + e.getMessage());
		}
		return fi;
	}
}
