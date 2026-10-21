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
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.seda.SedaVersionConverter;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The ReSIP session files, without the graphic interface.
 */
class SessionServiceTest {

    private static Work sampleWork(SedaVersion sedaVersion) throws Exception {
        Work work = new ImportService(Preferences.getInstance(), ImportService.USE_AS_IS).importWork(
            new DiskImportContext(
                List.of("Thumbs.db"),
                false,
                "src/test/resources/PacketSamples/SampleWithoutLinksModelV2",
                "target/tmpJunit"
            ),
            sedaVersion,
            null,
            null
        );
        work.setExportContext(new ExportContext("src/test/resources/PacketSamples/ExportContext.config"));
        return work;
    }

    @Test
    void savesAndLoadsBackAWork(@TempDir Path dir) throws Exception {
        Work work = sampleWork(SedaVersion.V2_2);
        String file = dir.resolve("session.rsp").toString();

        SessionService.save(work, file);
        Work loaded = SessionService.load(file);

        assertThat(SessionService.readSedaVersion(file)).isEqualTo(SedaVersion.V2_2);
        assertThat(loaded.getVersion()).isEqualTo(SedaVersion.V2_2);
        DataObjectPackage loadedPackage = loaded.getDataObjectPackage();
        assertThat(loadedPackage.getSedaVersion()).isEqualTo(SedaVersion.V2_2);
        assertThat(loadedPackage.getArchiveUnitCount()).isEqualTo(work.getDataObjectPackage().getArchiveUnitCount());
        assertThat(loaded.getExportContext().getArchiveTransferGlobalMetadata().messageIdentifier).isEqualTo(
            "SIP SEDA de test"
        );
        // the references the json doesn't hold are restored
        assertThat(loadedPackage.getGhostRootAu().getDataObjectPackage()).isSameAs(loadedPackage);
        for (ArchiveUnit au : loadedPackage.getAuInDataObjectPackageIdMap().values()) assertThat(
            au.getDataObjectPackage()
        ).isSameAs(loadedPackage);
        for (DataObjectGroup dog : loadedPackage.getDogInDataObjectPackageIdMap().values()) {
            assertThat(dog.getDataObjectPackage()).isSameAs(loadedPackage);
            assertThat(dog.getBinaryDataObjectList()).allSatisfy(bdo -> {
                assertThat(bdo.getDataObjectPackage()).isSameAs(loadedPackage);
                assertThat(bdo.getDataObjectGroup()).isSameAs(dog);
            });
        }
    }

    @Test
    void failsWhenTheSessionCantBeWritten(@TempDir Path dir) throws Exception {
        Work work = sampleWork(SedaVersion.V2_1);

        // a directory can't be written as a file
        assertThatThrownBy(() -> SessionService.save(work, dir.toString()))
            .isInstanceOf(ResipException.class)
            .hasMessageContaining("Impossible de sauvegarder la session");
    }

    @Test
    void refusesAFileWhichIsNotASession(@TempDir Path dir) throws Exception {
        String file = dir.resolve("other.zip").toString();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            zos.putNextEntry(new ZipEntry("other.txt"));
            zos.write("other".getBytes());
            zos.closeEntry();
        }

        assertThatThrownBy(() -> SessionService.load(file))
            .isInstanceOf(ResipException.class)
            .hasMessageContaining("n'est pas une sauvegarde de session Resip");
        assertThatThrownBy(() -> SessionService.readSedaVersion(dir.resolve("missing.rsp").toString()))
            .isInstanceOf(ResipException.class)
            .hasMessageContaining("ne permet pas de retrouver une session Resip");
    }

    @Test
    void findsTheSessionsOfADirectoryAndOfItsSubdirectories(@TempDir Path dir) throws Exception {
        Work work = sampleWork(SedaVersion.V2_1);
        File older = dir.resolve("older.rsp").toFile();
        File newer = dir.resolve("sample-tmpdir").resolve("newer.zip").toFile();
        newer.getParentFile().mkdirs();
        SessionService.save(work, older.toString());
        SessionService.save(work, newer.toString());
        older.setLastModified(newer.lastModified() - 10_000);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dir.resolve("other.zip").toFile()))) {
            zos.putNextEntry(new ZipEntry("other.txt"));
            zos.closeEntry();
        }
        dir.resolve("preferences.properties").toFile().createNewFile();

        assertThat(SessionService.findSessions(dir.toString())).containsExactly(newer, older);
        assertThat(SessionService.findSessions(dir.resolve("missing").toString())).isEmpty();
    }

    @Test
    void givesAWorkTheVersionOfItsPackage() throws Exception {
        Work work = sampleWork(SedaVersion.V2_1);

        work.setDataObjectPackage(
            new SedaVersionConverter(null).convert(work.getDataObjectPackage(), SedaVersion.V2_3)
        );

        assertThat(work.getVersion()).isEqualTo(SedaVersion.V2_3);
    }
}
