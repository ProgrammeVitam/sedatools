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

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.InOutDialog;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.parameters.DiskImportContext;
import fr.gouv.vitam.tools.resip.parameters.ExportContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.inout.importer.DiskToDataObjectPackageImporter;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLogWithoutInterruption;

/**
 * The type Add thread.
 */
public class AddThread extends SwingWorker<String, String> {

    //input
    private Work work;
    private DataObjectPackageTreeNode targetNode;
    private List<Path> lp;
    private InOutDialog inOutDialog;
    //run output
    private DiskToDataObjectPackageImporter di;
    private String summary;
    private Throwable exitThrowable;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Adds the new files, with a dedicated thread.
     *
     * @param files      the files
     * @param targetPath the target path
     */
    public static void launchAddThread(List<File> files, TreePath targetPath) {
        AddThread addThread;

        try {
            InOutDialog inOutDialog = new InOutDialog(ResipGraphicApp.getTheWindow(), "Import");
            addThread = new AddThread(
                ResipGraphicApp.getTheApp().currentWork,
                (targetPath == null ? null : (DataObjectPackageTreeNode) targetPath.getLastPathComponent()),
                files,
                inOutDialog
            );
            addThread.execute();
            inOutDialog.setVisible(true);
        } catch (Throwable e) {
            UserInteractionDialog.getUserAnswer(
                ResipGraphicApp.getTheWindow(),
                "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                "Erreur",
                UserInteractionDialog.ERROR_DIALOG,
                null
            );
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import", e);
        }
    }

    /**
     * Instantiates a new Add thread.
     *
     * @param work       the work
     * @param targetNode the target node (is null if tree as to be initialised before adding)
     * @param files      the files
     * @param dialog     the dialog
     */
    public AddThread(Work work, DataObjectPackageTreeNode targetNode, List<File> files, InOutDialog dialog) {
        if (targetNode != null) this.work = work;
        else this.work = new Work(null, new DiskImportContext(Preferences.getInstance()), null);

        this.targetNode = targetNode;
        this.lp = new ArrayList<>();
        for (File f : files) lp.add(f.toPath());
        this.inOutDialog = dialog;
        this.summary = null;
        this.exitThrowable = null;
        this.spl = new ThreadLoggerFactory(
            inOutDialog.extProgressTextArea,
            ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()
        ).getLogger();
        dialog.setThread(this);
    }

    private void setWorkFromDataObjectPackage(DataObjectPackage dataObjectPackage) {
        work.setDataObjectPackage(dataObjectPackage);
        ExportContext newExportContext = new ExportContext(Preferences.getInstance());
        if (dataObjectPackage.getManagementMetadataXmlData() != null) newExportContext.setManagementMetadataXmlData(
            dataObjectPackage.getManagementMetadataXmlData()
        );
        work.setExportContext(newExportContext);
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
        inOutDialog.extProgressTextArea.setText("Import par glisser/déposer de fichiers\n");
        ResipGraphicApp.getTheApp().addThreadRunning = true;
        try {
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());

            DiskImportContext dic;
            if (this.work.getCreationContext() instanceof DiskImportContext) dic =
                (DiskImportContext) this.work.getCreationContext();
            else dic = new DiskImportContext(Preferences.getInstance());
            di = new DiskToDataObjectPackageImporter(lp, dic.isNoLinkFlag(), null, spl);
            for (String ip : dic.getIgnorePatternList()) di.addIgnorePattern(ip);
            di.doImport();
            summary = di.getSummary();
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
        if (isCancelled()) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "Ajout annulé, les données n'ont pas été modifiées",
            null
        );
        else if (exitThrowable != null) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "Erreur durant l'ajout, les données n'ont pas été modifiées",
            exitThrowable
        );
        else if (targetNode == null) {
            ((DiskImportContext) work.getCreationContext()).setModelVersion(di.getModelVersion());
            setWorkFromDataObjectPackage(di.getDataObjectPackage());
            work.getCreationContext().setOnDiskInput("DragAndDrop");
            work.getCreationContext().setSummary(summary);
            ResipGraphicApp.getTheApp().currentWork = work;
            ResipGraphicApp.getTheApp().setFilenameWork(null);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
            ResipGraphicApp.getTheApp().setContextLoaded(true);
            ResipGraphicApp.getTheWindow().load();
            doProgressLogWithoutInterruption(spl, GLOBAL, "Ajout terminé", null);
            doProgressLogWithoutInterruption(spl, GLOBAL, summary, null);
        } else {
            ResipGraphicApp.getTheApp().currentWork = this.work;
            List<ArchiveUnit> addedNodes = di
                .getDataObjectPackage()
                .getGhostRootAu()
                .getChildrenAuList()
                .getArchiveUnitList();
            targetNode
                .getArchiveUnit()
                .getDataObjectPackage()
                .moveContentFromDataObjectPackage(di.getDataObjectPackage(), targetNode.getArchiveUnit());
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
            ResipGraphicApp.getTheWindow().treePane.reset();
            doProgressLogWithoutInterruption(spl, GLOBAL, "Ajout terminé", null);
            doProgressLogWithoutInterruption(spl, GLOBAL, summary, null);
        }
        ResipGraphicApp.getTheApp().addThreadRunning = false;
    }
}
