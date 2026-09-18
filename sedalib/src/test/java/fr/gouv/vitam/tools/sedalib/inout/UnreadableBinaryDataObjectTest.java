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
package fr.gouv.vitam.tools.sedalib.inout;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.TestUtilities;
import fr.gouv.vitam.tools.sedalib.UseTestFiles;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A BinaryDataObject carries its metadata on one side and the path of its binary file on the other,
 * and nothing used to guarantee that both agree. A SIP whose manifest declares a file absent from the
 * package was imported without a word, and the discrepancy only surfaced at export time, as a failure
 * on the first missing file naming nothing but the SIP being written. That is the "export incomplet,
 * binaires manquants alors que les métadonnées descriptives sont présentes" report.
 */
@ExtendWith(SedaContextExtension.class)
class UnreadableBinaryDataObjectTest implements UseTestFiles {

    private static SIPToArchiveTransferImporter importSampleSip(String tmpDir) throws Exception {
        TestUtilities.eraseAll(tmpDir);
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            "src/test/resources/PacketSamples/SIP_OK.zip",
            tmpDir,
            null
        );
        si.doImport();
        return si;
    }

    private static BinaryDataObject firstBinaryDataObject(SIPToArchiveTransferImporter si) {
        for (DataObjectGroup dog : si
            .getArchiveTransfer()
            .getDataObjectPackage()
            .getDogInDataObjectPackageIdMap()
            .values()) {
            if ((dog.getBinaryDataObjectList() != null) && !dog.getBinaryDataObjectList().isEmpty()) {
                return dog.getBinaryDataObjectList().get(0);
            }
        }
        throw new IllegalStateException("le SIP de test n'a aucun BinaryDataObject");
    }

    @Test
    void shouldReportNoUnreadableBinaryDataObjectOnACompleteSip() throws Exception {
        SIPToArchiveTransferImporter si = importSampleSip("target/tmpJunit/UnreadableBDO-complete");

        assertThat(
            si.getArchiveTransfer().getDataObjectPackage().getUnreadableBinaryDataObjectDescriptions()
        ).isEmpty();
    }

    /**
     * SIP_OK.zip declares its binaries as "Content/ID13.txt" while the importer extracts them into a
     * lowercase "content" directory. The on disk path was built from the raw Uri, so on a case
     * sensitive file system every binary of such a SIP was unreachable, with its metadata present.
     */
    @Test
    void shouldFindTheBinaryFilesWhenTheManifestUriIsCapitalized() throws Exception {
        SIPToArchiveTransferImporter si = importSampleSip("target/tmpJunit/UnreadableBDO-case");

        BinaryDataObject bdo = firstBinaryDataObject(si);
        assertThat(bdo.getOnDiskPath().getParent().getFileName().toString()).isEqualTo("content");
        assertThat(Files.exists(bdo.getOnDiskPath())).isTrue();
    }

    @Test
    void shouldReportTheBinaryDataObjectWhoseFileIsGone() throws Exception {
        SIPToArchiveTransferImporter si = importSampleSip("target/tmpJunit/UnreadableBDO-missing");
        BinaryDataObject bdo = firstBinaryDataObject(si);
        Files.delete(bdo.getOnDiskPath());

        assertThat(si.getArchiveTransfer().getDataObjectPackage().getUnreadableBinaryDataObjectDescriptions())
            .hasSize(1)
            .allSatisfy(
                description ->
                    assertThat(description).contains(bdo.getInDataObjectPackageId()).contains("qui n'existe pas")
            );
    }

    @Test
    void shouldRefuseTheSipExportAndNameEveryMissingFile() throws Exception {
        SIPToArchiveTransferImporter si = importSampleSip("target/tmpJunit/UnreadableBDO-export");
        BinaryDataObject bdo = firstBinaryDataObject(si);
        Files.delete(bdo.getOnDiskPath());
        Path exportedSip = Paths.get("target/tmpJunit/UnreadableBDO-export/exported.zip");
        Files.deleteIfExists(exportedSip);
        ArchiveTransferToSIPExporter exporter = new ArchiveTransferToSIPExporter(si.getArchiveTransfer(), null);

        assertThatThrownBy(() -> exporter.doExportToSEDASIP(exportedSip.toString(), true, true))
            .isInstanceOf(SEDALibException.class)
            .hasMessageContaining("Export du SIP impossible")
            .hasMessageContaining(bdo.getInDataObjectPackageId());

        // nothing has been written, rather than a truncated SIP silently missing its binaries
        assertThat(Files.exists(exportedSip)).isFalse();
    }

    @Test
    void shouldCountAndReportTheFilesIgnoredAtDiskImport() throws Exception {
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(
            "src/test/resources/PacketSamples/SampleWithTitleDirectoryNameModelV2",
            null
        );
        di.addIgnorePattern(".*\\.jpg");
        di.doImport();

        assertThat(di.getSummary()).contains("fichier(s) ignoré(s)");
    }
}
