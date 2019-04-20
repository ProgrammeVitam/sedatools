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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

public class AddThread extends SwingWorker<String, String> {
    //input
    private Work work;
    private DataObjectPackageTreeNode targetNode;
    private List<Path> lp;
    private InOutDialog inOutDialog;
    //run output
    private DiskToDataObjectPackageImporter di;
    private String summary;
    private Exception exitException;

    /**
     * Instantiates a new Add thread.
     *
     * @param work       the work
     * @param targetNode the target node (is null if tree as to be initialised before adding)
     * @param files      the files
     * @param dialog     the dialog
     */
    public AddThread(Work work, DataObjectPackageTreeNode targetNode, List<File> files,
                     InOutDialog dialog) {
        if (targetNode != null)
            this.work = work;
        else
            this.work = new Work(null,
                    new DiskImportContext(Prefs.getInstance().getPrefsContextNode()),
                    null);

        this.targetNode = targetNode;
        this.lp = new ArrayList<Path>();
        for (File f : files)
            lp.add(f.toPath());
        this.inOutDialog = dialog;
        this.summary = null;
        this.exitException = null;
        dialog.setThread(this);
    }

    private void setWorkFromDataObjectPackage(DataObjectPackage dataObjectPackage) {
        work.setDataObjectPackage(dataObjectPackage);
        ExportContext newExportContext = new ExportContext(Prefs.getInstance().getPrefsContextNode());
        if (dataObjectPackage.getManagementMetadataXmlData() != null)
            newExportContext.setManagementMetadataXmlData(
                    dataObjectPackage.getManagementMetadataXmlData());
        work.setExportContext(newExportContext);
    }


    @Override
    public String doInBackground() {
        while (ResipGraphicApp.getTheApp().addThreadRunning) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                exitException = e;
                return "";
            }
        }

        inOutDialog.extProgressTextArea.setText("Import par glisser/déposer de fichiers\n");
        ResipGraphicApp.getTheApp().addThreadRunning = true;
        SEDALibProgressLogger spl = null;
        try {
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP, (count, log) -> {
                String newLog = inOutDialog.extProgressTextArea.getText() + "\n" + log;
                inOutDialog.extProgressTextArea.setText(newLog);
                inOutDialog.extProgressTextArea.setCaretPosition(newLog.length());
            }, 1000,2);

            DiskImportContext dic;
            if (this.work.getCreationContext() instanceof DiskImportContext)
                dic = (DiskImportContext) this.work.getCreationContext();
            else
                dic = new DiskImportContext(Prefs.getInstance().getPrefsContextNode());
            di = new DiskToDataObjectPackageImporter(lp,dic.isNoLinkFlag(),null, spl);
            for (String ip : dic.getIgnorePatternList())
                di.addIgnorePattern(ip);
            di.doImport();
            summary = di.getSummary();
        } catch (Exception e) {
            try {
                if (spl != null)
                    spl.progressLog(SEDALibProgressLogger.GLOBAL, "Ajout de fichiers impossible\n->" + e.getMessage());
            } catch (InterruptedException ignored) {
            }
            exitException = e;
        }
        return summary;
    }

    @Override
    protected void done() {
        JTextArea progressTextArea = inOutDialog.extProgressTextArea;

        inOutDialog.okButton.setEnabled(true);
        inOutDialog.cancelButton.setEnabled(false);
        if (isCancelled() || (exitException != null))
            progressTextArea.setText(progressTextArea.getText() + "\n-> " + "les données n'ont pas été modifiées.");
        else if (targetNode == null) {
            ((DiskImportContext) work.getCreationContext()).setModelVersion(di.getModelVersion());
            setWorkFromDataObjectPackage(di.getDataObjectPackage());
            work.getCreationContext().setOnDiskInput("DragAndDrop");
            summary=di.getSummary();
            work.getCreationContext().setSummary(summary);
            progressTextArea.setText(progressTextArea.getText() + "\n-> " + summary);
            ResipGraphicApp.getTheApp().currentWork = work;
            ResipGraphicApp.getTheApp().setFilenameWork(null);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
            ResipGraphicApp.getTheApp().setContextLoaded(true);
            ResipGraphicApp.getTheApp().mainWindow.load();
        } else {
            ResipGraphicApp.getTheApp().currentWork = this.work;
            List<ArchiveUnit> addedNodes = di.getDataObjectPackage().getGhostRootAu().getChildrenAuList()
                    .getArchiveUnitList();
            targetNode.getArchiveUnit().getDataObjectPackage().moveContentFromDataObjectPackage(di.getDataObjectPackage(), targetNode.getArchiveUnit());
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

            progressTextArea.setText(progressTextArea.getText() + "\n-> " + summary);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
            ResipGraphicApp.getTheApp().mainWindow.refreshInformations();
        }
        ResipGraphicApp.getTheApp().addThreadRunning = false;
    }
}
