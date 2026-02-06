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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class Weight.
 * <p>
 * For weight SEDA metadata
 */
public class Weight extends NamedTypeMetadata {

    /**
     * Enum restricted values.
     */
    public static final List<String> enumValues = Arrays.asList("microgram","MC","milligram","MGM","gram","GRM","kilogram","KGM");

    /**
     * The value.
     */
    private Double value;

    /**
     * The linear measure unit.
     */
    private String unit;

    /**
     * Instantiates a new weight.
     *
     * @throws SEDALibException not supposed to occur but compliant with other init
     */
    public Weight() throws SEDALibException {
        this(null, null);
    }

    /**
     * Instantiates a new weight.
     *
     * @param value       the value
     * @throws SEDALibException not supposed to occur but compliant with other init
     */
    public Weight(Double value) throws SEDALibException {
        this(value, null);
    }

    /**
     * Instantiates a new weight.
     *
     * @param value       the value
     * @param unit        the unit
     * @throws SEDALibException if unknown measure unit
     */
    public Weight(Double value, String unit) throws SEDALibException {
        super("Weight");
        this.value = value;
        if ((unit!=null) && !enumValues.contains(unit))
            throw new SEDALibException("["+unit+"] n'est pas une unité de mesure linéraire");
        this.unit = unit;
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
            if (unit != null)
                xmlWriter.writeAttribute("unit", unit);
            if (value!=null)
                xmlWriter.writeCharactersIfNotEmpty(Double.toString(value));
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML dans un élément de type Weight", e);
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
        if (value != null) {
            result.put("", value.toString());
            if (unit != null)
                result.put("attr", "unit=\"" + unit + "\"");
        }
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
                unit = xmlReader.peekAttribute("unit");
                xmlReader.nextUsefullEvent();
                XMLEvent event = xmlReader.nextUsefullEvent();
                if (event.isCharacters()) {
                    value = Double.parseDouble(event.asCharacters().getData());
                    event = xmlReader.nextUsefullEvent();
                } else
                    value = null;
                if ((!event.isEndElement()) || (!elementName.equals(event.asEndElement().getName().getLocalPart())))
                    throw new SEDALibException("Elément " + elementName + " mal terminé");
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément de type Weight", e);
        }
        return true;
    }

    // Getters and setters

    /**
     * Get the unit
     *
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Get the value
     *
     * @return the value
     */
    @Override
    @JsonIgnore(false)
    public Double getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Sets unit.
     *
     * @param unit the unit
     * @throws SEDALibException if unknown measure unit
     */
    public void setUnit(String unit) throws SEDALibException {
        if ((unit!=null) && !enumValues.contains(unit))
            throw new SEDALibException("["+unit+"] n'est pas une unité de mesure de poids");
        this.unit = unit;
    }
}
