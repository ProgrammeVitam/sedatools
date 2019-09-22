package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.StructuredDataObjectGroupEditorPanel;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;

import javax.swing.*;
import java.awt.*;

public class StructuredDataObjectGroupEditorPanelTest {

    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        else
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        ResipGraphicApp rga = new ResipGraphicApp(null);
        Thread.sleep(1000);

        JDialog dialog = new JDialog(rga.getTheWindow(), "DOG test", true);
        dialog.setMinimumSize(new Dimension(600, 600));
        dialog.setPreferredSize(new Dimension(600, 600));

        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter("sedalib/src/test/resources/PacketSamples/TestSipDogMerge.zip",
                "target/tmpJunit/TestSipDogMerge.zip-tmpdir", null);
        si.doImport();
        ArchiveUnit au = si.getArchiveTransfer().getDataObjectPackage().getAuInDataObjectPackageIdMap()
                .get("ID21");
        DataObjectGroup dog = si.getArchiveTransfer().getDataObjectPackage().getDogInDataObjectPackageIdMap()
                .get("ID16");

        StructuredDataObjectGroupEditorPanel sdogep = new StructuredDataObjectGroupEditorPanel();
        sdogep.editDataObjectGroup(null, au);

        dialog.setContentPane(sdogep);
        dialog.setVisible(true);
        for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
            System.out.println("BinaryDataObject:");
            System.out.println(bdo.toSedaXmlFragments());
        }
        for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList()) {
            System.out.println("PhysicalDataObject:");
            System.out.println(pdo.toSedaXmlFragments());
        }
        System.out.println("LogBook:");
        System.out.println(dog.toSedaXmlFragments());
    }
}
