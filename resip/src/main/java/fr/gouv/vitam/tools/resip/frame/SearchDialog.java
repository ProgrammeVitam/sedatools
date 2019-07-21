package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.threads.SearchThread;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeViewer;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The class SearchDialog.
 * <p>
 * Class for search dialog.
 */
public class SearchDialog extends JDialog {

    /**
     * The actions components.
     */
    private JTextField searchTextField;
    private JCheckBox regExpCheckBox;
    private JCheckBox caseCheckBox;
    private JCheckBox metadataCheckBox;
    private JCheckBox idCheckBox;
    private JLabel resultLabel;
    private MainWindow mainWindow;
    private JPanel optionalInfoPanel;

    /**
     * The data.
     */
    private DataObjectPackageTreeViewer dataObjectPackageTreeViewer;
    private DataObjectPackageTreeModel dataObjectPackageTreeModel;
    private List<ArchiveUnit> searchResult;
    private int searchResultPosition;
    private SearchThread searchThread;

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
        TestDialogWindow window = new TestDialogWindow(SearchDialog.class);
    }

    /**
     * Instantiates a new SearchDialog for test.
     *
     * @param owner the owner
     * @throws ResipException the resip exception
     */
    public SearchDialog(JFrame owner) throws ResipException {
        this(new MainWindow(new ResipGraphicApp(null)));
    }

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public SearchDialog(MainWindow owner) {
        super(owner, "Chercher des unités d'archives", false);

        searchThread = null;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        mainWindow=owner;
        dataObjectPackageTreeViewer=mainWindow.getDataObjectPackageTreePaneViewer();
        dataObjectPackageTreeModel=(DataObjectPackageTreeModel)(dataObjectPackageTreeViewer.getModel());

        setMinimumSize(new Dimension(500, 110));
        setPreferredSize(new Dimension(500, 110));
        setResizable(false);

        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        searchTextField = new JTextField();
        searchTextField.setText("");
        searchTextField.setFont(MainWindow.DETAILS_FONT);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 0);
        contentPane.add(searchTextField, gbc);

        JButton searchButton = new JButton();
        searchButton.setIcon(new ImageIcon(getClass().getResource("/icon/search-system.png")));
        searchButton.setText("");
        searchButton.setMaximumSize(new Dimension(26, 26));
        searchButton.setMinimumSize(new Dimension(26, 26));
        searchButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
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
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
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
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 0, 5, 5);
        contentPane.add(previousButton, gbc);
        previousButton.addActionListener(arg0 -> buttonPrevious());

        regExpCheckBox = new JCheckBox();
        regExpCheckBox.setText("RegExp");
        regExpCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(regExpCheckBox, gbc);
        regExpCheckBox.addItemListener(arg -> regexpEvent(arg));

        caseCheckBox = new JCheckBox();
        caseCheckBox.setText("Respecter la casse");
        caseCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(caseCheckBox, gbc);

        metadataCheckBox = new JCheckBox();
        metadataCheckBox.setText("Métadonnées");
        metadataCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(metadataCheckBox, gbc);

        idCheckBox = new JCheckBox();
        idCheckBox.setText("ID");
        idCheckBox.setFont(MainWindow.CLICK_FONT);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        contentPane.add(idCheckBox, gbc);

        resultLabel = new JLabel();
        resultLabel.setText("Aucune recherche");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridwidth = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(resultLabel, gbc);

        optionalInfoPanel = new JPanel();
        optionalInfoPanel.setLayout(new GridBagLayout());
        optionalInfoPanel.setVisible(true);
        gbc = new GridBagConstraints();
        gbc.gridwidth = 7;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(optionalInfoPanel, gbc);
        final JSeparator separator = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        optionalInfoPanel.add(separator, gbc);
        JLabel informationLabel = new JLabel();
        informationLabel.setText("");
        informationLabel.setIcon(new ImageIcon(getClass().getResource("/icon/large-dialog-information.png")));
        informationLabel.setFont(MainWindow.BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 1;
        optionalInfoPanel.add(informationLabel, gbc);
        JTextArea informationTextArea = new JTextArea("Il s'agit d'une recherche par expression régulière de type java d'une partie quelconque de la chaine, avec les caractères spéciaux classiques .?+*[]^|\\\\s\\w...\n"+
                "Pour plus d'info chercher sur Internet \"Regexp java\"");
        informationTextArea.setFont(MainWindow.LABEL_FONT);
        informationTextArea.setEditable(false);
        informationTextArea.setLineWrap(true);
        informationTextArea.setWrapStyleWord(true);
        informationTextArea.setBackground(MainWindow.GENERAL_BACKGROUND);
        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        optionalInfoPanel.add(informationTextArea, gbc);

        getRootPane().setDefaultButton(searchButton);

        pack();
        optionalInfoPanel.setVisible(false);
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
        if (searchThread!=null) {
            searchThread.cancel(true);
            searchThread=null;
            resultLabel.setText("Aucune recherche");
        }
        setVisible(false);
    }

    private void regexpEvent(ItemEvent event) {
        if (event.getStateChange() == SELECTED) {
            optionalInfoPanel.setVisible(true);
            Dimension dim = this.getSize();
            pack();
            dim.height = dim.height + optionalInfoPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            caseCheckBox.setEnabled(false);
            pack();
        } else if (event.getStateChange() == DESELECTED) {
            optionalInfoPanel.setVisible(false);
            Dimension dim = this.getSize();
            dim.height = dim.height - optionalInfoPanel.getHeight();
            this.setSize(dim);
            this.setPreferredSize(dim);
            caseCheckBox.setEnabled(true);
            pack();
        }
    }

    private void focusArchiveUnit(ArchiveUnit au) {
        TreePath path = new TreePath(dataObjectPackageTreeModel.getPathToRoot(dataObjectPackageTreeModel.findTreeNode(au)));
        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        mainWindow.dataObjectPackageTreeItemClick(path);
    }

    private void buttonSearch() {
        if (searchThread==null) {
            dataObjectPackageTreeModel = (DataObjectPackageTreeModel) mainWindow.getDataObjectPackageTreePaneViewer().getModel();
            searchThread = new SearchThread(this, mainWindow.getApp().currentWork.getDataObjectPackage().getGhostRootAu());
            searchThread.execute();
            searchResult=null;
            resultLabel.setText("En cours");
        }
    }

    private void buttonNext() {
        if (searchResult != null && searchResult.size() > 0) {
            searchResultPosition = Math.min(searchResultPosition + 1, searchResult.size() - 1);
                resultLabel.setText((searchResultPosition + 1) + "/" + searchResult.size()+" trouvé"+(searchResult.size()>1?"s":""));
            focusArchiveUnit(searchResult.get(searchResultPosition));
        }
    }

    private void buttonPrevious() {
        if (searchResult != null && searchResult.size() > 0) {
            searchResultPosition = Math.max(searchResultPosition - 1, 0);
            resultLabel.setText((searchResultPosition + 1) + "/" + searchResult.size()+" trouvé"+(searchResult.size()>1?"s":""));
            focusArchiveUnit(searchResult.get(searchResultPosition));
        }
    }

    /**
     * Is regexp check boolean.
     *
     * @return the boolean
     */
    public boolean isRegExpCheck(){
        return regExpCheckBox.isSelected();
    }

    /**
     * Is ID check boolean.
     *
     * @return the boolean
     */
    public boolean isIdCheck(){
        return idCheckBox.isSelected();
    }

    /**
     * Is metadata check boolean.
     *
     * @return the boolean
     */
    public boolean isMetadataCheck(){
        return metadataCheckBox.isSelected();
    }

    /**
     * Is case check boolean.
     *
     * @return the boolean
     */
    public boolean isCaseCheck(){
        return caseCheckBox.isSelected();
    }

    /**
     * Get search text string.
     *
     * @return the string
     */
    public String getSearchText(){
        return searchTextField.getText();
    }

    /**
     * Set ArchiveUnit list search result.
     *
     * @param searchResult the search result
     */
    public void setSearchResult(List<ArchiveUnit> searchResult){
        this.searchResult=searchResult;
        searchResultPosition=0;
        if (searchResult.size()>0) {
            resultLabel.setText("1/"+searchResult.size()+" trouvé"+(searchResult.size()>1?"s":""));
            focusArchiveUnit(searchResult.get(0));
        }
        else
            resultLabel.setText("0 trouvé");
        searchThread=null;
    }

    /**
     * Empty dialog. To be used when the context is changed.
     */
    public void emptyDialog(){
        searchTextField.setText("");
        regExpCheckBox.setSelected(false);
        metadataCheckBox.setSelected(false);
        caseCheckBox.setSelected(false);
        caseCheckBox.setEnabled(true);
    }
}
