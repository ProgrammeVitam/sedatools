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
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static java.nio.file.StandardOpenOption.READ;

public class NioDigestComputer {

    private static final int MIN_BUFFER_SIZE = 1;
    private static final int MAX_BUFFER_SIZE = 64 * (int) FileUtils.ONE_KB;

    public byte[] compute(MessageDigest digest, Path path, SEDALibProgressLogger logger)
        throws SEDALibException {

        final long fileSize;
        try {
            fileSize = Files.size(path);
        } catch (IOException e) {
            throw new SEDALibException(
                String.format("Impossible d'accéder au fichier [%s]", path), e);
        }

        try (FileChannel channel = FileChannel.open(path, READ)) {

            DigestProgressLogger progressLogger =
                new DigestProgressLogger(logger, path, fileSize);

            final int bufferSize = (int) Math.max(MIN_BUFFER_SIZE, Math.min(fileSize, MAX_BUFFER_SIZE));
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

            long bytesReadTotal = 0;
            int bytesRead;

            while ((bytesRead = channel.read(buffer)) != -1) {
                buffer.flip();
                digest.update(buffer);
                buffer.clear();

                bytesReadTotal += bytesRead;
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
