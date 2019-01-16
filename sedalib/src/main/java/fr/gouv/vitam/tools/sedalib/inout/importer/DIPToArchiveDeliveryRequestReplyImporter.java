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
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLEventReader;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Enumeration;

/**
 * The Class DIPToArchiveDeliveryRequestReplyImporter.
 * <p>
 * Class for SEDA Dissemination Information Packet (DIP) import in
 * ArchiveDeliveryRequestReply object. Elements not used in Vitam DIPs, for the
 * moment, are not supported in this import.
 */
public class DIPToArchiveDeliveryRequestReplyImporter {

    /** The zip file containing the DIP. */
    private String zipFile;

    /** The directory for zip uncompress. */
    private String unCompressDirectory;

    /** The ArchiveDeliveryRequestReply SEDA message. */
    private ArchiveDeliveryRequestReply archiveDeliveryRequestReply;

    /** The start, end of import process. */
    Instant start, end;

    /** The progress logger. */
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
        try (FileInputStream fis=new FileInputStream(zipFile);
             ZipArchiveInputStream zais = new ZipArchiveInputStream(fis)) {
            // create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                folder.mkdir();
            }
            // get the zipped file list entry
            ArchiveEntry ze;
            while ((ze=zais.getNextEntry())!=null) {
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
                        if (sedaLibProgressLogger != null)
                            sedaLibProgressLogger.log(SEDALibProgressLogger.OBJECTS, "Unzip manifest [" + zipFile + "]");
                    } else if (sedaLibProgressLogger != null)
                        sedaLibProgressLogger.log(SEDALibProgressLogger.OBJECTS, "Unzip fichier [" + zipFile + "]");

                    // create all non exists folders
                    // else you will hit FileNotFoundException for compressed folder
                    if (!Files.exists(newPath.getParent()))
                        Files.createDirectories(newPath.getParent());

                    FileOutputStream fos = new FileOutputStream(newPath.toFile());
                    IOUtils.copy(zais, fos);
                    counter++;
                    if (sedaLibProgressLogger !=null)
                        sedaLibProgressLogger.progressLogIfStep(SEDALibProgressLogger.OBJECTS_GROUP, counter, Integer.toString(counter) + " fichiers " +
                                "extraits");
                    fos.close();
                }
            }
        } catch (IOException ex) {
            throw new SEDALibException("Impossible de décompresser le fichier [" + zipFile + "] dans le répertoire ["
                    + outputFolder + "]\n->" + ex.getMessage());
        }
        if (sedaLibProgressLogger !=null)
            sedaLibProgressLogger.progressLogIfStep(SEDALibProgressLogger.OBJECTS_GROUP, counter, Integer.toString(counter) + " fichiers " +
                    "extraits");
        if (manifest == null)
            throw new SEDALibException("DIP mal formé, pas de manifest");
        return manifest;
    }

    /**
     * Instantiates a new SEDA DIP importer.
     *
     * @param zipFile        the zip file
     * @param workDir        the work dir
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException if file or directory doesn't exist
     */
    public DIPToArchiveDeliveryRequestReplyImporter(String zipFile, String workDir,
                                                    SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException {
        Path pathFile, pathDirectory;

        pathFile = Paths.get(zipFile);
        if (!Files.isRegularFile(pathFile, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("Le chemin [" + zipFile + "] pointant vers le DIP ne désigne pas un fichier");
        pathDirectory = Paths.get(workDir);
        if (!Files.exists(pathDirectory))
            try {
                Files.createDirectories(pathDirectory);
            } catch (IOException e) {
                throw new SEDALibException("Impossible de créer le répertoire de travail [" + workDir + "]");
            }
        if (!Files.isDirectory(pathDirectory, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException(
                    "Le chemin [" + workDir + "] pointant le répertoire de travail ne désigne pas un répertoire");

        this.zipFile = zipFile;
        this.unCompressDirectory = pathDirectory.normalize().toString() + File.separator
                + pathFile.getFileName().toString() + "-tmpdir";
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
        if (sedaLibProgressLogger !=null)
            sedaLibProgressLogger.log(SEDALibProgressLogger.GLOBAL,
                "Début de l'import du DIP [" + zipFile + "] date=" + DateFormat.getDateTimeInstance().format(d));

        manifest = unZipDip(zipFile, unCompressDirectory);

        try (FileInputStream fis = new FileInputStream(unCompressDirectory + File.separator + manifest);
             SEDAXMLEventReader xmlReader = new SEDAXMLEventReader(fis)) {
            archiveDeliveryRequestReply = ArchiveDeliveryRequestReply.fromSedaXml(xmlReader, unCompressDirectory,
                    sedaLibProgressLogger);
        } catch (XMLStreamException | IOException e) {
            throw new SEDALibException(
                    "Impossible d'importer le fichier [" + manifest + "] comme manifest du DIP\n->" + e.getMessage());
        }

        end = Instant.now();
        if (sedaLibProgressLogger !=null)
            sedaLibProgressLogger.log(SEDALibProgressLogger.GLOBAL, getSummary());
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

        result = "Import depuis un DIP SEDA\n";
        result += "en [" + zipFile + "]\n";
        result += archiveDeliveryRequestReply.getDescription() + "\n";
        if (start != null)
            result += "chargé en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}
