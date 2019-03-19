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
 * The Class IntegerType.
 * <p>
 * For abstract integer formatted SEDA metadata
 */
public class IntegerType extends NamedTypeMetadata {

    /** The value. */
    private long value;

    /**
     * Instantiates a new integer.
     */
    public IntegerType() {
        this(null, 0);
    }

    /**
     * Instantiates a new integer.
     *
     * @param elementName the XML element name
     */
    public IntegerType(String elementName) {
        this(elementName, 0);
    }

    /**
     * Instantiates a new integer.
     *
     * @param elementName the XML element name
     * @param value       the value
     */
    public IntegerType(String elementName, long value) {
        super(elementName);
        this.value = value;
    }

    /**
     * Instantiates a new integer from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public IntegerType(String elementName, Object[] args) throws SEDALibException {
        super(elementName);
        if ((args.length == 1) && (args[0] instanceof Integer)) {
            this.value = (Integer)args[0];
        }
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
        try {
            xmlWriter.writeElementValue(elementName, Long.toString(value));
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type IntegerType\n->" + e.getMessage());
        }
    }

    /**
     * Import an element of type IntegerType in XML expected form for the SEDA
     * Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read IntegerType
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static IntegerType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        IntegerType st;
        try {
            st = new IntegerType();
            XMLEvent event = xmlReader.peekUsefullEvent();
            st.elementName = event.asStartElement().getName().getLocalPart();
            fromSedaXmlInObject(xmlReader, st);
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément IntegerType\n->" + e.getMessage());
        }
        return st;
    }

    /**
     * Import an element of type IntegerType in XML expected form for the SEDA
     * Manifest.
     *
     * @param xmlReader  the SEDAXMLEventReader reading the SEDA manifest
     * @param integerType the IntegerType to complete
     * @return the read IntegerType
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static IntegerType fromSedaXmlInObject(SEDAXMLEventReader xmlReader, IntegerType integerType)
            throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed(integerType.elementName)) {
                xmlReader.nextUsefullEvent();
                XMLEvent event = xmlReader.nextUsefullEvent();
                if (event.isCharacters()) {
                    String tmp = event.asCharacters().getData();
                    integerType.value = Long.parseLong(tmp);
                    event = xmlReader.nextUsefullEvent();
                } else
                    throw new SEDALibException("Erreur de lecture XML dans un élément de type IntegerType qui est vide");
                if ((!event.isEndElement())
                        || (!integerType.elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + integerType.elementName + " mal terminé");
            } else
                return null;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type IntegerType\n->" + e.getMessage());
        }
        return integerType;
    }

    // Getters and setters

    /**
     * Get the value
     *
     * @return the value
     */
    public long getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(long value) {
        this.value = value;
    }
}
