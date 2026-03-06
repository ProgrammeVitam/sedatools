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
import fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.parameters.CompactContext;
import fr.gouv.vitam.tools.resip.parameters.CreationContext;
import fr.gouv.vitam.tools.resip.parameters.Preferences;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.process.DeCompactor;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog.*;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.*;

/**
 * The type Compact thread.
 */
public class DeCompactThread extends SwingWorker<String, String> {

    //input
    private final Work work;
    private final DataObjectPackageTreeNode targetNode;
    private ArchiveUnit decompactedArchiveUnit;
    private final InOutDialog inOutDialog;
    private String summary;
    private Throwable exitThrowable;
    private int fileCounter;
    // logger
    private SEDALibProgressLogger spl;

    /**
     * Compact an archive unit hierarchy, in a dedicated thread.
     *
     * @param node the displayed tree node
     */
    public static void launchDeCompactThread(DataObjectPackageTreeNode node) {
        DeCompactThread compactThread;

        try {
            InOutDialog inOutDialog = new InOutDialog(ResipGraphicApp.mainWindow, "Décompactage");
            compactThread = new DeCompactThread(ResipGraphicApp.getTheApp().currentWork, node, inOutDialog);
            compactThread.execute();
            inOutDialog.setVisible(true);
        } catch (Throwable e) {
            UserInteractionDialog.getUserAnswer(
                ResipGraphicApp.mainWindow,
                "Erreur fatale, impossible de faire le décompactage \n->" + e.getMessage(),
                "Erreur",
                UserInteractionDialog.ERROR_DIALOG,
                null
            );
            ResipLogger.getGlobalLogger()
                .log(ResipLogger.ERROR, "Erreur fatale, impossible de faire le décompactage", e);
        }
    }

    /**
     * Instantiates a new Compact thread.
     *
     * @param work       the work
     * @param targetNode the target node
     * @param dialog     the dialog
     */
    public DeCompactThread(Work work, DataObjectPackageTreeNode targetNode, InOutDialog dialog) {
        this.work = work;
        this.targetNode = targetNode;
        this.inOutDialog = dialog;
        this.summary = null;
        this.exitThrowable = null;
        this.spl = new ThreadLoggerFactory(
            inOutDialog.extProgressTextArea,
            ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()
        ).getLogger();
        dialog.setThread(this);
    }

    private void recursiveDelete(File inFile) throws InterruptedException {
        if (inFile.isDirectory()) {
            for (File f : inFile.listFiles()) recursiveDelete(f);
            inFile.delete();
        } else {
            inFile.delete();
            fileCounter++;
            doProgressLogIfStep(
                spl,
                SEDALibProgressLogger.OBJECTS_GROUP,
                fileCounter,
                fileCounter + " fichiers effacés"
            );
        }
    }

    private String getTmpDirTarget(String workDir, String srcPathName, String id)
        throws ResipException, InterruptedException {
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
            } else { // STATUS_CANCEL
                this.cancel(false);
                throw new ResipException("Opération annulée");
            }
        }
        return target;
    }

    private Map<String, Integer> getContentMetadataFilter(CompactContext coc) {
        Map<String, Integer> contentMetadataFilter = new HashMap<>();
        for (String m : coc.getKeptMetadataList()) {
            if (m.trim().isEmpty()) continue;
            else if (!m.contains(":")) contentMetadataFilter.put(m.trim(), 0);
            else {
                int tmp = 0;
                try {
                    tmp = Integer.parseInt(m.substring(m.indexOf(":") + 1));
                } catch (NumberFormatException ignored) {
                    // no real case
                }
                if (tmp < 0) tmp = 0;
                contentMetadataFilter.put(m.substring(0, m.indexOf(":")).trim(), tmp);
            }
        }
        return contentMetadataFilter;
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
        try {
            spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());

            ArchiveUnit targetArchiveUnit = targetNode.getArchiveUnit();
            doProgressLog(
                spl,
                GLOBAL,
                "Décompactage de l'ArchiveUnit [" +
                targetArchiveUnit.getInDataObjectPackageId() +
                "]=" +
                targetArchiveUnit.getContent().getSimpleMetadata("Title"),
                null
            );

            CreationContext cc = new CreationContext(Preferences.getInstance());

            String target = getTmpDirTarget(cc.getWorkDir(), "DeCompact", targetArchiveUnit.getInDataObjectPackageId());
            //run output
            DeCompactor decompactor = new DeCompactor(targetArchiveUnit, target, spl);
            decompactedArchiveUnit = decompactor.doDeCompact();

            summary = decompactor.getSummary();
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
            "Décompactage annulé, les données n'ont pas été modifiées",
            null
        );
        else if (exitThrowable != null) doProgressLogWithoutInterruption(
            spl,
            GLOBAL,
            "Erreur durant le décompactage, les données n'ont pas été modifiées",
            exitThrowable
        );
        else {
            ResipGraphicApp.getTheApp().currentWork = this.work;

            DataObjectPackageTreeNode newNode = null;
            List<DataObjectPackageTreeNode> parents = List.copyOf(targetNode.getParents());
            for (DataObjectPackageTreeNode targetNodeParent : parents) {
                targetNodeParent.removeChildrenNode(targetNode);
                newNode = targetNode.getTreeModel().generateArchiveUnitNode(decompactedArchiveUnit, targetNodeParent);
            }

            if (newNode != null) newNode.getTreeModel().nodeStructureChanged(newNode.getParent());
            work.getCreationContext().setStructureChanged(true);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
            ResipGraphicApp.mainWindow.treePane.reset();
            doProgressLogWithoutInterruption(spl, GLOBAL, summary, null);
        }
        ResipGraphicApp.getTheApp().addThreadRunning = false;
    }
}
