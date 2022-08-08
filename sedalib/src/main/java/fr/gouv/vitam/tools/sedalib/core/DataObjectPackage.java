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

import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * The Class DataObjectPackage
 * <p>
 * Class for the SEDA SIP DataObjectPackage. It contains all the elements
 * in tree structure ArchiveUnits and DataObjects.
 * <p>
 * It has also a specific exportMetadataList field which define the list
 * of descriptive metadata elements kept in export to XML manifest or csv metadata file. This is used to restrict
 * to the set of metadata that an archiving system can handle. This export filter do not apply to any other XML
 * or csv format function (for example toString, toSedaXMLFragments, toCsvList...).
 */
public class DataObjectPackage {

    // SEDA elements
    /**
     * The map of all the ArchiveUnits by inDataPackageObjectId.
     */
    private HashMap<String, ArchiveUnit> auInDataObjectPackageIdMap;

    /**
     * The map of all the DataObjectGroups by inDataPackageObjectId.
     */
    private HashMap<String, DataObjectGroup> dogInDataObjectPackageIdMap;

    /**
     * The map of all the BinaryDataObjects by inDataPackageObjectId.
     */
    private HashMap<String, BinaryDataObject> bdoInDataObjectPackageIdMap;

    /**
     * The map of all the PhysicalDataObjects by inDataPackageObjectId.
     */
    private HashMap<String, PhysicalDataObject> pdoInDataObjectPackageIdMap;

    /**
     * The management metadata xml data.
     */
    private String managementMetadataXmlData;

    /**
     * The "ghost" root ArchiveUnit containing as childs the ArchiveUnits at root
     * level.
     */
    private ArchiveUnit ghostRootAu;

    // Inner objects

    /**
     * The kept metadata list for export.
     */
    private List<String> exportMetadataList;

    /**
     * The counter used to generate inDataObjectPackageIds.
     */
    private int idCounter;

    /**
     * The counter used to generate RefID in XML.
     */
    private int refIdCounter;

    /**
     * The counter of in/out events used for progress log.
     */
    private int inOutCounter;

    /**
     * The map used to accumulate the touched ArchiveUnits or DataObjects
     * inDataObjectPackageIds during a treatment. It's useful to touch only one time
     * all ArchiveUnits or DataObjects in the graph, or to count the time they are
     * touched.
     */
    private final HashMap<String, Integer> touchedInDataObjectPackageIdMap;

    /**
     * The Constant NORMALIZATION_STATUS_UNKNOWN.
     */
    public static final int NORMALIZATION_STATUS_UNKNOWN = 0;

    /**
     * The Constant NORMALIZATION_STATUS_OK.
     */
    public static final int NORMALIZATION_STATUS_OK = 1;

    /**
     * The Constant NORMALIZATION_STATUS_KO.
     */
    public static final int NORMALIZATION_STATUS_KO = 2;

    /**
     * The indicator of Vitam SIP rules respect.
     */
    private int vitamNormalizationStatus;

    // Constructors

    /**
     * Instantiates a new archive transfer. Used for json deserialization.
     */
    public DataObjectPackage() {
        this.auInDataObjectPackageIdMap = new HashMap<>();
        this.dogInDataObjectPackageIdMap = new HashMap<>();

        this.bdoInDataObjectPackageIdMap = new HashMap<>();
        this.pdoInDataObjectPackageIdMap = new HashMap<>();
        this.ghostRootAu = new ArchiveUnit();
        Content c = new Content();
        try {
            c.addNewMetadata("Title", "GhostRootAu");
        } catch (SEDALibException ignored) {
            //ignored
        }
        this.ghostRootAu.setDataObjectPackage(this);

        this.exportMetadataList = null;
        this.resetIdCounter();
        this.resetRefIdCounter();
        this.resetInOutCounter();
        this.touchedInDataObjectPackageIdMap = new HashMap<>();
        this.vitamNormalizationStatus = NORMALIZATION_STATUS_UNKNOWN;
    }

    // Methods

    /**
     * Checks if is inDataPackageObjectId already known in this DataObjectPackage.
     *
     * @param inDataPackageObjectId the id in DataObjectPackage
     * @return true, if the id is used in DataObjectPackage
     */
    private boolean isInDataObjectPackageIdUsed(String inDataPackageObjectId) {
        if (auInDataObjectPackageIdMap.containsKey(inDataPackageObjectId))
            return true;
        if (dogInDataObjectPackageIdMap.containsKey(inDataPackageObjectId))
            return true;
        if (bdoInDataObjectPackageIdMap.containsKey(inDataPackageObjectId))
            return true;
        //noinspection RedundantIfStatement
        return pdoInDataObjectPackageIdMap.containsKey(inDataPackageObjectId);
    }

    /**
     * Adds an ArchiveUnit as an DataObjectPackage element, defining a uniqID if not
     * already defined (null).
     * <p>
     * This is only useful when you create an ArchiveUnit without giving an outer
     * DataObjectPackage in constructor, for example during json deserialization.
     *
     * @param au the ArchiveUnit
     * @throws SEDALibException when the ArchiveUnit has a defined UniqId which is
     *                          already in the DataObjectPackage
     */
    public void addArchiveUnit(ArchiveUnit au) throws SEDALibException {
        if (au.inDataPackageObjectId == null)
            au.inDataPackageObjectId = getNextInDataObjectPackageID();
        if (isInDataObjectPackageIdUsed(au.inDataPackageObjectId))
            throw new SEDALibException(
                    "Deux objets ne peuvent avoir la même référence [" + au.inDataPackageObjectId + "]");
        auInDataObjectPackageIdMap.put(au.inDataPackageObjectId, au);
        au.setDataObjectPackage(this);
    }

    /**
     * Adds a data object group as an DataObjectPackage element, defining a uniqID if
     * not already defined (null).
     * <p>
     * This is only useful when you create a DataObjectGroup without giving an outer
     * DataObjectPackage in constructor, for example during json deserialization.
     *
     * @param dog the DataObjectGroup
     * @throws SEDALibException when the DataObjectGroup has a defined UniqId which
     *                          is already in the DataObjectPackage
     */
    public void addDataObjectGroup(DataObjectGroup dog) throws SEDALibException {
        if (dog.inDataPackageObjectId == null)
            dog.inDataPackageObjectId = getNextInDataObjectPackageID();
        if (isInDataObjectPackageIdUsed(dog.inDataPackageObjectId))
            throw new SEDALibException(
                    "Deux objets ne peuvent avoir la même référence [" + dog.inDataPackageObjectId + "]");
        dogInDataObjectPackageIdMap.put(dog.inDataPackageObjectId, dog);
        dog.setDataObjectPackage(this);
    }

    /**
     * Adds a binary data object as an DataObjectPackage element, defining a uniqID if
     * not already defined (null).
     * <p>
     * This is only useful when you create a BinaryDataObject without giving an
     * outer DataObjectPackage in constructor, for example during json
     * deserialization.
     *
     * @param bdo the BinaryDataObject
     * @throws SEDALibException when the BinaryDataObject has a defined UniqId which
     *                          is already in the DataObjectPackage
     */
    public void addBinaryDataObject(BinaryDataObject bdo) throws SEDALibException {
        if (bdo.inDataPackageObjectId == null)
            bdo.inDataPackageObjectId = getNextInDataObjectPackageID();
        if (isInDataObjectPackageIdUsed(bdo.inDataPackageObjectId))
            throw new SEDALibException(
                    "Deux objets ne peuvent avoir la même référence [" + bdo.inDataPackageObjectId + "]");
        bdoInDataObjectPackageIdMap.put(bdo.inDataPackageObjectId, bdo);
        bdo.setDataObjectPackage(this);
    }

    /**
     * Adds a PhysicalDataObject as an DataObjectPackage element, defining a uniqID if
     * not already defined (null).
     * <p>
     * This is only useful when you create a PhysicalDataObject without giving an
     * outer DataObjectPackage in constructor, for example during json
     * deserialization.
     *
     * @param pdo the PhysicalDataObject
     * @throws SEDALibException when the PhysicalDataObject has a defined UniqId
     *                          which is already in the DataObjectPackage
     */
    public void addPhysicalDataObject(PhysicalDataObject pdo) throws SEDALibException {
        if (pdo.inDataPackageObjectId == null)
            pdo.inDataPackageObjectId = getNextInDataObjectPackageID();
        if (isInDataObjectPackageIdUsed(pdo.inDataPackageObjectId))
            throw new SEDALibException(
                    "Deux objets ne peuvent avoir la même référence [" + pdo.inDataPackageObjectId + "]");
        pdoInDataObjectPackageIdMap.put(pdo.inDataPackageObjectId, pdo);
        pdo.setDataObjectPackage(this);
    }

    /**
     * Reset the touched ArchiveUnit or DataObject inDataPackageObjectId Map, the
     * Map is then empty and ready to use.
     */
    public void resetTouchedInDataObjectPackageIdMap() {
        touchedInDataObjectPackageIdMap.clear();
    }

    /**
     * Checks if the ArchiveUnit or DataObject with given inDataPackageObjectId has
     * been touched.
     *
     * @param inDataObjectPackageId the id in DataObjectPackage
     * @return true, if has been touched
     */
    public boolean isTouchedInDataObjectPackageId(String inDataObjectPackageId) {
        return touchedInDataObjectPackageIdMap.containsKey(inDataObjectPackageId);
    }

    /**
     * Adds the ArchiveUnit or DataObject inDataPackageObjectId in the touched map,
     * with a count value set to 1.
     * <p>
     * This is used when going through all the ArchiveUnits of the graph so has to
     * be sure to treat them only one time, for example when exporting to XML or
     * when searching for the list of roots after an import from xml
     *
     * @param inDataObjectPackageId the id in DataObjectPackage
     */
    public void addTouchedInDataObjectPackageId(String inDataObjectPackageId) {
        touchedInDataObjectPackageIdMap.put(inDataObjectPackageId, 1);
    }

    /**
     * Increment the ArchiveUnit or DataObject inDataPackageObjectId value in the
     * touched map, if touched for the first time the count value is set to 1.
     *
     * @param inDataObjectPackageId the id in DataObjectPackage
     */
    public void incTouchedInDataObjectPackageId(String inDataObjectPackageId) {
        Integer value = touchedInDataObjectPackageIdMap.get(inDataObjectPackageId);
        if (value == null)
            touchedInDataObjectPackageIdMap.put(inDataObjectPackageId, 1);
        else
            touchedInDataObjectPackageIdMap.put(inDataObjectPackageId, value + 1);
    }

    /**
     * Gets the count value for a touched id in DataObjectPackage.
     *
     * @param inDataObjectPackageId the id in DataObjectPackage
     * @return the count value for the touched id in DataObjectPackage, or null if
     * not touched
     */
    public Integer getTouchedInDataObjectPackageId(String inDataObjectPackageId) {
        return touchedInDataObjectPackageIdMap.get(inDataObjectPackageId);
    }

    /**
     * Gets the ArchiveUnits count.
     *
     * @return the ArchiveUnits count
     */
    public int getArchiveUnitCount() {
        return auInDataObjectPackageIdMap.size();
    }

    /**
     * Gets the DataObjectGroups count.
     *
     * @return the DataObjectGroups count
     */
    public int getDataObjectGroupCount() {
        return dogInDataObjectPackageIdMap.size();
    }

    /**
     * Gets the BinaryDataObjects count.
     *
     * @return the BinaryDataObjects count
     */
    public int getBinaryDataObjectCount() {
        return bdoInDataObjectPackageIdMap.size();
    }

    /**
     * Gets the BinaryDataObjects total size in bytes.
     *
     * @return the BinaryDataObjects total size
     */
    public long getDataObjectsTotalSize() {
        long result = 0;
        for (Map.Entry<String, BinaryDataObject> pair : bdoInDataObjectPackageIdMap.entrySet()) {
            IntegerType size = pair.getValue().size;
            if (size != null)
                result += size.getValue();
        }
        return result;
    }

    /**
     * Gets the PhysicalDataObjects count.
     *
     * @return the PhysicalDataObjects count
     */
    public int getPhysicalDataObjectCount() {
        return pdoInDataObjectPackageIdMap.size();
    }

    /**
     * Gets the ArchiveUnit by inDataObjectPackageId.
     *
     * @param inDataObjectPackageId the ArchiveUnit id in DataObjectPackage
     * @return the ArchiveUnit or null if not found
     */
    public ArchiveUnit getArchiveUnitById(String inDataObjectPackageId) {
        return auInDataObjectPackageIdMap.get(inDataObjectPackageId);
    }

    /**
     * Gets the DataObjectGroup by inDataObjectPackageId.
     *
     * @param inDataObjectPackageId the DataObjectGroup id in DataObjectPackage
     * @return the DataObjectGroup or null if not found
     */
    public DataObjectGroup getDataObjectGroupById(String inDataObjectPackageId) {
        return dogInDataObjectPackageIdMap.get(inDataObjectPackageId);
    }

    /**
     * Gets the DataObject (group, binary or physical) by inDataPackageObjectId.
     *
     * @param inDataObjectPackageId the DataObject id in DataObjectPackage
     * @return the DataObject or null if not found
     */
    public DataObject getDataObjectById(String inDataObjectPackageId) {
        DataObject dataObject;

        dataObject = this.dogInDataObjectPackageIdMap.get(inDataObjectPackageId);
        if (dataObject != null)
            return dataObject;
        dataObject = this.bdoInDataObjectPackageIdMap.get(inDataObjectPackageId);
        if (dataObject != null)
            return dataObject;
        dataObject = this.pdoInDataObjectPackageIdMap.get(inDataObjectPackageId);
        return dataObject;
    }

    /**
     * Gets the next ID to use in DataObjectPackage.
     * <p>
     * It uses the idCounter to generate an ID in the "IDxxx" form. It tests if this
     * ID is already used, and if so increment idCounter till the ID is uniq.
     *
     * @return the next ID in DataObjectPackage
     */
    public String getNextInDataObjectPackageID() {
        String id = "ID" + idCounter++;
        while (auInDataObjectPackageIdMap.containsKey(id) || dogInDataObjectPackageIdMap.containsKey(id)
                || bdoInDataObjectPackageIdMap.containsKey(id) || pdoInDataObjectPackageIdMap.containsKey(id))
            id = "ID" + idCounter++;
        return id;
    }

    /**
     * Gets the next RefID to use in DataObjectPackage.
     * <p>
     * It uses the refIdCounter to generate an ID in the "RefIDxxx" form.
     *
     * @return the next RefID in DataObjectPackage
     */
    public String getNextRefID() {
        return "RefID" + refIdCounter++;
    }

    /**
     * Reset refID counter to value 1.
     * <p>
     * This counter is used to generate the RefIDs in the manifest during export
     */
    public void resetRefIdCounter() {
        refIdCounter = 1;
    }

    /**
     * Reset ID counter to value 10.
     * <p>
     * This counter is used to generate the IDs in the manifest during import. It
     * begins at 10 to prevent an id collision with those used in headers of SEDA
     * messages
     */
    public void resetIdCounter() {
        idCounter = 10;
    }

    /**
     * Human readable file size, using the best scale (B, kB, MB, GB or TB) in bytes
     *
     * @param size the size
     * @return the size in human readable string format
     */
    private static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Gets the summary description of DataObjectPackage.
     * <p>
     * It list the number of ArchiveUnits, DataObjectGroup, BinaryDataObject (with
     * size in bytes) and PhysicalDatObject
     *
     * @return the description String
     */
    public String getDescription() {
        String result;

        result = "  - " + getArchiveUnitCount() + " ArchiveUnit(s)\n  - " + getDataObjectGroupCount()
                + " DataObjectGroup(s)\n  - " + getBinaryDataObjectCount() + " BinaryDataObject(s) ("
                + readableFileSize(getDataObjectsTotalSize()) + ")\n  - " + getPhysicalDataObjectCount()
                + " PhysicalDataObject(s)";

        return result;
    }

    /**
     * Sets the all references by objects.
     * <p>
     * In {@link DataObjectRefList} and {@link ArchiveUnitRefList} the objects can
     * be kept by reference or (exclusive) by Id. When it's needed to work on Ids,
     * and to change them, this function permit to be sure that all lists use the
     * object reference form.
     */
    private void setAllReferencesByObjects() {
        // These calls guaranty that the references lists use objects as references and
        // not inDataObjectPackageID, as the ids will be changed during the process
        for (Map.Entry<String, ArchiveUnit> pair : getAuInDataObjectPackageIdMap().entrySet()) {
            pair.getValue().getChildrenAuList().getArchiveUnitList();
            pair.getValue().getDataObjectRefList().getDataObjectList();
        }
        getGhostRootAu().getChildrenAuList().getArchiveUnitList();
    }

    /**
     * Move the whole content from childDataObjectPackage in the present
     * DataObjectPackage as targetAU sub-graph, and then empty
     * childDataObjectPackage.
     *
     * @param childDataObjectPackage the child DataObjectPackage
     * @param targetAU               the target AU
     */
    public void moveContentFromDataObjectPackage(DataObjectPackage childDataObjectPackage, ArchiveUnit targetAU) {
        childDataObjectPackage.setAllReferencesByObjects();

        for (Map.Entry<String, ArchiveUnit> pair : childDataObjectPackage.getAuInDataObjectPackageIdMap().entrySet()) {
            pair.getValue().setInDataObjectPackageId(null);
            try {
                addArchiveUnit(pair.getValue());
            } catch (SEDALibException ignored) {
                // impossible
            }
        }

        for (Map.Entry<String, DataObjectGroup> pair : childDataObjectPackage.getDogInDataObjectPackageIdMap()
                .entrySet()) {
            pair.getValue().setInDataObjectPackageId(null);
            try {
                addDataObjectGroup(pair.getValue());
            } catch (SEDALibException ignored) {
                // impossible
            }
        }

        for (Map.Entry<String, BinaryDataObject> pair : childDataObjectPackage.getBdoInDataObjectPackageIdMap()
                .entrySet()) {
            pair.getValue().setInDataObjectPackageId(null);
            try {
                addBinaryDataObject(pair.getValue());
            } catch (SEDALibException ignored) {
                // impossible
            }
        }

        for (Map.Entry<String, PhysicalDataObject> pair : childDataObjectPackage.getPdoInDataObjectPackageIdMap()
                .entrySet()) {
            pair.getValue().setInDataObjectPackageId(null);
            try {
                addPhysicalDataObject(pair.getValue());
            } catch (SEDALibException ignored) {
                // impossible
            }
        }

        for (ArchiveUnit au : childDataObjectPackage.ghostRootAu.getChildrenAuList().getArchiveUnitList())
            targetAU.addChildArchiveUnit(au);

        childDataObjectPackage.setAuInDataObjectPackageIdMap(new HashMap<>());
        childDataObjectPackage.setDogInDataObjectPackageIdMap(new HashMap<>());
        childDataObjectPackage.setBdoInDataObjectPackageIdMap(new HashMap<>());
        childDataObjectPackage.setPdoInDataObjectPackageIdMap(new HashMap<>());
        childDataObjectPackage.setGhostRootAu(new ArchiveUnit());
        Content c = new Content();
        try {
            c.addNewMetadata("Title", "GhostRootAu");
        } catch (SEDALibException ignored) {
            //ignored
        }
        childDataObjectPackage.getGhostRootAu().setDataObjectPackage(this);
        childDataObjectPackage.getGhostRootAu().setContent(c);

    }

    // Test and normalization methods

    /**
     * ArchiveUnits path from root to string.
     *
     * @param path the ArchiveUnits path
     * @return the string representation
     */
    private String archiveUnitPathToString(List<ArchiveUnit> path) {
        StringBuilder result = new StringBuilder();
        if (path != null) {
            for (ArchiveUnit au : path)
                result.append(au.getInDataObjectPackageId()).append("->");
        }
        return result.toString();
    }

    /**
     * Verify the ArchiveUnit is not in a cycle and recurse in his children
     *
     * @param au   the ArchiveUnit
     * @param path the ArchiveUnits path
     * @throws SEDALibException if the graph is cyclic
     */
    private void verifyAcyclicArchiveUnit(ArchiveUnit au, List<ArchiveUnit> path) throws SEDALibException {
        if (path.contains(au))
            throw new SEDALibException(
                    "Cycle détecté " + archiveUnitPathToString(path) + au.getInDataObjectPackageId());
        if (isTouchedInDataObjectPackageId(au.getInDataObjectPackageId()))
            return;
        path.add(au);
        for (ArchiveUnit childAu : au.getChildrenAuList().getArchiveUnitList())
            verifyAcyclicArchiveUnit(childAu, path);
        path.remove(au);
        addTouchedInDataObjectPackageId(au.getInDataObjectPackageId());
    }

    /**
     * Verify if there's a cycle in the ArchiveUnits graph.
     *
     * @throws SEDALibException if the graph is cyclic
     */
    public void verifyAcyclic() throws SEDALibException {
        List<ArchiveUnit> path = new ArrayList<>();
        resetTouchedInDataObjectPackageIdMap();
        for (ArchiveUnit childAu : ghostRootAu.getChildrenAuList().getArchiveUnitList())
            verifyAcyclicArchiveUnit(childAu, path);
    }

    /**
     * Increment the number of references for each DataObjectGroup in this
     * ArchiveUnit, and recurse in his children.
     *
     * @param archiveUnit the ArchiveUnit
     */
    private void countDogReferencesArchiveUnit(ArchiveUnit archiveUnit) {
        if (isTouchedInDataObjectPackageId(archiveUnit.getInDataObjectPackageId()))
            return;
        for (DataObject zdo : archiveUnit.getDataObjectRefList().getDataObjectList()) {
            if (zdo instanceof DataObjectGroup) {
                incTouchedInDataObjectPackageId(zdo.getInDataObjectPackageId());
            }
        }
        for (ArchiveUnit childAu : archiveUnit.getChildrenAuList().getArchiveUnitList())
            countDogReferencesArchiveUnit(childAu);
        addTouchedInDataObjectPackageId(archiveUnit.getInDataObjectPackageId());
    }

    /**
     * Verify if all DataObject lists in ArchiveUnit can be reduced to one
     * DataObjectGroup.
     *
     * @throws SEDALibException if one not reducible DataObject list is detected or
     *                          a DataObjectGroup has a LogBook wrong format
     */
    public void verifyDogUnicityCapacity() throws SEDALibException {
        resetTouchedInDataObjectPackageIdMap();
        // count all Dog declaration and reference
        for (ArchiveUnit childAu : ghostRootAu.getChildrenAuList().getArchiveUnitList())
            countDogReferencesArchiveUnit(childAu);
        // verify that there is no Dog with reference (count>1) to merge with other
        // elements in one ArchiveUnit
        for (Entry<String, ArchiveUnit> pair : getAuInDataObjectPackageIdMap().entrySet()) {
            DataObjectRefList dorl = pair.getValue().getDataObjectRefList();
            boolean referencedDog = false;
            for (DataObject zdo : dorl.getDataObjectList()) {
                if ((zdo instanceof DataObjectGroup) &&
                     (getTouchedInDataObjectPackageId(zdo.getInDataObjectPackageId()) > 1))
                        referencedDog = true;
            }
            if (referencedDog && (dorl.getCount() > 1))
                throw new SEDALibException("Regroupement des références de DataObject impossible sur l'ArchiveUnit ["
                        + pair.getValue().getInDataObjectPackageId() + "]");
        }
    }

    /**
     * Put all the DataObjects from the DataObjectRefList in the DataObjectGroup.
     *
     * @param dataObjectGroup the DataObjectGroup
     * @param dorl            the DataObjectRefList
     * @throws SEDALibException if a DataObjectGroup can't be merge due to LogBook
     *                          wrong format
     */
    private void putAllInDog(DataObjectGroup dataObjectGroup, DataObjectRefList dorl) throws SEDALibException {
        for (DataObject zdo : dorl.getDataObjectList()) {
            if (zdo instanceof DataObjectGroup) {
                dataObjectGroup.mergeDataObjectGroup((DataObjectGroup) zdo);
                dogInDataObjectPackageIdMap.remove(zdo.getInDataObjectPackageId());
            } else if ((zdo instanceof BinaryDataObject) || (zdo instanceof PhysicalDataObject))
                dataObjectGroup.addDataObject(zdo);
        }
    }

    /**
     * Normalize each ArchiveUnit to set a uniq DataObjectGroup with all the
     * DataObjects inside.
     *
     * @throws SEDALibException if one not reducible DataObject list is detected or
     *                          one DataObjectGroup can't be merge due to LogBook
     *                          wrong format. Important: nothing has been modified
     *                          in the DataObjectPackage.
     */
    public void normalizeUniqDataObjectGroup() throws SEDALibException {
        verifyDogUnicityCapacity();

        for (Entry<String, ArchiveUnit> pair : getAuInDataObjectPackageIdMap().entrySet()) {
            DataObjectRefList dorl = pair.getValue().getDataObjectRefList();
            if (((dorl.getCount() == 1) && !(dorl.getDataObjectList().get(0) instanceof DataObjectGroup))
                    || (dorl.getCount() > 1)) {
                DataObjectGroup dog = new DataObjectGroup(this, null);
                putAllInDog(dog, pair.getValue().getDataObjectRefList());
                dorl = new DataObjectRefList(this);
                dorl.add(dog);
                pair.getValue().setDataObjectRefList(dorl);
            }
        }
    }

    /**
     * Remove from DataObjectPackage lists all DataObjects (DataObjectGroup, BinaryDataObject,
     * PhysicalDataObject) not used by an ArchiveUnit
     *
     * @param spl the SEDALib progress logger
     * @throws InterruptedException the interrupted exception
     */
    public void removeUnusedDataObjects(SEDALibProgressLogger spl) throws InterruptedException {
        HashSet<DataObject> usedDataObjects = new HashSet<>(1000);
        for (Entry<String, ArchiveUnit> pair : getAuInDataObjectPackageIdMap().entrySet()) {
            DataObjectRefList dorl = pair.getValue().getDataObjectRefList();
            for (DataObject dataObject : dorl.getDataObjectList()) {
                usedDataObjects.add(dataObject);
                if (dataObject instanceof DataObjectGroup) {
                    usedDataObjects.addAll(((DataObjectGroup) dataObject).getPhysicalDataObjectList());
                    usedDataObjects.addAll(((DataObjectGroup) dataObject).getBinaryDataObjectList());
                }
            }
        }

        Iterator<Entry<String, DataObjectGroup>> iteratorDog = getDogInDataObjectPackageIdMap().entrySet().iterator();
        while (iteratorDog.hasNext()) {
            Entry<String, DataObjectGroup> entry = iteratorDog.next();
            if (!usedDataObjects.contains(entry.getValue())) {
                iteratorDog.remove();
                doProgressLog(spl, SEDALibProgressLogger.GLOBAL, "sedalib: un DataObjectGroup [" + entry.getKey() + "] déclaré n'est pas utilisé, il est déréférencé.", null);
            }
        }

        Iterator<Entry<String, BinaryDataObject>> iteratorBdo = getBdoInDataObjectPackageIdMap().entrySet().iterator();
        while (iteratorBdo.hasNext()) {
            Entry<String, BinaryDataObject> entry = iteratorBdo.next();
            if (!usedDataObjects.contains(entry.getValue())) {
                iteratorBdo.remove();
                doProgressLog(spl, SEDALibProgressLogger.GLOBAL, "sedalib: un BinaryDataObject [" + entry.getKey() + "] déclaré n'est pas utilisé, il est déréférencé.", null);
            }
        }

        Iterator<Entry<String, PhysicalDataObject>> iteratorPdo = getPdoInDataObjectPackageIdMap().entrySet().iterator();
        while (iteratorPdo.hasNext()) {
            Entry<String, PhysicalDataObject> entry = iteratorPdo.next();
            if (!usedDataObjects.contains(entry.getValue())) {
                iteratorPdo.remove();
                doProgressLog(spl, SEDALibProgressLogger.GLOBAL, "sedalib: un PhysicalDataObject [" + entry.getKey() + "] déclaré n'est pas utilisé, il est déréférencé.", null);
            }
        }
    }

    /**
     * Regenerate ArchiveUnit id and maintain an ordered list of DataObjectGroup to
     * reindex.
     *
     * @param archiveUnit                the archive unit
     * @param orderedDataObjectGroupList the ordered data object group list
     */
    private void regenerateArchiveUnitId(ArchiveUnit archiveUnit, List<DataObjectGroup> orderedDataObjectGroupList) {
        if (archiveUnit.inDataPackageObjectId == null) {
            DataObjectGroup dog = archiveUnit.getDataObjectRefList().getNormalizedDataObjectGroup();
            if (dog != null)
                orderedDataObjectGroupList.add(dog);
            try {
                addArchiveUnit(archiveUnit);
            } catch (SEDALibException ignored) {
                // impossible
            }
            for (ArchiveUnit childAu : archiveUnit.getChildrenAuList().getArchiveUnitList())
                regenerateArchiveUnitId(childAu, orderedDataObjectGroupList);
        }
    }

    /**
     * Regenerate DataObjectGroup id and all contained DataObjects.
     *
     * @param dataObjectGroup the DataObjectGroup
     */
    private void regenerateDataObjectGroup(DataObjectGroup dataObjectGroup) {
        if (dataObjectGroup.inDataPackageObjectId == null) {
            try {
                addDataObjectGroup(dataObjectGroup);
            } catch (SEDALibException ignored) {
                // impossible
            }
            for (BinaryDataObject bdo : dataObjectGroup.getBinaryDataObjectList()) {
                bdo.inDataPackageObjectId = null;
                try {
                    addBinaryDataObject(bdo);
                } catch (SEDALibException ignored) {
                    // impossible
                }
            }
            for (PhysicalDataObject pdo : dataObjectGroup.getPhysicalDataObjectList()) {
                pdo.inDataPackageObjectId = null;
                try {
                    addPhysicalDataObject(pdo);
                } catch (SEDALibException ignored) {
                    // impossible
                }
            }
        }
    }

    /**
     * Removes the archive unit and data object group ids before regeneration.
     */
    private void removeArchiveUnitAndDataObjectGroupId() {
        for (Map.Entry<String, ArchiveUnit> pair : auInDataObjectPackageIdMap.entrySet())
            pair.getValue().setInDataObjectPackageId(null);
        for (Map.Entry<String, DataObjectGroup> pair : dogInDataObjectPackageIdMap.entrySet())
            pair.getValue().setInDataObjectPackageId(null);
    }

    /**
     * Regenerate all IDs of ArchiveUnits, DataObjectGroup, BinaryDataObject and
     * PhysicalDataObject in incremented IDxxx form.
     */
    public void regenerateContinuousIds() {
        setAllReferencesByObjects();
        removeArchiveUnitAndDataObjectGroupId();
        resetIdCounter();

        auInDataObjectPackageIdMap = new HashMap<>();
        dogInDataObjectPackageIdMap = new HashMap<>();
        bdoInDataObjectPackageIdMap = new HashMap<>();
        pdoInDataObjectPackageIdMap = new HashMap<>();

        resetInOutCounter();
        List<DataObjectGroup> orderedDataObjectGroupList = new ArrayList<>(
                dogInDataObjectPackageIdMap.size());
        for (ArchiveUnit root : ghostRootAu.getChildrenAuList().getArchiveUnitList())
            regenerateArchiveUnitId(root, orderedDataObjectGroupList);

        resetInOutCounter();
        for (DataObjectGroup zdo : orderedDataObjectGroupList)
            regenerateDataObjectGroup(zdo);
    }

    /**
     * Actualise DataObject id and all contained DataObjects in maps.
     *
     * @param dataObject the DataObject
     */
    private void actualiseDataObjectId(DataObject dataObject) {
        if (!isTouchedInDataObjectPackageId(dataObject.getInDataObjectPackageId())) {
            if (dataObject instanceof DataObjectGroup) {
                DataObjectGroup dataObjectGroup = (DataObjectGroup) dataObject;
                dogInDataObjectPackageIdMap.put(dataObjectGroup.inDataPackageObjectId, dataObjectGroup);
                for (BinaryDataObject bdo : dataObjectGroup.getBinaryDataObjectList())
                    bdoInDataObjectPackageIdMap.put(bdo.getInDataObjectPackageId(), bdo);
                for (PhysicalDataObject pdo : dataObjectGroup.getPhysicalDataObjectList())
                    pdoInDataObjectPackageIdMap.put(pdo.getInDataObjectPackageId(), pdo);
            } else if (dataObject instanceof BinaryDataObject)
                bdoInDataObjectPackageIdMap.put(dataObject.getInDataObjectPackageId(), (BinaryDataObject) dataObject);
            else if (dataObject instanceof PhysicalDataObject)
                pdoInDataObjectPackageIdMap.put(dataObject.getInDataObjectPackageId(), (PhysicalDataObject) dataObject);
        }
    }

    /**
     * Actualise ArchiveUnit in maps and maintain an ordered list of all DataObjects to
     * reindex.
     *
     * @param archiveUnit           the archive unit
     * @param orderedDataObjectList the ordered data object list
     */
    private void actualiseArchiveUnitId(ArchiveUnit archiveUnit, List<DataObject> orderedDataObjectList) {
        if (!isTouchedInDataObjectPackageId(archiveUnit.inDataPackageObjectId)) {
            addTouchedInDataObjectPackageId(archiveUnit.inDataPackageObjectId);
            orderedDataObjectList.addAll(archiveUnit.getDataObjectRefList().getDataObjectList());
            auInDataObjectPackageIdMap.put(archiveUnit.inDataPackageObjectId, archiveUnit);
            for (ArchiveUnit childAu : archiveUnit.getChildrenAuList().getArchiveUnitList())
                actualiseArchiveUnitId(childAu, orderedDataObjectList);
        }
    }

    /**
     * Actualise all maps used to reference oll ArchiveUnits, DataObjectGroup, BinaryDataObject and
     * PhysicalDataObject IDs.
     * <p>
     * This is usefull when an ArchiveUnit is removed, as the non more usefull reference is kept in the maps,
     * and so all ArchiveUnit defining the object can't be garbage collected.
     */
    public void actualiseIdMaps() {
        List<DataObject> orderedDataObjectList = new ArrayList<>(
                dogInDataObjectPackageIdMap.size() +
                        bdoInDataObjectPackageIdMap.size() +
                        pdoInDataObjectPackageIdMap.size());
        setAllReferencesByObjects();
        resetTouchedInDataObjectPackageIdMap();
        auInDataObjectPackageIdMap = new HashMap<>();
        dogInDataObjectPackageIdMap = new HashMap<>();
        bdoInDataObjectPackageIdMap = new HashMap<>();
        pdoInDataObjectPackageIdMap = new HashMap<>();

        for (ArchiveUnit root : ghostRootAu.getChildrenAuList().getArchiveUnitList())
            actualiseArchiveUnitId(root, orderedDataObjectList);
        for (DataObject dataObject : orderedDataObjectList)
            actualiseDataObjectId(dataObject);
    }

    /**
     * Normalize the structure in Vitam standard form:
     * <ul>
     * <li>verify that there's no cycle in the ArchiveUnit graph</li>
     * <li>verify that it's possible to get only one DataObjectGroup by
     * ArchiveUnit</li>
     * <li>for each ArchiveUnit set a uniq DataObjectGroup with all the DataObjects
     * inside</li>
     * </ul>
     * and set the vitamNormalizationStatus value.
     *
     * @param spl the SEDALib progress logger
     * @throws SEDALibException     if one verification fail. Important: nothing has                          been modified in the DataObjectPackage.
     * @throws InterruptedException if interrupted
     */
    public void vitamNormalize(SEDALibProgressLogger spl) throws SEDALibException, InterruptedException {
        vitamNormalizationStatus = NORMALIZATION_STATUS_KO;
        verifyAcyclic();
        normalizeUniqDataObjectGroup();
        removeUnusedDataObjects(spl);
        vitamNormalizationStatus = NORMALIZATION_STATUS_OK;
    }


    /**
     * Remove empty (no child archive unit, no data object) archive unit from all it's fathers and from the data object package.
     *
     * @param archiveUnit the empty archive unit
     * @return true if it's done, false if it's not possible (not empty or not found)
     */
    public boolean removeEmptyArchiveUnit(ArchiveUnit archiveUnit){
        if (archiveUnit.getChildrenAuList().getCount()!=0)
            return false;
        if (archiveUnit.getDataObjectRefList().getCount()!=0)
            return false;
        for (Map.Entry<String,ArchiveUnit> e:auInDataObjectPackageIdMap.entrySet()){
            e.getValue().getChildrenAuList().getArchiveUnitList().remove(archiveUnit);
        }
        return auInDataObjectPackageIdMap.remove(archiveUnit.inDataPackageObjectId)!=null;
    }

    // SEDA XML exporter

    /**
     * The ID comparator to sort elements by inDataPackageObjectId.
     */
    private static final Comparator<String> idComparator = (id1, id2) -> {
        int num1;
        int num2;
        if (id1.toLowerCase().startsWith("id") && id2.toLowerCase().startsWith("id")) {
            try {
                num1 = Integer.parseInt(id1.substring(2));
                num2 = Integer.parseInt(id2.substring(2));
                return num1 - num2;
            } catch (NumberFormatException ignored) {
                //ignored
            }
        }
        return id1.compareTo(id2);
    };

    /**
     * Export data object package, DataObjects part, of SEDA DataObjectPackage XML.
     *
     * @param xmlWriter             the SEDAXMLStreamWriter generating the SEDA manifest
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws InterruptedException if export process is interrupted
     * @throws SEDALibException     if the XML can't be written
     */
    public void exportDataObjectPackageObjects(SEDAXMLStreamWriter xmlWriter, SEDALibProgressLogger sedaLibProgressLogger)
            throws InterruptedException, SEDALibException {
        try {
            xmlWriter.writeStartElement("DataObjectPackage");
            DataObjectGroup dog;
            BinaryDataObject bdo;
            PhysicalDataObject pdo;
            String[] tempArray;

            resetTouchedInDataObjectPackageIdMap();
            // first write all DataObjectGroup
            Set<String> dogSet = dogInDataObjectPackageIdMap.keySet();
            tempArray = dogSet.toArray(new String[0]);
            Arrays.sort(tempArray);
            for (String s : tempArray) {
                dog = dogInDataObjectPackageIdMap.get(s);
                dog.toSedaXml(xmlWriter, sedaLibProgressLogger);
                for (BinaryDataObject b : dog.getBinaryDataObjectList())
                    addTouchedInDataObjectPackageId(b.inDataPackageObjectId);
                for (PhysicalDataObject p : dog.getPhysicalDataObjectList())
                    addTouchedInDataObjectPackageId(p.inDataPackageObjectId);
            }

            // then all alone BinaryDataObject
            Set<String> bdoSet = bdoInDataObjectPackageIdMap.keySet();
            tempArray = bdoSet.toArray(new String[0]);
            Arrays.sort(tempArray);
            for (String s : tempArray) {
                bdo = bdoInDataObjectPackageIdMap.get(s);
                if (!isTouchedInDataObjectPackageId(bdo.inDataPackageObjectId))
                    bdo.toSedaXml(xmlWriter, sedaLibProgressLogger);
            }

            // and at last all alone PhysicalDataObject
            Set<String> pdoSet = pdoInDataObjectPackageIdMap.keySet();
            tempArray = pdoSet.toArray(new String[0]);
            Arrays.sort(tempArray);
            for (String s : tempArray) {
                pdo = pdoInDataObjectPackageIdMap.get(s);
                if (!isTouchedInDataObjectPackageId(pdo.inDataPackageObjectId))
                    pdo.toSedaXml(xmlWriter, sedaLibProgressLogger);
            }
        } catch (XMLStreamException e) {
            throw new SEDALibException("Erreur d'écriture XML des métadonnées des DataObjects", e);
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP,
                "sedalib: " + getNextInOutCounter() + " métadonnées DataObject exportées dans le DataObjectPackage", null);
    }

    /**
     * Export data object package, ArchiveUnits and global metadata part, of SEDA
     * DataObjectPackage XML.
     *
     * @param xmlWriter             the SEDAXMLStreamWriter generating the SEDA manifest
     * @param imbricateFlag         indicates if the manifest ArchiveUnits are to be
     *                              exported in imbricate mode (true) or in flat mode
     *                              (false)
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException     if the XML can't be written
     * @throws InterruptedException if export process is interrupted
     */
    public void exportDataObjectPackageMetadata(SEDAXMLStreamWriter xmlWriter, boolean imbricateFlag,
                                                SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        try {
            resetTouchedInDataObjectPackageIdMap();
            xmlWriter.writeStartElement("DescriptiveMetadata");
            if (!imbricateFlag) {
                Set<String> auSet = auInDataObjectPackageIdMap.keySet();
                String[] tempArray = auSet.toArray(new String[0]);
                Arrays.sort(tempArray, idComparator);
                for (String s : tempArray)
                    auInDataObjectPackageIdMap.get(s).toSedaXml(xmlWriter, false, sedaLibProgressLogger);
            } else {
                List<String> roots = ghostRootAu.getChildrenAuList().getInDataObjectPackageIdList();
                if (roots != null) {
                    String[] tempArray = roots.toArray(new String[0]);
                    Arrays.sort(tempArray, idComparator);
                    for (String s : tempArray) {
                        auInDataObjectPackageIdMap.get(s).toSedaXml(xmlWriter, true, sedaLibProgressLogger);
                    }
                }
            }
            xmlWriter.writeEndElement();
            if (managementMetadataXmlData!=null)
                xmlWriter.writeRawXMLBlockIfNotEmpty(managementMetadataXmlData);
            else
                xmlWriter.writeRawXMLBlockIfNotEmpty("<ManagementMetadata/>");
            xmlWriter.writeEndElement();
            xmlWriter.flush();
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur d'écriture XML des métadonnées dans le DataObjectPackage", e);
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, "sedalib: " + getNextInOutCounter() +
                " métadonnées ArchiveUnit exportées dans le DataObjectPackage", null);
    }

    /**
     * Export the whole structure in XML SEDA Manifest.
     *
     * @param xmlWriter             the SEDAXMLStreamWriter generating the SEDA manifest
     * @param imbricateFlag         indicates if the manifest ArchiveUnits are to be
     *                              exported in imbricate mode (true) or in flat mode
     *                              (false)
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException     if the XML can't be written
     * @throws InterruptedException if export process is interrupted
     */
    public void toSedaXml(SEDAXMLStreamWriter xmlWriter, boolean imbricateFlag, SEDALibProgressLogger sedaLibProgressLogger)
            throws SEDALibException, InterruptedException {
        resetRefIdCounter();
        resetInOutCounter();
        exportDataObjectPackageObjects(xmlWriter, sedaLibProgressLogger);
        resetInOutCounter();
        exportDataObjectPackageMetadata(xmlWriter, imbricateFlag, sedaLibProgressLogger);
    }

    // SEDA XML importer

    /**
     * Import data object package, DataObjects part, of SEDA DataObjectPackage XML.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage     the DataObjectPackage to be completed
     * @param rootDir               the directory of the BinaryDataObject files
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException     if the XML can't be read or SEDA scheme is not
     *                              respected
     * @throws InterruptedException if export process is interrupted
     */
    public static void importDataObjectPackageObjects(SEDAXMLEventReader xmlReader, DataObjectPackage dataObjectPackage,
                                                      String rootDir, SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        String tmp;
        BinaryDataObject bdo;
        PhysicalDataObject pdo;
        boolean inDataObjectObjects = true;

        try {
            if (!xmlReader.nextBlockIfNamed("DataObjectPackage"))
                throw new SEDALibException("Pas d'élément DataObjectPackage");
            while (inDataObjectObjects) {
                tmp = xmlReader.peekName();
                switch (tmp) {
                    case "DataObjectGroup":
                        String dogId = DataObjectGroup.idFromSedaXml(xmlReader, dataObjectPackage, rootDir, sedaLibProgressLogger);
                        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS, "sedalib: DataObjectGroup [" + dogId + "] " +
                                "importé", null);
                        break;
                    case "BinaryDataObject":
                        bdo = BinaryDataObject.fromSedaXml(xmlReader, dataObjectPackage, sedaLibProgressLogger);
                        //noinspection ConstantConditions
                        bdo.setOnDiskPathFromString(rootDir + File.separator + bdo.uri.getValue());
                        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS, "sedalib: BinaryDataObject [" + bdo.inDataPackageObjectId + "] importé", null);
                        break;
                    case "PhysicalDataObject":
                        pdo = PhysicalDataObject.fromSedaXml(xmlReader, dataObjectPackage, sedaLibProgressLogger);
                        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS, "sedalib: PhysicalDataObject [" + pdo.inDataPackageObjectId +
                                "] importé", null);
                        break;
                    default:
                        inDataObjectObjects = false;
                }
            }
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture des métadonnées des DataObjects", e);
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP,
                "sedalib: " + dataObjectPackage.getNextInOutCounter() + " métadonnées DataObject importées depuis le DataObjectPackage", null);
    }

    /**
     * Import data object package, ArchiveUnits and global metadata part, of SEDA
     * DataObjectPackage XML.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param dataObjectPackage     the DataObjectPackage to be completed
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException     if the XML can't be read or SEDA scheme is not
     *                              respected
     * @throws InterruptedException if export process is interrupted
     */
    public static void importDataObjectPackageMetadata(SEDAXMLEventReader xmlReader,
                                                       DataObjectPackage dataObjectPackage, SEDALibProgressLogger sedaLibProgressLogger)
            throws SEDALibException, InterruptedException {
        String tmp;
        boolean inArchiveUnits = true;

        try {
            if (!xmlReader.nextBlockIfNamed("DescriptiveMetadata"))
                throw new SEDALibException("Pas d'élément DescriptiveMetadata");
            while (inArchiveUnits) {
                tmp = xmlReader.peekName();
                if (tmp == null)
                    break;
                if ("ArchiveUnit".equals(tmp)) {
                    String auId = ArchiveUnit.idFromSedaXml(xmlReader, dataObjectPackage, sedaLibProgressLogger);
                    doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS, "sedalib: ArchiveUnit [" + auId + "] importé", null);
                } else {
                    inArchiveUnits = false;
                }
            }
            xmlReader.endBlockNamed("DescriptiveMetadata");
            dataObjectPackage.managementMetadataXmlData = xmlReader.nextMandatoryBlockAsString("ManagementMetadata");
            xmlReader.endBlockNamed("DataObjectPackage");
        } catch (XMLStreamException | SEDALibException e) {
            throw new SEDALibException("Erreur de lecture des métadonnées des ArchiveUnits", e);
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP,
                "sedalib: " + dataObjectPackage.getNextInOutCounter() + " métadonnées ArchiveUnit importées depuis le DataObjectPackage", null);
    }

    /**
     * Import the whole structure in XML SEDA Manifest.
     *
     * @param xmlReader             the SEDAXMLEventReader reading the SEDA manifest
     * @param rootDir               the directory where the BinaryDataObject files are
     *                              exported
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @return the read DataObjectPackage
     * @throws SEDALibException     if the XML can't be read or SEDA scheme is not
     *                              respected
     * @throws InterruptedException if export process is interrupted
     */
    public static DataObjectPackage fromSedaXml(SEDAXMLEventReader xmlReader, String rootDir,
                                                SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException, InterruptedException {
        DataObjectPackage dataObjectPackage = new DataObjectPackage();
        dataObjectPackage.resetInOutCounter();
        importDataObjectPackageObjects(xmlReader, dataObjectPackage, rootDir, sedaLibProgressLogger);
        dataObjectPackage.resetInOutCounter();
        importDataObjectPackageMetadata(xmlReader, dataObjectPackage, sedaLibProgressLogger);

        // verify ArchiveUnit references && find roots
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        ArchiveUnit tmp;
        for (Map.Entry<String, ArchiveUnit> pair : dataObjectPackage.auInDataObjectPackageIdMap.entrySet()) {
            tmp = pair.getValue();
            if ((tmp.getChildrenAuList() != null) && (tmp.getChildrenAuList().getInDataObjectPackageIdList() != null))
                for (String inDataObjectPackageId : pair.getValue().getChildrenAuList().getInDataObjectPackageIdList()) {
                    if (dataObjectPackage.getArchiveUnitById(inDataObjectPackageId) == null)
                        throw new SEDALibException(
                                "Erreur de référence d'ArchiveUnit, [" + inDataObjectPackageId + "] n'existe pas");
                    dataObjectPackage.addTouchedInDataObjectPackageId(inDataObjectPackageId);
                }
        }
        for (Map.Entry<String, ArchiveUnit> pair : dataObjectPackage.auInDataObjectPackageIdMap.entrySet())
            if (!dataObjectPackage.isTouchedInDataObjectPackageId(pair.getValue().inDataPackageObjectId))
                dataObjectPackage
                        .addRootAu(dataObjectPackage.getArchiveUnitById(pair.getValue().inDataPackageObjectId));

        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP, "sedalib: manifest importé", null);

        return dataObjectPackage;
    }

    // Getters and setters

    /**
     * Gets the BinaryDataObject in inDataPackageObjectId map.
     *
     * @return the BinaryDataObject in inDataPackageObjectId map
     */
    public HashMap<String, BinaryDataObject> getBdoInDataObjectPackageIdMap() {
        return bdoInDataObjectPackageIdMap;
    }

    /**
     * Sets the BinaryDataObject in inDataPackageObjectId map.
     *
     * @param bdoInDataObjectPackageIdMap the BinaryDataObject in
     *                                    inDataPackageObjectId map
     */
    public void setBdoInDataObjectPackageIdMap(HashMap<String, BinaryDataObject> bdoInDataObjectPackageIdMap) {
        this.bdoInDataObjectPackageIdMap = bdoInDataObjectPackageIdMap;
    }

    /**
     * Gets the PhysicalDataObject in inDataPackageObjectId map.
     *
     * @return the PhysicalDataObject in inDataPackageObjectId map
     */
    public HashMap<String, PhysicalDataObject> getPdoInDataObjectPackageIdMap() {
        return pdoInDataObjectPackageIdMap;
    }

    /**
     * Sets the PhysicalDataObject in inDataPackageObjectId map.
     *
     * @param pdoInDataObjectPackageIdMap the PhysicalDataObject in
     *                                    inDataPackageObjectId map
     */
    public void setPdoInDataObjectPackageIdMap(HashMap<String, PhysicalDataObject> pdoInDataObjectPackageIdMap) {
        this.pdoInDataObjectPackageIdMap = pdoInDataObjectPackageIdMap;
    }

    /**
     * Gets the DataObjectGroup in inDataPackageObjectId map.
     *
     * @return the DataObjectGroup in inDataPackageObjectId map
     */
    public HashMap<String, DataObjectGroup> getDogInDataObjectPackageIdMap() {
        return dogInDataObjectPackageIdMap;
    }

    /**
     * Sets the DataObjectGroup in inDataPackageObjectId map.
     *
     * @param dogInDataObjectPackageIdMap the DataObjectGroup in
     *                                    inDataPackageObjectId map
     */
    public void setDogInDataObjectPackageIdMap(HashMap<String, DataObjectGroup> dogInDataObjectPackageIdMap) {
        this.dogInDataObjectPackageIdMap = dogInDataObjectPackageIdMap;
    }

    /**
     * Gets the ArchiveUnit in inDataPackageObjectId map.
     *
     * @return the ArchiveUnit in inDataPackageObjectId map
     */
    public HashMap<String, ArchiveUnit> getAuInDataObjectPackageIdMap() {
        return auInDataObjectPackageIdMap;
    }

    /**
     * Sets the ArchiveUnit in inDataPackageObjectId map.
     *
     * @param auInDataObjectPackageIdMap the ArchiveUnit in inDataPackageObjectId
     *                                   map
     */
    public void setAuInDataObjectPackageIdMap(HashMap<String, ArchiveUnit> auInDataObjectPackageIdMap) {
        this.auInDataObjectPackageIdMap = auInDataObjectPackageIdMap;
    }

    /**
     * Gets the "ghost" root ArchiveUnit, it's children being the root ArchiveUnits
     * of the DataObjectPackage.
     *
     * @return the root ArchiveUnits list
     */
    public ArchiveUnit getGhostRootAu() {
        return ghostRootAu;
    }

    /**
     * Adds the root au.
     *
     * @param au the au
     */
    public void addRootAu(ArchiveUnit au) {
        ghostRootAu.getChildrenAuList().add(au);
    }

    /**
     * Removes the root au.
     *
     * @param au the au
     */
    public void removeRootAu(ArchiveUnit au) {
        ghostRootAu.getChildrenAuList().getArchiveUnitList().remove(au);
    }

    /**
     * Sets the root au list.
     *
     * @param ghostRootAu the new ghost root au
     */
    public void setGhostRootAu(ArchiveUnit ghostRootAu) {
        this.ghostRootAu = ghostRootAu;
    }

    /**
     * Gets the next special counter increment.
     *
     * @return the next special counter
     */
    public int getNextInOutCounter() {
        return inOutCounter++;
    }

    /**
     * Reset special counter.
     */
    public void resetInOutCounter() {
        inOutCounter = 0;
    }

    /**
     * Gets the indicator of Vitam SIP rules respect.
     * <p>
     * It can be NORMALIZATION_STATUS_UNKNOWN, NORMALIZATION_STATUS_OK or
     * NORMALIZATION_STATUS_KO
     *
     * @return the vitam normalization status
     */
    public int getVitamNormalizationStatus() {
        return vitamNormalizationStatus;
    }

    /**
     * Sets the indicator of Vitam SIP rules respect.
     * <p>
     * It can be NORMALIZATION_STATUS_UNKNOWN, NORMALIZATION_STATUS_OK or
     * NORMALIZATION_STATUS_KO
     *
     * @param vitamNormalizationStatus the new vitam normalization status
     */
    public void setVitamNormalizationStatus(int vitamNormalizationStatus) {
        this.vitamNormalizationStatus = vitamNormalizationStatus;
    }

    /**
     * Gets the in out counter.
     *
     * @return the in out counter
     */
    public int getInOutCounter() {
        return inOutCounter;
    }

    /**
     * Gets the ManagementMetadata xml data.
     *
     * @return the ManagementMetadata xml data
     */
    public String getManagementMetadataXmlData() {
        return managementMetadataXmlData;
    }

    /**
     * Sets the ManagementMetadata xml data.
     *
     * @param managementMetadataXmlData the new ManagementMetadata xml data
     */
    public void setManagementMetadataXmlData(String managementMetadataXmlData) {
        this.managementMetadataXmlData = managementMetadataXmlData;
    }

    /**
     * Gets export metadata list.
     *
     * @return the export metadata list
     */
    public List<String> getExportMetadataList() {
        return exportMetadataList;
    }

    /**
     * Sets export metadata list.
     *
     * @param exportMetadataList the export metadata list
     */
    public void setExportMetadataList(List<String> exportMetadataList) {
        this.exportMetadataList = exportMetadataList;
    }
}