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
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The Class PhysicalDataObject.
 * <p>
 * Class for SEDA element PhysicalDataObject. It contains metadata.
 */
public class PhysicalDataObject extends DataObjectPackageIdElement implements DataObject, ComplexListInterface {

    // SEDA elements

    /**
     * Init metadata map.
     */
    @ComplexListMetadataMap(isExpandable = true, seda2Version = {1})
    public static final Map<String, ComplexListMetadataKind> metadataMap_v1;

    static {
        metadataMap_v1 = new LinkedHashMap<>();
        metadataMap_v1.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        metadataMap_v1.put("DataObjectGroupReferenceId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("DataObjectGroupId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        metadataMap_v1.put("PhysicalId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v1.put("PhysicalDimensions", new ComplexListMetadataKind(PhysicalDimensions.class, false));
    }

    @ComplexListMetadataMap(isExpandable = true, seda2Version = {2})
    public static final Map<String, ComplexListMetadataKind> metadataMap_v2;

    static {
        metadataMap_v2 = new LinkedHashMap<>();
        metadataMap_v2.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        metadataMap_v2.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));

        metadataMap_v2.put("PhysicalId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v2.put("PhysicalDimensions", new ComplexListMetadataKind(PhysicalDimensions.class, false));
    }

    @ComplexListMetadataMap(isExpandable = true, seda2Version = {3})
    public static final Map<String, ComplexListMetadataKind> metadataMap_v3;

    static {
        metadataMap_v3 = new LinkedHashMap<>();
        metadataMap_v3.put("DataObjectProfile", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("DataObjectSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("DataObjectGroupSystemId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("Relationship", new ComplexListMetadataKind(AnyXMLType.class, true));
        metadataMap_v3.put("DataObjectVersion", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("PersistentIdentifier", new ComplexListMetadataKind(PersistentIdentifier.class, true));
        metadataMap_v3.put("DataObjectUse", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("DataObjectNumber", new ComplexListMetadataKind(IntegerType.class, false));

        metadataMap_v3.put("PhysicalId", new ComplexListMetadataKind(StringType.class, false));
        metadataMap_v3.put("PhysicalDimensions", new ComplexListMetadataKind(PhysicalDimensions.class, false));
    }

    public final static LinkedHashMap<String, ComplexListMetadataKind>[] metadataMaps = new LinkedHashMap[SEDA2Version.MAX_SUPPORTED_VERSION + 1];
    public final static Boolean[] notExpandables = new Boolean[SEDA2Version.MAX_SUPPORTED_VERSION + 1];

    static {
        ComplexListInterface.initMetadataMaps(PhysicalDataObject.class, metadataMaps, notExpandables);
    }
    /**
     * The metadata list.
     */
    private List<SEDAMetadata> metadataList;

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public LinkedHashMap<String, ComplexListMetadataKind> getMetadataMap() throws SEDALibException {
        LinkedHashMap<String, ComplexListMetadataKind> result = metadataMaps[SEDA2Version.getSeda2Version()];
        if (result == null)
            result = metadataMaps[0];
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @JsonIgnore
    @Override
    public boolean isNotExpandable() throws SEDALibException {
        Boolean result = notExpandables[SEDA2Version.getSeda2Version()];
        if (result == null)
            result = notExpandables[0];
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SEDAMetadata> getMetadataList() {
        return metadataList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMetadataList(List<SEDAMetadata> metadataList) {
        this.metadataList = metadataList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public String getXmlElementName() {
        return "PhysicalDataObject";
    }

    // Inner element
    /**
     * The DataObjectGroup in which is the PhysicalDataObject, or null.
     */
    @JsonIgnore
    DataObjectGroup dataObjectGroup;

    // Constructors

    /**
     * Instantiates a new PhysicalDataObject.
     */
    public PhysicalDataObject() {
        this(null);
    }

    /**
     * Instantiates a new PhysicalDataObject.
     * <p>
     * If DataObjectPackage is defined the new ArchiveUnit is added with a generated
     * uniqID in the structure.
     *
     * @param dataObjectPackage the DataObjectPackage
     */
    public PhysicalDataObject(DataObjectPackage dataObjectPackage) {
        super(dataObjectPackage);

        this.metadataList = new ArrayList<>(10);

        if (dataObjectPackage != null)
            try {
                dataObjectPackage.addPhysicalDataObject(this);
            } catch (SEDALibException e) {
                // impossible as the uniqID is generated by the called function.
            }
    }

    /**
     * Instantiates a new PhysicalDataObject.
     * <p>
     * If DataObjectPackage is defined the new ArchiveUnit is added with a generated
     * uniqID in the structure.
     * <p>
     * Fragment sample: <code>
     * &lt;DataObjectProfile&gt;DataObject1&lt;DataObjectProfile&gt; // only in SEDA 2.2
     * &lt;DataObjectVersion&gt;PhysicalMaster_1&lt;/DataObjectVersion&gt;
     * &lt;PhysicalId&gt;940 W&lt;/PhysicalId&gt;
     * &lt;PhysicalDimensions&gt;
     * &lt;Width unit="centimetre"&gt;10&lt;/Width&gt;
     * &lt;Height unit="centimetre"&gt;8&lt;/Height&gt;
     * &lt;Depth unit="centimetre"&gt;1&lt;/Depth&gt;
     * &lt;Diameter unit="centimetre"&gt;0&lt;/Diameter&gt;
     * &lt;Weight unit="gram"&gt;59&lt;/Weight&gt;
     * &lt;/PhysicalDimensions&gt;
     * </code>
     *
     * @param dataObjectPackage the DataObjectPackage
     * @param xmlData           the raw XML content describing this PhysicalDataObject
     *                          in manifest but without DataObjectGroup ID or RefID
     *                          information
     * @throws SEDALibException if any xmlData reading exception
     */
    public PhysicalDataObject(DataObjectPackage dataObjectPackage, String xmlData) throws SEDALibException {
        this(dataObjectPackage);
        fromSedaXmlFragments(xmlData);
    }

    // SEDA XML exporter

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.core.DataObject#toSedaXml(fr.gouv.vitam.tools.
     * sedalib.xml.SEDAXMLStreamWriter)
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter, SEDALibProgressLogger sedaLibProgressLogger)
            throws InterruptedException, SEDALibException {
        try {
            // XML write
            xmlWriter.writeStartElement("PhysicalDataObject");
            xmlWriter.writeAttributeIfNotEmpty("id", inDataPackageObjectId);
            toSedaXmlMetadataList(xmlWriter);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException(
                    "Erreur d'écriture XML du PhysicalDataObject [" + inDataPackageObjectId + "]", e);
        }

        int counter = getDataObjectPackage().getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger,SEDALibProgressLogger.OBJECTS_GROUP, counter,
                "sedalib: "+ counter + " métadonnées DataObject exportées");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.sedalib.core.DataObject#toSedaXmlFragments()
     */
    @Override
    public String toSedaXmlFragments() throws SEDALibException {
        String result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            try(SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
                toSedaXml(xmlWriter, null);
            }
            result = baos.toString(StandardCharsets.UTF_8);
        } catch (SEDALibException | XMLStreamException | IOException e) {
            throw new SEDALibException("Erreur interne", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SEDALibException("Interruption", e);
        }

        if (result != null) {
            if (result.isEmpty())
                result = null;
            else {
                result = result.replaceFirst("<PhysicalDataObject .*>", "");
                result = result.substring(1, result.lastIndexOf("</PhysicalDataObject>") - 1);
            }
        }
        return result;
    }

    // SEDA XML importer

    /**
     * Read the PhysicalDataObject element content in XML expected form from the
     * SEDA Manifest in the ArchiveTransfer. Utility methods for fromSedaXml and
     * fromSedaXmlFragments
     *
     * @param xmlReader the SEDAXMLEventReader reading the SEDA manifest
     * @throws SEDALibException if the XML can't be read or the SEDA scheme is
     *                          not respected
     */
    private void setFromXmlContent(SEDAXMLEventReader xmlReader)
            throws SEDALibException {
        try {
            metadataList = new ArrayList<>(18);
            fillFromSedaXmlMetadataList(xmlReader);
        } catch (SEDALibException e) {
            throw new SEDALibException("Erreur de lecture XML du BinaryDataObject", e);
        }
    }

    /**
     * Import the PhysicalDataObject in XML expected form from the SEDA Manifest in
     * the ArchiveTransfer.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage     the DataObjectPackage to be completed
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read PhysicalDataObject, or null if not a PhysicalDataObject
     * @throws SEDALibException     if the XML can't be read or the SEDA scheme is
     *                              not respected
     * @throws InterruptedException if export process is interrupted
     */
    public static PhysicalDataObject fromSedaXml(SEDAXMLEventReader xmlReader, DataObjectPackage dataObjectPackage,
                                                 SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        PhysicalDataObject pdo = null;
        DataObjectGroup dog;
        String tmp;
        try {
            tmp = xmlReader.peekAttributeBlockIfNamed("PhysicalDataObject", "id");
            if (tmp != null) {
                pdo = new PhysicalDataObject();
                pdo.inDataPackageObjectId = tmp;
                dataObjectPackage.addPhysicalDataObject(pdo);
                xmlReader.nextUsefullEvent();
                pdo.setFromXmlContent(xmlReader);
                xmlReader.endBlockNamed("PhysicalDataObject");
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML du PhysicalDataObject"
                    + (pdo != null ? " [" + pdo.inDataPackageObjectId + "]" : ""), e);
        }
        //case not a PhysicalDataObject
        if (pdo == null)
            return null;

        StringType dataObjectGroupId = (StringType) pdo.getFirstNamedMetadata("DataObjectGroupId");
        StringType dataObjectGroupReferenceId = (StringType) pdo.getFirstNamedMetadata("DataObjectGroupReferenceId");

        if ((dataObjectGroupId != null) && (dataObjectGroupReferenceId != null))
            throw new SEDALibException("Eléments DataObjectGroupReferenceId et DataObjectGroupId incompatibles");
        if (dataObjectGroupId != null) {
            if (dataObjectPackage.getDataObjectGroupById(dataObjectGroupId.getValue()) != null)
                throw new SEDALibException("Elément DataObjectGroup [" + dataObjectGroupId.getValue() + "] déjà créé");
            dog = new DataObjectGroup(dataObjectPackage, null);
            dog.setInDataObjectPackageId(dataObjectGroupId.getValue());
            dataObjectPackage.addDataObjectGroup(dog);
            dog.addDataObject(pdo);
            doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.OBJECTS_WARNINGS, "sedalib: dataObjectGroup [" + dog.inDataPackageObjectId
                        + "] créé depuis PhysicalDataObject [" + pdo.inDataPackageObjectId + "]", null);
        } else if (dataObjectGroupReferenceId != null) {
            dog = dataObjectPackage.getDataObjectGroupById(dataObjectGroupReferenceId.getValue());
            if (dog == null)
                throw new SEDALibException("Erreur de référence au DataObjectGroup [" + dataObjectGroupReferenceId.getValue() + "]");
            dog.addDataObject(pdo);
        }
        pdo.removeFirstNamedMetadata("DataObjectGroupReferenceId");
        pdo.removeFirstNamedMetadata("DataObjectGroupId");

        int counter = dataObjectPackage.getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger,SEDALibProgressLogger.OBJECTS_GROUP, counter,
                "sedalib: "+ counter + " métadonnées DataObject importées");
        return pdo;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.core.DataObject#fromSedaXmlFragments(java.lang.
     * String)
     */
    @Override
    public void fromSedaXmlFragments(String fragments) throws SEDALibException {
        PhysicalDataObject pdo = new PhysicalDataObject();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(fragments.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            // jump StartDocument
            xmlReader.nextUsefullEvent();
            pdo.setFromXmlContent(xmlReader);
            XMLEvent event = xmlReader.xmlReader.peek();
            if (!event.isEndDocument())
                throw new SEDALibException("Il y a des champs illégaux");
        } catch (XMLStreamException | SEDALibException | IOException e) {
            throw new SEDALibException("Erreur de lecture du PhysicalDataObject", e);
        }

        metadataList = pdo.metadataList;
    }

    /**
     * Gets the DataObjectVersion metadata from the metadata list.
     *
     * @return the DataObjectVersion metadata, or null if not found
     */
    @JsonIgnore
    public StringType getMetadataDataObjectVersion() {
        return (StringType) getFirstNamedMetadata("DataObjectVersion");
    }

    // Getters and setters

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.sedalib.core.DataObject#getDataObjectGroup()
     */
    public DataObjectGroup getDataObjectGroup() {
        return dataObjectGroup;
    }

    /**
     * Sets the dataObjectGroup.
     *
     * @param dataObjectGroup the new dataObjectGroup
     */
    public void setDataObjectGroup(DataObjectGroup dataObjectGroup) {
        this.dataObjectGroup = dataObjectGroup;
    }
}
