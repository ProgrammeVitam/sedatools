/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveDeliveryRequestReply the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.LinkedHashMap;

/**
 * The Class TextType.
 * <p>
 * For abstract String formatted with optional language SEDA metadata
 */
public class TextType extends NamedTypeMetadata {

    /**
     * The value.
     */
    private String value;

    /**
     * The lang.
     */
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
                xmlWriter.writeAttribute("xml:lang", lang);
            xmlWriter.writeCharactersIfNotEmpty(value);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type TextType [" + getXmlElementName() + "]", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toCsvList()
     */
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        if (value != null) {
            result.put("", value);
            if (lang != null)
                result.put("attr", "xml:lang=\""+lang+"\"");
        }
        return result;
    }

    /**
     * Import the metadata content in XML expected form from the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return true, if it finds something convenient, false if not
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not respected, for example
     */
    public boolean fillFromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed(elementName)) {
                lang = xmlReader.peekAttribute(XMLConstants.XML_NS_URI,"lang");
                xmlReader.nextUsefullEvent();
                XMLEvent event = xmlReader.nextUsefullEvent();
                if (event.isCharacters()) {
                    value = event.asCharacters().getData();
                    event = xmlReader.nextUsefullEvent();
                } else
                    value = "";
                if ((!event.isEndElement()) || (!elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + elementName + " mal terminé");
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type StringType", e);
        }
        return true;
    }

    // Getters and setters

    /**
     * Get the lang
     *
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Sets lang.
     *
     * @param lang the lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }


}
