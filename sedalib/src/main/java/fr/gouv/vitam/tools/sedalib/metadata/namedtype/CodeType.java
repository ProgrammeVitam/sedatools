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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * The Class SchemeType.
 * <p>
 * For abstract String formatted with optional scheme details SEDA metadata
 */
public class CodeType extends NamedTypeMetadata {

    /**
     * The value.
     */
    private String value;

    /**
     * The listID.
     */
    private String listID;

    /**
     * The listAgencyID.
     */
    private String listAgencyID;

    /**
     * The listAgencyName.
     */
    private String listAgencyName;

    /**
     * The listName.
     */
    private String listName;

    /**
     * The listVersionID.
     */
    private String listVersionID;

    /**
     * The name.
     */
    private String name;

    /**
     * The languageID.
     */
    private String languageID;

    /**
     * The listURI.
     */
    private String listURI;

    /**
     * The listSchemeURI.
     */
    private String listSchemeURI;

    /**
     * Instantiates a new code type.
     */
    public CodeType() {
        this(null);
    }

    /**
     * Instantiates a new code type without all attributes.
     *
     * @param elementName the XML element name
     */
    public CodeType(String elementName) {
        this(elementName, null, null, null, null, null,
                null, null, null, null, null);
    }

    /**
     * Instantiates a new code type without all attributes.
     *
     * @param value       the value
     * @param elementName the XML element name
     */
    public CodeType(String elementName, String value) {
        this(elementName, value, null, null, null, null,
                null, null, null, null, null);
    }

    /**
     * Instantiates a new code type with all attributes.
     *
     * @param elementName    the XML element name
     * @param value          the value
     * @param listID         the list id attribute
     * @param listAgencyID   the list agency id attribute
     * @param listAgencyName the list agency name attribute
     * @param listName       the list name attribute
     * @param listVersionID  the list version id attribute
     * @param name           the name attribute
     * @param languageID     the language id attribute
     * @param listURI        the list uri attribute
     * @param listSchemeURI  the list scheme uri attribute
     */
    public CodeType(String elementName, String value, String listID, String listAgencyID, String listAgencyName,
                    String listName, String listVersionID, String name, String languageID,
                    String listURI, String listSchemeURI) {
        super(elementName);

        this.value = value;
        this.listID = listID;
        this.listAgencyID = listAgencyID;
        this.listAgencyName = listAgencyName;
        this.listName = listName;
        this.listVersionID = listVersionID;
        this.name = name;
        this.languageID = languageID;
        this.listURI = listURI;
        this.listSchemeURI = listSchemeURI;
    }

    /**
     * Instantiates a new string with all schemes attributes from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public CodeType(String elementName, Object[] args) throws SEDALibException {
        super(elementName);
        if ((args.length == 1) && (args[0] instanceof String)) {
            this.value = (String) args[0];
        } else if ((args.length == 10) && (args[0] instanceof String) && (args[1] instanceof String)
                && (args[2] instanceof String) && (args[3] instanceof String) && (args[4] instanceof String)
                && (args[5] instanceof String) && (args[6] instanceof String) && (args[7] instanceof String)
                && (args[8] instanceof String) && (args[9] instanceof String)) {
            this.value = (String) args[0];
            this.listID = (String) args[1];
            this.listAgencyID = (String) args[2];
            this.listAgencyName = (String) args[3];
            this.listName = (String) args[4];
            this.listVersionID = (String) args[5];
            this.name = (String) args[6];
            this.languageID = (String) args[7];
            this.listURI = (String) args[8];
            this.listSchemeURI = (String) args[9];
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
            xmlWriter.writeAttributeIfNotEmpty("listID", listID);
            xmlWriter.writeAttributeIfNotEmpty("listAgencyID", listAgencyID);
            xmlWriter.writeAttributeIfNotEmpty("listAgencyName", listAgencyName);
            xmlWriter.writeAttributeIfNotEmpty("listName", listName);
            xmlWriter.writeAttributeIfNotEmpty("listVersionID", listVersionID);
            xmlWriter.writeAttributeIfNotEmpty("name", name);
            xmlWriter.writeAttributeIfNotEmpty("languageID", languageID);
            xmlWriter.writeAttributeIfNotEmpty("listURI", listURI);
            xmlWriter.writeAttributeIfNotEmpty("listSchemeURI", listSchemeURI);
            xmlWriter.writeCharacters(value);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type SchemeType\n->" + e.getMessage());
        }
    }

    /**
     * Import the SchemeType in XML expected form for the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read SchemeType
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static CodeType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        CodeType st;
        try {
            st = new CodeType();
            XMLEvent event = xmlReader.peekUsefullEvent();
            st.elementName = event.asStartElement().getName().getLocalPart();
            fromSedaXmlInObject(xmlReader, st);
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément StorageRule\n->" + e.getMessage());
        }
        return st;
    }

    /**
     * Import an element of type SchemeType in XML expected form for the SEDA
     * Manifest.
     *
     * @param xmlReader  the SEDAXMLEventReader reading the SEDA manifest
     * @param schemeType the SchemeType to complete
     * @return the read SchemeType
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static CodeType fromSedaXmlInObject(SEDAXMLEventReader xmlReader, CodeType schemeType)
            throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed(schemeType.elementName)) {
                //XMLEvent event = xmlReader.peekUsefullEvent();
                schemeType.listID = xmlReader.peekAttribute("listID");
                schemeType.listAgencyID = xmlReader.peekAttribute("listAgencyID");
                schemeType.listAgencyName = xmlReader.peekAttribute("listAgencyName");
                schemeType.listName = xmlReader.peekAttribute("listName");
                schemeType.listVersionID = xmlReader.peekAttribute("listVersionID");
                schemeType.name = xmlReader.peekAttribute("name");
                schemeType.languageID = xmlReader.peekAttribute("languageID");
                schemeType.listURI = xmlReader.peekAttribute("listURI");
                schemeType.listSchemeURI = xmlReader.peekAttribute("listSchemeURI");
                xmlReader.nextUsefullEvent();
                XMLEvent event = xmlReader.nextUsefullEvent();
                if (event.isCharacters()) {
                    schemeType.value = event.asCharacters().getData();
                    event = xmlReader.nextUsefullEvent();
                } else
                    schemeType.value = "";
                if ((!event.isEndElement())
                        || (!schemeType.elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + schemeType.elementName + " mal terminé");
            } else
                return null;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type TextType\n->" + e.getMessage());
        }
        return schemeType;
    }

    // Getters and setters

    /**
     * Gets value.
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
     * Gets list id.
     *
     * @return the list id
     */
    public String getListID() {
        return listID;
    }

    /**
     * Sets list id.
     *
     * @param listID the list id
     */
    public void setListID(String listID) {
        this.listID = listID;
    }

    /**
     * Gets list agency id.
     *
     * @return the list agency id
     */
    public String getListAgencyID() {
        return listAgencyID;
    }

    /**
     * Sets list agency id.
     *
     * @param listAgencyID the list agency id
     */
    public void setListAgencyID(String listAgencyID) {
        this.listAgencyID = listAgencyID;
    }

    /**
     * Gets list agency name.
     *
     * @return the list agency name
     */
    public String getListAgencyName() {
        return listAgencyName;
    }

    /**
     * Sets list agency name.
     *
     * @param listAgencyName the list agency name
     */
    public void setListAgencyName(String listAgencyName) {
        this.listAgencyName = listAgencyName;
    }

    /**
     * Gets list name.
     *
     * @return the list name
     */
    public String getListName() {
        return listName;
    }

    /**
     * Sets list name.
     *
     * @param listName the list name
     */
    public void setListName(String listName) {
        this.listName = listName;
    }

    /**
     * Gets list version id.
     *
     * @return the list version id
     */
    public String getListVersionID() {
        return listVersionID;
    }

    /**
     * Sets list version id.
     *
     * @param listVersionID the list version id
     */
    public void setListVersionID(String listVersionID) {
        this.listVersionID = listVersionID;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets language id.
     *
     * @return the language id
     */
    public String getLanguageID() {
        return languageID;
    }

    /**
     * Sets language id.
     *
     * @param languageID the language id
     */
    public void setLanguageID(String languageID) {
        this.languageID = languageID;
    }

    /**
     * Gets list uri.
     *
     * @return the list uri
     */
    public String getListURI() {
        return listURI;
    }

    /**
     * Sets list uri.
     *
     * @param listURI the list uri
     */
    public void setListURI(String listURI) {
        this.listURI = listURI;
    }

    /**
     * Gets list scheme uri.
     *
     * @return the list scheme uri
     */
    public String getListSchemeURI() {
        return listSchemeURI;
    }

    /**
     * Sets list scheme uri.
     *
     * @param listSchemeURI the list scheme uri
     */
    public void setListSchemeURI(String listSchemeURI) {
        this.listSchemeURI = listSchemeURI;
    }
}
