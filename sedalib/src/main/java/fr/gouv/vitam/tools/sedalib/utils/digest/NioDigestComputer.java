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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

public class NioDigestComputer {

    public byte[] compute(MessageDigest digest, Path path, SEDALibProgressLogger logger) throws SEDALibException {
        long fileSize;
        try {
            fileSize = Files.size(path);
        } catch (IOException e) {
            throw new SEDALibException(String.format("Impossible d'accéder au fichier [%s]", path), e);
        }

        final int CHUNK_BUFFER_SIZE = Math.toIntExact(BinaryUnit.KIBI.toBytes(64));

        try (FileChannel channel = FileChannel.open(path)) {

            DigestProgressLogger progressLogger = new DigestProgressLogger(logger, path, fileSize, "NIO");
            long bytesReadTotal = 0;

            int bufferSize = (int) Math.min(fileSize > 0 ? fileSize : CHUNK_BUFFER_SIZE, CHUNK_BUFFER_SIZE);
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer);
                int read = buffer.limit();
                bytesReadTotal += read;
                buffer.clear();

                progressLogger.logProgress(bytesReadTotal);
            }

            progressLogger.logEnd();

            return digest.digest();
        } catch (IOException e) {
            throw new SEDALibException(
                    String.format("Impossible de calculer le hash du fichier [%s]", path), e);
        }
    }
}
