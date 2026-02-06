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
package fr.gouv.vitam.tools.resip.sedaobjecteditor;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.XMLArchiveUnitEditorPanel;
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
        sedaMetadata= SEDAObjectEditor.createSEDAMetadataSample("ArchiveUnitProfile","ArchiveUnitProfile",true);
        archiveUnit.setArchiveUnitProfile((ArchiveUnitProfile)sedaMetadata);
        sedaMetadata= SEDAObjectEditor.createSEDAMetadataSample("Content","Content",true);
        archiveUnit.setContent((Content)sedaMetadata);
        sedaMetadata= SEDAObjectEditor.createSEDAMetadataSample("Management","Management",true);
        archiveUnit.setManagement((Management)sedaMetadata);
        dop.addArchiveUnit(archiveUnit);

        XMLArchiveUnitEditorPanel xauep=new XMLArchiveUnitEditorPanel();
        xauep.editArchiveUnit(archiveUnit);

        dialog.setContentPane(xauep);
        dialog.setVisible(true);
    }
}
