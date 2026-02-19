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
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class DeCompactorTest implements UseTestFiles {

    private void eraseAll(String dirOrFile) {
        try {
            Files.delete(Paths.get(dirOrFile));
        } catch (Exception ignored) {}
        try {
            FileUtils.deleteDirectory(new File(dirOrFile));
        } catch (Exception ignored) {}
    }

    /**
     * The Sort by title tool.
     */
    static class SortByTitle implements Comparator<ArchiveUnit> {

        public int compare(ArchiveUnit a, ArchiveUnit b) {
            String titleA = null;
            String titleB = null;
            try {
                titleA = a.getContent().getSimpleMetadata("Title").toLowerCase();
                titleB = b.getContent().getSimpleMetadata("Title").toLowerCase();
            } catch (SEDALibException e) {
                throw new RuntimeException(e);
            }
            return titleA.compareTo(titleB);
        }

        public SortByTitle() {}
    }

    private void sortDataObjectPackage(DataObjectPackage dataObjectPackage) {
        SortByTitle sortByTitle = new SortByTitle();
        for (Map.Entry<String, ArchiveUnit> pair : dataObjectPackage.getAuInDataObjectPackageIdMap().entrySet()) {
            Collections.sort(pair.getValue().getChildrenAuList().getArchiveUnitList(), sortByTitle);
        }
        Collections.sort(dataObjectPackage.getGhostRootAu().getChildrenAuList().getArchiveUnitList(), sortByTitle);
    }

    private String removeLines(String s, int number) {
        String[] lines = s.split("\\R");
        List<String> linelist = new ArrayList<>(lines.length);
        for (int i = number; i < lines.length; i++) linelist.add(lines[i]);
        return String.join(System.lineSeparator(), linelist);
    }

    @Test
    void TestDeCompactor() throws Exception {
        // Given this test directory imported with Management metadata put on root and ArchiveUnit sorted
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            "src/test/resources/PacketSamples/SampleWithoutLinksModelV1",
            null
        );
        di.addIgnorePattern("Thumbs.db");
        di.addIgnorePattern("pagefile.sys");
        di.doImport();
        ArchiveTransferToSIPExporter sm = new ArchiveTransferToSIPExporter(di.getArchiveTransfer(), null);
        di.getArchiveTransfer().setGlobalMetadata(new GlobalMetadata());
        DataObjectPackage dataObjectPackage = di.getArchiveTransfer().getDataObjectPackage();
        dataObjectPackage.setManagementMetadataXmlData(
            "    <ManagementMetadata>\n" +
            "      <AcquisitionInformation>Acquisition Information</AcquisitionInformation>\n" +
            "      <LegalStatus>Public Archive</LegalStatus>\n" +
            "      <OriginatingAgencyIdentifier>Service_producteur</OriginatingAgencyIdentifier>\n" +
            "      <SubmissionAgencyIdentifier>Service_versant</SubmissionAgencyIdentifier>\n" +
            "    </ManagementMetadata>"
        );
        sortDataObjectPackage(dataObjectPackage);
        dataObjectPackage.regenerateContinuousIds();
        Management management = dataObjectPackage.getArchiveUnitById("ID11").getManagement();
        dataObjectPackage.getArchiveUnitById("ID11").setManagement(null);
        dataObjectPackage.getArchiveUnitById("ID10").setManagement(management);
        String originManifestString = removeLines(sm.getSEDAXMLManifest(true, true), 2);
        Map<String, BinaryDataObject> originBinaryMap = Map.copyOf(dataObjectPackage.getBdoInDataObjectPackageIdMap());

        // When compact the root ArchiveUnit and decompact it
        ArchiveUnit rootAu = dataObjectPackage.getGhostRootAu().getChildrenAuList().getArchiveUnitList().get(0);
        eraseAll("target/tmpJunit/CompactorTest");
        Compactor compactor = new Compactor(rootAu, "target/tmpJunit/CompactorTest", null);
        compactor.setObjectVersionFilters(
            List.of("BinaryMaster", "TextContent"),
            List.of("BinaryMaster", "TextContent")
        );
        compactor.setCompactedDocumentPackLimit(4096, 4);
        compactor.setDeflatedFlag(true);
        ArchiveUnit compactedAU = compactor.doCompact();
        int compactedArchiveUnitNumber = dataObjectPackage.getArchiveUnitCount();

        DeCompactor decompactor = new DeCompactor(compactedAU, "target/tmpJunit/DeCompactorTest", null);
        eraseAll("target/tmpJunit/DeCompactorTest");
        ArchiveUnit decompactedAU = decompactor.doDeCompact();
        sortDataObjectPackage(dataObjectPackage);
        dataObjectPackage.regenerateContinuousIds();
        String destManifestString = removeLines(sm.getSEDAXMLManifest(true, true), 2);

        // Then assert that
        // - there are only 2 ArchiveUnits in compacted version
        assertThat(compactedArchiveUnitNumber).isEqualTo(6);
        // - the manifest is the same except date
        assertThat(destManifestString).isEqualTo(originManifestString);
        // - a Management has been kept
        assertThat(destManifestString).contains("<Management>");
        // - there are the same number of BinaryDataObjects that in origin, and that all have the same size on disk
        assertThat(dataObjectPackage.getBdoInDataObjectPackageIdMap().size()).isEqualTo(originBinaryMap.size());
        for (Map.Entry<String, BinaryDataObject> e : dataObjectPackage.getBdoInDataObjectPackageIdMap().entrySet()) {
            File originBinary = e.getValue().getOnDiskPath().toFile();
            File newBinary = dataObjectPackage
                .getBdoInDataObjectPackageIdMap()
                .get(e.getKey())
                .getOnDiskPath()
                .toFile();
            assertThat(originBinary.length()).isEqualTo(newBinary.length());
        }
    }
}
