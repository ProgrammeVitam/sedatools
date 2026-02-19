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
package fr.gouv.vitam.tools.sedalib.process;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.ResourceUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class CompactorTest implements UseTestFiles {

    private void eraseAll(String dirOrFile) {
        try {
            Files.delete(Paths.get(dirOrFile));
        } catch (Exception ignored) {}
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception ignored) {}
    }

    @Test
    void TestCompactor() throws Exception {
        // Given this test directory imported
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            "src/test/resources/PacketSamples/SampleWithoutLinksModelV1",
            null
        );
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // When Compact the root ArchiveUnit
        ArchiveUnit rootAu = di
            .getArchiveTransfer()
            .getDataObjectPackage()
            .getGhostRootAu()
            .getChildrenAuList()
            .getArchiveUnitList()
            .get(0);
        eraseAll("target/tmpJunit/CompactorTest");
        Compactor compactor = new Compactor(rootAu, "target/tmpJunit/CompactorTest", null);
        compactor.setObjectVersionFilters(List.of("BinaryMaster"), List.of("BinaryMaster", "TextContent"));
        compactor.setCompactedDocumentPackLimit(4096, 4);
        compactor.setDeflatedFlag(true);
        ArchiveUnit compactedAU = compactor.doCompact();

        // Then assert the root DocumentContainer AU content
        String rootContent = ResourceUtils.getResourceAsString("import/AU_ID1.xml");

        assertThat(compactedAU.getContentXmlData()).isEqualTo(rootContent);

        // And assert there are 5 DocumentPack AU content
        assertThat(compactedAU.getChildrenAuList().getCount()).isEqualTo(5);

        // And assert the first DocumentPack AU content
        ArchiveUnit packAU = compactedAU.getChildrenAuList().getArchiveUnitList().get(0);

        assertThat(
            TestUtilities.SlackNormalize(
                packAU
                    .getContentXmlData()
                    .replaceAll("<LastModified>.+<\\/LastModified>", "<LastModified>###TIMESTAMP###<\\/LastModified>")
            )
        ).isEqualTo(ResourceUtils.getResourceAsString("import/AU_ID2.xml"));

        // And assert created files
        File doc = new File("target/tmpJunit/CompactorTest/Document1.zip");
        assertThat(doc).exists();
        assertThat(doc.length()).isGreaterThan(124 * 1024);
        assertThat(doc.length()).isLessThan(126 * 1024);
        doc = new File("target/tmpJunit/CompactorTest/Document3.zip");
        assertThat(doc).exists();
        assertThat(doc.length()).isGreaterThan(2 * 1024);
        assertThat(doc.length()).isLessThan(3 * 1024);
    }

    @Test
    void TestCompactorSeda2V3() throws Exception {
        // Given this test directory imported
        SedaContext.setVersion(SedaVersion.V2_3);
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            "src/test/resources/PacketSamples/SampleWithoutLinksModelV1",
            null
        );
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // When Compact the root ArchiveUnit
        ArchiveUnit rootAu = di
            .getArchiveTransfer()
            .getDataObjectPackage()
            .getGhostRootAu()
            .getChildrenAuList()
            .getArchiveUnitList()
            .get(0);
        BinaryDataObject bdo = di
            .getArchiveTransfer()
            .getDataObjectPackage()
            .getBdoInDataObjectPackageIdMap()
            .get("ID18");
        bdo.addNewMetadata("DataObjectUse", "BinaryMaster");
        bdo.addNewMetadata("DataObjectNumber", 1);
        eraseAll("target/tmpJunit/CompactorTest");
        Compactor compactor = new Compactor(rootAu, "target/tmpJunit/CompactorTest", null);
        compactor.setObjectVersionFilters(List.of("BinaryMaster"), List.of("BinaryMaster", "TextContent"));
        compactor.setCompactedDocumentPackLimit(4096, 4);
        compactor.setDeflatedFlag(true);
        ArchiveUnit compactedAU = compactor.doCompact();

        // Then assert the first DocumentPack AU content
        ArchiveUnit packAU = compactedAU.getChildrenAuList().getArchiveUnitList().get(0);

        assertThat(
            TestUtilities.SlackNormalize(
                packAU
                    .getContentXmlData()
                    .replaceAll("<LastModified>.+<\\/LastModified>", "<LastModified>###TIMESTAMP###<\\/LastModified>")
            )
        ).isEqualTo(ResourceUtils.getResourceAsString("import/AU_ID2V3.xml"));
    }
}
