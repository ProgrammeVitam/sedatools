package fr.gouv.vitam.tools.resip.metadataeditor;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.metadataeditor.components.highlevelcomponents.XMLArchiveUnitEditorPanel;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.SEDAMetadata;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;

import javax.swing.*;
import java.awt.*;

public class XMLArchiveUnitEditorPanelTest {

    public static void main(String[] args) throws Exception {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        else
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        ResipGraphicApp rga = new ResipGraphicApp(null);
        Thread.sleep(1000);
        JDialog dialog = new JDialog();
        dialog.setMinimumSize(new Dimension(600, 600));
        dialog.setPreferredSize(new Dimension(600, 600));

        DataObjectPackage dop=new DataObjectPackage();
        ArchiveUnit archiveUnit=new ArchiveUnit();
        SEDAMetadata sedaMetadata;

        archiveUnit.setInDataObjectPackageId("TestID");
        sedaMetadata=MetadataEditor.createSEDAMetadataSample("ArchiveUnitProfile","ArchiveUnitProfile",true);
        archiveUnit.setArchiveUnitProfile((ArchiveUnitProfile)sedaMetadata);
        sedaMetadata=MetadataEditor.createSEDAMetadataSample("Content","Content",true);
        archiveUnit.setContent((Content)sedaMetadata);
        sedaMetadata=MetadataEditor.createSEDAMetadataSample("Management","Management",true);
        archiveUnit.setManagement((Management)sedaMetadata);
        dop.addArchiveUnit(archiveUnit);

        XMLArchiveUnitEditorPanel xauep=new XMLArchiveUnitEditorPanel();
        xauep.editArchiveUnit(archiveUnit);

        dialog.setContentPane(xauep);
        dialog.setVisible(true);
    }
}
