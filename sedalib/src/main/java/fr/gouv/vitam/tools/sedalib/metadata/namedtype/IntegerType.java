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

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.LinkedHashMap;

/**
 * The Class IntegerType.
 * <p>
 * For abstract integer formatted SEDA metadata
 */
public class IntegerType extends NamedTypeMetadata {

    /**
     * The value.
     */
    private Long value;

    /**
     * Instantiates a new integer.
     */
    public IntegerType() {
        this(null, (Long)null);
    }

    /**
     * Instantiates a new integer.
     *
     * @param elementName the XML element name
     */
    public IntegerType(String elementName) {
        this(elementName, (Long)null);
    }

    /**
     * Instantiates a new integer.
     *
     * @param elementName the XML element name
     * @param value       the value
     */
    public IntegerType(String elementName, Long value) {
        super(elementName);
        this.value = value;
    }

    /**
     * Instantiates a new integer.
     *
     * @param elementName the XML element name
     * @param value       the value
     */
    public IntegerType(String elementName, int value) {
        super(elementName);
        this.value = ((long)value);
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
            this.value = (Long) args[0];
        } else
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
            xmlWriter.writeElementValue(elementName, (value==null?"":Long.toString(value)));
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type IntegerType [" + getXmlElementName() + "]", e);
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
        result.put("", (value==null?"":Long.toString(value)));
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
                xmlReader.nextUsefullEvent();
                XMLEvent event = xmlReader.nextUsefullEvent();
                if (event.isCharacters()) {
                    String tmp = event.asCharacters().getData();
                    if ((tmp == null) || tmp.isEmpty())
                        value = null;
                    else
                        value = Long.parseLong(tmp);
                    event = xmlReader.nextUsefullEvent();
                } else
                    throw new SEDALibException("Erreur de lecture XML dans un élément de type IntegerType qui est vide");
                if ((!event.isEndElement())
                        || (!elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + elementName + " mal terminé");
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type IntegerType", e);
        }
        return true;
    }

    // Getters and setters

    /**
     * Get the value
     *
     * @return the value
     */
    @Override
    @JsonIgnore(false)
    public Long getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(Long value) {
        this.value = value;
    }
}
