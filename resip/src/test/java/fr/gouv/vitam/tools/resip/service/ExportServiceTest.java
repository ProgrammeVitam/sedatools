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
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.service.ExportService.ExportType;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The exports and validations of ReSIP, without its graphic interface.
 */
class ExportServiceTest {

    private static final String OUTPUT_DIR = "target/tmpJunit/ExportService";
    private static final String SAMPLES = "src/test/resources/PacketSamples/";

    private final ExportService exportService = new ExportService(Preferences.getInstance());

    // a work imported as ReSIP does, with the export context of the samples
    private static Work workIn(SedaVersion sedaVersion) throws Exception {
        DiskImportContext context = new DiskImportContext(
            List.of("Thumbs.db"),
            false,
            SAMPLES + "SampleWithoutLinksModelV2",
            OUTPUT_DIR
        );
        Work work = new ImportService(Preferences.getInstance(), ImportService.USE_AS_IS).importWork(
            context,
            sedaVersion,
            null,
            null
        );
        work.setExportContext(new ExportContext(SAMPLES + "ExportContext.config"));
        return work;
    }

    private static String output(String name) {
        new File(OUTPUT_DIR).mkdirs();
        return OUTPUT_DIR + File.separator + name;
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void exportsTheManifestInTheVersionOfTheWork(SedaVersion sedaVersion) throws Exception {
        Work work = workIn(sedaVersion);
        String manifestFile = output("manifest-" + sedaVersion + ".xml");
        work.getExportContext().setOnDiskOutput(manifestFile);

        exportService.export(work, ExportType.SIP_MANIFEST, null);

        String manifest = Files.readString(Paths.get(manifestFile), StandardCharsets.UTF_8);
        assertThat(manifest).contains("xmlns=\"" + sedaVersion.getNamespace() + "\"");
        // the global and management metadata come from the export context
        assertThat(manifest).contains("SIP SEDA de test").contains("Service_producteur");
    }

    @ParameterizedTest
    @EnumSource(ExportType.class)
    void exportsInEveryKind(ExportType exportType) throws Exception {
        Work work = workIn(SedaVersion.V2_1);
        String target = output("export-" + exportType);
        work.getExportContext().setOnDiskOutput(target);

        String summary = exportService.export(work, exportType, null);

        assertThat(summary).isNotBlank();
        assertThat(new File(target)).exists();
        if ((exportType == ExportType.DISK) || (exportType == ExportType.CSV_ALL_DISK)) assertThat(
            new File(target)
        ).isDirectory();
        else assertThat(new File(target)).isFile();
    }

    @Test
    void generatesTheManifestWithTheKeptMetadataOnly() throws Exception {
        Work work = workIn(SedaVersion.V2_1);
        assertThat(exportService.getManifest(work, null)).contains("<Writer>");

        work.getExportContext().setMetadataFilterFlag(true);
        work.getExportContext().setKeptMetadataList(List.of("DescriptionLevel", "Title"));
        String filtered = exportService.getManifest(work, null);

        assertThat(filtered).contains("<Title>").doesNotContain("<Writer>");
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void validatesTheWholeWorkAgainstTheXsdOfItsVersion(SedaVersion sedaVersion) throws Exception {
        Work work = workIn(sedaVersion);
        work.getExportContext().setMetadataFilterFlag(true);
        work.getExportContext().setKeptMetadataList(List.of("DescriptionLevel", "Title"));

        exportService.validate(work, null, null);

        // the filter is an export choice, not applied to the validation
        assertThat(work.getDataObjectPackage().getExportMetadataList()).isNull();
    }
}
