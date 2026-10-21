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
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.CompositeRules;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.EditedObjects;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.MetadataSamples;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
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
     * @param father   the father
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
        return (
            translateTag("DataObjectGroup") +
            " - " +
            (editedObject == null
                    ? translateTag("Unknown")
                    : (getDataObjectGroupMetadata().getInDataObjectPackageId() == null
                            ? "Tbd"
                            : getDataObjectGroupMetadata().getInDataObjectPackageId()))
        );
    }

    @Override
    public DataObjectGroup extractEditedObject() throws SEDALibException {
        List<Object> parts = new ArrayList<>();
        for (SEDAObjectEditor me : objectEditorList) parts.add(me.extractEditedObject());
        EditedObjects.rebuild(getDataObjectGroupMetadata(), parts);
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
        return toExtensionList(CompositeRules.addableDataObjectGroupParts(getPresentTags()), false);
    }

    private int getInsertionIndex(String metadataName) {
        return CompositeRules.dataObjectGroupInsertionIndex(getPresentTags(), metadataName);
    }

    @Override
    public void addChild(String editedObjectName) throws SEDALibException {
        Object metadata = null;
        SEDAObjectEditor addedObjectEditor = null;
        switch (editedObjectName) {
            case "BinaryDataObject":
                metadata = MetadataSamples.binaryDataObjectSample(
                    getDataObjectGroupMetadata().getDataObjectPackage(),
                    true
                );
                addedObjectEditor = new BinaryDataObjectEditor((BinaryDataObject) metadata, this);
                break;
            case "PhysicalDataObject":
                metadata = MetadataSamples.physicalDataObjectSample(
                    getDataObjectGroupMetadata().getDataObjectPackage(),
                    true
                );
                addedObjectEditor = new PhysicalDataObjectEditor((PhysicalDataObject) metadata, this);
                break;
            case "LogBook":
                metadata = MetadataSamples.sample(getSedaVersion(), "LogBook", "LogBook", true);
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
        return CompositeRules.isRepeatableInDataObjectGroup(editedObjectName);
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
