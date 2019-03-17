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
package fr.gouv.vitam.tools.resip.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.MainWindow;
import fr.gouv.vitam.tools.resip.frame.TargetDialog;
import fr.gouv.vitam.tools.resip.inout.MailImporter;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveDeliveryRequestReply;
import fr.gouv.vitam.tools.sedalib.core.ArchiveTransfer;
import fr.gouv.vitam.tools.sedalib.inout.importer.CSVTreeToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DIPToArchiveDeliveryRequestReplyImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.inout.importer.SIPToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;
import org.apache.commons.io.FileUtils;

import static fr.gouv.vitam.tools.resip.frame.TargetDialog.*;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;

public class ImportThread extends SwingWorker<Work, String> {
    //input
    private Work work;
    private InOutDialog inOutDialog;
    //run output
    private String summary;
    private Exception exitException;
    // logger
    private SEDALibProgressLogger spl;

    ImportThread(CreationContext cc, InOutDialog dialog) {
        this.work = new Work(null, cc, null);
        this.inOutDialog = dialog;
        this.summary = null;
        this.exitException = null;
        this.spl = null;
        dialog.setThread(this);
    }

    private void setWorkFromArchiveTransfer(ArchiveTransfer archiveTransfer) {
        work.setDataObjectPackage(archiveTransfer.getDataObjectPackage());
        ExportContext newExportContext = new ExportContext(Prefs.getInstance().getPrefsContextNode());
        if (archiveTransfer.getGlobalMetadata() != null)
            newExportContext.setArchiveTransferGlobalMetadata(archiveTransfer.getGlobalMetadata());
        if (archiveTransfer.getDataObjectPackage().getManagementMetadataXmlData() != null)
            newExportContext.setManagementMetadataXmlData(
                    archiveTransfer.getDataObjectPackage().getManagementMetadataXmlData());
        work.setExportContext(newExportContext);
    }

    private void setWorkFromArchiveDeliveryRequestReply(ArchiveDeliveryRequestReply archiveDeliveryRequestReply) {
        work.setDataObjectPackage(archiveDeliveryRequestReply.getDataObjectPackage());
        ExportContext newExportContext = new ExportContext(Prefs.getInstance().getPrefsContextNode());
        if (archiveDeliveryRequestReply.getGlobalMetadata() != null)
            newExportContext.setArchiveTransferGlobalMetadata(archiveDeliveryRequestReply.getGlobalMetadata());
        if (archiveDeliveryRequestReply.getDataObjectPackage().getManagementMetadataXmlData() != null)
            newExportContext.setManagementMetadataXmlData(
                    archiveDeliveryRequestReply.getDataObjectPackage().getManagementMetadataXmlData());
        work.setExportContext(newExportContext);
    }

    private String getNewTarget(String target) throws ResipException {
        TargetDialog td = new TargetDialog(ResipGraphicApp.getTheApp().mainWindow, target);
        td.setVisible(true);
        if (td.returnValue == STATUS_CLEAN) {
            try {
                if (Files.isDirectory(Paths.get(td.target)))
                    FileUtils.deleteDirectory(new File(td.target));
                else
                    Files.deleteIfExists(Paths.get(td.target));
            } catch (IOException e) {
                JTextArea progressTextArea = inOutDialog.extProgressTextArea;
                progressTextArea.setText(progressTextArea.getText() +
                        "\n-> l'effacement demandé sur [" + td.target + "] n'a pas été possible.\n->" +
                        e.getMessage());
                spl.log(GLOBAL, e.getMessage());
                this.cancel(false);
                throw new ResipException("Effacement impossible");
            }
            return td.target;
        } else if ((td.returnValue == STATUS_CONTINUE) || (td.returnValue == STATUS_CHANGE)) {
            return td.target;
        } else {// STATUS_CANCEL
            this.cancel(false);
            throw new ResipException("Opération annulée");
        }
    }

    @Override
    public Work doInBackground() {
        ResipGraphicApp.getTheApp().importThreadRunning = true;
        try {
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP, (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            }, 1000);

            if (work.getCreationContext() instanceof DiskImportContext) {
                inOutDialog.extProgressTextArea.setText("Import depuis une hiérarchie disque en " + work.getCreationContext().getOnDiskInput() + "\n");
                DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(work.getCreationContext().getOnDiskInput(),
                        spl);
                for (String ip : ((DiskImportContext) work.getCreationContext()).getIgnorePatternList())
                    di.addIgnorePattern(ip);
                di.doImport();
                ((DiskImportContext) work.getCreationContext()).setModelVersion(di.getModelVersion());
                setWorkFromArchiveTransfer(di.getArchiveTransfer());
                summary = di.getSummary();
            } else if (work.getCreationContext() instanceof SIPImportContext) {
                inOutDialog.extProgressTextArea.setText("Import depuis un fichier SIP en " + work.getCreationContext().getOnDiskInput() + "\n");
                SIPImportContext sic=(SIPImportContext)work.getCreationContext();
                String target = sic.getWorkDir() + File.separator + Paths.get(sic.getOnDiskInput()).getFileName().toString() + "-tmpdir";
                if (Files.exists(Paths.get(target)))
                    target = getNewTarget(target);
                SIPToArchiveTransferImporter si = new SIPToArchiveTransferImporter(sic.getOnDiskInput(),
                        target, spl);
                si.doImport();
                setWorkFromArchiveTransfer(si.getArchiveTransfer());
                summary = si.getSummary();
            } else if (work.getCreationContext() instanceof DIPImportContext) {
                inOutDialog.extProgressTextArea.setText("Import depuis un fichier DIP en " + work.getCreationContext().getOnDiskInput() + "\n");
                DIPImportContext dic=(DIPImportContext)work.getCreationContext();
                String target = dic.getWorkDir() + File.separator + Paths.get(dic.getOnDiskInput()).getFileName().toString() + "-tmpdir";
                if (Files.exists(Paths.get(target)))
                    target = getNewTarget(target);
                DIPToArchiveDeliveryRequestReplyImporter si = new DIPToArchiveDeliveryRequestReplyImporter(
                        dic.getOnDiskInput(), target, spl);
                si.doImport();
                setWorkFromArchiveDeliveryRequestReply(si.getArchiveDeliveryRequestReply());
                summary = si.getSummary();
            } else if (work.getCreationContext() instanceof CSVTreeImportContext) {
                inOutDialog.extProgressTextArea.setText("Import depuis un csv d'arbre de classement en " + work.getCreationContext().getOnDiskInput() + "\n");
                CSVTreeToDataObjectPackageImporter cti = new CSVTreeToDataObjectPackageImporter(
                        work.getCreationContext().getOnDiskInput(), "Cp1252", ';', spl);
                cti.doImport();
                work.setDataObjectPackage(cti.getDataObjectPackage());
                work.setExportContext(new ExportContext(Prefs.getInstance().getPrefsContextNode()));
                summary = cti.getSummary();
            } else if (work.getCreationContext() instanceof MailImportContext) {
                inOutDialog.extProgressTextArea.setText("Import depuis un conteneur courriel en " + work.getCreationContext().getOnDiskInput() + "\n");
                MailExtractProgressLogger mepl = null;
                mepl = new MailExtractProgressLogger(ResipLogger.getGlobalLogger().getLogger(), MailExtractProgressLogger.MESSAGE_GROUP, (count, log) -> {
                    String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                    inOutDialog.extProgressTextArea.setText(newLog);
                    inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
                }, 100);
                MailImportContext mic = (MailImportContext) work.getCreationContext();
                String target = mic.getWorkDir() + File.separator + Paths.get(mic.getOnDiskInput()).getFileName().toString() + "-tmpdir";
                if (Files.exists(Paths.get(target)))
                    target = getNewTarget(target);
                MailImporter mi = new MailImporter(mic.isExtractMessageTextFile(), mic.isExtractMessageTextMetadata(),
                        mic.isExtractAttachmentTextFile(), mic.isExtractAttachmentTextMetadata(), mic.getProtocol(),
                        mic.getDefaultCharsetName(), mic.getOnDiskInput(), mic.getMailFolder(), target, mepl);
                mi.doExtract();
                spl.progressLog(GLOBAL, "Extraction terminée\n" + mi.getSummary());

                List<Path> lp = new ArrayList<Path>();
                lp.add(Paths.get(mi.getTarget()));
                DiskToArchiveTransferImporter di = new DiskToArchiveTransferImporter(lp, spl);
                for (String ip : new DiskImportContext(Prefs.getInstance().getPrefsContextNode())
                        .getIgnorePatternList())
                    di.addIgnorePattern(ip);
                di.doImport();
                setWorkFromArchiveTransfer(di.getArchiveTransfer());
                summary = mi.getSummary() + "\n" + di.getSummary();
            }
            if (work.getDataObjectPackage() != null)
                summary += "\n" + work.doVitamNormalize(spl);
        } catch (Exception e) {
            try {
                if (spl != null)
                    spl.progressLog(GLOBAL, "Import impossible\n-> " + e.getMessage());
            } catch (InterruptedException ignored) {
            }
            exitException = e;
            work = null;
        }
        return work;
    }

    @Override
    protected void done() {
        JTextArea progressTextArea = inOutDialog.extProgressTextArea;
        ResipGraphicApp theApp = ResipGraphicApp.getTheApp();

        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled())
            progressTextArea.setText(progressTextArea.getText() + "\n-> " + "les données n'ont pas été modifiées.");
        else if ((work == null) || (work.getDataObjectPackage() == null))
            progressTextArea.setText(progressTextArea.getText() + "\n-> "
                    + "Erreur durant l'import, les données n'ont pas été modifiées." + (exitException != null ? "\n-> " + exitException.getMessage() : ""));
        else {
            work.getCreationContext().setSummary(summary);
            progressTextArea.setText(progressTextArea.getText() + "\n-> " + summary);
            Prefs.getInstance().setPrefsImportDirFromChild(work.getCreationContext().getOnDiskInput());
            theApp.currentWork = work;
            theApp.setFilenameWork(null);
            theApp.setModifiedContext(true);
            theApp.setContextLoaded(true);
            theApp.mainWindow.load();
        }
        theApp.importThreadRunning = false;
    }
}
