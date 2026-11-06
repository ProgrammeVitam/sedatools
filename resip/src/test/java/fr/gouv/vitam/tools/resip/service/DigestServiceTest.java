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
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The computation and the check of the digests of a work, without the graphic interface.
 */
class DigestServiceTest {

    @TempDir
    Path tempDir;

    private Work work;

    private final AddService addService = new AddService(Preferences.getInstance(), ImportService.USE_AS_IS);

    @BeforeEach
    void buildWork() throws Exception {
        // Given a work of two root ArchiveUnits, of one file each, built in SHA-512
        Path root = tempDir.resolve("root");
        Files.createDirectories(root.resolve("first"));
        Files.createDirectories(root.resolve("second"));
        Files.writeString(root.resolve("first").resolve("a.txt"), "first content");
        Files.writeString(root.resolve("second").resolve("b.txt"), "second content");
        work = addService.importNewWork(List.of(root.resolve("first"), root.resolve("second")), SedaVersion.V2_1, null);
    }

    private DataObjectPackage dataObjectPackage() {
        return work.getDataObjectPackage();
    }

    private ArchiveUnit root(String title) {
        for (ArchiveUnit au : dataObjectPackage().getGhostRootAu().getChildrenAuList().getArchiveUnitList()) {
            if (DigestService.binaryDataObjectsUnder(au).iterator().next().getOnDiskPath().toString().contains(title)) {
                return au;
            }
        }
        throw new IllegalStateException("no root " + title);
    }

    private BinaryDataObject bdoUnder(String title) {
        return DigestService.binaryDataObjectsUnder(root(title)).iterator().next();
    }

    private static String digestOf(String content, String algorithm) throws Exception {
        return HexFormat.of()
            .formatHex(MessageDigest.getInstance(algorithm).digest(content.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void shouldComputeTheDigestsOfTheWholePackageInAGivenAlgorithm() throws Exception {
        // When
        DigestService.DigestResult result = DigestService.computeDigests(dataObjectPackage(), null, "SHA-256", null);

        // Then
        assertThat(result.getComputedCount()).isEqualTo(2);
        assertThat(result.getWithoutFileIds()).isEmpty();
        assertThat(result.getFailures()).isEmpty();
        assertThat(bdoUnder("a.txt").getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-256");
        assertThat(bdoUnder("a.txt").getMetadataMessageDigest().getValue()).isEqualTo(
            digestOf("first content", "SHA-256")
        );
        assertThat(bdoUnder("b.txt").getMetadataMessageDigest().getValue()).isEqualTo(
            digestOf("second content", "SHA-256")
        );
        // the work now computes its digests in this algorithm
        assertThat(dataObjectPackage().getDigestAlgorithm()).isEqualTo("SHA-256");
    }

    @Test
    void shouldComputeTheDigestsInTheAlgorithmOfThePackageByDefault() throws Exception {
        // When
        DigestService.DigestResult result = DigestService.computeDigests(dataObjectPackage(), null, null, null);

        // Then
        assertThat(result.getAlgorithm()).isEqualTo("SHA-512");
        assertThat(bdoUnder("a.txt").getMetadataMessageDigest().getValue()).isEqualTo(
            digestOf("first content", "SHA-512")
        );
    }

    @Test
    void shouldComputeOnlyTheDigestsOfASubTree() throws Exception {
        // When
        DigestService.DigestResult result = DigestService.computeDigests(
            dataObjectPackage(),
            root("a.txt"),
            "SHA-256",
            null
        );

        // Then
        assertThat(result.getComputedCount()).isEqualTo(1);
        assertThat(bdoUnder("a.txt").getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-256");
        assertThat(bdoUnder("b.txt").getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-512");
        // a sub-tree doesn't change the algorithm of the work
        assertThat(dataObjectPackage().getDigestAlgorithm()).isEqualTo("SHA-512");
    }

    @Test
    void shouldComputeOnlyTheMessageDigest() throws Exception {
        // Given technical metadata corrected by hand
        BinaryDataObject bdo = bdoUnder("a.txt");
        bdo.addMetadata(new IntegerType("Size", 42));
        bdo.addMetadata(new FormatIdentification(SedaVersion.V2_1, "Hand made", "text/plain", "x-fmt/111", null));
        String fileInfoBefore = String.valueOf(bdo.getMetadataFileInfo());

        // When
        DigestService.computeDigests(dataObjectPackage(), null, "SHA-384", null);

        // Then
        assertThat(bdo.getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-384");
        assertThat(bdo.getMetadataSize().getValue()).isEqualTo(42L);
        assertThat(bdo.getMetadataFormatIdentification().getSimpleMetadata("FormatLitteral")).isEqualTo("Hand made");
        assertThat(String.valueOf(bdo.getMetadataFileInfo())).isEqualTo(fileInfoBefore);
    }

    @Test
    void shouldSkipAndReportABinaryDataObjectWithoutFile() throws Exception {
        // Given
        BinaryDataObject withoutFile = bdoUnder("b.txt");
        Files.delete(withoutFile.getOnDiskPath());
        String digestBefore = withoutFile.getMetadataMessageDigest().getValue();

        // When
        DigestService.DigestResult result = DigestService.computeDigests(dataObjectPackage(), null, "SHA-256", null);

        // Then
        assertThat(result.getComputedCount()).isEqualTo(1);
        assertThat(result.getWithoutFileIds()).containsExactly(withoutFile.getInDataObjectPackageId());
        assertThat(result.getSummary()).contains(withoutFile.getInDataObjectPackageId());
        assertThat(withoutFile.getMetadataMessageDigest().getValue()).isEqualTo(digestBefore);
        assertThat(bdoUnder("a.txt").getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-256");
    }

    @Test
    void shouldComputeTheDigestsInAnAlgorithmVitamDoesNotAccept() throws Exception {
        // When
        DigestService.computeDigests(dataObjectPackage(), null, "SHA3-256", null);

        // Then
        assertThat(bdoUnder("a.txt").getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA3-256");
        assertThat(bdoUnder("a.txt").getMetadataMessageDigest().getValue()).isEqualTo(
            digestOf("first content", "SHA3-256")
        );
        assertThat(DigestService.getDigestWarnings(dataObjectPackage())).anyMatch(w -> w.contains("Vitam"));
    }

    @Test
    void shouldRefuseToComputeInAnAlgorithmUnknownToJava() {
        assertThatThrownBy(() -> DigestService.computeDigests(dataObjectPackage(), null, "UNKNOWN-ALGORITHM", null))
            .isInstanceOf(SEDALibException.class)
            .hasMessageContaining("UNKNOWN-ALGORITHM");
        assertThat(dataObjectPackage().getDigestAlgorithm()).isEqualTo("SHA-512");
    }

    @Test
    void shouldNotWarnAboutAConsistentPackage() {
        assertThat(DigestService.getDigestWarnings(dataObjectPackage())).isEmpty();
        assertThat(ExportService.getDigestWarningsSummary(dataObjectPackage())).isEmpty();
    }

    @Test
    void shouldWarnAboutDigestAnomaliesWithoutBlocking() throws Exception {
        // Given a wrong digest set by hand, and a package mixing algorithms
        BinaryDataObject wrong = bdoUnder("a.txt");
        wrong.addMetadata(new DigestType("MessageDigest", "not-a-digest", "UNKNOWN-ALGORITHM"));
        DigestService.computeDigests(dataObjectPackage(), root("b.txt"), "SHA-256", null);

        // When
        List<String> warnings = DigestService.getDigestWarnings(dataObjectPackage());

        // Then the wrong digest is kept, and reported
        assertThat(wrong.getMetadataMessageDigest().getValue()).isEqualTo("not-a-digest");
        assertThat(warnings.get(0)).contains("plusieurs algorithmes");
        assertThat(warnings)
            .filteredOn(w -> w.startsWith(wrong.getInDataObjectPackageId()))
            .anyMatch(w -> w.contains("Vitam"))
            .anyMatch(w -> w.contains("Java"))
            .anyMatch(w -> w.contains("hexadécimale"));
        assertThat(ExportService.getDigestWarningsSummary(dataObjectPackage()))
            .contains("Avertissement")
            .contains(wrong.getInDataObjectPackageId());
    }

    @Test
    void shouldListTheBinaryDataObjectsOfASubTreeOnce() {
        // When
        Set<BinaryDataObject> all = DigestService.binaryDataObjectsUnder(dataObjectPackage().getGhostRootAu());

        // Then
        assertThat(all).hasSize(2).contains(bdoUnder("a.txt"), bdoUnder("b.txt"));
    }
}
