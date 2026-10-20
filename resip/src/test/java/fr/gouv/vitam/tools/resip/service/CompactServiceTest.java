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
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The compaction of ArchiveUnits, without the graphic interface of ReSIP.
 */
class CompactServiceTest {

    private final List<String> resolvedSubDirs = new ArrayList<>();

    private final CompactService compactService = new CompactService(Preferences.getInstance(), (workDir, subDir) -> {
        resolvedSubDirs.add(subDir);
        return ImportService.USE_AS_IS.resolve(workDir, subDir);
    });

    @Test
    void readsTheKeptMetadataWithTheirMaximumLength() {
        assertThat(
            CompactService.contentMetadataFilter(List.of("Title", " Description:200", " ", "Bad:x", "Negative:-5"))
        ).isEqualTo(Map.of("Title", 0, "Description", 200, "Bad", 0, "Negative", 0));
    }

    @Test
    void compactsAnArchiveUnitAndDecompactsIt() throws Exception {
        Work work = new ImportService(Preferences.getInstance(), ImportService.USE_AS_IS).importWork(
            new DiskImportContext(
                List.of("Thumbs.db"),
                false,
                "src/test/resources/PacketSamples/SampleWithoutLinksModelV2",
                "target/tmpJunit/CompactService"
            ),
            SedaVersion.V2_3,
            null,
            null
        );
        ArchiveUnit root = work.getDataObjectPackage().getGhostRootAu().getChildrenAuList().getArchiveUnitList().get(0);

        CompactService.Result compacted = compactService.compact(work, root, null);

        assertThat(compacted.getArchiveUnit()).isNotNull().isNotSameAs(root);
        assertThat(compacted.getSummary()).isNotBlank();
        assertThat(work.getCreationContext().isStructureChanged()).isTrue();

        CompactService.Result decompacted = compactService.decompact(work, compacted.getArchiveUnit(), null);

        assertThat(decompacted.getArchiveUnit()).isNotNull();
        assertThat(decompacted.getSummary()).isNotBlank();
        assertThat(resolvedSubDirs).containsExactly(
            CompactService.tmpSubDirOf("Compact", root),
            CompactService.tmpSubDirOf("DeCompact", compacted.getArchiveUnit())
        );
    }
}
