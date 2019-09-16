package fr.gouv.vitam.tools.resip.metadataeditor;

import fr.gouv.vitam.tools.resip.metadataeditor.composite.ComplexListTypeEditor;

import javax.swing.*;
import java.awt.*;

public class AllMetadataEditorTest {

    static String[] demoMetadataTypes = {"ArchiveUnitProfile", "Management", "Content"};
//    static String[] demoMetadataTypes = {"ArchiveUnitProfile", "Management", "Content",
//            "CustodialHistory","Keyword","Coverage","RelatedObjectReference", "Event", "Signature", "Gps", "Signer",
//            "StringType", "TextType", "IntegerType", "SIPInternalIDType", "DateType", "DateTimeType", "AnyXMLType",
//            "KeywordType"};

    static String[] specialDemoMetadataTypes = {};
//    static String[] specialDemoMetadataTypes = {"OriginatingAgency", "AgencyType", "Writer", "AgentType"};

    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        else
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        JDialog dialog = new JDialog();
        dialog.setMinimumSize(new Dimension(600, 800));
        dialog.setPreferredSize(new Dimension(600, 800));

        Container full = dialog.getContentPane();
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contentPane);
        full.add(scrollPane);


        int i;
        for (i = 0; i < demoMetadataTypes.length; i++) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(demoMetadataTypes[i], demoMetadataTypes[i] + "Name", false, null);
            if (metadataEditor instanceof ComplexListTypeEditor)
                ((ComplexListTypeEditor) metadataEditor).setExtended(true, true);
            contentPane.add(metadataEditor.getMetadataEditorPanel());
        }
        int count = i;
        for (; i < count + specialDemoMetadataTypes.length; i += 2) {
            MetadataEditor metadataEditor = MetadataEditor.createMetadataEditor(specialDemoMetadataTypes[i + 1 - count], specialDemoMetadataTypes[i - count] + "Name", false, null);
            if (metadataEditor instanceof ComplexListTypeEditor)
                ((ComplexListTypeEditor) metadataEditor).setExtended(true, true);
            contentPane.add(metadataEditor.getMetadataEditorPanel());
        }
        dialog.setVisible(true);
    }
}
