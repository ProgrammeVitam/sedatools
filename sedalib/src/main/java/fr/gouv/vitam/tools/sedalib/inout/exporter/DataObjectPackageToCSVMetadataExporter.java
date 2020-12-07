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
 * circulated by CEA, CNRS and INRIA dataObjectPackage the following URL "http://www.cecill.info".
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
package fr.gouv.vitam.tools.sedalib.inout.exporter;

import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class DataObjectPackageToCSVMetadataExporter.
 * <p>
 * It will export a DataObjectPackage to a set of files, the disk hierarchy and all metadata defined in a csv file,
 * optionally gathered in a zip file. The disk hierarchy is organised as the DataObjectPackage ArchiveUnit tree.
 * The export follows theese few rules:
 * <ul>
 * <li>An ArchiveUnit with child AUs is exported
 * as a directory with name being the Title (shorten if more than maxNameSize long</li>
 * <li>An AU with no child AUs and only one object file is exported as a file with the object filename as name</li>
 * <li>In other cases an object with only one usage_version is exported as a file with the object filename as name</li>
 * <li>Each usage_version of an object is exported as a file with the object filename being derived as name inserting
 * before the extension of a short field composed with '_', the first letter of the Usage and with the Version number</li>
 * <li>For any export name, directory or file, if there is a collision the name is made uniq by adding '_xmlID'</li>
 * </ul>
 * With theese rules in simple cases the disk hierarchy is very simple and very near from the original version if this
 * has been created from a disk hierarchy, but it's also possible to export any structure.
 * <p>
 * The csv has one header line at the beginning there is one column with 'File' which contains the file (or directory)
 * name which is also used as uniq ID and which defines the hierarchy as the file hierarchy.
 * After that the columns defines metadata path, each tag being separeted by a dot, or an attribute ('attr' column) of the metadata in the previous colum.
 * <p>
 * For example: Writer.0.FullName|Description.0|Description.0.attr defines first colum of values to put in
 * &lt;Writer&gt;&lt;FullName&gt;VALUE&lt;/FullName&gt;&lt;/Writer&gt;, then values to put in &lt;Description&gt;VALUE&lt;/Description&gt;
 * and finally attributes to put in &lt;Description&gt; if any.
 * <p>
 * Many values of a tag can be defined in csv, for example 2 Writers, so it's written as Writer.0 and Writer.1, and FullName is Writer.0.FullName.
 */
public class DataObjectPackageToCSVMetadataExporter {
    /**
     * The data object package.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * The encoding format.
     */
    private String encoding;

    /**
     * The separator char.
     */
    private char separator;

    /**
     * The usage_version selection mode
     */
    private int usageVersionSelectionMode;

    /**
     * The First dataobject selection mode.
     */
    static public final int FIRST_DATAOBJECT = 1;
    /**
     * The Last dataobject selection mode.
     */
    static public final int LAST_DATAOBJECT = 2;
    /**
     * The All dataobjects selection mode.
     */
    static public final int ALL_DATAOBJECTS = 3;

    /**
     * The max name size used to limit directory names.
     */
    private int maxNameSize;

    /**
     * The progress logger.
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * The export action type
     */
    private int exportAction;

    private static final int ALL_DISK_EXPORT = 1;
    private static final int METADATA_FILE_EXPORT = 2;
    private static final int ALL_ZIP_EXPORT = 3;

    /**
     * The start and end instants, for duration computation.
     */
    private Instant start, end;

    /**
     * The output operation defined context.
     */
    private String zipFileName;
    private String csvMetadataFileName;
    private Path rootPath;
    private boolean fileExportFlag;

    /**
     * The output operation data.
     */
    private ZipOutputStream zipOS;
    private HashMap<ArchiveUnit, Path> auRelativePathMap;
    private HashSet<String> relativePathStringSet;
    private List<String> headerNames;
    private ByteArrayOutputStream csvBAOS;
    private PrintStream csvPrintStream;

    /**
     * Instantiates a new DataObjectPackage to csv metadata exporter.
     *
     * @param dataObjectPackage         the data object package
     * @param encoding                  the encoding
     * @param separator                 the separator
     * @param usageVersionSelectionMode the usage version selection mode
     * @param maxNameSize               the max name size
     * @param sedaLibProgressLogger     the progress logger or null if no progress log expected
     */
    public DataObjectPackageToCSVMetadataExporter(DataObjectPackage dataObjectPackage, String encoding,
                                                  char separator, int usageVersionSelectionMode, int maxNameSize,
                                                  SEDALibProgressLogger sedaLibProgressLogger) {
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.dataObjectPackage = dataObjectPackage;
        this.encoding = encoding;
        this.separator = separator;
        this.usageVersionSelectionMode = usageVersionSelectionMode;
        this.maxNameSize = maxNameSize;
    }

    // compute the number of appearance of one type of metadata in all headers name
    // for example for Writer if there is Writer.0.FullName and Writer.1.Identifier this will be 2
    // 0 is for metadata present but without number derivation
    // -1 is for not present metadata
    private int getMaxRank(Set<String> headerNames, String name) {
        if (headerNames.contains(name))
            return 0;
        int pos = name.split("\\.").length;
        int maxRank = -1;
        for (String tmp : headerNames) {
            if (tmp.startsWith(name)) {
                if (maxRank == -1) maxRank = 0;
                String[] splittedTmp = tmp.split("\\.");
                if (splittedTmp.length > pos) {
                    try {
                        maxRank = Math.max(maxRank, Integer.parseInt(splittedTmp[pos]) + 1);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return maxRank;
    }


    // extract and sort headernames for RuleType metadata
    private List<String> getRuleTypeHeaderNames(Set<String> headerNames, String ruleName) {
        List<String> ruleHeaderNames = new ArrayList<String>();
        int rank = 0;
        while (headerNames.contains(ruleName + ".Rule." + Integer.toString(rank))) {
            ruleHeaderNames.add(ruleName + ".Rule." + Integer.toString(rank));
            if (headerNames.contains(ruleName + ".StartDate." + Integer.toString(rank)))
                ruleHeaderNames.add(ruleName + ".StartDate." + Integer.toString(rank));
            rank++;
        }
        if (headerNames.contains(ruleName + ".PreventInheritance"))
            ruleHeaderNames.add(ruleName + ".PreventInheritance");
        rank = 0;
        while (headerNames.contains(ruleName + ".RefNonRuleId." + Integer.toString(rank))) {
            ruleHeaderNames.add(ruleName + ".RefNonRuleId." + Integer.toString(rank));
            rank++;
        }
        if (headerNames.contains(ruleName + ".FinalAction"))
            ruleHeaderNames.add(ruleName + ".FinalAction");
        return ruleHeaderNames;
    }

    // extract and sort headernames for ClassificationRule metadata
    private List<String> getClassificationRuleHeaderNames(Set<String> headerNames, String ruleName) {
        List<String> ruleHeaderNames = getRuleTypeHeaderNames(headerNames, ruleName);

        if (headerNames.contains(ruleName + ".ClassificationLevel"))
            ruleHeaderNames.add(ruleName + ".ClassificationLevel");
        if (headerNames.contains(ruleName + ".ClassificationOwner"))
            ruleHeaderNames.add(ruleName + ".ClassificationOwner");
        if (headerNames.contains(ruleName + ".ClassificationReassessingDate"))
            ruleHeaderNames.add(ruleName + ".ClassificationReassessingDate");
        if (headerNames.contains(ruleName + ".NeedReassessingAuthorization"))
            ruleHeaderNames.add(ruleName + ".NeedReassessingAuthorization");

        return ruleHeaderNames;
    }


    // extract and sort headernames by there defined order in composed types
    private List<String> getSortedHeaderNames(List<String> sortedHeaderNames, Set<String> headerNames,
                                              String prefixHeaderName, String xmlElementName, Class metadataClass) {
        String currentName = prefixHeaderName + (prefixHeaderName.isEmpty() ? "" : ".") + xmlElementName;
        String infix;
        int maxRank = getMaxRank(headerNames, currentName);
        if (maxRank != -1) {
            {
                if (maxRank == 0)
                    infix = "";
                else infix = ".0";
                int i = 0;
                do {
                    // manage special RuleType with rule/startdata special order
                    if (RuleType.class.isAssignableFrom(metadataClass)) {
                        List<String> ruleHeaderNames;
                        ruleHeaderNames = getRuleTypeHeaderNames(headerNames, currentName + infix);
                        sortedHeaderNames.addAll(ruleHeaderNames);
                        headerNames.removeAll(ruleHeaderNames);
                    } else if (ComplexListType.class.isAssignableFrom(metadataClass)) {
                        for (Map.Entry<String, ComplexListMetadataKind> e : ComplexListType.getMetadataMap(metadataClass).entrySet()) {
                            getSortedHeaderNames(sortedHeaderNames, headerNames, currentName + infix,
                                    e.getKey(), e.getValue().metadataClass);
                        }
                        // add extensions if any
                        TreeSet<String> extensions = new TreeSet<String>();
                        for (String header : headerNames) {
                            if (header.startsWith(currentName + "."))
                                extensions.add(header);
                        }
                        headerNames.removeAll(extensions);
                        sortedHeaderNames.addAll(extensions);
                    }
                    // at last simple types
                    else {
                        sortedHeaderNames.add(currentName + infix);
                        headerNames.remove(currentName + infix);
                        if (headerNames.contains(currentName + infix + ".attr")) {
                            sortedHeaderNames.add(currentName + infix + ".attr");
                            headerNames.remove(currentName + infix + ".attr");
                        }
                    }
                    i++;
                    infix = "." + i;
                } while (i < maxRank);
            }
        }
        return sortedHeaderNames;
    }

    // determine the csv header line by extracting metadata names from all ArchiveUnits, sorting this list in SEDA order
    // and simplifying the unnecessary .0
    private void computeCsvHeader() throws SEDALibException {
        Set<String> headerNames = new HashSet<String>();
        List<String> sortedHeaderNames;
        for (ArchiveUnit au : dataObjectPackage.getAuInDataObjectPackageIdMap().values()) {
            Management management = au.getManagement();
            if (management != null)
                headerNames.addAll(management.externToCsvList().keySet());
            headerNames.addAll(au.getContent().externToCsvList(dataObjectPackage.getExportMetadataList()).keySet());
        }
        sortedHeaderNames = getSortedHeaderNames(new ArrayList<String>(), headerNames, "", "Content",
                Content.class);
        sortedHeaderNames.addAll(getSortedHeaderNames(new ArrayList<String>(), headerNames, "", "Management",
                Management.class));
        this.headerNames = sortedHeaderNames;
    }

    // simplify header line in the csv removing unnecessary .0
    private List<String> getSimplifiedHeaderNames() {
        List<String> simplifiedHeaderNames = new ArrayList<String>(headerNames);
        for (int i = 0; i < simplifiedHeaderNames.size(); i++) {
            String header = simplifiedHeaderNames.get(i);
            int pos = 0;
            String headerRoot = "";
            while (header.indexOf(".0", pos) != -1) {
                pos = header.indexOf(".0", pos);
                headerRoot = header.substring(0, pos);
                boolean onlyZero = true;
                //on each headerRoot.0 test if there's also a headerRoot.1.yyy
                for (int j = i + 1; j < simplifiedHeaderNames.size(); j++) {
                    if (simplifiedHeaderNames.get(j).startsWith(headerRoot + ".1")) {
                        onlyZero = false;
                        break;
                    }
                }
                //if there is no headerRoot.1.yyy replace all headerRoot.0.yyy by headerRoot.yyy
                if (onlyZero) {
                    for (int k = i; k < simplifiedHeaderNames.size(); k++) {
                        String toSimplifyheader = simplifiedHeaderNames.get(k);
                        if (toSimplifyheader.startsWith(headerRoot + ".")) {
                            toSimplifyheader = headerRoot + toSimplifyheader.substring(headerRoot.length() + 2);
                            simplifiedHeaderNames.set(k, toSimplifyheader);
                        }
                    }
                }
                pos++;
            }
        }
        return simplifiedHeaderNames;
    }

    // print header line in the csv, after simplifying header names (remove unnecessary .0)
    private void printCsvHeader() {
        List<String> simplifiedHeaderNames = getSimplifiedHeaderNames();
        csvPrintStream.print("File");
        for (String header : simplifiedHeaderNames)
            csvPrintStream.print(separator + header);
        csvPrintStream.println();
    }

    // generate one ArchiveUnit line in the csv
    private void generateCsvLine(ArchiveUnit au, Path auRelativePath) throws SEDALibException {
        LinkedHashMap<String, String> contentMetadataHashMap, managementMetadataHashMap = null;

        String value = "\"" + auRelativePath.toString().replace("\"", "\"\"") + "\"";
        csvPrintStream.print(value);
        contentMetadataHashMap = au.getContent().externToCsvList(dataObjectPackage.getExportMetadataList());
        Management management = au.getManagement();
        if (management != null)
            managementMetadataHashMap = management.externToCsvList();
        for (String header : headerNames) {
            value = contentMetadataHashMap.get(header);
            if ((value == null) && (managementMetadataHashMap != null))
                value = managementMetadataHashMap.get(header);
            if (value == null)
                value = "";
            else
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            csvPrintStream.print(separator + value);
        }
        csvPrintStream.println();
    }

    // get the best Usage_Version object in a list of objects. First find the best Usage and then find the first or
    // last version of this usage depending on firstFlag
    private BinaryDataObject getBestUsageVersionObject(List<BinaryDataObject> objectList, boolean firstFlag) throws InterruptedException {
        int rank, version;
        TreeMap<Integer, BinaryDataObject> rankMap = new TreeMap<Integer, BinaryDataObject>();
        for (BinaryDataObject bdo : objectList) {
            if ((bdo.dataObjectVersion == null) || (bdo.dataObjectVersion.getValue().isEmpty())) {
                doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_WARNINGS, "Un objet binaire n'a pas d'usage_version," +
                        " il ne peut être choisi pour l'extraction", null);
                continue;
            }
            String[] usageVersion = bdo.dataObjectVersion.getValue().split("_");
            switch (usageVersion[0]) {
                case "BinaryMaster":
                    rank = 0;
                    break;
                case "Dissemination":
                    rank = 1;
                    break;
                case "TextContent":
                    rank = 2;
                    break;
                case "Thumbnail":
                    rank = 3;
                    break;
                default:
                    rank = 4;
            }
            if (usageVersion.length == 1)
                version = 1;
            else {
                try {
                    version = Integer.parseInt(usageVersion[1]);
                } catch (NumberFormatException e) {
                    doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_WARNINGS, "Un objet binaire n'a pas d'usage_version," +
                            " il ne peut être choisi pour l'extraction", e);
                    continue;
                }
            }
            rankMap.put(rank * 1000 + version, bdo);
        }
        //no result
        if (rankMap.size() == 0)
            return null;
        //the first version of best usage
        if (firstFlag)
            return rankMap.firstEntry().getValue();
        //the last version of best usage
        rank = rankMap.firstKey() / 1000;
        BinaryDataObject bdo = null;
        for (Map.Entry<Integer, BinaryDataObject> e : rankMap.entrySet()) {
            if (rank != e.getKey() / 1000) break;
            bdo = e.getValue();
        }
        return bdo;
    }

    // get the list of all objects in an ArchiveUnit (with DataObjectGroup or not)
    private List<BinaryDataObject> getArchiveUnitObjectList(ArchiveUnit au) throws InterruptedException {
        if ((au.getDataObjectRefList() == null) || (au.getDataObjectRefList().getCount() == 0))
            return null;
        ArrayList<BinaryDataObject> objectList = new ArrayList<BinaryDataObject>();
        for (DataObject dataObject : au.getDataObjectRefList().getDataObjectList()) {
            if (dataObject instanceof BinaryDataObject)
                objectList.add((BinaryDataObject) dataObject);
            else if (dataObject instanceof DataObjectGroup)
                objectList.addAll(((DataObjectGroup) dataObject).getBinaryDataObjectList());
        }
        if (usageVersionSelectionMode == ALL_DATAOBJECTS)
            return objectList;
        else
            return Collections.singletonList(getBestUsageVersionObject(objectList, usageVersionSelectionMode == FIRST_DATAOBJECT));
    }

    // strip a String of all characters not allowed in a file name, and from ending points and space
    private String stripFileName(String fileName) {
        String filteredName = fileName.replaceAll("[\\/|\\\\|\\*|\\:|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]", "_");
        while (filteredName.endsWith(".") || filteredName.endsWith(" "))
            filteredName = filteredName.substring(0, filteredName.length() - 1);
        return filteredName;
    }

    // Construct directory name for ArchiveUnit and insert id if already exists.
    private String constructArchiveUnitDirectoryName(Path auRrelativePath, ArchiveUnit au) throws SEDALibException {
        String dirName = "";
        if (au.getContent() != null) {
            dirName = au.getContent().getSimpleMetadata("Title");
            if (dirName == null)
                dirName = "NoTitle";
            if ((maxNameSize > 0) && (dirName.length() > maxNameSize))
                dirName = dirName.substring(0, maxNameSize);
            dirName = stripFileName(dirName);
            dirName = dirName.trim();
            if (fileExists(auRrelativePath.resolve(dirName))) {
                String id = stripFileName("-" + au.getInDataObjectPackageId());
                if (maxNameSize <= 0)
                    dirName += id;
                else if (id.length() >= maxNameSize)
                    dirName = id;
                else if (id.length() + dirName.length() <= maxNameSize)
                    dirName += id;
                else
                    dirName = dirName.substring(0, maxNameSize - id.length()) + id;
            }
        }

        return dirName;
    }

    // Construct file name for Object, either uniq or in a list of different usage_version and insert id if already
    // exists.
    private String constructObjectFileName(Path auRrelativePath, BinaryDataObject bdo, boolean uniqFlag) {
        String filename = null, name, ext;
        if (bdo.fileInfo != null)
            filename = bdo.fileInfo.getSimpleMetadata("Filename");
        if (filename == null)
            filename = "undefined";
        int point = filename.lastIndexOf('.');
        if (point == -1) {
            name = filename;
            ext = "";
        } else {
            name = filename.substring(0, point);
            ext = filename.substring(point);
        }

        if (!uniqFlag) {
            String[] usageVersion;
            if (bdo.dataObjectVersion == null)
                usageVersion = "undefined".split("_");
            else
                usageVersion = bdo.dataObjectVersion.getValue().split("_");
            String shortUsageVersion;
            if (usageVersion[0].isEmpty())
                shortUsageVersion = "Z";
            else
                shortUsageVersion = usageVersion[0].substring(0, 1);
            shortUsageVersion += usageVersion[1];
            ext = "_" + shortUsageVersion + ext;
        }

        filename = stripFileName(name + ext);
        if (fileExists(auRrelativePath.resolve(filename)))
            filename = stripFileName(name + "-" + bdo.getInDataObjectPackageId() + ext);

        return filename;
    }

    private boolean fileExists(Path relativePath) {
        return relativePathStringSet.contains(relativePath.toString());
    }

    private void createDirectories(Path relativePath) throws SEDALibException {
        if (zipOS == null)
            try {
                Files.createDirectories((relativePath == null ? rootPath : rootPath.resolve(relativePath)));
            } catch (Exception e) {
                throw new SEDALibException(
                        "Création du répertoire [" + (relativePath == null ? rootPath : rootPath.resolve(relativePath)) + "] impossible", e);
            }
        else if ((relativePath != null) && !relativePathStringSet.contains(relativePath.toString()))
            try {
                ZipEntry e = new ZipEntry((relativePath.toString() + File.separator).replace('\\', '/'));
                zipOS.putNextEntry(e);
                zipOS.closeEntry();
                //       } catch (ZipException ignored) {
            } catch (IOException e) {
                throw new SEDALibException(
                        "Création du répertoire [" + relativePath.toString() +
                                "] dans le zip [" + rootPath.resolve(zipFileName).toString() + "] impossible", e);
            }

        if (relativePath != null)
            relativePathStringSet.add(relativePath.toString());
    }

    private void writeString(Path relativePath, String content) throws SEDALibException {
        if (zipOS == null)
            try {
                Files.write(rootPath.resolve(relativePath), content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new SEDALibException(
                        "Ecriture du fichier  [" + rootPath.resolve(relativePath).toString() + "] impossible", e);
            }
        else
            try {
                ZipEntry e = new ZipEntry(relativePath.toString().replace('\\', '/'));
                zipOS.putNextEntry(e);
                zipOS.write(content.getBytes(StandardCharsets.UTF_8));
                zipOS.closeEntry();
            } catch (IOException e) {
                throw new SEDALibException(
                        "Ecriture du fichier [" + relativePath.toString() +
                                "] dans le zip [" + rootPath.resolve(zipFileName).toString() + "] impossible", e);
            }
        relativePathStringSet.add(relativePath.toString());
    }

    private void copyFile(Path originPath, Path relativePath) throws SEDALibException {
        if (zipOS == null)
            try {
                Files.copy(originPath, rootPath.resolve(relativePath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new SEDALibException(
                        "Ecriture du fichier  [" + rootPath.resolve(relativePath).toString() + "] impossible", e);
            }
        else
            try {
                ZipEntry e = new ZipEntry(relativePath.toString().replace('\\', '/'));
                zipOS.putNextEntry(e);
                FileInputStream fis = new FileInputStream(originPath.toFile());
                int l;
                byte[] buffer = new byte[65536];
                while ((l = fis.read(buffer)) != -1)
                    zipOS.write(buffer, 0, l);
                zipOS.closeEntry();
            } catch (IOException e) {
                throw new SEDALibException(
                        "Ecriture du fichier [" + relativePath.toString() +
                                "] dans le zip [" + rootPath.resolve(zipFileName).toString() + "] impossible", e);
            }
        relativePathStringSet.add(relativePath.toString());
    }

    // export in containerPath a link to au an already created ArchiveUnit export
    private void exportLink(ArchiveUnit au, Path auRelativePath) throws SEDALibException, InterruptedException {
        Path originAURelativePath = auRelativePathMap.get(au);
        Path auPath = rootPath.resolve(auRelativePath).resolve(originAURelativePath.getFileName().toString() + ".link");
        boolean linkFlag = false;
        if (zipOS == null)
            try {
                createDirectories(auPath.getParent());
                Files.createSymbolicLink(auPath.toAbsolutePath(),
                        auPath.getParent().relativize(rootPath.resolve(originAURelativePath)));
                linkFlag = true;
            } catch (IOException e) {
                doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_WARNINGS, "Lien vers [" +
                        rootPath.resolve(originAURelativePath).toString() + "] n'a pas pu être créé", e);
            }
        if (!linkFlag) {
            writeString(rootPath.toAbsolutePath().relativize(auPath), "Link to " + originAURelativePath.toString());
        }
    }

    // export all objects in auPath, return last used filename
    private String exportObjectList(Path auRelativePath, List<BinaryDataObject> objectList) throws SEDALibException {
        String filename = null;

        if (fileExportFlag)
            createDirectories(auRelativePath);

        if ((objectList != null) && (objectList.size() > 0)) {
            for (BinaryDataObject bdo : objectList) {
                filename = constructObjectFileName(auRelativePath, bdo, objectList.size() == 1);
                if (fileExportFlag)
                    copyFile(bdo.getOnDiskPath(), auRelativePath.resolve(filename));
            }
        }
        return filename;
    }

    // Recursively export all ArchiveUnit files and metadata to disk.
    private void exportArchiveUnit(ArchiveUnit au, Path relativePath)
            throws SEDALibException, InterruptedException {
        Path auRelativePath;
        String filename;

        if (auRelativePathMap.containsKey(au)) {
            if (fileExportFlag) exportLink(au, relativePath);
            return;
        }

        List<BinaryDataObject> objectList = getArchiveUnitObjectList(au);
        // if not only a file ArchiveUnit create a named directory with the ArchiveUnit Title
        if (((au.getChildrenAuList() != null) && (au.getChildrenAuList().getCount() != 0)) ||
                ((objectList == null) || (objectList.size() > 1) || (objectList.size() == 0))) {
            auRelativePath = relativePath.resolve(constructArchiveUnitDirectoryName(relativePath, au));
        } else auRelativePath = relativePath;
        filename = exportObjectList(auRelativePath, objectList);
        // if a file ArchiveUnit the kept path for links is the file path
        if (auRelativePath == relativePath)
            auRelativePath = auRelativePath.resolve(filename);
        auRelativePathMap.put(au, auRelativePath);

        // recursively export
        if ((au.getChildrenAuList() != null) && (au.getChildrenAuList().getCount() != 0)) {
            for (ArchiveUnit childAU : au.getChildrenAuList().getArchiveUnitList())
                exportArchiveUnit(childAU, auRelativePath);
        }

        generateCsvLine(au, auRelativePath);

        int counter = dataObjectPackage.getNextInOutCounter();
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
                Integer.toString(counter) + " ArchiveUnit exportées");
    }

    private String getDescription(Date d) throws SEDALibException, InterruptedException {
        String log = "Début de l'export csv simplifié";
        if (!fileExportFlag)
            log += " (csv seul)\n";
        else
            log += " (csv et fichiers)\n";
        if (zipFileName != null)
            log += "dans le zip [" + rootPath.resolve(zipFileName).toString() + "]";
        else
            log += "dans le répertoire [" + rootPath.toString() + "]";
        log += " date=" + DateFormat.getDateTimeInstance().format(d);
        return log;
    }

    private void defineZipOutputStreamOrNull(Path rootPath, String zipFileName) throws SEDALibException {
        if (zipFileName == null)
            zipOS = null;
        else
            try {
                zipOS = new ZipOutputStream(new FileOutputStream(rootPath.resolve(zipFileName).toFile()));
            } catch (IOException e) {
                throw new SEDALibException(
                        "Création du conteneur zip [" + rootPath.resolve(zipFileName).toString() + "] impossible", e);
            }
    }

    private void createCsvBAOSPrintScream() throws SEDALibException {
        try {
            csvBAOS = new ByteArrayOutputStream();
            csvPrintStream = new PrintStream(csvBAOS, true, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new SEDALibException("Encodage [" + encoding + "] inconnu", e);
        }
    }

    private void finaliseWithCsvMetadataFile() throws SEDALibException {
        csvPrintStream.flush();
        if (zipOS != null)
            try {
                ZipEntry e = new ZipEntry(csvMetadataFileName);
                zipOS.putNextEntry(e);
                zipOS.write(csvBAOS.toByteArray());
                zipOS.closeEntry();
                zipOS.close();
                zipOS = null;
            } catch (IOException e) {
                throw new SEDALibException(
                        "Finalisation du conteneur zip [" + rootPath.resolve(zipFileName).toString() +
                                "] avec sauvegarde du fichier de métadonnées [" + csvMetadataFileName + "] impossible", e);
            }
        else {
            try {
                FileUtils.writeByteArrayToFile(rootPath.resolve(csvMetadataFileName).toFile(), csvBAOS.toByteArray());
            } catch (IOException e) {
                throw new SEDALibException(
                        "Finalisation de l'export en [" + rootPath.toString() +
                                "] avec sauvegarde du fichier de métadonnées [" + csvMetadataFileName + "] impossible", e);
            }
        }
        csvPrintStream.close();
        csvPrintStream = null;
        csvBAOS = null;
    }

    // inner utility function to export all disk representation, optionnaly in zip form, or only csv file
    // csv and zip file has to be in same directory
    private void exportAll() throws SEDALibException, InterruptedException {
        Date d = new Date();
        start = Instant.now();
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, getDescription(d), null);

        auRelativePathMap = new HashMap<ArchiveUnit, Path>();
        relativePathStringSet = new HashSet<String>();

        createDirectories(null);
        defineZipOutputStreamOrNull(rootPath, zipFileName);
        createCsvBAOSPrintScream();

        computeCsvHeader();
        printCsvHeader();

        dataObjectPackage.resetInOutCounter();
        for (ArchiveUnit au : dataObjectPackage.getGhostRootAu().getChildrenAuList().getArchiveUnitList())
            exportArchiveUnit(au, Paths.get(""));

        finaliseWithCsvMetadataFile();

        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, "Export csv simplifié terminé", null);
        end = Instant.now();
    }

    /**
     * Do export the DataObjectPackage metadata in a csv file.
     *
     * @param csvMetadataFilePathName the csv metadata file path name
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void doExportToCSVMetadataFile(String csvMetadataFilePathName) throws SEDALibException, InterruptedException {
        this.rootPath = Paths.get(csvMetadataFilePathName).toAbsolutePath().getParent().normalize();
        this.zipFileName = null;
        this.csvMetadataFileName = Paths.get(csvMetadataFilePathName).getFileName().toString();
        this.fileExportFlag = false;
        exportAll();
        exportAction = METADATA_FILE_EXPORT;
    }

    /**
     * Do export the DataObjectPackage to a disk hierarchy with the metadata csv.
     * <p>
     * It will export in the output directory at dirPathName:
     * <ul>
     * <li>csvMetadataFileName, the csv file with all descriptive metadata</li>
     * <li>each root ArchiveUnit as a sub directory, and recursively all the
     * DataObjectPackage structure in a simplified way as near as possible of original form if it was a disk
     * hierarchy</li>
     * </ul>
     *
     * @param dirPathName         the dir path name
     * @param csvMetadataFileName the csv metadata file name
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void doExportToCSVDiskHierarchy(String dirPathName, String csvMetadataFileName) throws SEDALibException, InterruptedException {
        this.rootPath = Paths.get(dirPathName).toAbsolutePath().normalize();
        this.zipFileName = null;
        this.csvMetadataFileName = csvMetadataFileName;
        this.fileExportFlag = true;
        exportAll();
        exportAction = ALL_DISK_EXPORT;
    }

    /**
     * Do export the DataObjectPackage to zip file with the metadata csv.
     * <p>
     * It will export in the zip file at zipFilePathName:
     * <ul>
     * <li>csvMetadataFileName, the csv file with all descriptive metadata</li>
     * <li>each root ArchiveUnit as a sub directory, and recursively all the
     * DataObjectPackage structure in a simplified way as near as possible of original form if it was a disk
     * hierarchy</li>
     * </ul>
     *
     * @param zipFilePathName     the zip file path name
     * @param csvMetadataFileName the csv metadata file name
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void doExportToCSVZip(String zipFilePathName, String csvMetadataFileName) throws SEDALibException, InterruptedException {
        this.rootPath = Paths.get(zipFilePathName).toAbsolutePath().getParent().normalize();
        this.zipFileName = Paths.get(zipFilePathName).getFileName().toString();
        this.csvMetadataFileName = csvMetadataFileName;
        this.fileExportFlag = true;
        exportAll();
        exportAction = ALL_ZIP_EXPORT;
    }

    /**
     * Gets the summary of the export process.
     *
     * @return the summary String
     */
    public String getSummary() {
        String result = "Export d'un DataObjectPackage en ";
        switch (exportAction) {
            case ALL_DISK_EXPORT:
                result += "hiérarchie disque simplifiée avec le fichier csv des métadonnées\n";
                break;
            case ALL_ZIP_EXPORT:
                result += "zip contenant hiérarchie simplifiée avec le fichier csv des métadonnées\n";
                break;
            case METADATA_FILE_EXPORT:
                result += "fichier csv des métadonnées\n";
                break;
            default:
                result += "???\n";
        }

        if ((start != null) && (end != null))
            result += "effectué en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}