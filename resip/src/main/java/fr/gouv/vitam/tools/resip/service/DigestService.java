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

import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.digest.DigestComputer;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.OBJECTS;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.OBJECTS_WARNINGS;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * Computes and checks the digests of the BinaryDataObjects of a work, without any user interface.
 * <p>
 * The digest algorithm of a work is the one of its package: the new works take the one of the
 * preferences, and the digests computed afterwards in the work (added files, expansions, mail
 * extractions, recomputations) use the one of the package. Nothing is refused for its algorithm or
 * value, as ReSIP is also used to build erroneous packages for testing: the anomalies are only
 * reported as warnings.
 */
public final class DigestService {

    private DigestService() {}

    /**
     * The result of a digest computation.
     */
    public static final class DigestResult {

        private final String algorithm;
        private int computedCount;
        private final List<String> withoutFileIds = new ArrayList<>();
        private final List<String> failures = new ArrayList<>();

        private DigestResult(String algorithm) {
            this.algorithm = algorithm;
        }

        /**
         * @return the algorithm of the computed digests
         */
        public String getAlgorithm() {
            return algorithm;
        }

        /**
         * @return the number of computed digests
         */
        public int getComputedCount() {
            return computedCount;
        }

        /**
         * @return the identifiers of the BinaryDataObjects skipped because they have no file
         */
        public List<String> getWithoutFileIds() {
            return Collections.unmodifiableList(withoutFileIds);
        }

        /**
         * @return the BinaryDataObjects whose digest couldn't be computed, with the reason
         */
        public List<String> getFailures() {
            return Collections.unmodifiableList(failures);
        }

        /**
         * @return the summary of the computation
         */
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            summary.append(computedCount).append(" empreinte(s) ").append(algorithm).append(" calculée(s)");
            if (!withoutFileIds.isEmpty()) summary
                .append("\n")
                .append(withoutFileIds.size())
                .append(" objet(s) sans fichier ignoré(s) : ")
                .append(String.join(", ", withoutFileIds));
            if (!failures.isEmpty()) summary
                .append("\n")
                .append(failures.size())
                .append(" objet(s) en erreur :\n")
                .append(String.join("\n", failures));
            return summary.toString();
        }
    }

    /**
     * Recomputes the digests of the BinaryDataObjects of a package, or of a sub-tree of it. Only the
     * MessageDigest metadata is replaced, the size, format identification and file information are
     * kept. A BinaryDataObject without file is skipped, and one whose file can't be read is reported,
     * without stopping the computation.
     * <p>
     * Computing the whole package in another algorithm than its own changes the algorithm of the
     * package, which is then used for the digests computed afterwards. Computing a sub-tree doesn't,
     * so that a package mixing algorithms can be built on purpose.
     *
     * @param dataObjectPackage the package
     * @param from              the ArchiveUnit of the sub-tree, or null for the whole package
     * @param algorithm         the digest algorithm, or null for the one of the package
     * @param spl               the progress logger, or null
     * @return the result of the computation
     * @throws SEDALibException     if Java can't compute the algorithm
     * @throws InterruptedException if the computation is interrupted
     */
    public static DigestResult computeDigests(
        DataObjectPackage dataObjectPackage,
        ArchiveUnit from,
        String algorithm,
        SEDALibProgressLogger spl
    ) throws SEDALibException, InterruptedException {
        String usedAlgorithm = (algorithm == null) || algorithm.isBlank()
            ? dataObjectPackage.getDigestAlgorithm()
            : algorithm.trim();
        if (!DigestComputer.isComputable(usedAlgorithm)) throw new SEDALibException(
            "Impossible de calculer des empreintes avec l'algorithme de hachage [" +
            usedAlgorithm +
            "], inconnu de Java"
        );
        Collection<BinaryDataObject> bdos = from == null
            ? sortedById(dataObjectPackage.getBdoInDataObjectPackageIdMap().values())
            : binaryDataObjectsUnder(from);
        doProgressLog(
            spl,
            GLOBAL,
            "resip: calcul des empreintes " +
            usedAlgorithm +
            " de " +
            bdos.size() +
            " objet(s) binaire(s)" +
            (from == null ? "" : " sous l'ArchiveUnit " + from.getInDataObjectPackageId()),
            null
        );
        if (from == null) dataObjectPackage.setDigestAlgorithm(usedAlgorithm);

        DigestResult result = new DigestResult(usedAlgorithm);
        int counter = 0;
        for (BinaryDataObject bdo : bdos) {
            counter++;
            if ((bdo.getOnDiskPath() == null) || !Files.isRegularFile(bdo.getOnDiskPath())) {
                result.withoutFileIds.add(bdo.getInDataObjectPackageId());
                doProgressLog(
                    spl,
                    OBJECTS_WARNINGS,
                    "resip: objet " + bdo.getInDataObjectPackageId() + " sans fichier, ignoré",
                    null
                );
            } else {
                try {
                    bdo.computeMessageDigest(usedAlgorithm, null);
                    result.computedCount++;
                } catch (SEDALibException e) {
                    result.failures.add(bdo.getInDataObjectPackageId() + " : " + e.getMessage());
                    doProgressLog(
                        spl,
                        OBJECTS_WARNINGS,
                        "resip: empreinte de l'objet " + bdo.getInDataObjectPackageId() + " non calculée",
                        e
                    );
                }
            }
            doProgressLogIfStep(spl, OBJECTS, counter, "resip: " + counter + " objet(s) binaire(s) traité(s)");
        }
        doProgressLog(spl, GLOBAL, "resip: " + result.getSummary(), null);
        return result;
    }

    /**
     * Lists the BinaryDataObjects of an ArchiveUnit and of all its descendants, through their
     * DataObjectGroups, each one once.
     *
     * @param from the ArchiveUnit
     * @return the BinaryDataObjects, in tree order
     */
    public static Set<BinaryDataObject> binaryDataObjectsUnder(ArchiveUnit from) {
        Set<BinaryDataObject> result = new LinkedHashSet<>();
        Set<ArchiveUnit> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        List<ArchiveUnit> toVisit = new ArrayList<>();
        toVisit.add(from);
        while (!toVisit.isEmpty()) {
            ArchiveUnit au = toVisit.remove(0);
            if (!visited.add(au)) continue;
            for (DataObject dataObject : au.getDataObjectRefList().getDataObjectList()) {
                if (dataObject instanceof DataObjectGroup) result.addAll(
                    ((DataObjectGroup) dataObject).getBinaryDataObjectList()
                );
                else if (dataObject instanceof BinaryDataObject) result.add((BinaryDataObject) dataObject);
            }
            toVisit.addAll(au.getChildrenAuList().getArchiveUnitList());
        }
        return result;
    }

    /**
     * Lists the warnings about the digests of a package: BinaryDataObject without digest, algorithm
     * Vitam doesn't accept or Java doesn't know, value not hexadecimal or not of the length of its
     * algorithm, and package mixing several algorithms. They never prevent from exporting the
     * package.
     *
     * @param dataObjectPackage the package
     * @return the warnings, prefixed by the BinaryDataObject identifier, empty if none
     */
    public static List<String> getDigestWarnings(DataObjectPackage dataObjectPackage) {
        List<String> warnings = new ArrayList<>();
        Set<String> algorithms = new TreeSet<>();
        for (BinaryDataObject bdo : sortedById(dataObjectPackage.getBdoInDataObjectPackageIdMap().values())) {
            DigestType messageDigest = bdo.getMetadataMessageDigest();
            if (messageDigest == null) {
                warnings.add(bdo.getInDataObjectPackageId() + " : pas d'empreinte");
                continue;
            }
            if (messageDigest.getAlgorithm() != null) algorithms.add(messageDigest.getAlgorithm());
            for (String warning : DigestComputer.getValueWarnings(
                messageDigest.getValue(),
                messageDigest.getAlgorithm()
            )) warnings.add(bdo.getInDataObjectPackageId() + " : " + warning);
        }
        if (algorithms.size() > 1) warnings.add(0, "Le paquet mélange plusieurs algorithmes de hachage " + algorithms);
        return warnings;
    }

    private static List<BinaryDataObject> sortedById(Collection<BinaryDataObject> bdos) {
        TreeMap<String, BinaryDataObject> byId = new TreeMap<>();
        for (BinaryDataObject bdo : bdos) byId.put(bdo.getInDataObjectPackageId(), bdo);
        return new ArrayList<>(byId.values());
    }
}
