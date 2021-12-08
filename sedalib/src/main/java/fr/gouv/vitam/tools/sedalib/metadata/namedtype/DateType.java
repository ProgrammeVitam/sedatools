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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;

/**
 * The Class DateType.
 * <p>
 * For abstract Date formatted SEDA metadata
 */
public class DateType extends NamedTypeMetadata {

    /**
     * The value.
     */
    private LocalDate value;

    /**
     * Instantiates a new string.
     */
    public DateType() {
        this(null, (LocalDate)null);
    }

    /**
     * Instantiates a new string.
     *
     * @param elementName the XML element name
     */
    public DateType(String elementName) {
        this(elementName, (LocalDate)null);
    }

    /**
     * Instantiates a new string.
     *
     * @param elementName the XML element name
     * @param value       the value
     */
    public DateType(String elementName, LocalDate value) {
        super(elementName);
        this.value = value;
    }

    /**
     * Instantiates  a new date from String.
     *
     * @param elementName the XML element name
     * @param dateString   the date string value
     * @throws SEDALibException if wrong date time format
     */
    public DateType(String elementName, String dateString) throws SEDALibException {
        super(elementName);
        try {
            this.value = SEDAXMLEventReader.getDateFromString(dateString);
        }
        catch( DateTimeParseException e) {
            throw new SEDALibException("Problème de formatage de date à la création d'un élément [" + elementName + "]");
        }
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
            if (value == null)
                xmlWriter.writeElementValue(elementName, null);
            else
                xmlWriter.writeElementValue(elementName, SEDAXMLStreamWriter.getStringFromDate(value));
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type DateTimeType [" + getXmlElementName() + "]", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata#toCsvList()
     */
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if (value != null)
            result.put("",SEDAXMLStreamWriter.getStringFromDate(value));
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
        String tmpDate;
        try {
            if (xmlReader.peekBlockIfNamed(elementName)) {
                XMLEvent event = xmlReader.nextUsefullEvent();
                elementName = event.asStartElement().getName().getLocalPart();
                event = xmlReader.nextUsefullEvent();
                if (event.isCharacters()) {
                    tmpDate = event.asCharacters().getData();
                    try {
                        value = SEDAXMLEventReader.getDateFromString(tmpDate);
                    } catch (DateTimeParseException e) {
                        throw new SEDALibException("La date est mal formatée", e);
                    }
                    event = xmlReader.nextUsefullEvent();
                } else
                    value = null;
                if ((!event.isEndElement()) || (!elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + elementName + " mal terminé");
            } else return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type DateTimeType", e);
        }
        return true;
    }

    // Getters and setters

    /**
     * Get the value
     *
     * @return the value
     */
    public LocalDate getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(LocalDate value) {
        this.value = value;
    }
}
