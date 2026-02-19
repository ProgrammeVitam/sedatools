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
package fr.gouv.vitam.tools.sedalib.inout.importer;

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
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
 * Class for compressed file (zip, tar...) import in ArchiveTransfer object,
 * similar to disk hierarchy import.
 * <p>
 * Known compression format are zip, tar, tar.gz, bzip
 */
public class CompressedFileToArchiveTransferImporter {

    /**
     * Wrap org.apache.commons.compress SevenZFile to have same ArchiveInputStream
     * behavior
     */
    private static class SevenZWrapper extends ArchiveInputStream {

        private SevenZFile file;

        private SevenZWrapper(SevenZFile file) {
            this.file = file;
        }

        @Override
        public int read() throws IOException {
            return file.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return file.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return file.read(b, off, len);
        }

        @Override
        public ArchiveEntry getNextEntry() throws IOException {
            return file.getNextEntry();
        }

        @Override
        public void close() throws IOException {
            file.close();
        }
    }

    /**
     * The compressed file.
     */
    private final String compressedFile;

    /**
     * The un compress directory.
     */
    private String unCompressDirectory;

    /**
     * The droidFormat.
     */
    private String droidFormat;

    /**
     * The encoding charset.
     */
    private String encoding;

    /**
     * The lambda function to extract title from file name.
     */
    private final Function<String, String> extractTitleFromFileNameFunction;

    /**
     * The ignore patterns Strings.
     */
    private final List<String> ignorePatternStrings;

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
    private final List<Path> onDiskRootPaths;

    /**
     * The GlobalMetaData file path .
     */
    private Path onDiskGlobalMetadataPath;

    /**
     * The start instant, for duration computation.
     */
    Instant start;

    /**
     * The end instant, for duration computation.
     */
    Instant end;

    /**
     * The progress logger.
     */
    private final SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * The digest algorithm.
     */
    private String digestAlgorithm;

    /**
     * The constant ZIP.
     */
    public static final String ZIP = "x-fmt/263";
    /**
     * The constant TAR.
     */
    public static final String TAR = "x-fmt/265";
    /**
     * The constant GZIP.
     */
    public static final String GZIP = "x-fmt/266";
    /**
     * The constant BZIP2.
     */
    public static final String BZIP2 = "x-fmt/268";
    /**
     * The constant SEVENZIP.
     */
    public static final String SEVENZIP = "fmt/484";

    /**
     * Test if droid format is of a known compressed format.
     *
     * @param droidFormat the Droid format
     * @return true or false
     */
    public static boolean isKnownCompressedDroidFormat(String droidFormat) {
        if (droidFormat != null) {
            switch (droidFormat) {
                case ZIP:
                case TAR:
                case GZIP:
                case BZIP2:
                case SEVENZIP:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    private ArchiveInputStream createArchiveInputStream() throws SEDALibException {
        Path onDiskPath = null;
        FileInputStream fis = null;
        ArchiveInputStream ais;

        try {
            onDiskPath = Paths.get(compressedFile);
            switch (droidFormat) {
                case ZIP:
                    fis = new FileInputStream(compressedFile); // NOSONAR
                    ais = new ZipArchiveInputStream(fis, encoding); // NOSONAR
                    break;
                case TAR:
                    fis = new FileInputStream(compressedFile); // NOSONAR
                    ais = new TarArchiveInputStream(fis, encoding); // NOSONAR
                    break;
                case GZIP:
                    fis = new FileInputStream(compressedFile); // NOSONAR
                    ais = new TarArchiveInputStream(new GzipCompressorInputStream(fis), encoding); // NOSONAR
                    break;
                case BZIP2:
                    fis = new FileInputStream(compressedFile); // NOSONAR
                    ais = new TarArchiveInputStream(new BZip2CompressorInputStream(fis), encoding); // NOSONAR
                    break;
                case SEVENZIP:
                    ais = new SevenZWrapper(new SevenZFile(new File(compressedFile))); // NOSONAR
                    break;
                default:
                    throw new SEDALibException("Format " + droidFormat + " de compression inconnu.");
            }
        } catch (IOException e) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            throw new SEDALibException("Impossible d'ouvrir le fichier compressé [" + onDiskPath.toString() + "]", e);
        }
        return ais;
    }

    private void unCompressContainer() throws SEDALibException, InterruptedException {
        int counter = 0;

        try (final ArchiveInputStream archiveInputStream = createArchiveInputStream()) {
            ArchiveEntry entry;

            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (archiveInputStream.canReadEntryData(entry)) {
                    String entryName = entry.getName();
                    if (entryName.contains("?")) {
                        entryName = entryName.replace("?", "_");
                        doProgressLog(
                            sedaLibProgressLogger,
                            SEDALibProgressLogger.GLOBAL,
                            "Le nom du fichier [" +
                            entryName +
                            "] a un problème d'encodage, le(s) caractère(s) problématique à été rempalcé par _ ",
                            null
                        );
                    }
                    final Path target = Paths.get(unCompressDirectory, entryName);
                    final Path parent = target.getParent();

                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    if (!entry.isDirectory()) {
                        doProgressLog(
                            sedaLibProgressLogger,
                            SEDALibProgressLogger.OBJECTS,
                            "Décompresse le fichier [" + entryName + "]",
                            null
                        );
                        Files.copy(archiveInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                        counter++;
                        doProgressLogIfStep(
                            sedaLibProgressLogger,
                            SEDALibProgressLogger.OBJECTS_GROUP,
                            counter,
                            Integer.toString(counter) + " fichiers extraits"
                        );
                    } else if (!Files.exists(target)) {
                        Files.createDirectories(target);
                    }
                }
            }
        } catch (final IOException e) {
            throw new SEDALibException("Impossible d'extraire le fichier compressé [" + compressedFile + "]", e);
        }
    }

    /**
     * Instantiates a new compressed file importer.
     *
     * @param compressedFile                   the compressed file
     * @param unCompressDirectory              the directory where the
     *                                         compressedfile is uncompressed
     * @param encoding                         the filename encoding charset, if
     *                                         null will be determine considering
     *                                         the compressed file format
     * @param extractTitleFromFileNameFunction the extract title from file name
     *                                         function
     * @param sedaLibProgressLogger            the progress logger or null if no
     *                                         progress log expected
     * @throws SEDALibException if file or directory doesn't exist
     */
    public CompressedFileToArchiveTransferImporter(
        String compressedFile,
        String unCompressDirectory,
        String encoding,
        Function<String, String> extractTitleFromFileNameFunction,
        SEDALibProgressLogger sedaLibProgressLogger
    ) throws SEDALibException {
        Path compressedFilePath;
        Path unCompressDirectoryPath;

        this.onDiskGlobalMetadataPath = null;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.extractTitleFromFileNameFunction = extractTitleFromFileNameFunction;
        this.diskToDataObjectPackageImporter = null;
        this.archiveTransfer = null;
        this.compressedFile = compressedFile;
        this.droidFormat = "";
        this.encoding = encoding;
        this.ignorePatternStrings = new ArrayList<>();
        this.onDiskRootPaths = new ArrayList<>();

        compressedFilePath = Paths.get(compressedFile);
        if (
            !Files.isRegularFile(compressedFilePath, java.nio.file.LinkOption.NOFOLLOW_LINKS)
        ) throw new SEDALibException(
            "Le chemin [" + compressedFile + "] pointant vers le fichier compressé ne désigne pas un fichier"
        );
        unCompressDirectoryPath = Paths.get(unCompressDirectory).toAbsolutePath();
        this.unCompressDirectory = unCompressDirectoryPath.normalize().toString();
        if (!Files.exists(unCompressDirectoryPath)) try {
            Files.createDirectories(unCompressDirectoryPath);
        } catch (IOException e) {
            throw new SEDALibException("Impossible de créer le répertoire d'extraction [" + unCompressDirectory + "]");
        }
        if (
            !Files.isDirectory(unCompressDirectoryPath, java.nio.file.LinkOption.NOFOLLOW_LINKS)
        ) throw new SEDALibException(
            "Le chemin [" + unCompressDirectory + "] pointant le répertoire d'extraction ne désigne pas un répertoire"
        );
        this.digestAlgorithm = "SHA-512";
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
            throw new SEDALibException(
                "Lecture des métadonnées globales à partir du fichier [" + path + "] impossible",
                e
            );
        }
        return atgm;
    }

    /**
     * Do import the compressed file to ArchiveTransfer.
     *
     * @throws SEDALibException     if the XML manifest can't be read or is not in
     *                              expected
     *                              form, or compressed file can't be uncompressed
     * @throws InterruptedException if export process is interrupted
     */
    public void doImport() throws SEDALibException, InterruptedException {
        Path path;
        Iterator<Path> pi;

        Date d = new Date();
        start = Instant.now();
        String log = "sedalib: début de l'import du fichier compressé\n";
        log += "en [" + compressedFile + "]\n";
        log += " date=" + DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, log, null);

        droidFormat = DroidIdentifier.getFileDroidFormat(compressedFile);
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, "sedalib: droidFormat=" + droidFormat, null);

        // determine encoding from the compressed format
        if (encoding == null) {
            if (ZIP.equals(droidFormat)) encoding = "CP850";
            else encoding = "UTF8";
        }

        unCompressContainer();

        path = Paths.get(unCompressDirectory);
        try (Stream<Path> sp = Files.list(path)) {
            pi = sp.iterator();
            while (pi.hasNext()) {
                path = pi.next();
                if (path.getFileName().toString().equals("__GlobalMetadata.xml")) this.onDiskGlobalMetadataPath = path;
                else this.onDiskRootPaths.add(path);
            }
        } catch (IOException e) {
            throw new SEDALibException(
                "Impossible de lister les fichiers du répertoire [" + unCompressDirectory + "]",
                e
            );
        }

        this.diskToDataObjectPackageImporter = new DiskToDataObjectPackageImporter(
            this.onDiskRootPaths,
            true,
            extractTitleFromFileNameFunction,
            sedaLibProgressLogger
        );
        this.archiveTransfer = new ArchiveTransfer();

        if (onDiskGlobalMetadataPath != null) archiveTransfer.setGlobalMetadata(
            processGlobalMetadata(onDiskGlobalMetadataPath)
        );
        for (String patternString : ignorePatternStrings) diskToDataObjectPackageImporter.addIgnorePattern(
            patternString
        );
        diskToDataObjectPackageImporter.setDigestAlgorithm(digestAlgorithm);
        diskToDataObjectPackageImporter.doImport();
        archiveTransfer.setDataObjectPackage(diskToDataObjectPackageImporter.getDataObjectPackage());

        end = Instant.now();
        doProgressLog(
            sedaLibProgressLogger,
            SEDALibProgressLogger.GLOBAL,
            "sedalib: import d'un ArchiveTransfer depuis un fichier compressé sur disque terminé",
            null
        );
    }

    /**
     * Sets the digest algorithm.
     *
     * @param digestAlgorithm the digest algorithm
     */
    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
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
            if (start != null) result += "chargé en " + Duration.between(start, end).toString().substring(2) + "\n";
        }
        return result;
    }
}
