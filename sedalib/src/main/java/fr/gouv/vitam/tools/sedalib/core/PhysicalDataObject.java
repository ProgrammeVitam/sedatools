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
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class PhysicalDataObject.
 * <p>
 * Class for SEDA element PhysicalDataObject. It contains metadata.
 */
public class PhysicalDataObject extends DataObjectPackageIdElement implements DataObject {

    // SEDA elements

    /**
     * The data object system id.
     */
    public StringType dataObjectSystemId;

    /**
     * The data object group system id.
     */
    public StringType dataObjectGroupSystemId;

    /**
     * The relationships xml element in String form.
     */
    public List<AnyXMLType> relationshipsXmlData;

    /**
     * The data object group reference id.
     */
    public StringType dataObjectGroupReferenceId;

    /**
     * The data object group id.
     */
    public StringType dataObjectGroupId;

    /**
     * The data object version.
     */
    public StringType dataObjectVersion;

    /**
     * The physical id xml element in String form.
     */
    public StringType physicalId;

    /**
     * The physical dimensions xml element.
     */
    public PhysicalDimensions physicalDimensions;

    /**
     * The other Dimensions AbstractXml for any rawXML.
     */
    public List<AnyXMLType> otherDimensionsAbstractXml;


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
        this.dataObjectSystemId = null;
        this.dataObjectGroupSystemId = null;
        this.relationshipsXmlData = new ArrayList<AnyXMLType>();
        this.dataObjectGroupReferenceId = null;
        this.dataObjectGroupId = null;
        this.dataObjectVersion = null;
        this.physicalId = null;
        this.physicalDimensions = null;
        if (dataObjectPackage != null)
            try {
                dataObjectPackage.addPhysicalDataObject(this);
            } catch (SEDALibException e) {
                // impossible as the uniqID is generated by the called function.
            }
        this.otherDimensionsAbstractXml = new ArrayList<AnyXMLType>();
    }

    /**
     * Instantiates a new PhysicalDataObject.
     * <p>
     * If DataObjectPackage is defined the new ArchiveUnit is added with a generated
     * uniqID in the structure.
     * <p>
     * Fragment sample: <code>
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
            xmlWriter.writeStartElement("PhysicalDataObject");
            xmlWriter.writeAttribute("id", inDataPackageObjectId);

            if (dataObjectSystemId != null)
                dataObjectSystemId.toSedaXml(xmlWriter);
            if (dataObjectGroupSystemId != null)
                dataObjectGroupSystemId.toSedaXml(xmlWriter);
            for (AnyXMLType rXmlData : relationshipsXmlData)
                rXmlData.toSedaXml(xmlWriter);
//		dataObjectGroupReferenceId not used in 2.1 DataObjectGroup mode
//		dataObjectGroupId not used in 2.1 DataObjectGroup mode
            if (dataObjectVersion != null)
                dataObjectVersion.toSedaXml(xmlWriter);
            if (physicalId!=null)
                physicalId.toSedaXml(xmlWriter);
            if (physicalDimensions!=null)
                physicalDimensions.toSedaXml(xmlWriter);
            for (AnyXMLType otherXmlData : otherDimensionsAbstractXml)
                otherXmlData.toSedaXml(xmlWriter);
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
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
            toSedaXml(xmlWriter, null);
            xmlWriter.close();
            result = baos.toString("UTF-8");
        } catch (SEDALibException | XMLStreamException | InterruptedException | IOException e) {
            throw new SEDALibException("Erreur interne", e);
        }
        if (result != null) {
            if (result.isEmpty())
                result = null;
            else {
                result = result.replaceFirst("\\<PhysicalDataObject .*\\>", "");
                result = result.substring(0, result.lastIndexOf("</PhysicalDataObject>") - 1);
                result = result.trim();
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
        String nextElementName;
        try {
            nextElementName = xmlReader.peekName();
            if ((nextElementName != null) && (nextElementName.equals("DataObjectSystemId"))) {
                dataObjectSystemId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectGroupSystemId"))) {
                dataObjectGroupSystemId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            relationshipsXmlData = new ArrayList<>();
            while ((nextElementName != null) && (nextElementName.equals("Relationship"))) {
                relationshipsXmlData.add((AnyXMLType) SEDAMetadata.fromSedaXml(xmlReader, AnyXMLType.class));
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectGroupReferenceId"))) {
                dataObjectGroupReferenceId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectGroupId"))) {
                dataObjectGroupId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("DataObjectVersion"))) {
                dataObjectVersion = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("PhysicalId"))) {
                physicalId = (StringType) SEDAMetadata.fromSedaXml(xmlReader, StringType.class);
                nextElementName = xmlReader.peekName();
            }
            if ((nextElementName != null) && (nextElementName.equals("PhysicalDimensions"))) {
                physicalDimensions = (PhysicalDimensions) SEDAMetadata.fromSedaXml(xmlReader, PhysicalDimensions.class);
                nextElementName = xmlReader.peekName();
            }
            otherDimensionsAbstractXml = new ArrayList<>();
            while (nextElementName != null) {
                otherDimensionsAbstractXml.add((AnyXMLType) SEDAMetadata.fromSedaXml(xmlReader, AnyXMLType.class));
                nextElementName = xmlReader.peekName();
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML du PhysicalDataObject", e);
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

        if ((pdo.dataObjectGroupReferenceId != null) && (pdo.dataObjectGroupId != null))
            throw new SEDALibException("Eléments DataObjectGroupReferenceId et DataObjectGroupId incompatibles");
        if (pdo.dataObjectGroupId != null) {
            if (dataObjectPackage.getDataObjectGroupById(pdo.dataObjectGroupId.getValue()) != null)
                throw new SEDALibException("Elément DataObjectGroup [" + pdo.dataObjectGroupId.getValue() + "] déjà créé");
            dog = new DataObjectGroup(dataObjectPackage, null);
            dog.setInDataObjectPackageId(pdo.dataObjectGroupId.getValue());
            dataObjectPackage.addDataObjectGroup(dog);
            dog.addDataObject(pdo);
            doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.OBJECTS_WARNINGS, "sedalib: dataObjectGroup [" + dog.inDataPackageObjectId
                        + "] créé depuis PhysicalDataObject [" + pdo.inDataPackageObjectId + "]", null);
        } else if (pdo.dataObjectGroupReferenceId != null) {
            dog = dataObjectPackage.getDataObjectGroupById(pdo.dataObjectGroupReferenceId.getValue());
            if (dog == null)
                throw new SEDALibException("Erreur de référence au DataObjectGroup [" + pdo.dataObjectGroupId.getValue() + "]");
            dog.addDataObject(pdo);
        }

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

        try (ByteArrayInputStream bais = new ByteArrayInputStream(fragments.getBytes("UTF-8"));
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

        if ((pdo.dataObjectGroupId != null) || (pdo.dataObjectGroupReferenceId != null))
            throw new SEDALibException("La référence à un DataObjectGroup n'est pas modifiable par édition");
        this.dataObjectSystemId = pdo.dataObjectSystemId;
        this.dataObjectGroupSystemId = pdo.dataObjectGroupSystemId;
        this.relationshipsXmlData = pdo.relationshipsXmlData;
        this.dataObjectVersion = pdo.dataObjectVersion;
        this.physicalId = pdo.physicalId;
        this.physicalDimensions = pdo.physicalDimensions;
        this.otherDimensionsAbstractXml=pdo.otherDimensionsAbstractXml;
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
