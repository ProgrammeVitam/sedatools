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
package fr.gouv.vitam.tools.sedalib.utils.digest;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DigestComputerTest {

    @TempDir
    Path tempDir;

    @Test
    void testComputeDigestSmallFile() throws IOException, SEDALibException, NoSuchAlgorithmException {
        // Given
        Path file = tempDir.resolve("small.txt");
        String content = "Hello World";
        Files.writeString(file, content);

        // When
        String digest = DigestComputer.compute(file, "SHA-512");

        // Then
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] expectedBytes = md.digest(content.getBytes());
        String expected = bytesToHex(expectedBytes);

        assertThat(digest).isEqualTo(expected);
    }

    @Test
    void testComputeDigestEmptyFile() throws IOException, SEDALibException, NoSuchAlgorithmException {
        // Given
        Path file = tempDir.resolve("empty.txt");
        Files.createFile(file);

        // When
        String digest = DigestComputer.compute(file, "SHA-512");

        // Then
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] expectedBytes = md.digest(new byte[0]);
        String expected = bytesToHex(expectedBytes);

        assertThat(digest).isEqualTo(expected);
    }

    @Test
    void testComputeDigestLargeFile() throws IOException, SEDALibException, NoSuchAlgorithmException {
        // Given: Create a file larger than 20MB to trigger parallel prefetching
        // 21 MB
        int size = 21 * 1024 * 1024;
        Path file = tempDir.resolve("large.bin");

        // Generate random content
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        Files.write(file, data);

        // When
        String digest = DigestComputer.compute(file, "SHA-512");

        // Then
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] expectedBytes = md.digest(data);
        String expected = bytesToHex(expectedBytes);

        assertThat(digest).isEqualTo(expected);
    }

    @Test
    void testComputeDigestFileLargerThan2GB() throws IOException, SEDALibException, NoSuchAlgorithmException {
        // Given: Create a sparse file larger than 2GB (e.g., 2.5 GB)
        // 2.5 GB = 2684354560 bytes
        long size = 2684354560L;
        Path file = tempDir.resolve("huge_sparse.bin");

        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file.toFile(), "rw")) {
            raf.setLength(size);
        }

        // When
        String digest = DigestComputer.compute(file, "SHA-512");

        // Then
        // We know a sparse file reads as zeros.
        // To verify, we can either hardcode the hash of 2.5GB of zeros or compute it.
        // Computing it in the test ensures correctness but takes time.
        // 2.5GB read ~ 5-10s at 500MB/s.

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] buffer = new byte[64 * 1024]; // 64KB of zeros
        long remaining = size;
        while (remaining > 0) {
            int toRead = (int) Math.min(buffer.length, remaining);
            md.update(buffer, 0, toRead);
            remaining -= toRead;
        }
        String expected = bytesToHex(md.digest());

        assertThat(digest).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = { "SHA-512", "SHA-384", "SHA-256" })
    void shouldComputeDigestWithEachSupportedAlgorithm(String algorithm)
        throws IOException, SEDALibException, NoSuchAlgorithmException {
        // Given
        Path file = tempDir.resolve("small.txt");
        Files.writeString(file, "Hello World");

        // When
        String digest = DigestComputer.compute(file, algorithm);

        // Then
        assertThat(digest).isEqualTo(bytesToHex(MessageDigest.getInstance(algorithm).digest("Hello World".getBytes())));
    }

    @ParameterizedTest
    @ValueSource(strings = { "MD5", "SHA-1", "SHA3-512", "MD2", "SHA-224" })
    void shouldComputeDigestWithAnAlgorithmVitamDoesNotPropose(String algorithm)
        throws IOException, SEDALibException, NoSuchAlgorithmException {
        // Given
        Path file = tempDir.resolve("small.txt");
        Files.writeString(file, "Hello World");

        // When
        String digest = DigestComputer.compute(file, algorithm);

        // Then
        assertThat(digest).isEqualTo(bytesToHex(MessageDigest.getInstance(algorithm).digest("Hello World".getBytes())));
    }

    @Test
    void shouldRefuseToComputeDigestWithAnAlgorithmUnknownToJava() throws IOException {
        // Given
        Path file = tempDir.resolve("small.txt");
        Files.writeString(file, "Hello World");

        // When / Then
        assertThatThrownBy(() -> DigestComputer.compute(file, "UNKNOWN-ALGORITHM"))
            .isInstanceOf(SEDALibException.class)
            .hasMessageContaining("UNKNOWN-ALGORITHM");
    }

    @ParameterizedTest
    @ValueSource(strings = { "SHA-512", "SHA-384", "SHA-256", "SHA-1", "MD5" })
    void shouldNotWarnAboutAnAlgorithmVitamAccepts(String algorithm) {
        assertThat(DigestComputer.getAlgorithmWarnings(algorithm)).isEmpty();
    }

    @Test
    void shouldWarnAboutAnAlgorithmVitamDoesNotAccept() {
        assertThat(DigestComputer.getAlgorithmWarnings("SHA3-512")).singleElement().asString().contains("Vitam");
    }

    @Test
    void shouldWarnAboutAnAlgorithmUnknownToJava() {
        assertThat(DigestComputer.getAlgorithmWarnings("UNKNOWN-ALGORITHM"))
            .hasSize(2)
            .anyMatch(warning -> warning.contains("Vitam"))
            .anyMatch(warning -> warning.contains("Java"));
    }

    @Test
    void shouldWarnAboutAMissingAlgorithm() {
        assertThat(DigestComputer.getAlgorithmWarnings(null)).singleElement().asString().contains("absent");
        assertThat(DigestComputer.getAlgorithmWarnings(" ")).singleElement().asString().contains("absent");
    }

    @Test
    void shouldNotWarnAboutADigestValueOfTheLengthOfItsAlgorithm() {
        // Given
        String sha256 = "a".repeat(64);

        // When / Then
        assertThat(DigestComputer.getValueWarnings(sha256, "SHA-256")).isEmpty();
        assertThat(DigestComputer.getValueWarnings(sha256.toUpperCase(), "SHA-256")).isEmpty();
    }

    @Test
    void shouldWarnAboutADigestValueOfAnotherLengthThanItsAlgorithm() {
        // Given
        String sha256 = "a".repeat(64);

        // When / Then
        assertThat(DigestComputer.getValueWarnings(sha256, "SHA-512")).singleElement().asString().contains("128");
    }

    @Test
    void shouldWarnAboutADigestValueThatIsNotHexadecimal() {
        assertThat(DigestComputer.getValueWarnings("not-a-digest", "SHA-512"))
            .singleElement()
            .asString()
            .contains("hexadécimale");
        assertThat(DigestComputer.getValueWarnings(null, "SHA-512"))
            .singleElement()
            .asString()
            .contains("hexadécimale");
    }

    @Test
    void shouldOnlyWarnAboutTheAlgorithmOfADigestOfAnAlgorithmUnknownToJava() {
        assertThat(DigestComputer.getValueWarnings("abcd", "UNKNOWN-ALGORITHM")).noneMatch(
            w -> w.contains("caractères")
        );
        assertThat(DigestComputer.getValueWarnings("abcd", null)).singleElement().asString().contains("absent");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
