/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.utils.digest;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;

import java.util.concurrent.*;

/**
 * Utility class to compute digest of a large file with parallel chunk loading
 * (Prefetching).
 * <p>
 * This class optimizes the calculation of standard digests (like SHA-512) for
 * large files
 * by decouping the IO reading phase from the CPU computing phase.
 * It uses multiple threads to read chunks of the file in parallel (saturating
 * IO bandwidth),
 * orders them, and feeds them to the sequential MessageDigest updater.
 */
public class PrefetchedDigestComputer {

    // Default configuration
    private static final int DEFAULT_BUFFER_SIZE = Math.toIntExact(BinaryUnit.KIBI.toBytes(128));
    private static final int DEFAULT_QUEUE_DEPTH = 16;
    private static final int DEFAULT_IO_THREADS = 4;

    private final int bufferSize;

    private final ExecutorService executor;

    /**
     * Instantiates a new Prefetched digest computer.
     * Uses default settings (128KB buffer, 4 IO threads).
     */
    public PrefetchedDigestComputer() {
        this(DEFAULT_BUFFER_SIZE, DEFAULT_IO_THREADS);
    }

    /**
     * Instantiates a new Prefetched digest computer.
     *
     * @param bufferSize size of each chunk to read (in bytes)
     * @param ioThreads  number of parallel threads for reading the file
     */
    public PrefetchedDigestComputer(int bufferSize, int ioThreads) {
        this.bufferSize = bufferSize;
        this.executor = Executors.newFixedThreadPool(ioThreads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Digest-IO-Prefetcher");
            return t;
        });
    }

    /**
     * Computes the digest using the provided MessageDigest instance.
     *
     * @param digest the message digest to update
     * @param path   the file path
     * @param logger information logger
     * @return the digest bytes
     * @throws SEDALibException on error
     */
    public byte[] compute(MessageDigest digest, Path path, SEDALibProgressLogger logger) throws SEDALibException {

        final long fileSize;
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            fileSize = channel.size();
        } catch (IOException e) {
            throw new SEDALibException("Unable to open file " + path, e);
        }

        // Calculate total chunks
        long totalChunks = (fileSize + bufferSize - 1) / bufferSize;

        // PriorityBlockingQueue is perfect for reordering out-of-order completions if
        // we used CompletionService,
        // but simple "submission window" is easier.
        // Let's use a wrapper allowing us to wait for chunks in order.

        // Actually, the simplest parallel prefetch is:
        // Submit chunks 0..N.
        // Wait for Future 0, Update, Wait for Future 1, Update...
        // While waiting, other IO threads continue processing 2, 3, 4.

        final int LOOK_AHEAD = DEFAULT_QUEUE_DEPTH;
        @SuppressWarnings("unchecked")
        Future<ReaderResult>[] window = (Future<ReaderResult>[]) new Future[LOOK_AHEAD];

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            DigestProgressLogger progressLogger = new DigestProgressLogger(logger, path, fileSize, "parallèle");
            long logBytesReadTotal = 0;

            int currentSubmitIndex = 0;
            int currentConsumeIndex = 0;

            // Fill pipeline
            for (int i = 0; i < LOOK_AHEAD && i < totalChunks; i++) {
                window[i] = submitReadTask(channel, i);
                currentSubmitIndex++;
            }

            // Consume loop
            while (currentConsumeIndex < totalChunks) {
                int windowSlot = currentConsumeIndex % LOOK_AHEAD;
                Future<ReaderResult> future = window[windowSlot];

                ReaderResult result;
                try {
                    result = future.get(); // BLOCKS until chunk is ready
                } catch (InterruptedException | ExecutionException e) {
                    throw new SEDALibException("Error reading file chunk " + currentConsumeIndex, e);
                }

                // Update digest
                result.buffer.flip();
                digest.update(result.buffer);
                logBytesReadTotal += result.bytesRead;

                progressLogger.logProgress(logBytesReadTotal);

                // Move window forward
                currentConsumeIndex++;
                if (currentSubmitIndex < totalChunks) {
                    window[windowSlot] = submitReadTask(channel, currentSubmitIndex);
                    currentSubmitIndex++;
                }
            }

            progressLogger.logEnd();

            return digest.digest();

        } catch (IOException e) {
            throw new SEDALibException("IO Error scanning file " + path, e);
        }
    }

    private Future<ReaderResult> submitReadTask(FileChannel channel, int chunkIndex) {
        long position = (long) chunkIndex * bufferSize;
        return executor.submit(() -> {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
            int read;
            try {
                // FileChannel read(buf, pos) is thread-safe and allows parallel reads at
                // different positions
                read = channel.read(buffer, position);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new ReaderResult(buffer, read);
        });
    }

    private static class ReaderResult {
        final ByteBuffer buffer;
        final int bytesRead;

        ReaderResult(ByteBuffer buffer, int bytesRead) {
            this.buffer = buffer;
            this.bytesRead = bytesRead;
        }
    }

    /**
     * Stop the internal thread pool.
     */
    public void close() {
        executor.shutdown();
    }
}
