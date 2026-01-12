package fr.gouv.vitam.tools.sedalib.utils.digest;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class DigestSha512Test {

    @TempDir
    Path tempDir;

    @Test
    void testComputeDigestSmallFile() throws IOException, SEDALibException, NoSuchAlgorithmException {
        // Given
        Path file = tempDir.resolve("small.txt");
        String content = "Hello World";
        Files.writeString(file, content);

        // When
        String digest = DigestSha512.compute(file);

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
        String digest = DigestSha512.compute(file);

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
        String digest = DigestSha512.compute(file);

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
        String digest = DigestSha512.compute(file);

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

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
