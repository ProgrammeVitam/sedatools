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
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants.translateTag;

/**
 * The ArchiveUnit object editor class.
 */
public class ArchiveUnitEditor extends CompositeEditor {

    /**
     * Ordered SEDAObjectEditor collection
     */
    private SEDAObjectEditor[] objectEditorArray;

    /**
     * Instantiates a new ArchiveUnit editor.
     *
     * @param editedObject the ArchiveUnit editedObject
     * @param father       the father
     */
    public ArchiveUnitEditor(ArchiveUnit editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
        objectEditorArray = new SEDAObjectEditor[3];
    }

    private ArchiveUnit getArchiveUnitMetadata() {
        return (ArchiveUnit) editedObject;
    }

    @Override
    public String getTag() {
        return "ArchiveUnit";
    }

    @Override
    public String getName() {
        return translateTag("ArchiveUnit") + " - " + (editedObject == null ? translateTag("Unknown") : getArchiveUnitMetadata().getInDataObjectPackageId());
    }

    @Override
    public ArchiveUnit extractEditedObject() throws SEDALibException {
        ArchiveUnit tmpAu = new ArchiveUnit();
        for (SEDAObjectEditor me : objectEditorList) {
            SEDAMetadata sedaMetadata = (SEDAMetadata) me.extractEditedObject();
            switch (sedaMetadata.getXmlElementName()) {
                case "ArchiveUnitProfile":
                    tmpAu.setArchiveUnitProfile((ArchiveUnitProfile) sedaMetadata);
                    break;
                case "Content":
                    tmpAu.setContent((Content) sedaMetadata);
                    break;
                case "Management":
                    tmpAu.setManagement((Management) sedaMetadata);
                    break;
            }
        }

        getArchiveUnitMetadata().setContent(tmpAu.getContent());
        getArchiveUnitMetadata().setManagement(tmpAu.getManagement());
        getArchiveUnitMetadata().setArchiveUnitProfile(tmpAu.getArchiveUnitProfile());

        return getArchiveUnitMetadata();
    }

    @Override
    public String getSummary() throws SEDALibException {
        return getTag();
    }

    private void updateObjectEditorList() throws SEDALibException {
        List<SEDAObjectEditor> result = new ArrayList<SEDAObjectEditor>();
        for (int i = 0; i < 3; i++)
            if (objectEditorArray[i] != null)
                result.add(objectEditorArray[i]);
        objectEditorList = result;
        ((SEDAObjectEditorCompositePanel) sedaObjectEditorPanel).synchronizePanels();
    }

    private void renewObjectEditorList() throws SEDALibException {
        for (int i = 0; i < 3; i++)
            objectEditorArray[i] = null;
        if (getArchiveUnitMetadata() != null) {
            if (getArchiveUnitMetadata().getContent() != null) {
                objectEditorArray[0] = SEDAObjectEditor.createSEDAObjectEditor(getArchiveUnitMetadata().getContent(), this);
                ((CompositeEditor) objectEditorArray[0]).doExpand(true, false);
            }
            if (getArchiveUnitMetadata().getManagement() != null) {
                objectEditorArray[1] = SEDAObjectEditor.createSEDAObjectEditor(getArchiveUnitMetadata().getManagement(), this);
                ((CompositeEditor) objectEditorArray[1]).doExpand(true, false);
            }
            if (getArchiveUnitMetadata().getArchiveUnitProfile() != null)
                objectEditorArray[2] = SEDAObjectEditor.createSEDAObjectEditor(getArchiveUnitMetadata().getArchiveUnitProfile(), this);
        }

        updateObjectEditorList();
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        this.sedaObjectEditorPanel = new SEDAObjectEditorCompositePanel(this, null, true);
        renewObjectEditorList();
    }

    @Override
    public List<Pair<String, String>> getExtensionList() {
        if (getArchiveUnitMetadata() == null)
            return new ArrayList<Pair<String, String>>();

        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(Arrays.asList(
                Pair.of("ArchiveUnitProfile", translateTag("ArchiveUnitProfile")),
                Pair.of("Content", translateTag("Content")),
                Pair.of("Management", translateTag("Management"))));

        for (SEDAObjectEditor me : objectEditorList) {
            String name = me.getTag();
            extensionList.remove(Pair.of(name, translateTag(name)));
        }
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata = null;
        switch (metadataName) {
            case "Content":
                sedaMetadata = createSEDAMetadataSample("Content", "Content", true);
                objectEditorArray[0] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "Management":
                sedaMetadata = createSEDAMetadataSample("Management", "Management", true);
                objectEditorArray[1] = createSEDAObjectEditor(sedaMetadata, this);
                break;
            case "ArchiveUnitProfile":
                sedaMetadata = createSEDAMetadataSample("ArchiveUnitProfile", "ArchiveUnitProfile", true);
                objectEditorArray[2] = createSEDAObjectEditor(sedaMetadata, this);
                break;
        }
        if (sedaMetadata == null)
            throw new SEDALibException("La métadonnée [" + metadataName + "] n'existe pas dans un ArchiveUnit");

        updateObjectEditorList();
    }

    @Override
    public boolean canContainsMultiple(String metadataName) throws SEDALibException {
        return false;
    }

    /**
     * Edit a new archive unit.
     *
     * @param archiveUnit the archive unit
     * @throws SEDALibException the seda lib exception
     */
    public void editArchiveUnit(ArchiveUnit archiveUnit) throws SEDALibException {
        this.editedObject = archiveUnit;
        renewObjectEditorList();
        ((SEDAObjectEditorCompositePanel) getSEDAObjectEditorPanel()).refreshLoad(editedObject == null);
    }
}
