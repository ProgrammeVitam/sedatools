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
import java.util.ArrayList;
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
     * The digest algorithms proposed to build a package, the strongest first. Any other algorithm
     * Java can compute may still be used, for example to build erroneous packages for testing.
     */
    public static final List<String> PROPOSED_ALGORITHMS = List.of(DEFAULT_ALGORITHM, "SHA-384", "SHA-256");

    /**
     * The digest algorithms Vitam accepts (its {@code DigestType}). The SEDA schema itself accepts any
     * algorithm name.
     */
    public static final List<String> VITAM_ALGORITHMS = List.of("MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512");

    private static final Pattern HEXADECIMAL = Pattern.compile("[0-9a-fA-F]+");

    // Private constructor to hide the implicit public one
    private DigestComputer() {}

    /**
     * Tells if Vitam accepts a digest algorithm.
     *
     * @param algorithm the algorithm name, as in the SEDA MessageDigest algorithm attribute
     * @return true if accepted by Vitam
     */
    public static boolean isAcceptedByVitam(String algorithm) {
        return VITAM_ALGORITHMS.contains(algorithm);
    }

    /**
     * Tells if Java can compute digests with an algorithm.
     *
     * @param algorithm the algorithm name
     * @return true if computable
     */
    public static boolean isComputable(String algorithm) {
        if (algorithm == null) return false;
        try {
            MessageDigest.getInstance(algorithm);
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Lists the warnings about a digest algorithm. They never prevent from using it, as erroneous
     * packages may be built on purpose.
     *
     * @param algorithm the algorithm name, or null
     * @return the warnings, empty if none
     */
    public static List<String> getAlgorithmWarnings(String algorithm) {
        List<String> warnings = new ArrayList<>();
        if ((algorithm == null) || algorithm.isBlank()) {
            warnings.add("Algorithme de hachage absent");
            return warnings;
        }
        if (!isAcceptedByVitam(algorithm)) warnings.add(
            "Algorithme de hachage [" + algorithm + "] non accepté par Vitam " + VITAM_ALGORITHMS
        );
        if (!isComputable(algorithm)) warnings.add(
            "Algorithme de hachage [" + algorithm + "] inconnu de Java : les empreintes ne pourront pas être calculées"
        );
        return warnings;
    }

    /**
     * Lists the warnings about a digest value: not hexadecimal, or not of the length its algorithm
     * produces, plus the warnings about the algorithm. They never prevent from using the value, as
     * erroneous packages may be built on purpose.
     *
     * @param value     the digest value
     * @param algorithm the digest algorithm, or null
     * @return the warnings, empty if none
     */
    public static List<String> getValueWarnings(String value, String algorithm) {
        List<String> warnings = new ArrayList<>(getAlgorithmWarnings(algorithm));
        if ((value == null) || !HEXADECIMAL.matcher(value).matches()) {
            warnings.add("L'empreinte [" + value + "] n'est pas une valeur hexadécimale");
            return warnings;
        }
        if (!isComputable(algorithm)) return warnings;
        int expectedLength;
        try {
            expectedLength = MessageDigest.getInstance(algorithm).getDigestLength() * 2;
        } catch (NoSuchAlgorithmException e) {
            return warnings;
        }
        if (value.length() != expectedLength) warnings.add(
            "L'empreinte " +
            algorithm +
            " devrait compter " +
            expectedLength +
            " caractères hexadécimaux, et non " +
            value.length()
        );
        return warnings;
    }

    /**
     * Compute the digest of a file.
     * Automatically switches between standard IO and parallel prefetching based on
     * file size.
     *
     * @param path      the file path
     * @param algorithm the digest algorithm, any algorithm Java can compute
     * @param logger    the logger (can be null)
     * @return the hex string of the digest
     * @throws SEDALibException if Java can't compute the algorithm or the file can't be read
     */
    public static String compute(Path path, String algorithm, SEDALibProgressLogger logger) throws SEDALibException {
        if (!isComputable(algorithm)) throw new SEDALibException(
            "Impossible de calculer une empreinte avec l'algorithme de hachage [" + algorithm + "], inconnu de Java"
        );
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
     * @param algorithm the digest algorithm, any algorithm Java can compute
     * @return the hex string of the digest
     * @throws SEDALibException if Java can't compute the algorithm or the file can't be read
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
