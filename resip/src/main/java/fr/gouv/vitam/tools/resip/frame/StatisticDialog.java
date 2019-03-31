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
package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.app.StatisticThread;
import fr.gouv.vitam.tools.resip.data.StatisticData;
import fr.gouv.vitam.tools.resip.viewer.DefaultTableHeaderCellRenderer;
import fr.gouv.vitam.tools.resip.viewer.StatisticCellRenderer;
import fr.gouv.vitam.tools.resip.viewer.StatisticTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static fr.gouv.vitam.tools.resip.frame.MainWindow.BOLD_LABEL_FONT;

/**
 * The class StatisticDialog.
 * <p>
 * Class for formats category statistics display.
 */
public class StatisticDialog extends JDialog {

    /**
     * The actions components.
     */
    public JTable statisticTable;
    public JButton okButton;
    private JPanel warningPanel;

    /**
     * The data.
     */
    public SwingWorker<?, ?> thread;

    // Dialog test context

    /**
     * The entry point of dialog test.
     *
     * @param args the input arguments
     * @throws ClassNotFoundException          the class not found exception
     * @throws UnsupportedLookAndFeelException the unsupported look and feel exception
     * @throws InstantiationException          the instantiation exception
     * @throws IllegalAccessException          the illegal access exception
     * @throws NoSuchMethodException           the no such method exception
     * @throws InvocationTargetException       the invocation target exception
     */
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        TestDialogWindow window = new TestDialogWindow(StatisticDialog.class);
        ArrayList<StatisticData> sdl = new ArrayList<StatisticData>();
        sdl.add(new StatisticData("Test", Arrays.asList(new Long(1), new Long(2),
                new Long(3), new Long(4), new Long(5))));
        sdl.add(new StatisticData("No", Arrays.asList()));
        ((StatisticTableModel) (((StatisticDialog) (window.it)).statisticTable.getModel()))
                .setStatisticDataList(sdl);
        ((DefaultRowSorter) ((StatisticDialog) (window.it)).statisticTable.getRowSorter()).toggleSortOrder(1);
        ((DefaultRowSorter) ((StatisticDialog) (window.it)).statisticTable.getRowSorter()).toggleSortOrder(1);
        ((StatisticDialog) (window.it)).statisticTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        ((StatisticTableModel) (((StatisticDialog) (window.it)).statisticTable.getModel()))
                .fireTableDataChanged();
        ((StatisticDialog) (window.it)).statisticTable.getColumnModel().getColumn(0)
                .setPreferredWidth(200);
    }

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public StatisticDialog(JFrame owner) {
        super(owner, "Statistiques", false);
        GridBagConstraints gbc;

        setMinimumSize(new Dimension(600, 200));
        setPreferredSize(new Dimension(600, 400));

        Container contentPane = getContentPane();
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[]{1.0, 1.0,1.0};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0};
        contentPane.setLayout(gridBagLayout);

        JLabel lblNewLabel = new JLabel("Par catégories de format");
        lblNewLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        contentPane.add(lblNewLabel, gbc);

        JScrollPane scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        contentPane.add(scrollPane, gbc);
        statisticTable = new JTable(new StatisticTableModel());
        statisticTable.setFont(MainWindow.LABEL_FONT);
        statisticTable.setAutoCreateRowSorter(true);
        statisticTable.setDefaultRenderer(Object.class, new StatisticCellRenderer());
        statisticTable.setDefaultRenderer(Long.class, new StatisticCellRenderer());
        statisticTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderCellRenderer());
        statisticTable.getColumnModel().getColumn(0)
                .setPreferredWidth(250);
        scrollPane.setViewportView(statisticTable);

        JButton copyButton = new JButton("Copier");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        contentPane.add(copyButton, gbc);
        copyButton.addActionListener(arg -> copyToClipboard());

        JButton refreshButton = new JButton("Recalculer");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        contentPane.add(refreshButton, gbc);
        refreshButton.addActionListener(arg -> refresh());

        JButton okButton = new JButton("Fermer");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 5, 5);
        contentPane.add(okButton, gbc);
        okButton.addActionListener(arg -> buttonOk());

        warningPanel = new JPanel();
        warningPanel.setLayout(new GridBagLayout());
        warningPanel.setVisible(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(warningPanel, gbc);
        JSeparator separator2 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        warningPanel.add(separator2, gbc);
        JLabel informationLabel = new JLabel();
        informationLabel.setText("");
        informationLabel.setIcon(new ImageIcon(getClass().getResource("/icon/dialog-warning.png")));
        informationLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        warningPanel.add(informationLabel, gbc);
        JTextArea warningTextArea = new JTextArea("Attention, votre contexte de travail contient des fichiers vides ce qui n'est " +
                "pas accepté dans le standard SEDA. Veillez à les supprimer pour construire un SIP valide.");
        warningTextArea.setFont(MainWindow.LABEL_FONT);
        warningTextArea.setEditable(false);
        warningTextArea.setLineWrap(true);
        warningTextArea.setWrapStyleWord(true);
        warningTextArea.setBackground(MainWindow.GENERAL_BACKGROUND);
        warningTextArea.setForeground(Color.RED);
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        warningPanel.add(warningTextArea, gbc);
        JButton emptySearchButton = new JButton("Chercher");
        gbc = new GridBagConstraints();
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        gbc.gridx = 2;
        gbc.gridy = 1;
        emptySearchButton.addActionListener(arg -> emptySearch());
        warningPanel.add(emptySearchButton, gbc);

        pack();
        setLocationRelativeTo(owner);
    }

    // actions

    private void buttonOk() {
        setVisible(false);
    }

    public void refresh() {
        StatisticThread statisticsThread = new StatisticThread(this);
        statisticsThread.execute();
    }

    public void copyToClipboard() {
        StringBuffer sbf = new StringBuffer();
        int numRows = statisticTable.getRowCount();
        int numColumns = statisticTable.getColumnCount();

        for (int j = 0; j < numColumns; j++) {
            sbf.append(statisticTable.getColumnName(j));
            if (j < numColumns - 1) sbf.append("\t");
        }
        sbf.append("\n");

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                Object value = statisticTable.getValueAt(i, j);
                if (!(value instanceof Long) || ((long)value != Long.MAX_VALUE))
                    sbf.append(value);
                if (j < numColumns - 1) sbf.append("\t");
            }
            sbf.append("\n");
        }
        StringSelection stsel = new StringSelection(sbf.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stsel, stsel);
    }

    public void emptySearch() {
        if (ResipGraphicApp.getTheApp().technicalSearchDialog == null)
            ResipGraphicApp.getTheApp().technicalSearchDialog =
                    new TechnicalSearchDialog(ResipGraphicApp.getTheApp().mainWindow);
        TechnicalSearchDialog technicalSearchDialog = ResipGraphicApp.getTheApp().technicalSearchDialog;
        technicalSearchDialog.emptyDialog();
        technicalSearchDialog.setMinMax(0, 0);
        technicalSearchDialog.search();
        technicalSearchDialog.setVisible(true);
    }

    private void showWarning() {
        if (!warningPanel.isVisible()) {
            Dimension dim = this.getSize();
            warningPanel.setVisible(true);
            pack();
            dim.height = dim.height + warningPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        }
    }

    private void hideWarning() {
        if (warningPanel.isVisible()) {
            warningPanel.setVisible(false);
            Dimension dim = this.getSize();
            dim.height = dim.height - warningPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        }
    }

    // data

    public void setStatisticDataList(List<StatisticData> statisticDataList) {
        StatisticTableModel stm = ((StatisticTableModel) (statisticTable.getModel()));
        stm.setStatisticDataList(statisticDataList);
        statisticTable.getRowSorter().toggleSortOrder(1);
        statisticTable.getRowSorter().toggleSortOrder(1);
        stm.fireTableDataChanged();

        hideWarning();
        for (StatisticData sd : statisticDataList) {
            if (sd.getMinSize() == 0) {
                showWarning();
                break;
            }
        }
    }
}
