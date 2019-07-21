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
