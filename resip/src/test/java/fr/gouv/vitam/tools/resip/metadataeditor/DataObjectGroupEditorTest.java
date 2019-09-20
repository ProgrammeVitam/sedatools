package fr.gouv.vitam.tools.resip.metadataeditor;

import fr.gouv.vitam.tools.resip.metadataeditor.composite.BinaryDataObjectEditor;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;

import javax.swing.*;
import java.awt.*;

public class DataObjectGroupEditorTest {

    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        else
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        JDialog dialog = new JDialog();
        dialog.setMinimumSize(new Dimension(600, 600));
        dialog.setPreferredSize(new Dimension(600, 600));

        DataObjectPackage dop=new DataObjectPackage();
        String bdoXML = "<DataObjectVersion>BinaryMaster_1</DataObjectVersion>\n" +
                "  <Uri>content/ID52.jpg</Uri>\n" +
                "  <MessageDigest algorithm=\"SHA-512\">e321b289f1800e5fa3be1b8d01687c8999ef3ecfec759bd0e19ccd92731036755c8f79cbd4af8f46fc5f4e14ad805f601fe2e9b58ad0b9f5a13695c0123e45b3</MessageDigest>\n" +
                "  <Size>21232</Size>\n" +
                "  <FormatIdentification>\n" +
                "    <FormatLitteral>Exchangeable Image File Format (Compressed)</FormatLitteral>\n" +
                "    <MimeType>image/jpeg</MimeType>\n" +
                "    <FormatId>fmt/645</FormatId>\n" +
                "  </FormatIdentification>\n" +
                "  <FileInfo>\n" +
                "    <Filename>image001.jpg</Filename>\n" +
                "    <LastModified>2018-08-28T19:22:19</LastModified>\n" +
                "  </FileInfo>\n" +
                "  <Metadata>\n" +
                "    <Image>\n" +
                "      <Dimensions>117x76</Dimensions>\n" +
                "      <Width>117px</Width>\n" +
                "      <Height>76px</Height>\n" +
                "      <VerticalResolution>96ppp</VerticalResolution>\n" +
                "      <HorizontalResolution>96ppp</HorizontalResolution>\n" +
                "      <ColorDepth>24</ColorDepth>\n" +
                "    </Image>\n" +
                "  </Metadata>";
        BinaryDataObject bdo=new BinaryDataObject(dop);
        bdo.fromSedaXmlFragments(bdoXML);
        bdo.setInDataObjectPackageId("TestID");

        BinaryDataObjectEditor bdoe=new BinaryDataObjectEditor(bdo,null);
        bdoe.doExpand(true, false);

        JScrollPane scrollPane=new JScrollPane(bdoe.metadataEditorPanel);
        dialog.setContentPane(scrollPane);
        dialog.setVisible(true);
    }
}
