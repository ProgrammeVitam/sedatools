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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers;

import fr.gouv.vitam.tools.resip.data.StatisticData;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * The type Statistic table model.
 */
public class StatisticTableModel extends AbstractTableModel {

    private final String[] entetes = { "Catégorie", "Nombre", "Taille min", "Taille moy", "Taille max", "Total" };
    private List<StatisticData> statisticDataList;

    /**
     * Gets statistic data list.
     *
     * @return the statistic data list
     */
    public List<StatisticData> getStatisticDataList() {
        return statisticDataList;
    }

    /**
     * Sets statistic data list.
     *
     * @param statisticDataList the statistic data list
     */
    public void setStatisticDataList(List<StatisticData> statisticDataList) {
        this.statisticDataList = statisticDataList;
    }

    @Override
    public int getColumnCount() {
        return entetes.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return entetes[columnIndex];
    }

    @Override
    public Class getColumnClass(int col) {
        if (col > 0) return Long.class; //second column accepts only Integer values
        else return String.class; //other columns accept String values
    }

    @Override
    public int getRowCount() {
        if (statisticDataList == null) return 0;
        return statisticDataList.size();
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        if (statisticDataList == null) return null;
        if (arg0 >= statisticDataList.size()) throw new IllegalArgumentException();
        StatisticData statisticData = statisticDataList.get(arg0);
        switch (arg1) {
            case 0:
                return statisticData.getFormatCategory();
            case 1:
                return statisticData.getObjectNumber();
            case 2:
                if (statisticData.getObjectNumber() == 0) return Long.MAX_VALUE;
                return statisticData.getMinSize();
            case 3:
                if (statisticData.getObjectNumber() == 0) return Long.MAX_VALUE;
                return Math.round(statisticData.getMeanSize());
            case 4:
                if (statisticData.getObjectNumber() == 0) return Long.MAX_VALUE;
                return statisticData.getMaxSize();
            case 5:
                if (statisticData.getObjectNumber() == 0) return Long.MAX_VALUE;
                return statisticData.getTotalSize();
            default:
                throw new IllegalArgumentException();
        }
    }
}
