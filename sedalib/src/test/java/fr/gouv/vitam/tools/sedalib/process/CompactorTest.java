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
        } catch (Exception ignored) {
        }
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception ignored) {
        }
    }

    @Test
    void TestCompactor() throws Exception {

        // Given this test directory imported
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
                "src/test/resources/PacketSamples/SampleWithoutLinksModelV1", null);
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // When Compact the root ArchiveUnit
        ArchiveUnit rootAu = di.getArchiveTransfer().getDataObjectPackage().getGhostRootAu().getChildrenAuList().getArchiveUnitList().get(0);
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

        assertThat(TestUtilities.SlackNormalize(packAU.getContentXmlData().replaceAll("<LastModified>.+<\\/LastModified>",
            "<LastModified>###TIMESTAMP###<\\/LastModified>")))
                .isEqualTo(ResourceUtils.getResourceAsString("import/AU_ID2.xml"));

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
                "src/test/resources/PacketSamples/SampleWithoutLinksModelV1", null);
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();

        // When Compact the root ArchiveUnit
        ArchiveUnit rootAu = di.getArchiveTransfer().getDataObjectPackage().getGhostRootAu().getChildrenAuList().getArchiveUnitList().get(0);
        BinaryDataObject bdo=di.getArchiveTransfer().getDataObjectPackage().getBdoInDataObjectPackageIdMap().get("ID18");
        bdo.addNewMetadata("DataObjectUse","BinaryMaster");
        bdo.addNewMetadata("DataObjectNumber",1);
        eraseAll("target/tmpJunit/CompactorTest");
        Compactor compactor = new Compactor(rootAu, "target/tmpJunit/CompactorTest", null);
        compactor.setObjectVersionFilters(List.of("BinaryMaster"), List.of("BinaryMaster", "TextContent"));
        compactor.setCompactedDocumentPackLimit(4096, 4);
        compactor.setDeflatedFlag(true);
        ArchiveUnit compactedAU = compactor.doCompact();

        // Then assert the first DocumentPack AU content
        ArchiveUnit packAU = compactedAU.getChildrenAuList().getArchiveUnitList().get(0);

        assertThat(TestUtilities.SlackNormalize(packAU.getContentXmlData().replaceAll("<LastModified>.+<\\/LastModified>",
                "<LastModified>###TIMESTAMP###<\\/LastModified>")))
                .isEqualTo(ResourceUtils.getResourceAsString("import/AU_ID2V3.xml"));
    }
}
