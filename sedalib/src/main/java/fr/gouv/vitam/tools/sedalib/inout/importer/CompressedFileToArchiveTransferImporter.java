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

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class CompressedFileToArchiveTransferImporter.
 * <p>
 * Class for compressed file (zip, tar...) import in ArchiveTransfer object, similar to disk hierarchy import.
 * <p>
 * Known compression format are zip, tar, tar.gz, bzip
 */
public class CompressedFileToArchiveTransferImporter {

    /**
     * The zip file.
     */
    private String zipFile;

    /**
     * The un compress directory.
     */
    private String unCompressDirectory;

    /**
     * The encoding charset.
     */
    private String encoding;

    /**
     * The lambda function to extract title from file name.
     */
    private Function<String, String> extractTitleFromFileNameFunction;

    /**
     * The ignore patterns Strings.
     */
    private List<String> ignorePatternStrings;

    /**
     * The archive transfer.
     */
    private ArchiveTransfer archiveTransfer;

    /**
     * The disk to archive transfer importer
     */
    private DiskToDataObjectPackageImporter diskToDataObjectPackageImporter;

    /**
     * The on disk root paths.
     */
    private List<Path> onDiskRootPaths;

    /**
     * The GlobalMetaData file path .
     */
    private Path onDiskGlobalMetadataPath;

    /**
     * The start and end instants, for duration computation.
     */
    Instant start, /**
     * The End.
     */
    end;

    /**
     * The progress logger.
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * The constant ZIP.
     */
    public static final String ZIP = "application/zip";
    /**
     * The constant TAR.
     */
    public static final String TAR = "application/x-tar";
    /**
     * The constant XGZIP.
     */
    public static final String XGZIP = "application/x-gzip";
    /**
     * The constant GZIP.
     */
    public static final String GZIP = "application/gzip";
    /**
     * The constant BZIP2.
     */
    public static final String BZIP2 = "application/x-bzip2";

    /**
     * Test if mimetype is a known compressed format.
     *
     * @param mimeType the mime type
     * @return the string
     */
    public static boolean isKnownCompressedMimeType(String mimeType) {
        if (mimeType==null)
            return false;
        switch (mimeType) {
            case ZIP:
            case TAR:
            case XGZIP:
            case GZIP:
            case BZIP2:
                return true;
            default:
                return false;
        }
    }

    private ArchiveInputStream createArchiveInputStream(String filename)
            throws SEDALibException, InterruptedException {
        Path onDiskPath = null;
        String mimeType;
        FileInputStream fis=null;
        ArchiveInputStream ais=null;

        try {
            onDiskPath = Paths.get(filename);
            IdentificationResult ir = DroidIdentifier.getInstance().getIdentificationResult(onDiskPath);
            mimeType = ir.getMimeType();
        } catch (SEDALibException e) {
            throw new SEDALibException("Impossible de faire l'identification de format Droid pour le fichier compressé ["
                    + onDiskPath.toString() + "]", e);
        }

        try {
            fis = new FileInputStream(filename);
            switch (mimeType) {
                case ZIP:
                    ais= new ZipArchiveInputStream(fis,encoding);
                    break;
                case TAR:
                    ais= new TarArchiveInputStream(fis,encoding);
                    break;
                case XGZIP:
                case GZIP:
                    ais= new TarArchiveInputStream(new GzipCompressorInputStream(fis),encoding);
                    break;
                case BZIP2:
                    ais= new TarArchiveInputStream(new BZip2CompressorInputStream(fis),encoding);
                    break;
                default:
                    throw new SEDALibException("Format " + mimeType + " de compression inconnu.");
            }
            if (ais==null)
                    fis.close();
        } catch (IOException e) {
            throw new SEDALibException("Impossible d'ouvrir le fichier compressé ["
                    + onDiskPath.toString() + "]", e);
        }
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL,"mimetype=" + mimeType, null);
        return ais;
    }

    private void unCompressContainer(String containerFile, String outputFolder)
            throws SEDALibException, InterruptedException {
        int counter = 0;

        try (final ArchiveInputStream archiveInputStream = createArchiveInputStream(containerFile)) {
            ArchiveEntry entry;

            outputFolder = Paths.get(outputFolder).toAbsolutePath().normalize().toString();

            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (archiveInputStream.canReadEntryData(entry)) {
                    String entryName = entry.getName();
                    if (entryName.contains("?")) {
                        entryName = entryName.replace("?", "_");
                        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL,
                                "Le nom du fichier [" + entryName + "] a un problème d'encodage, le(s) caratère(s) problématique à été rempalcé par _ ", null);
                    }
                    final Path target = Paths.get(outputFolder, entryName);
                    final Path parent = target.getParent();

                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    if (!entry.isDirectory()) {
                        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS,
                                "Décompresse le fichier [" + entryName + "]", null);
                        Files.copy(archiveInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                        counter++;
                        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
                                Integer.toString(counter) +
                                " fichiers extraits");
                    } else if (!Files.exists(target)) {
                        Files.createDirectories(target);
                    }
                }
            }
        } catch (final IOException e) {
            throw new SEDALibException("Impossible d'extraire le fichier compressé [" + containerFile + "]", e);
        }
    }

    /**
     * Instantiates a new zip file importer.
     *
     * @param zipFile                          the zip file
     * @param unCompressDirectory              the directory where the zipfile is uncompressed
     * @param encoding                         the encoding charset
     * @param extractTitleFromFileNameFunction the extract title from file name function
     * @param sedaLibProgressLogger            the progress logger or null if no progress log expected
     * @throws SEDALibException if file or directory doesn't exist
     */
    public CompressedFileToArchiveTransferImporter(String zipFile, String unCompressDirectory, String encoding,
                                                   Function<String, String> extractTitleFromFileNameFunction,
                                                   SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException {
        Path zipFilePath, unCompressDirectoryPath;

        this.onDiskGlobalMetadataPath = null;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.extractTitleFromFileNameFunction = extractTitleFromFileNameFunction;
        this.diskToDataObjectPackageImporter = null;
        this.archiveTransfer = null;
        this.zipFile = zipFile;
        this.encoding=encoding;
        this.ignorePatternStrings = new ArrayList<>();
        this.onDiskRootPaths = new ArrayList<>();

        zipFilePath = Paths.get(zipFile);
        if (!Files.isRegularFile(zipFilePath, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("Le chemin [" + zipFile + "] pointant vers le zip ne désigne pas un fichier");
        unCompressDirectoryPath = Paths.get(unCompressDirectory).toAbsolutePath();
        this.unCompressDirectory = unCompressDirectoryPath.normalize().toString();
        if (!Files.exists(unCompressDirectoryPath))
            try {
                Files.createDirectories(unCompressDirectoryPath);
            } catch (IOException e) {
                throw new SEDALibException("Impossible de créer le répertoire d'extraction [" + unCompressDirectory + "]");
            }
        if (!Files.isDirectory(unCompressDirectoryPath, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("Le chemin [" + unCompressDirectory + "] pointant le répertoire d'extraction ne désigne pas un répertoire");
    }

    /**
     * Adds the ignore pattern string.
     *
     * @param patternString the pattern string
     */
    public void addIgnorePattern(String patternString) {
        ignorePatternStrings.add(patternString);
    }


    /**
     * Process the GlobalMetadata file.
     *
     * @param path the path
     * @return the ArchiveUnit generated from path
     * @throws SEDALibException if reading file has failed
     */
    private GlobalMetadata processGlobalMetadata(Path path) throws SEDALibException {
        GlobalMetadata atgm = new GlobalMetadata();
        try {
            atgm.fromSedaXmlFragments(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
        } catch (SEDALibException | IOException e) {
            throw new SEDALibException("Lecture des métadonnées globales à partir du fichier [" + path
                    + "] impossible", e);
        }
        return atgm;
    }

    /**
     * Do import the zip file to ArchiveTransfer.
     *
     * @throws SEDALibException     if the XML manifest can't be read or is not in expected                              form, or zip file can't be uncompressed
     * @throws InterruptedException if export process is interrupted
     */
    public void doImport() throws SEDALibException, InterruptedException {
        Path path;
        Iterator<Path> pi;

        Date d = new Date();
        start = Instant.now();
        String log = "sedalib: début de l'import du fichier compressé\n";
        log += "en [" + zipFile + "]\n";
        log += " date=" + DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, log, null);

        unCompressContainer(zipFile, unCompressDirectory);

        path = Paths.get(unCompressDirectory);
        try (Stream<Path> sp = Files.list(path)) {
            pi = sp.iterator();
            while (pi.hasNext()) {
                path = pi.next();
                if (path.getFileName().toString().equals("__GlobalMetadata.xml"))
                    this.onDiskGlobalMetadataPath = path;
                else
                    this.onDiskRootPaths.add(path);
            }
        } catch (IOException e) {
            throw new SEDALibException(
                    "Impossible de lister les fichiers du répertoire [" + unCompressDirectory + "]", e);
        }

        this.diskToDataObjectPackageImporter = new DiskToDataObjectPackageImporter(this.onDiskRootPaths, true,
                extractTitleFromFileNameFunction,
                sedaLibProgressLogger);
        this.archiveTransfer = new ArchiveTransfer();

        if (onDiskGlobalMetadataPath != null)
            archiveTransfer.setGlobalMetadata(processGlobalMetadata(onDiskGlobalMetadataPath));
        for (String patternString : ignorePatternStrings)
            diskToDataObjectPackageImporter.addIgnorePattern(patternString);
        diskToDataObjectPackageImporter.doImport();
        archiveTransfer.setDataObjectPackage(diskToDataObjectPackageImporter.getDataObjectPackage());

        end = Instant.now();
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, "sedalib: import d'un ArchiveTransfer depuis un fichier zip sur disque terminé", null);
    }

    /**
     * Gets the archive transfer.
     *
     * @return the archive transfer
     */
    public ArchiveTransfer getArchiveTransfer() {
        return archiveTransfer;
    }

    /**
     * Gets the summary of the import process.
     *
     * @return the summary
     */
    public String getSummary() {
        String result = null;
        if (archiveTransfer != null) {
            result = archiveTransfer.getDescription() + "\n";
            if (start != null)
                result += "chargé en "
                        + Duration.between(start, end).toString().substring(2) + "\n";
        }
        return result;
    }

}
