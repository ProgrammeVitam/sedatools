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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.composite;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The PhysicalDataObject object editor class.
 */
public class PhysicalDataObjectEditor extends CompositeEditor {


    /**
     * Explicative texts
     */
    static final String UNKNOWN = "Unknown";
    static final String TO_BE_DEFINED = "Tbd";

    /**
     * Instantiates a new PhysicalDataObject editor.
     *
     * @param editedObject the PhysicalDataObject editedObject
     * @param father       the father
     */
    public PhysicalDataObjectEditor(PhysicalDataObject editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
    }

    private PhysicalDataObject getPhysicalDataObject() {
        return (PhysicalDataObject) editedObject;
    }

    @Override
    public String getTag() {
        return "PhysicalDataObject";
    }

    @Override
    public String getName() {
        if (editedObject == null)
            return translateTag(getTag()) + " - " + translateTag(UNKNOWN);
        return translateTag(getTag()) + " - " +
                (getPhysicalDataObject().getInDataObjectPackageId() == null ? TO_BE_DEFINED :
                        getPhysicalDataObject().getInDataObjectPackageId());
    }

    @Override
    public PhysicalDataObject extractEditedObject() throws SEDALibException {
        PhysicalDataObject tmpPdo = new PhysicalDataObject();
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            SEDAMetadata subMetadata = (SEDAMetadata) objectEditor.extractEditedObject();
            tmpPdo.addMetadata(subMetadata);
        }
        getPhysicalDataObject().setMetadataList(tmpPdo.getMetadataList());

        return getPhysicalDataObject();
    }

    private String getItOrUnknown(String str) {
        if ((str == null) || (str.isEmpty()))
            return translateTag(UNKNOWN);
        return str;
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<>(objectEditorList.size());
        String tmp;
        for (SEDAMetadata sm : getPhysicalDataObject().getMetadataList()) {
            if (sm instanceof StringType)
                summaryList.add(((StringType) sm).getValue());
            else if (sm instanceof PersistentIdentifier)
                summaryList.add(((PersistentIdentifier) sm).getSummary());
        }
        return String.join(", ", summaryList);
    }

    private void updateObjectEditorList() throws SEDALibException {
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<>();

        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this, null, false);
        for (SEDAMetadata sm : getPhysicalDataObject().getMetadataList()) {
            objectEditorList.add(SEDAObjectEditor.createSEDAObjectEditor(sm, this));
        }
        updateObjectEditorList();
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        List<String> used = new ArrayList<>();
        List<Pair<String, String>> result = new ArrayList<>();
        for (SEDAObjectEditor soe : objectEditorList) {
            used.add(soe.getTag());
        }
        for (String metadataName : getPhysicalDataObject().getMetadataMap().keySet()) {
            if (metadataName.endsWith("SystemId"))
                continue;
            ComplexListMetadataKind complexListMetadataKind = getPhysicalDataObject().getMetadataMap().get(metadataName);
            if ((complexListMetadataKind.isMany()) || (!used.contains(metadataName)))
                result.add(Pair.of(metadataName, translateTag(metadataName)));
        }
        if (!getPhysicalDataObject().isNotExpandable())
            result.add(Pair.of("AnyXMLType", translateTag("AnyXMLType")));

        result.sort((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
        return result;
    }


    private void replaceOrAddObjectEditor(SEDAObjectEditor newObjectEditor) throws SEDALibException {
        // replace if it exists
        for (int i = 0; i < objectEditorList.size(); i++) {
            if (objectEditorList.get(i).getTag().equals(newObjectEditor.getTag())) {
                objectEditorList.set(i, newObjectEditor);
                return;
            }
        }

        // add in BinaryDataObject metadata order
        objectEditorList.add(getInsertionSEDAObjectEditorIndex(newObjectEditor.getTag()), newObjectEditor);
    }

    protected int getInsertionSEDAObjectEditorIndex(String metadataName) throws SEDALibException {
        int addOrderIndex;
        int curOrderIndex;
        int i;
        boolean manyFlag;
        addOrderIndex = getPhysicalDataObject().indexOfMetadata(metadataName);
        i = 0;
        if (addOrderIndex == -1)
            return Integer.MAX_VALUE;
        else {
            manyFlag = getPhysicalDataObject().getMetadataMap().get(metadataName).isMany();
            for (SEDAObjectEditor soe : objectEditorList) {
                curOrderIndex = getPhysicalDataObject().indexOfMetadata(soe.getTag());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex) ||
                        (curOrderIndex == -1) || (curOrderIndex > addOrderIndex)) {
                    break;
                }
                i++;
            }
        }
        return i;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata;
        SEDAMetadata sm = createSEDAMetadataSample(getPhysicalDataObject().getMetadataMap().get(metadataName).getMetadataClass().getSimpleName(), metadataName, true);
        replaceOrAddObjectEditor(createSEDAObjectEditor(sm, this));
        updateObjectEditorList();
    }

    @Override
    public void removeChild(SEDAObjectEditor objectEditor) throws SEDALibException {
        objectEditorList.remove(objectEditor);
        updateObjectEditorList();
    }

    @Override
    public boolean canContainsMultiple(String metadataName) {
        try {
            ComplexListMetadataKind cmk = getPhysicalDataObject().getMetadataMap().get(metadataName);
            if (cmk != null)
                return cmk.isMany();
            return !getPhysicalDataObject().isNotExpandable();
        } catch (SEDALibException ignored) {
            // Exception impossible
            return false;
        }
    }

    /**
     * Create physical data object sample binary data object.
     *
     * @param minimal the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the physical data object
     * @throws SEDALibException the seda lib exception
     */
    public static PhysicalDataObject createPhysicalDataObjectSample(boolean minimal) throws SEDALibException {
        PhysicalDataObject result = new PhysicalDataObject();

        for (Map.Entry<String, ComplexListMetadataKind> e : result.getMetadataMap().entrySet()) {
            if (SEDAObjectEditorConstants.minimalTagList.contains(e.getKey()))
                result.addMetadata(createSEDAMetadataSample(e.getValue().getMetadataClass().getName(), e.getKey(), minimal));
        }

        return result;
    }
}
