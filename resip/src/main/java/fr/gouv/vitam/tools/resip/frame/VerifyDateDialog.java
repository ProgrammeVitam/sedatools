package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.threads.CheckEndDateThread;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * The class SearchDialog.
 * <p>
 * Class for search dialog.
 */
public class VerifyDateDialog extends JDialog {

    /**
     * The actions components.
     */
    private final JLabel resultArchiveUnitLabel;
    private final JTextArea extProgressTextArea;

    /**
     * The data.
     */
    private List<ArchiveUnit> searchArchiveUnitResult;
    private int searchResultPosition;

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public VerifyDateDialog(MainWindow owner) {
        super(owner, "Vérifier EndDate > StartDate", false);
        GridBagConstraints gbc;
        GridBagLayout gbl;


        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        setMinimumSize(new Dimension(300, 200));
        setPreferredSize(new Dimension(600, 300));

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[]{1.0,1.0};
        gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0};
        contentPane.setLayout(gridBagLayout);

        JLabel lblNewLabel = new JLabel("Informations de progression");
        lblNewLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth=2;
        contentPane.add(lblNewLabel, gbc);


        JScrollPane scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth=5;
        contentPane.add(scrollPane, gbc);
        extProgressTextArea = new JTextArea();
        extProgressTextArea.setFont(MainWindow.LABEL_FONT);
        extProgressTextArea.setWrapStyleWord(true);
        extProgressTextArea.setEditable(false);
        extProgressTextArea.setLineWrap(true);
        scrollPane.setViewportView(extProgressTextArea);



        JButton searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(getClass().getResource("/icon/search-system.png")));
        searchButton.setText("");
        searchButton.setMaximumSize(new Dimension(26, 26));
        searchButton.setMinimumSize(new Dimension(26, 26));
        searchButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(searchButton, gbc);
        searchButton.addActionListener(arg0 -> buttonSearch());

        JButton nextButton = new JButton();
        nextButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-down.png")));
        nextButton.setText("");
        nextButton.setMaximumSize(new Dimension(26, 26));
        nextButton.setMinimumSize(new Dimension(26, 26));
        nextButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 0);
        contentPane.add(nextButton, gbc);
        nextButton.addActionListener(arg0 -> buttonNext());

        JButton previousButton = new JButton();
        previousButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-up.png")));
        previousButton.setText("");
        previousButton.setMaximumSize(new Dimension(26, 26));
        previousButton.setMinimumSize(new Dimension(26, 26));
        previousButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0, 5, 5);
        contentPane.add(previousButton, gbc);
        previousButton.addActionListener(arg0 -> buttonPrevious());

        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{0.0, 1.0};
        gbl.columnWidths = new int[]{30, 0};
        JPanel choicePanel = new JPanel(gbl);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridheight = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        contentPane.add(choicePanel, gbc);



        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridBagLayout());
        resultPanel.setMinimumSize(new Dimension(150, 36));
        resultPanel.setPreferredSize(new Dimension(150, 36));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(resultPanel, gbc);
        resultArchiveUnitLabel = new JLabel();
        resultArchiveUnitLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        resultPanel.add(resultArchiveUnitLabel, gbc);

        prepareProgessLogger();



        pack();
        setLocationRelativeTo(owner);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    private void prepareProgessLogger() {
        int localLogLevel, localLogStep;
        if (ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag()) {
            localLogLevel = SEDALibProgressLogger.OBJECTS_WARNINGS;
            localLogStep = 1;
        } else {
            localLogLevel = SEDALibProgressLogger.OBJECTS_GROUP;
            localLogStep = 1000;
        }
        SEDALibProgressLogger spl =
            new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), localLogLevel, (count, log) -> {
                String newLog = extProgressTextArea.getText() + "\n" + log;
                extProgressTextArea.setText(newLog);
                extProgressTextArea.setCaretPosition(newLog.length());
            }, localLogStep, 2);
        spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());


        spl = new SEDALibProgressLogger(ResipLogger.getGlobalLogger().getLogger(), localLogLevel, (count, log) -> {
            String newLog = extProgressTextArea.getText() + "\n" + log;
            extProgressTextArea.setText(newLog);
            extProgressTextArea.setCaretPosition(newLog.length());
        }, localLogStep, 2);
        spl.setDebugFlag(ResipGraphicApp.getTheApp().interfaceParameters.isDebugFlag());
    }

    // actions

    private void close() {
        emptyDialog();
        setVisible(false);
    }

    private void buttonSearch() {
        SwingWorker<?, ?> thread = new CheckEndDateThread(this);
        resultArchiveUnitLabel.setText("En cours");
        searchArchiveUnitResult = null;
        thread.execute();
    }

    private void buttonNext() {
        if (searchArchiveUnitResult != null && searchArchiveUnitResult.size() > 0) {
            searchResultPosition = Math.min(searchResultPosition + 1, searchArchiveUnitResult.size() - 1);
            resultArchiveUnitLabel.setText((searchResultPosition + 1) + "/" + searchArchiveUnitResult.size() + " trouvé" + (searchArchiveUnitResult.size() > 1 ? "s" : ""));
            ResipGraphicApp.getTheWindow().treePane.focusArchiveUnit(searchArchiveUnitResult.get(searchResultPosition));
        }
    }

    private void buttonPrevious() {
        if (searchArchiveUnitResult != null && searchArchiveUnitResult.size() > 0) {
            searchResultPosition = Math.max(searchResultPosition - 1, 0);
            resultArchiveUnitLabel.setText((searchResultPosition + 1) + "/" + searchArchiveUnitResult.size() + " trouvé" + (searchArchiveUnitResult.size() > 1 ? "s" : ""));
            ResipGraphicApp.getTheWindow().treePane.focusArchiveUnit(searchArchiveUnitResult.get(searchResultPosition));
        }
    }

    /**
     * Set ArchiveUnit list search result.
     *
     * @param searchArchiveUnitResult the search result
     */
    public void setSearchArchiveUnitResult(List<ArchiveUnit> searchArchiveUnitResult) {
        this.searchArchiveUnitResult = searchArchiveUnitResult;
        searchResultPosition = 0;
        if (searchArchiveUnitResult.size() > 0) {
            resultArchiveUnitLabel.setText("1/" + searchArchiveUnitResult.size() + " trouvé" + (searchArchiveUnitResult.size() > 1 ? "s" : ""));
            ResipGraphicApp.getTheWindow().treePane.focusArchiveUnit(searchArchiveUnitResult.get(0));
        } else
            resultArchiveUnitLabel.setText("0 trouvé");
    }

    /**
     * Empty dialog. To be used when the context is changed.
     */
    public void emptyDialog() {
        resultArchiveUnitLabel.setText("Aucune recherche");
    }

    public JTextArea getExtProgressTextArea() {
        return extProgressTextArea;
    }
}
