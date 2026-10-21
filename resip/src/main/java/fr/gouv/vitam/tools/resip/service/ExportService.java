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
package fr.gouv.vitam.tools.resip.service;

import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.CSVImportContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToDiskExporter;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import fr.gouv.vitam.tools.sedaschema.SedaFlavor;

import java.io.File;
import java.text.DecimalFormat;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * Exports, shows and validates the archive transfer of a work as its export context describes it,
 * without any user interface: the graphic application and the command line both export through it.
 */
public class ExportService {

    /**
     * The kinds of export.
     */
    public enum ExportType {
        /**
         * The SIP, manifest and files, in a zip.
         */
        SIP_ALL,
        /**
         * The SEDA manifest only.
         */
        SIP_MANIFEST,
        /**
         * The complete disk hierarchy, with the metadata files.
         */
        DISK,
        /**
         * The simplified disk hierarchy, with a csv file of the metadata.
         */
        CSV_ALL_DISK,
        /**
         * The csv file of the metadata only.
         */
        CSV_METADATA_FILE,
        /**
         * The simplified disk hierarchy, with a csv file of the metadata, in a zip.
         */
        CSV_ALL_ZIP,
    }

    private static final String CSV_METADATA_FILENAME = "metadata.csv";

    private final Preferences preferences;

    /**
     * Instantiates a new export service.
     *
     * @param preferences the preferences, giving the csv delimiter
     */
    public ExportService(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * @param size a size in bytes
     * @return the size in a readable form, such as "1.2 MB"
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Exports a work in its export context output, after a reindexation if the context asks it.
     *
     * @param work       the work
     * @param exportType the kind of export
     * @param spl        the progress logger, or null
     * @return the summary of the export
     * @throws SEDALibException     if the export fails
     * @throws InterruptedException if the export is interrupted
     */
    public String export(Work work, ExportType exportType, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException {
        ExportContext exportContext = work.getExportContext();
        String output = exportContext.getOnDiskOutput();
        reindexIfAsked(work);
        ArchiveTransfer archiveTransfer = toArchiveTransfer(work, true);
        switch (exportType) {
            case SIP_MANIFEST: {
                ArchiveTransferToSIPExporter exporter = new ArchiveTransferToSIPExporter(archiveTransfer, spl);
                exporter.doExportToSEDAXMLManifest(
                    output,
                    exportContext.isHierarchicalArchiveUnits(),
                    exportContext.isIndented()
                );
                logSavedFileSize(output, spl);
                return exporter.getSummary();
            }
            case SIP_ALL: {
                ArchiveTransferToSIPExporter exporter = new ArchiveTransferToSIPExporter(archiveTransfer, spl);
                exporter.doExportToSEDASIP(
                    output,
                    exportContext.isHierarchicalArchiveUnits(),
                    exportContext.isIndented()
                );
                logSavedFileSize(output, spl);
                return exporter.getSummary();
            }
            case DISK: {
                ArchiveTransferToDiskExporter exporter = new ArchiveTransferToDiskExporter(archiveTransfer, spl);
                exporter.doExport(output);
                return exporter.getSummary();
            }
            case CSV_ALL_DISK: {
                DataObjectPackageToCSVMetadataExporter exporter = csvExporter(archiveTransfer, exportContext, spl);
                exporter.doExportToCSVDiskHierarchy(output, CSV_METADATA_FILENAME);
                return exporter.getSummary();
            }
            case CSV_ALL_ZIP: {
                DataObjectPackageToCSVMetadataExporter exporter = csvExporter(archiveTransfer, exportContext, spl);
                exporter.doExportToCSVZip(output, CSV_METADATA_FILENAME);
                return exporter.getSummary();
            }
            case CSV_METADATA_FILE: {
                DataObjectPackageToCSVMetadataExporter exporter = csvExporter(archiveTransfer, exportContext, spl);
                exporter.doExportToCSVMetadataFile(output);
                return exporter.getSummary();
            }
            default:
                throw new SEDALibException("Export attendu inconnu [" + exportType + "]");
        }
    }

    /**
     * Generates the SEDA manifest of a work, as it would be exported, without reindexation.
     *
     * @param work the work
     * @param spl  the progress logger, or null
     * @return the manifest
     * @throws SEDALibException     if the manifest can't be generated
     * @throws InterruptedException if the generation is interrupted
     */
    public String getManifest(Work work, SEDALibProgressLogger spl) throws SEDALibException, InterruptedException {
        ArchiveTransferToSIPExporter exporter = new ArchiveTransferToSIPExporter(toArchiveTransfer(work, true), spl);
        return exporter.getSEDAXMLManifest(
            work.getExportContext().isHierarchicalArchiveUnits(),
            work.getExportContext().isIndented()
        );
    }

    /**
     * Validates the whole archive transfer of a work, against the XSD of its SEDA version or a
     * profile, after a reindexation if the export context asks it.
     *
     * @param work            the work
     * @param profileFileName the profile file, or null to validate against the XSD
     * @param sedaFlavor      the flavour of the XSD, official or Vitam, unused with a profile
     * @param spl             the progress logger, or null
     * @throws SEDALibException     if the archive transfer is not valid
     * @throws InterruptedException if the validation is interrupted
     */
    public void validate(Work work, String profileFileName, SedaFlavor sedaFlavor, SEDALibProgressLogger spl)
        throws SEDALibException, InterruptedException {
        reindexIfAsked(work);
        // the metadata filter is an export choice, the validation is of the whole archive transfer
        ArchiveTransfer archiveTransfer = toArchiveTransfer(work, false);
        if (profileFileName == null) archiveTransfer.sedaSchemaValidate(sedaFlavor, spl);
        else archiveTransfer.sedaProfileValidate(profileFileName, spl);
    }

    private static void reindexIfAsked(Work work) {
        if (work.getExportContext().isReindex()) work.getDataObjectPackage().regenerateContinuousIds();
    }

    // the package of the work with the management and global metadata of its export context
    private static ArchiveTransfer toArchiveTransfer(Work work, boolean filterMetadata) {
        ExportContext exportContext = work.getExportContext();
        DataObjectPackage dataObjectPackage = work.getDataObjectPackage();
        dataObjectPackage.setManagementMetadataXmlData(exportContext.getManagementMetadataXmlData());
        if (filterMetadata) dataObjectPackage.setExportMetadataList(
            exportContext.isMetadataFilterFlag() ? exportContext.getKeptMetadataList() : null
        );
        ArchiveTransfer archiveTransfer = new ArchiveTransfer(dataObjectPackage.getSedaVersion());
        archiveTransfer.setDataObjectPackage(dataObjectPackage);
        archiveTransfer.setGlobalMetadata(exportContext.getArchiveTransferGlobalMetadata());
        return archiveTransfer;
    }

    private DataObjectPackageToCSVMetadataExporter csvExporter(
        ArchiveTransfer archiveTransfer,
        ExportContext exportContext,
        SEDALibProgressLogger spl
    ) throws SEDALibException {
        return new DataObjectPackageToCSVMetadataExporter(
            archiveTransfer.getDataObjectPackage(),
            exportContext.getCsvCharsetName(),
            new CSVImportContext(preferences).getDelimiter(),
            exportContext.getUsageVersionSelectionMode(),
            exportContext.isCsvExtendedFormat(),
            exportContext.getMaxNameSize(),
            spl
        );
    }

    private static void logSavedFileSize(String output, SEDALibProgressLogger spl) throws InterruptedException {
        doProgressLog(
            spl,
            GLOBAL,
            "resip: fichier sauvegardé (" + readableFileSize(new File(output).length()) + ")",
            null
        );
    }
}
