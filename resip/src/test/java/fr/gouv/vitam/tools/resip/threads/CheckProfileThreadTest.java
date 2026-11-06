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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaContext;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersion;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class CheckProfileThreadTest {

    // samples shared with sedalib tests, the tests run in the resip module directory
    private static final String SEDALIB_SAMPLES = "../sedalib/src/test/resources/PacketSamples/";

    private static final String PROFILE = SEDALIB_SAMPLES + "profile.rng";

    private static final String SIP = SEDALIB_SAMPLES + "OK_468.zip";

    @TempDir
    Path tempDir;

    private SedaVersion previousVersion;

    @BeforeEach
    void setSedaVersion() {
        previousVersion = SedaContext.getVersion();
        SedaContext.setVersion(SedaVersion.V2_1);
    }

    @AfterEach
    void restoreSedaVersion() {
        SedaContext.setVersion(previousVersion);
    }

    /**
     * Bug #16844: on the same SIP and the same profile, a profile check reported anomalies and the
     * following checks reported none, after a manifest view with the metadata filter active.
     */
    @Test
    void shouldReportTheSameProfileAnomaliesAfterAFilteredManifestView() throws Exception {
        Work work = importWork();
        // a SEDA compliant metadata the profile doesn't allow in this ArchiveUnit
        ArchiveUnit archiveUnit = work.getDataObjectPackage().getArchiveUnitById("ID03");
        archiveUnit.setContentXmlData(
            archiveUnit
                .getContentXmlData()
                .replace(
                    "</Content>",
                    "<CustodialHistory><CustodialHistoryItem>Historique</CustodialHistoryItem></CustodialHistory></Content>"
                )
        );

        Throwable first = catchThrowable(() -> CheckProfileThread.validate(work, PROFILE, null));
        seeFilteredManifest(work, List.of("DescriptionLevel", "Title", "Description", "OriginatingAgency"));
        Throwable second = catchThrowable(() -> CheckProfileThread.validate(work, PROFILE, null));

        assertThat(first).isNotNull().hasMessageContaining("element \"CustodialHistory\" not allowed here");
        assertThat(second).as("the second profile check has to report anomalies").isNotNull();
        // the manifest view normalizes the Content of the ArchiveUnits, dropping the XML comments of
        // the imported manifest, so the anomalies are compared and not their line in the manifest
        assertThat(anomalies(second.getMessage()))
            .as("the second profile check has to report the same anomalies")
            .isEqualTo(anomalies(first.getMessage()));
    }

    private Work importWork() throws Exception {
        SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(
            SIP,
            tempDir.resolve("OK_468.zip-tmpdir").toString(),
            null
        );
        si.doImport();
        ArchiveTransfer archiveTransfer = si.getArchiveTransfer();
        ExportContext exportContext = new ExportContext();
        exportContext.setDefaultPrefs();
        exportContext.setArchiveTransferGlobalMetadata(archiveTransfer.getGlobalMetadata());
        exportContext.setManagementMetadataXmlData(
            archiveTransfer.getDataObjectPackage().getManagementMetadataXmlData()
        );
        return new Work(archiveTransfer.getDataObjectPackage(), null, exportContext);
    }

    // what SeeManifestThread does with the metadata filter active
    private static void seeFilteredManifest(Work work, List<String> keptMetadataList) throws Exception {
        work.getExportContext().setMetadataFilterFlag(true);
        work.getExportContext().setKeptMetadataList(keptMetadataList);
        work.getDataObjectPackage().setExportMetadataList(work.getExportContext().getKeptMetadataList());
        ArchiveTransfer archiveTransfer = new ArchiveTransfer();
        archiveTransfer.setDataObjectPackage(work.getDataObjectPackage());
        archiveTransfer.setGlobalMetadata(work.getExportContext().getArchiveTransferGlobalMetadata());
        new ArchiveTransferToSIPExporter(archiveTransfer, null).getSEDAXMLManifest(true, true);
    }

    // the summary line and the context and raw message of each anomaly, without its position
    private static List<String> anomalies(String validationMessage) {
        return validationMessage
            .lines()
            .filter(
                line ->
                    line.startsWith("Le flux XML") ||
                    line.startsWith("-> Contexte de l'erreur") ||
                    line.startsWith("erreur brute")
            )
            .toList();
    }
}
