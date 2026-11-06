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
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for computing file digests using optimized methods.
 */
public class DigestComputer {

    /**
     * The default digest algorithm.
     */
    public static final String DEFAULT_ALGORITHM = "SHA-512";

    /**
     * The digest algorithms a package can be built with, the strongest first.
     * <p>
     * Vitam accepts MD5, SHA-1, SHA-256, SHA-384 and SHA-512; MD5 and SHA-1 are obsolete, so only the
     * SHA-2 ones are proposed.
     */
    public static final List<String> SUPPORTED_ALGORITHMS = List.of(DEFAULT_ALGORITHM, "SHA-384", "SHA-256");

    private static final Pattern HEXADECIMAL = Pattern.compile("[0-9a-fA-F]+");

    // Private constructor to hide the implicit public one
    private DigestComputer() {}

    /**
     * Tells if a digest algorithm is one a package can be built with.
     *
     * @param algorithm the algorithm name, as in the SEDA MessageDigest algorithm attribute
     * @return true if supported
     */
    public static boolean isSupported(String algorithm) {
        return SUPPORTED_ALGORITHMS.contains(algorithm);
    }

    /**
     * Checks that a digest algorithm is one a package can be built with.
     *
     * @param algorithm the algorithm name
     * @return the algorithm
     * @throws SEDALibException if the algorithm is not supported
     */
    public static String checkSupported(String algorithm) throws SEDALibException {
        if (!isSupported(algorithm)) throw new SEDALibException(
            "Algorithme de hachage [" +
            algorithm +
            "] non supporté, les algorithmes possibles sont " +
            SUPPORTED_ALGORITHMS
        );
        return algorithm;
    }

    /**
     * Checks that a digest value is consistent with its algorithm: an hexadecimal string of the
     * length the algorithm produces. An algorithm unknown to Java is not checked further than being
     * hexadecimal, as a manifest may declare any algorithm.
     *
     * @param value     the digest value
     * @param algorithm the digest algorithm, or null
     * @throws SEDALibException if the value is not consistent
     */
    public static void checkValue(String value, String algorithm) throws SEDALibException {
        if ((value == null) || !HEXADECIMAL.matcher(value).matches()) throw new SEDALibException(
            "L'empreinte [" + value + "] n'est pas une valeur hexadécimale"
        );
        if (algorithm == null) return;
        int expectedLength;
        try {
            expectedLength = MessageDigest.getInstance(algorithm).getDigestLength() * 2;
        } catch (NoSuchAlgorithmException e) {
            return;
        }
        if (value.length() != expectedLength) throw new SEDALibException(
            "L'empreinte " +
            algorithm +
            " doit compter " +
            expectedLength +
            " caractères hexadécimaux, et non " +
            value.length()
        );
    }

    /**
     * Compute the digest of a file.
     * Automatically switches between standard IO and parallel prefetching based on
     * file size.
     *
     * @param path      the file path
     * @param algorithm the digest algorithm, one of {@link #SUPPORTED_ALGORITHMS}
     * @param logger    the logger (can be null)
     * @return the hex string of the digest
     * @throws SEDALibException if the algorithm is not supported or the file can't be read
     */
    public static String compute(Path path, String algorithm, SEDALibProgressLogger logger) throws SEDALibException {
        checkSupported(algorithm);
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = new NioDigestComputer().compute(digest, path, logger);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new SEDALibException("Impossible de mobiliser l'algorithme de hachage " + algorithm, e);
        }
    }

    /**
     * Compute the digest of a file (no logger).
     *
     * @param path      the file path
     * @param algorithm the digest algorithm, one of {@link #SUPPORTED_ALGORITHMS}
     * @return the hex string of the digest
     * @throws SEDALibException if the algorithm is not supported or the file can't be read
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
