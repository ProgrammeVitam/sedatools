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
import fr.gouv.vitam.tools.resip.service.AnalysisService;
import fr.gouv.vitam.tools.sedalib.core.*;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * The type Technical search thread.
 */
public class TechnicalSearchThread extends SwingWorker<String, String> {

    // input
    private final ArchiveUnit searchUnit;
    private final List<String> formats;
    private final long min;
    private final long max;
    private final LinkedHashMap<String, List<String>> formatByCategoryMap;
    private final Consumer<LinkedHashMap<ArchiveUnit, List<BinaryDataObject>>> callBack;
    // run output
    private LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchDataObjectResult;

    /**
     * Instantiates a new Technical search thread.
     *
     * @param au       the ArchiveUnit under which to search
     * @param formats  the searched format identifiers, "Other" standing for the ones of no category
     * @param min      the minimum size, 0 for none
     * @param max      the maximum size, Long.MAX_VALUE for none
     * @param callBack the consumer of the found BinaryDataObjects by ArchiveUnit
     */
    public TechnicalSearchThread(
        ArchiveUnit au,
        List<String> formats,
        long min,
        long max,
        Consumer<LinkedHashMap<ArchiveUnit, List<BinaryDataObject>>> callBack
    ) {
        this.searchUnit = au;
        this.formats = formats;
        this.min = min;
        this.max = max;
        this.formatByCategoryMap = ResipGraphicApp.getTheApp().treatmentParameters.getFormatByCategoryMap();
        this.callBack = callBack;
    }

    @Override
    public String doInBackground() {
        searchDataObjectResult = AnalysisService.technicalSearch(searchUnit, formats, min, max, formatByCategoryMap);
        return "OK";
    }

    @Override
    protected void done() {
        callBack.accept(searchDataObjectResult);
    }
}
