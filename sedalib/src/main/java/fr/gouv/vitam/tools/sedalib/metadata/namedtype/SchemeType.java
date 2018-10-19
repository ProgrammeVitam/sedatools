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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

/**
 * The Class SchemeType.
 * <p>
 * For abstract String formatted with optional scheme details SEDA metadata
 */
public class SchemeType extends NamedTypeMetadata {

    /**
     * The value.
     */
    private String value;

    /**
     * The scheme agency ID.
     */
    private String schemeAgencyID;

    /**
     * The scheme agency name.
     */
    private String schemeAgencyName;

    /**
     * The scheme data URI.
     */
    private String schemeDataURI;

    /**
     * The scheme ID.
     */
    private String schemeID;

    /**
     * The scheme name.
     */
    private String schemeName;

    /**
     * The scheme URI.
     */
    private String schemeURI;

    /**
     * The scheme version ID.
     */
    private String schemeVersionID;

    /**
     * Instantiates a new string with language attribute.
     */
    public SchemeType() {
        this(null);
    }

    /**
     * Instantiates a new string without all schemes attributes.
     *
     * @param elementName the XML element name
     */
    public SchemeType(String elementName) {
        this(elementName, null, null, null, null, null, null, null, null);
    }

    /**
     * Instantiates a new string without all schemes attributes.
     *
     * @param value       the value
     * @param elementName the XML element name
     */
    public SchemeType(String elementName, String value) {
        this(elementName, value, null, null, null, null, null, null, null);
    }

    /**
     * Instantiates a new string with all schemes attributes.
     *
     * @param elementName      the XML element name
     * @param value            the value
     * @param schemeAgencyID   the scheme agency ID
     * @param schemeAgencyName the scheme agency name
     * @param schemeDataURI    the scheme data URI
     * @param schemeID         the scheme ID
     * @param schemeName       the scheme name
     * @param schemeURI        the scheme URI
     * @param schemeVersionID  the scheme version ID
     */
    public SchemeType(String elementName, String value, String schemeAgencyID, String schemeAgencyName,
                      String schemeDataURI, String schemeID, String schemeName, String schemeURI, String schemeVersionID) {
        super(elementName);

        this.value=value;
        this.schemeAgencyID = schemeAgencyID;
        this.schemeAgencyName = schemeAgencyName;
        this.schemeDataURI = schemeDataURI;
        this.schemeID = schemeID;
        this.schemeName = schemeName;
        this.schemeURI = schemeURI;
        this.schemeVersionID = schemeVersionID;
    }

    /**
     * Instantiates a new string with all schemes attributes from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public SchemeType(String elementName, Object[] args) throws SEDALibException {
        super(elementName);
        if ((args.length == 1) && (args[0] instanceof String)) {
            this.value = (String) args[0];
        }
        else if ((args.length == 8) && (args[0] instanceof String) && (args[1] instanceof String)
                && (args[2] instanceof String) && (args[3] instanceof String) && (args[4] instanceof String)
                && (args[5] instanceof String) && (args[6] instanceof String) && (args[7] instanceof String)) {
            this.value = (String) args[0];
            this.schemeAgencyID = (String) args[1];
            this.schemeAgencyName = (String) args[2];
            this.schemeDataURI = (String) args[3];
            this.schemeID = (String) args[4];
            this.schemeName = (String) args[5];
            this.schemeURI = (String) args[6];
            this.schemeVersionID = (String) args[7];
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
            xmlWriter.writeAttributeIfNotEmpty("schemeAgencyID", schemeAgencyID);
            xmlWriter.writeAttributeIfNotEmpty("schemeAgencyName", schemeAgencyName);
            xmlWriter.writeAttributeIfNotEmpty("schemeDataURI", schemeDataURI);
            xmlWriter.writeAttributeIfNotEmpty("schemeID", schemeID);
            xmlWriter.writeAttributeIfNotEmpty("schemeName", schemeName);
            xmlWriter.writeAttributeIfNotEmpty("schemeURI", schemeURI);
            xmlWriter.writeAttributeIfNotEmpty("schemeVersionID", schemeVersionID);
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
    public static SchemeType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        SchemeType st;
        try {
            st = new SchemeType();
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
    public static SchemeType fromSedaXmlInObject(SEDAXMLEventReader xmlReader, SchemeType schemeType)
            throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed(schemeType.elementName)) {
                //XMLEvent event = xmlReader.peekUsefullEvent();
                schemeType.schemeAgencyID = xmlReader.peekAttribute("schemeAgencyID");
                schemeType.schemeAgencyName = xmlReader.peekAttribute("schemeAgencyName");
                schemeType.schemeDataURI = xmlReader.peekAttribute("schemeDataURI");
                schemeType.schemeID = xmlReader.peekAttribute("schemeID");
                schemeType.schemeName = xmlReader.peekAttribute("schemeName");
                schemeType.schemeURI = xmlReader.peekAttribute("schemeURI");
                schemeType.schemeVersionID = xmlReader.peekAttribute("schemeVersionID");
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
     * Gets scheme agency id.
     *
     * @return the scheme agency id
     */
    public String getSchemeAgencyID() {
        return schemeAgencyID;
    }

    /**
     * Sets scheme agency id.
     *
     * @param schemeAgencyID the scheme agency id
     */
    public void setSchemeAgencyID(String schemeAgencyID) {
        this.schemeAgencyID = schemeAgencyID;
    }

    /**
     * Gets scheme agency name.
     *
     * @return the scheme agency name
     */
    public String getSchemeAgencyName() {
        return schemeAgencyName;
    }

    /**
     * Sets scheme agency name.
     *
     * @param schemeAgencyName the scheme agency name
     */
    public void setSchemeAgencyName(String schemeAgencyName) {
        this.schemeAgencyName = schemeAgencyName;
    }

    /**
     * Gets scheme data uri.
     *
     * @return the scheme data uri
     */
    public String getSchemeDataURI() {
        return schemeDataURI;
    }

    /**
     * Sets scheme data uri.
     *
     * @param schemeDataURI the scheme data uri
     */
    public void setSchemeDataURI(String schemeDataURI) {
        this.schemeDataURI = schemeDataURI;
    }

    /**
     * Gets scheme id.
     *
     * @return the scheme id
     */
    public String getSchemeID() {
        return schemeID;
    }

    /**
     * Sets scheme id.
     *
     * @param schemeID the scheme id
     */
    public void setSchemeID(String schemeID) {
        this.schemeID = schemeID;
    }

    /**
     * Gets scheme name.
     *
     * @return the scheme name
     */
    public String getSchemeName() {
        return schemeName;
    }

    /**
     * Sets scheme name.
     *
     * @param schemeName the scheme name
     */
    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    /**
     * Gets scheme uri.
     *
     * @return the scheme uri
     */
    public String getSchemeURI() {
        return schemeURI;
    }

    /**
     * Sets scheme uri.
     *
     * @param schemeURI the scheme uri
     */
    public void setSchemeURI(String schemeURI) {
        this.schemeURI = schemeURI;
    }

    /**
     * Gets scheme version id.
     *
     * @return the scheme version id
     */
    public String getSchemeVersionID() {
        return schemeVersionID;
    }

    /**
     * Sets scheme version id.
     *
     * @param schemeVersionID the scheme version id
     */
    public void setSchemeVersionID(String schemeVersionID) {
        this.schemeVersionID = schemeVersionID;
    }


}
