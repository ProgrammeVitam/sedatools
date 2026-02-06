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
package fr.gouv.vitam.tools.sedalib.metadata.namedtype;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class KeywordType.
 * <p>
 * For abstract Enum formatted SEDA metadata
 */
public class EnumType extends NamedTypeMetadata {

    /**
     * The value.
     */
    private String value;

    /**
     * The values list.
     */
    private String enumValues;

    /**
     * Instantiates a new enum type.
     */
    public EnumType() throws SEDALibException {
        this(null,null);
    }

    /**
     * Instantiates a new enum type.
     *
     * @param elementName the XML element name
     */
    public EnumType(String elementName) throws SEDALibException {
        this(elementName, null);
    }

    /**
     * Instantiates a new enum type.
     *
     * @param elementName the XML element name
     * @param value the value
     * @throws SEDALibException the seda lib exception
     */
    public EnumType(String elementName,String value) throws SEDALibException {
        super(elementName);
        List<String> enumValues= EnumTypeConstants.enumListMap.get(elementName);
        if (enumValues==null)
            throw new SEDALibException("Type Enuméré ["+elementName+"] inconnu");
        if (value==null)
            this.value= "";
        else if (value.isEmpty() || enumValues.contains(value))
            this.value = value;
        else
            throw new SEDALibException("Valeur interdite ["+value+"] dans un élément [" + elementName + "]");
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
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type KeywordType [" + getXmlElementName() + "]", e);
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
        result.put("",value);
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
                    value = event.asCharacters().getData();
                    List<String> enumValues= EnumTypeConstants.enumListMap.get(elementName);
                    if (enumValues==null)
                        throw new SEDALibException("Type Enuméré ["+elementName+"] inconnu");
                    if (!enumValues.contains(value))
                        throw new SEDALibException("Valeur interdite dans un élément [" + elementName + "]");
                    event = xmlReader.nextUsefullEvent();
                } else
                    value = "";
                if ((!event.isEndElement())
                        || (!elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + elementName + " mal terminé");
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type KeywordType", e);
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
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     * @throws SEDALibException the seda lib exception
     */
    public void setValue(String value) throws SEDALibException {
        List<String> enumValues= EnumTypeConstants.enumListMap.get(elementName);
        if (enumValues==null)
            throw new SEDALibException("Type Enuméré ["+elementName+"] inconnu");
        if (value==null)
            this.value= "";
        else if (value.isEmpty() || enumValues.contains(value))
            this.value = value;
        else
            throw new SEDALibException("Valeur interdite dans un élément [" + elementName + "]");
    }
}
