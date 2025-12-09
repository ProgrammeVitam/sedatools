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
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * The Class ComplexListType.
 * <p>
 * For abstract SEDA metadata composed with list of other metadata or types
 */
public abstract class ComplexListType extends NamedTypeMetadata implements ComplexListInterface {

    /**
     * The Sub type metadata map map by versions.
     */
    protected static Map<Class<?>, Map<SedaVersion, Map<String, ComplexListMetadataKind>>> subTypeMetadataMapsMap = new HashMap<>();

    /**
     * The Sub type expandable map by versions.
     */
    protected static Map<Class<?>, Map<SedaVersion, Boolean>> subTypeNotExpandablesMap = new HashMap<>();

    /**
     * The metadata list.
     */
    protected List<SEDAMetadata> metadataList;

    /**
     * Gets the metadata list.
     *
     * @return the metadata list
     */
    public List<SEDAMetadata> getMetadataList() {
        return metadataList;
    }

    /**
     * Sets the metadata list.
     *
     * @param metadataList the new metadata list
     */
    public void setMetadataList(List<SEDAMetadata> metadataList) {
        this.metadataList = metadataList;
    }

    /**
     * Instantiates a new complex list.
     *
     * @param elementName the element name
     */
    protected ComplexListType(String elementName) {
        super(elementName);
        this.metadataList = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter) throws SEDALibException {
        try {
            xmlWriter.writeStartElement(getXmlElementName());
            toSedaXmlMetadataList(xmlWriter);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException(
                    "Erreur d'écriture XML dans un élément [" + getXmlElementName() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkedHashMap<String, String> toCsvList() throws SEDALibException {
        return ComplexListInterface.super.toCsvList();
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
            if (xmlReader.nextBlockIfNamed(elementName)) {
                fillFromSedaXmlMetadataList(xmlReader);
                xmlReader.endBlockNamed(elementName);
            } else
                return false;
        } catch (XMLStreamException | IllegalArgumentException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML dans un élément [" + elementName + "]", e);
        }
        return true;
    }

    /**
     * Checks if the metadata list is closed.
     *
     * @return true, if is not expendable
     */
    @JsonIgnore
    public boolean isNotExpandable() {
        return ComplexListInterface.isNotExpandable(this.getClass());
    }

    /**
     * Gets the metadata map, which link xml element name with metadata class and
     * cardinality.
     *
     * @return the metadata map
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    @JsonIgnore
    public LinkedHashMap<String, ComplexListMetadataKind> getMetadataMap() throws SEDALibException {
        return (LinkedHashMap<String, ComplexListMetadataKind>) ComplexListInterface.getMetadataMap(this.getClass());
    }

    @JsonIgnore
    public static LinkedHashMap<String, ComplexListMetadataKind> getMetadataMap(Class<?> clazz) throws SEDALibException {
        return (LinkedHashMap<String, ComplexListMetadataKind>) ComplexListInterface.getMetadataMap(clazz);
    }
}
