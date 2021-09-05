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

import fr.gouv.vitam.tools.sedalib.core.ArchiveDeliveryRequestReply;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class DIPToArchiveDeliveryRequestReplyImporter.
 * <p>
 * Class for SEDA Dissemination Information Packet (DIP) import in
 * ArchiveDeliveryRequestReply object. Elements not used in Vitam DIPs, for the
 * moment, are not supported in this import.
 */
public class DIPToArchiveDeliveryRequestReplyImporter {

    /**
     * The zip file containing the DIP.
     */
    private String zipFile;

    /**
     * The directory for zip uncompress.
     */
    private String unCompressDirectory;

    /**
     * The ArchiveDeliveryRequestReply SEDA message.
     */
    private ArchiveDeliveryRequestReply archiveDeliveryRequestReply;

    /**
     * The start, end of import process.
     */
    Instant start, end;

    /**
     * The progress logger.
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * Unzip file.
     *
     * @param zipFile      input zip file
     * @param outputFolder the output folder
     * @return the manifest file name
     * @throws SEDALibException     zip uncompress problem or lack of manifest file
     *                              (name has to begin by manifest in any case)
     * @throws InterruptedException if import process is interrupted
     */
    public String unZipDip(String zipFile, String outputFolder) throws SEDALibException, InterruptedException {
        String manifest = null;
        int counter = 0;
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipArchiveInputStream zais = new ZipArchiveInputStream(fis)) {
            // create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                folder.mkdir();
            }
            // get the zipped file list entry
            ArchiveEntry ze;
            while ((ze = zais.getNextEntry()) != null) {
                String fileName = ze.getName().trim();
                // change any case ConTenT to lowercase content on import as in fromSEDA in
                // BinaryDataObject
                if (fileName.toLowerCase().startsWith("content"))
                    fileName = "content" + fileName.substring(7);

                Path newPath = Paths.get(outputFolder + File.separator + fileName);

                if (fileName.endsWith("/")) {
                    if (!Files.exists(newPath))
                        Files.createDirectories(newPath);
                } else {
                    if (fileName.toLowerCase().matches("[^/\\\\]*manifest.*\\.xml")) {
                        if (manifest != null)
                            throw new SEDALibException("DIP mal formé, plusieurs fichiers manifest potentiels");
                        manifest = fileName;
                        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS, "sedalib: unzip manifest [" + zipFile + "]", null);
                    } else
                        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS, "sedalib: unzip fichier [" + zipFile + "]", null);

                    // create all non exists folders
                    // else you will hit FileNotFoundException for compressed folder
                    if (!Files.exists(newPath.getParent()))
                        Files.createDirectories(newPath.getParent());

                    try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        IOUtils.copy(zais, fos);
                        counter++;
                        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter, "sedalib: " + counter + " fichiers " +
                                "extraits");
                    }
                }
            }
        } catch (IOException ex) {
            throw new SEDALibException("Impossible de décompresser le fichier [" + zipFile + "] dans le répertoire ["
                    + outputFolder + "]", ex);
        }
        doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.STEP, counter, "sedalib: " + counter + " fichiers " +
                    "extraits");
        if (manifest == null)
            throw new SEDALibException("DIP mal formé, pas de manifest");
        return manifest;
    }

    /**
     * Instantiates a new SEDA DIP importer.
     *
     * @param zipFile               the zip file
     * @param unCompressDirectory   the directory where the zipfile is uncompressed
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException if file or directory doesn't exist
     */
    public DIPToArchiveDeliveryRequestReplyImporter(String zipFile, String unCompressDirectory,
                                                    SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException {
        Path zipFilePath, unCompressDirectoryPath;

        zipFilePath = Paths.get(zipFile);
        if (!Files.isRegularFile(zipFilePath, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("Le chemin [" + zipFile + "] pointant vers le DIP ne désigne pas un fichier");
        unCompressDirectoryPath = Paths.get(unCompressDirectory);
        if (!Files.exists(unCompressDirectoryPath))
            try {
                Files.createDirectories(unCompressDirectoryPath);
            } catch (IOException e) {
                throw new SEDALibException("Impossible de créer le répertoire d'extraction [" + unCompressDirectory + "]", e);
            }
        if (!Files.isDirectory(unCompressDirectoryPath, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException(
                    "Le chemin [" + unCompressDirectory + "] pointant le répertoire d'extraction ne désigne pas un répertoire");

        this.zipFile = zipFile;
        this.unCompressDirectory = unCompressDirectory;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
    }

    /**
     * Do import the SEDA DIP to ArchiveDeliveryRequestReply.
     *
     * @throws SEDALibException     if the XML manifest can't be read or is not in expected
     *                              form, or zip file can't be uncompressed
     * @throws InterruptedException if export process is interrupted
     */
    public void doImport() throws SEDALibException, InterruptedException {
        String manifest;

        Date d = new Date();
        start = Instant.now();
        String log = "sedalib: début de l'import du DIP\n";
        log += "en [" + zipFile + "]\n";
        log += "date=" + DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, log, null);

        manifest = unZipDip(zipFile, unCompressDirectory);

        try (FileInputStream fis = new FileInputStream(unCompressDirectory + File.separator + manifest);
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(fis)) {
            archiveDeliveryRequestReply = ArchiveDeliveryRequestReply.fromSedaXml(xmlReader, unCompressDirectory,
                    sedaLibProgressLogger);
        } catch (XMLStreamException | IOException e) {
            throw new SEDALibException(
                    "Impossible d'importer le fichier [" + manifest + "] comme manifest du DIP", e);
        }

        end = Instant.now();
        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, "sedalib: import du DIP terminé", null);
    }

    /**
     * Gets the ArchiveDeliveryRequestReply.
     *
     * @return the ArchiveDeliveryRequestReply
     */
    public ArchiveDeliveryRequestReply getArchiveDeliveryRequestReply() {
        return archiveDeliveryRequestReply;
    }

    /**
     * Gets the summary of the import process.
     *
     * @return the summary
     */
    public String getSummary() {
        String result;

        result = archiveDeliveryRequestReply.getDescription() + "\n";
        if (start != null)
            result += "chargé en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}
