package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.app.TechnicalSearchThread;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.viewer.DataObjectListViewer;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeViewer;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The class TehcnicalSearchDialog.
 * <p>
 * Class for search dialog on technical caracteristics.
 */
public class TechnicalSearchDialog extends JDialog {

    /**
     * The actions components.
     */
    private JCheckBox formatCategoryCheckBox;
    private JComboBox formatCategoryComboBox;
    private JCheckBox formatListCheckBox;
    private JTextField formatListTextField;
    private JTextArea formatsTextArea;
    private JLabel resultArchiveUnitLabel;
    private JLabel resultObjectLabel;
    private MainWindow mainWindow;
    private JPanel optionalInfoPanel;

    /**
     * The data.
     */
    private DataObjectPackageTreeViewer dataObjectPackageTreeViewer;
    private DataObjectPackageTreeModel dataObjectPackageTreeModel;
    private DataObjectListViewer dataObjectListViewer;
    private LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchResult;
    private List<ArchiveUnit> searchResultList;
    private int searchArchiveUnitPosition;
    private ArchiveUnit searchCurrentArchiveUnit;
    private int searchObjectPosition;
    private int searchResultPosition;
    private int searchResultCount;
    private int searchResultListCount;
    private boolean searchRunning;

    static private String[] formatCategories = {"Zip,arc...", "Video", "Eml", "Txt"};
    static private List<String>[] formatCategoriesList;

    static {
        formatCategoriesList = new List[4];
        formatCategoriesList[0] = Arrays.asList("x-fmt/263", "x-fmt/265", "x-fmt/264", "fmt/411", "fmt/613");
        formatCategoriesList[1] = Arrays.asList("fmt/5", "fmt/569");
        formatCategoriesList[2] = Arrays.asList("fmt/278", "fmt/950");
        formatCategoriesList[3] = Arrays.asList("x-fmt/111");
    }


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
        TestDialogWindow window = new TestDialogWindow(TechnicalSearchDialog.class);
    }

    /**
     * Instantiates a new SearchDialog for test.
     *
     * @param owner the owner
     */
    public TechnicalSearchDialog(JFrame owner) throws ResipException {
        this(new MainWindow(new ResipGraphicApp(null)));
    }

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public TechnicalSearchDialog(MainWindow owner) {
        super(owner, "Chercher des objets", false);
        GridBagConstraints gbc;

        searchRunning = false;

        mainWindow = owner;
        dataObjectPackageTreeViewer = mainWindow.getDataObjectPackageTreePaneViewer();
        dataObjectPackageTreeModel = (DataObjectPackageTreeModel) (dataObjectPackageTreeViewer.getModel());
        dataObjectListViewer=mainWindow.getDataObjectListViewer();

        setMinimumSize(new Dimension(500, 150));
        setResizable(false);

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        final JPanel criteriaPanel = new JPanel();
        criteriaPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(criteriaPanel, gbc);

        JLabel formatsLabel = new JLabel("Sélection des formats recherchés");
        formatsLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        criteriaPanel.add(formatsLabel, gbc);

        formatCategoryCheckBox = new JCheckBox();
        formatCategoryCheckBox.setText("Catégorie de formats :");
        formatCategoryCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        criteriaPanel.add(formatCategoryCheckBox, gbc);
        formatCategoryCheckBox.addItemListener(arg -> formatCategoryEvent(arg));
        formatCategoryComboBox = new JComboBox(formatCategories);
        formatCategoryComboBox.setEnabled(false);
        formatCategoryComboBox.setFont(MainWindow.LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        criteriaPanel.add(formatCategoryComboBox, gbc);

        formatListCheckBox = new JCheckBox();
        formatListCheckBox.setText("Liste libre de formats :");
        formatListCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        criteriaPanel.add(formatListCheckBox, gbc);
        formatListCheckBox.addItemListener(arg -> formatListEvent(arg));
        formatListTextField = new JTextField();
        formatListTextField.setText("");
        formatListTextField.setFont(MainWindow.DETAILS_FONT);
        formatListTextField.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 5);
        criteriaPanel.add(formatListTextField, gbc);

        JButton seeListButton = new JButton();
        seeListButton.setIcon(new ImageIcon(getClass().getResource("/icon/view-refresh.png")));
        seeListButton.setText("");
        seeListButton.setToolTipText("Voir la liste des formats recherchés");
        seeListButton.setMaximumSize(new Dimension(26, 26));
        seeListButton.setMinimumSize(new Dimension(26, 26));
        seeListButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        seeListButton.addActionListener(arg -> showFormatList());
        criteriaPanel.add(seeListButton, gbc);

        JScrollPane scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 5, 5);
        gbc.gridx = 3;
        gbc.gridy = 1;
        criteriaPanel.add(scrollPane, gbc);
        formatsTextArea = new JTextArea();
        formatsTextArea.setToolTipText("Liste des formats recherchés");
        formatsTextArea.setEditable(false);
        formatsTextArea.setFont(MainWindow.DETAILS_FONT);
        scrollPane.setViewportView(formatsTextArea);

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
        JLabel emptyLabel=new JLabel(" ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight=2;
        gbc.weightx=1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        actionPanel.add(emptyLabel, gbc);
        JButton searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(getClass().getResource("/icon/search-system.png")));
        searchButton.setText("");
        searchButton.setMaximumSize(new Dimension(26, 26));
        searchButton.setMinimumSize(new Dimension(26, 26));
        searchButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight=2;
        gbc.weightx=0.0;
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
        gbc.gridheight=2;
        gbc.weightx=0.0;
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
        gbc.gridheight=2;
        gbc.weightx=0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 5);
        actionPanel.add(previousButton, gbc);

        JPanel resultPanel=new JPanel();
        resultPanel.setLayout(new GridBagLayout());
        resultPanel.setPreferredSize(new Dimension(80,32));
        resultPanel.setMinimumSize(new Dimension(80,32));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridheight=2;
        gbc.weightx=1.0;
        gbc.weighty=1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        actionPanel.add(resultPanel, gbc);
        previousButton.addActionListener(arg -> buttonPrevious());
        resultArchiveUnitLabel = new JLabel();
        resultArchiveUnitLabel.setText("0 trouvé");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        resultPanel.add(resultArchiveUnitLabel, gbc);
        resultObjectLabel = new JLabel();
        resultObjectLabel.setText("                       ");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        resultPanel.add(resultObjectLabel, gbc);

        pack();
        setLocationRelativeTo(owner);
    }

    // actions

    private List<String> constructFormatList() {
        List<String> result = new ArrayList<String>();
        if (formatCategoryCheckBox.isSelected()) {
            result.addAll(formatCategoriesList[formatCategoryComboBox.getSelectedIndex()]);
        }
        if (formatListCheckBox.isSelected()) {
            List<String> formats = Arrays.asList(formatListTextField.getText().split(","));
            formats.replaceAll(String::trim);
            result.addAll(formats);
        }
        return result;
    }

    private void formatCategoryEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            formatCategoryComboBox.setEnabled(true);
        } else if (event.getStateChange() == DESELECTED) {
            formatCategoryComboBox.setEnabled(false);
        }
    }

    private void formatListEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            formatListTextField.setEnabled(true);
        } else if (event.getStateChange() == DESELECTED) {
            formatListTextField.setEnabled(false);
        }
    }

    private void showFormatList() {
        formatsTextArea.setText(String.join("\n", constructFormatList()));
    }


    private void focusObject() {
        TreePath path = new TreePath(dataObjectPackageTreeModel.getPathToRoot(dataObjectPackageTreeModel
                .findTreeNode(searchCurrentArchiveUnit)));
        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        mainWindow.dataObjectPackageTreeItemClick(path);
        mainWindow.dataObjectListItemClick(searchResult.get(searchCurrentArchiveUnit).get(searchObjectPosition));
        dataObjectListViewer.selectDataObject(searchResult.get(searchCurrentArchiveUnit).get(searchObjectPosition));
    }

    private void buttonSearch() {
        showFormatList();
        if (searchRunning) {
            resultArchiveUnitLabel.setText("En cours");
            resultObjectLabel.setText("");
        } else {
            searchRunning = true;
            dataObjectPackageTreeModel = (DataObjectPackageTreeModel) mainWindow.getDataObjectPackageTreePaneViewer().getModel();
            TechnicalSearchThread tst = new TechnicalSearchThread(this, mainWindow.getApp().currentWork.getDataObjectPackage().getGhostRootAu(),
                    constructFormatList());
            tst.execute();
        }
    }

    private void buttonNext() {
        if (searchResultCount > 0) {
            searchObjectPosition++;
            searchResultPosition = Math.min(searchResultPosition + 1, searchResultCount - 1);
            if (searchObjectPosition >= searchResult.get(searchCurrentArchiveUnit).size()) {
                searchArchiveUnitPosition++;
                if (searchArchiveUnitPosition >= searchResultList.size()) {
                    searchObjectPosition--;
                    searchArchiveUnitPosition--;
                } else {
                    searchObjectPosition = 0;
                    searchCurrentArchiveUnit = searchResultList.get(searchArchiveUnitPosition);
                }
            }
            resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
            resultObjectLabel.setText(stepObjectInfo());
            focusObject();
        }
    }

    private void buttonPrevious() {
        if (searchResultCount > 0) {
            searchObjectPosition--;
            searchResultPosition = Math.max(0, searchResultPosition - 1);
            if (searchObjectPosition < 0) {
                searchArchiveUnitPosition--;
                if (searchArchiveUnitPosition <0) {
                    searchObjectPosition++;
                    searchArchiveUnitPosition++;
                } else {
                    searchCurrentArchiveUnit = searchResultList.get(searchArchiveUnitPosition);
                    searchObjectPosition = searchResult.get(searchCurrentArchiveUnit).size() - 1;
                }
            }
            resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
            resultObjectLabel.setText(stepObjectInfo());
            focusObject();
        }
    }

    private String stepArchiveUnitInfo() {
        return "" + (searchArchiveUnitPosition + 1) + "/" + searchResultListCount + " AU trouvé" +
                (searchResultCount > 1 ? "s" : "");
    }

    private String stepObjectInfo() {
        if (searchResultListCount==0)
            return "";
        else
            return "" + (searchResultPosition + 1) + "/" + searchResultCount +
                " obj. trouvé" + (searchResultCount > 1 ? "s" : "");
    }

    /**
     * Set ArchiveUnit list search result.
     *
     * @param searchResult the search result
     */
    public void setSearchResult(LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchResult) {
        this.searchResult = searchResult;
        this.searchResultList = new ArrayList(searchResult.keySet());
        searchArchiveUnitPosition = 0;
        searchObjectPosition = 0;
        searchResultPosition = 0;
        searchResultCount = searchResultList.stream().mapToInt(a -> searchResult.get(a).size()).sum();
        searchResultListCount = searchResultList.size();
        if (searchResultCount > 0) {
            searchCurrentArchiveUnit = searchResultList.get(0);
            resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
            resultObjectLabel.setText(stepObjectInfo());
            focusObject();
        } else {
            resultArchiveUnitLabel.setText("0 trouvé");
            resultObjectLabel.setText("");
        }
        searchRunning = false;
    }
}
