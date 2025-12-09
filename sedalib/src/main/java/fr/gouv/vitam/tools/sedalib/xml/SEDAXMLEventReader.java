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

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.api.WstxOutputProperties;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/**
 * The Class SEDAXMLEventReader.
 * <p>
 * Wrapper class for XMLEventReader for high level functions used in all SEDA
 * metadata readers. It can read either XML document, either XML fragments. It's
 * based on WoodStox STAX objects.
 */
public class SEDAXMLEventReader implements AutoCloseable {

    /** The XMLInputFactory. */
    private static XMLInputFactory xmlif;

    /** The XMLInputFactory for fragments. */
    private static XMLInputFactory xmlifFragments;

    /** The XMLOutputFactory. */
    private static XMLOutputFactory xmlof;

    /** The XMLOutputFactory for fragments. */
    private static XMLOutputFactory xmlofFragments;

    static {
        try {
            xmlif = XMLInputFactory.newInstance();
            xmlifFragments = XMLInputFactory2.newInstance();
            // Warning it's a Woodstox specific mode
            xmlifFragments.setProperty(WstxInputProperties.P_INPUT_PARSING_MODE,
                    WstxInputProperties.PARSING_MODE_FRAGMENT);
            xmlof = XMLOutputFactory.newInstance();
            xmlofFragments = XMLOutputFactory2.newInstance();
            xmlofFragments.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
            // XML objects initialization
        } catch (Exception e) {
            System.err.println("Erreur fatale, impossible de créer les outils de manipulation Xml et/ou Droid");
            System.exit(1);
        }
    }

    /** The xml reader. */
    public XMLEventReader xmlReader;

    /**
     * Gets the named element.
     *
     * @param elementName the element name
     * @param xmlString   the xml string
     * @return the named element
     */
    public static String extractNamedElement(String elementName, String xmlString) {
        String result = null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {

            XMLEvent event = xmlReader.nextUsefullEvent();
            while (true) {
                if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(elementName)) {
                    event = xmlReader.nextUsefullEvent();
                    if (event.isCharacters())
                        result = event.asCharacters().getData();
                    break;
                }
                event = xmlReader.nextUsefullEvent();
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * Gets the fragments of a XML document with a tag element surrounding.
     *
     * @param elementName the element name
     * @param xmlData the XML data String
     * @return the fragments String
     */
    public static String extractFragments(String elementName, String xmlData) {
        StringWriter sw = new StringWriter();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            XMLEvent event = xmlReader.nextUsefullEvent();
            while (true) {
                if (event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(elementName)) {
                    int count = 1;
                    XMLEventWriter xw = xmlofFragments.createXMLEventWriter(sw);

                    while (xmlReader.xmlReader.hasNext()) {
                        event = xmlReader.xmlReader.nextEvent();
                        if (event.isStartElement()
                                && ((StartElement) event).getName().getLocalPart().equals(elementName))
                            count++;
                        else if (event.isEndElement()
                                && ((EndElement) event).getName().getLocalPart().equals(elementName)) {
                            count--;
                            if (count == 0)
                                break;
                        }
                        xw.add(event);
                    }
                    if (xw != null)
                        xw.close();
                    return sw.toString();
                }
                event = xmlReader.nextUsefullEvent();
            }
        } catch (Exception e) {
            return null;
        }
    }

    // Constructors

    /**
     * Instantiates a new SEDAXML event reader.
     *
     * @param is the InputStream
     * @throws SEDALibException if impossible to open the stream
     */
    public SEDAXMLEventReader(InputStream is) throws SEDALibException {
        this(is, false);
    }

    /**
     * Instantiates a new SEDAXML event reader.
     *
     * @param is            the InputStream
     * @param isForElements true, if the reader must be able to read fragements
     *                      (multi root)
     * @throws SEDALibException if impossible to open the stream
     */
    public SEDAXMLEventReader(InputStream is, boolean isForElements) throws SEDALibException {
        InputStreamReader readerFIS = null;
        try {
            readerFIS = new InputStreamReader(is, StandardCharsets.UTF_8);
            if (isForElements)
                xmlReader = xmlifFragments.createXMLEventReader(readerFIS);
            else
                xmlReader = xmlif.createXMLEventReader(readerFIS);
        } catch (Exception e) {
            if (readerFIS != null)
                try {
                    readerFIS.close();
                } catch (IOException e1) {
                    // too bad
                }
            throw new SEDALibException("Impossible d'ouvrir un flux de lecture XML", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.AutoCloseable#close()
     */
    // Methods
    public void close() throws XMLStreamException {
        xmlReader.close();
    }

    /**
     * Get the date and time from XML format "yyyy-MM-dd'T'HH:mm:sszone-offset"
     *
     * @param datetimeString contains XML formated date of the given date
     * @return the date and time
     * @throws DateTimeParseException if not a well formed date and time
     */

    public static LocalDateTime getDateTimeFromString(String datetimeString) throws DateTimeParseException {
        LocalDateTime ldt;
        try {
            ldt = LocalDateTime.parse(datetimeString, ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            ldt = LocalDate.parse(datetimeString, ISO_DATE).atStartOfDay();
        }
        return ldt;
    }

    /**
     * Get the date from XML format "yyyy-MM-ddzone-offset"
     *
     * @param dateString contains XML formated date of the given date
     * @return the date and time
     * @throws DateTimeParseException if not a well formed date
     */

    public static LocalDate getDateFromString(String dateString) throws DateTimeParseException {
        return LocalDate.parse(dateString, ISO_DATE);
    }

    /**
     * Peek name.
     *
     * @return the name
     * @throws XMLStreamException the XML stream exception
     */
    public String peekName() throws XMLStreamException {
        XMLEvent event = peekUsefullEvent();

        if (!event.isStartElement())
            return null;
        return event.asStartElement().getName().getLocalPart();
    }

    /**
     * Peek usefull event.
     *
     * @return the XML event
     * @throws XMLStreamException the XML stream exception
     */
    public XMLEvent peekUsefullEvent() throws XMLStreamException {
        XMLEvent result;

        result = xmlReader.peek();
        while ((result.getEventType() == XMLEvent.COMMENT)
                || (result.isCharacters() && result.asCharacters().isWhiteSpace())) {
            xmlReader.nextEvent();
            result = xmlReader.peek();
        }
        return result;
    }

    /**
     * Peek attribute.
     *
     * @param attribute the attribute
     * @return the attribute string value, or null if the "attribute" isn't set
     * @throws XMLStreamException the XML stream exception
     */
    public String peekAttribute(String attribute) throws XMLStreamException {
        XMLEvent peek = peekUsefullEvent();
        String result = null;

        if (peek.isStartElement()) {
            Attribute a = peek.asStartElement().getAttributeByName(new QName(attribute));
            if (a != null)
                result = a.getValue();
        }
        return result;
    }

    /**
     * Peek attribute.
     *
     * @param namespace the namespace
     * @param attribute the attribute
     * @return the attribute string value, or null if the "attribute" isn't set
     * @throws XMLStreamException the XML stream exception
     */
    public String peekAttribute(String namespace, String attribute) throws XMLStreamException {
        XMLEvent peek = peekUsefullEvent();
        String result = null;

        if (peek.isStartElement()) {
            Attribute a = peek.asStartElement().getAttributeByName(new QName(namespace, attribute));
            if (a != null)
                result = a.getValue();
        }
        return result;
    }

    /**
     * Next usefull event.
     *
     * @return the XML event
     * @throws XMLStreamException the XML stream exception
     */
    public XMLEvent nextUsefullEvent() throws XMLStreamException {
        XMLEvent result;

        result = xmlReader.nextEvent();
        while ((result.getEventType() == XMLEvent.COMMENT)
                || (result.isCharacters() && result.asCharacters().isWhiteSpace()))
            result = xmlReader.nextEvent();
        return result;
    }

    /**
     * Next block if named.
     *
     * @param tag the tag
     * @return true, if successful
     * @throws XMLStreamException the XML stream exception
     */
    public boolean nextBlockIfNamed(String tag) throws XMLStreamException {
        XMLEvent peek = peekUsefullEvent();

        if (!peek.isStartElement())
            return false;
        if (!tag.equals(peek.asStartElement().getName().getLocalPart()))
            return false;
        nextUsefullEvent();
        return true;
    }

    /**
     * Peek block if named.
     *
     * @param tag the tag
     * @return true, if successful
     * @throws XMLStreamException the XML stream exception
     */
    public boolean peekBlockIfNamed(String tag) throws XMLStreamException {
        XMLEvent peek = peekUsefullEvent();

        if (!peek.isStartElement())
            return false;
        return tag.equals(peek.asStartElement().getName().getLocalPart());
    }

    /**
     * Peek attribute block if named.
     *
     * @param tag       the tag
     * @param attribute the attribute
     * @return the attribute string value, or null if next element is not the "tag"
     *         element or if the "attribute" isn't set
     * @throws XMLStreamException the XML stream exception
     */
    public String peekAttributeBlockIfNamed(String tag, String attribute) throws XMLStreamException {
        XMLEvent peek = peekUsefullEvent();

        if (!peek.isStartElement()) return null;
        if (!tag.equals(peek.asStartElement().getName().getLocalPart())) return null;

        final QName qName = new QName(attribute);
        final StartElement startElement = peek.asStartElement();
        final Attribute attr = startElement.getAttributeByName(qName);

        return Optional.ofNullable(attr).map(Attribute::getValue).orElse(null);
    }

    /**
     * End block named.
     *
     * @param tag the tag
     * @throws XMLStreamException the XML stream exception
     * @throws SEDALibException   if "tag" element is ended here
     */
    public void endBlockNamed(String tag) throws XMLStreamException, SEDALibException {
        XMLEvent event = nextUsefullEvent();

        if (!event.isEndElement())
            throw new SEDALibException("Elément " + tag + " mal terminé");
        if (!tag.equals(event.asEndElement().getName().getLocalPart()))
            throw new SEDALibException("Elément " + tag + " mal terminé");
    }

    /**
     * Next value if named.
     *
     * @param tag the tag
     * @return the value String for the "tag" element, or null if next element is
     *         not the "tag" element
     * @throws XMLStreamException the XML stream exception
     * @throws SEDALibException   if "tag" element is badly formed
     */
    public String nextValueIfNamed(String tag) throws XMLStreamException, SEDALibException {
        XMLEvent event;
        String result = null;

        if (nextBlockIfNamed(tag)) {
            event = nextUsefullEvent();
            if (event.isCharacters()) {
                result = event.asCharacters().getData();
                event = nextUsefullEvent();
            }
            if (!event.isEndElement() || !event.asEndElement().getName().getLocalPart().equals(tag))
                throw new SEDALibException("Elément " + tag + " mal formé");
            if (result == null)
                result = "";
        }
        return result;
    }

    /**
     * Next Date value if named.
     *
     * @param tag the tag
     * @return the value LocalDateTime for the "tag" element, or null if next element is not
     *         the "tag" element
     * @throws XMLStreamException the XML stream exception
     * @throws SEDALibException   if "tag" element is badly formed
     */
    public LocalDateTime nextDateValueIfNamed(String tag) throws XMLStreamException, SEDALibException {
        XMLEvent event;
        String tmp = null;
        LocalDateTime result = null;

        if (nextBlockIfNamed(tag)) {
            event = nextUsefullEvent();
            if (event.isCharacters()) {
                tmp = event.asCharacters().getData();
                event = nextUsefullEvent();
            }
            if (!event.isEndElement() || !event.asEndElement().getName().getLocalPart().equals(tag) || (tmp == null))
                throw new SEDALibException("Elément date " + tag + " mal formé");
            try {
                result = getDateTimeFromString(tmp);
            } catch (DateTimeParseException e) {
                throw new SEDALibException("Valeur non interprétable [" + tmp + "] dans l'élément date " + tag, e);
            }
        }
        return result;
    }

    /**
     * Next Boolean value if named.
     *
     * @param tag the tag
     * @return the value Boolean for the "tag" element, or null if next element is
     *         not the "tag" element
     * @throws XMLStreamException the XML stream exception
     * @throws SEDALibException   the SEDALibException
     */
    public Boolean nextBooleanValueIfNamed(String tag) throws XMLStreamException, SEDALibException {
        XMLEvent event;
        String tmp = null;
        Boolean result = null;

        if (nextBlockIfNamed(tag)) {
            event = nextUsefullEvent();
            if (event.isCharacters()) {
                tmp = event.asCharacters().getData();
                event = nextUsefullEvent();
            }
            if (!event.isEndElement() || !event.asEndElement().getName().getLocalPart().equals(tag) || (tmp == null))
                throw new SEDALibException("Elément booléen " + tag + " mal formé");
            switch (tmp) {
                case "true":
                case "1":
                    result = true;
                    break;
                case "false":
                case "0":
                    result = false;
                    break;
                default:
                    throw new SEDALibException("Valeur interdite [" + tmp + "] dans l'élément booléen " + tag);
            }
        }
        return result;
    }

    /**
     * Next mandatory value.
     *
     * @param tag the tag
     * @return the value String for the "tag" element, or null if next element is
     *         not the "tag" element
     * @throws XMLStreamException the XML stream exception
     * @throws SEDALibException   the SEDALibException
     */
    public String nextMandatoryValue(String tag) throws XMLStreamException, SEDALibException {
        if (!peekBlockIfNamed(tag))
            throw new SEDALibException("Element " + tag + " non trouvé");
        return nextValueIfNamed(tag);
    }

    /**
     * Next block as string if named.
     *
     * @param tag the tag
     * @return the String containing all the "tag" element (with begin and end
     *         tags), or null if next element is not the "tag" element
     * @throws XMLStreamException the XML stream exception
     */
    public String nextBlockAsStringIfNamed(String tag) throws XMLStreamException {
        XMLEvent event = peekUsefullEvent();

        if (event.isStartElement() && tag.equals(event.asStartElement().getName().getLocalPart())) {
            int count = 0;
            StringWriter sw = new StringWriter();
            XMLEventWriter xw = xmlof.createXMLEventWriter(sw);

            while (xmlReader.hasNext()) {
                event = xmlReader.nextEvent();
                xw.add(event);
                if (event.isStartElement() && ((StartElement) event).getName().getLocalPart().equals(tag))
                    count++;
                else if (event.isEndElement() && ((EndElement) event).getName().getLocalPart().equals(tag)) {
                    count--;
                    if (count == 0)
                        break;
                }
            }
            if (xw != null)
                xw.close();
            return sw.toString();
        } else
            return null;
    }

    /**
     * Next mandatory block as string.
     *
     * @param tag the tag
     * @return the String containing all the "tag" element (with begin and end
     *         tags), or null if next element is not the "tag" element
     * @throws XMLStreamException the XML stream exception
     * @throws SEDALibException   if the next block is not a "tag" element
     */
    public String nextMandatoryBlockAsString(String tag) throws XMLStreamException, SEDALibException {
        if (!peekBlockIfNamed(tag))
            throw new SEDALibException("Elément " + tag + " non trouvé");
        return nextBlockAsStringIfNamed(tag);
    }
}
