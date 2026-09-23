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

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for computing file digests using optimized methods.
 */
public class DigestComputer {

    /**
     * The constant ALGORITHMS.
     */
    public static final List<String> ALGORITHMS = Arrays.asList(
        "SHA-512",
        "SHA-384",
        "SHA-256",
        "SHA-1",
        "SHA3-512",
        "SHA3-384",
        "SHA3-256",
        "SHA3-224",
        "MD5",
        "MD2"
    );

    // Private constructor to hide the implicit public one
    private DigestComputer() {}

    /**
     * Compute digest for a file with a specific algorithm.
     * Automatically switches between standard IO and parallel prefetching based on
     * file size.
     *
     * @param path      the file path
     * @param algorithm the hash algorithm (e.g., "SHA-512", "SHA-256")
     * @param logger    the logger (can be null)
     * @return the hex string of the digest
     * @throws SEDALibException if an error occurs
     */
    public static String compute(Path path, String algorithm, SEDALibProgressLogger logger) throws SEDALibException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = new NioDigestComputer().compute(digest, path, logger);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new SEDALibException("Impossible de mobiliser l'algorithme de hashage " + algorithm, e);
        }
    }

    /**
     * Compute digest for a file with a specific algorithm (no logger).
     *
     * @param path      the file path
     * @param algorithm the hash algorithm
     * @return the hex string of the digest
     * @throws SEDALibException if an error occurs
     */
    public static String compute(Path path, String algorithm) throws SEDALibException {
        return compute(path, algorithm, null);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
