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
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The Class DataObjectPackageToCSVMetadataExporter.
 * <p>
 * It will export a DataObjectPackage to a set of files, the disk hierarchy and metadata being defined in a csv file.
 * The disk hierarchy is organised as the DataObjectPackage ArchiveUnit tree and filenames are recovered from metadata
 * (in case of name collision the second file as a derived name).
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
     * The archive transfer.
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
     * The First dataobject.
     */
    static public final int FIRST_DATAOBJECT = 1;
    /**
     * The Last dataobject.
     */
    static public final int LAST_DATAOBJECT = 2;
    /**
     * The All dataobjects.
     */
    static public final int ALL_DATAOBJECTS = 3;

    /**
     * The max name size.
     */
    private int maxNameSize;

    /**
     * The progress logger.
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * The header row in csv metadata.
     */
    private List<String> headerNames;

    /**
     * The ArchiveUnit path string map, used to manage symbolic links.
     */
    private HashMap<ArchiveUnit, Path> auPathStringMap;

    /**
     * The set of all generated files paths to detect collision.
     */
    private Set<Path> filesPathSet;

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

    private int getMaxRank(Set<String> headerNames, String name) {
        if (headerNames.contains(name))
            return -1;
        int pos = name.split("\\.").length;
        int maxRank = -2;
        for (String tmp : headerNames) {
            if (tmp.startsWith(name)) {
                if (maxRank == -2) maxRank = -1;
                String[] splittedTmp = tmp.split("\\.");
                if (splittedTmp.length > pos) {
                    try {
                        maxRank = Math.max(maxRank, Integer.parseInt(splittedTmp[pos]));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return maxRank;
    }

    private List<String> getSortedHeaderNames(List<String> sortedHeaderNames, Set<String> headerNames,
                                              String prefixHeaderName, String xmlElementName, Class metadataClass) {
        String currentName = prefixHeaderName + (prefixHeaderName.isEmpty() ? "" : ".") + xmlElementName, infix;
        int maxRank = getMaxRank(headerNames, currentName);
        if (maxRank != -2) {
            {
                if (maxRank == -1)
                    infix = "";
                else infix = ".0";
                int i = 0;
                do {
                    if (ComplexListType.class.isAssignableFrom(metadataClass)) {
                        for (Map.Entry<String, ComplexListMetadataKind> e : ComplexListType.getMetadataMap(metadataClass).entrySet()) {
                            getSortedHeaderNames(sortedHeaderNames, headerNames, currentName + infix,
                                    e.getKey(), e.getValue().metadataClass);
                        }
                    } else {
                        sortedHeaderNames.add(currentName + infix);
                        headerNames.remove(currentName + infix);
                        if (headerNames.contains(currentName + infix + ".attr")) {
                            sortedHeaderNames.add(currentName + infix + ".attr");
                            headerNames.remove(currentName + infix + ".attr");
                        }
                    }
                    i++;
                    infix = "." + i;
                } while (i <= maxRank);
            }
        }
        return sortedHeaderNames;
    }

    private void computeCsvHeader() throws SEDALibException {
        Set<String> headerNames = new HashSet<String>();
        for (ArchiveUnit au : dataObjectPackage.getAuInDataObjectPackageIdMap().values()) {
            headerNames.addAll(au.getContent().filteredToCsvList(dataObjectPackage.getExportMetadataList()).keySet());
        }
        this.headerNames = getSortedHeaderNames(new ArrayList<String>(), headerNames, "", "Content",
                Content.class);
    }

    private void generateHeader(List<String> headerNames, PrintStream csvPrintStream){
        csvPrintStream.print("Path");
        for (String header:headerNames)
            csvPrintStream.print(separator+header);
        csvPrintStream.println();
    }

    private void generateCsvLine(ArchiveUnit au, Path auPath, Path rootPath, PrintStream csvPrintStream){
        LinkedHashMap<String,String> metadataHashMap;

        String value="\""+rootPath.relativize(auPath).toString().replace("\"","\"\"")+"\"";
        csvPrintStream.print(value);
        try {
            metadataHashMap=au.getContent().filteredToCsvList(null);
        } catch (SEDALibException e) {
            csvPrintStream.print(separator+"Extraction des métadonnées impossible");
            return;
        }
        for (String header:headerNames){
            value=metadataHashMap.get(header);
            if (value==null)
                value="";
            else
                value="\""+value.replace("\"","\"\"")+"\"";
            csvPrintStream.print(separator+value);
        }
        csvPrintStream.println();
    }

    private BinaryDataObject getBestUsageVersionObject(List<BinaryDataObject> objectList, boolean firstFlag) {
        int rank, version;
        TreeMap<Integer, BinaryDataObject> rankMap = new TreeMap<Integer, BinaryDataObject>();
        for (BinaryDataObject bdo : objectList) {
            if ((bdo.dataObjectVersion == null) || (bdo.dataObjectVersion.isEmpty())) {
                sedaLibProgressLogger.log(SEDALibProgressLogger.OBJECTS_WARNINGS, "Un objet binaire n'a pas d'usage_version," +
                        " il ne peut être choisi pour l'extraction");
                continue;
            }
            String[] usageVersion = bdo.dataObjectVersion.split("_");
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
            try {
                version = Integer.parseInt(usageVersion[1]);
            } catch (NumberFormatException e) {
                sedaLibProgressLogger.log(SEDALibProgressLogger.OBJECTS_WARNINGS, "Un objet binaire n'a pas d'usage_version," +
                        " il ne peut être choisi pour l'extraction");
                continue;
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

    private List<BinaryDataObject> getArchiveUnitObjectList(ArchiveUnit au) {
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

    /**
     * Strip a String of all characters not allowed in a file name.
     *
     * @param fileName the file name
     * @return the string
     */
    @SuppressWarnings("Annotator")
    private String stripFileName(String fileName) {
        return fileName.replaceAll("[\\/|\\\\|\\*|\\:|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]", "_");
    }

    /**
     * Construct directory name for ArchiveUnit.
     *
     * @param au the ArchiveUnit
     * @return the file name
     */
    private String constructDirectoryName(ArchiveUnit au) throws SEDALibException {
        String result = "";
        if (au.getContent() != null) {
            result = au.getContent().getSimpleMetadata("Title");
            if (result == null)
                result = "NoTitle";
            if ((maxNameSize > 0) && (result.length() > maxNameSize - au.getInDataObjectPackageId().length() - 1))
                result = result.substring(0, 11);
            result += "_";
        }
        result += au.getInDataObjectPackageId();
        return stripFileName(result);
    }

    private String getShortUsageVersionFilename(BinaryDataObject bdo,String filename) {
        String[] usageVersion = bdo.dataObjectVersion.split("_");
        String shortUsageVersion, result;

        if (usageVersion[0].isEmpty())
            shortUsageVersion = "Z";
        else
            shortUsageVersion = usageVersion[0].substring(0, 1);
        shortUsageVersion += usageVersion[1];

        int point=filename.lastIndexOf('.');
        if (point==-1)
            result=filename+shortUsageVersion;
        else {
            result=filename.substring(0,point)+"_"+shortUsageVersion+"."+filename.substring(point+1);
        }
        return result;
    }

    /**
     * Export archive unit to disk.
     *
     * @param au            the ArchiveUnit
     * @param containerPath the container path
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException the interrupted exception
     */
    private void exportArchiveUnit(ArchiveUnit au, Path containerPath, Path rootPath, PrintStream csvPrintStream) throws SEDALibException, InterruptedException {
        Path auPath;
        String filename = null;

        if (auPathStringMap.containsKey(au)) {
            // exportLink(auPathStringMap.get(au), containerPath, constructDirectoryName(au));
            return;
        }

        List<BinaryDataObject> objectList = getArchiveUnitObjectList(au);
        // if not only a file ArchiveUnit create a named directory with the ArchiveUnit Title
        if (((au.getChildrenAuList() != null) && (au.getChildrenAuList().getCount() != 0)) ||
                ((objectList==null) || (objectList.size() > 1) || (objectList.size() == 0))) {
            auPath = containerPath.resolve(constructDirectoryName(au));
        } else auPath = containerPath;
        try {
            Files.createDirectories(auPath);
        } catch (IOException e) {
            throw new SEDALibException(
                    "Création du répertoire [" + auPath + "] impossible\n->" + e.getMessage());
        }
        if ((objectList!=null) && (objectList.size()>0)) {
            try {
                for (BinaryDataObject bdo : objectList) {
                    filename = bdo.fileInfo.filename;
                    if (objectList.size() > 1)
                        filename = getShortUsageVersionFilename(bdo,filename);
                    Files.copy(bdo.getOnDiskPath(), auPath.resolve(filename));
                }
            } catch (IOException e) {
                throw new SEDALibException("Le fichier [" + filename + "] n'a pas pu être recopié\n-> "
                        + e.getMessage());
            }
        }
        if ((au.getChildrenAuList() != null) && (au.getChildrenAuList().getCount() != 0)) {
            for (ArchiveUnit childAU : au.getChildrenAuList().getArchiveUnitList())
                exportArchiveUnit(childAU, auPath, rootPath, csvPrintStream);
        }

        // if a file ArchiveUnit the kept path for links is the file path
        if (auPath == containerPath)
            auPath = auPath.resolve(filename);
        auPathStringMap.put(au, auPath);

        generateCsvLine(au, auPath, rootPath, csvPrintStream);
    }

    /**
     * Do export the DataObjectPackage to a disk hierarchy.
     * <p>
     * It will export in the output directory:
     * <ul>
     * <li>metadata.csv, the csv file with all descriptive metadata</li>
     * <li>each root ArchiveUnit as a sub directory, and recursively all the
     * DataObjectPackage structure</li>
     * </ul>
     *
     * @param csvMetadataFileName the csv metadata file name
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void doExport(String csvMetadataFileName) throws SEDALibException, InterruptedException {
        Path csvFolderPath = Paths.get(csvMetadataFileName).getParent().toAbsolutePath();
        try {
            Files.createDirectories(csvFolderPath);
        } catch (Exception e) {
            throw new SEDALibException(
                    "Création du répertoire [" + csvFolderPath + "] impossible\n->" + e.getMessage());
        }
        computeCsvHeader();
        PrintStream csvPrintStream;
        try {
            csvPrintStream = new PrintStream(csvMetadataFileName, encoding);
        } catch (FileNotFoundException e) {
            throw new SEDALibException(
                    "Impossible de créer le fichier csv [" + csvMetadataFileName + "]\n->" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new SEDALibException(
                    "Encodage [" + encoding + "] inconnu\n->" + e.getMessage());
        }
        generateHeader(headerNames, csvPrintStream);

        auPathStringMap=new HashMap<ArchiveUnit,Path>();
        for (ArchiveUnit au : dataObjectPackage.getGhostRootAu().getChildrenAuList().getArchiveUnitList())
            exportArchiveUnit(au, csvFolderPath, csvFolderPath, csvPrintStream);

        if (sedaLibProgressLogger != null)
            sedaLibProgressLogger.progressLog(SEDALibProgressLogger.OBJECTS_GROUP,
                    Integer.toString(dataObjectPackage.getInOutCounter()) + " ArchiveUnit/DataObject exportées\n"
                            + dataObjectPackage.getDescription());
    }
}
