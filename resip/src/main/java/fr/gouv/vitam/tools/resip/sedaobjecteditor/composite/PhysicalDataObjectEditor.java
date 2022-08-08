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
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The PhysicalDataObject object editor class.
 */
public class PhysicalDataObjectEditor extends CompositeEditor {

    /**
     * Ordered SEDAObjectEditor collection
     */
    private SEDAObjectEditor[] objectEditorArray;
    private List<SEDAObjectEditor> otherObjectEditorList;

    /**
     * Ordered SEDAObjectEditor collection order and metadata element name and classes
     */
    static final int DATA_OBJECT_PROFILE = 0;
    static final String DATA_OBJECT_PROFILE_ELEMENT_NAME = "DataObjectProfile";
    static final int DATA_OBJECT_VERSION = 1;
    static final String DATA_OBJECT_VERSION_ELEMENT_NAME = "DataObjectVersion";
    static final int PHYSICAL_ID = 2;
    static final String PHYSICAL_ID_ELEMENT_NAME = "PhysicalId";
    static final int PHYSICAL_DIMENSIONS = 3;
    static final String PHYSICAL_DIMENSIONS_ELEMENT_NAME = "PhysicalDimensions";
    static final String ANY_XML_TYPE_ELEMENT_NAME = "AnyXMLType";

    static final String STRING_TYPE_CLASS = "StringType";
    static final String PHYSICAL_DIMENSIONS_CLASS = "PhysicalDimensions";
    static final String ANY_XML_TYPE_CLASS = "AnyXMLType";

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
        objectEditorArray = new SEDAObjectEditor[4];
        otherObjectEditorList = new ArrayList<>();
    }

    private PhysicalDataObject getPhysicalDataObjectMetadata() {
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
                (getPhysicalDataObjectMetadata().getInDataObjectPackageId() == null ? TO_BE_DEFINED :
                        getPhysicalDataObjectMetadata().getInDataObjectPackageId());
    }

    @Override
    public PhysicalDataObject extractEditedObject() throws SEDALibException {
        PhysicalDataObject tmpPdo = new PhysicalDataObject();
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            SEDAMetadata subMetadata = (SEDAMetadata) objectEditor.extractEditedObject();
            switch (subMetadata.getXmlElementName()) {
                case DATA_OBJECT_PROFILE_ELEMENT_NAME:
                    tmpPdo.dataObjectProfile = (StringType) subMetadata;
                    break;
                case DATA_OBJECT_VERSION_ELEMENT_NAME:
                    tmpPdo.dataObjectVersion = (StringType) subMetadata;
                    break;
                case PHYSICAL_ID_ELEMENT_NAME:
                    tmpPdo.physicalId = (StringType) subMetadata;
                    break;
                case PHYSICAL_DIMENSIONS_ELEMENT_NAME:
                    tmpPdo.physicalDimensions = (PhysicalDimensions) subMetadata;
                    break;
                case ANY_XML_TYPE_ELEMENT_NAME:
                    tmpPdo.otherDimensionsAbstractXml.add((AnyXMLType) subMetadata);
                    break;
                default:
                    // no other sub-editors metadata expected
                    break;
            }
        }
        getPhysicalDataObjectMetadata().dataObjectProfile = tmpPdo.dataObjectProfile;
        getPhysicalDataObjectMetadata().dataObjectVersion = tmpPdo.dataObjectVersion;
        getPhysicalDataObjectMetadata().physicalId = tmpPdo.physicalId;
        getPhysicalDataObjectMetadata().physicalDimensions = tmpPdo.physicalDimensions;
        getPhysicalDataObjectMetadata().otherDimensionsAbstractXml = tmpPdo.otherDimensionsAbstractXml;

        return getPhysicalDataObjectMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<>(objectEditorList.size());
        String tmp;

        if (getPhysicalDataObjectMetadata().dataObjectProfile != null)
            summaryList.add(getPhysicalDataObjectMetadata().dataObjectProfile.getValue());

        if (getPhysicalDataObjectMetadata().dataObjectVersion != null)
            tmp = getPhysicalDataObjectMetadata().dataObjectVersion.getValue();
        else
            tmp = translateTag(UNKNOWN);
        tmp = tmp.trim();
        if (!tmp.isEmpty())
            summaryList.add(tmp);

        if (getPhysicalDataObjectMetadata().physicalId != null) {
            tmp = getPhysicalDataObjectMetadata().physicalId.getValue();
            if (tmp == null)
                tmp = translateTag(UNKNOWN);
        } else
            tmp = translateTag(UNKNOWN);
        tmp = tmp.trim();
        if (!tmp.isEmpty())
            summaryList.add(tmp);

        return String.join(", ", summaryList);
    }

    private void updateObjectEditorList() throws SEDALibException {
        List<SEDAObjectEditor> result = new ArrayList<>();
        for (int i = 0; i < objectEditorArray.length; i++)
            if (objectEditorArray[i] != null)
                result.add(objectEditorArray[i]);
        result.addAll(otherObjectEditorList);
        objectEditorList = result;
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<>();

        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this);
        if (getPhysicalDataObjectMetadata().dataObjectProfile != null)
            objectEditorArray[DATA_OBJECT_PROFILE] = SEDAObjectEditor.createSEDAObjectEditor(getPhysicalDataObjectMetadata().dataObjectProfile, this);
        if (getPhysicalDataObjectMetadata().dataObjectVersion != null)
            objectEditorArray[DATA_OBJECT_VERSION] = SEDAObjectEditor.createSEDAObjectEditor(getPhysicalDataObjectMetadata().dataObjectVersion, this);
        if (getPhysicalDataObjectMetadata().physicalId != null)
            objectEditorArray[PHYSICAL_ID] = SEDAObjectEditor.createSEDAObjectEditor(getPhysicalDataObjectMetadata().physicalId, this);
        if (getPhysicalDataObjectMetadata().physicalDimensions != null)
            objectEditorArray[PHYSICAL_DIMENSIONS] = SEDAObjectEditor.createSEDAObjectEditor(getPhysicalDataObjectMetadata().physicalDimensions, this);

        for (AnyXMLType sub : getPhysicalDataObjectMetadata().otherDimensionsAbstractXml)
            otherObjectEditorList.add(SEDAObjectEditor.createSEDAObjectEditor(sub, this));

        updateObjectEditorList();
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        List<Pair<String, String>> extensionList;

        switch (SEDA2Version.getSeda2Version()) {
            case 1:
                extensionList = new ArrayList<>(Arrays.asList(
                        Pair.of(DATA_OBJECT_VERSION_ELEMENT_NAME, translateTag(DATA_OBJECT_VERSION_ELEMENT_NAME)),
                        Pair.of(PHYSICAL_ID_ELEMENT_NAME, translateTag(PHYSICAL_ID_ELEMENT_NAME)),
                        Pair.of(PHYSICAL_DIMENSIONS_ELEMENT_NAME, translateTag(PHYSICAL_DIMENSIONS_ELEMENT_NAME))));
                break;
            case 2:
                extensionList = new ArrayList<>(Arrays.asList(
                        Pair.of(DATA_OBJECT_PROFILE_ELEMENT_NAME, translateTag(DATA_OBJECT_PROFILE_ELEMENT_NAME)),
                        Pair.of(DATA_OBJECT_VERSION_ELEMENT_NAME, translateTag(DATA_OBJECT_VERSION_ELEMENT_NAME)),
                        Pair.of(PHYSICAL_ID_ELEMENT_NAME, translateTag(PHYSICAL_ID_ELEMENT_NAME)),
                        Pair.of(PHYSICAL_DIMENSIONS_ELEMENT_NAME, translateTag(PHYSICAL_DIMENSIONS_ELEMENT_NAME))));
                break;
            default:
                throw new SEDALibException("Version SEDA [" + SEDA2Version.getSeda2VersionString() + "] inconnue", null);
        }

        for (SEDAObjectEditor me : objectEditorList) {
            String name = me.getTag();
            extensionList.remove(Pair.of(name, translateTag(name)));
        }

        extensionList.add(Pair.of(ANY_XML_TYPE_ELEMENT_NAME, translateTag(ANY_XML_TYPE_ELEMENT_NAME)));
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata;
        switch (metadataName) {
            case DATA_OBJECT_PROFILE_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_PROFILE_ELEMENT_NAME, true);
                objectEditorArray[DATA_OBJECT_PROFILE] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case DATA_OBJECT_VERSION_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_VERSION_ELEMENT_NAME, true);
                objectEditorArray[DATA_OBJECT_VERSION] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case PHYSICAL_ID_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(STRING_TYPE_CLASS, PHYSICAL_ID_ELEMENT_NAME, true);
                objectEditorArray[PHYSICAL_ID] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case PHYSICAL_DIMENSIONS_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(PHYSICAL_DIMENSIONS_CLASS, PHYSICAL_DIMENSIONS_ELEMENT_NAME, true);
                objectEditorArray[PHYSICAL_DIMENSIONS] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case ANY_XML_TYPE_ELEMENT_NAME:
                sedaMetadata = createSEDAMetadataSample(ANY_XML_TYPE_CLASS, ANY_XML_TYPE_ELEMENT_NAME, true);
                otherObjectEditorList.add(createSEDAObjectEditor(sedaMetadata, this));
                break;
            default:
                throw new SEDALibException("La métadonnée [" + metadataName + "] n'existe pas dans un PhysicalDataObject");
        }
        updateObjectEditorList();
    }

    @Override
    public void removeChild(SEDAObjectEditor objectEditor) throws SEDALibException {
        for (int i = 0; i < objectEditorArray.length; i++)
            if (objectEditorArray[i] == objectEditor)
                objectEditorArray[i] = null;
        for (int i = 0; i < otherObjectEditorList.size(); i++)
            if (otherObjectEditorList.get(i) == objectEditor) {
                otherObjectEditorList.remove(i);
                break;
            }
        updateObjectEditorList();
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        return metadataName.equals(ANY_XML_TYPE_ELEMENT_NAME);
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
        if (SEDA2Version.getSeda2Version() > 1)
            result.dataObjectProfile = (StringType) createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_PROFILE_ELEMENT_NAME, minimal);
        result.dataObjectVersion = (StringType) createSEDAMetadataSample(STRING_TYPE_CLASS, DATA_OBJECT_VERSION_ELEMENT_NAME, minimal);
        result.physicalId = (StringType) createSEDAMetadataSample(STRING_TYPE_CLASS, PHYSICAL_ID_ELEMENT_NAME, minimal);
        if (!minimal) {
            result.physicalDimensions = (PhysicalDimensions) createSEDAMetadataSample(PHYSICAL_DIMENSIONS_CLASS, PHYSICAL_DIMENSIONS_ELEMENT_NAME, minimal);
            result.otherDimensionsAbstractXml.add((AnyXMLType) createSEDAMetadataSample(ANY_XML_TYPE_CLASS, ANY_XML_TYPE_ELEMENT_NAME, minimal));
        }
        return result;
    }
}
