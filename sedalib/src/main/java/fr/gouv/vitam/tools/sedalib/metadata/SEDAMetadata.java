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
package fr.gouv.vitam.tools.sedalib.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.gouv.vitam.tools.sedalib.metadata.content.Event;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.Weight;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

/**
 * The Class SEDAMetadata.
 * <p>
 * Abstract class for SEDA element metadata.
 */
//used for only json saved SEDAMetadata
//FileInfo
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = FileInfo.class, name = "FileInfo"),
        @JsonSubTypes.Type(value = AnyXMLListType.class, name = "AnyXMLListType"),
        @JsonSubTypes.Type(value = AnyXMLType.class, name = "AnyXMLType"),
        @JsonSubTypes.Type(value = StringType.class, name = "StringType"),
        @JsonSubTypes.Type(value = TextType.class, name = "TextType"),
        @JsonSubTypes.Type(value = DateTimeType.class, name = "DateTimeType"),
        @JsonSubTypes.Type(value = DateType.class, name = "DateType"),
        @JsonSubTypes.Type(value = DigestType.class, name = "DigestType"),
        @JsonSubTypes.Type(value = IntegerType.class, name = "IntegerType"),
        @JsonSubTypes.Type(value = Event.class, name = "Event"),
        @JsonSubTypes.Type(value = LinearDimensionType.class, name = "LinearDimensionType"),
        @JsonSubTypes.Type(value = Weight.class, name = "Weight")})
public abstract class SEDAMetadata {

    /**
     * Export the metadata in XML expected form for the SEDA Manifest.
     *
     * @param xmlWriter the SEDAXMLStreamWriter generating the SEDA manifest
     * @throws SEDALibException if the XML can't be written
     */
    public abstract void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException;

    /**
     * Export the metadata to csv List for the csv metadata file.
     * <p>
     * In the HashMap result, the key is a metadata path of a leaf and the value is the leaf of the metadata value.
     *
     * @return the linked hash map with header title as key and metadata value as value
     * @throws SEDALibException if the XML can't be written
     */
    public abstract LinkedHashMap<String,String> toCsvList() throws SEDALibException;

    /**
     * Return the indented XML export form as the String representation.
     *
     * @return the indented XML form String
     */
    public String toString() {
        String result = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
            toSedaXml(xmlWriter);
            xmlWriter.flush();
            result = baos.toString("UTF-8");
            if (result.startsWith("\n"))
                result = result.substring(1);
        } catch (XMLStreamException | IOException | SEDALibException e) {
            if (result == null)
                result = super.toString();
        }
        return result;
    }

    /**
     * Fill a SEDAMetadata subtype from SEDA XML content.
     *
     * @param xmlReader the xml reader
     * @return true if the SEDAMetadata has been generated, false if not
     * @throws SEDALibException the seda lib exception
     */
    public abstract boolean fillFromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException;


    /**
     * Return the SEDAMetadata object from an XML event reader.
     *
     * @param xmlReader the xml reader
     * @param target    the target sub-class of SEDAMetadata
     * @return the read SEDAMetadata object
     * @throws SEDALibException if XML read exception or inappropriate sub-class
     */
    public static SEDAMetadata fromSedaXml(SEDAXMLEventReader xmlReader, Class<?> target) throws SEDALibException {
        try {
            boolean needName = target.getName().contains(".namedtype.");
            SEDAMetadata sm;
            if (needName) {
                XMLEvent event = xmlReader.peekUsefullEvent();
                sm = (SEDAMetadata) ConstructorUtils.invokeConstructor(target, event.asStartElement().getName().getLocalPart());
            } else
                sm = (SEDAMetadata) ConstructorUtils.invokeConstructor(target, (Object[])null);
            if (sm.fillFromSedaXml(xmlReader))
                return sm;
            Method method = target.getMethod("fromSedaXml", SEDAXMLEventReader.class);
            return (SEDAMetadata) method.invoke(null, xmlReader);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException
                | SecurityException | InstantiationException e) {
            throw new SEDALibException("Erreur de construction du " + target.getSimpleName(), e);
        } catch (InvocationTargetException te) {
            throw new SEDALibException("Erreur de construction du " + target.getSimpleName(), te.getTargetException());
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type "+target.getSimpleName(), e);
        }
    }

    /**
     * Return the SEDAMetadata object from an XML the String representation.
     *
     * @param xmlData the xml data
     * @param target  the target sub-class of SEDAMetadata
     * @return the SEDAMetadata object
     * @throws SEDALibException if XML read exception or inappropriate sub-class
     */
    public static SEDAMetadata fromString(String xmlData, Class<?> target) throws SEDALibException {
        SEDAMetadata result;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            // jump StartDocument
            xmlReader.nextUsefullEvent();
            result = fromSedaXml(xmlReader, target);
            XMLEvent event = xmlReader.xmlReader.peek();
            if (!event.isEndDocument())
                throw new SEDALibException("Il y a des champs illégaux");
        } catch (XMLStreamException | SEDALibException | IOException e) {
            throw new SEDALibException("Erreur de lecture de " + target.getSimpleName(), e);
        }

        return result;
    }

    /**
     * Gets the xml element name (local form) in SEDA XML messages.
     *
     * @return the xml element name
     */
    @JsonIgnore
    public abstract String getXmlElementName();
}
