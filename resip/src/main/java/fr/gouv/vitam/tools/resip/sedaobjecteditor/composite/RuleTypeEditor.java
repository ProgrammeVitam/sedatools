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
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.MetadataSamples;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Rule;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.*;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class RuleTypeEditor extends ComplexListTypeEditor {

    public RuleTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
    }

    public void addChildTo(String metadataName, Rule ruleParent) throws SEDALibException {
        SEDAMetadata sedaMetadata;
        if (!getComplexListTypeMetadata().isNotExpandable() && metadataName.equals("AnyXMLType")) sedaMetadata =
            MetadataSamples.sample(getSedaVersion(), "AnyXMLType", "AnyXMLType", true);
        else {
            ComplexListMetadataKind complexListMetadataKind = getComplexListTypeMetadata()
                .getMetadataMap()
                .get(metadataName);
            if (complexListMetadataKind == null) throw new SEDALibException(
                "La sous-métadonnée [" + metadataName + "] n'existe pas dans la métadonnée [" + getTag() + "]"
            );
            sedaMetadata = MetadataSamples.sample(
                getSedaVersion(),
                complexListMetadataKind.getMetadataClass().getSimpleName(),
                metadataName,
                true
            );
        }

        if (sedaMetadata != null) {
            int insertionSEDAObjectEditorIndex = getInsertionSEDAObjectEditorIndex(metadataName, ruleParent);
            objectEditorList.add(insertionSEDAObjectEditorIndex, createSEDAObjectEditor(sedaMetadata, this));
            ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
        }
    }

    private int getInsertionSEDAObjectEditorIndex(String metadataName, Rule ruleParent) throws SEDALibException {
        int ruleIndex = objectEditorList
            .stream()
            .map(SEDAObjectEditor::getEditedObject)
            .collect(Collectors.toList())
            .indexOf(ruleParent);
        if (ruleIndex == -1) throw new IllegalStateException("Cannot find parent element");
        return CompositeRules.ruleBlockInsertionIndex(
            (RuleType) getComplexListTypeMetadata(),
            getPresentTags(),
            ruleIndex,
            metadataName
        );
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        return toExtensionList(CompositeRules.addableFields(getComplexListTypeMetadata(), getPresentTags()), true);
    }
}
