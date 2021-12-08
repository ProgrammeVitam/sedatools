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
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The ComplexListType object editor class.
 */
public class ComplexListTypeEditor extends CompositeEditor {

    /**
     * Instantiates a new ComplexListType editor.
     *
     * @param metadata the ComplexListType editedObject
     * @param father   the father
     * @throws SEDALibException if not a ComplexListType editedObject
     */
    public ComplexListTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof ComplexListType))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    /**
     * Gets ComplexListType form of edited object.
     *
     * @return the ComplexListType
     */
    public ComplexListType getComplexListTypeMetadata() {
        return (ComplexListType) editedObject;
    }

    /**
     * Gets ComplexListType sample.
     *
     * @param complexListSubType the complex list sub type
     * @param elementName        the element name, corresponding to the XML tag in SEDA
     * @param minimal            the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the seda editedObject sample
     * @throws SEDALibException the seda lib exception
     */
    static public SEDAMetadata getSEDAMetadataSample(Class<?> complexListSubType, String elementName, boolean minimal) throws SEDALibException {
        ComplexListType result;
        try {
            result = (ComplexListType) complexListSubType.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e1) {
            try {
                result = (ComplexListType) complexListSubType.getConstructor(String.class).newInstance(elementName);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e2) {
                throw new SEDALibException("La création d'une métadonnée de type [" + complexListSubType + "] n'est pas possible", e2);
            } catch (InvocationTargetException te) {
                throw new SEDALibException("La création d'une métadonnée de type [" + complexListSubType + "] a généré une erreur", te.getTargetException());
            }
        } catch (InvocationTargetException te) {
            throw new SEDALibException("La création d'une métadonnée de type [" + complexListSubType + "] a généré une erreur", te.getTargetException());
        }
        result.elementName = elementName;
        for (String metadataName : result.getMetadataOrderedList()) {
            if (!minimal || (SEDAObjectEditorConstants.minimalTagList.contains(metadataName))) {
                ComplexListMetadataKind complexListMetadataKind = result.getMetadataMap().get(metadataName);
                SEDAMetadata metadataObject = SEDAObjectEditor.createSEDAMetadataSample(complexListMetadataKind.getMetadataClass().getSimpleName(), metadataName, minimal);
                try {
                    result.addMetadata(metadataObject);
                } catch (SEDALibException ignored) {
                }
                if ((complexListMetadataKind.isMany()) && !minimal) {
                    metadataObject = SEDAObjectEditor.createSEDAMetadataSample(complexListMetadataKind.getMetadataClass().getSimpleName(), metadataName, minimal);
                    try {
                        result.addMetadata(metadataObject);
                    } catch (SEDALibException ignored) {
                    }
                }
            }
        }
        if (!result.isNotExpendable() && !minimal)
            try {
                result.addNewMetadata("AnyXMLType", "<AnyOtherMetadata><SubTag1>Text1</SubTag1><SubTag2>Text2</SubTag2></AnyOtherMetadata>");
            } catch (SEDALibException ignored) {
            }
        return result;
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        setEditedObject(getEmptySameSEDAMetadata(getComplexListTypeMetadata()));
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            SEDAMetadata subMetadata = (SEDAMetadata) objectEditor.extractEditedObject();
            ((ComplexListType) editedObject).addMetadata(subMetadata);
        }
        return getComplexListTypeMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<String>(objectEditorList.size());
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            String summary = objectEditor.getSummary().trim();
            if (!summary.isEmpty() && !summary.equals("{}")) {
                if (objectEditor instanceof ComplexListTypeEditor)
                    summary = "{" + summary + "}";
                summaryList.add(summary);
            }
        }
        return String.join(", ", summaryList);
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<SEDAObjectEditor>();
        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this);
        int position = 0;
        for (SEDAMetadata detail : getComplexListTypeMetadata().metadataList) {
            SEDAObjectEditor objectEditor = SEDAObjectEditor.createSEDAObjectEditor(detail, this);
            objectEditorList.add(objectEditor);
        }
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        List<String> used = new ArrayList<String>();
        List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        for (SEDAObjectEditor soe : objectEditorList) {
            used.add(soe.getTag());
        }
        for (String metadataName : getComplexListTypeMetadata().getMetadataOrderedList()) {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata().getMetadataMap().get(metadataName);
            if ((complexListMetadataKind.isMany()) || (!used.contains(metadataName)))
                result.add(Pair.of(metadataName, translateTag(metadataName)));
        }
        if (!getComplexListTypeMetadata().isNotExpendable())
            result.add(Pair.of("AnyXMLType", translateTag("AnyXMLType")));

        result.sort((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
        return result;
    }

    protected int getInsertionSEDAObjectEditorIndex(String metadataName) throws SEDALibException {
        int addOrderIndex, curOrderIndex, i;
        boolean manyFlag;
        addOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(metadataName);
        i = 0;
        if (addOrderIndex == -1)
            return Integer.MAX_VALUE;
        else {
            manyFlag = getComplexListTypeMetadata().getMetadataMap().get(metadataName).isMany();
            for (SEDAObjectEditor soe : objectEditorList) {
                curOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(soe.getTag());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
                    break;
                }
                if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex))
                    break;
                i++;
            }
        }
        return i;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata = null;
        if (!getComplexListTypeMetadata().isNotExpendable() && metadataName.equals("AnyXMLType"))
            sedaMetadata = createSEDAMetadataSample("AnyXMLType", "AnyXMLType", true);
        else {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata().getMetadataMap().get(metadataName);
            if (complexListMetadataKind == null)
                throw new SEDALibException("La sous-métadonnée [" + metadataName + "] n'existe pas dans la métadonnée [" + getTag() + "]");
            sedaMetadata = createSEDAMetadataSample(complexListMetadataKind.getMetadataClass().getSimpleName(), metadataName, true);
        }

        if (sedaMetadata != null) {
            int insertionSEDAObjectEditorIndex = getInsertionSEDAObjectEditorIndex(metadataName);
            SEDAObjectEditor addedSEDAObjectEditor = createSEDAObjectEditor(sedaMetadata, this);
            if (insertionSEDAObjectEditorIndex == Integer.MAX_VALUE)
                objectEditorList.add(addedSEDAObjectEditor);
            else
                objectEditorList.add(insertionSEDAObjectEditorIndex, addedSEDAObjectEditor);
            ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
        }
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        if (getComplexListTypeMetadata().getMetadataMap().get(metadataName) == null)
            return true;
        return getComplexListTypeMetadata().getMetadataMap().get(metadataName).isMany();
    }
}
