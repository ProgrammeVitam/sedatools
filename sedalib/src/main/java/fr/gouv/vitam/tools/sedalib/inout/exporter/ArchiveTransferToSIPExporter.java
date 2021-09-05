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
package fr.gouv.vitam.tools.sedalib.inout.exporter;

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;
import fr.gouv.vitam.tools.sedalib.xml.SEDAXMLStreamWriter;
import org.apache.commons.io.IOUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogIfStep;

/**
 * The Class ArchiveTransferToSIPExporter.
 * <p>
 * Class for ArchiveTransfer object export in a SEDA Submission Information
 * Packet (SIP).
 */
public class ArchiveTransferToSIPExporter {

    /** The archiveTransfer. */
    protected ArchiveTransfer archiveTransfer;

    /** The export path. */
    private Path exportPath;

    /** The parameters flag for manifest generation. */
    private boolean hierarchicalFlag, indentedFlag;

    /** The xml writer. */
    protected SEDAXMLStreamWriter xmlWriter;

    /** The start and end instants, for duration computation. */
    private Instant start, end;

    /** The progress logger. */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /** The export mode. */
    private boolean manifestOnly;

    /**
     * Instantiates a new ArchiveTransfer to SIP exporter.
     *
     * @param archiveTransfer the ArchiveTransfer
     * @param sedaLibProgressLogger  the progress logger
     */
    public ArchiveTransferToSIPExporter(ArchiveTransfer archiveTransfer, SEDALibProgressLogger sedaLibProgressLogger) {
        this.archiveTransfer = archiveTransfer;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
    }

    /**
     * Export SEDA XML manifest output stream.
     *
     * @param os               the OutputStream
     * @param hierarchicalFlag the hierarchical flag
     * @param indentedFlag         the indentedFlag
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void exportManifestOutputStream(OutputStream os, boolean hierarchicalFlag, boolean indentedFlag)
            throws SEDALibException, InterruptedException {
        try (SEDAXMLStreamWriter ixsw = new SEDAXMLStreamWriter(os, (indentedFlag ? IndentXMLTool.STANDARD_INDENT : 0))) {
            xmlWriter = ixsw;
            archiveTransfer.toSedaXml(xmlWriter, hierarchicalFlag, sedaLibProgressLogger);
        } catch (XMLStreamException e) {
            throw new SEDALibException("Echec d'écriture XML du manifest", e);
        }
    }

    /**
     * Do export the ArchiveTransfer to SEDA XML manifest.
     *
     * @param fileName         the file name
     * @param hierarchicalFlag the hierarchical flag
     * @param indentedFlag         the indentedFlag
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void doExportToSEDAXMLManifest(String fileName, boolean hierarchicalFlag, boolean indentedFlag)
            throws SEDALibException, InterruptedException {
        Date d = new Date();
        start = Instant.now();
        String log = "sedalib: début de l'export d'un ArchiveTransfer dans un manifest SEDA\n";
        log += "en [" + fileName + "] date=";
        log += DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, log, null);

        this.exportPath = Paths.get(fileName);
        this.hierarchicalFlag = hierarchicalFlag;
        this.indentedFlag = indentedFlag;
        this.manifestOnly = true;

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            exportManifestOutputStream(fos, hierarchicalFlag, indentedFlag);
        } catch (SEDALibException | IOException e) {
            throw new SEDALibException("Echec de l'export du manifest dans le fichier [" + fileName + "]", e);
        }

        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, "sedalib: export terminé", null);
        end = Instant.now();
    }

    /**
     * Get the ArchiveTransfer SEDA XML manifest as a String.
     *
     * @param hierarchicalFlag the hierarchical flag
     * @param indentedFlag         the indentedFlag
     * @return the ArchiveTransfert XML string
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public String getSEDAXMLManifest(boolean hierarchicalFlag, boolean indentedFlag)
            throws SEDALibException, InterruptedException {
        String result;
        Date d = new Date();
        start = Instant.now();
        String log = "sedalib: début de l'export d'un ArchiveTransfer dans un manifest SEDA\n";
        log += "en chaîne de caractères interne date=";
        log += DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, log, null);

        this.hierarchicalFlag = hierarchicalFlag;
        this.indentedFlag = indentedFlag;
        this.manifestOnly = true;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            exportManifestOutputStream(baos, hierarchicalFlag, indentedFlag);
            result=baos.toString("UTF8");
        } catch (SEDALibException | IOException e) {
            throw new SEDALibException("Echec de l'export du manifest", e);
        }

        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, "sedalib: export terminé", null);
        end = Instant.now();
        return result;
    }

    /**
     * Do export the ArchiveTransfer to SEDA Submission Information Packet (SIP).
     *
     * @param fileName         the file name
     * @param hierarchicalFlag the hierarchical flag
     * @param indentedFlag         the indentedFlag
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void doExportToSEDASIP(String fileName, boolean hierarchicalFlag, boolean indentedFlag)
            throws SEDALibException, InterruptedException {
        int counter = 0;
        Date d = new Date();
        start = Instant.now();
        String log = "sedalib: début de l'export d'un ArchiveTransfer dans un SIP\n";
        log += "en [" + fileName + "] date=";
        log += DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, log, null);

        this.exportPath = Paths.get(fileName);
        this.hierarchicalFlag = hierarchicalFlag;
        this.indentedFlag = indentedFlag;
        this.manifestOnly = false;

        try {
            Files.createDirectories(Paths.get(fileName).toAbsolutePath().getParent());
        } catch (IOException e1) {
            throw new SEDALibException("Impossible de créer le répertoire [" + Paths.get(fileName).toAbsolutePath().getParent().toString() + "]", e1);
        }
        try (ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(fileName))) {
            ZipEntry e = new ZipEntry("manifest.xml");
            // manifest
            zipout.putNextEntry(e);
            exportManifestOutputStream(zipout, hierarchicalFlag, indentedFlag);
            zipout.closeEntry();
            doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, "sedalib: manifest exporté", null);
            // all binary objects
            if (archiveTransfer.getDataObjectPackage().getDataObjectGroupCount() > 0) {
                for (Map.Entry<String, DataObjectGroup> pair : archiveTransfer.getDataObjectPackage()
                        .getDogInDataObjectPackageIdMap().entrySet()) {
                    DataObjectGroup og = pair.getValue();
                    if (og.getBinaryDataObjectList() != null) {
                        for (BinaryDataObject bo : og.getBinaryDataObjectList()) {
                            e = new ZipEntry(bo.uri.getValue());
                            zipout.putNextEntry(e);
                            try (FileInputStream fis = new FileInputStream(bo.getOnDiskPath().toFile())) {
                                IOUtils.copy(fis, zipout);
                            }
                            zipout.closeEntry();
                            counter++;
                            doProgressLogIfStep(sedaLibProgressLogger, SEDALibProgressLogger.OBJECTS_GROUP, counter,
                                    "sedalib: " + counter + " fichiers BinaryDataObject exportés");
                        }
                    }
                }
            }
        } catch (IOException | SEDALibException e) {
            throw new SEDALibException("Echec de l'export du SIP dans le fichier [" + fileName + "]", e);
        }

        doProgressLog(sedaLibProgressLogger,SEDALibProgressLogger.GLOBAL, "sedalib: export d'un ArchiveTransfer dans un SIP terminé", null);
        end = Instant.now();
    }

    /**
     * Gets the summary of the export process.
     *
     * @return the summary String
     */
    public String getSummary() {
        String result = "Export d'un ArchiveTransfer dans un ";
        if (manifestOnly)
            result += "manifest SEDA\n";
        else
            result += "SIP\n";
        result += "en [" + exportPath + "]\n";
        if (hierarchicalFlag)
            result += "avec une structure imbriquée ";
        else
            result += "avec une structure à plat ";
        if (indentedFlag)
            result += "en XML identé\n";
        else
            result += "en XML continu\n";

        if ((start != null) && (end != null))
            result += "effectué en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}
