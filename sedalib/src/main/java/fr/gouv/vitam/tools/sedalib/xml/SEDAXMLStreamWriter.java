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
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.xml;

import com.ctc.wstx.api.WstxOutputProperties;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerFactoryConfigurationError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/**
 * The Class SEDAXMLStreamWriter.
 * <p>
 * Wrapper class for XMLStreamWriter for high level functions used in all SEDA
 * metadata writers and to be able to indent generated XML. It can write either
 * XML document, either XML fragments. It's based on WoodStox STAX objects.
 */
public class SEDAXMLStreamWriter implements AutoCloseable {

    public static final String dayTimePattern = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * The Constant LINEFEED_CHAR.
     */
    private static final String LINEFEED_CHAR = "\n";

    /**
     * The XMLOutputFactory.
     */
    private static XMLOutputFactory xmlof;

    /**
     * The XMLOutputFactory for fragments.
     */
    private static XMLOutputFactory xmlofFragments;

    /**
     * The raw writer.
     */
    private Writer rawWriter;

    /**
     * The xml writer.
     */
    private XMLStreamWriter xmlWriter;

    /**
     * The depth.
     */
    private int depth;

    /**
     * The has child element structure used for indent management.
     */
    private final Map<Integer, Boolean> hasChildElement;

    /**
     * The indent element, elementary brick of indentation (indentLength spaces).
     */
    private String indentElement;

    /**
     * The indent length.
     */
    private int indentLength;

    /**
     * The indent flag.
     */
    private boolean indentFlag;

    /**
     * The first line flag.
     */
    private boolean firstLineFlag;

    // XML objects initialization
    static {
        try {
            xmlof = XMLOutputFactory.newInstance();
            xmlofFragments = XMLOutputFactory.newInstance();
            xmlofFragments.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
        } catch (TransformerFactoryConfigurationError e) {
            System.err.println("Erreur fatale, impossible de créer les outils de manipulation Xml et/ou Droid");
            System.exit(1);
        }
    }

    /**
     * The id counter.
     */
    private int idCounter;

    // constructors

    /**
     * Instantiates a new SEDAXML stream writer.
     *
     * @param os           the os
     * @param indentLength the indent length
     * @throws SEDALibException the SEDALibException
     */
    public SEDAXMLStreamWriter(OutputStream os, int indentLength) throws SEDALibException {
        this(os, indentLength, false);
    }

    /**
     * Instantiates a new SEDAXML stream writer.
     *
     * @param os            the os
     * @param indentLength  the indent length
     * @param isForElements the is for elements
     * @throws SEDALibException the SEDALibException
     */
    public SEDAXMLStreamWriter(OutputStream os, int indentLength, boolean isForElements) throws SEDALibException {
        try {
            // for xml raw writing
            rawWriter = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            // for xml indentend structured writing
            if (isForElements)
                xmlWriter = xmlofFragments.createXMLStreamWriter(rawWriter);
            else
                xmlWriter = xmlof.createXMLStreamWriter(rawWriter);
        } catch (Exception e) {
            throw new SEDALibException("Impossible d'ouvrir un flux d'écriture XML", e);
        }

        this.depth = 0;
        this.hasChildElement = new HashMap<>();
        this.indentElement = StringUtils.repeat(' ', indentLength);
        this.indentLength = indentLength;
        this.indentFlag = (indentLength > 0);
        this.firstLineFlag = true;

        this.idCounter = 1;
    }

    /**
     * Reset id counter.
     */
    void resetIdCounter() {
        idCounter = 1;
    }

    /**
     * Add to the current element an unique ID.
     *
     * @param prefix : attribute name is id if false, attribute name is xml:id if
     *               true
     * @throws XMLStreamException the XML stream exception
     */

    public void setXmlId(boolean prefix) throws XMLStreamException {
        if (prefix)
            xmlWriter.writeAttribute("xml", "xml", "id", "ID" + Integer.toString(idCounter++));
        else
            xmlWriter.writeAttribute("id", "ID" + Integer.toString(idCounter++));

    }

    /**
     * Write an element with only one value.
     *
     * @param element the element
     * @param value   the value
     * @throws XMLStreamException the XML stream exception
     */
    public void writeElementValue(String element, String value) throws XMLStreamException {
        if (value == null) {
            writeEmptyIndent();
            xmlWriter.writeEmptyElement(element);
        } else {
            writeStartIndent();
            xmlWriter.writeStartElement(element);
            xmlWriter.writeCharacters(value);
            writeEndIndent();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Write a Boolean element with only one value.
     *
     * @param element the element
     * @param value   the Boolean value
     * @throws XMLStreamException the XML stream exception
     */
    public void writeBooleanElementValue(String element, Boolean value) throws XMLStreamException {
        if (value == null) {
            writeEmptyIndent();
            xmlWriter.writeEmptyElement(element);
        } else {
            writeStartIndent();
            xmlWriter.writeStartElement(element);
            if (value)
                xmlWriter.writeCharacters("true");
            else
                xmlWriter.writeCharacters("false");
            writeEndIndent();
            xmlWriter.writeEndElement();
        }
    }

    /**
     * Write an element with only one value with default if null or empty.
     *
     * @param element the element
     * @param value   the value
     * @param def     the def
     * @throws XMLStreamException the XML stream exception
     */
    public void writeElementValueWithDefault(String element, String value, String def) throws XMLStreamException {
        if ((value == null) || value.isEmpty()) {
            writeElementValue(element, def);
        } else
            writeElementValue(element, value);

    }

    /**
     * Write an element with only one value if not null or empty.
     *
     * @param element the element
     * @param value   the value
     * @throws XMLStreamException the XML stream exception
     */
    public void writeElementValueIfNotEmpty(String element, String value) throws XMLStreamException {
        if ((value != null) && !value.isEmpty())
            writeElementValue(element, value);
    }

    /**
     * Write a Boolean element with only one value if not null or empty.
     *
     * @param element the element
     * @param value   the Boolean value
     * @throws XMLStreamException the XML stream exception
     */
    public void writeElementValueIfNotEmpty(String element, Boolean value) throws XMLStreamException {
        if (value != null)
            writeBooleanElementValue(element, value);
    }

    /**
     * Get the date and time in XML format "yyyy-MM-dd'T'HH:mm:sszone-offset".
     *
     * @param dateTime the date and time
     * @return a String which contains XML formated date and time of the given date and time
     */

    public static String getStringFromDateTime(LocalDateTime dateTime) {
        LocalDateTime d;
        if (dateTime == null) {
            d = LocalDateTime.now();
        } else {
            d = dateTime;
        }
        return d.format(ISO_DATE_TIME);
    }

    /**
     * Get the date in XML format "yyyy-MM-ddzone-offset".
     *
     * @param date the date
     * @return a String which contains XML formated date of the given date
     */

    public static String getStringFromDate(LocalDate date) {
        LocalDate d;
        if (date == null) {
            d = LocalDate.now();
        } else {
            d = date;
        }
        return d.format(ISO_DATE);
    }

    /**
     * Write a raw xml string.
     *
     * @param rawXml the raw xml
     * @throws XMLStreamException the XML stream exception
     */
    public void writeRawXMLBlockIfNotEmpty(String rawXml) throws XMLStreamException {
        if ((rawXml != null) && !rawXml.isEmpty()) {
            xmlWriter.writeCharacters("");
            xmlWriter.flush();

            try {

                String identXml;
                if (indentFlag) {
                    // indent line by line the raw block
                    try {
                        identXml = IndentXMLTool.getInstance(indentLength).indentString(rawXml);
                        Scanner s = new Scanner(identXml);
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while (s.hasNextLine()) {
                            line = s.nextLine();
                            if (line.trim().startsWith("<"))
                                sb.append(StringUtils.repeat(indentElement, depth));
                            sb.append(line).append('\n');
                        }
                        s.close();
                        if (sb.length() > 1)
                            sb.setLength(sb.length() - 1);
                        identXml = "\n" + sb.toString();
                    } catch (Exception e) {
                        identXml = "\n" + rawXml;
                    }
                } else
                    identXml = rawXml;

                rawWriter.write(identXml);
                rawWriter.flush();
                if ((indentFlag) &&
                    (depth > 0))
                    hasChildElement.put(depth - 1, true);
            } catch (IOException e) {
                throw new XMLStreamException("Erreur d'écriture d'un bloc Raw XML", e);
            }
        }
    }

    /**
     * Gets the xml writer.
     *
     * @return the xml writer
     */
    public XMLStreamWriter getXmlWriter() {
        return xmlWriter;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.stream.XMLStreamWriter#close()
     */
    @Override
    public void close() throws XMLStreamException {
        xmlWriter.close();
    }

    /**
     * Wrap XMLStreamWriter flush.
     *
     * @throws XMLStreamException the XML stream exception
     */
    public void flush() throws XMLStreamException {
        xmlWriter.flush();
    }

    /**
     * Wrap XMLStreamWriter write attribute.
     *
     * @param localName the local name
     * @param value     the value
     * @throws XMLStreamException the XML stream exception
     */
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        xmlWriter.writeAttribute(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI, localName, value);
    }

    /**
     * Write attribute if not empty.
     *
     * @param localName the local name
     * @param value     the value
     * @throws XMLStreamException the XML stream exception
     */
    public void writeAttributeIfNotEmpty(String localName, String value) throws XMLStreamException {
        if ((value != null) && !value.isEmpty())
            writeAttribute(localName, value);
    }

    /**
     * Wrap XMLStreamWriter write attribute.
     *
     * @param prefix       the prefix
     * @param namespaceURI the namespace URI
     * @param localName    the local name
     * @param value        the value
     * @throws XMLStreamException the XML stream exception
     */
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            throws XMLStreamException {
        xmlWriter.writeAttribute(prefix, namespaceURI, localName, value);
    }

    /**
     * Wrap XMLStreamWriter write characters.
     *
     * @param text the text
     * @throws XMLStreamException the XML stream exception
     */
    public void writeCharacters(String text) throws XMLStreamException {
        xmlWriter.writeCharacters(text);
    }

    /**
     * Wrap XMLStreamWriter write characters.
     *
     * @param text the text
     * @throws XMLStreamException the XML stream exception
     */
    public void writeCharactersIfNotEmpty(String text) throws XMLStreamException {
        if ((text != null) && !text.isEmpty())
            xmlWriter.writeCharacters(text);
    }

    /**
     * Wrap XMLStreamWriter write comment.
     *
     * @param data the data
     * @throws XMLStreamException the XML stream exception
     */
    public void writeComment(String data) throws XMLStreamException {
        xmlWriter.writeComment(data);
    }

    /**
     * Wrap XMLStreamWriter write default namespace.
     *
     * @param namespaceURI the namespace URI
     * @throws XMLStreamException the XML stream exception
     */
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        xmlWriter.writeDefaultNamespace(namespaceURI);
    }

    /**
     * Wrap XMLStreamWriter write end document.
     *
     * @throws XMLStreamException the XML stream exception
     */
    public void writeEndDocument() throws XMLStreamException {
        xmlWriter.writeEndDocument();
    }

    /**
     * Wrap XMLStreamWriter write end element.
     *
     * @throws XMLStreamException the XML stream exception
     */
    public void writeEndElement() throws XMLStreamException {
        writeEndIndent();
        xmlWriter.writeEndElement();
    }

    /**
     * Wrap XMLStreamWriter write namespace.
     *
     * @param prefix       the prefix
     * @param namespaceURI the namespace URI
     * @throws XMLStreamException the XML stream exception
     */
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        xmlWriter.writeNamespace(prefix, namespaceURI);
    }

    /**
     * Wrap XMLStreamWriter write start document.
     *
     * @throws XMLStreamException the XML stream exception
     */
    public void writeStartDocument() throws XMLStreamException {
        xmlWriter.writeStartDocument();
    }

    /**
     * Wrap XMLStreamWriter write start element.
     *
     * @param localName the local name
     * @throws XMLStreamException the XML stream exception
     */
    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartIndent();
        xmlWriter.writeStartElement(localName);
    }

    /**
     * Write start indent.
     *
     * @throws XMLStreamException the XML stream exception
     */
    private void writeStartIndent() throws XMLStreamException {
        if (indentFlag) {
            if (depth > 0) {
                hasChildElement.put(depth - 1, true);
            }
            // reset state of current node
            hasChildElement.put(depth, false);
            // indent for current depth
            if (firstLineFlag)
                firstLineFlag = false;
            else
                xmlWriter.writeCharacters(LINEFEED_CHAR);
            xmlWriter.writeCharacters(StringUtils.repeat(indentElement, depth));
            depth++;
        }
    }

    /**
     * Write empty indent.
     *
     * @throws XMLStreamException the XML stream exception
     */
    private void writeEmptyIndent() throws XMLStreamException {
        if (indentFlag) {
            if (depth > 0) {
                hasChildElement.put(depth - 1, true);
            }
            // indent for current depth
            if (firstLineFlag)
                firstLineFlag = false;
            else
                xmlWriter.writeCharacters(LINEFEED_CHAR);
            xmlWriter.writeCharacters(StringUtils.repeat(indentElement, depth));
        }
    }

    /**
     * Write end indent.
     *
     * @throws XMLStreamException the XML stream exception
     */
    private void writeEndIndent() throws XMLStreamException {
        if (indentFlag) {
            depth--;
            if (hasChildElement.get(depth)) {
                xmlWriter.writeCharacters(LINEFEED_CHAR);
                xmlWriter.writeCharacters(StringUtils.repeat(indentElement, depth));
            }
        }
    }

}
