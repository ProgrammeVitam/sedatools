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
package fr.gouv.vitam.tools.sedalib.inout.exporter;

import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.GlobalMetadata;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * The Class ArchiveTransferToDiskExporter.
 * <p>
 * Class for ArchiveTransfer object export on disk hierarchy.
 * <p>
 * It will export in the output directory:
 * <ul>
 * <li>GlobalMetadata XML fragments in the __GlobalMetadata.xml file</li>
 * <li>ManagementMetadata XML element at the end of DataObjectPackage in the
 * __ManagementMetadata.xml file</li>
 * <li>each root ArchiveUnit as a sub directory, and recursively all the
 * DataObjectPackage structure (see {@link DataObjectPackageToDiskExporter} for
 * details)</li>
 * </ul>
 * the export is compliant to Model V2-Extended model
 * ({@link fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter}
 */
public class ArchiveTransferToDiskExporter {

    /**
     * The DataObjectPackage to disk exporter.
     */
    private DataObjectPackageToDiskExporter dataObjectPackageToDiskExporter;

    /**
     * The export path.
     */
    private Path exportPath;

    /**
     * The ArchiveTransfer.
     */
    private final ArchiveTransfer archiveTransfer;

    /**
     * The start and end instants, for duration computation.
     */
    private Instant start, end;

    /**
     * The progress logger.
     */
    private final SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * Instantiates a new ArchiveTransfer to disk exporter
     *
     * @param archiveTransfer       the archive transfer
     * @param sedaLibProgressLogger the progress logger
     */
    public ArchiveTransferToDiskExporter(ArchiveTransfer archiveTransfer, SEDALibProgressLogger sedaLibProgressLogger) {
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.archiveTransfer = archiveTransfer;
        dataObjectPackageToDiskExporter = new DataObjectPackageToDiskExporter(
            archiveTransfer.getDataObjectPackage(),
            sedaLibProgressLogger
        );
    }

    /**
     * Export ArchiveTransfer global metadata.
     *
     * @param globalMetadata the GlobalMetadata
     * @param containerPath  the container path
     * @throws SEDALibException if writing has failed
     */
    public void exportArchiveTransferGlobalMetadata(GlobalMetadata globalMetadata, Path containerPath)
        throws SEDALibException {
        Path targetOnDiskPath;
        // write binary file
        targetOnDiskPath = containerPath.resolve("__GlobalMetadata.xml");
        try (
            FileOutputStream fos = new FileOutputStream(targetOnDiskPath.toFile());
            Writer rawWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8)
        ) {
            rawWriter.write(globalMetadata.toSedaXmlFragments());
        } catch (Exception e) {
            throw new SEDALibException(
                "Ecriture des métadonnées globales [" + targetOnDiskPath + "] impossible\n->" + e.getMessage()
            );
        }
    }

    /**
     * Do export the ArchiveTransfer to a disk hierarchy.
     * <p>
     * It will export in the output directory:
     * <ul>
     * <li>GlobalMetadata XML fragments in the __GlobalMetadata.xml file</li>
     * <li>ManagementMetadata XML element at the end of DataObjectPackage in the
     * __ManagementMetadata.xml file</li>
     * <li>each root ArchiveUnit as a sub directory, and recursively all the
     * DataObjectPackage structure</li>
     * </ul>
     *
     * @param directoryName the export directory name
     * @throws SEDALibException     if writing has failed
     * @throws InterruptedException if export process is interrupted
     */
    public void doExport(String directoryName) throws SEDALibException, InterruptedException {
        Date d = new Date();
        start = Instant.now();
        String log = "Début de l'export d'un ArchiveTransfer dans une hiérarchie sur disque\n";
        log += "en [" + directoryName + "]";
        log += " date=" + DateFormat.getDateTimeInstance().format(d);
        doProgressLog(sedaLibProgressLogger, SEDALibProgressLogger.GLOBAL, log, null);

        exportPath = Paths.get(directoryName);
        try {
            Files.createDirectories(exportPath);
        } catch (Exception e) {
            throw new SEDALibException(
                "Création du répertoire d'export [" + exportPath + "] impossible\n->" + e.getMessage()
            );
        }

        if (archiveTransfer.getGlobalMetadata() != null) exportArchiveTransferGlobalMetadata(
            archiveTransfer.getGlobalMetadata(),
            exportPath
        );
        dataObjectPackageToDiskExporter.doExport(directoryName);

        doProgressLog(
            sedaLibProgressLogger,
            SEDALibProgressLogger.GLOBAL,
            "sedalib: export d'un ArchiveTransfer dans une hiérarchie sur disque terminé",
            null
        );
        end = Instant.now();
    }

    /**
     * Gets the summary of the export process.
     *
     * @return the summary String
     */
    public String getSummary() {
        String result = "Export d'un ArchiveTransfer dans une hiérarchie sur disque\n";
        result += "en [" + exportPath + "]\n";
        result += "encodé selon un modèle V2 de la structure\n";
        if ((start != null) && (end != null)) result +=
        "effectué en " + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }
}
