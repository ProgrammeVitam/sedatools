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
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
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
        if (metadataName.equals(RULE_TAG)) return getNewRuleIndex();
        int addOrderIndex = getComplexListTypeMetadata().indexOfMetadata(metadataName);
        int i = 0;
        if (addOrderIndex == -1) return Integer.MAX_VALUE;
        else {
            boolean manyFlag = getComplexListTypeMetadata().getMetadataMap().get(metadataName).isMany();
            for (SEDAObjectEditor soe : objectEditorList) {
                int curOrderIndex = getComplexListTypeMetadata().indexOfMetadata(soe.getTag());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
                    break;
                }
                if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex)) break;
                i++;
            }
        }
        return i;
    }

    private int getNewRuleIndex() throws SEDALibException {
        List<String> ruleMetadataKindList = ((RuleType) editedObject).getRuleMetadataKindList();
        for (int i = objectEditorList.size() - 1; i >= 0; i--) {
            String elementName = ((NamedTypeMetadata) objectEditorList.get(i).getEditedObject()).getXmlElementName();
            if (elementName.equals(RULE_TAG) || ruleMetadataKindList.contains(elementName)) return i + 1;
        }
        return 0;
    }

    public void addChildTo(String metadataName, Rule ruleParent) throws SEDALibException {
        SEDAMetadata sedaMetadata;
        if (!getComplexListTypeMetadata().isNotExpandable() && metadataName.equals("AnyXMLType")) sedaMetadata =
            createSEDAMetadataSample("AnyXMLType", "AnyXMLType", true);
        else {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata()
                .getMetadataMap()
                .get(metadataName);
            if (complexListMetadataKind == null) throw new SEDALibException(
                "La sous-métadonnée [" + metadataName + "] n'existe pas dans la métadonnée [" + getTag() + "]"
            );
            sedaMetadata = createSEDAMetadataSample(
                complexListMetadataKind.getMetadataClass().getSimpleName(),
                metadataName,
                true
            );
        }

        if (sedaMetadata != null) {
            int insertionSEDAObjectEditorIndex = getInsertionSEDAObjectEditorIndex(metadataName, ruleParent);
            SEDAObjectEditor addedSEDAObjectEditor = createSEDAObjectEditor(sedaMetadata, this);
            if (insertionSEDAObjectEditorIndex == Integer.MAX_VALUE) objectEditorList.add(addedSEDAObjectEditor);
            else objectEditorList.add(insertionSEDAObjectEditorIndex, addedSEDAObjectEditor);
            ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
        }
    }

    private int getInsertionSEDAObjectEditorIndex(String metadataName, Rule ruleParent) throws SEDALibException {
        int addOrderIndex = getComplexListTypeMetadata().indexOfMetadata(metadataName);
        int parentIndex = objectEditorList
            .stream()
            .map(SEDAObjectEditor::getEditedObject)
            .collect(Collectors.toList())
            .indexOf(ruleParent);
        if (parentIndex == -1) throw new IllegalStateException("Cannot find parent element");
        int i = 1; // first position for RuleId
        if (addOrderIndex == -1) return Integer.MAX_VALUE;
        else {
            boolean manyFlag = getComplexListTypeMetadata().getMetadataMap().get(metadataName).isMany();
            for (SEDAObjectEditor soe : objectEditorList.subList(parentIndex + 1, objectEditorList.size())) {
                if (soe.getTag().equals("Rule")) break;
                int curOrderIndex = getComplexListTypeMetadata().indexOfMetadata(soe.getTag());
                if ((!manyFlag) && (curOrderIndex == addOrderIndex)) {
                    break;
                }
                if ((curOrderIndex == -1) || (curOrderIndex > addOrderIndex)) break;
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
        List<String> metadataOrderedList = getComplexListTypeMetadata()
            .getMetadataMap()
            .entrySet()
            .stream()
            .filter(e -> !(e.getValue() instanceof RuleMetadataKind))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        for (String metadataName : metadataOrderedList) {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata()
                .getMetadataMap()
                .get(metadataName);
            if ((complexListMetadataKind.isMany()) || (!used.contains(metadataName))) result.add(
                Pair.of(metadataName, translateTag(metadataName))
            );
        }
        if (!getComplexListTypeMetadata().isNotExpandable()) result.add(
            Pair.of("AnyXMLType", translateTag("AnyXMLType"))
        );

        result.sort(comparingByValue());
        return result;
    }
}
