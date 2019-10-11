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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.parameters.CSVImportContext;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToDiskExporter;
import fr.gouv.vitam.tools.sedalib.inout.exporter.ArchiveTransferToSIPExporter;
import fr.gouv.vitam.tools.sedalib.inout.exporter.DataObjectPackageToCSVMetadataExporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.io.File;
import java.text.DecimalFormat;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The type Export thread.
 */
public class ExportThread extends SwingWorker<String, String> {

    /**
     * The Sip all export.
     */
    static public final int SIP_ALL_EXPORT = 1;
    /**
     * The Sip manifest export.
     */
    static public final int SIP_MANIFEST_EXPORT = 2;
    /**
     * The Disk export.
     */
    static public final int DISK_EXPORT = 3;
    /**
     * The Csv all disk export.
     */
    static public final int CSV_ALL_DISK_EXPORT = 4;
    /**
     * The Csv metadata file export.
     */
    static public final int CSV_METADATA_FILE_EXPORT = 5;
    /**
     * The Csv all zip export.
     */
    static public final int CSV_ALL_ZIP_EXPORT = 6;

    //input
    private Work work;
    private InOutDialog inOutDialog;
    private int exportType;
    //run output
    private String summary;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;


    /**
     * Instantiates a new Export thread.
     *
     * @param work       the work
     * @param exportType the export type
     * @param dialog     the dialog
     */
    public ExportThread(Work work, int exportType, InOutDialog dialog) {
        this.work = work;
        this.exportType = exportType;
        dialog.setThread(this);
        this.inOutDialog = dialog;
        this.exitThrowable = null;
    }

    /**
     * Readable file size.
     *
     * @param size the size
     * @return the string
     */
    public static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public String doInBackground() {
        spl = null;
        try {
                spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP, (count, log) -> {
                    String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                    inOutDialog.extProgressTextArea.setText(newLog);
                    inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
                }, 1000, 2);

            // first verify and reindex if neccesary
            if (work.getExportContext().isReindex()) {
                work.getDataObjectPackage().regenerateContinuousIds();
                ResipGraphicApp.getTheApp().mainWindow.treePane.allTreeChanged();
            }

            ArchiveTransfer archiveTransfer = new ArchiveTransfer();
            work.getDataObjectPackage().setManagementMetadataXmlData(work.getExportContext().getManagementMetadataXmlData());
            archiveTransfer.setDataObjectPackage(work.getDataObjectPackage());
            archiveTransfer.setGlobalMetadata(work.getExportContext().getArchiveTransferGlobalMetadata());
            if (work.getExportContext().isMetadataFilterFlag())
                work.getDataObjectPackage().setExportMetadataList(work.getExportContext().getKeptMetadataList());
            else
                work.getDataObjectPackage().setExportMetadataList(null);
            switch (exportType) {
                case SIP_MANIFEST_EXPORT:
                    inOutDialog.extProgressTextArea.setText("Export du manifest SEDA en " + work.getExportContext().getOnDiskOutput() + "\n");
                    ArchiveTransferToSIPExporter sm = new ArchiveTransferToSIPExporter(archiveTransfer, spl);
                    sm.doExportToSEDAXMLManifest(work.getExportContext().getOnDiskOutput(), work.getExportContext().isHierarchicalArchiveUnits(),
                            work.getExportContext().isIndented());
                    doProgressLog(spl, GLOBAL,
                            "resip: fichier sauvegardé (" + readableFileSize(new File(work.getExportContext().getOnDiskOutput()).length()) + ")", null);
                    summary = sm.getSummary();
                    break;
                case SIP_ALL_EXPORT:
                    inOutDialog.extProgressTextArea.setText("Export du SIP SEDA en " + work.getExportContext().getOnDiskOutput() + "\n");
                    ArchiveTransferToSIPExporter smm = new ArchiveTransferToSIPExporter(archiveTransfer, spl);
                    smm.doExportToSEDASIP(work.getExportContext().getOnDiskOutput(), work.getExportContext().isHierarchicalArchiveUnits(),
                            work.getExportContext().isIndented());
                    doProgressLog(spl, GLOBAL,
                            "resip: fichier sauvegardé (" + readableFileSize(new File(work.getExportContext().getOnDiskOutput()).length()) + ")", null);
                    summary = smm.getSummary();
                    break;
                case DISK_EXPORT:
                    inOutDialog.extProgressTextArea.setText("Export en hiérarchie disque complète en " + work.getExportContext().getOnDiskOutput() + "\n");
                    ArchiveTransferToDiskExporter de = new ArchiveTransferToDiskExporter(archiveTransfer, spl);
                    de.doExport(work.getExportContext().getOnDiskOutput());
                    summary = de.getSummary();
                    break;
                case CSV_ALL_DISK_EXPORT:
                    inOutDialog.extProgressTextArea.setText("Export en hiérarchie disque simplifiée avec fichier csv des métadonnées " + work.getExportContext().getOnDiskOutput() + "\n");
                    CSVImportContext cmic = new CSVImportContext(Prefs.getInstance());
                    DataObjectPackageToCSVMetadataExporter cme = new DataObjectPackageToCSVMetadataExporter(
                            archiveTransfer.getDataObjectPackage(), cmic.getCsvCharsetName(), cmic.getDelimiter(),
                            work.getExportContext().getUsageVersionSelectionMode(), work.getExportContext().getMaxNameSize(), spl);
                    cme.doExportToCSVDiskHierarchy(work.getExportContext().getOnDiskOutput(),"metadata.csv");
                    summary = cme.getSummary();
                    break;
                case CSV_ALL_ZIP_EXPORT:
                    inOutDialog.extProgressTextArea.setText("Export en hiérarchie disque simplifiée avec fichier csv des métadonnées " + work.getExportContext().getOnDiskOutput() + "\n");
                    CSVImportContext cmicz = new CSVImportContext(Prefs.getInstance());
                    DataObjectPackageToCSVMetadataExporter cmez = new DataObjectPackageToCSVMetadataExporter(
                            archiveTransfer.getDataObjectPackage(), cmicz.getCsvCharsetName(), cmicz.getDelimiter(),
                            work.getExportContext().getUsageVersionSelectionMode(), work.getExportContext().getMaxNameSize(), spl);
                    cmez.doExportToCSVZip(work.getExportContext().getOnDiskOutput(),"metadata.csv");
                    summary = cmez.getSummary();
                    break;
                case CSV_METADATA_FILE_EXPORT:
                    inOutDialog.extProgressTextArea.setText("Export en hiérarchie disque simplifiée avec fichier csv des métadonnées " + work.getExportContext().getOnDiskOutput() + "\n");
                    CSVImportContext cmicm = new CSVImportContext(Prefs.getInstance());
                    DataObjectPackageToCSVMetadataExporter cmem = new DataObjectPackageToCSVMetadataExporter(
                            archiveTransfer.getDataObjectPackage(), cmicm.getCsvCharsetName(), cmicm.getDelimiter(),
                            work.getExportContext().getUsageVersionSelectionMode(), work.getExportContext().getMaxNameSize(), spl);
                    cmem.doExportToCSVMetadataFile(work.getExportContext().getOnDiskOutput());
                    summary = cmem.getSummary();
                    break;

                default:
                    throw new ResipException("Export attendu inconnu");
            }
        } catch (Throwable e) {
            exitThrowable=e;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        JTextArea loadText = inOutDialog.extProgressTextArea;
        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled())
            doProgressLogWithoutInterruption(spl, GLOBAL,"resip: export annulé, les données seront partiellement sur le disque",null);
        else if (exitThrowable != null)
            doProgressLogWithoutInterruption(spl, GLOBAL,"resip: erreur durant l'export, les données seront partiellement sur le disque",exitThrowable);
        else {
            doProgressLogWithoutInterruption(spl, GLOBAL,"resip: export terminé",null);
            doProgressLogWithoutInterruption(spl, GLOBAL,summary,null);
            try {
                Prefs.getInstance().setPrefsExportDirFromChild(work.getExportContext().getOnDiskOutput());
            } catch (ResipException e) {
                doProgressLogWithoutInterruption(spl, GLOBAL,"resip: la localisation d'export par défaut n'a pu être actualisée dans les préférences",e);
            }
        }
    }
}
