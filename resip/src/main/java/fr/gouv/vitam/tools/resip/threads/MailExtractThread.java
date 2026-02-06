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
package fr.gouv.vitam.tools.resip.threads;

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.inout.MailImporter;
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.MailImportContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog.*;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The type Add thread.
 */
public class MailExtractThread extends SwingWorker<String, String> {
    //input
    private Work work;
    private DataObjectPackageTreeNode targetNode;
    private BinaryDataObject bdoToExpand;
    private InOutDialog inOutDialog;
    //run output
    private DiskToArchiveTransferImporter di;
    private String summary;
    private Throwable exitThrowable;
    private int fileCounter;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * MailExtract a binary data object replacing it by the archive unit hierarchy, in a dedicated thread.
     *
     * @param node        the displayed tree node
     * @param bdoToExpand the binary data object to expand
     */
    public static void launchMailExtractThread(DataObjectPackageTreeNode node, BinaryDataObject bdoToExpand) {
        MailExtractThread mailExtractThread;

        try {
            InOutDialog inOutDialog = new InOutDialog(ResipGraphicApp.mainWindow, "Extraction de messages");
            mailExtractThread = new MailExtractThread(ResipGraphicApp.getTheApp().currentWork, node, bdoToExpand, inOutDialog);
            mailExtractThread.execute();
            inOutDialog.setVisible(true);
        } catch (Throwable e) {
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.mainWindow,
                    "Erreur fatale, impossible de faire l'extraction de messages \n->" + e.getMessage(), "Erreur",
                    UserInteractionDialog.ERROR_DIALOG, null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'extraction de messages", e);
        }

    }

    /**
     * Instantiates a new Add thread.
     *
     * @param work        the work
     * @param targetNode  the target node (is null if tree as to be initialised before adding)
     * @param bdoToExpand the binary data object to expand
     * @param dialog      the dialog
     */
    public MailExtractThread(Work work, DataObjectPackageTreeNode targetNode, BinaryDataObject bdoToExpand,
                             InOutDialog dialog) {
        this.work = work;
        this.targetNode = targetNode;
        this.bdoToExpand = bdoToExpand;
        this.inOutDialog = dialog;
        this.summary = null;
        this.exitThrowable = null;
        dialog.setThread(this);
    }

    private void recursiveDelete(File inFile) throws InterruptedException {
        if (inFile.isDirectory()) {
            for (File f : inFile.listFiles())
                recursiveDelete(f);
            inFile.delete();
        } else {
            inFile.delete();
            fileCounter++;
            doProgressLogIfStep(spl, SEDALibProgressLogger.OBJECTS_GROUP, fileCounter, fileCounter + " fichiers effacés");
        }
    }

    private String getTmpDirTarget(String workDir, String srcPathName, String id) throws ResipException, InterruptedException {
        String subDir = Paths.get(srcPathName).getFileName().toString() + "-" + id + "-tmpdir";
        String target = workDir + File.separator + subDir;
        if (Files.exists(Paths.get(target))) {
            UsedTmpDirDialog utdd = new UsedTmpDirDialog(ResipGraphicApp.mainWindow, workDir, subDir);
            utdd.setVisible(true);
            if (utdd.getReturnValue() == STATUS_CLEAN) {
                fileCounter = 0;
                recursiveDelete(new File(utdd.getResult()));
                doProgressLog(spl, SEDALibProgressLogger.STEP, fileCounter + " fichiers effacés au total", null);
                target = utdd.getResult();
            } else if ((utdd.getReturnValue() == STATUS_CONTINUE) || (utdd.getReturnValue() == STATUS_CHANGE)) {
                target = utdd.getResult();
            } else {// STATUS_CANCEL
                this.cancel(false);
                throw new ResipException("Opération annulée");
            }
        }
        return target;
    }

    @Override
    public String doInBackground() {
        while (ResipGraphicApp.getTheApp().addThreadRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                exitThrowable = e;
                return "KO";
            }
        }
        ResipGraphicApp.getTheApp().addThreadRunning = true;
        spl = null;
        try {
            int localLogLevel;
            int localLogStep;
            if (ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()) {
                localLogLevel = SEDALibProgressLogger.OBJECTS_WARNINGS;
                localLogStep = 1;
            } else {
                localLogLevel = SEDALibProgressLogger.OBJECTS_GROUP;
                localLogStep = 1000;
            }
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), localLogLevel, (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            }, localLogStep, 2,SEDALibProgressLogger.OBJECTS_GROUP,1000);
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());

            doProgressLog(spl, GLOBAL, "Extraction de massages du BinaryDataObject " + bdoToExpand.getInDataObjectPackageId() + ", fichier [" + bdoToExpand.getMetadataFileInfo().getSimpleMetadata("Filename") + "]", null);

            MailExtractProgressLogger mepl = new MailExtractProgressLogger(ResipLogger.getGlobalLogger().getLogger(),
                    localLogLevel, (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            }, localLogStep, 2,MailExtractProgressLogger.MESSAGE_GROUP,1000);
            mepl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());
            MailImportContext mic = new MailImportContext(Preferences.getInstance());
            String target = getTmpDirTarget(mic.getWorkDir(), bdoToExpand.getOnDiskPathToString(), bdoToExpand.getInDataObjectPackageId());
            MailImporter mi = new MailImporter(mic.isExtractMessageTextFile(), mic.isExtractMessageTextMetadata(),
                    mic.isExtractAttachmentTextFile(), mic.isExtractAttachmentTextMetadata(),
                    StoreExtractor.getProtocolFromDroidFormat(bdoToExpand.getMetadataFormatIdentification().getSimpleMetadata("FormatId")),
                    mic.getDefaultCharsetName(), bdoToExpand.getOnDiskPathToString(), "", target, mepl);
            mi.doExtract();
            doProgressLog(spl, GLOBAL, "resip: extraction de messages terminée\n" + mi.getSummary(), null);
            List<Path> lp = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(mi.getTarget()))) {
                for (Path path : stream) {
                    if (!path.toString().endsWith("__ArchiveUnitMetadata.xml"))
                        lp.add(path);
                }
            }
            di = new DiskToArchiveTransferImporter(lp, spl);
            for (String ip : new DiskImportContext(Preferences.getInstance())
                    .getIgnorePatternList())
                di.addIgnorePattern(ip);
            di.doImport();
            summary = mi.getSummary() + "\n" + di.getSummary();
        } catch (Throwable e) {
            exitThrowable = e;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled())
            doProgressLogWithoutInterruption(spl, GLOBAL, "Extraction de messages annulée, les données n'ont pas été modifiées", null);
        else if (exitThrowable != null)
            doProgressLogWithoutInterruption(spl, GLOBAL, "Erreur durant l'extraction de messages, les données n'ont pas été modifiées", exitThrowable);
        else {
            ResipGraphicApp.getTheApp().currentWork = this.work;
            List<ArchiveUnit> addedNodes = di.getArchiveTransfer().getDataObjectPackage().getGhostRootAu().getChildrenAuList()
                    .getArchiveUnitList();
            targetNode.getArchiveUnit().getDataObjectPackage().moveContentFromDataObjectPackage(di.getArchiveTransfer().getDataObjectPackage(), targetNode.getArchiveUnit());
            DataObject dataObject = targetNode.getArchiveUnit().getDataObjectRefList().getDataObjectList().get(0);
            if (dataObject instanceof DataObjectGroup) {
                DataObjectGroup dog = (DataObjectGroup) dataObject;
                dog.removeDataObject(bdoToExpand);
                if (((dog.getPhysicalDataObjectList() == null) || (dog.getPhysicalDataObjectList().isEmpty())) &&
                        dog.getBinaryDataObjectList().isEmpty()){
                    targetNode.getArchiveUnit().removeEmptyDataObjectGroup();
                    try {
                        targetNode.getArchiveUnit().getContent().addNewMetadata("DescriptionLevel", "RecordGrp");
                    } catch (SEDALibException e) {
                        //ignored
                    }
                }
            }
            try {
                targetNode.getArchiveUnit().getDataObjectPackage().removeUnusedDataObjects(spl);
            } catch (InterruptedException ignored) {
                //ignore
            }
            DataObjectPackageTreeModel treeModel = targetNode.getTreeModel();
            int auRecursivCount = 0;
            int ogRecursivCount = 0;
            for (ArchiveUnit au : addedNodes) {
                treeModel.generateArchiveUnitNode(au, targetNode);
                auRecursivCount += treeModel.findTreeNode(au).getAuRecursivCount() + 1;
                ogRecursivCount += treeModel.findTreeNode(au).getOgRecursivCount();
            }
            targetNode.actualiseRecursivCounts(auRecursivCount, ogRecursivCount);
            treeModel.nodeStructureChanged(targetNode);
            work.getCreationContext().setStructureChanged(true);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
            ResipGraphicApp.mainWindow.treePane.reset();
            doProgressLogWithoutInterruption(spl, GLOBAL, "Extraction de messages et ajout terminés", null);
            doProgressLogWithoutInterruption(spl, GLOBAL, summary, null);
        }
        ResipGraphicApp.getTheApp().addThreadRunning = false;
    }
}
