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
package fr.gouv.vitam.tools.resip.data;

import java.util.List;

/**
 * The type Statistic data.
 */
public class StatisticData {
   private String formatCategory;
    private int objectNumber;
    private long minSize;
    private long maxSize;
    private double meanSize;
    private long totalSize;

    /**
     * Instantiates a new Statistic data.
     *
     * @param formatCategory the format category
     * @param sizeList       the size list
     */
    public StatisticData(String formatCategory, List<Long> sizeList){
        this.formatCategory=formatCategory;
        this.objectNumber=sizeList.size();
        long min=Long.MAX_VALUE;
        long max=-1;
        long accu=0;
        for (long size:sizeList){
            if (size<min) min=size;
            if (size>max) max=size;
            accu+=size;
        }
        this.minSize=min;
        this.maxSize=max;
        this.meanSize=(double)accu/(double)this.objectNumber;
        this.totalSize=accu;
    }

    /**
     * Gets format category.
     *
     * @return the format category
     */
    public String getFormatCategory() {
        return formatCategory;
    }

    /**
     * Gets object number.
     *
     * @return the object number
     */
    public int getObjectNumber() {
        return objectNumber;
    }

    /**
     * Gets min size.
     *
     * @return the min size
     */
    public long getMinSize() {
        return minSize;
    }

    /**
     * Gets max size.
     *
     * @return the max size
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * Gets mean size.
     *
     * @return the mean size
     */
    public double getMeanSize() {
        return meanSize;
    }

    /**
     * Gets total size.
     *
     * @return the total size
     */
    public long getTotalSize() {
        return totalSize;
    }
}
