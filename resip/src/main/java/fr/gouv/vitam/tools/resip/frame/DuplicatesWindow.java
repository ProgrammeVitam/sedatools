package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.DuplicatesThread;
import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.viewer.*;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The class DuplicatesWindow.
 * <p>
 * Class for duplicates search and management.
 */
public class DuplicatesWindow extends JFrame {

    /**
     * The actions components.
     */
    private JCheckBox binaryHashCheckBox;
    private JCheckBox binaryFilenameCheckBox;
    private JCheckBox physicalAllMDCheckBox;
    private JTable duplicatesTable;
    private JLabel lineResultLabel;
    private JLabel globalResultLabel;

    private MainWindow mainWindow;
    private JPanel explanationPanel;

    /**
     * The data.
     */
    private DataObjectPackageTreeViewer dataObjectPackageTreeViewer;
    private DataObjectPackageTreeModel dataObjectPackageTreeModel;
    private DataObjectListViewer dataObjectListViewer;
    private LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchResult;
    private List<DataObjectGroup> dogList;
    private int dogListPosition;
    private int searchArchiveUnitPosition;
    private ArchiveUnit searchCurrentArchiveUnit;
    private int searchObjectPosition;
    private int searchResultPosition;
    private int searchResultCount;
    private int searchResultListCount;
    private boolean searchRunning;

    // Window test context

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
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ResipException, InterruptedException {
        ResipGraphicApp rga = new ResipGraphicApp(null);
        Thread.sleep(1000);
        DuplicatesWindow window = new DuplicatesWindow();
        window.setVisible(true);
    }

    /**
     * Create the window.
     */
    public DuplicatesWindow() {
        GridBagConstraints gbc;

        java.net.URL imageURL = getClass().getClassLoader().getResource("VitamIcon96.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            setIconImage(icon.getImage());
        }
        this.setTitle("Chercher des doublons");

        searchRunning = false;

        mainWindow = ResipGraphicApp.getTheApp().mainWindow;
        dataObjectPackageTreeViewer = mainWindow.getDataObjectPackageTreePaneViewer();
        dataObjectPackageTreeModel = (DataObjectPackageTreeModel) (dataObjectPackageTreeViewer.getModel());
        dataObjectListViewer = mainWindow.getDataObjectListViewer();

        setMinimumSize(new Dimension(600, 150));
        setPreferredSize(new Dimension(800, 500));

        Container contentPane = getContentPane();
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0};
        contentPane.setLayout(gridBagLayout);

        final JPanel criteriaPanel = new JPanel();
        criteriaPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(criteriaPanel, gbc);

        JLabel duplicatesLabel = new JLabel("Critères de sélection des doublons de groupes d'objets");
        duplicatesLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        criteriaPanel.add(duplicatesLabel, gbc);

        JLabel binaryLabel = new JLabel("  - pour les objets binaires :");
        binaryLabel.setFont(MainWindow.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        criteriaPanel.add(binaryLabel, gbc);

        binaryHashCheckBox = new JCheckBox();
        binaryHashCheckBox.setText("Hachage");
        binaryHashCheckBox.setSelected(true);
        binaryHashCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        criteriaPanel.add(binaryHashCheckBox, gbc);

        binaryFilenameCheckBox = new JCheckBox();
        binaryFilenameCheckBox.setText("Nom");
        binaryFilenameCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        criteriaPanel.add(binaryFilenameCheckBox, gbc);

        JLabel physicalLabel = new JLabel("  - pour les objets physiques :");
        physicalLabel.setFont(MainWindow.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        criteriaPanel.add(physicalLabel, gbc);

        physicalAllMDCheckBox = new JCheckBox();
        physicalAllMDCheckBox.setText("Métadonnées");
        physicalAllMDCheckBox.setSelected(true);
        physicalAllMDCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        criteriaPanel.add(physicalAllMDCheckBox, gbc);

        final JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(actionPanel, gbc);
        final JSeparator separator = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        actionPanel.add(separator, gbc);

        JCheckBox moreOptionsCheckBox = new JCheckBox();
        moreOptionsCheckBox.setEnabled(true);
        moreOptionsCheckBox.setVisible(false);
        moreOptionsCheckBox.setIcon(new ImageIcon(getClass().getResource("/icon/list-add.png")));
        moreOptionsCheckBox.setSelectedIcon(new ImageIcon(getClass().getResource("/icon/list-remove.png")));
        moreOptionsCheckBox.setText("En savoir plus");
        moreOptionsCheckBox.addItemListener(arg -> moreExplanationEvent(arg));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        actionPanel.add(moreOptionsCheckBox, gbc);

        JLabel placeHolder = new JLabel(" ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        actionPanel.add(placeHolder, gbc);

        JButton searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(getClass().getResource("/icon/search-system.png")));
        searchButton.setText("");
        searchButton.setMaximumSize(new Dimension(26, 26));
        searchButton.setMinimumSize(new Dimension(26, 26));
        searchButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        actionPanel.add(searchButton, gbc);
        searchButton.addActionListener(arg0 -> buttonSearch());
        JButton nextButton = new JButton();
        nextButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-down.png")));
        nextButton.setText("");
        nextButton.setMaximumSize(new Dimension(26, 26));
        nextButton.setMinimumSize(new Dimension(26, 26));
        nextButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 0);
        actionPanel.add(nextButton, gbc);
        nextButton.addActionListener(arg -> buttonNext());
        JButton previousButton = new JButton();
        previousButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-up.png")));
        previousButton.setText("");
        previousButton.setMaximumSize(new Dimension(26, 26));
        previousButton.setMinimumSize(new Dimension(26, 26));
        previousButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 5);
        actionPanel.add(previousButton, gbc);
        previousButton.addActionListener(arg -> buttonPrevious());

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridBagLayout());
        resultPanel.setPreferredSize(new Dimension(80, 32));
        resultPanel.setMinimumSize(new Dimension(80, 32));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        actionPanel.add(resultPanel, gbc);
        globalResultLabel = new JLabel();
        globalResultLabel.setText("Aucune recherche effectuée");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(5, 0, 0, 5);
        resultPanel.add(globalResultLabel, gbc);
        lineResultLabel = new JLabel();
        lineResultLabel.setText("                       ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(0, 0, 5, 5);
        resultPanel.add(lineResultLabel, gbc);

        JPanel detailedResultPanel = new JPanel();
        detailedResultPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(detailedResultPanel, gbc);
        JScrollPane scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        detailedResultPanel.add(scrollPane, gbc);
        duplicatesTable = new JTable(new DuplicatesTableModel());
        duplicatesTable.setFont(MainWindow.LABEL_FONT);
        duplicatesTable.setAutoCreateRowSorter(true);
        duplicatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        duplicatesTable.getTableHeader().setDefaultRenderer(new DefaultTableHeaderCellRenderer());
        duplicatesTable.getColumnModel().getColumn(0)
                .setPreferredWidth(20);
        duplicatesTable.getColumnModel().getColumn(1)
                .setPreferredWidth(20);
        ListSelectionModel selectionModel = duplicatesTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                handleDuplicatesSelectionEvent(e);
            }
        });
        scrollPane.setViewportView(duplicatesTable);

        explanationPanel = new JPanel();
        explanationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(explanationPanel, gbc);
        JSeparator separator2 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        explanationPanel.add(separator2, gbc);
        final JLabel label7 = new JLabel();
        label7.setIcon(new ImageIcon(getClass().getResource("/icon/dialog-information.png")));
        label7.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        explanationPanel.add(label7, gbc);
        JTextArea explanationTextArea = new JTextArea();
        explanationTextArea.setLineWrap(true);
        explanationTextArea.setFont(new JLabel().getFont());
        explanationTextArea.setBackground(UIManager.getColor("Dialog.background"));
        explanationTextArea.setFocusable(false);
        explanationTextArea.setText("La recherche se fait sur :\n" +
                "- l'ensemble des formats fournis, selon les choix cochés, par:\n" +
                "  - une catégorie de fichiers, déterminant une liste de PUID Pronom,\n" +
                "  - une liste libre de PUID Pronom séparés par des virgules (par exemple: x-fmt/111, fmt/101)\n" +
                "  En cliquant sur le bouton de mise à jour, on peut voir la liste de l'ensemble des formats pris en compte.\n" +
                "- la taille du fichier.");
        explanationTextArea.setWrapStyleWord(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 5);
        explanationPanel.add(explanationTextArea, gbc);

        pack();
        explanationPanel.setVisible(false);
        pack();
        setLocationRelativeTo(mainWindow);
    }

    // actions

    private void moreExplanationEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            Dimension dim = this.getSize();
            explanationPanel.setVisible(true);
            pack();
            dim.height = dim.height + explanationPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        } else if (event.getStateChange() == DESELECTED) {
            Dimension dim = this.getSize();
            dim.height = dim.height - explanationPanel.getHeight();
            explanationPanel.setVisible(false);
            this.setSize(dim);
            this.setPreferredSize(dim);
            pack();
        }
    }

    void handleDuplicatesSelectionEvent(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;

        final DefaultListSelectionModel target = (DefaultListSelectionModel) e.getSource();
        int selectedIndex = target.getMinSelectionIndex();
        if (selectedIndex < 0) {
            lineResultLabel.setText("");
            dogList = null;
            return;
        }
        lineResultLabel.setText("1/" + duplicatesTable.getModel().getValueAt(duplicatesTable
                .convertRowIndexToModel(selectedIndex), 1) + " doublons sur cette ligne");
        dogList = ((DuplicatesTableModel) duplicatesTable.getModel()).getRowDogList(duplicatesTable
                .convertRowIndexToModel(selectedIndex));
        dogListPosition = 0;
        focusObject(dogList.get(dogListPosition));
    }

    private void focusObject(DataObjectGroup dog) {
        TreePath path = new TreePath(dataObjectPackageTreeModel.getPathToRoot(dataObjectPackageTreeModel
                .findTreeNode(dog)));
        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        mainWindow.dataObjectPackageTreeItemClick(path);
        if (dog.getBinaryDataObjectList().size() >= 1) {
            mainWindow.dataObjectListItemClick(dog.getBinaryDataObjectList().get(0));
            dataObjectListViewer.selectDataObject(dog.getBinaryDataObjectList().get(0));
        }
    }

    private void buttonSearch() {
        //showFormatList();
        if (searchRunning) {
        } else {
            globalResultLabel.setText("En cours");
            DuplicatesThread dt = new DuplicatesThread(this, binaryHashCheckBox.isSelected(),
                    binaryFilenameCheckBox.isSelected(),
                    physicalAllMDCheckBox.isSelected());
            dt.execute();
        }
    }

    private void buttonNext() {
        if ((dogList != null) && (dogListPosition < dogList.size() - 1)) {
            dogListPosition++;
            lineResultLabel.setText("" + (dogListPosition + 1) + "/" + dogList.size() + " doublons sur cette ligne");
            focusObject(dogList.get(dogListPosition));
        }
    }

    private void buttonPrevious() {
        if ((dogList != null) && (dogListPosition > 0)) {
            dogListPosition--;
            lineResultLabel.setText("" + (dogListPosition + 1) + "/" + dogList.size() + " doublons sur cette ligne");
            focusObject(dogList.get(dogListPosition));
        }
    }


    public void setDuplicatesResult(LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap) {
        ((DuplicatesTableModel) duplicatesTable.getModel()).setDogByDigestMap(dogByDigestMap);

        DuplicatesTableModel dtm = ((DuplicatesTableModel) (duplicatesTable.getModel()));
        dtm.setDogByDigestMap(dogByDigestMap);
        dtm.fireTableDataChanged();
        duplicatesTable.getRowSorter().toggleSortOrder(1);
        duplicatesTable.getRowSorter().toggleSortOrder(1);
        duplicatesTable.getColumnModel().getColumn(0)
                .setPreferredWidth(20);
        duplicatesTable.getColumnModel().getColumn(1)
                .setPreferredWidth(20);
        globalResultLabel.setText("" + dogByDigestMap.size() + " lots de doublons trouvés");
    }

    public void emptyDialog() {
        DuplicatesTableModel dtm = ((DuplicatesTableModel) (duplicatesTable.getModel()));
        dtm.setDogByDigestMap(null);
        dtm.fireTableDataChanged();
    }
}
