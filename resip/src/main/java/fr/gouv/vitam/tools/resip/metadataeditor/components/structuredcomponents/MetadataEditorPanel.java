package fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents;

import fr.gouv.vitam.tools.resip.metadataeditor.composite.ComplexListTypeEditor;
import fr.gouv.vitam.tools.resip.metadataeditor.MetadataEditor;
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
                ((ComplexListTypeEditor) metadataEditor.getFather()).removeChild(metadataEditor);
                ((ComplexListTypeEditor) metadataEditor.getFather()).getMetadataEditorPanelTopParent().validate();
            } else if (getParent() instanceof InnerStructuredArchiveUnitEditorPanel) {
                InnerStructuredArchiveUnitEditorPanel auep=(InnerStructuredArchiveUnitEditorPanel)getParent();
                auep.removeChild(this.metadataEditor);
            }
        } catch (SEDALibException ignored) {
        }
    }

    public void addButton() {
        try {
            if (metadataEditor.getFather() != null) {
                ((ComplexListTypeEditor) metadataEditor.getFather()).addChild(metadataEditor.getMetadata().getXmlElementName());
                ((ComplexListTypeEditor) metadataEditor.getFather()).getMetadataEditorPanelTopParent().validate();
            }
        } catch (SEDALibException ignored) {
        }
    }
}
