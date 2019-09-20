package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.metadataeditor.composite.ComplexListTypeEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.composite.CompositeEditor;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;

public class MetadataEditorPanel extends ScrollablePanel {

    MetadataEditor metadataEditor;

    MetadataEditorPanel(MetadataEditor metadataEditor) {
        this.metadataEditor = metadataEditor;
    }

    public void lessButton() {
        try {
            if (metadataEditor.getFather() != null) {
                ((CompositeEditor) metadataEditor.getFather()).removeChild(metadataEditor);
                ((CompositeEditor) metadataEditor.getFather()).getMetadataEditorPanelTopParent().validate();
            }
        } catch (SEDALibException ignored) {
        }
    }

    public void addButton() {
        try {
            if (metadataEditor.getFather() != null) {
                ((ComplexListTypeEditor) metadataEditor.getFather()).addChild(metadataEditor.getName());
                ((ComplexListTypeEditor) metadataEditor.getFather()).getMetadataEditorPanelTopParent().validate();
            }
        } catch (SEDALibException ignored) {
        }
    }
}
