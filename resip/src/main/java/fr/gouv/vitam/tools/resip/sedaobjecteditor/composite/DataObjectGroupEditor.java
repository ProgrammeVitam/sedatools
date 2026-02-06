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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.composite;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.management.LogBook;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The ArchiveUnit object editor class.
 */
public class DataObjectGroupEditor extends CompositeEditor {

    /**
     * Instantiates a new DataObjectGroup editor.
     *
     * @param editedObject the DataObjectGroup editedObject
     * @param father       the father
     */
    public DataObjectGroupEditor(DataObjectGroup editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
    }

    private DataObjectGroup getDataObjectGroupMetadata() {
        return (DataObjectGroup) editedObject;
    }

    @Override
    public String getTag() {
        return "DataObjectGroup";
    }

    @Override
    public String getName() {
        String name =
            translateTag("DataObjectGroup") +
            " - " +
            (editedObject == null
                    ? translateTag("Unknown")
                    : (getDataObjectGroupMetadata().getInDataObjectPackageId() == null
                            ? "Tbd"
                            : getDataObjectGroupMetadata().getInDataObjectPackageId()));
        if (editedObject != null) {
            String algorithm = null;
            for (BinaryDataObject bdo : getDataObjectGroupMetadata().getBinaryDataObjectList()) {
                if (bdo.getMetadataMessageDigest() != null && bdo.getMetadataMessageDigest().getAlgorithm() != null) {
                    algorithm = bdo.getMetadataMessageDigest().getAlgorithm();
                    break;
                }
            }
            if (algorithm != null) name += " (" + algorithm + ")";
        }
        return name;
    }

    private void removeDataObject(DataObject dataObject) {
        getDataObjectGroupMetadata().removeDataObject(dataObject);
    }

    private void addDataObject(DataObject dataObject) throws SEDALibException {
        DataObjectGroup og;
        if (dataObject instanceof BinaryDataObject) {
            getDataObjectGroupMetadata()
                .getDataObjectPackage()
                .addDataObjectPackageIdElement((BinaryDataObject) dataObject);
            getDataObjectGroupMetadata().addDataObject((BinaryDataObject) dataObject);
        } else if (dataObject instanceof PhysicalDataObject) {
            getDataObjectGroupMetadata()
                .getDataObjectPackage()
                .addDataObjectPackageIdElement((PhysicalDataObject) dataObject);
            getDataObjectGroupMetadata().addDataObject((PhysicalDataObject) dataObject);
        }
    }

    @Override
    public DataObjectGroup extractEditedObject() throws SEDALibException {
        List<BinaryDataObject> bdoList = new ArrayList<BinaryDataObject>();
        List<PhysicalDataObject> pdoList = new ArrayList<PhysicalDataObject>();
        LogBook logBook = null;
        for (SEDAObjectEditor me : objectEditorList) {
            Object subObject = me.extractEditedObject();
            if (subObject instanceof BinaryDataObject) bdoList.add((BinaryDataObject) subObject);
            else if (subObject instanceof PhysicalDataObject) pdoList.add((PhysicalDataObject) subObject);
            else if (subObject instanceof LogBook) logBook = (LogBook) subObject;
        }

        getDataObjectGroupMetadata().setLogBook(logBook);
        List<BinaryDataObject> previousBdoList = new ArrayList<BinaryDataObject>(
            getDataObjectGroupMetadata().getBinaryDataObjectList()
        );
        for (BinaryDataObject bdo : previousBdoList) {
            if (!bdoList.contains(bdo)) removeDataObject(bdo);
        }
        for (BinaryDataObject bdo : bdoList) {
            if (!previousBdoList.contains(bdo)) addDataObject(bdo);
        }

        List<PhysicalDataObject> previousPdoList = new ArrayList<PhysicalDataObject>(
            getDataObjectGroupMetadata().getPhysicalDataObjectList()
        );
        for (PhysicalDataObject pdo : previousPdoList) {
            if (!pdoList.contains(pdo)) removeDataObject(pdo);
        }
        for (PhysicalDataObject pdo : pdoList) {
            if (!previousPdoList.contains(pdo)) addDataObject(pdo);
        }

        return getDataObjectGroupMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        return getTag();
    }

    private void renewObjectEditorList() throws SEDALibException {
        this.objectEditorList = new ArrayList<SEDAObjectEditor>();
        if (getDataObjectGroupMetadata() != null) {
            List<BinaryDataObject> bdoList = getDataObjectGroupMetadata().getBinaryDataObjectList();
            for (BinaryDataObject bdo : bdoList) {
                SEDAObjectEditor objectEditor = new BinaryDataObjectEditor(bdo, this);
                objectEditorList.add(objectEditor);
                ((CompositeEditor) objectEditor).doExpand(false, false);
            }
            List<PhysicalDataObject> pdoList = getDataObjectGroupMetadata().getPhysicalDataObjectList();
            for (PhysicalDataObject pdo : pdoList) {
                SEDAObjectEditor objectEditor = new PhysicalDataObjectEditor(pdo, this);
                objectEditorList.add(objectEditor);
                ((CompositeEditor) objectEditor).doExpand(false, false);
            }
            if (getDataObjectGroupMetadata().logBook != null) {
                SEDAObjectEditor objectEditor = SEDAObjectEditor.createSEDAObjectEditor(
                    getDataObjectGroupMetadata().logBook,
                    this
                );
                objectEditorList.add(objectEditor);
                ((CompositeEditor) objectEditor).doExpand(false, false);
            }
        }
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this, null, true);
        renewObjectEditorList();
    }

    @Override
    public List<Pair<String, String>> getExtensionList() {
        if (getDataObjectGroupMetadata() == null) return new ArrayList<Pair<String, String>>();

        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(
            Arrays.asList(
                Pair.of("BinaryDataObject", translateTag("BinaryDataObject")),
                Pair.of("PhysicalDataObject", translateTag("PhysicalDataObject"))
            )
        );

        if (
            (objectEditorList.size() == 0) ||
            (!objectEditorList.get(objectEditorList.size() - 1).getTag().equals("LogBook"))
        ) extensionList.add(Pair.of("LogBook", translateTag("LogBook")));
        return extensionList;
    }

    private int getOrder(String tag) {
        switch (tag) {
            case "BinaryDataObject":
                return 0;
            case "PhysicalDataObject":
                return 1;
            default:
                return 2;
        }
    }

    private int getInsertionIndex(String metadataName) {
        int index = 0;
        int addOrder = getOrder(metadataName);

        for (SEDAObjectEditor me : objectEditorList) {
            if (getOrder(me.getTag()) > addOrder) break;
            index++;
        }
        return index;
    }

    @Override
    public void addChild(String editedObjectName) throws SEDALibException {
        Object metadata = null;
        SEDAObjectEditor addedObjectEditor = null;
        switch (editedObjectName) {
            case "BinaryDataObject":
                metadata = BinaryDataObjectEditor.createBinaryDataObjectSample(true);
                addedObjectEditor = new BinaryDataObjectEditor((BinaryDataObject) metadata, this);
                break;
            case "PhysicalDataObject":
                metadata = PhysicalDataObjectEditor.createPhysicalDataObjectSample(true);
                addedObjectEditor = new PhysicalDataObjectEditor((PhysicalDataObject) metadata, this);
                break;
            case "LogBook":
                metadata = createSEDAMetadataSample("LogBook", "LogBook", true);
                addedObjectEditor = createSEDAObjectEditor((SEDAMetadata) metadata, this);
                break;
        }
        if (metadata == null) throw new SEDALibException(
            "L'objet [" + editedObjectName + "] n'existe pas dans un DataObjectGroup"
        );
        objectEditorList.add(getInsertionIndex(editedObjectName), addedObjectEditor);
        ((CompositeEditor) addedObjectEditor).doExpand(true, false);
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public boolean canContainsMultiple(String editedObjectName) throws SEDALibException {
        return !editedObjectName.equals("LogBook");
    }

    /**
     * Edit a new data object group.
     *
     * @param dataObjectGroup the data object group
     * @throws SEDALibException the seda lib exception
     */
    public void editDataObjectGroup(DataObjectGroup dataObjectGroup) throws SEDALibException {
        this.editedObject = dataObjectGroup;
        renewObjectEditorList();
        ((SEDAObjectEditorCompositePanel) getSEDAObjectEditorPanel()).refreshLoad(editedObject == null);
    }
}
