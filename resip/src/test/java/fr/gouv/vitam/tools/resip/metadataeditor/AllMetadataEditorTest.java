package fr.gouv.vitam.tools.resip.metadataeditor;

import fr.gouv.vitam.tools.resip.metadataeditor.components.structuredcomponents.ScrollablePanel;
import fr.gouv.vitam.tools.resip.metadataeditor.composite.ComplexListTypeEditor;

import javax.swing.*;
import java.awt.*;

public class AllMetadataEditorTest {

    static String[] demoMetadataTypes = {"ArchiveUnitProfile", "Management", "Content"};

    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        else
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        JDialog dialog = new JDialog();
        dialog.setMinimumSize(new Dimension(600, 800));
        dialog.setPreferredSize(new Dimension(600, 800));

        Container full = dialog.getContentPane();
        ScrollablePanel contentPane = new ScrollablePanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contentPane);
        full.add(scrollPane);

        contentPane.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        contentPane.setScrollableHeight(ScrollablePanel.ScrollableSizeHint.NONE);

        int i;
        for (i = 0; i < demoMetadataTypes.length; i++) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(demoMetadataTypes[i], demoMetadataTypes[i] + "Name", false, null);
            if (metadataEditor instanceof ComplexListTypeEditor)
                ((ComplexListTypeEditor) metadataEditor).doExpand(true, false);
            contentPane.add(metadataEditor.getMetadataEditorPanel());
        }
        dialog.setVisible(true);
    }
}
