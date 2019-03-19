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
import java.util.Arrays;
import java.util.List;

/**
 * The Class CodeKeywordType.
 * <p>
 * For abstract String formatted SEDA metadata
 */
public class CodeKeywordType extends NamedTypeMetadata {

    /** Enum restricted values. */
    static final List<String> enumValues= Arrays.asList("corpname","famname","geogname","name",
            "occupation","persname","subject","genreform","function");

    /** The value. */
    private String value;

    /**
     * Instantiates a new code keyword.
     */
    public CodeKeywordType() {
        this(null);
    }

    /**
     * Instantiates a new code keyword.
     *
     * @param elementName the XML element name
     */
    public CodeKeywordType(String elementName) {
        super(elementName);
    }

    /**
     * Instantiates a new code keyword.
     *
     * @param elementName the XML element name
     * @param value       the value
     */
    public CodeKeywordType(String elementName, String value) throws SEDALibException {
        super(elementName);
        if (enumValues.contains(value))
            this.value = value;
        else
            throw new SEDALibException("Valeur interdite dans un élément [" + elementName + "]");
    }

    /**
     * Instantiates a new code keyword from args.
     *
     * @param elementName the XML element name
     * @param args        the generic args for NameTypeMetadata construction
     * @throws SEDALibException if args are not suitable for constructor
     */
    public CodeKeywordType(String elementName, Object[] args) throws SEDALibException {
        super(elementName);
        if ((args.length == 1) && (args[0] instanceof String)) {
            if (enumValues.contains((String)args[0]))
                this.value = (String)args[0];
            else
                throw new SEDALibException("Valeur interdite dans un élément [" + elementName + "]");
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
            xmlWriter.writeElementValue(elementName, value);
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type CodeKeywordType\n->" + e.getMessage());
        }
    }

    /**
     * Import an element of type CodeKeywordType in XML expected form for the SEDA
     * Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return the read CodeKeywordType
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static CodeKeywordType fromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        CodeKeywordType st;
        try {
            st = new CodeKeywordType();
            XMLEvent event = xmlReader.peekUsefullEvent();
            st.elementName = event.asStartElement().getName().getLocalPart();
            fromSedaXmlInObject(xmlReader, st);
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément CodeKeywordType\n->" + e.getMessage());
        }
        return st;
    }

    /**
     * Import an element of type CodeKeywordType in XML expected form for the SEDA
     * Manifest.
     *
     * @param xmlReader  the SEDAXMLEventReader reading the SEDA manifest
     * @param codeKeywordType the CodeKeywordType to complete
     * @return the read CodeKeywordType
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not
     *                          respected
     */
    public static CodeKeywordType fromSedaXmlInObject(SEDAXMLEventReader xmlReader, CodeKeywordType codeKeywordType)
            throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed(codeKeywordType.elementName)) {
                xmlReader.nextUsefullEvent();
                XMLEvent event = xmlReader.nextUsefullEvent();
                if (event.isCharacters()) {
                    codeKeywordType.value = event.asCharacters().getData();
                    if (!enumValues.contains(codeKeywordType.value))
                        throw new SEDALibException("Valeur interdite dans un élément [" + codeKeywordType.elementName + "]");
                    event = xmlReader.nextUsefullEvent();
                } else
                    codeKeywordType.value = "";
                if ((!event.isEndElement())
                        || (!codeKeywordType.elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + codeKeywordType.elementName + " mal terminé");
            } else
                return null;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type CodeKeywordType\n->" + e.getMessage());
        }
        return codeKeywordType;
    }

    // Getters and setters

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
    public void setValue(String value) throws SEDALibException {
        if (enumValues.contains(value))
            this.value = value;
        else
            throw new SEDALibException("Valeur interdite dans un élément [" + elementName + "]");
    }
}
