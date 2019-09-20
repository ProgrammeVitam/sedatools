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
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * Abstract class for composite metadata editor for structured metadata list edition.
 * <p>Used for CompositeEditorPanel.
 */
abstract public class CompositeEditor extends MetadataEditor{

    /**
     * The metadata edition graphic component
     */
    protected List<MetadataEditor> metadataEditorList;

    /**
     * Instantiates a new Composite editor.
     *
     * @param metadata the composite metadata
     * @param father   the father
     */
    public CompositeEditor(Object metadata, MetadataEditor father) {
        super (metadata,father);
        this.metadataEditorList = null;
    }

    /**
     * Gets the list of all metadata that can be added (one time metadata not present and all multiple metadata).
     *
     * @return the extension list
     * @throws SEDALibException the seda lib exception
     */
    abstract public List<Pair<String,String>> getExtensionList() throws SEDALibException;

    /**
     * Add a named metadata child editor.
     *
     * @param metadataName the metadata name
     * @throws SEDALibException the seda lib exception
     */
    abstract public void addChild(String metadataName) throws SEDALibException;

    /**
     * Remove a child editor.
     *
     * @param metadataEditor the metadata editor
     * @throws SEDALibException the seda lib exception
     */
    public void removeChild(MetadataEditor metadataEditor) throws SEDALibException {
        metadataEditorList.remove(metadataEditor);
        ((CompositeEditorPanel)getMetadataEditorPanel()).removeMetadataEditorPanel(metadataEditor.getMetadataEditorPanel());
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
        if (metadataEditorPanel == null)
            createMetadataEditorPanel();
        ((CompositeEditorPanel) metadataEditorPanel).setExpanded(extendedFlag);
        for (MetadataEditor metadataEditor : metadataEditorList)
            if (metadataEditor instanceof CompositeEditor)
                ((CompositeEditor) metadataEditor).doExpand(innerFlag, innerFlag);
    }
}
