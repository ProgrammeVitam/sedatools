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
package fr.gouv.vitam.tools.sedalib.inout.importer;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.importer.model.Line;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class CSVMetadataToDataObjectPackageImporter.
 * <p>
 * It will import a DataObjectPackage from metadata being defined in a csv file and files on disk.
 * <p>
 * The csv has one header line. At the beginning there must be either:
 * <ul>
 * <li>only one column with 'File': then this column contains the file (or directory) path which is also used as uniq ID and which defines the hierarchy as the file hierarchy;</li>
 * <li>two columns with 'File' and 'ParentFile': then the 'File' column contains the file (or directory) path which is also used as uniq ID  and
 * the 'ParentFile' column contains the file (or directory )name of the parent ArchiveUnit;</li>
 * <li>three columns with 'ID', 'File' and 'ParentFile': then the 'File' column contains the file (or directory) path which is also used as uniq ID  and
 * the 'ParentFile' column contains the file (or directory )name of the parent ArchiveUnit. ID is ignored;</li>
 * <li>three columns with 'ID', 'ParentID' and 'File' : then the 'File' column contains the file (or directory) path,
 * the 'ID' column is used as uniq ID  and the 'ParentID' column contains the ID of the parent ArchiveUnit.</li>
 * <li>three columns with 'ID', 'ParentID' and 'ObjectFiles' : then the 'ObjectFiles' column contains the concatenated
 * binary data objects files path with '|' joiner, the 'ID' column is used as uniq ID  and the 'ParentID' column contains the ID of the parent ArchiveUnit.</li>
 * <li>four columns with 'ID', 'ParentID', 'File' and 'ObjectFiles' : then the 'File' column contains the file
 * (or directory) path for ArchiveUnit (only given for easy human reading), the 'ObjectFiles' column contains the concatenated
 * binary data objects files path with '|' joiner,
 * the 'ID' column is used as uniq ID  and the 'ParentID' column contains the ID of the parent ArchiveUnit.
 * This is the extended format written by csv exporter {@link  fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter}.</li>
 * </ul>
 * Paths are absolute or if relative this is from the metadata csv file directory.
 * <p>
 * After that the columns defines metadata path, each tag being separeted by a dot, or an attribute ('attr' column) of the metadata in the previous colum.
 * <p>
 * For example: Writer.FullName|Description|attr defines first colum of values to put in
 * &lt;Writer&gt;&lt;FullName&gt;VALUE&lt;/FullName&gt;&lt;/Writer&gt;, then values to put in &lt;Description&gt;VALUE&lt;/Description&gt;
 * and finally attributes to put in &lt;Description&gt; if any.
 * If many values of a tag has to be defined in csv, for example 2 Writers, then it's defined has Writer.0 and Writer.1, and FullName is Writer.0.FullName.
 * <p>
 * The importer imports objects:
 * <ul>
 * <li>For the non extended formats, the importer try to integrate all files in the ArchiveUnits directories defined on disk.</li>
 * <li>For the extended format, the importer simply try to import in the DataObjectPackage all files defined in ObjectFiles.</li>
 * </ul>
 * The file name are analysed to extract Usage Version if encoded like in
 * {@link  fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter} (filename formatted __Usage_Version__originalfilename or by default considered as BinaryMaster_1). If the file doesn't exist, this is only logged (no import fail) and the object is not imported.
 */
public class CSVMetadataToDataObjectPackageImporter {

    /**
     * The csv metadata file name .
     */
    private String csvMetadataFileName;

    /**
     * The csv metadata file directory path.
     */
    private Path csvMetadataDirectoryPath;

    /**
     * The encoding format.
     */
    private String encoding;

    /**
     * The extended format flag.
     */
    private boolean isExtendedFormat;

    /**
     * The separator char.
     */
    private char separator;

    /**
     * The archive transfer.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * The start and end instants, for duration computation.
     */
    private Instant start;
    private Instant end;

    private Map<String, Line> linesMap;

    /**
     * The progress logger.
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * Instantiates a new DataObjectPackage importer from a csv metadata file with associated file collection.
     * <p>
     * It will analyse the csv metadata file, verify that described files exist and associates metadata and file.
     *
     * @param csvMetadataFileName   the csv metadata file name
     * @param encoding              the encoding format string (most of the time UTF8 or Cp1252)
     * @param separator             the char used as column separator (; or , or \t...)
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException if file doesn't exist
     */
    public CSVMetadataToDataObjectPackageImporter(String csvMetadataFileName, String encoding, char separator, SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException {
        if (!Files.isRegularFile(Paths.get(csvMetadataFileName), java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("Le chemin [" + csvMetadataFileName + "] pointant vers le fichier csv ne désigne pas un fichier");

        this.csvMetadataFileName = csvMetadataFileName;
        this.csvMetadataDirectoryPath = Paths.get(csvMetadataFileName).getParent();
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.encoding = encoding;
        this.separator = separator;
    }

    /**
     * Read csv file and construct the map with all parsed csv lines by GUID either ID or file name
     *
     * @return the need ID regeneration flag
     * @throws SEDALibException     if csv file can't be accessed or is badly formatted
     * @throws InterruptedException if import process is interrupted
     */
    private boolean readCSVFile() throws SEDALibException, InterruptedException {
        int lineCount = 0;
        CSVMetadataFormatter metadataFormatter = null;
        Line currentLine;

        linesMap = new HashMap<>();
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(String[].class).withColumnSeparator(separator);
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(csvMetadataFileName), encoding);
             MappingIterator<String[]> it = mapper.readerFor(String[].class).with(schema).readValues(isr)) {
            while (it.hasNext()) {
                String[] row = it.next();
                lineCount++;
                // jump header line
                if (lineCount == 1) {
                    metadataFormatter = new CSVMetadataFormatter(row, Paths.get(csvMetadataFileName).toAbsolutePath().getParent());
                    isExtendedFormat = metadataFormatter.isExtendedFormat();
                    continue;
                }
                try {
                    currentLine = new Line(metadataFormatter.getGUID(row),
                            metadataFormatter.getParentGUID(row), //NOSONAR
                            metadataFormatter.getFile(row),
                            metadataFormatter.getObjectFiles(row),
                            metadataFormatter.doFormatAndExtractContentXML(row),
                            metadataFormatter.extractManagementXML());
                } catch (SEDALibException e) {
                    throw new SEDALibException("Erreur sur la ligne "+lineCount, e);
                }
                linesMap.put(metadataFormatter.getGUID(row), currentLine);
                doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, lineCount, "sedalib: " + lineCount + " lignes interprétées");
            }
        } catch (IOException e) {
            throw new SEDALibException("Le fichier csv [" + csvMetadataFileName + "] n'est pas accessible");
        }
        if (metadataFormatter != null)
            return metadataFormatter.needIdRegeneration();
        else //if file empty...
            return false;
    }

    private Path getAbsolutePath(String file) {
        Path path = Paths.get(file);
        if (!path.isAbsolute())
            path = csvMetadataDirectoryPath.resolve(path);
        return path;
    }

    private ArchiveUnit createLineAU(Line line) throws SEDALibException, InterruptedException {
        ArchiveUnit au;
        BinaryDataObject bdo;

        au = new ArchiveUnit();
        au.setInDataObjectPackageId("Import-" + line.guid);
        dataObjectPackage.addArchiveUnit(au);

        au.setContentXmlData(line.contentXMLMetadata);
        au.getContent();
        if (!line.managementXMLMetadata.isEmpty()) {
            au.setManagementXmlData(line.managementXMLMetadata);
            au.getManagement();
        }
        Path path = getAbsolutePath(line.file);
        DataObjectGroup implicitDog = null;
        if (isExtendedFormat) {
            for (String objectFile : line.objectFiles) {
                Path objectPath = getAbsolutePath(objectFile);
                if (!Files.isRegularFile(objectPath)) {
                    doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP,
                            "sedalib: la référence sur le disque du fichier objet [" + objectFile + "] n'est plus valable", null);
                    continue;
                }
                String fileName = path.getFileName().toString();
                if ((objectFile.matches("__\\w+__.+")) || (objectFile.matches("__\\w+_.+")))
                    implicitDog = DiskToDataObjectPackageImporter.addBinaryDataObject(dataObjectPackage, objectPath, fileName, au, implicitDog);
                else {
                    bdo = new BinaryDataObject(dataObjectPackage, objectPath, path.getFileName().toString(),
                            "BinaryMaster_1");
                    au.addDataObjectById(bdo.getInDataObjectPackageId());
                }
            }
        } else {
            if (Files.isRegularFile(path)) {
                bdo = new BinaryDataObject(dataObjectPackage, path, path.getFileName().toString(),
                        "BinaryMaster_1");
                au.addDataObjectById(bdo.getInDataObjectPackageId());
            } else if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path subPath : stream) {
                        if (Files.isRegularFile(subPath)) {
                            String fileName = path.getFileName().toString();
                            if ((fileName.matches("__\\w+__.+")) || (fileName.matches("__\\w+_.+")))
                                implicitDog = DiskToDataObjectPackageImporter.addBinaryDataObject(dataObjectPackage, path, fileName, au, implicitDog);
                        }
                    }
                } catch (IOException e) {
                    throw new SEDALibException("Les fichiers dans le répertoire [" + path + "] ne sont pas accessibles");
                }
            } else if (!line.file.isEmpty()) {
                doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP,
                        "sedalib: la référence sur le disque de l'archiveUnit [" + line.guid + "] n'est plus valable", null);
            }
        }
        return au;
    }

    private ArchiveUnit createToRoot(Line line) throws SEDALibException, InterruptedException {
        if (line.au == null) {
            line.au = createLineAU(line);
            Line parentLine = linesMap.get(line.parentGUID);
            if ((parentLine == null) || parentLine.equals(line)) {
                doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP,
                        "sedalib: archiveUnit [" + line.guid + "] n'a pas de parent, elle est mise en racine", null);
                dataObjectPackage.addRootAu(line.au);
            } else {
                ArchiveUnit parentAU = createToRoot(parentLine);
                parentAU.addChildArchiveUnit(line.au);
            }
        }
        return line.au;
    }

    /**
     * Do import the csv metadata file to DataObjectPackage. It will import from a
     * directory content or from a list of files or directories:
     * <ul>
     * <li>GlobalMetadata XML fragments from the __GlobalMetadata.xml file</li>
     * <li>ManagementMetadata XML element at the end of DataObjectPackage from the
     * __ManagementMetadata.xml file</li>
     * <li>each root ArchiveUnit from a sub directory, and recursively all the
     * DataObjectPackage structure (see {@link DiskToDataObjectPackageImporter} for
     * details)</li>
     * </ul>
     *
     * @throws SEDALibException     any import exception
     * @throws InterruptedException if export process is interrupted
     */
    public void doImport() throws SEDALibException, InterruptedException {
        Date d = new Date();
        start = Instant.now();
        String log = "sedalib: début de l'import du fichier csv de métadonnées\n";
        log += "en [" + csvMetadataFileName + "]\n";
        log += "date=" + DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, log, null);

        boolean needIdRegeneration = readCSVFile();
        dataObjectPackage = new DataObjectPackage();

        int lineCount=0;
        for (Map.Entry<String, Line> e : linesMap.entrySet()) {
            if (e.getValue().au != null) continue;
            createToRoot(e.getValue());
            lineCount++;
            doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, lineCount, "sedalib: " + lineCount + " ArchiveUnits importées");
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP, "sedalib: " + lineCount + " ArchiveUnits importées", null);

        dataObjectPackage.vitamNormalize(sedaLibProgressLogger);
        if (needIdRegeneration)
            dataObjectPackage.regenerateContinuousIds();

        lineCount = 0;
        for (Map.Entry<String, BinaryDataObject> pair : dataObjectPackage.getBdoInDataObjectPackageIdMap().entrySet()) {
            if (pair.getValue().fileInfo.getSimpleMetadata("LastModified") == null)
                pair.getValue().extractTechnicalElements(sedaLibProgressLogger);
            lineCount++;
            doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, lineCount, "sedalib: " + lineCount +
                    " fichiers BinaryDataObject analysés");
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.STEP, "sedalib: " + lineCount + " fichiers BinaryDataObject analysés et importés dans le DataObjectPackage", null);
        end = Instant.now();
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, "sedalib: import du fichier csv de métadonnées terminé", null);
    }

    /**
     * Gets the data object package.
     *
     * @return the data object package
     */
    public DataObjectPackage getDataObjectPackage() {
        return dataObjectPackage;
    }

    /**
     * Gets the summary of the import process.
     *
     * @return the summary
     */
    public String getSummary() {
        String result;

        result = dataObjectPackage.getDescription() + "\n";
        if (start != null)
            result += "chargé en "
                    + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}
