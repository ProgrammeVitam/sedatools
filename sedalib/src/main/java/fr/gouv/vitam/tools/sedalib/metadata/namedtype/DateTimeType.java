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
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * The Class StringType.
 * <p>
 * For abstract String formatted SEDA metadata
 */
public class DateTimeType extends NamedTypeMetadata {

    /**
     * The value.
     */
    private LocalDateTime value;

    /**
     * Instantiates a new string.
     */
    public DateTimeType() {
        this(null, (LocalDateTime) null);
    }

    /**
     * Instantiates a new string.
     *
     * @param elementName the XML element name
     */
    public DateTimeType(String elementName) {
        this(elementName, (LocalDateTime) null);
    }

    /**
     * Instantiates a new string.
     *
     * @param elementName the XML element name
     * @param value       the value
     */
    public DateTimeType(String elementName, LocalDateTime value) {
        super(elementName);
        this.value = value;
    }

    /**
     * Instantiates a new string from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public DateTimeType(String elementName, Object[] args) throws SEDALibException {
        super(elementName);
        if ((args.length == 1) && (args[0] instanceof LocalDateTime))
            this.value = (LocalDateTime) args[0];
        else
            throw new SEDALibException("Mauvais constructeur de l'élément [" + elementName + "]");
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
        String tmpDate;
        try {
            if (value == null)
                xmlWriter.writeElementValue(elementName, null);
            else
                xmlWriter.writeElementValue(elementName, xmlWriter.getStringFromDateTime(value));
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type DateTimeType\n->" + e.getMessage());
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
    public static DateTimeType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        DateTimeType st;
        String tmpDate;
        try {
            st = new DateTimeType();
            XMLEvent event = xmlReader.nextUsefullEvent();
            st.elementName = event.asStartElement().getName().getLocalPart();
            event = xmlReader.nextUsefullEvent();
            if (event.isCharacters()) {
                tmpDate = event.asCharacters().getData();
                try {
                    st.value = xmlReader.getDateTimeFromString(tmpDate);
                } catch (ParseException e) {
                    throw new SEDALibException("La date est mal formatée");
                }
                event = xmlReader.nextUsefullEvent();
            } else
                st.value = null;
            if ((!event.isEndElement()) || (!st.elementName.equals(event.asEndElement().getName().getLocalPart())))
                throw new SEDALibException("Elément " + st.elementName + " mal terminé");
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type StringType\n->" + e.getMessage());
        }
        return st;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    public LocalDateTime getValue() {
         return value;
    }
}
