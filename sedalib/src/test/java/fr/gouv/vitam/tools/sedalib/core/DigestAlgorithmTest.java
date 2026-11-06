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
package fr.gouv.vitam.tools.sedalib.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DigestAlgorithmTest {

    private static final String CONTENT = "Hello World";

    @TempDir
    Path tempDir;

    private static String digestOf(String content, String algorithm) throws NoSuchAlgorithmException {
        return HexFormat.of()
            .formatHex(MessageDigest.getInstance(algorithm).digest(content.getBytes(StandardCharsets.UTF_8)));
    }

    private static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(DataObjectPackage.class, new DataObjectPackageSerializer());
        module.addDeserializer(DataObjectPackage.class, new DataObjectPackageDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    private Path file() throws IOException {
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, CONTENT);
        return file;
    }

    @Test
    void shouldBuildAPackageInSha512ByDefault() {
        assertThat(new DataObjectPackage(SedaVersion.V2_1).getDigestAlgorithm()).isEqualTo("SHA-512");
    }

    @Test
    void shouldRefuseAnUnsupportedDigestAlgorithm() {
        DataObjectPackage dataObjectPackage = new DataObjectPackage(SedaVersion.V2_1);

        assertThatThrownBy(() -> dataObjectPackage.setDigestAlgorithm("MD5")).isInstanceOf(SEDALibException.class);
        assertThat(dataObjectPackage.getDigestAlgorithm()).isEqualTo("SHA-512");
    }

    @Test
    void shouldKeepTheDigestAlgorithmThroughJson() throws Exception {
        // Given
        DataObjectPackage dataObjectPackage = new DataObjectPackage(SedaVersion.V2_2);
        dataObjectPackage.setDigestAlgorithm("SHA-256");

        // When
        String json = mapper().writeValueAsString(dataObjectPackage);
        DataObjectPackage read = mapper().readValue(json, DataObjectPackage.class);

        // Then
        assertThat(json).contains("\"digestAlgorithm\":\"SHA-256\"");
        assertThat(read.getDigestAlgorithm()).isEqualTo("SHA-256");
    }

    @Test
    void shouldReadAPackageSavedWithoutDigestAlgorithmInSha512() throws Exception {
        // Given a package saved before the algorithm was configurable
        String json = "{\"sedaVersion\":\"2.1\",\"dataObjectGroupList\":[],\"ArchiveUnitList\":[]}";

        // When
        DataObjectPackage read = mapper().readValue(json, DataObjectPackage.class);

        // Then
        assertThat(read.getDigestAlgorithm()).isEqualTo("SHA-512");
    }

    @Test
    void shouldRefuseAJsonPackageWithAnUnsupportedDigestAlgorithm() {
        String json = "{\"sedaVersion\":\"2.1\",\"digestAlgorithm\":\"MD2\"}";

        assertThatThrownBy(() -> mapper().readValue(json, DataObjectPackage.class)).hasMessageContaining("MD2");
    }

    @Test
    void shouldExtractTechnicalElementsInTheAlgorithmOfThePackage() throws Exception {
        // Given
        DataObjectPackage dataObjectPackage = new DataObjectPackage(SedaVersion.V2_1);
        dataObjectPackage.setDigestAlgorithm("SHA-384");
        BinaryDataObject bdo = new BinaryDataObject(dataObjectPackage, file(), null, "BinaryMaster_1");

        // When
        bdo.extractTechnicalElements(null);

        // Then
        DigestType digest = bdo.getMetadataMessageDigest();
        assertThat(digest.getAlgorithm()).isEqualTo("SHA-384");
        assertThat(digest.getValue()).isEqualTo(digestOf(CONTENT, "SHA-384"));
    }

    @Test
    void shouldComputeOnlyTheMessageDigestOfABinaryDataObject() throws Exception {
        // Given a BinaryDataObject whose size and format were corrected by hand
        DataObjectPackage dataObjectPackage = new DataObjectPackage(SedaVersion.V2_1);
        dataObjectPackage.setDigestAlgorithm("SHA-256");
        BinaryDataObject bdo = new BinaryDataObject(dataObjectPackage, file(), null, "BinaryMaster_1");
        bdo.addMetadata(new DigestType("MessageDigest", "abcd", "SHA-512"));
        bdo.addMetadata(new IntegerType("Size", 42));
        bdo.addMetadata(new FormatIdentification(SedaVersion.V2_1, "Hand made", "text/plain", "x-fmt/111", null));
        String fileInfoBefore = String.valueOf(bdo.getMetadataFileInfo());

        // When
        bdo.computeMessageDigest(null);

        // Then
        assertThat(bdo.getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-256");
        assertThat(bdo.getMetadataMessageDigest().getValue()).isEqualTo(digestOf(CONTENT, "SHA-256"));
        assertThat(bdo.getMetadataSize().getValue()).isEqualTo(42L);
        assertThat(bdo.getMetadataFormatIdentification().getSimpleMetadata("FormatLitteral")).isEqualTo("Hand made");
        assertThat(String.valueOf(bdo.getMetadataFileInfo())).isEqualTo(fileInfoBefore);
    }

    @Test
    void shouldComputeTheMessageDigestInAGivenAlgorithm() throws Exception {
        // Given
        BinaryDataObject bdo = new BinaryDataObject(
            new DataObjectPackage(SedaVersion.V2_1),
            file(),
            null,
            "BinaryMaster_1"
        );

        // When
        bdo.computeMessageDigest("SHA-384", null);

        // Then
        assertThat(bdo.getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-384");
        assertThat(bdo.getMetadataMessageDigest().getValue()).isEqualTo(digestOf(CONTENT, "SHA-384"));
    }

    @Test
    void shouldRefuseToComputeTheMessageDigestOfABinaryDataObjectWithoutFile() {
        BinaryDataObject bdo = new BinaryDataObject(new DataObjectPackage(SedaVersion.V2_1));

        assertThatThrownBy(() -> bdo.computeMessageDigest(null))
            .isInstanceOf(SEDALibException.class)
            .hasMessageContaining("pas de fichier");
    }

    @Test
    void shouldImportFromDiskInTheConfiguredAlgorithm() throws Exception {
        // Given
        file();
        DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(SedaVersion.V2_1, List.of(tempDir), null);
        di.setDigestAlgorithm("SHA-256");

        // When
        di.doImport();

        // Then
        DataObjectPackage imported = di.getArchiveTransfer().getDataObjectPackage();
        assertThat(imported.getDigestAlgorithm()).isEqualTo("SHA-256");
        assertThat(imported.getBdoInDataObjectPackageIdMap().values())
            .singleElement()
            .satisfies(bdo -> {
                assertThat(bdo.getMetadataMessageDigest().getAlgorithm()).isEqualTo("SHA-256");
                assertThat(bdo.getMetadataMessageDigest().getValue()).isEqualTo(digestOf(CONTENT, "SHA-256"));
            });
    }
}
