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
package fr.gouv.vitam.tools.resip.metadataeditor.composite;

import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants;
import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.CompositeEditorPanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The StringType metadata editor class.
 */
public class ComplexListTypeEditor extends CompositeEditor {

    public ComplexListTypeEditor(SEDAMetadata metadata, MetadataEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof ComplexListType))
            throw new SEDALibException("La métadonnée à éditer n'est pas du bon type");
    }

    private ComplexListType getComplexListTypeMetadata() {
        return (ComplexListType) metadata;
    }


    public SEDAMetadata extractEditedObject() throws SEDALibException {
        setEditedObject(getEmptySameMetadata(getSEDAMetadata()));
        for (MetadataEditor metadataEditor : metadataEditorList) {
            SEDAMetadata subMetadata=(SEDAMetadata)metadataEditor.extractEditedObject();
            ((ComplexListType)metadata).addMetadata(subMetadata);
        }
        return getSEDAMetadata();
    }

    static public SEDAMetadata getSample(Class complexListType, String elementName, boolean minimal) throws SEDALibException {
        ComplexListType result;
        try {
            result = (ComplexListType) (complexListType.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e1) {
            try {
                result = (ComplexListType) (complexListType.getConstructor(String.class).newInstance(elementName));
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e2) {
                throw new SEDALibException("La création d'une métadonnée de type [" + complexListType + "] n'est pas possible", e2);
            } catch (InvocationTargetException te) {
                throw new SEDALibException("La création d'une métadonnée de type [" + complexListType + "] a généré une erreur", te.getTargetException());
            }
        } catch (InvocationTargetException te) {
            throw new SEDALibException("La création d'une métadonnée de type [" + complexListType + "] a généré une erreur", te.getTargetException());
        }
        for (String metadataName : result.getMetadataOrderedList()) {
            if (!minimal || (MetadataEditorConstants.minimalTagList.contains(metadataName))) {
                ComplexListMetadataKind complexListMetadataKind = result.getMetadataMap().get(metadataName);
                SEDAMetadata metadataObject = MetadataEditor.createMetadataSample(complexListMetadataKind.metadataClass.getSimpleName(), metadataName, minimal);
                try {
                    result.addMetadata(metadataObject);
                } catch (SEDALibException ignored) {
                }
                if ((complexListMetadataKind.many) && !minimal) {
                    metadataObject = MetadataEditor.createMetadataSample(complexListMetadataKind.metadataClass.getSimpleName(), metadataName, minimal);
                    try {
                        result.addMetadata(metadataObject);
                    } catch (SEDALibException ignored) {
                    }
                }
            }
        }
        if (!result.isNotExpendable() && !minimal)
            try {
                result.addNewMetadata("RawXML", "<AnyOtherMetadata><SubTag1>Text1</SubTag1><SubTag2>Text2</SubTag2></AnyOtherMetadata>");
            } catch (SEDALibException ignored) {
            }
        return result;
    }

    public void createMetadataEditorPanel() throws SEDALibException {
        this.metadataEditorList = new ArrayList<MetadataEditor>();
        this.metadataEditorPanel = new CompositeEditorPanel(this);
        for (SEDAMetadata detail : getComplexListTypeMetadata().metadataList) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(detail, this);
            metadataEditorList.add(metadataEditor);
            ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(metadataEditor.getMetadataEditorPanel());
        }
    }

    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<String>(metadataEditorList.size());
        for (MetadataEditor metadataEditor : metadataEditorList) {
            String summary = metadataEditor.getSummary();
            if (!summary.isEmpty() && !summary.equals("{}")) {
                if (metadataEditor instanceof ComplexListTypeEditor)
                    summary = "{" + summary + "}";
                summaryList.add(summary);
            }
        }
        return String.join(", ", summaryList);
    }

    public List<Pair<String,String>> getExtensionList() throws SEDALibException {
        List<String> used = new ArrayList<String>();
        List<Pair<String,String>> result = new ArrayList<Pair<String,String>>();
        for (MetadataEditor detail : metadataEditorList) {
            used.add(detail.getName());
        }
        for (String metadataName : getComplexListTypeMetadata().getMetadataOrderedList()) {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata().getMetadataMap().get(metadataName);
            if ((complexListMetadataKind.many) || (!used.contains(metadataName)))
                result.add(Pair.of(metadataName,translate(metadataName)));
        }
        if (!getComplexListTypeMetadata().isNotExpendable())
            result.add(Pair.of("AnyXMLType",translate("AnyXMLType")));

        result.sort((p1, p2) -> p1.getValue().compareTo(p2.getValue()));
        return result;
    }

    public void removeChild(MetadataEditor metadataEditor) throws SEDALibException {
        metadataEditorList.remove(metadataEditor);
        ((CompositeEditorPanel)getMetadataEditorPanel()).removeMetadataEditorPanel(metadataEditor.getMetadataEditorPanel());
    }

    private int getInsertionMetadataEditorIndex(String metadataName) throws SEDALibException {
        int addOrderIndex, curOrderIndex, i;
        boolean manyFlag, setFlag;
        addOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(metadataName);
        i = 0;
        if (addOrderIndex == -1)
            i=metadataEditorList.size();
        else {
            manyFlag = getComplexListTypeMetadata().getMetadataMap().get(metadataName).many;
            for (MetadataEditor me : metadataEditorList) {
                curOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(me.getName());
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

    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata;
        if (!getComplexListTypeMetadata().isNotExpendable() && metadataName.equals("AnyXMLType"))
            sedaMetadata = createMetadataSample("AnyXMLType", "AnyXMLType", true);
        else {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata().getMetadataMap().get(metadataName);
            if (complexListMetadataKind == null)
                throw new SEDALibException("La sous-métadonnée [" + metadataName + "] n'existe pas dans la métadonnée [" + getName() + "]");
            sedaMetadata = createMetadataSample(complexListMetadataKind.metadataClass.getSimpleName(), metadataName, true);
        }

        int insertionMetadataEditorIndex=getInsertionMetadataEditorIndex(metadataName);
        MetadataEditor addedMetadataEditor = createMetadataEditor(sedaMetadata, this);
        metadataEditorList.add(insertionMetadataEditorIndex,
                addedMetadataEditor);
        ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(insertionMetadataEditorIndex,addedMetadataEditor.getMetadataEditorPanel());
    }

    public boolean containsMultiple(String metadataName) throws SEDALibException {
        if (getComplexListTypeMetadata().getMetadataMap().get(metadataName) == null)
            return true;
        return getComplexListTypeMetadata().getMetadataMap().get(metadataName).many;
    }
}
