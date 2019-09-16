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
import fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.parameters.ZipImportContext;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.inout.importer.CompressedFileToArchiveTransferImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog.*;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The type Add thread.
 */
public class ExpandThread extends SwingWorker<String, String> {
    //input
    private Work work;
    private DataObjectPackageTreeNode targetNode;
    private BinaryDataObject bdoToExpand;
    private InOutDialog inOutDialog;
    //run output
    private CompressedFileToArchiveTransferImporter zi;
    private String summary;
    private Exception exitException;
    private int fileCounter;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Expand a binary data object replacing it by the archive unit hierarchy, in a dedicated thread.
     *
     * @param node        the displayed tree node
     * @param bdoToExpand the binary data object to expand
     */
    public static void launchExpandThread(DataObjectPackageTreeNode node, BinaryDataObject bdoToExpand) {
        ExpandThread expandThread;

        try {
            InOutDialog inOutDialog = new InOutDialog(ResipGraphicApp.getTheApp().mainWindow, "Expansion");
            expandThread = new ExpandThread(ResipGraphicApp.getTheApp().currentWork, node, bdoToExpand, inOutDialog);
            expandThread.execute();
            inOutDialog.setVisible(true);
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheApp().mainWindow,
                    "Erreur fatale, impossible de faire l'expansion \n->" + e.getMessage(), "Erreur",
                    UserInteractionDialog.ERROR_DIALOG, null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'expansion \n->" + e.getMessage());
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
    public ExpandThread(Work work, DataObjectPackageTreeNode targetNode, BinaryDataObject bdoToExpand,
                        InOutDialog dialog) {
        this.work = work;
        this.targetNode = targetNode;
        this.bdoToExpand = bdoToExpand;
        this.inOutDialog = dialog;
        this.summary = null;
        this.exitException = null;
        dialog.setThread(this);
    }

    private void setWorkFromDataObjectPackage(DataObjectPackage dataObjectPackage) {
        work.setDataObjectPackage(dataObjectPackage);
        ExportContext newExportContext = new ExportContext(Prefs.getInstance());
        if (dataObjectPackage.getManagementMetadataXmlData() != null)
            newExportContext.setManagementMetadataXmlData(
                    dataObjectPackage.getManagementMetadataXmlData());
        work.setExportContext(newExportContext);
    }

    private void recursiveDelete(File inFile) throws InterruptedException {
        if (inFile.isDirectory()) {
            for (File f : inFile.listFiles())
                recursiveDelete(f);
            inFile.delete();
        } else {
            inFile.delete();
            fileCounter++;
            doProgressLogIfStep(spl,SEDALibProgressLogger.OBJECTS_GROUP,fileCounter,fileCounter+" fichiers effacés");
        }
    }

    private String getTmpDirTarget(String workDir, String srcPathName, String id) throws ResipException, InterruptedException {
        String subDir = Paths.get(srcPathName).getFileName().toString() + "-" + id + "-tmpdir";
        String target = workDir + File.separator + subDir;
        if (Files.exists(Paths.get(target))) {
            UsedTmpDirDialog utdd = new UsedTmpDirDialog(ResipGraphicApp.getTheApp().mainWindow, workDir, subDir);
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
                exitException = e;
                return "KO";
            }
        }
        ResipGraphicApp.getTheApp().addThreadRunning = true;
        spl = null;
        try {
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP, (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            }, 1000, 2);
            doProgressLog(spl, GLOBAL, "Expansion du BinaryDataObject " + bdoToExpand.getInDataObjectPackageId() + ", fichier [" + bdoToExpand.fileInfo.getSimpleMetadata("Filename") + "]", null);

            //TODO add preferences for compressed filename import
            String encoding;
            if (bdoToExpand.formatIdentification.mimeType.toLowerCase().equals("application/zip"))
                encoding="CP850";
            else
                encoding="UTF8";
            ZipImportContext zic = new ZipImportContext(Prefs.getInstance());
            String target = getTmpDirTarget(zic.getWorkDir(), bdoToExpand.getOnDiskPathToString(),bdoToExpand.getInDataObjectPackageId());
            zi = new CompressedFileToArchiveTransferImporter(bdoToExpand.getOnDiskPathToString(), target, encoding, null, spl);
            for (String ip : zic.getIgnorePatternList())
                zi.addIgnorePattern(ip);
            zi.doImport();
            summary = zi.getSummary();
        } catch (Exception e) {
            exitException = e;
            return "KO";
        }
        return "OK";
    }

    @Override
    protected void done() {
        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled())
            doProgressLogWithoutInterruption(spl, GLOBAL, "Expansion annulée, les données n'ont pas été modifiées", null);
        else if (exitException != null)
            doProgressLogWithoutInterruption(spl, GLOBAL, "Erreur durant l'expansion, les données n'ont pas été modifiées", exitException);
        else {
            ResipGraphicApp.getTheApp().currentWork = this.work;
            List<ArchiveUnit> addedNodes = zi.getArchiveTransfer().getDataObjectPackage().getGhostRootAu().getChildrenAuList()
                    .getArchiveUnitList();
            targetNode.getArchiveUnit().getDataObjectPackage().moveContentFromDataObjectPackage(zi.getArchiveTransfer().getDataObjectPackage(), targetNode.getArchiveUnit());
            DataObject dataObject=targetNode.getArchiveUnit().getDataObjectRefList().getDataObjectList().get(0);
            if (dataObject instanceof DataObjectGroup){
                DataObjectGroup dog=(DataObjectGroup) dataObject;
                dog.removeDataObject(bdoToExpand);
                if (((dog.getPhysicalDataObjectList()==null) || (dog.getPhysicalDataObjectList().isEmpty())) &&
                        dog.getBinaryDataObjectList().isEmpty())
                targetNode.getArchiveUnit().removeDataObjectById(dog.getInDataObjectPackageId());
            }
            try {
                targetNode.getArchiveUnit().getDataObjectPackage().removeUnusedDataObjects(spl);
            } catch (InterruptedException ignored) {}
            DataObjectPackageTreeModel treeModel = targetNode.getTreeModel();
            int auRecursivCount = 0, ogRecursivCount = 0;
            for (ArchiveUnit au : addedNodes) {
                treeModel.generateArchiveUnitNode(au, targetNode);
                auRecursivCount += treeModel.findTreeNode(au).getAuRecursivCount() + 1;
                ogRecursivCount += treeModel.findTreeNode(au).getOgRecursivCount();
            }
            targetNode.actualiseRecursivCounts(auRecursivCount, ogRecursivCount);
            treeModel.nodeStructureChanged(targetNode);
            work.getCreationContext().setStructureChanged(true);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
            ResipGraphicApp.getTheApp().mainWindow.refreshInformations();
            doProgressLogWithoutInterruption(spl, GLOBAL, "Expansion et ajout terminés", null);
            doProgressLogWithoutInterruption(spl, GLOBAL, summary, null);
        }
        ResipGraphicApp.getTheApp().addThreadRunning = false;
    }
}
