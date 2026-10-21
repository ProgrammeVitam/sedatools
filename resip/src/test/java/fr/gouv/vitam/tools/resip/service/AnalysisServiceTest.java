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

import fr.gouv.vitam.tools.resip.data.StatisticData;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.service.AnalysisService.SearchCriteria;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The analyses of the tree of a work, without the graphic interface of ReSIP.
 */
class AnalysisServiceTest {

    private static final String SAMPLE = "src/test/resources/PacketSamples/SampleWithoutLinksModelV2";

    private static final Function<ArchiveUnit, String> TITLE = au -> {
        try {
            return au.getContent().getSimpleMetadata("Title");
        } catch (SEDALibException e) {
            throw new IllegalStateException(e);
        }
    };

    private static Work sampleWork() throws Exception {
        DiskImportContext context = new DiskImportContext(List.of("Thumbs.db"), false, SAMPLE, "target/tmpJunit");
        Work work = new ImportService(Preferences.getInstance(), ImportService.USE_AS_IS).importWork(
            context,
            SedaVersion.V2_1,
            null,
            null
        );
        work.setExportContext(new ExportContext("src/test/resources/PacketSamples/ExportContext.config"));
        return work;
    }

    private static ArchiveUnit ghostRoot(Work work) {
        return work.getDataObjectPackage().getGhostRootAu();
    }

    private static SearchCriteria text(String text, boolean regExp, boolean caseSensitive) {
        return new SearchCriteria(text, false, false, false, false, regExp, caseSensitive);
    }

    @Test
    void searchesTheTitlesWithOrWithoutTheCase() throws Exception {
        Work work = sampleWork();
        ArchiveUnit target = work
            .getDataObjectPackage()
            .getAuInDataObjectPackageIdMap()
            .values()
            .stream()
            .filter(au -> !TITLE.apply(au).equals(TITLE.apply(au).toLowerCase()))
            .findFirst()
            .orElseThrow();
        String lowerCaseTitle = TITLE.apply(target).toLowerCase();

        List<ArchiveUnit> found = AnalysisService.search(ghostRoot(work), text(lowerCaseTitle, false, false), TITLE);

        assertThat(found)
            .contains(target)
            .allSatisfy(au -> assertThat(TITLE.apply(au).toLowerCase()).contains(lowerCaseTitle));
        assertThat(AnalysisService.search(ghostRoot(work), text(lowerCaseTitle, false, true), TITLE)).doesNotContain(
            target
        );
    }

    @Test
    void searchesTheTitlesWithARegularExpression() throws Exception {
        Work work = sampleWork();

        List<ArchiveUnit> found = AnalysisService.search(ghostRoot(work), text("Node \\d\\.\\d", true, false), TITLE);

        assertThat(found).isNotEmpty().allSatisfy(au -> assertThat(TITLE.apply(au)).containsPattern("Node \\d\\.\\d"));
    }

    @Test
    void searchesAnIdentifierExactly() throws Exception {
        Work work = sampleWork();
        ArchiveUnit root = ghostRoot(work).getChildrenAuList().getArchiveUnitList().get(0);

        List<ArchiveUnit> found = AnalysisService.search(
            ghostRoot(work),
            new SearchCriteria(root.getInDataObjectPackageId(), false, false, true, false, false, false),
            TITLE
        );

        assertThat(found).containsExactly(root);
    }

    @Test
    void searchesTheArchiveUnitsWithoutChildOnly() throws Exception {
        Work work = sampleWork();

        List<ArchiveUnit> found = AnalysisService.search(
            ghostRoot(work),
            new SearchCriteria("", true, false, false, false, false, false),
            TITLE
        );

        assertThat(found).isNotEmpty().allSatisfy(au -> assertThat(au.getChildrenAuList().getCount()).isZero());
    }

    @Test
    void searchesTheBinaryDataObjectsByFormatAndSize() throws Exception {
        Work work = sampleWork();
        int bdoCount = work.getDataObjectPackage().getBdoInDataObjectPackageIdMap().size();

        LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> all = AnalysisService.technicalSearch(
            ghostRoot(work),
            List.of(),
            0,
            Long.MAX_VALUE,
            Map.of()
        );
        LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> others = AnalysisService.technicalSearch(
            ghostRoot(work),
            List.of(AnalysisService.OTHER_FORMATS),
            0,
            Long.MAX_VALUE,
            Map.of()
        );

        assertThat(all.values().stream().mapToInt(List::size).sum()).isEqualTo(bdoCount);
        // with no category, every identified format is an other one
        assertThat(others).isNotEmpty();
        // no BinaryDataObject is at most 0 byte
        assertThat(AnalysisService.technicalSearch(ghostRoot(work), List.of(), 0, 0, Map.of())).isEmpty();
    }

    @Test
    void findsTheEndDatesBeforeTheStartDates() throws Exception {
        Work work = sampleWork();
        ArchiveUnit wrong = ghostRoot(work).getChildrenAuList().getArchiveUnitList().get(0);
        wrong.getContent().addNewMetadata("StartDate", "2020-01-02");
        wrong.getContent().addNewMetadata("EndDate", "2020-01-01");

        assertThat(AnalysisService.findEndDatesBeforeStartDates(work, null)).containsExactly(wrong);
    }

    @Test
    void computesTheStatisticsByCategory() throws Exception {
        Work work = sampleWork();
        LinkedHashMap<String, List<String>> categories = new LinkedHashMap<>();
        categories.put("Texte", List.of("x-fmt/111", "fmt/101"));
        categories.put("Autres", List.of(AnalysisService.OTHER_FORMATS));

        List<StatisticData> statistics = AnalysisService.statistics(work.getDataObjectPackage(), categories, null);

        assertThat(statistics)
            .extracting(StatisticData::getFormatCategory)
            .containsExactly("Texte", "Autres", AnalysisService.ALL_FORMATS);
        // every BinaryDataObject is in a category, and all of them in all formats
        assertThat(statistics.get(0).getObjectNumber() + statistics.get(1).getObjectNumber()).isEqualTo(
            statistics.get(2).getObjectNumber()
        );
        assertThat(statistics.get(2).getObjectNumber()).isEqualTo(
            work.getDataObjectPackage().getBdoInDataObjectPackageIdMap().size()
        );
    }

    @Test
    void findsTheDuplicatedDataObjectGroups() throws Exception {
        Work work = sampleWork();
        AddService addService = new AddService(Preferences.getInstance(), ImportService.USE_AS_IS);
        ArchiveUnit root = ghostRoot(work).getChildrenAuList().getArchiveUnitList().get(0);
        // the same files once more
        ImportResult again = addService.importFiles(work, List.of(Paths.get(SAMPLE, "Root")), null);
        addService.addTo(work, root, again.getDataObjectPackage());

        AnalysisService.DuplicatesResult duplicates = AnalysisService.duplicates(
            work.getDataObjectPackage(),
            true,
            true,
            false,
            null
        );

        assertThat(duplicates.getDataObjectGroupsByKey()).isNotEmpty();
        assertThat(duplicates.getArchiveUnitsByKey().keySet()).isEqualTo(
            duplicates.getDataObjectGroupsByKey().keySet()
        );
        assertThat(duplicates.getArchiveUnitsByKey().values()).allSatisfy(aus -> assertThat(aus).hasSizeGreaterThan(1));
    }
}
