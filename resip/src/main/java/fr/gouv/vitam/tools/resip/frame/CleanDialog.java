package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.threads.SearchThread;
import fr.gouv.vitam.tools.resip.threads.TechnicalSearchThread;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fr.gouv.vitam.tools.resip.app.ResipGraphicApp.OK_DIALOG;
import static fr.gouv.vitam.tools.resip.sedaobjecteditor.SEDAObjectEditor.BOLD_LABEL_FONT;
import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

/**
 * The class SearchDialog.
 * <p>
 * Class for search dialog.
 */
public class CleanDialog extends JDialog {

    /**
     * The actions components.
     */
    private JRadioButton zeroSizeRadioButton;
    private JRadioButton withoutChildArchiveUnitRadioButton;
    private JPanel explanationPanel;
    private JLabel resultArchiveUnitLabel;
    private JLabel resultObjectLabel;

    /**
     * The data.
     */
    private List<ArchiveUnit> searchArchiveUnitResult;
    private int searchResultPosition;
    private SearchThread searchThread;
    private TechnicalSearchThread technicalSearchThread;

    private LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchDataObjectResult;
    private ArrayList<ArchiveUnit> searchDataObjectResultList;
    private int searchArchiveUnitPosition;
    private ArchiveUnit searchCurrentArchiveUnit;
    private int searchObjectPosition;
    private int searchResultCount;
    private int searchResultListCount;

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
        TestDialogWindow window = new TestDialogWindow(CleanDialog.class);
    }

    /**
     * Instantiates a new SearchDialog for test.
     *
     * @param owner the owner
     * @throws ResipException the resip exception
     */
    public CleanDialog(JFrame owner) throws ResipException, InterruptedException {
        this(new MainWindow(new ResipGraphicApp(null)));
        Thread.sleep(1000);
    }

    /**
     * Create the dialog.
     *
     * @param owner the owner
     */
    public CleanDialog(MainWindow owner) {
        super(owner, "Nettoyer", false);
        GridBagConstraints gbc;
        GridBagLayout gbl;


        searchThread = null;
        technicalSearchThread = null;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        setMinimumSize(new Dimension(500, 170));
        setPreferredSize(new Dimension(500, 170));
        setResizable(false);

        Container contentPane = getContentPane();
        gbl = new GridBagLayout();
        gbl.columnWeights = new double[]{1.0, 0.1, 0.0, 0.0};
        gbl.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0};
        contentPane.setLayout(gbl);

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
        gbc.gridx = 2;
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
        gbc.gridx = 3;
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

        JLabel cleanChoiceLabel = new JLabel("Action de nettoyage:");
        cleanChoiceLabel.setFont(BOLD_LABEL_FONT);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        choicePanel.add(cleanChoiceLabel, gbc);

        zeroSizeRadioButton = new JRadioButton();
        zeroSizeRadioButton.setText("Supprimer un objet numérique de taille nulle");
        zeroSizeRadioButton.setFont(MainWindow.CLICK_FONT);
        zeroSizeRadioButton.setHorizontalAlignment(SwingConstants.LEADING);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        choicePanel.add(zeroSizeRadioButton, gbc);
        zeroSizeRadioButton.setSelected(true);
        zeroSizeRadioButton.addActionListener(e -> changeChoice(e));

        withoutChildArchiveUnitRadioButton = new JRadioButton();
        withoutChildArchiveUnitRadioButton.setText("Supprimer une unité d'archives sans descendance");
        withoutChildArchiveUnitRadioButton.setFont(MainWindow.CLICK_FONT);
        withoutChildArchiveUnitRadioButton.setHorizontalAlignment(SwingConstants.LEADING);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        choicePanel.add(withoutChildArchiveUnitRadioButton, gbc);
        withoutChildArchiveUnitRadioButton.addActionListener(e -> changeChoice(e));

        ButtonGroup groupLocalProtocol = new ButtonGroup();
        groupLocalProtocol.add(zeroSizeRadioButton);
        groupLocalProtocol.add(withoutChildArchiveUnitRadioButton);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridBagLayout());
        resultPanel.setMinimumSize(new Dimension(150, 36));
        resultPanel.setPreferredSize(new Dimension(150, 36));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.gridy = 1;
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
        resultObjectLabel = new JLabel();
        resultObjectLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        resultPanel.add(resultObjectLabel, gbc);

        JButton cleanAll = new JButton("Nettoyer tout");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(cleanAll, gbc);
        cleanAll.addActionListener(e -> doCleanAll());

        JButton cleanOne = new JButton("Nettoyer la sélection");
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(cleanOne, gbc);
        cleanOne.addActionListener(e -> doCleanOne());

        JCheckBox moreOptionsCheckBox = new JCheckBox();
        moreOptionsCheckBox.setEnabled(true);
        moreOptionsCheckBox.setIcon(new ImageIcon(getClass().getResource("/icon/list-add.png")));
        moreOptionsCheckBox.setSelectedIcon(new ImageIcon(getClass().getResource("/icon/list-remove.png")));
        moreOptionsCheckBox.setText("En savoir plus");
        moreOptionsCheckBox.addItemListener(arg -> moreExplanationEvent(arg));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        contentPane.add(moreOptionsCheckBox, gbc);

        explanationPanel = new JPanel();
        explanationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(explanationPanel, gbc);
        final JSeparator separator = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        explanationPanel.add(separator, gbc);
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
        explanationTextArea.setText("Le nettoyage va éliminer des éléments jugés inutiles ou contraire à la construction de paquets SEDA corrects:\n\n" +
                "  - objet numériques de taille nulle: l'objet et ses métadonnées sont supprimés ainsi que le groupe d'objets si lui-même se retrouve vide,\n\n" +
                "  - unité d'archives sans descendance: l'unité d'archive qui n'a ni descendance en unité d'archives, ni objet (numérique ou physique) associé est supprimée avec toutes ses métadonnées.");
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
        setLocationRelativeTo(owner);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    void changeChoice(ActionEvent e) {
        if (searchThread != null) {
            searchThread.cancel(true);
            searchThread = null;
        }
        if (technicalSearchThread != null) {
            technicalSearchThread.cancel(true);
            technicalSearchThread = null;
        }
        resultArchiveUnitLabel.setText("Aucune recherche");
        resultObjectLabel.setText("");
    }

    private void removeDataObject(ArchiveUnit archiveUnit, BinaryDataObject bdo) {
        archiveUnit.getTheDataObjectGroup().removeDataObject(bdo);
    }

    private void removeDataObjectGroup(ArchiveUnit archiveUnit) {
        DataObjectGroup dog = archiveUnit.getTheDataObjectGroup();
        archiveUnit.removeDataObjectById(dog.getInDataObjectPackageId());
        archiveUnit.getDataObjectPackage().getDogInDataObjectPackageIdMap().remove(dog.getInDataObjectPackageId());
    }

    private boolean confirmCleanAll(int auCount, int bdoCount){
        return (UserInteractionDialog.getUserAnswer(ResipGraphicApp.getTheWindow(),
                "Vous allez supprimer "+(auCount!=0?auCount+" unité"+(auCount>1?"s":"")+" d'archives":"")+
                        (bdoCount!=0?bdoCount+" objet"+(bdoCount>1?"s":"")+" binaire"+(bdoCount>1?"s":""):"")+".\n"
                        + "Voulez-vous continuer?",
                "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                null) == OK_DIALOG);
    }

    void doCleanAll() {
        if ((searchDataObjectResult != null) && (searchResultCount > 0)) {
            if (!confirmCleanAll(0,searchResultCount))
                return;
            for (Map.Entry<ArchiveUnit, List<BinaryDataObject>> e : searchDataObjectResult.entrySet()) {
                for (BinaryDataObject bdo : e.getValue()) {
                    removeDataObject(e.getKey(), bdo);
                    DataObjectGroup dog = e.getKey().getTheDataObjectGroup();
                    if (dog.getBinaryDataObjectList().size() + dog.getPhysicalDataObjectList().size() == 0)
                        removeDataObjectGroup(e.getKey());
                }
            }
            searchResultCount = 0;
            searchResultListCount = 0;
            searchDataObjectResult = null;
            searchDataObjectResultList = null;
            resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
            resultObjectLabel.setText(stepObjectInfo());
            ResipGraphicApp.getTheWindow().treePane.doRefreshTree();
        } else if ((searchArchiveUnitResult != null) && (searchArchiveUnitResult.size() > 0)) {
            if (!confirmCleanAll(searchArchiveUnitResult.size(),0))
                return;
           for (ArchiveUnit archiveUnit : searchArchiveUnitResult) {
                archiveUnit.getDataObjectPackage().removeEmptyArchiveUnit(archiveUnit);
            }
            searchArchiveUnitResult = null;
            resultArchiveUnitLabel.setText("0 trouvé");
            ResipGraphicApp.getTheWindow().treePane.doRefreshTree();
        }
    }

    void doCleanOne() {
        if ((searchDataObjectResult != null) && (searchResultCount > 0)) {
            removeDataObject(searchCurrentArchiveUnit, searchDataObjectResult.get(searchCurrentArchiveUnit).get(searchObjectPosition));
            DataObjectGroup dog = searchCurrentArchiveUnit.getTheDataObjectGroup();
            if (dog.getBinaryDataObjectList().size() + dog.getPhysicalDataObjectList().size() == 0)
                removeDataObjectGroup(searchCurrentArchiveUnit);
            searchDataObjectResult.get(searchCurrentArchiveUnit).remove(searchObjectPosition);
            searchResultCount--;
            if (searchDataObjectResult.get(searchCurrentArchiveUnit).size() == 0) {
                searchDataObjectResult.remove(searchCurrentArchiveUnit);
                searchResultListCount--;
                searchDataObjectResultList = new ArrayList(searchDataObjectResult.keySet());
                if (searchArchiveUnitPosition >= searchDataObjectResultList.size()) {
                    searchArchiveUnitPosition--;
                    if (searchArchiveUnitPosition >= 0) {
                        searchCurrentArchiveUnit = searchDataObjectResultList.get(searchArchiveUnitPosition);
                        searchObjectPosition = searchDataObjectResult.get(searchCurrentArchiveUnit).size() - 1;
                        searchResultPosition--;
                    }
                } else {
                    searchCurrentArchiveUnit = searchDataObjectResultList.get(searchArchiveUnitPosition);
                    searchObjectPosition = 0;
                }
                resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
                resultObjectLabel.setText(stepObjectInfo());
                ResipGraphicApp.getTheWindow().treePane.doRefreshTree();
                if (searchResultCount > 0) {
                    ResipGraphicApp.getTheWindow().treePane.focusDataObject(searchCurrentArchiveUnit, searchDataObjectResult.get(searchCurrentArchiveUnit).get(searchObjectPosition));
                }
            }
            try {
                ResipGraphicApp.getTheWindow().auMetadataPane.editArchiveUnit(searchCurrentArchiveUnit);
            } catch (SEDALibException ignored) {
            }
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        } else if ((searchArchiveUnitResult != null) && (searchArchiveUnitResult.size() > 0)) {
            searchArchiveUnitResult.get(searchResultPosition).getDataObjectPackage().removeEmptyArchiveUnit(searchArchiveUnitResult.get(searchResultPosition));
            searchArchiveUnitResult.remove(searchResultPosition);
            searchResultPosition = Math.min(searchResultPosition, searchArchiveUnitResult.size() - 1);
            ResipGraphicApp.getTheWindow().treePane.doRefreshTree();
            if (searchResultPosition >= 0) {
                resultArchiveUnitLabel.setText((searchResultPosition + 1) + "/" + searchArchiveUnitResult.size() + " trouvé" + (searchArchiveUnitResult.size() > 1 ? "s" : ""));
                ResipGraphicApp.getTheWindow().treePane.focusArchiveUnit(searchArchiveUnitResult.get(searchResultPosition));
            }
            else
                resultArchiveUnitLabel.setText("0 trouvé");
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        }
    }

    // actions

    private void close() {
        if (searchThread != null) {
            searchThread.cancel(true);
            searchThread = null;
        }
        if (technicalSearchThread != null) {
            technicalSearchThread.cancel(true);
            technicalSearchThread = null;
        }
        emptyDialog();
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

    private void buttonSearch() {
        if ((searchThread == null) && (technicalSearchThread == null)) {
            if (withoutChildArchiveUnitRadioButton.isSelected()) {
                searchThread = new SearchThread(ResipGraphicApp.getTheApp().currentWork.getDataObjectPackage().getGhostRootAu(),
                        true, true,
                        false, false, false, false,
                        "", e -> setSearchArchiveUnitResult(e));
                searchThread.execute();
            } else {
                technicalSearchThread = new TechnicalSearchThread(ResipGraphicApp.getTheApp().currentWork.getDataObjectPackage().getGhostRootAu(),
                        new ArrayList<String>(), 0, 0, e -> setDataObjectSearchResult(e));
                technicalSearchThread.execute();
            }
            resultArchiveUnitLabel.setText("En cours");
            resultObjectLabel.setText("");
            searchArchiveUnitResult = null;
            searchDataObjectResult = null;
            searchDataObjectResultList = null;
        }
    }

    private String stepArchiveUnitInfo() {
        if (searchResultCount == 0)
            return "0 trouvé";
        else
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

    private void buttonNext() {
        if (searchArchiveUnitResult != null && searchArchiveUnitResult.size() > 0) {
            searchResultPosition = Math.min(searchResultPosition + 1, searchArchiveUnitResult.size() - 1);
            resultArchiveUnitLabel.setText((searchResultPosition + 1) + "/" + searchArchiveUnitResult.size() + " trouvé" + (searchArchiveUnitResult.size() > 1 ? "s" : ""));
            ResipGraphicApp.getTheWindow().treePane.focusArchiveUnit(searchArchiveUnitResult.get(searchResultPosition));
        } else if ((searchDataObjectResult != null) && (searchResultCount > 0)) {
            searchObjectPosition++;
            searchResultPosition = Math.min(searchResultPosition + 1, searchResultCount - 1);
            if (searchObjectPosition >= searchDataObjectResult.get(searchCurrentArchiveUnit).size()) {
                searchArchiveUnitPosition++;
                if (searchArchiveUnitPosition >= searchDataObjectResultList.size()) {
                    searchObjectPosition--;
                    searchArchiveUnitPosition--;
                } else {
                    searchObjectPosition = 0;
                    searchCurrentArchiveUnit = searchDataObjectResultList.get(searchArchiveUnitPosition);
                }
            }
            resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
            resultObjectLabel.setText(stepObjectInfo());
            ResipGraphicApp.getTheWindow().treePane.focusDataObject(searchCurrentArchiveUnit, searchDataObjectResult.get(searchCurrentArchiveUnit).get(searchObjectPosition));
        }
    }

    private void buttonPrevious() {
        if (searchArchiveUnitResult != null && searchArchiveUnitResult.size() > 0) {
            searchResultPosition = Math.max(searchResultPosition - 1, 0);
            resultArchiveUnitLabel.setText((searchResultPosition + 1) + "/" + searchArchiveUnitResult.size() + " trouvé" + (searchArchiveUnitResult.size() > 1 ? "s" : ""));
            ResipGraphicApp.getTheWindow().treePane.focusArchiveUnit(searchArchiveUnitResult.get(searchResultPosition));
        } else if ((searchDataObjectResult != null) && (searchResultCount > 0)) {
            searchObjectPosition--;
            searchResultPosition = Math.max(0, searchResultPosition - 1);
            if (searchObjectPosition < 0) {
                searchArchiveUnitPosition--;
                if (searchArchiveUnitPosition < 0) {
                    searchObjectPosition++;
                    searchArchiveUnitPosition++;
                } else {
                    searchCurrentArchiveUnit = searchDataObjectResultList.get(searchArchiveUnitPosition);
                    searchObjectPosition = searchDataObjectResult.get(searchCurrentArchiveUnit).size() - 1;
                }
            }
            resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
            resultObjectLabel.setText(stepObjectInfo());
            ResipGraphicApp.getTheWindow().treePane.focusDataObject(searchCurrentArchiveUnit, searchDataObjectResult.get(searchCurrentArchiveUnit).get(searchObjectPosition));
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
        searchThread = null;
    }

    /**
     * Set DataObject list search result.
     *
     * @param searchResult the search result
     */
    public void setDataObjectSearchResult(LinkedHashMap<ArchiveUnit, List<BinaryDataObject>> searchResult) {
        this.searchDataObjectResult = searchResult;
        this.searchDataObjectResultList = new ArrayList(searchResult.keySet());
        searchArchiveUnitPosition = 0;
        searchObjectPosition = 0;
        searchResultPosition = 0;
        searchResultCount = searchDataObjectResultList.stream().mapToInt(a -> searchResult.get(a).size()).sum();
        searchResultListCount = searchDataObjectResultList.size();
        if (searchResultCount > 0) {
            searchCurrentArchiveUnit = searchDataObjectResultList.get(0);
            resultArchiveUnitLabel.setText(stepArchiveUnitInfo());
            resultObjectLabel.setText(stepObjectInfo());
            ResipGraphicApp.getTheWindow().treePane.focusDataObject(searchCurrentArchiveUnit, searchResult.get(searchCurrentArchiveUnit).get(searchObjectPosition));
        } else {
            resultArchiveUnitLabel.setText("0 trouvé");
            resultObjectLabel.setText("");
        }
        technicalSearchThread = null;
    }

    /**
     * Empty dialog. To be used when the context is changed.
     */
    public void emptyDialog() {
        zeroSizeRadioButton.setSelected(true);
        resultArchiveUnitLabel.setText("Aucune recherche");
        resultObjectLabel.setText("");
    }
}
