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
package fr.gouv.vitam.tools.resip.service;

import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The imports of ReSIP, without its graphic interface.
 */
class ImportServiceTest {

    private static final String WORK_DIR = "target/tmpJunit/ImportService";
    private static final String RESIP_SAMPLES = "src/test/resources/PacketSamples/";
    // the bigger samples are sedalib's, not duplicated here
    private static final String SEDALIB_SAMPLES = "../sedalib/src/test/resources/PacketSamples/";

    private final List<String> resolvedSources = new ArrayList<>();

    private final ImportService importService = new ImportService(Preferences.getInstance(), (workDir, sourcePath) -> {
        resolvedSources.add(sourcePath);
        return ImportService.tmpDirOf(workDir, sourcePath);
    });

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void importsADirectoryInTheGivenVersion(SedaVersion sedaVersion) throws Exception {
        DiskImportContext context = new DiskImportContext(
            List.of("Thumbs.db"),
            false,
            RESIP_SAMPLES + "SampleWithoutLinksModelV2",
            WORK_DIR
        );

        ImportResult result = importService.doImport(context, sedaVersion, null, null);

        assertThat(result.getDataObjectPackage().getSedaVersion()).isEqualTo(sedaVersion);
        assertThat(result.getDataObjectPackage().getAuInDataObjectPackageIdMap()).hasSize(22);
        assertThat(result.getSummary()).isNotBlank();
        // the directory needs no temporary extraction
        assertThat(resolvedSources).isEmpty();
        assertThat(context.getModelVersion()).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void importsASipInTheVersionOfItsManifest(SedaVersion interfaceVersion) throws Exception {
        String sip = SEDALIB_SAMPLES + "TestSip.zip";

        ImportResult result = importService.doImport(new SIPImportContext(sip, WORK_DIR), interfaceVersion, null, null);

        assertThat(result.getDataObjectPackage().getSedaVersion()).isEqualTo(SedaVersion.V2_1);
        assertThat(result.getGlobalMetadata()).isNotNull();
        assertThat(resolvedSources).containsExactly(sip);
        assertThat(new File(ImportService.tmpDirOf(WORK_DIR, sip))).isDirectory();
    }

    @Test
    void importsADip() throws Exception {
        String dip = SEDALIB_SAMPLES + "TestDIP.zip";

        ImportResult result = importService.doImport(new DIPImportContext(dip, WORK_DIR), SedaVersion.V2_2, null, null);

        assertThat(result.getDataObjectPackage().getSedaVersion()).isEqualTo(SedaVersion.V2_1);
        assertThat(result.getDataObjectPackage().getAuInDataObjectPackageIdMap()).isNotEmpty();
        assertThat(resolvedSources).containsExactly(dip);
    }

    @Test
    void importsACompressedFileInTheGivenVersion() throws Exception {
        String zip = "../sedalib/src/test/resources/zip/TestImport.zip";

        ImportResult result = importService.doImport(
            new ZipImportContext(List.of(), zip, WORK_DIR),
            SedaVersion.V2_3,
            null,
            null
        );

        assertThat(result.getDataObjectPackage().getSedaVersion()).isEqualTo(SedaVersion.V2_3);
        assertThat(result.getDataObjectPackage().getAuInDataObjectPackageIdMap()).isNotEmpty();
        assertThat(resolvedSources).containsExactly(zip);
    }

    @Test
    void importsACsvTreeInTheGivenVersion() throws Exception {
        ImportResult result = importService.doImport(
            new CSVTreeImportContext("Cp1252", ';', SEDALIB_SAMPLES + "TestOK.csv", WORK_DIR),
            SedaVersion.V2_2,
            null,
            null
        );

        assertThat(result.getDataObjectPackage().getSedaVersion()).isEqualTo(SedaVersion.V2_2);
        assertThat(result.getDataObjectPackage().getAuInDataObjectPackageIdMap()).isNotEmpty();
        assertThat(result.getGlobalMetadata()).isNull();
    }

    @Test
    void importsACsvOfMetadataInTheGivenVersion() throws Exception {
        ImportResult result = importService.doImport(
            new CSVMetadataImportContext("windows-1252", ';', SEDALIB_SAMPLES + "MetadataTestOK1col.csv", WORK_DIR),
            SedaVersion.V2_2,
            null,
            null
        );

        assertThat(result.getDataObjectPackage().getSedaVersion()).isEqualTo(SedaVersion.V2_2);
        assertThat(result.getDataObjectPackage().getAuInDataObjectPackageIdMap()).isNotEmpty();
    }

    @Test
    void importsAWorkWithTheMetadataOfTheSipAndNormalised() throws Exception {
        SIPImportContext context = new SIPImportContext(SEDALIB_SAMPLES + "TestSip.zip", WORK_DIR);
        String messageIdentifier = importService
            .doImport(context, SedaVersion.V2_1, null, null)
            .getGlobalMetadata()
            .messageIdentifier;

        Work work = importService.importWork(context, SedaVersion.V2_1, null, null);

        assertThat(work.getCreationContext()).isSameAs(context);
        assertThat(work.getExportContext().getArchiveTransferGlobalMetadata().messageIdentifier).isEqualTo(
            messageIdentifier
        );
        assertThat(context.getSummary()).contains("normalisation Vitam effectuées");
    }

    @Test
    void stopsWhenTheTemporaryDirectoryIsRefused() {
        ImportService refusing = new ImportService(Preferences.getInstance(), (workDir, sourcePath) -> {
            throw new ResipException("Opération annulée");
        });

        assertThatThrownBy(
            () ->
                refusing.doImport(
                    new SIPImportContext(SEDALIB_SAMPLES + "TestSip.zip", WORK_DIR),
                    SedaVersion.V2_1,
                    null,
                    null
                )
        )
            .isInstanceOf(ResipException.class)
            .hasMessage("Opération annulée");
    }

    @Test
    void refusesAnUnknownKindOfImport() {
        assertThatThrownBy(() -> importService.doImport(new CreationContext(), SedaVersion.V2_1, null, null))
            .isInstanceOf(ResipException.class)
            .hasMessageContaining("Type d'import inconnu");
    }
}
