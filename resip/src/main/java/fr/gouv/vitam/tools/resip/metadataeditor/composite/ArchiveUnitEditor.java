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
import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.CompositeEditorPanel;
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

import static fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditorConstants.translateMetadataName;

/**
 * The ArchiveUnit metadata editor class.
 */
public class ArchiveUnitEditor extends CompositeEditor {

    /**
     * Instantiates a new ArchiveUnitEditor editor.
     *
     * @param metadata the ArchiveUnitEditor metadata
     * @param father   the father
     */
    public ArchiveUnitEditor(ArchiveUnit metadata, MetadataEditor father) {
        super(metadata, father);
    }

    private ArchiveUnit getArchiveUnitMetadata() {
        return (ArchiveUnit) metadata;
    }

    @Override
    public String getName() {
        return translateMetadataName("ArchiveUnit") + " - " + (metadata == null ? translateMetadataName("Unknown") : getArchiveUnitMetadata().getInDataObjectPackageId());
    }

    @Override
    public ArchiveUnit extractEditedObject() throws SEDALibException {
        ArchiveUnit tmpAu = new ArchiveUnit();
        for (MetadataEditor me : metadataEditorList) {
            SEDAMetadata sedaMetadata = (SEDAMetadata)me.extractEditedObject();
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
        return getName();
    }

    private void renewMetadataEditorList() throws SEDALibException {
        if (metadataEditorList != null)
            for (MetadataEditor me : metadataEditorList)
                ((CompositeEditorPanel)getMetadataEditorPanel()).removeMetadataEditorPanel(me.getMetadataEditorPanel());

        this.metadataEditorList = new ArrayList<MetadataEditor>();
        if (getArchiveUnitMetadata() != null) {
            if (getArchiveUnitMetadata().getContent() != null) {
                MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getArchiveUnitMetadata().getContent(), this);
                metadataEditorList.add(metadataEditor);
                ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(0, metadataEditor.getMetadataEditorPanel());
                ((CompositeEditor)metadataEditor).doExpand(true,false);
            }
            if (getArchiveUnitMetadata().getManagement() != null) {
                MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getArchiveUnitMetadata().getManagement(), this);
                metadataEditorList.add(metadataEditor);
                ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(1, metadataEditor.getMetadataEditorPanel());
                ((CompositeEditor)metadataEditor).doExpand(true,false);
            }
            if (getArchiveUnitMetadata().getArchiveUnitProfile() != null) {
                MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(getArchiveUnitMetadata().getArchiveUnitProfile(), this);
                metadataEditorList.add(metadataEditor);
                ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(2, metadataEditor.getMetadataEditorPanel());
                ((CompositeEditor)metadataEditor).doExpand(true,false);
            }
        }
    }

    @Override
    public void createMetadataEditorPanel() throws SEDALibException {
        this.metadataEditorPanel = new CompositeEditorPanel(this,null,true);
        renewMetadataEditorList();
    }

    @Override
    public List<Pair<String, String>> getExtensionList() {
        if (getArchiveUnitMetadata() == null)
            return new ArrayList<Pair<String, String>>();

        List<Pair<String, String>> extensionList = new ArrayList<Pair<String, String>>(Arrays.asList(
                Pair.of("ArchiveUnitProfile", translateMetadataName("ArchiveUnitProfile")),
                Pair.of("Content", translateMetadataName("Content")),
                Pair.of("Management", translateMetadataName("Management"))));

        for (MetadataEditor me : metadataEditorList) {
            String name = me.getName();
            extensionList.remove(Pair.of(name, translateMetadataName(name)));
        }
        return extensionList;
    }

    @Override
    public void addChild(String metadataName) throws SEDALibException {
        SEDAMetadata sedaMetadata = null;
        int insertionIndex = 0;
        switch (metadataName) {
            case "Content":
                sedaMetadata = createSEDAMetadataSample("Content", "Content", true);
                insertionIndex = 0;
                break;
            case "Management":
                sedaMetadata = createSEDAMetadataSample("Management", "Management", true);
                insertionIndex = 1;
                break;
            case "ArchiveUnitProfile":
                sedaMetadata = createSEDAMetadataSample("ArchiveUnitProfile", "ArchiveUnitProfile", true);
                insertionIndex = 2;
                break;
        }
        if (sedaMetadata == null)
            throw new SEDALibException("La métadonnée [" + metadataName + "] n'existe pas dans un ArchiveUnit");
        MetadataEditor addedMetadataEditor = createMetadataEditor(sedaMetadata, this);
        metadataEditorList.add(insertionIndex,
                addedMetadataEditor);
        ((CompositeEditorPanel) metadataEditorPanel).addMetadataEditorPanel(insertionIndex, addedMetadataEditor.getMetadataEditorPanel());
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
        this.metadata = archiveUnit;
        renewMetadataEditorList();
        ((CompositeEditorPanel) getMetadataEditorPanel()).refreshLoad(metadata == null);
    }
}
