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
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
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
    protected static HashMap<Class<?>, LinkedHashMap<String, ComplexListMetadataKind>[]> subTypeMetadataMapsMap =
            new HashMap<>();

    /**
     * The Sub type expandable map by versions.
     */
    protected static HashMap<Class<?>, Boolean[]> subTypeNotExpandablesMap = new HashMap<>();

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
        Class<?> metadataClass;
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
     * Creates new metadata map and expandable arrays for a ComplexListType subclass.
     * Initializes them by calling ComplexListInterface.initMetadataMaps() and stores
     * them in the static maps.
     *
     * @param subClass the ComplexListType subclass to initialize
     */
    private static void getNewComplexListSubType(Class<?> subClass) {
        LinkedHashMap<String, ComplexListMetadataKind>[] metadataMaps = new LinkedHashMap[SEDA2Version.MAX_SUPPORTED_VERSION + 1];
        Boolean[] isNotExpandables = new Boolean[SEDA2Version.MAX_SUPPORTED_VERSION + 1];

        ComplexListInterface.initMetadataMaps(subClass, metadataMaps, isNotExpandables);
        subTypeMetadataMapsMap.put(subClass, metadataMaps);
        subTypeNotExpandablesMap.put(subClass, isNotExpandables);
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
        LinkedHashMap<String, ComplexListMetadataKind>[] metadataMaps = subTypeMetadataMapsMap.get(this.getClass());
        if (metadataMaps == null) {
            getNewComplexListSubType(this.getClass());
            metadataMaps = subTypeMetadataMapsMap.get(this.getClass());
        }
        LinkedHashMap<String, ComplexListMetadataKind> metadataMap = metadataMaps[SEDA2Version.getSeda2Version()];
        return metadataMap != null ? metadataMap : metadataMaps[0];
    }

    /**
     * Gets the metadata map, which link xml element name with metadata class and
     * cardinality for a given ComplexListType sub class.
     *
     * @param complexListTypeMetadataClass the complex list type metadata class
     * @return the metadata map
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    public static Map<String, ComplexListMetadataKind> getMetadataMap(Class<?> complexListTypeMetadataClass) throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind>[] metadataMaps = subTypeMetadataMapsMap.get(complexListTypeMetadataClass);
        if (metadataMaps == null) {
            getNewComplexListSubType(complexListTypeMetadataClass);
            metadataMaps = subTypeMetadataMapsMap.get(complexListTypeMetadataClass);
        }
        HashMap<String, ComplexListMetadataKind> metadataMap = metadataMaps[SEDA2Version.getSeda2Version()];
        return metadataMap != null ? metadataMap : metadataMaps[0];
    }

    /**
     * Checks if the metadata list is closed.
     *
     * @return true, if is not expendable
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    @JsonIgnore
    public boolean isNotExpandable() throws SEDALibException {
        Boolean[] notExpandables = subTypeNotExpandablesMap.get(this.getClass());
        if (notExpandables == null) {
            getNewComplexListSubType(this.getClass());
            notExpandables = subTypeNotExpandablesMap.get(this.getClass());
        }
        Boolean notExpandable = notExpandables[SEDA2Version.getSeda2Version()];
        return (notExpandable != null ? notExpandable : notExpandables[0]);
    }

    /**
     * Checks if it the metadata list is closed for a given ComplexListType sub class.
     *
     * @param complexListTypeMetadataClass the complex list type metadata class
     * @return true, if is not expendable
     * @throws SEDALibException if the @ComplexListMetadataMap annotated static variable doesn't exist or is badly formed
     */
    public static Boolean isNotExpandable(Class<?> complexListTypeMetadataClass) throws SEDALibException {
        Boolean[] notExpandables = subTypeNotExpandablesMap.get(complexListTypeMetadataClass);
        if (notExpandables == null) {
            getNewComplexListSubType(complexListTypeMetadataClass);
            notExpandables = subTypeNotExpandablesMap.get(complexListTypeMetadataClass);
        }
        Boolean notExpandable = notExpandables[SEDA2Version.getSeda2Version()];
        return (notExpandable != null ? notExpandable : notExpandables[0]);
    }
}
