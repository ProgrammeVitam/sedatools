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
        sdogep.editDataObjectGroup(au);

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
