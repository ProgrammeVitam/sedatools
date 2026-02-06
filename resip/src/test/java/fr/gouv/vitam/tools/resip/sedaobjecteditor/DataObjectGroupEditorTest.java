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

import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.structuredcomponents.ScrollablePanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.BinaryDataObjectEditor;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.composite.PhysicalDataObjectEditor;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;

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

        GridBagLayout gbl=new GridBagLayout();
        ScrollablePanel content=new ScrollablePanel(gbl);
        content.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        content.setScrollableHeight(ScrollablePanel.ScrollableSizeHint.NONE);

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
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weightx=1.0;
        gbc.gridx=0;
        gbc.gridy=0;
        content.add(bdoe.sedaObjectEditorPanel,gbc);

        String pdoXML = "<DataObjectVersion>PhysicalMaster_1</DataObjectVersion>\n" +
                "<PhysicalId>940 W</PhysicalId>\n" +
                "<PhysicalDimensions>\n" +
                "  <Width unit=\"centimetre\">10.0</Width>\n" +
                "  <Height unit=\"centimetre\">8.0</Height>\n" +
                "  <Depth unit=\"centimetre\">1.0</Depth>\n" +
                "  <Diameter unit=\"centimetre\">0.0</Diameter>\n" +
                "  <Weight unit=\"gram\">59.0</Weight>\n" +
                "</PhysicalDimensions>\n" +
                "<Extent>1carteimprimée</Extent>\n" +
                "<Dimensions>10,5cmx14,8cm</Dimensions>\n" +
                "<Color>Noiretblanc</Color>\n" +
                "<Framing>Paysage</Framing>\n" +
                "<Technique>Phototypie</Technique>";
        PhysicalDataObject pdo=new PhysicalDataObject(dop);
        pdo.fromSedaXmlFragments(pdoXML);
        pdo.setInDataObjectPackageId("TestID");

        PhysicalDataObjectEditor pdoe=new PhysicalDataObjectEditor(pdo,null);
        pdoe.doExpand(true, false);
        gbc=new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weightx=1.0;
        gbc.gridx=0;
        gbc.gridy=1;
        content.add(pdoe.sedaObjectEditorPanel,gbc);

        JScrollPane scrollPane=new JScrollPane(content);
        dialog.setContentPane(scrollPane);
        dialog.setVisible(true);
    }
}
