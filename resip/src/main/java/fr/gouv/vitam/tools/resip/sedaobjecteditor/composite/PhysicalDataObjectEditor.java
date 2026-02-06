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
import fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditorConstants;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.PersistentIdentifier;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.StringType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The PhysicalDataObject object editor class.
 */
public class PhysicalDataObjectEditor extends AbstractUnitaryDataObjectEditor {


    /**
     * Instantiates a new PhysicalDataObject editor.
     *
     * @param editedObject the PhysicalDataObject editedObject
     * @param father       the father
     */
    public PhysicalDataObjectEditor(PhysicalDataObject editedObject, SEDAObjectEditor father) {
        super(editedObject, father);
    }

    private PhysicalDataObject getPhysicalDataObject() {
        return (PhysicalDataObject) editedObject;
    }

    @Override
    public PhysicalDataObject extractEditedObject() throws SEDALibException {
        return (PhysicalDataObject) super.extractEditedObject();
    }

    @Override
    public String getSummary() throws SEDALibException {
        List<String> summaryList = new ArrayList<>(objectEditorList.size());
        String tmp;
        for (SEDAMetadata sm : getPhysicalDataObject().getMetadataList()) {
            if (sm instanceof StringType)
                summaryList.add(((StringType) sm).getValue());
            else if (sm instanceof PersistentIdentifier)
                summaryList.add(((PersistentIdentifier) sm).getSummary());
        }
        return String.join(", ", summaryList);
    }

    @Override
    public void createSEDAObjectEditorPanel() throws SEDALibException {
        prepareSEDAObjectEditorPanel(null );
    }

    /**
     * Create physical data object sample binary data object.
     *
     * @param minimal the minimal flag, if true subfields are selected and values are empty, if false all subfields are added and values are default values
     * @return the physical data object
     * @throws SEDALibException the seda lib exception
     */
    public static PhysicalDataObject createPhysicalDataObjectSample(boolean minimal) throws SEDALibException {
        PhysicalDataObject result = new PhysicalDataObject();

        for (Map.Entry<String, ComplexListMetadataKind> e : result.getMetadataMap().entrySet()) {
            if (SEDAObjectEditorConstants.minimalTagList.contains(e.getKey()))
                result.addMetadata(createSEDAMetadataSample(e.getValue().getMetadataClass().getName(), e.getKey(), minimal));
        }

        return result;
    }
}
