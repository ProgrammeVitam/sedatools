package fr.gouv.vitam.tools.resip.viewer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

public class StatisticCellRenderer extends JLabel implements TableCellRenderer {

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
