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

import fr.gouv.vitam.tools.resip.data.StatisticData;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.data.FormatIdentification;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.DigestType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.IntegerType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.OBJECTS;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.OBJECTS_GROUP;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

/**
 * Analyses the tree of a work, without changing it nor any user interface: searches, end dates
 * check, statistics and duplicates.
 * <p>
 * The tree walks mark the ArchiveUnits they visit in their DataObjectPackage, so that an ArchiveUnit
 * reached by several paths is visited once.
 */
public final class AnalysisService {

    /**
     * The category of all the formats in the statistics.
     */
    public static final String ALL_FORMATS = "Tous formats";

    /**
     * The format standing for all the formats of no category in a technical search.
     */
    public static final String OTHER_FORMATS = "Other";

    private AnalysisService() {}

    /**
     * What a text search looks for.
     */
    public static final class SearchCriteria {

        private final String text;
        private final boolean withoutChildArchiveUnit;
        private final boolean withoutDataObject;
        private final boolean inIds;
        private final boolean inMetadata;
        private final boolean regExp;
        private final boolean caseSensitive;

        /**
         * @param text                    the searched text, or regular expression
         * @param withoutChildArchiveUnit true to keep only the ArchiveUnits without child
         * @param withoutDataObject       true to keep only the ArchiveUnits without DataObject
         * @param inIds                   true to search the identifiers of the ArchiveUnits and their
         *                                DataObjects, for the exact text
         * @param inMetadata              true to search the content metadata, instead of the title
         * @param regExp                  true if the text is a regular expression
         * @param caseSensitive           true to respect the case, when the text is not a regular
         *                                expression
         */
        public SearchCriteria(
            String text,
            boolean withoutChildArchiveUnit,
            boolean withoutDataObject,
            boolean inIds,
            boolean inMetadata,
            boolean regExp,
            boolean caseSensitive
        ) {
            this.text = text;
            this.withoutChildArchiveUnit = withoutChildArchiveUnit;
            this.withoutDataObject = withoutDataObject;
            this.inIds = inIds;
            this.inMetadata = inMetadata;
            this.regExp = regExp;
            this.caseSensitive = caseSensitive;
        }
    }

    /**
     * Searches the ArchiveUnits under an ArchiveUnit.
     *
     * @param from     the ArchiveUnit under which to search
     * @param criteria what to search
     * @param titleOf  the title an ArchiveUnit is searched in, the one shown in the tree
     * @return the found ArchiveUnits, in tree order
     */
    public static List<ArchiveUnit> search(
        ArchiveUnit from,
        SearchCriteria criteria,
        Function<ArchiveUnit, String> titleOf
    ) {
        String searchExp = criteria.text;
        Pattern searchPattern = null;
        if (criteria.regExp) searchPattern = Pattern.compile("[\\S\\s]*" + searchExp + "[\\S\\s]*");
        else if (!criteria.caseSensitive) searchExp = searchExp.toLowerCase();
        if (criteria.inIds) searchExp = "<" + searchExp + ">";
        DataObjectPackage dataObjectPackage = from.getDataObjectPackage();
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        List<ArchiveUnit> result = new LinkedList<>();
        searchUnder(from, criteria, searchExp, searchPattern, titleOf, result);
        return result;
    }

    private static void searchUnder(
        ArchiveUnit au,
        SearchCriteria criteria,
        String searchExp,
        Pattern searchPattern,
        Function<ArchiveUnit, String> titleOf,
        List<ArchiveUnit> result
    ) {
        DataObjectPackage dataObjectPackage = au.getDataObjectPackage();
        for (ArchiveUnit childUnit : au.getChildrenAuList().getArchiveUnitList()) {
            if (dataObjectPackage.isTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId())) continue;
            try {
                int dataObjectCount = 0;
                if (childUnit.getTheDataObjectGroup() != null) {
                    DataObjectGroup dataObjectGroup = childUnit.getTheDataObjectGroup();
                    dataObjectCount = dataObjectGroup.getBinaryDataObjectList().size() +
                    dataObjectGroup.getPhysicalDataObjectList().size();
                }
                if (
                    !(criteria.withoutChildArchiveUnit && (childUnit.getChildrenAuList().getCount() != 0)) &&
                    !(criteria.withoutDataObject && (dataObjectCount != 0))
                ) {
                    String searched;
                    if (criteria.inIds) searched = idsOf(childUnit);
                    else if (criteria.inMetadata) searched = childUnit.getContent().toString();
                    else searched = titleOf.apply(childUnit);
                    if (criteria.regExp) {
                        if (searchPattern.matcher(searched).matches()) result.add(childUnit);
                    } else {
                        if (!criteria.caseSensitive) searched = searched.toLowerCase();
                        if (searched.contains(searchExp)) result.add(childUnit);
                    }
                }
                dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());
            } catch (SEDALibException ignored) {
                // an ArchiveUnit whose content can't be read is not found
            }
            searchUnder(childUnit, criteria, searchExp, searchPattern, titleOf, result);
        }
    }

    // the identifiers of an ArchiveUnit and of its DataObjects, each one between < and >
    private static String idsOf(ArchiveUnit au) {
        StringBuilder ids = new StringBuilder("<" + au.getInDataObjectPackageId() + ">");
        for (DataObject dataObject : au.getDataObjectRefList().getDataObjectList()) {
            ids.append("<").append(dataObject.getInDataObjectPackageId()).append(">");
            if (dataObject instanceof DataObjectGroup) {
                for (BinaryDataObject bo : ((DataObjectGroup) dataObject).getBinaryDataObjectList()) ids
                    .append("<")
                    .append(bo.getInDataObjectPackageId())
                    .append(">");
                for (PhysicalDataObject po : ((DataObjectGroup) dataObject).getPhysicalDataObjectList()) ids
                    .append("<")
                    .append(po.getInDataObjectPackageId())
                    .append(">");
            }
        }
        return ids.toString();
    }

    /**
     * Searches the BinaryDataObjects under an ArchiveUnit by format and size.
     *
     * @param from             the ArchiveUnit under which to search
     * @param formats          the searched format identifiers, {@link #OTHER_FORMATS} standing for
     *                         the ones of no category, none for all the formats
     * @param min              the minimum size, 0 for none
     * @param max              the maximum size, Long.MAX_VALUE for none
     * @param formatByCategory the format identifiers of each category
     * @return the found BinaryDataObjects by ArchiveUnit, in tree order
     */
    public static LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> technicalSearch(
        ArchiveUnit from,
        List<String> formats,
        long min,
        long max,
        Map<String, List<String>> formatByCategory
    ) {
        List<String> searchedFormats = new ArrayList<>(formats);
        boolean searchOthers = searchedFormats.remove(OTHER_FORMATS);
        List<String> categorizedFormats = searchOthers
            ? formatByCategory.values().stream().flatMap(List::stream).collect(Collectors.toList())
            : null;
        boolean allFormats = searchedFormats.isEmpty() && !searchOthers;
        boolean sizeFiltered = (min != 0) || (max != Long.MAX_VALUE);

        Function<BinaryDataObject, Boolean> isSearched = bdo -> {
            IntegerType size = bdo.getMetadataSize();
            if (sizeFiltered && ((size == null) || (size.getValue() < min) || (size.getValue() > max))) return false;
            if (allFormats) return true;
            FormatIdentification formatIdentification = bdo.getMetadataFormatIdentification();
            if (formatIdentification == null) return false;
            String formatId = formatIdentification.getSimpleMetadata("FormatId");
            return searchedFormats.contains(formatId) || (searchOthers && !categorizedFormats.contains(formatId));
        };

        from.getDataObjectPackage().resetTouchedInDataObjectPackageIdMap();
        LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> result = new LinkedHashMap<>();
        technicalSearchUnder(from, isSearched, result);
        return result;
    }

    private static void technicalSearchUnder(
        ArchiveUnit au,
        Function<BinaryDataObject, Boolean> isSearched,
        LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> result
    ) {
        DataObjectPackage dataObjectPackage = au.getDataObjectPackage();
        for (ArchiveUnit childUnit : au.getChildrenAuList().getArchiveUnitList()) {
            if (dataObjectPackage.isTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId())) continue;
            List<BinaryDataObject> bdos = new ArrayList<>();
            for (DataObject dataObject : childUnit.getDataObjectRefList().getDataObjectList()) {
                if (dataObject instanceof BinaryDataObject) bdos.add((BinaryDataObject) dataObject);
                else if (dataObject instanceof DataObjectGroup) bdos.addAll(
                    ((DataObjectGroup) dataObject).getBinaryDataObjectList()
                );
            }
            for (BinaryDataObject bdo : bdos) if (isSearched.apply(bdo)) result
                .computeIfAbsent(childUnit, k -> new ArrayList<>())
                .add(bdo);
            dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());
            technicalSearchUnder(childUnit, isSearched, result);
        }
    }

    /**
     * Finds the ArchiveUnits of a work whose end date is before their start date, after a
     * reindexation if its export context asks it.
     *
     * @param work the work
     * @param spl  the progress logger, or null, logging each found ArchiveUnit
     * @return the found ArchiveUnits
     * @throws SEDALibException if the content of an ArchiveUnit can't be read
     */
    public static List<ArchiveUnit> findEndDatesBeforeStartDates(Work work, SEDALibProgressLogger spl)
        throws SEDALibException {
        if (work.getExportContext().isReindex()) work.getDataObjectPackage().regenerateContinuousIds();
        List<ArchiveUnit> result = new ArrayList<>();
        for (Map.Entry<String, ArchiveUnit> entry : work
            .getDataObjectPackage()
            .getAuInDataObjectPackageIdMap()
            .entrySet()) {
            ArchiveUnit archiveUnit = entry.getValue();
            String startDateString = archiveUnit.getContent().getSimpleMetadata("StartDate");
            String endDateString = archiveUnit.getContent().getSimpleMetadata("EndDate");
            if ((endDateString == null) || (startDateString == null)) continue;
            Date startDate = ISODateTimeFormat.dateTimeParser().parseDateTime(startDateString).toDate();
            Date endDate = ISODateTimeFormat.dateTimeParser().parseDateTime(endDateString).toDate();
            if (startDate.after(endDate)) {
                result.add(archiveUnit);
                doProgressLogWithoutInterruption(
                    spl,
                    GLOBAL,
                    "resip: l'unité archivistique " +
                    entry.getKey() +
                    " contient une erreur : la date de fin est antérieure à la date de début",
                    null
                );
            }
        }
        return result;
    }

    /**
     * Computes the size statistics of the BinaryDataObjects of a package, by format category and
     * for all formats, the BinaryDataObjects of no category being in the one of {@link #OTHER_FORMATS}.
     *
     * @param dataObjectPackage the package
     * @param formatByCategory  the format identifiers of each category
     * @param spl               the progress logger, or null
     * @return the statistics of each category, then of all formats
     * @throws InterruptedException if the computation is interrupted
     */
    public static List<StatisticData> statistics(
        DataObjectPackage dataObjectPackage,
        LinkedHashMap<String, List<String>> formatByCategory,
        SEDALibProgressLogger spl
    ) throws InterruptedException {
        LinkedHashMap<String, List<Long>> sizeByCategory = new LinkedHashMap<>();
        String otherCategory = null;
        for (Map.Entry<String, List<String>> category : formatByCategory.entrySet()) {
            sizeByCategory.put(category.getKey(), new ArrayList<>());
            if (category.getValue().contains(OTHER_FORMATS)) otherCategory = category.getKey();
        }
        sizeByCategory.put(ALL_FORMATS, new ArrayList<>());
        int counter = 0;
        for (BinaryDataObject bdo : dataObjectPackage.getBdoInDataObjectPackageIdMap().values()) {
            String category = null;
            FormatIdentification formatIdentification = bdo.getMetadataFormatIdentification();
            if (formatIdentification != null) category = categoryOf(
                formatIdentification.getSimpleMetadata("FormatId"),
                formatByCategory
            );
            if (category == null) category = otherCategory;
            IntegerType size = bdo.getMetadataSize();
            if (size != null) {
                if (category != null) sizeByCategory.get(category).add(size.getValue());
                sizeByCategory.get(ALL_FORMATS).add(size.getValue());
            }
            counter++;
            doProgressLogIfStep(
                spl,
                OBJECTS_GROUP,
                counter,
                "resip: " + counter + " objets pris en compte dans les statistiques"
            );
        }
        return sizeByCategory
            .entrySet()
            .stream()
            .map(e -> new StatisticData(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    private static String categoryOf(String formatId, Map<String, List<String>> formatByCategory) {
        for (Map.Entry<String, List<String>> category : formatByCategory.entrySet()) {
            if (category.getValue().contains(formatId)) return category.getKey();
        }
        return null;
    }

    /**
     * The sets of similar DataObjectGroups of a package, by their common key.
     */
    public static final class DuplicatesResult {

        private final LinkedHashMap<String, List<DataObjectGroup>> dataObjectGroupsByKey;
        private final HashMap<String, List<ArchiveUnit>> archiveUnitsByKey;

        private DuplicatesResult(
            LinkedHashMap<String, List<DataObjectGroup>> dataObjectGroupsByKey,
            HashMap<String, List<ArchiveUnit>> archiveUnitsByKey
        ) {
            this.dataObjectGroupsByKey = dataObjectGroupsByKey;
            this.archiveUnitsByKey = archiveUnitsByKey;
        }

        /**
         * @return the similar DataObjectGroups of each set, in tree order
         */
        public LinkedHashMap<String, List<DataObjectGroup>> getDataObjectGroupsByKey() {
            return dataObjectGroupsByKey;
        }

        /**
         * @return the ArchiveUnits referring to the DataObjectGroups of each set
         */
        public HashMap<String, List<ArchiveUnit>> getArchiveUnitsByKey() {
            return archiveUnitsByKey;
        }
    }

    /**
     * Finds the DataObjectGroups of a package which are similar, by their LogBook and the chosen
     * characteristics of their DataObjects, and referred to by more than one ArchiveUnit.
     *
     * @param dataObjectPackage the package
     * @param binaryHash        true to compare the digests of the BinaryDataObjects
     * @param binaryFilename    true to compare the filenames of the BinaryDataObjects
     * @param physicalAllMD     true to compare all the metadata of the PhysicalDataObjects
     * @param spl               the progress logger, or null
     * @return the sets of similar DataObjectGroups
     * @throws SEDALibException     if a PhysicalDataObject can't be written to be compared
     * @throws InterruptedException if the search is interrupted
     */
    public static DuplicatesResult duplicates(
        DataObjectPackage dataObjectPackage,
        boolean binaryHash,
        boolean binaryFilename,
        boolean physicalAllMD,
        SEDALibProgressLogger spl
    ) throws SEDALibException, InterruptedException {
        doProgressLog(
            spl,
            GLOBAL,
            "resip: recherche de doublons ( " +
            (binaryHash ? "hachage de fichier " : "") +
            (binaryFilename ? "nom de fichier " : "") +
            (physicalAllMD ? "toute MD physique " : "") +
            ")",
            null
        );
        HashMap<String, List<DataObjectGroup>> dogsByKey = new HashMap<>();
        HashMap<DataObjectGroup, String> keyByDog = new HashMap<>();
        int counter = 0;
        for (DataObjectGroup dog : dataObjectPackage.getDogInDataObjectPackageIdMap().values()) {
            StringBuilder key = new StringBuilder(dog.logBook == null ? "" : dog.logBook.toString());
            for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
                if (binaryHash) {
                    DigestType md = bdo.getMetadataMessageDigest();
                    key.append("|BDO=").append(md == null ? null : md.getValue());
                }
                if (binaryFilename) {
                    FileInfo fi = bdo.getMetadataFileInfo();
                    key.append("|").append(fi == null ? null : fi.getSimpleMetadata("Filename"));
                }
            }
            for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList()) {
                if (physicalAllMD) key.append("|PDO=").append(pdo.toSedaXmlFragments());
            }
            keyByDog.put(dog, key.toString());
            dogsByKey.computeIfAbsent(key.toString(), k -> new ArrayList<>()).add(dog);
            counter++;
            doProgressLogIfStep(spl, OBJECTS, counter, "resip: " + counter + " groupes d'objets comparés");
        }

        // in tree order, with the ArchiveUnits referring to them
        dataObjectPackage.resetTouchedInDataObjectPackageIdMap();
        LinkedHashMap<String, List<DataObjectGroup>> sortedDogsByKey = new LinkedHashMap<>();
        HashMap<String, List<ArchiveUnit>> sortedAusByKey = new HashMap<>();
        for (String key : dogsByKey.keySet()) {
            sortedDogsByKey.put(key, new ArrayList<>());
            sortedAusByKey.put(key, new ArrayList<>());
        }
        followTree(
            dataObjectPackage,
            dataObjectPackage.getGhostRootAu(),
            dogsByKey,
            keyByDog,
            sortedDogsByKey,
            sortedAusByKey
        );

        // a set referred to by a single ArchiveUnit has no duplicate
        for (Iterator<Map.Entry<String, List<ArchiveUnit>>> it = sortedAusByKey.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, List<ArchiveUnit>> entry = it.next();
            if (entry.getValue().size() == 1) {
                it.remove();
                sortedDogsByKey.remove(entry.getKey());
            }
        }
        doProgressLog(spl, GLOBAL, "resip: " + sortedDogsByKey.size() + " lots de groupes d'objets semblables", null);
        return new DuplicatesResult(sortedDogsByKey, sortedAusByKey);
    }

    private static void followTree(
        DataObjectPackage dataObjectPackage,
        ArchiveUnit au,
        HashMap<String, List<DataObjectGroup>> dogsByKey,
        HashMap<DataObjectGroup, String> keyByDog,
        LinkedHashMap<String, List<DataObjectGroup>> sortedDogsByKey,
        HashMap<String, List<ArchiveUnit>> sortedAusByKey
    ) {
        for (ArchiveUnit childUnit : au.getChildrenAuList().getArchiveUnitList()) {
            if (dataObjectPackage.isTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId())) continue;
            for (DataObject dataObject : childUnit.getDataObjectRefList().getDataObjectList()) {
                if (dataObject instanceof DataObjectGroup) {
                    String key = keyByDog.get(dataObject);
                    if (dogsByKey.get(key).contains(dataObject)) {
                        dogsByKey.get(key).remove(dataObject);
                        sortedDogsByKey.get(key).add((DataObjectGroup) dataObject);
                        sortedAusByKey.get(key).add(childUnit);
                    } else if (sortedDogsByKey.get(key).contains(dataObject)) sortedAusByKey.get(key).add(childUnit);
                }
            }
            dataObjectPackage.addTouchedInDataObjectPackageId(childUnit.getInDataObjectPackageId());
            followTree(dataObjectPackage, childUnit, dogsByKey, keyByDog, sortedDogsByKey, sortedAusByKey);
        }
    }
}
