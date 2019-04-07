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

import fr.gouv.vitam.tools.resip.data.StatisticData;
import fr.gouv.vitam.tools.resip.frame.StatisticWindow;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger.GLOBAL;

public class StatisticThread extends SwingWorker<String, String> {

    private StatisticWindow statisticWindow;
    private DataObjectPackage dataObjectPackage;
    private List<StatisticData> statisticDataList;
    // logger
    private SEDALibProgressLogger spl;

    public StatisticThread(StatisticWindow statisticWindow) {
        this.statisticWindow = statisticWindow;
    }

    private String findCategory(String formatId, LinkedHashMap<String, List<String>> formatByCatgeoryMap) {
        for (Map.Entry<String, List<String>> category : formatByCatgeoryMap.entrySet()) {
            if (category.getValue().contains(formatId))
                return category.getKey();
        }
        return null;
    }

    @Override
    public String doInBackground() {
        try {
            spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), SEDALibProgressLogger.OBJECTS_GROUP, null, 1000);
            dataObjectPackage = ResipGraphicApp.getTheApp().currentWork.getDataObjectPackage();
            LinkedHashMap<String, List<Long>> sizeByCategoryMap = new LinkedHashMap<String, List<Long>>();
            LinkedHashMap<String, List<String>> formatByCatgeoryMap = ResipGraphicApp.getTheApp().treatmentParameters.getFormatByCategoryMap();
            String otherCategory = null;
            for (Map.Entry<String, List<String>> category : formatByCatgeoryMap.entrySet()) {
                sizeByCategoryMap.put(category.getKey(), new ArrayList<Long>());
                if (category.getValue().contains("Other"))
                    otherCategory = category.getKey();
            }
            sizeByCategoryMap.put("Tous formats", new ArrayList<Long>());
            int counter=0;
            for (BinaryDataObject bdo : dataObjectPackage.getBdoInDataObjectPackageIdMap().values()) {
                String category = findCategory(bdo.formatIdentification.formatId, formatByCatgeoryMap);
                if (category == null) category = otherCategory;
                if (category != null)
                    sizeByCategoryMap.get(category).add(bdo.size);
                sizeByCategoryMap.get("Tous formats").add(bdo.size);
                counter++;
                spl.progressLogIfStep(SEDALibProgressLogger.GLOBAL,counter,
                        Integer.toString(counter)+" objets pris en compte dans les statistiques");
            }
            statisticDataList = sizeByCategoryMap.entrySet().stream()
                    .map(e -> new StatisticData(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            try {
                if (spl != null)
                    spl.progressLog(GLOBAL, "Statistiques impossibles\n-> " + e.getMessage());
            } catch (InterruptedException ignored) {
            }
        }
        return "OK";
    }

    @Override
    protected void done() {
        ResipGraphicApp theApp = ResipGraphicApp.getTheApp();

        if ((!isCancelled()) && (statisticDataList != null)) {
            statisticWindow.setStatisticDataList(statisticDataList);
            try {
                spl.progressLog(SEDALibProgressLogger.GLOBAL, String.format(
                        "%-40.40s %10s %10s %10s %10s", "Categorie", "Nb", "Min", "Moyenne", "Max"));
                for (StatisticData sd : statisticDataList) {
                    if (sd.getObjectNumber()!=0)
                        spl.progressLog(SEDALibProgressLogger.GLOBAL, String.format(
                            "%-40.40s %10d %10d %10.0f %10d",
                            sd.getFormatCategory(),
                            sd.getObjectNumber(),
                            sd.getMinSize(),
                            sd.getMeanSize(),
                            sd.getMaxSize()));
                    else
                        spl.progressLog(SEDALibProgressLogger.GLOBAL, String.format(
                                "%-40.40s %10d %10s %10s %10s",
                                sd.getFormatCategory(),
                                0,"-","-","-"));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
