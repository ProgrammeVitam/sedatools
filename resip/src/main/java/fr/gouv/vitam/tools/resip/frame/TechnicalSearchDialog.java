package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.threads.TechnicalSearchThread;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.viewer.DataObjectListViewer;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeViewer;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The class TechnicalSearchDialog.
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
    private JCheckBox sizeCheckBox;
    private JTextField minTextField;
    private JTextField maxTextField;
    private JTextArea formatsTextArea;
    private JLabel resultArchiveUnitLabel;
    private JLabel resultObjectLabel;
    private MainWindow mainWindow;
    private JPanel explanationPanel;
    private NumberFormatter numberFormatter;

    /**
     * The data.
     */
    private DataObjectPackageTreeViewer dataObjectPackageTreeViewer;
    private DataObjectPackageTreeModel dataObjectPackageTreeModel;
    private DataObjectListViewer dataObjectListViewer;
    private LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchResult;
    private ArrayList<ArchiveUnit> searchResultList;
    private int searchArchiveUnitPosition;
    private ArchiveUnit searchCurrentArchiveUnit;
    private int searchObjectPosition;
    private int searchResultPosition;
    private int searchResultCount;
    private int searchResultListCount;
    private TechnicalSearchThread technicalSearchThread;

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
     * @throws ResipException                  the resip exception
     * @throws InterruptedException            the interrupted exception
     */
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ResipException, InterruptedException {
        ResipGraphicApp rga = new ResipGraphicApp(null);
        Thread.sleep(1000);
        TestDialogWindow window = new TestDialogWindow(TechnicalSearchDialog.class);
    }

    /**
     * Instantiates a new SearchDialog for test.
     *
     * @param owner the owner
     * @throws ResipException the resip exception
     */
    public TechnicalSearchDialog(JFrame owner) throws ResipException {
        this(ResipGraphicApp.getTheApp().mainWindow);
    }

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public TechnicalSearchDialog(MainWindow owner) {
        super(owner, "Chercher des objets", false);
        GridBagConstraints gbc;

        technicalSearchThread = null;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        mainWindow = owner;
        dataObjectPackageTreeViewer = mainWindow.getDataObjectPackageTreePaneViewer();
        dataObjectPackageTreeModel = (DataObjectPackageTreeModel) (dataObjectPackageTreeViewer.getModel());
        dataObjectListViewer = mainWindow.getDataObjectListViewer();

        setMinimumSize(new Dimension(700, 200));
        //setResizable(false);

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
        formatCategoryComboBox = new JComboBox<>(ResipGraphicApp.getTheApp().
                treatmentParameters.getFormatByCategoryMap().keySet().toArray());
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
        formatsTextArea.setFont(MainWindow.LABEL_FONT);
        formatsTextArea.setColumns(10);
        scrollPane.setViewportView(formatsTextArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        sizeCheckBox = new JCheckBox();
        sizeCheckBox.setText("Taille entre :");
        sizeCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        criteriaPanel.add(sizeCheckBox, gbc);
        sizeCheckBox.addItemListener(arg -> sizeEvent(arg));
        minTextField = new JTextField();
        DocumentFilter filter = new NumericFilter();
        ((AbstractDocument) minTextField.getDocument()).setDocumentFilter(filter);
        minTextField.setText("");
        minTextField.setFont(MainWindow.DETAILS_FONT);
        minTextField.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 5);
        criteriaPanel.add(minTextField, gbc);
        JLabel andLabel = new JLabel(" et ");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        criteriaPanel.add(andLabel, gbc);
        maxTextField = new JTextField();
        ((AbstractDocument) maxTextField.getDocument()).setDocumentFilter(filter);
        maxTextField.setText("");
        maxTextField.setFont(MainWindow.DETAILS_FONT);
        maxTextField.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        criteriaPanel.add(maxTextField, gbc);

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
        moreOptionsCheckBox.setPreferredSize(new Dimension(250,36));
        moreOptionsCheckBox.setMinimumSize(new Dimension(250,36));
        moreOptionsCheckBox.setEnabled(true);
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
        JButton searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(getClass().getResource("/icon/search-system.png")));
        searchButton.setText("");
        searchButton.setMaximumSize(new Dimension(26, 26));
        searchButton.setMinimumSize(new Dimension(26, 26));
        searchButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 2;
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
        nextButton.addActionListener(arg -> buttonNext());
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 0);
        actionPanel.add(nextButton, gbc);
        JButton previousButton = new JButton();
        previousButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-up.png")));
        previousButton.setText("");
        previousButton.setMaximumSize(new Dimension(26, 26));
        previousButton.setMinimumSize(new Dimension(26, 26));
        previousButton.setPreferredSize(new Dimension(26, 26));
        previousButton.addActionListener(arg -> buttonPrevious());
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 5);
        actionPanel.add(previousButton, gbc);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridBagLayout());
        resultPanel.setMinimumSize(new Dimension(250, 36));
        resultPanel.setPreferredSize(new Dimension(250,36));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        actionPanel.add(resultPanel, gbc);
        resultArchiveUnitLabel = new JLabel();
        resultArchiveUnitLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        resultPanel.add(resultArchiveUnitLabel, gbc);
        resultObjectLabel = new JLabel();
        resultObjectLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        resultPanel.add(resultObjectLabel, gbc);

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
        resultArchiveUnitLabel.setText("Aucune recherche");
        pack();
        setLocationRelativeTo(owner);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    // actions

    private void close(){
        if (technicalSearchThread!=null) {
            technicalSearchThread.cancel(true);
            technicalSearchThread=null;
            resultArchiveUnitLabel.setText("Aucune recherche");
            resultObjectLabel.setText("");
        }
        setVisible(false);
    }

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

    private List<String> constructFormatList() {
        List<String> result = new ArrayList<String>();
        if (formatCategoryCheckBox.isSelected()) {
            result.addAll(ResipGraphicApp.getTheApp().treatmentParameters.getFormatByCategoryMap().
                    get((String) formatCategoryComboBox.getSelectedItem()));
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

    private void sizeEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            minTextField.setEnabled(true);
            maxTextField.setEnabled(true);
        } else if (event.getStateChange() == DESELECTED) {
            minTextField.setEnabled(false);
            maxTextField.setEnabled(false);
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
        if (technicalSearchThread==null) {
            dataObjectPackageTreeModel = (DataObjectPackageTreeModel) mainWindow.getDataObjectPackageTreePaneViewer().getModel();
            long min, max;
            if (minTextField.getText().isEmpty() || !sizeCheckBox.isSelected())
                min = 0;
            else {
                try {
                    min = Long.parseLong(minTextField.getText());
                } catch (NumberFormatException e) {
                    min = 0;
                }
            }
            if (maxTextField.getText().isEmpty() || !sizeCheckBox.isSelected())
                max = Long.MAX_VALUE;
            else
                try {
                    max = Long.parseLong(maxTextField.getText());
                } catch (NumberFormatException e) {
                    max = Long.MAX_VALUE;
                }
            if (min > max) {
                long tmp = max;
                max = min;
                min = tmp;
            }
            technicalSearchThread = new TechnicalSearchThread(this, mainWindow.getApp().currentWork.getDataObjectPackage().getGhostRootAu(),
                    constructFormatList(), min, max);
            technicalSearchThread.execute();
            resultArchiveUnitLabel.setText("En cours");
            resultObjectLabel.setText("");
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
                if (searchArchiveUnitPosition < 0) {
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
        if (searchResultListCount == 0)
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
        technicalSearchThread = null;
    }

    /**
     * Sets format category.
     *
     * @param formatCategory the format category
     */
    public void setFormatCategory(String formatCategory) {
        if (formatCategory == null) {
            formatCategoryCheckBox.setSelected(false);
            return;
        }
        if (ResipGraphicApp.getTheApp().treatmentParameters.getFormatByCategoryMap()
                .keySet().contains(formatCategory)) {
            formatCategoryCheckBox.setSelected(true);
            formatCategoryComboBox.setSelectedItem(formatCategory);
        }
    }

    /**
     * Sets format list.
     *
     * @param formatList the format list
     */
    public void setFormatList(String formatList) {
        if (formatList == null) {
            formatListCheckBox.setSelected(false);
            formatListTextField.setText("");
            return;
        }
        formatListCheckBox.setSelected(true);
        formatListTextField.setText(formatList);
    }


    /**
     * Sets min max.
     *
     * @param min the min
     * @param max the max
     */
    public void setMinMax(long min,long max) {
        if (min<0) {
            sizeCheckBox.setSelected(false);
            minTextField.setText("");
            maxTextField.setText("");
            return;
        }
        sizeCheckBox.setSelected(true);
        minTextField.setText(Long.toString(min));
        maxTextField.setText(Long.toString(max));
    }

    /**
     * Empty dialog. To be used when the context is changed.
     */
    public void emptyDialog(){
        setFormatCategory(null);
        setFormatList(null);
        setMinMax(-1,0);
        resultArchiveUnitLabel.setText("Aucune recherche effectuée");
        resultObjectLabel.setText("");
    }

    /**
     * Launch search process from another frame.
     */
    public void search(){
        buttonSearch();
    }
}
