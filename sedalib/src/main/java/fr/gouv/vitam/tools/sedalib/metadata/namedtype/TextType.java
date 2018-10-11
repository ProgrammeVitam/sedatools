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
 * The Class TextType.
 * <p>
 * For abstract String formatted with optional language SEDA metadata
 */
public class TextType extends NamedTypeMetadata {

	/** The value. */
	private String value;
	
	/** The lang. */
	private String lang;

	/**
	 * Instantiates a new string with language attribute.
	 */
	public TextType() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new string with language attribute.
	 *
	 * @param elementName the XML element name
	 */
	public TextType(String elementName) {
		this(elementName, null, null);
	}

	/**
	 * Instantiates a new string with language attribute.
	 *
	 * @param elementName the XML element name
	 * @param value       the value
	 */
	public TextType(String elementName, String value) {
		this(elementName, value, null);
	}

	/**
	 * Instantiates a new string with language attribute.
	 *
	 * @param elementName the XML element name
	 * @param value       the value
	 * @param lang        the language
	 */
	public TextType(String elementName, String value, String lang) {
		super(elementName);
		this.value = value;
		this.lang = lang;
	}

	/**
	 * Instantiates a new string with language attribute from args.
	 *
	 * @param elementName the XML element name
	 * @param args        the generic args for NameTypeMetadata construction
	 * @throws SEDALibException if args are not suitable for constructor
	 */
	public TextType(String elementName, Object[] args) throws SEDALibException {
		super(elementName);
		if ((args.length == 1) && (args[0] instanceof String)) {
			this.value = (String) args[0];
			this.lang = null;
		} else if ((args.length == 2) && (args[0] instanceof String) && (args[1] instanceof String)) {
			this.value = (String) args[0];
			this.lang = (String) args[1];
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
			xmlWriter.writeStartElement(elementName);
			if (lang != null)
				xmlWriter.writeAttribute("xml", "xml", "lang", lang);
			xmlWriter.writeCharacters(value);
			xmlWriter.writeEndElement();
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
	public static TextType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
		TextType tt;
		try {
			tt = new TextType();
			XMLEvent event = xmlReader.peekUsefullEvent();
			tt.elementName = event.asStartElement().getName().getLocalPart();
			tt.lang=xmlReader.peekAttribute("http://www.w3.org/XML/1998/namespace","lang");
			xmlReader.nextUsefullEvent();
			event = xmlReader.nextUsefullEvent();
			if (event.isCharacters()) {
				tt.value = event.asCharacters().getData();
				event=xmlReader.nextUsefullEvent();
			} else
				tt.value = "";
			if ((!event.isEndElement()) || (!tt.elementName.equals(event.asEndElement().getName().getLocalPart())))
				throw new SEDALibException("Elément " + tt.elementName + " mal terminé");
		} catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
			throw new SEDALibException("Erreur de lecture XML dans un élément de type TextType\n->" + e.getMessage());
		}
		return tt;
	}
}
