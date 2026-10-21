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

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.SEDAObjectEditorCompositePanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.CompositeRules;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.EditedObjects;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.model.MetadataSamples;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * The ComplexListType object editor class.
 */
public class ComplexListTypeEditor extends CompositeEditor {

    // A flag to track when sub editors have been created on expansion
    private boolean noSubEditors = true;

    /**
     * Instantiates a new ComplexListType editor.
     *
     * @param metadata the ComplexListType editedObject
     * @param father   the father
     * @throws SEDALibException if not a ComplexListType editedObject
     */
    public ComplexListTypeEditor(SEDAMetadata metadata, SEDAObjectEditor father) throws SEDALibException {
        super(metadata, father);
        if (!(metadata instanceof ComplexListType)) throw new SEDALibException(
            "La métadonnée à éditer n'est pas du bon type"
        );
    }

    /**
     * Gets ComplexListType form of edited object.
     *
     * @return the ComplexListType
     */
    public ComplexListType getComplexListTypeMetadata() {
        return (ComplexListType) editedObject;
    }

    @Override
    public SEDAMetadata extractEditedObject() throws SEDALibException {
        List<SEDAMetadata> fields = new ArrayList<>();
        for (SEDAObjectEditor objectEditor : objectEditorList) fields.add(
            (SEDAMetadata) objectEditor.extractEditedObject()
        );
        setEditedObject(EditedObjects.rebuild(getComplexListTypeMetadata(), fields));
        return getComplexListTypeMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<>(objectEditorList.size());
        // before its expansion, from the metadata values instead of the sub editors
        if (hasSubeditorsCreatedWhenExpandedFlag()) return EditedObjects.summary((SEDAMetadata) editedObject);
        for (SEDAObjectEditor objectEditor : objectEditorList) {
            String summary = objectEditor.getSummary().trim();
            if (!summary.isEmpty() && !summary.equals("{}")) {
                if (objectEditor instanceof ComplexListTypeEditor) summary = "{" + summary + "}";
                summaryList.add(summary);
            }
        }
        return String.join(", ", summaryList);
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.objectEditorList = new ArrayList<SEDAObjectEditor>();
        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this);
        if (!hasSubeditorsCreatedWhenExpandedFlag()) {
            for (SEDAMetadata detail : getComplexListTypeMetadata().getMetadataList()) {
                SEDAObjectEditor objectEditor = SEDAObjectEditor.createSEDAObjectEditor(detail, this);
                objectEditorList.add(objectEditor);
            }
            noSubEditors = false;
            ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
        }
    }

    @Override
    public List<Pair<String, String>> getExtensionList() throws SEDALibException {
        return toExtensionList(CompositeRules.addableFields(getComplexListTypeMetadata(), getPresentTags()), true);
    }

    protected int getInsertionSEDAObjectEditorIndex(String metadataName) throws SEDALibException {
        return CompositeRules.insertionIndex(getComplexListTypeMetadata(), getPresentTags(), metadataName);
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata = null;
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
            int insertionSEDAObjectEditorIndex = getInsertionSEDAObjectEditorIndex(metadataName);
            objectEditorList.add(insertionSEDAObjectEditorIndex, createSEDAObjectEditor(sedaMetadata, this));
            ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
        }
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        return CompositeRules.isRepeatable(getComplexListTypeMetadata(), metadataName);
    }

    /**
     * Get the create sub editors only when expanded flag, false by default.
     */
    @Override
    public boolean hasSubeditorsCreatedWhenExpandedFlag() {
        if (((SEDAMetadata) getEditedObject()).getXmlElementName().contains("Document")) return noSubEditors;
        return false;
    }

    /**
     * Create sub editors.
     */
    @Override
    public void createSubEditors() {
        try {
            for (SEDAMetadata detail : getComplexListTypeMetadata().getMetadataList()) {
                SEDAObjectEditor objectEditor = SEDAObjectEditor.createSEDAObjectEditor(detail, this);
                objectEditorList.add(objectEditor);
                if (objectEditor instanceof CompositeEditor) ((CompositeEditor) objectEditor).doExpand(false, false);
            }
            ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
        } catch (SEDALibException e) {
            UserInteractionDialog.getUserAnswer(
                ResipGraphicApp.getTheWindow(),
                "Erreur fatale durant la création d'éditeur à la volée des structures\n" + " -> " + e.getMessage(),
                "Erreur",
                UserInteractionDialog.ERROR_DIALOG,
                null
            );
        }
        noSubEditors = false;
    }
}
