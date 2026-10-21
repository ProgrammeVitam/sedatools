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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The cleaning of the work directory.
 */
class WorkDirServiceTest {

    @Test
    void deletesOnlyTheTemporaryDirectories(@TempDir Path workDir) throws Exception {
        Files.createDirectories(workDir.resolve("SIP.zip-tmpdir/content"));
        Files.writeString(workDir.resolve("SIP.zip-tmpdir/manifest.xml"), "m");
        Files.writeString(workDir.resolve("SIP.zip-tmpdir/content/file.txt"), "f");
        Files.createDirectories(workDir.resolve("kept"));
        Files.writeString(workDir.resolve("kept/file.txt"), "k");
        // a file named as a temporary directory is not one
        Files.writeString(workDir.resolve("file-tmpdir"), "t");

        int deleted = WorkDirService.cleanTmpDirs(workDir.toString(), null);

        assertThat(deleted).isEqualTo(2);
        assertThat(workDir.resolve("SIP.zip-tmpdir")).doesNotExist();
        assertThat(workDir.resolve("kept/file.txt")).exists();
        assertThat(workDir.resolve("file-tmpdir")).exists();
    }

    @Test
    void findsTheTemporaryDirectoriesWhichCanBeImportedAgain(@TempDir Path workDir) throws Exception {
        File older = Files.createDirectory(workDir.resolve("Sip.zip-tmpdir")).toFile();
        File newer = Files.createDirectory(workDir.resolve("Mail.pst-tmpdir")).toFile();
        older.setLastModified(newer.lastModified() - 10_000);
        Files.createDirectory(workDir.resolve("Compact-ID10-tmpdir"));
        Files.createDirectory(workDir.resolve("DeCompact-ID58-tmpdir"));
        Files.createDirectory(workDir.resolve("other"));
        Files.createFile(workDir.resolve("file-tmpdir"));

        assertThat(WorkDirService.findReimportableTmpDirs(workDir.toString())).containsExactly(newer, older);
    }

    @Test
    void deletesAFileOrADirectoryRecursively(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir.resolve("a/b"));
        Files.writeString(dir.resolve("a/b/c.txt"), "c");
        Files.writeString(dir.resolve("a/d.txt"), "d");

        assertThat(WorkDirService.deleteRecursively(new File(dir.toFile(), "a"), null)).isEqualTo(2);
        assertThat(dir.resolve("a")).doesNotExist();
    }
}
