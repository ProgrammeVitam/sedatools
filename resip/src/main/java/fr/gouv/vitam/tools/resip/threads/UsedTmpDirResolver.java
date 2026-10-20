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
import fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog;
import fr.gouv.vitam.tools.resip.service.TmpDirResolver;
import fr.gouv.vitam.tools.resip.service.WorkDirService;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;

import static fr.gouv.vitam.tools.resip.frame.UsedTmpDirDialog.*;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.STEP;
import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.doProgressLog;

/**
 * Asks the user what to do with an already used temporary directory: clean it, use it as it is,
 * use another one, or cancel the operation.
 */
class UsedTmpDirResolver implements TmpDirResolver {

    private final SwingWorker<?, ?> worker;
    private final Supplier<SEDALibProgressLogger> splSupplier;

    /**
     * @param worker      the operation, cancelled if the user cancels
     * @param splSupplier the progress logger of the operation, known once it runs
     */
    UsedTmpDirResolver(SwingWorker<?, ?> worker, Supplier<SEDALibProgressLogger> splSupplier) {
        this.worker = worker;
        this.splSupplier = splSupplier;
    }

    @Override
    public String resolve(String workDir, String subDir) throws ResipException, InterruptedException {
        String target = workDir + File.separator + subDir;
        if (!Files.exists(Paths.get(target))) return target;
        UsedTmpDirDialog utdd = new UsedTmpDirDialog(ResipGraphicApp.getTheWindow(), workDir, subDir);
        utdd.setVisible(true);
        if (utdd.getReturnValue() == STATUS_CLEAN) {
            SEDALibProgressLogger spl = splSupplier.get();
            int deleted = WorkDirService.deleteRecursively(new File(utdd.getResult()), spl);
            doProgressLog(spl, STEP, deleted + " fichiers effacés au total", null);
            return utdd.getResult();
        }
        if (
            (utdd.getReturnValue() == STATUS_CONTINUE) || (utdd.getReturnValue() == STATUS_CHANGE)
        ) return utdd.getResult();
        // STATUS_CANCEL
        worker.cancel(false);
        throw new ResipException("Opération annulée");
    }
}
