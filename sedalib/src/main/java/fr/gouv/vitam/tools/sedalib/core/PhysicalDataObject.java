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
import fr.gouv.vitam.tools.sedalib.utils.ProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * The Class PhysicalDataObject.
 * <p>
 * Class for SEDA element PhysicalDataObject. It contains metadata.
 */
public class PhysicalDataObject extends DataObjectPackageIdElement implements DataObject {

    // SEDA elements

    /** The data object system id. */
    public String dataObjectSystemId;

    /** The data object group system id. */
    public String dataObjectGroupSystemId;

    /** The relationships xml element in String form. */
    public List<String> relationshipsXmlData;

    /** The data object group reference id. */
    String dataObjectGroupReferenceId;

    /** The data object group id. */
    String dataObjectGroupId;

    /** The data object version. */
    public String dataObjectVersion;

    /** The physical id xml element in String form. */
    public String physicalIdXmlData;

    /** The physical dimensions xml element in String form. */
    public String physicalDimensionsXmlData;

    // otherDimensionsAbstractXmlData not supported

    // Inner element

    /** The DataObjectGroup in which is the PhysicalDataObject, or null. */
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
     * @param dataObjectPackage the DataObjectPackage
     */
    public PhysicalDataObject(DataObjectPackage dataObjectPackage) {
        super(dataObjectPackage);
        this.dataObjectSystemId = null;
        this.dataObjectGroupSystemId = null;
        this.relationshipsXmlData = new ArrayList<String>();
        this.dataObjectGroupReferenceId = null;
        this.dataObjectGroupId = null;
        this.dataObjectVersion = null;
        this.physicalIdXmlData = null;
        this.physicalDimensionsXmlData = null;
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
     * &lt;DataObjectVersion&gt;PhysicalMaster_1&lt;/DataObjectVersion&gt;
     * &lt;PhysicalId&gt;940 W&lt;/PhysicalId&gt;
     * &lt;PhysicalDimensions&gt;
     *   &lt;Width unit="centimetre"&gt;10&lt;/Width&gt;
     *   &lt;Height unit="centimetre"&gt;8&lt;/Height&gt;
     *   &lt;Depth unit="centimetre"&gt;1&lt;/Depth&gt;
     *   &lt;Diameter unit="centimetre"&gt;0&lt;/Diameter&gt;
     *   &lt;Weight unit="gram"&gt;59&lt;/Weight&gt;
     * &lt;/PhysicalDimensions&gt;
     * </code>
     *
     * @param dataObjectPackage the DataObjectPackage
     * @param xmlData         the raw XML content describing this PhysicalDataObject
     *                        in manifest but without DataObjectGroup ID or RefID
     *                        information
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
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter, ProgressLogger progressLogger)
            throws InterruptedException, SEDALibException {
        try {
            xmlWriter.writeStartElement("PhysicalDataObject");
            xmlWriter.writeAttribute("id", inDataPackageObjectId);
            xmlWriter.writeElementValueIfNotEmpty("DataObjectSystemId", dataObjectSystemId);
            xmlWriter.writeElementValueIfNotEmpty("DataObjectGroupSystemId", dataObjectGroupSystemId);
            for (String rXmlData : relationshipsXmlData)
                xmlWriter.writeRawXMLBlockIfNotEmpty(rXmlData);
//		dataObjectGroupReferenceId not used in 2.1 DataObjectGroup mode
//		dataObjectGroupId not used in 2.1 DataObjectGroup mode
            xmlWriter.writeElementValueIfNotEmpty("DataObjectVersion", dataObjectVersion);
            xmlWriter.writeRawXMLBlockIfNotEmpty(physicalIdXmlData);
            xmlWriter.writeRawXMLBlockIfNotEmpty(physicalDimensionsXmlData);
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException(
                    "Erreur d'écriture XML du PhysicalDataObject [" + inDataPackageObjectId + "]\n->" + e.getMessage());
        }

        int counter = getDataObjectPackage().getNextInOutCounter();
        if (progressLogger != null)
            progressLogger.progressLogIfStep(ProgressLogger.OBJECTS_GROUP, counter,
                    Integer.toString(counter) + " DataObject (métadonnées) exportés");
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
            throw new SEDALibException("Erreur interne ->" + e.getMessage());
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
     * @param xmlReader          the SEDAXMLEventReader reading the SEDA manifest
     * @throws SEDALibException     if the XML can't be read or the SEDA scheme is
     *                              not respected
     */
    private void setFromXmlContent(SEDAXMLEventReader xmlReader)
            throws SEDALibException {
        String tmp;
        try {
            dataObjectSystemId = xmlReader.nextValueIfNamed("DataObjectSystemId");
            dataObjectGroupSystemId = xmlReader.nextValueIfNamed("DataObjectGroupSystemId");
            while (true) {
                tmp = xmlReader.nextValueIfNamed("Relationship");
                if (tmp != null)
                    relationshipsXmlData.add(tmp);
                else
                    break;
            }
            dataObjectGroupReferenceId = xmlReader.nextValueIfNamed("DataObjectGroupReferenceId");
            dataObjectGroupId = xmlReader.nextValueIfNamed("DataObjectGroupId");
            dataObjectVersion = xmlReader.nextValueIfNamed("DataObjectVersion");
            physicalIdXmlData = xmlReader.nextBlockAsStringIfNamed("PhysicalId");
            physicalDimensionsXmlData = xmlReader.nextBlockAsStringIfNamed("PhysicalDimensions");
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML du PhysicalDataObject\n->" + e.getMessage());
        }
    }

    /**
     * Import the PhysicalDataObject in XML expected form from the SEDA Manifest in
     * the ArchiveTransfer.
     *
     * @param xmlReader       the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage the DataObjectPackage to be completed
     * @param progressLogger the progress logger or null if no progress log expected
     * @return the read PhysicalDataObject, or null if not a PhysicalDataObject
     * @throws SEDALibException     if the XML can't be read or the SEDA scheme is
     *                              not respected
     * @throws InterruptedException if export process is interrupted
     */
    public static PhysicalDataObject fromSedaXml(SEDAXMLEventReader xmlReader, DataObjectPackage dataObjectPackage,
                                                 ProgressLogger progressLogger) throws SEDALibException, InterruptedException {
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
                    + (pdo != null ? " [" + pdo.inDataPackageObjectId + "]" : "") + "\n->" + e.getMessage());
        }
        //case not a PhysicalDataObject
        if (pdo == null)
            return null;

        if ((pdo.dataObjectGroupReferenceId != null) && (pdo.dataObjectGroupId != null))
            throw new SEDALibException("Eléments DataObjectGroupReferenceId et DataObjectGroupId incompatibles");
        if (pdo.dataObjectGroupId != null) {
            if (dataObjectPackage.getDataObjectGroupById(pdo.dataObjectGroupId) != null)
                throw new SEDALibException("Elément DataObjectGroup [" + pdo.dataObjectGroupId + "] déjà créé");
            dog = new DataObjectGroup(dataObjectPackage, null);
            dog.setInDataObjectPackageId(pdo.dataObjectGroupId);
            dataObjectPackage.addDataObjectGroup(dog);
            dog.addDataObject(pdo);
            if (progressLogger!=null)
                progressLogger.log(ProgressLogger.OBJECTS_WARNINGS, "DataObjectGroup [" + dog.inDataPackageObjectId
                    + "] créé depuis PhysicalDataObject [" + pdo.inDataPackageObjectId + "]");
        } else if (pdo.dataObjectGroupReferenceId != null) {
            dog = dataObjectPackage.getDataObjectGroupById(pdo.dataObjectGroupReferenceId);
            if (dog == null)
                throw new SEDALibException("Erreur de référence au DataObjectGroup [" + pdo.dataObjectGroupId + "]");
            dog.addDataObject(pdo);
        }

        int counter = dataObjectPackage.getNextInOutCounter();
        if (progressLogger != null)
            progressLogger.progressLogIfStep(ProgressLogger.OBJECTS_GROUP, counter,
                    Integer.toString(counter) + " DataObject (métadonnées) importés");
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
            throw new SEDALibException("Erreur de lecture du PhysicalDataObject\n->" + e.getMessage());
        }

        if ((pdo.dataObjectGroupId != null) || (pdo.dataObjectGroupReferenceId != null))
            throw new SEDALibException("La référence à un DataObjectGroup n'est pas modifiable par édition");
        this.dataObjectSystemId = pdo.dataObjectSystemId;
        this.dataObjectGroupSystemId = pdo.dataObjectGroupSystemId;
        this.relationshipsXmlData = pdo.relationshipsXmlData;
        this.dataObjectVersion = pdo.dataObjectVersion;
        this.physicalIdXmlData = pdo.physicalIdXmlData;
        this.physicalDimensionsXmlData = pdo.physicalDimensionsXmlData;
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