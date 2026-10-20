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
import fr.gouv.vitam.tools.resip.parameters.CompactContext;
import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.process.Compactor;
import fr.gouv.vitam.tools.sedalib.process.DeCompactor;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * Compacts an ArchiveUnit tree of a work into a few ArchiveUnits holding the files and their
 * metadata, and decompacts it back, without any user interface.
 */
public class CompactService {

    /**
     * The ArchiveUnit replacing the one compacted or decompacted, and the summary of the operation.
     */
    public static final class Result {

        private final ArchiveUnit archiveUnit;
        private final String summary;

        private Result(ArchiveUnit archiveUnit, String summary) {
            this.archiveUnit = archiveUnit;
            this.summary = summary;
        }

        public ArchiveUnit getArchiveUnit() {
            return archiveUnit;
        }

        public String getSummary() {
            return summary;
        }
    }

    private final Preferences preferences;
    private final TmpDirResolver tmpDirResolver;

    /**
     * Instantiates a new compact service.
     *
     * @param preferences    the preferences, giving the compaction options and the work directory
     * @param tmpDirResolver the choice of the temporary directory of the compacted files
     */
    public CompactService(Preferences preferences, TmpDirResolver tmpDirResolver) {
        this.preferences = preferences;
        this.tmpDirResolver = tmpDirResolver;
    }

    /**
     * @param operation "Compact" or "DeCompact"
     * @param target    the compacted or decompacted ArchiveUnit
     * @return the subdirectory of the work directory the files are put in
     */
    public static String tmpSubDirOf(String operation, ArchiveUnit target) {
        return operation + "-" + target.getInDataObjectPackageId() + "-tmpdir";
    }

    /**
     * Reads the content metadata filter of the compaction options: each kept metadata is a name,
     * possibly followed by ":" and the maximum length of its text, 0 for no limit.
     *
     * @param keptMetadataList the kept metadata of the compaction options
     * @return the maximum text length by kept metadata name
     */
    public static Map<String, Integer> contentMetadataFilter(List<String> keptMetadataList) {
        Map<String, Integer> contentMetadataFilter = new HashMap<>();
        for (String m : keptMetadataList) {
            if (m.trim().isEmpty()) continue;
            if (!m.contains(":")) {
                contentMetadataFilter.put(m.trim(), 0);
                continue;
            }
            int maxLength = 0;
            try {
                maxLength = Integer.parseInt(m.substring(m.indexOf(":") + 1));
            } catch (NumberFormatException ignored) {
                // no limit
            }
            contentMetadataFilter.put(m.substring(0, m.indexOf(":")).trim(), Math.max(maxLength, 0));
        }
        return contentMetadataFilter;
    }

    /**
     * Compacts an ArchiveUnit of a work, with the compaction options of the preferences.
     *
     * @param work   the work
     * @param target the ArchiveUnit to compact
     * @param spl    the progress logger, or null
     * @return the compacted ArchiveUnit, to be put in place of the target
     * @throws ResipException       if the compaction is abandoned
     * @throws SEDALibException     if the compaction fails
     * @throws InterruptedException if the compaction is interrupted
     */
    public Result compact(Work work, ArchiveUnit target, SEDALibProgressLogger spl)
        throws ResipException, SEDALibException, InterruptedException {
        logOperation("Compactage", target, spl);
        CompactContext coc = new CompactContext(preferences);
        String tmpDir = tmpDirResolver.resolve(coc.getWorkDir(), tmpSubDirOf("Compact", target));
        Compactor compactor = new Compactor(target, tmpDir, spl);
        compactor.setCompactedDocumentPackLimit(coc.getMaxMetadataSize(), coc.getMaxDocumentNumber());
        compactor.setObjectVersionFilters(
            coc.getDocumentKeptDataObjectVersionList(),
            coc.getSubDocumentKeptDataObjectVersionList()
        );
        if (!coc.isMetadataFilterFlag()) compactor.setMetadataFilters(null, null);
        else {
            Map<String, Integer> contentMetadataFilter = contentMetadataFilter(coc.getKeptMetadataList());
            compactor.setMetadataFilters(contentMetadataFilter, contentMetadataFilter);
        }
        compactor.setDeflatedFlag(coc.isDeflatedFlag());
        ArchiveUnit compacted = compactor.doCompact();
        work.getCreationContext().setStructureChanged(true);
        return new Result(compacted, compactor.getSummary());
    }

    /**
     * Decompacts a compacted ArchiveUnit of a work.
     *
     * @param work   the work
     * @param target the ArchiveUnit to decompact
     * @param spl    the progress logger, or null
     * @return the decompacted ArchiveUnit, to be put in place of the target
     * @throws ResipException       if the decompaction is abandoned
     * @throws SEDALibException     if the decompaction fails
     * @throws InterruptedException if the decompaction is interrupted
     */
    public Result decompact(Work work, ArchiveUnit target, SEDALibProgressLogger spl)
        throws ResipException, SEDALibException, InterruptedException {
        logOperation("Décompactage", target, spl);
        String tmpDir = tmpDirResolver.resolve(
            new CreationContext(preferences).getWorkDir(),
            tmpSubDirOf("DeCompact", target)
        );
        DeCompactor decompactor = new DeCompactor(target, tmpDir, spl);
        ArchiveUnit decompacted = decompactor.doDeCompact();
        work.getCreationContext().setStructureChanged(true);
        return new Result(decompacted, decompactor.getSummary());
    }

    private static void logOperation(String operation, ArchiveUnit target, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException {
        doProgressLog(
            spl,
            GLOBAL,
            operation +
            " de l'ArchiveUnit [" +
            target.getInDataObjectPackageId() +
            "]=" +
            target.getContent().getSimpleMetadata("Title"),
            null
        );
    }
}
