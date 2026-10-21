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

import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.OBJECTS_GROUP;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.STEP;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * Cleans the work directory, where ReSIP extracts the files it imports, expands and compacts.
 */
public final class WorkDirService {

    private WorkDirService() {}

    /**
     * Deletes a file or a directory with all its content.
     *
     * @param file the file or directory
     * @param spl  the progress logger, or null
     * @return the number of deleted files, directories excluded
     * @throws InterruptedException if the deletion is interrupted
     */
    public static int deleteRecursively(File file, SEDALibProgressLogger spl) throws InterruptedException {
        return deleteRecursively(file, spl, 0);
    }

    private static int deleteRecursively(File file, SEDALibProgressLogger spl, int deletedCount)
        throws InterruptedException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deletedCount = deleteRecursively(
                child,
                spl,
                deletedCount
            );
            file.delete(); //NOSONAR use the quickest method
        } else {
            file.delete(); //NOSONAR use the quickest method
            deletedCount++;
            doProgressLogIfStep(spl, OBJECTS_GROUP, deletedCount, deletedCount + " fichiers effacés");
        }
        return deletedCount;
    }

    /**
     * Finds the temporary directories of the work directory which can be imported again, all but
     * the ones of compacting and decompacting operations (see
     * {@link CompactService#tmpSubDirOf}).
     *
     * @param workDir the work directory
     * @return the temporary directories, the most recently modified first
     */
    public static List<File> findReimportableTmpDirs(String workDir) {
        List<File> tmpDirs = new ArrayList<>();
        File[] files = new File(workDir).listFiles();
        if (files != null) for (File f : files) {
            String name = f.getName();
            if (
                f.isDirectory() &&
                name.endsWith("-tmpdir") &&
                !name.startsWith("Compact-") &&
                !name.startsWith("DeCompact-")
            ) tmpDirs.add(f);
        }
        tmpDirs.sort(Comparator.comparingLong(File::lastModified).reversed());
        return tmpDirs;
    }

    private static boolean isTmpDir(File file) {
        return file.isDirectory() && file.getName().endsWith("-tmpdir");
    }

    /**
     * Finds the sessions of the work directory a cleaning breaks: the ones saved in a temporary
     * directory, and the ones using files of a temporary directory, whose binaries would be lost.
     *
     * @param workDir the work directory
     * @return the sessions, the most recently modified first
     */
    public static List<File> findSessionsUsingTmpDirs(String workDir) {
        List<Path> tmpDirs = new ArrayList<>();
        File[] files = new File(workDir).listFiles();
        if (files != null) for (File f : files) if (isTmpDir(f)) tmpDirs.add(normalized(f.getPath()));
        List<File> sessions = new ArrayList<>();
        if (tmpDirs.isEmpty()) return sessions;
        for (File session : SessionService.findSessions(workDir)) {
            if (isInOneOf(normalized(session.getPath()), tmpDirs)) sessions.add(session);
            else try {
                for (String onDiskPath : SessionService.readOnDiskPaths(session.getPath())) {
                    Path path = normalized(onDiskPath);
                    if ((path != null) && isInOneOf(path, tmpDirs)) {
                        sessions.add(session);
                        break;
                    }
                }
            } catch (ResipException e) {
                // a session which can't be read isn't broken by the cleaning
            }
        }
        return sessions;
    }

    private static Path normalized(String path) {
        try {
            return Paths.get(path).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            return null;
        }
    }

    private static boolean isInOneOf(Path path, List<Path> dirs) {
        for (Path dir : dirs) if (path.startsWith(dir)) return true;
        return false;
    }

    /**
     * Deletes the temporary directories of the work directory, the ones named "*-tmpdir".
     *
     * @param workDir the work directory
     * @param spl     the progress logger, or null
     * @return the number of deleted files
     * @throws InterruptedException if the cleaning is interrupted
     */
    public static int cleanTmpDirs(String workDir, SEDALibProgressLogger spl) throws InterruptedException {
        doProgressLog(spl, GLOBAL, "Nettoyage du répertoire: " + workDir, null);
        int total = 0;
        File[] files = new File(workDir).listFiles();
        if (files != null) for (File f : files) {
            if (isTmpDir(f)) {
                doProgressLog(spl, STEP, "  - Sous-répertoire pris en compte: " + f, null);
                int deleted = deleteRecursively(f, spl);
                doProgressLog(spl, STEP, "    Terminé, " + deleted + " effacés", null);
                total += deleted;
            }
        }
        return total;
    }
}
