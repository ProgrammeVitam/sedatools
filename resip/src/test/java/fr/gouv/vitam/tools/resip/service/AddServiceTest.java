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
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The additions of ReSIP, without its graphic interface.
 */
class AddServiceTest {

    private static final Path SAMPLE = Paths.get("src/test/resources/PacketSamples/SampleWithoutLinksModelV2/Root");
    private static final Path ZIP = Paths.get("../sedalib/src/test/resources/zip/TestImport.zip");

    private final List<String> resolvedSubDirs = new ArrayList<>();

    private final AddService addService = new AddService(Preferences.getInstance(), (workDir, subDir) -> {
        resolvedSubDirs.add(subDir);
        return ImportService.USE_AS_IS.resolve(workDir, subDir);
    });

    private static List<ArchiveUnit> roots(Work work) {
        return work.getDataObjectPackage().getGhostRootAu().getChildrenAuList().getArchiveUnitList();
    }

    @ParameterizedTest
    @EnumSource(SedaVersion.class)
    void makesANewWorkOfDroppedFilesInTheGivenVersion(SedaVersion sedaVersion) throws Exception {
        Work work = addService.importNewWork(List.of(SAMPLE.resolve("Node 1")), sedaVersion, null);

        assertThat(work.getDataObjectPackage().getSedaVersion()).isEqualTo(sedaVersion);
        assertThat(roots(work)).hasSize(1);
        assertThat(work.getCreationContext()).isInstanceOf(DiskImportContext.class);
        assertThat(work.getCreationContext().getOnDiskInput()).isEqualTo(AddService.DRAG_AND_DROP);
        assertThat(work.getCreationContext().getSummary()).isNotBlank();
        assertThat(work.getExportContext()).isNotNull();
    }

    @Test
    void addsDroppedFilesUnderAnArchiveUnitInTheVersionOfTheWork() throws Exception {
        Work work = addService.importNewWork(List.of(SAMPLE.resolve("Node 1")), SedaVersion.V2_2, null);
        ArchiveUnit target = roots(work).get(0);
        int archiveUnitCount = work.getDataObjectPackage().getArchiveUnitCount();

        ImportResult imported = addService.importFiles(work, List.of(SAMPLE.resolve("Node 2")), null);
        int importedCount = imported.getDataObjectPackage().getArchiveUnitCount();
        List<ArchiveUnit> added = addService.addTo(work, target, imported.getDataObjectPackage());

        assertThat(imported.getDataObjectPackage().getSedaVersion()).isEqualTo(SedaVersion.V2_2);
        assertThat(added).hasSize(1);
        assertThat(target.getChildrenAuList().getArchiveUnitList()).containsAll(added);
        assertThat(work.getDataObjectPackage().getArchiveUnitCount()).isEqualTo(archiveUnitCount + importedCount);
        assertThat(added.get(0).getDataObjectPackage()).isSameAs(work.getDataObjectPackage());
        assertThat(work.getCreationContext().isStructureChanged()).isTrue();
    }

    @Test
    void expandsACompressedFileInPlaceOfIt() throws Exception {
        Work work = addService.importNewWork(List.of(ZIP), SedaVersion.V2_3, null);
        ArchiveUnit archiveUnit = roots(work).get(0);
        DataObjectGroup dog = (DataObjectGroup) archiveUnit.getDataObjectRefList().getDataObjectList().get(0);
        BinaryDataObject zip = dog.getBinaryDataObjectList().get(0);

        ImportResult imported = addService.expandCompressedFile(zip, null);
        List<ArchiveUnit> added = addService.replaceByExpansion(
            work,
            archiveUnit,
            zip,
            imported.getDataObjectPackage(),
            null
        );

        assertThat(resolvedSubDirs).containsExactly(AddService.tmpSubDirOf(zip));
        assertThat(imported.getSummary()).isNotBlank();
        assertThat(added).isNotEmpty();
        assertThat(archiveUnit.getChildrenAuList().getArchiveUnitList()).containsAll(added);
        // the zip was the only DataObject: it is removed and the ArchiveUnit becomes a RecordGrp
        assertThat(work.getDataObjectPackage().getBdoInDataObjectPackageIdMap()).doesNotContainValue(zip);
        assertThat(archiveUnit.getContent().getSimpleMetadata("DescriptionLevel")).isEqualTo("RecordGrp");
        assertThat(work.getDataObjectPackage().getSedaVersion()).isEqualTo(SedaVersion.V2_3);
    }
}
