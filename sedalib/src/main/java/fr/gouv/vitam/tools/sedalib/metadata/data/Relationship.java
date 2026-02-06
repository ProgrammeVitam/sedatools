/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.NamedTypeMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.LinkedHashMap;

/**
 * The Class Relationship.
 * <p>
 * For Relationship empty tag with link annotations.
 */
public class Relationship extends NamedTypeMetadata {
    /**
     * The target reference.
     */
    private String target;

    /**
     * The relationship type.
     */
    private String type;

    /**
     * Instantiates a new Relationship.
     */
    public Relationship() {
        this( null, null);
    }

     /**
     * Instantiates a new Relationship.
     *
     * @param target      the target reference
     */
    public Relationship(String target) {
        this( target, null);
    }

    /**
     * Instantiates a new Relationship.
     *
     * @param target      the target reference
     * @param type        the relationship type
     */
    public Relationship( String target, String type) {
        super("Relationship");
        this.target = target;
        this.type = type;
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
            if (target != null)
                xmlWriter.writeAttribute("xml:target", target);
            if (type != null)
                xmlWriter.writeAttribute("xml:type", type);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type Relationship", e);
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
        if (target != null)
            result.put("attr", "xml:target=\"" + target + "\"");
        if (type != null)
            result.put("attr", "xml:type=\"" + type + "\"");
        return result;
    }

    /**
     * Import the metadata content in XML expected form from the SEDA Manifest.
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @return true, if it finds something convenient, false if not
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is not respected
     */
    public boolean fillFromSedaXml(SEDAXMLEventReader xmlReader) throws SEDALibException {
        try {
            if (xmlReader.peekBlockIfNamed(elementName)) {
                target = xmlReader.peekAttribute(XMLConstants.XML_NS_URI, "target");
                type = xmlReader.peekAttribute(XMLConstants.XML_NS_URI, "type");
                xmlReader.nextUsefullEvent();
                XMLEvent event = xmlReader.nextUsefullEvent();
                if ((!event.isEndElement()) || (!elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + elementName + " mal terminé");
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type TextType", e);
        }
        return true;
    }

    /**
     * Gets the target reference.
     *
     * @return the target reference
     */
    public String getTarget() {
        return target;
    }

    /**
     * Gets the relationship type.
     *
     * @return the relationship type
     */
    public String getType() {
        return type;
    }

     /**
     * Sets the target reference.
     *
     * @param target the target reference to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Sets the relationship type.
     *
     * @param type the relationship type to set
     */
    public void setType(String type) {
        this.type = type;
    }
}