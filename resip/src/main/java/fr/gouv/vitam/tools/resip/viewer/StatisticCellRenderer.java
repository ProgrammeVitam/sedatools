package fr.gouv.vitam.tools.resip.viewer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * The type Statistic cell renderer.
 */
public class StatisticCellRenderer extends DefaultTableCellRenderer {

    /**
     * Readable file size.
     *
     * @param size the size
     * @return the string
     */
    private static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table,"",isSelected,hasFocus,row,column);

        String cellContent;
        if (value != null) {
            if (value instanceof String)
                cellContent = (String) value;
            else if (value instanceof Long) {
                if ((Long) value == Long.MAX_VALUE)
                    cellContent = "-";
                else if (column==5)
                    cellContent=readableFileSize((Long)value);
                else
                    cellContent = String.format("%,d", (Long) value);
            } else cellContent = value.toString();

            if (column <= 1) {
                setHorizontalAlignment(SwingConstants.CENTER);
                setText(cellContent);
            }
            else {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(cellContent+" ");
            }

            }
        return this;
    }
}
