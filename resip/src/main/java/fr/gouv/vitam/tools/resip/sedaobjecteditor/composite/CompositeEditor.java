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
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Abstract class for composite object editor for structured editedObject list edition.
 * <p>Used for SEDAObjectEditorCompositePanel.
 */
public abstract class CompositeEditor extends SEDAObjectEditor {

    /**
     * The editedObject edition graphic component
     */
    public List<SEDAObjectEditor> objectEditorList;

    /**
     * Instantiates a new Composite editor.
     *
     * @param editedObject the composite editedObject
     * @param father       the father
     */
    protected CompositeEditor(Object editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
        this.objectEditorList = null;
    }

    /**
     * Gets the list of all editedObject that can be added (one time editedObjects not present and all multiple editedObjects).
     *
     * @return the extension list
     * @throws SEDALibException the seda lib exception
     */
    public abstract List<Pair<String, String>> getExtensionList() throws SEDALibException;

    /**
     * Add a named editedObject child editor.
     *
     * @param metadataName the editedObject name
     * @throws SEDALibException the seda lib exception
     */
    public abstract void addChild(String metadataName) throws SEDALibException;

    /**
     * Remove a child SEDA object editor.
     *
     * @param objectEditor the SEDA object editor
     * @throws SEDALibException the seda lib exception
     */
    public void removeChild(SEDAObjectEditor objectEditor) throws SEDALibException {
        objectEditorList.remove(objectEditor);
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    /**
     * Refresh editedObject label.
     */
    public void refreshEditedObjectLabel()  {
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).refreshEditedObjectLabel();
    }

    /**
     * Expand the imbricated structure:
     * <p>- if extendedFlag true of the upper level
     * <p>- if innerFlag true of all the innerFlag levels
     *
     * @param extendedFlag the extendedFlag
     * @param innerFlag    the innerFlag
     * @throws SEDALibException the seda lib exception
     */
    public void doExpand(boolean extendedFlag, boolean innerFlag) throws SEDALibException {
        if (sedaObjectEditorPanel == null)
            createSEDAObjectEditorPanel();
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).setExpanded(extendedFlag);
        for (SEDAObjectEditor objectEditor : objectEditorList)
            if (objectEditor instanceof CompositeEditor)
                ((CompositeEditor) objectEditor).doExpand(innerFlag, innerFlag);
    }

    /**
     * Get the create sub editors only when expanded flag, false by default.
     */
    public boolean hasSubeditorsCreatedWhenExpandedFlag() {
        return false;
    }

    /**
     * Create sub editors.
     */
    public void createSubEditors() {
    }
}
