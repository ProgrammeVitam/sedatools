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
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.data.PhysicalDimensions;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.AnyXMLType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
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
     * Instantiates a new PhysicalDataObject editor.
     *
     * @param editedObject the PhysicalDataObject editedObject
     * @param father   the father
     */
    public PhysicalDataObjectEditor(PhysicalDataObject editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
        objectEditorArray = new SEDAObjectEditor[3];
        otherObjectEditorList = new ArrayList<SEDAObjectEditor>();
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
        return translateTag("PhysicalDataObject") + " - " +
                (editedObject == null ? translateTag("Unknown") :
                        (getPhysicalDataObjectMetadata().getInDataObjectPackageId() == null ? "Tbd" :
                                getPhysicalDataObjectMetadata().getInDataObjectPackageId()));
    }

    @Override
    public PhysicalDataObject extractEditedObject() throws SEDALibException {
        PhysicalDataObject tmpPdo = new PhysicalDataObject();
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            SEDAMetadata subMetadata = (SEDAMetadata) objectEditor.extractEditedObject();
            switch (subMetadata.getXmlElementName()) {
                case "DataObjectVersion":
                    tmpPdo.dataObjectVersion = (StringType) subMetadata;
                    break;
                case "PhysicalId":
                    tmpPdo.physicalId = (StringType) subMetadata;
                    break;
                case "PhysicalDimensions":
                    tmpPdo.physicalDimensions = (PhysicalDimensions) subMetadata;
                    break;
                case "AnyXMLType":
                    tmpPdo.otherDimensionsAbstractXml.add((AnyXMLType) subMetadata);
                    break;
            }
        }
        getPhysicalDataObjectMetadata().dataObjectVersion = tmpPdo.dataObjectVersion;
        getPhysicalDataObjectMetadata().physicalId = tmpPdo.physicalId;
        getPhysicalDataObjectMetadata().physicalDimensions = tmpPdo.physicalDimensions;
        getPhysicalDataObjectMetadata().otherDimensionsAbstractXml = tmpPdo.otherDimensionsAbstractXml;

        return getPhysicalDataObjectMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<String>(objectEditorList.size());
        String tmp;

        if (getPhysicalDataObjectMetadata().dataObjectVersion != null)
            summaryList.add(getPhysicalDataObjectMetadata().dataObjectVersion.getValue());
        else
            summaryList.add(translateTag("Unknown"));

        if (getPhysicalDataObjectMetadata().physicalId != null) {
            tmp = getPhysicalDataObjectMetadata().physicalId.getValue();
            if (tmp == null)
                tmp = translateTag("Unknown");
        } else
            tmp = translateTag("Unknown");
        summaryList.add(tmp);

        return String.join(", ", summaryList);
    }

    private void updateObjectEditorList() throws SEDALibException {
        List<SEDAObjectEditor> result = new ArrayList<SEDAObjectEditor>();
        for (int i = 0; i < 3; i++)
            if (objectEditorArray[i] != null)
                result.add(objectEditorArray[i]);
        result.addAll(otherObjectEditorList);
        objectEditorList = result;
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<SEDAObjectEditor>();

        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this);
        if (getPhysicalDataObjectMetadata().dataObjectVersion != null)
            objectEditorArray[0] = SEDAObjectEditor.createSEDAObjectEditor(getPhysicalDataObjectMetadata().dataObjectVersion, this);
        if (getPhysicalDataObjectMetadata().physicalId != null)
            objectEditorArray[1] = SEDAObjectEditor.createSEDAObjectEditor(getPhysicalDataObjectMetadata().physicalId, this);
        if (getPhysicalDataObjectMetadata().physicalDimensions != null)
            objectEditorArray[2] = SEDAObjectEditor.createSEDAObjectEditor(getPhysicalDataObjectMetadata().physicalDimensions, this);

        for (AnyXMLType sub : getPhysicalDataObjectMetadata().otherDimensionsAbstractXml)
            otherObjectEditorList.add(SEDAObjectEditor.createSEDAObjectEditor(sub, this));

        updateObjectEditorList();
    }

    static private void openButton(Path path) {
        System.out.println(path);
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(Arrays.asList(
                Pair.of("DataObjectVersion", translateTag("DataObjectVersion")),
                Pair.of("PhysicalId", translateTag("PhysicalId")),
                Pair.of("PhysicalDimensions", translateTag("PhysicalDimensions"))));

        for (SEDAObjectEditor me : objectEditorList) {
            String name = me.getTag();
            extensionList.remove(Pair.of(name, translateTag(name)));
        }

        extensionList.add(Pair.of("AnyXMLType", translateTag("AnyXMLType")));
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata = null;
        switch (metadataName) {
            case "DataObjectVersion":
                sedaMetadata = createSEDAMetadataSample("StringType", "DataObjectVersion", true);
                objectEditorArray[0] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "PhysicalId":
                sedaMetadata = createSEDAMetadataSample("StringType", "PhysicalId", true);
                objectEditorArray[1] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "PhysicalDimensions":
                sedaMetadata = createSEDAMetadataSample("PhysicalDimensions", "PhysicalDimensions", true);
                objectEditorArray[2] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "AnyXMLType":
                sedaMetadata = createSEDAMetadataSample("AnyXMLType", "AnyXMLType", true);
                otherObjectEditorList.add(createSEDAObjectEditor(sedaMetadata, this));
                break;
        }
        if (sedaMetadata == null)
            throw new SEDALibException("La métadonnée [" + metadataName + "] n'existe pas dans un PhysicalDataObject");

        updateObjectEditorList();
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        return metadataName.equals("AnyXMLType");
    }

    /**
     * Create physical data object sample binary data object.
     *
     * @param minimal the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the physical data object
     * @throws SEDALibException the seda lib exception
     */
    static public PhysicalDataObject createPhysicalDataObjectSample(boolean minimal) throws SEDALibException {
        PhysicalDataObject result = new PhysicalDataObject();
        result.dataObjectVersion = (StringType) createSEDAMetadataSample("StringType", "DataObjectVersion", minimal);
        result.physicalId = (StringType) createSEDAMetadataSample("StringType", "PhysicalId", minimal);
        if (!minimal) {
            result.physicalDimensions = (PhysicalDimensions) createSEDAMetadataSample("PhysicalDimensions", "PhysicalDimensions", minimal);
            result.otherDimensionsAbstractXml.add((AnyXMLType) createSEDAMetadataSample("AnyXMLType", "AnyXMLType", minimal));
        }
        return result;
    }
}
