package fr.gouv.vitam.tools.sedalib.utils;

import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import uk.gov.nationalarchives.droid.core.interfaces.IdentificationResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

public class CompressUtility {
    /**
     * Log and error string finals.
     */
    private static final String MODULE = "sedalib: ";

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

    // The encoding charset.

    private String encoding;

    // The progress logger.
    private SEDALibProgressLogger sedaLibProgressLogger;

    // The compressed file path.
    private Path compressedFilePath;

    // The uncompressed folder path.
    private Path uncompressedFolderPath;


    /**
     * Instantiates a new Compress utility.
     *
     * @param compressedFilePath     the compressed file path
     * @param uncompressedFolderPath the uncompressed folder path
     * @param encoding               the encoding
     * @param sedaLibProgressLogger  the seda lib progress logger
     */
    public CompressUtility(Path compressedFilePath, Path uncompressedFolderPath, String encoding,SEDALibProgressLogger sedaLibProgressLogger) {
        this.compressedFilePath=compressedFilePath;
        this.uncompressedFolderPath=uncompressedFolderPath;
        this.encoding=encoding;
        this.sedaLibProgressLogger=sedaLibProgressLogger;
    }

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

    private ArchiveInputStream createArchiveInputStream(Path compressedFilePath)
            throws SEDALibException, InterruptedException {
        String mimeType;
        FileInputStream fis=null;
        ArchiveInputStream ais=null;

        try {
            IdentificationResult ir = DroidIdentifier.getInstance().getIdentificationResult(compressedFilePath);
            mimeType = ir.getMimeType();
        } catch (SEDALibException e) {
            throw new SEDALibException(MODULE+ "impossible de faire l'identification de format Droid pour le fichier compressé ["
                    + compressedFilePath + "]", e);
        }

        try {
            fis = new FileInputStream(compressedFilePath.toFile()); // NOSONAR keep it open
            switch (mimeType) {
                case ZIP:
                    ais= new ZipArchiveInputStream(fis,encoding);
                    break;
                case XGZIP:
                case GZIP:
                    ais= new TarArchiveInputStream(new GzipCompressorInputStream(fis),encoding);
                    break;
                case BZIP2:
                    ais= new TarArchiveInputStream(new BZip2CompressorInputStream(fis),encoding);
                    break;
                default: //NOSONAR in a tar, Droid identify the first contained file format
                    doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL,
                            MODULE+ "format " + mimeType
                                    + " de compression inconnu, essai d'utilisation du format tar.",null);
                case TAR:
                    ais= new TarArchiveInputStream(fis,encoding);
                    break;
            }
        } catch (IOException e) {
            throw new SEDALibException(MODULE+ "impossible d'ouvrir le fichier compressé ["
                    + compressedFilePath + "]", e);
        }
        return ais;
    }

    /**
     * Uncompress
     *
     * @throws SEDALibException     the seda lib exception
     * @throws InterruptedException the interrupted exception
     */
    public void unCompress()
            throws SEDALibException, InterruptedException {
        int counter = 0;

        try (final ArchiveInputStream archiveInputStream = createArchiveInputStream(compressedFilePath)) {
            ArchiveEntry entry;

            uncompressedFolderPath = uncompressedFolderPath.toAbsolutePath().normalize();

            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (archiveInputStream.canReadEntryData(entry)) {
                    String entryName = entry.getName();
                    if (entryName.contains("?")) {
                        entryName = entryName.replace("?", "_");
                        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL,
                                "Le nom du fichier [" + entryName + "] a un problème d'encodage, le(s) caratère(s) problématique à été rempalcé par _ ", null);
                    }
                    final Path target = uncompressedFolderPath.resolve(entryName);
                    final Path parent = target.getParent();

                    if (parent != null && !Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }
                    if (!entry.isDirectory()) {
                        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS,
                                MODULE+"décompresse le fichier [" + entryName + "]", null);
                        Files.copy(archiveInputStream, target, StandardCopyOption.REPLACE_EXISTING);
                        counter++;
                        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
                                MODULE+counter + " fichiers extraits");
                    } else if (!Files.exists(target)) {
                        Files.createDirectories(target);
                    }
                }
            }
        } catch (final IOException e) {
            throw new SEDALibException("Impossible d'extraire le fichier compressé [" + compressedFilePath + "]", e);
        }
    }
}
