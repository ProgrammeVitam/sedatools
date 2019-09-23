package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.ScrollablePanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.ComplexListTypeEditor;

import javax.swing.*;
import java.awt.*;

public class AllMetadataEditorTest {

    static String[] demoMetadataTypes = {"ArchiveUnitProfile", "Management", "Content","PhysicalDimensions"};

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
            SEDAObjectEditor objectEditor = SEDAObjectEditor.createSEDAObjectEditor(demoMetadataTypes[i], demoMetadataTypes[i] + "Name", false, null);
            if (objectEditor instanceof ComplexListTypeEditor)
                ((ComplexListTypeEditor) objectEditor).doExpand(true, false);
            contentPane.add(objectEditor.getSEDAObjectEditorPanel());
        }
        dialog.setVisible(true);
    }
}
