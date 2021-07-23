package fr.gouv.vitam.tools.resip.sedaobjecteditor.composite;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.NamedTypeMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;
import static fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType.RULE_TAG;
import static java.util.Map.Entry.comparingByValue;

public class RuleTypeEditor extends ComplexListTypeEditor {
    public RuleTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
    }

    @Override
    protected int getInsertionSEDAObjectEditorIndex(String metadataName) throws SEDALibException {
        if(metadataName.equals(RULE_TAG))
            return getNewRuleIndex();
        int addOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(metadataName);
        int i = 0;
        if (addOrderIndex == -1)
            return Integer.MAX_VALUE;
        else {
            boolean manyFlag = getComplexListTypeMetadata().getMetadataMap().get(metadataName).many;
            for (SEDAObjectEditor soe : objectEditorList) {
                int curOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(soe.getTag());
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

    private int getNewRuleIndex() throws SEDALibException {
        List<String> ruleMetadataKindList = ((RuleType) editedObject).getRuleMetadataKindList();
            for(int i = objectEditorList.size()-1; i >= 0; i--) {
                String elementName = ((NamedTypeMetadata) objectEditorList.get(i).getEditedObject()).getXmlElementName();
                if(elementName.equals(RULE_TAG) || ruleMetadataKindList.contains(elementName))
                    return i+1;
            }
        return 0;
    }

    public void addChildTo(String metadataName, Rule ruleParent) throws SEDALibException {
        SEDAMetadata sedaMetadata;
        if (!getComplexListTypeMetadata().isNotExpendable() && metadataName.equals("AnyXMLType"))
            sedaMetadata = createSEDAMetadataSample("AnyXMLType", "AnyXMLType", true);
        else {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata().getMetadataMap().get(metadataName);
            if (complexListMetadataKind == null)
                throw new SEDALibException("La sous-métadonnée [" + metadataName + "] n'existe pas dans la métadonnée [" + getTag() + "]");
            sedaMetadata = createSEDAMetadataSample(complexListMetadataKind.metadataClass.getSimpleName(), metadataName, true);
        }

        if (sedaMetadata != null) {
            int insertionSEDAObjectEditorIndex = getInsertionSEDAObjectEditorIndex(metadataName, ruleParent);
            SEDAObjectEditor addedSEDAObjectEditor = createSEDAObjectEditor(sedaMetadata, this);
            if (insertionSEDAObjectEditorIndex == Integer.MAX_VALUE)
                objectEditorList.add(addedSEDAObjectEditor);
            else
                objectEditorList.add(insertionSEDAObjectEditorIndex, addedSEDAObjectEditor);
            ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
        }
    }

    private int getInsertionSEDAObjectEditorIndex(String metadataName, Rule ruleParent) throws SEDALibException {
        int addOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(metadataName);
        int parentIndex = objectEditorList.stream().map(SEDAObjectEditor::getEditedObject).collect(Collectors.toList()).indexOf(ruleParent);
        if(parentIndex == -1)
            throw new IllegalStateException("Cannot find parent element");
        int i = 1; // first position for RuleId
        if (addOrderIndex == -1)
            return Integer.MAX_VALUE;
        else {
            boolean manyFlag = getComplexListTypeMetadata().getMetadataMap().get(metadataName).many;
            for (SEDAObjectEditor soe : objectEditorList.subList(parentIndex + 1, objectEditorList.size())) {
                if(soe.getTag().equals("Rule")) break;
                int curOrderIndex = getComplexListTypeMetadata().getMetadataOrderedList().indexOf(soe.getTag());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
                    break;
                }
                if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex))
                    break;
                i++;
            }
        }
        return i + parentIndex;
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        List<String> used = new ArrayList<>();
        List<Pair<String, String>> result = new ArrayList<>();
        for (SEDAObjectEditor soe : objectEditorList) {
            used.add(soe.getTag());
        }
        List<String> metadataOrderedList = getComplexListTypeMetadata().getMetadataMap().entrySet().stream().filter(e -> !(e.getValue() instanceof RuleMetadataKind)).map(
            Map.Entry::getKey).collect(
            Collectors.toList());
        for (String metadataName : metadataOrderedList) {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata().getMetadataMap().get(metadataName);
            if ((complexListMetadataKind.many) || (!used.contains(metadataName)))
                result.add(Pair.of(metadataName, translateTag(metadataName)));
        }
        if (!getComplexListTypeMetadata().isNotExpendable())
            result.add(Pair.of("AnyXMLType", translateTag("AnyXMLType")));

        result.sort(comparingByValue());
        return result;
    }
}
