/**
RegisteredDate * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

/**
 * The Class GenericXMLBlockType.
 * <p>
 * For abstract not determined SEDA metadata, take all the xml block
 */
public class GenericXMLBlockType extends NamedTypeMetadata {

	/** The raw xml. */
	private String rawXml;

	/**
	 * Instantiates a new XML block type.
	 */
	public GenericXMLBlockType() {
		this(null, (String) null);
	}

	/**
	 * Instantiates a new XML block type.
	 *
	 * @param elementName the XML element name
	 */
	public GenericXMLBlockType(String elementName) {
		this(elementName, (String) null);
	}

	/**
	 * Instantiates a new XML block type.
	 *
	 * @param elementName the XML element name
	 * @param rawXml       the raw Xml String
	 */
	public GenericXMLBlockType(String elementName, String rawXml) {
		super(elementName);
		this.rawXml = rawXml;
	}

	/**
	 * Instantiates a new string.
	 *
	 * @param elementName the XML element name
	 * @param args        the generic args for NameTypeMetadata construction
	 * @throws SEDALibException if args are not suitable for constructor
	 */
	public GenericXMLBlockType(String elementName, Object[] args) throws SEDALibException {
		super(elementName);
		if ((args.length == 1) && (args[0] instanceof String)) {
			this.rawXml = (String) args[0];
		} else
			throw new SEDALibException("Mauvais arguments pour le constructeur de l'élément [" + elementName + "]");
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
			xmlWriter.writeRawXMLBlockIfNotEmpty(rawXml);
		} catch (XMLStreamException e) {
			throw new SEDALibException("Erreur d'écriture XML dans un élément de type TextType\n->" + e.getMessage());
		}
	}

	/**
	 * Import an element of type TextType in XML expected form for the SEDA
	 * Manifest.
	 *
	 * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
	 * @return the read TextType
	 * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
	 *                          respected
	 */
	public static GenericXMLBlockType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
		GenericXMLBlockType xbt;
		try {
			xbt = new GenericXMLBlockType();
			XMLEvent event = xmlReader.peekUsefullEvent();
			xbt.elementName = event.asStartElement().getName().getLocalPart();
			xbt.rawXml = xmlReader.nextBlockAsStringIfNamed(xbt.elementName);
		} catch (XMLStreamException | IllegalArgumentException e) {
			throw new SEDALibException(
					"Erreur de lecture XML dans un élément de type GenericXMLBlockType\n->" + e.getMessage());
		}
		return xbt;
	}

	// Getters and setters

	/**
	 * Gets raw xml.
	 *
	 * @return the raw xml
	 */
	public String getRawXml() {
		return rawXml;
	}

	/**
	 * Sets raw xml.
	 *
	 * @param rawXml the raw xml
	 */
	public void setRawXml(String rawXml) {
		this.rawXml = rawXml;
	}
}
