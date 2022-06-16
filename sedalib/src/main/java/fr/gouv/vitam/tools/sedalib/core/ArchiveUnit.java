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
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
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
import java.util.Map;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class ArchiveUnit.
 * <p>
 * Class for SEDA element ArchiveUnit. It contains management and content
 * metadata and links to DataObjects.
 */
public class ArchiveUnit extends DataObjectPackageIdElement {

    // SEDA elements

    /**
     * The ArchiveUnit profile xml element in String form.
     */
    private String archiveUnitProfileXmlData;

    /**
     * The ArchiveUnit profile xml element in metadata form.
     */
    @JsonIgnore
    private ArchiveUnitProfile archiveUnitProfile;

    /**
     * The Management xml element in String form.
     */
    private String managementXmlData;

    /**
     * The Management xml element in metadata form.
     */
    @JsonIgnore
    private Management management;

    /**
     * The Content xml element in String form.
     */
    private String contentXmlData;

    /**
     * The Content xml element in String form.
     */
    @JsonIgnore
    private Content content;

    // ArchiveUnitReferenceAbstract
    // - specify system ArchiveUnit to link as child, not supported by SEDALib

    /**
     * The children ArchiveUnit references list.
     */
    private ArchiveUnitRefList childrenAuList;

    /**
     * The DataObjects references list.
     */
    private DataObjectRefList dataObjectRefList;

    /**
     * Instantiates a new archive unit used by deserialization.
     */
    public ArchiveUnit() {
        this.archiveUnitProfileXmlData = null;
        this.managementXmlData = null;
        this.contentXmlData = null;
        this.archiveUnitProfile = null;
        this.management = null;
        this.content = null;
        this.childrenAuList = new ArchiveUnitRefList(null);
        this.dataObjectRefList = new DataObjectRefList(null);
    }

    /**
     * Instantiates a new ArchiveUnit.
     * <p>
     * If DataObjectPackage is defined the new ArchiveUnit is added with a generated
     * uniqID in the structure.
     *
     * @param dataObjectPackage the DataObjectPackage containing the ArchiveUnit
     */
    public ArchiveUnit(DataObjectPackage dataObjectPackage) {
        super(dataObjectPackage);
        this.archiveUnitProfileXmlData = null;
        this.managementXmlData = null;
        this.contentXmlData = null;
        this.archiveUnitProfile = null;
        this.management = null;
        this.content = null;
        this.childrenAuList = new ArchiveUnitRefList(dataObjectPackage);
        this.dataObjectRefList = new DataObjectRefList(dataObjectPackage);
        if (dataObjectPackage != null)
            try {
                dataObjectPackage.addArchiveUnit(this);
            } catch (SEDALibException forget) {
                // impossible as the uniqID is generated by the called function.
            }
    }

    // Getters and setters

    /**
     * Gets archive unit profile xml data.
     *
     * @return the archive unit profile xml data
     */
    public String getArchiveUnitProfileXmlData() {
        if (archiveUnitProfileXmlData != null)
            return archiveUnitProfileXmlData;
        if (archiveUnitProfile == null)
            return null;
        archiveUnitProfileXmlData = archiveUnitProfile.toString();
        return archiveUnitProfileXmlData;
    }

    /**
     * Sets archive unit profile xml data.
     *
     * @param archiveUnitProfileXmlData the archive unit profile xml data
     */
    public void setArchiveUnitProfileXmlData(String archiveUnitProfileXmlData) {
        this.archiveUnitProfileXmlData = archiveUnitProfileXmlData;
        this.archiveUnitProfile = null;
    }

    /**
     * Gets archive unit profile.
     *
     * @return the archive unit profile
     * @throws SEDALibException if raw xml data is not convenient
     */
    public ArchiveUnitProfile getArchiveUnitProfile() throws SEDALibException {
        if (archiveUnitProfile != null) {
            archiveUnitProfileXmlData = null;
            return archiveUnitProfile;
        }
        if (archiveUnitProfileXmlData == null)
            return null;
        archiveUnitProfile = (ArchiveUnitProfile) SEDAMetadata.fromString(archiveUnitProfileXmlData,
                ArchiveUnitProfile.class);
        // as fromString function normalise ArchiveUnitProfile had to destroy approximate version archiveUnitProfileXmlData
        archiveUnitProfileXmlData = null;
        return archiveUnitProfile;
    }

    /**
     * Sets archive unit profile.
     *
     * @param archiveUnitProfile the archive unit profile
     */
    public void setArchiveUnitProfile(ArchiveUnitProfile archiveUnitProfile) {
        this.archiveUnitProfileXmlData = null;
        this.archiveUnitProfile = archiveUnitProfile;
    }

    /**
     * Gets management xml data.
     *
     * @return the management xml data
     */
    public String getManagementXmlData() {
        if (managementXmlData != null)
            return managementXmlData;
        if (management == null)
            return null;
        managementXmlData = management.toString();
        return managementXmlData;
    }

    /**
     * Sets management xml data.
     *
     * @param managementXmlData the management xml data
     */
    public void setManagementXmlData(String managementXmlData) {
        this.managementXmlData = managementXmlData;
        this.management = null;
    }

    /**
     * Gets management.
     *
     * @return the management
     * @throws SEDALibException if raw xml data is not convenient
     */
    public Management getManagement() throws SEDALibException {
        if (management != null) {
            managementXmlData = null;
            return management;
        }
        if (managementXmlData == null)
            return null;
        management = (Management) SEDAMetadata.fromString(managementXmlData,
                Management.class);
        // as fromString function normalise Management had to destroy approximate version managementXmlData
        managementXmlData = null;
        return management;
    }

    /**
     * Sets management.
     *
     * @param management the management
     */
    public void setManagement(Management management) {
        this.managementXmlData = null;
        this.management = management;
    }

    /**
     * Gets content xml data.
     *
     * @return the content xml data
     */
    public String getContentXmlData() {
        if (contentXmlData != null)
            return contentXmlData;
        if (content == null)
            return null;
        contentXmlData = content.toString();
        return contentXmlData;
    }

    /**
     * Is descriptive metadata in Content SEDA compliant.
     *
     * @return the compliance flag.
     */
    @JsonIgnore
    public boolean isContentSEDACompliant() {
        if (content != null)
            return true;
        if (contentXmlData == null)
            return false;
        try {
            content = (Content) SEDAMetadata.fromString(contentXmlData,
                    Content.class);
            contentXmlData = null;
        } catch (SEDALibException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets content in export form, filtered if needed.
     *
     * @return the content xml data
     */
    @JsonIgnore
    public String getFilteredContentExportString() {
        if (getDataObjectPackage().getExportMetadataList() == null)
            return getContentXmlData();
        if (content != null)
            return content.filteredToString(getDataObjectPackage().getExportMetadataList());
        try {
            content = (Content) SEDAMetadata.fromString(contentXmlData,
                    Content.class);
            contentXmlData = null;
        } catch (SEDALibException e) {
            return contentXmlData;
        }
        return content.filteredToString(getDataObjectPackage().getExportMetadataList());
    }

    /**
     * Sets content xml data.
     *
     * @param contentXmlData the content xml data
     */
    public void setContentXmlData(String contentXmlData) {
        this.contentXmlData = contentXmlData;
        this.content = null;
    }

    /**
     * Gets content.
     *
     * @return the content
     * @throws SEDALibException if raw xml data is not convenient
     */
    public Content getContent() throws SEDALibException {
        if (content != null) {
            contentXmlData = null;
            return content;
        }
        if (contentXmlData == null)
            return null;
        content = (Content) SEDAMetadata.fromString(contentXmlData,
                Content.class);
        // as fromString function normalise Content had to destroy approximate version contentXmlData
        contentXmlData = null;
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(Content content) {
        this.contentXmlData = null;
        this.content = content;
    }

    // Methods

    /**
     * Sets the Content xml element constructed with given title and description level.
     *
     * @param title            the ArchiveUnit title
     * @param descriptionLevel the ArchiveUnit description level
     * @throws SEDALibException if sub elements construction is not possible (not supposed to occur)
     */
    public void setDefaultContent(String title, String descriptionLevel) throws SEDALibException{
        Content c = new Content();
        c.addNewMetadata("DescriptionLevel", descriptionLevel);
        c.addNewMetadata("Title", title);
        setContent(c);
    }

    /**
     * Adds one DataObject by Id.
     *
     * @param inDataPackageObjectId the id in DataObjectPackage
     */
    public void addDataObjectById(String inDataPackageObjectId) {
        dataObjectRefList.addById(inDataPackageObjectId);
    }

    /**
     * Removes one DataObject by Id.
     *
     * @param inDataPackageObjectId the id in DataObjectPackage
     */
    public void removeDataObjectById(String inDataPackageObjectId) {
        dataObjectRefList.removeById(inDataPackageObjectId);
    }

    /**
     * Adds one child ArchiveUnit in the list kept in this ArchiveUnit.
     *
     * @param au the ArchiveUnit
     */
    public void addChildArchiveUnit(ArchiveUnit au) {
        childrenAuList.add(au);
    }

    /**
     * Removes the child ArchiveUnit from the list kept in this ArchiveUnit.
     *
     * @param au the ArchiveUnit
     */
    public void removeChildArchiveUnit(ArchiveUnit au) {
        childrenAuList.remove(au);
    }

    /**
     * Replace the child ArchiveUnit in the list kept in this ArchiveUnit.
     *
     * @param originAu    the origin ArchiveUnit
     * @param newAu the new ArchiveUnit
     */
    public void replaceChildArchiveUnit(ArchiveUnit originAu, ArchiveUnit newAu) {
        childrenAuList.replace(originAu,newAu);
    }

    /**
     * Adds one child ArchiveUnit, defined by id, in the list kept in this
     * ArchiveUnit.
     *
     * @param inDataPackageObjectId the id in DataObjectPackage
     */
    public void addChildArchiveUnitById(String inDataPackageObjectId) {
        childrenAuList.addById(inDataPackageObjectId);
    }

    /**
     * Removes the child ArchiveUnit, defined by id, from the list kept in this
     * ArchiveUnit.
     *
     * @param inDataPackageObjectId the id in DataObjectPackage
     */
    public void removeChildArchiveUnitById(String inDataPackageObjectId) {
        childrenAuList.removeById(inDataPackageObjectId);
    }

    // SEDA XML exporter

    /**
     * Export the ArchiveUnit in XML expected form for the SEDA Manifest.
     *
     * @param xmlWriter             the SEDAXMLStreamWriter generating the SEDA manifest
     * @param imbricateFlag         indicates if the manifest ArchiveUnits are to be                       exported in imbricate mode (true) or in flat mode                       (false)
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException     if the XML can't be written
     * @throws InterruptedException if export process is interrupted
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter, boolean imbricateFlag, SEDALibProgressLogger sedaLibProgressLogger)
            throws SEDALibException, InterruptedException {
        try {
            if (imbricateFlag) {
                if (getDataObjectPackage().isTouchedInDataObjectPackageId(inDataPackageObjectId)) {
                    xmlWriter.writeStartElement("ArchiveUnit");
                    xmlWriter.writeAttribute("id", getDataObjectPackage().getNextRefID());
                    xmlWriter.writeElementValue("ArchiveUnitRefId", inDataPackageObjectId);
                    xmlWriter.writeEndElement();
                    return;
                } else
                    getDataObjectPackage().addTouchedInDataObjectPackageId(inDataPackageObjectId);
            }

            xmlWriter.writeStartElement("ArchiveUnit");
            xmlWriter.writeAttribute("id", inDataPackageObjectId);
            xmlWriter.writeRawXMLBlockIfNotEmpty(getArchiveUnitProfileXmlData());
            xmlWriter.writeRawXMLBlockIfNotEmpty(getManagementXmlData());
            if ((getDataObjectPackage().getExportMetadataList() != null) &&
                    !isContentSEDACompliant()) {
                doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL,
                        "sedalib: l'ArchiveUnit [" + inDataPackageObjectId + "] ne peut être filtrée car son Content " +
                                "n'est pas conforme SEDA. Le Content est écrit tel quel.", null);
                xmlWriter.writeRawXMLBlockIfNotEmpty(getContentXmlData());
            } else
                xmlWriter.writeRawXMLBlockIfNotEmpty(getFilteredContentExportString());
            for (ArchiveUnit au : childrenAuList.getArchiveUnitList()) {
                if (!imbricateFlag) {
                    xmlWriter.writeStartElement("ArchiveUnit");
                    xmlWriter.writeAttribute("id", getDataObjectPackage().getNextRefID());
                    xmlWriter.writeElementValue("ArchiveUnitRefId", au.inDataPackageObjectId);
                    xmlWriter.writeEndElement();
                } else {
                    au.toSedaXml(xmlWriter, true, sedaLibProgressLogger);
                }
            }
            for (DataObject dataObject : dataObjectRefList.getDataObjectList()) {
                xmlWriter.writeStartElement("DataObjectReference");
                if (dataObject instanceof DataObjectGroup)
                    xmlWriter.writeElementValue("DataObjectGroupReferenceId", dataObject.getInDataObjectPackageId());
                else
                    xmlWriter.writeElementValue("DataObjectReferenceId", dataObject.getInDataObjectPackageId());
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SEDALibException(
                    "Erreur d'écriture XML de l'ArchiveUnit [" + inDataPackageObjectId + "]", e);
        }

        int counter = getDataObjectPackage().getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
                "sedalib: " + counter + " métadonnées ArchiveUnit exportées");
    }

    /**
     * Export the elements of ArchiveUnit that can be edited without changing the
     * structure. This is in XML expected form for the SEDA Manifest but in String.
     *
     * @return the XML elements in String format
     */
    public String toSedaXmlFragments() {
        StringBuilder sb = new StringBuilder();

        String tmp = getArchiveUnitProfileXmlData();
        if (tmp != null)
            sb.append(tmp).append("\n");
        tmp = getManagementXmlData();
        if (tmp != null)
            sb.append(tmp).append("\n");
        tmp = getContentXmlData();
        if (tmp != null)
            sb.append(tmp).append("\n");
        return sb.toString().trim();
    }

    // SEDA XML importer

    /**
     * Import the ArchiveUnit in XML expected form from the SEDA Manifest in the
     * ArchiveTransfer and return it's id in ArchiveTransfer.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage     the DataObjectPackage to be completed
     * @param sedaLibProgressLogger the progress logger
     * @return the inDataPackageObjectId of the read ArchiveUnit, or null if not an ArchiveUnit
     * @throws SEDALibException     if the XML can't be read
     * @throws InterruptedException if export process is interrupted
     */
    public static String idFromSedaXml(SEDAXMLEventReader xmlReader, DataObjectPackage dataObjectPackage,
                                       SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        ArchiveUnit au = null;
        DataObject dataObject;
        String tmp;
        try {
            tmp = xmlReader.peekAttributeBlockIfNamed("ArchiveUnit", "id");
            if (tmp != null) {
                xmlReader.nextUsefullEvent();
                if (xmlReader.peekBlockIfNamed("ArchiveUnitRefId")) {
                    tmp = xmlReader.nextMandatoryValue("ArchiveUnitRefId");
                    xmlReader.endBlockNamed("ArchiveUnit");
                    return tmp;

                } else {
                    au = new ArchiveUnit();
                    au.inDataPackageObjectId = tmp;
                    dataObjectPackage.addArchiveUnit(au);
                    au.setArchiveUnitProfileXmlData(xmlReader.nextBlockAsStringIfNamed("ArchiveUnitProfile"));
                    au.setManagementXmlData(xmlReader.nextBlockAsStringIfNamed("Management"));
                    au.setContentXmlData(xmlReader.nextBlockAsStringIfNamed("Content"));
                    while (true) {
                        tmp = xmlReader.peekName();
                        if (tmp == null)
                            break;
                        switch (tmp) {
                            case "ArchiveUnit":
                                String subAuId = idFromSedaXml(xmlReader, dataObjectPackage, sedaLibProgressLogger);
                                au.addChildArchiveUnitById(subAuId);
                                break;
                            case "DataObjectReference":
                                xmlReader.nextUsefullEvent();
                                tmp = xmlReader.peekName();
                                if (tmp == null)
                                    break;
                                switch (tmp) {
                                    case "DataObjectReferenceId":
                                        tmp = xmlReader.nextValueIfNamed("DataObjectReferenceId");
                                        dataObject = dataObjectPackage.getDataObjectById(tmp);
                                        if ((dataObject == null) || (dataObject instanceof DataObjectGroup))
                                            throw new SEDALibException(
                                                    "Erreur de référence DataObjectReferenceId [" + tmp + "]");
                                        au.addDataObjectById(tmp);
                                        break;
                                    case "DataObjectGroupReferenceId":
                                        tmp = xmlReader.nextValueIfNamed("DataObjectGroupReferenceId");
                                        dataObject = dataObjectPackage.getDataObjectById(tmp);
                                        if (!(dataObject instanceof DataObjectGroup))
                                            throw new SEDALibException(
                                                    "Erreur de référence DataObjectGroupReferenceId [" + tmp + "]");
                                        au.addDataObjectById(tmp);
                                        break;
                                    default:
                                        throw new SEDALibException("Element DataObjectReference mal formé");

                                }
                                xmlReader.endBlockNamed("DataObjectReference");
                                break;
                            default:
                                throw new SEDALibException("Element ArchiveUnit [" + au.inDataPackageObjectId
                                        + "] mal formé, élément [" + tmp + "] anormal");

                        }
                    }
                    xmlReader.endBlockNamed("ArchiveUnit");
                }
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur de lecture XML de l'ArchiveUnit"
                    + (au != null ? " [" + au.inDataPackageObjectId + "]" : ""), e);
        }
        // next XML element not an ArchiveUnit
        if (au == null)
            return null;

        int counter = dataObjectPackage.getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter, "sedalib: " + counter + " métadonnées ArchiveUnit importées");

        return au.inDataPackageObjectId;
    }

    /**
     * Import the elements of ArchiveUnit that can be edited without changing the
     * structure. This is in XML expected form for the SEDA Manifest but in String.
     *
     * @param fragments the XML elements in String format
     * @throws SEDALibException if the XML can't be read or don't have expected                          mandatory field - Content
     */
    public void fromSedaXmlFragments(String fragments) throws SEDALibException {
        ArchiveUnit au = new ArchiveUnit();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(fragments.getBytes(StandardCharsets.UTF_8));
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(bais, true)) {
            // jump StartDocument
            xmlReader.nextUsefullEvent();
            au.setArchiveUnitProfileXmlData(xmlReader.nextBlockAsStringIfNamed("ArchiveUnitProfile"));
            au.setManagementXmlData(xmlReader.nextBlockAsStringIfNamed("Management"));
            au.setContentXmlData(xmlReader.nextBlockAsStringIfNamed("Content"));
            XMLEvent event = xmlReader.xmlReader.peek();
            if (!event.isEndDocument())
                throw new SEDALibException("Il y a des champs illégaux");
        } catch (XMLStreamException | SEDALibException | IOException e) {
            throw new SEDALibException(
                    "Erreur de lecture XML de l'ArchiveUnit [" + inDataPackageObjectId + "]", e);
        }

        if (au.getContentXmlData() == null)
            throw new SEDALibException("La partie <Content> de l'ArchiveUnit est obligatoire");
        setArchiveUnitProfileXmlData(au.getArchiveUnitProfileXmlData());
        setManagementXmlData(au.getManagementXmlData());
        setContentXmlData(au.getContentXmlData());
    }

    /**
     * Return the indented XML export form as the String representation.
     *
     * @return the indented XML form String
     */
    public String toString() {
        String result = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             SEDAXMLStreamWriter xmlWriter = new SEDAXMLStreamWriter(baos, 2)) {
            toSedaXml(xmlWriter, true, null);
            xmlWriter.flush();
            result = baos.toString(StandardCharsets.UTF_8);
            if (result.startsWith("\n"))
                result = result.substring(1);
        } catch (XMLStreamException | IOException | SEDALibException e) {
                result = super.toString();
        }
        catch (InterruptedException e){
                result = super.toString();
            Thread.currentThread().interrupt();
        }
        return result;
    }

    // Getters and setters

    /**
     * Gets the children ArchiveUnit list.
     *
     * @return the children ArchiveUnit list
     */
    public ArchiveUnitRefList getChildrenAuList() {
        return childrenAuList;
    }

    /**
     * Sets the children ArchiveUnit list.
     *
     * @param childrenAuList the new children ArchiveUnit list
     */
    public void setChildrenAuList(ArchiveUnitRefList childrenAuList) {
        this.childrenAuList = childrenAuList;
    }

    /**
     * Gets the DataObject reference list.
     *
     * @return the DataObject reference list
     */
    public DataObjectRefList getDataObjectRefList() {
        return dataObjectRefList;
    }

    /**
     * Sets the DataObject reference list.
     *
     * @param dataObjectRefList the new DataObject reference list.
     */
    public void setDataObjectRefList(DataObjectRefList dataObjectRefList) {
        this.dataObjectRefList = dataObjectRefList;
    }

    /**
     * Gets the data object group, if the ArchiveUnit is normalized, or null.
     *
     * @return the the data object group
     */
    @JsonIgnore
    public DataObjectGroup getTheDataObjectGroup() {
        if (dataObjectRefList.getCount() != 1)
            return null;
        DataObject tmp = dataObjectRefList.getDataObjectList().get(0);
        if (tmp instanceof DataObjectGroup)
            return (DataObjectGroup) tmp;
        return null;
    }

    private ArchiveUnit searchDOGArchiveUnit(DataObjectGroup dataObjectGroup) {
        DataObjectGroup dog;
        for (Map.Entry<String, ArchiveUnit> e : getDataObjectPackage().getAuInDataObjectPackageIdMap().entrySet()) {
            dog = e.getValue().getTheDataObjectGroup();
            if ((dog != null) && (dog.equals(dataObjectGroup)))
                return e.getValue();
        }
        return null;
    }

    /**
     * Remove the data object group if empty.
     *
     * @return true if done, false if not possible
     */
    public boolean removeEmptyDataObjectGroup() {
        DataObjectGroup dog = getTheDataObjectGroup();
        if (dog != null) {
            if (dog.getPhysicalDataObjectList().size()+dog.getBinaryDataObjectList().size()!=0)
                return false;
            removeDataObjectById(dog.getInDataObjectPackageId());
            if (searchDOGArchiveUnit(dog) == null)
                getDataObjectPackage().getDogInDataObjectPackageIdMap().remove(dog.getInDataObjectPackageId());
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.gouv.vitam.tools.sedalib.core.DataObjectPackageElement#setArchiveTransfer(
     * fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer)
     */
    @Override
    public void setDataObjectPackage(DataObjectPackage dataObjectPackage) {
        super.setDataObjectPackage(dataObjectPackage);
        childrenAuList.setDataObjectPackage(dataObjectPackage);
        dataObjectRefList.setDataObjectPackage(dataObjectPackage);
    }

}
