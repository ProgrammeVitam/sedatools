/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.resip.frame;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.threads.DuplicatesThread;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.*;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;

/**
 * The class DuplicatesWindow.
 * <p>
 * Class for duplicates search and management.
 */
public class DuplicatesWindow extends JFrame {

    /**
     * Actions components.
     */
    private JCheckBox binaryHashCheckBox;
    private JCheckBox binaryFilenameCheckBox;
    private JCheckBox physicalAllMDCheckBox;
    private JTable duplicatesTable;
    private JLabel lineResultLabel;
    private JLabel globalResultLabel;
    private JButton lineDedupButton;
    private JButton allDedupButton;

    private MainWindow mainWindow;

    /**
     * Data.
     */
    private List<DataObjectGroup> dogList;
    private List<ArchiveUnit> auList;
    private int dogListPosition;
    private int auListPosition;

    /**
     * Duplicates thread.
     */
    DuplicatesThread duplicatesThread;

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
     * @throws ResipException                  the resip exception
     * @throws InterruptedException            the interrupted exception
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
        JLabel resultPlaceHolder;
        JPanel resultPanel;
        JPanel explanationPanel;
        GridBagConstraints gbc;

        java.net.URL imageURL = getClass().getClassLoader().getResource("VitamIcon96.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            setIconImage(icon.getImage());
        }
        this.setTitle("Chercher des doublons");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        duplicatesThread = null;

        mainWindow = ResipGraphicApp.getTheApp().mainWindow;

        setMinimumSize(new Dimension(600, 150));
        setPreferredSize(new Dimension(800, 500));

        Container contentPane = getContentPane();
        GridBagLayout gridBagLayout = new GridBagLayout();
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
        binaryHashCheckBox.setEnabled(false);
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
        gbc.weightx = 1.0;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        actionPanel.add(separator, gbc);

        resultPlaceHolder = new JLabel(" ");
        resultPlaceHolder.setMinimumSize(new Dimension(250, 36));
        resultPlaceHolder.setPreferredSize(new Dimension(250, 36));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        actionPanel.add(resultPlaceHolder, gbc);

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
        gbc.insets = new Insets(5, 5, 5, 5);
        actionPanel.add(searchButton, gbc);
        searchButton.addActionListener(arg0 -> buttonSearch());

        JLabel auLabel = new JLabel(" AU");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        actionPanel.add(auLabel, gbc);

        JButton auNextButton = new JButton();
        auNextButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-down.png")));
        auNextButton.setText("");
        auNextButton.setMaximumSize(new Dimension(26, 26));
        auNextButton.setMinimumSize(new Dimension(26, 26));
        auNextButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5, 5, 5, 0);
        actionPanel.add(auNextButton, gbc);
        auNextButton.addActionListener(arg -> buttonNextAU());
        JButton auPreviousButton = new JButton();
        auPreviousButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-up.png")));
        auPreviousButton.setText("");
        auPreviousButton.setMaximumSize(new Dimension(26, 26));
        auPreviousButton.setMinimumSize(new Dimension(26, 26));
        auPreviousButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        actionPanel.add(auPreviousButton, gbc);
        auPreviousButton.addActionListener(arg -> buttonPreviousAU());

        JLabel dogLabel = new JLabel(" DOG");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        actionPanel.add(dogLabel, gbc);

        JButton dogNextButton = new JButton();
        dogNextButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-down.png")));
        dogNextButton.setText("");
        dogNextButton.setMaximumSize(new Dimension(26, 26));
        dogNextButton.setMinimumSize(new Dimension(26, 26));
        dogNextButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5, 5, 5, 0);
        actionPanel.add(dogNextButton, gbc);
        dogNextButton.addActionListener(arg -> buttonNextDOG());
        JButton dogPreviousButton = new JButton();
        dogPreviousButton.setIcon(new ImageIcon(getClass().getResource("/icon/go-up.png")));
        dogPreviousButton.setText("");
        dogPreviousButton.setMaximumSize(new Dimension(26, 26));
        dogPreviousButton.setMinimumSize(new Dimension(26, 26));
        dogPreviousButton.setPreferredSize(new Dimension(26, 26));
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5, 0, 5, 5);
        actionPanel.add(dogPreviousButton, gbc);
        dogPreviousButton.addActionListener(arg -> buttonPreviousDOG());

        resultPanel = new JPanel();
        resultPanel.setLayout(new GridBagLayout());
        resultPanel.setMinimumSize(new Dimension(250, 36));
        resultPanel.setPreferredSize(new Dimension(250, 36));
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        actionPanel.add(resultPanel, gbc);
        globalResultLabel = new JLabel();
        globalResultLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(5, 5, 0, 5);
        resultPanel.add(globalResultLabel, gbc);
        lineResultLabel = new JLabel();
        lineResultLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(0, 5, 5, 5);
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
        duplicatesTable.getColumnModel().getColumn(2)
                .setPreferredWidth(20);
        ListSelectionModel selectionModel = duplicatesTable.getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                handleDuplicatesSelectionEvent(e);
            }
        });
        scrollPane.setViewportView(duplicatesTable);

        final JPanel deduplicateActionPanel = new JPanel();
        deduplicateActionPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(deduplicateActionPanel, gbc);

        lineDedupButton = new JButton("Fusionner la ligne");
        lineDedupButton.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        lineDedupButton.addActionListener(arg0 -> buttonLineMelt());
        deduplicateActionPanel.add(lineDedupButton, gbc);

        allDedupButton = new JButton("Fusionner tous les doublons");
        allDedupButton.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        allDedupButton.addActionListener(arg0 -> buttonAllMelt());
        deduplicateActionPanel.add(allDedupButton, gbc);

        JLabel placeHolder = new JLabel("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        deduplicateActionPanel.add(placeHolder, gbc);


        explanationPanel = new JPanel();
        explanationPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
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
        globalResultLabel.setText("Aucune recherche effectuée");
        lineResultLabel.setText("Aucun lot sélectionné");
        setLocationRelativeTo(mainWindow);

        dogList = null;
        auList = null;
        dogListPosition = 0;
        auListPosition = 0;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    // actions

    private void close() {
        if (duplicatesThread != null) {
            duplicatesThread.cancel(true);
            duplicatesThread = null;
        }
        setVisible(false);
    }

    private void meldLine(int line) {
        DuplicatesTableModel model = (DuplicatesTableModel) duplicatesTable.getModel();
        List<ArchiveUnit> auList = model.getRowAuList(line);
        List<DataObjectGroup> dogList = model.getRowDogList(line);
        List<BinaryDataObject> originBdoList = dogList.get(0).getBinaryDataObjectList();
        // meld using existing DOGs
        List<DataObjectGroup> newDogList = new ArrayList<DataObjectGroup>();
        int afterMeldDOGNumber = (int) Math.ceil((double) auList.size() / (double) ResipGraphicApp.getTheApp().treatmentParameters.getDupMax());
        if (dogList.size() <= afterMeldDOGNumber)
            return;
        for (int i = 0; i < afterMeldDOGNumber; i++) {
            DataObjectGroup dog = dogList.get(i);
            newDogList.add(dog);
            List<BinaryDataObject> bdoList = dog.getBinaryDataObjectList();
            for (int j = 0; j < originBdoList.size(); j++) {
                FileInfo bdoFIleInfo=bdoList.get(j).getMetadataFileInfo();
                FileInfo originBdoFIleInfo=originBdoList.get(j).getMetadataFileInfo();
                try {
                    bdoFIleInfo.addNewMetadata("Filename", originBdoFIleInfo.getSimpleMetadata("Filename"));
                    bdoFIleInfo.addNewMetadata("LastModified", originBdoFIleInfo.getSimpleMetadata("LastModified"));
                } catch (SEDALibException ignored) {
                }
            }
            for (int j = i * ResipGraphicApp.getTheApp().treatmentParameters.getDupMax(); j < Math.min((i + 1) * ResipGraphicApp.getTheApp().treatmentParameters.getDupMax(), auList.size()); j++) {
                ArchiveUnit au = auList.get(j);
                DataObjectRefList dorl = new DataObjectRefList(au.getDataObjectPackage());
                dorl.add(dog);
                au.setDataObjectRefList(dorl);
            }
        }
        ((DuplicatesTableModel) duplicatesTable.getModel()).changeRowDogList(newDogList, line);
        // suppress BDO, PDO and DOGs not used by melting
        DataObjectPackage dop = auList.get(0).getDataObjectPackage();
        for (int i = afterMeldDOGNumber; i < dogList.size(); i++) {
            for (BinaryDataObject bdo : dogList.get(i).getBinaryDataObjectList()) {
                dop.getBdoInDataObjectPackageIdMap().remove(bdo.getInDataObjectPackageId());
            }
            for (PhysicalDataObject pdo : dogList.get(i).getPhysicalDataObjectList()) {
                dop.getPdoInDataObjectPackageIdMap().remove(pdo.getInDataObjectPackageId());
            }
            dop.getDogInDataObjectPackageIdMap().remove(dogList.get(i).getInDataObjectPackageId());
        }
    }

    private void refreshGraphic() {
        mainWindow.treePane.doRefreshTree();
        duplicatesTable.repaint();
        if (dogList != null)
            lineResultLabel.setText("rang " + (dogListPosition + 1) + "/" + dogList.size() + " DOG et "
                    + (auListPosition + 1) + "/" + auList.size() + " AU sur la ligne");
    }

    private void buttonLineMelt() {
        if (auList.size() > ResipGraphicApp.getTheApp().treatmentParameters.getDupMax()) {
            if ((auList.size() / ResipGraphicApp.getTheApp().treatmentParameters.getDupMax()) + 1 >= dogList.size()) {
                UserInteractionDialog.getUserAnswer(mainWindow,
                        "Ce lot de doublons représente un groupe d'objets référencé par " + auList.size() + " ArchiveUnit.\n"
                                + "La fusion peut être faite au plus par lots de " + ResipGraphicApp.getTheApp().treatmentParameters.getDupMax() + " ArchiveUnit. Il n'est pas possible de fusionner plus.",
                        "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                        null);
                return;
            } else UserInteractionDialog.getUserAnswer(mainWindow,
                    "Ce lot de doublons représente un groupe d'objets référencé par " + auList.size() + " ArchiveUnit.\n"
                            + "La fusion sera faite par lots de " + ResipGraphicApp.getTheApp().treatmentParameters.getDupMax() + " ArchiveUnit au plus.",
                    "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                    null);
        }

        int selectedRow = duplicatesTable
                .convertRowIndexToModel(duplicatesTable.getSelectionModel().getMinSelectionIndex());
        meldLine(selectedRow);
        dogList = ((DuplicatesTableModel) duplicatesTable.getModel()).getRowDogList(selectedRow);
        dogListPosition = 0;
        auListPosition = 0;
        refreshGraphic();
    }

    private void buttonAllMelt() {
        DuplicatesTableModel dtm = ((DuplicatesTableModel) (duplicatesTable.getModel()));
        int moreThanDuplicatesLimit = 0;
        int cantMeltMore = 0;
        for (int i = 0; i < dtm.getRowCount(); i++) {
            List<ArchiveUnit> localAuList = dtm.getRowAuList(i);
            List<DataObjectGroup> localDogList = dtm.getRowDogList(i);
            if (localAuList.size() > ResipGraphicApp.getTheApp().treatmentParameters.getDupMax()) {
                if ((localAuList.size() / ResipGraphicApp.getTheApp().treatmentParameters.getDupMax()) + 1 >= localDogList.size())
                    cantMeltMore++;
                else
                    moreThanDuplicatesLimit++;
            }
        }
        if ((moreThanDuplicatesLimit > 0) || (cantMeltMore > 0)) {
            String message = "Dans l'ensemble:";
            if (moreThanDuplicatesLimit > 0)
                message += "\n  - " + moreThanDuplicatesLimit + " ligne" + (moreThanDuplicatesLimit > 1 ? "s ont" : " a")
                        + " plus de doublons que la taille maximale de fusion [" + ResipGraphicApp.getTheApp().treatmentParameters.getDupMax()
                        + "]. La fusion sera donc faite en plusieurs lots.";
            if (cantMeltMore > 0)
                message += "\n  - " + cantMeltMore + " ligne" + (cantMeltMore > 1 ? "s ont" : " a")
                        + " plus de doublons que la taille maximale de fusion [" + ResipGraphicApp.getTheApp().treatmentParameters.getDupMax()
                        + "], mais " + (cantMeltMore > 1 ? "ont" : "a") + " déjà été fusionnée" + (cantMeltMore > 1 ? "s" : "")
                        + " au maximum.";
            UserInteractionDialog.getUserAnswer(mainWindow, message, "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                    null);
        }


        int selectedindex = duplicatesTable.getSelectionModel().getMinSelectionIndex();
        int selectedRow = -1;
        if (selectedindex >= 0)
            selectedRow = duplicatesTable.convertRowIndexToModel(selectedindex);
        for (int i = 0; i < dtm.getRowCount(); i++)
            meldLine(i);
        if (selectedRow >= 0)
            dogList = dtm.getRowDogList(selectedRow);
        dogListPosition = 0;
        auListPosition = 0;
        refreshGraphic();
    }

    private void handleDuplicatesSelectionEvent(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;

        final DefaultListSelectionModel target = (DefaultListSelectionModel) e.getSource();
        int selectedIndex = target.getMinSelectionIndex();
        if (selectedIndex < 0) {
            lineResultLabel.setText("");
            dogList = null;
            return;
        }
        DuplicatesTableModel model = (DuplicatesTableModel) duplicatesTable.getModel();
        dogList = model.getRowDogList(duplicatesTable.convertRowIndexToModel(selectedIndex));
        auList = model.getRowAuList(duplicatesTable.convertRowIndexToModel(selectedIndex));
        dogListPosition = 0;
        auListPosition = 0;
        lineResultLabel.setText("rang " + (dogListPosition + 1) + "/" + dogList.size() + " DOG et "
                + (auListPosition + 1) + "/" + auList.size() + " AU sur la ligne");
        lineDedupButton.setEnabled(true);
        mainWindow.treePane.focusDataObjectGroup(dogList.get(dogListPosition));
    }

/*    private DataObjectPackageTreeNode focusNode(DataObjectGroup dog) {
        DataObjectPackageTreeNode focusNode = dataObjectPackageTreeModel.findTreeNode(dog);
        TreePath path = new TreePath(dataObjectPackageTreeModel.getPathToRoot(focusNode));

        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        mainWindow.treePane.selectTreePathItem(path);
        if (dog.getBinaryDataObjectList().size() >= 1) {
            try {
                mainWindow.dogMetadataPane.selectDataObject(dog.getBinaryDataObjectList().get(0));
            } catch (SEDALibException e) {
                e.printStackTrace();
            }
        }
        return (DataObjectPackageTreeNode) focusNode.getParent();
    }

    private void focusNode(ArchiveUnit au) {
        DataObjectPackageTreeNode focusNode = dataObjectPackageTreeModel.findTreeNode(au);
        DataObjectGroup dog = getDog(au);
        TreePath path = new TreePath(dataObjectPackageTreeModel.getPathToRoot(focusNode));

        dataObjectPackageTreeViewer.setExpandsSelectedPaths(true);
        dataObjectPackageTreeViewer.setSelectionPath(path);
        dataObjectPackageTreeViewer.scrollPathToVisible(path);
        mainWindow.treePane.selectTreePathItem(path);
        if (dog.getBinaryDataObjectList().size() >= 1) {
            try {
                mainWindow.dogMetadataPane.selectDataObject(dog.getBinaryDataObjectList().get(0));
            } catch (SEDALibException e) {
                e.printStackTrace();
            }
        }
    }*/

    private void buttonSearch() {
        if (duplicatesThread == null) {
            emptyDialog();
            globalResultLabel.setText("En cours");
            duplicatesThread = new DuplicatesThread(this, binaryHashCheckBox.isSelected(),
                    binaryFilenameCheckBox.isSelected(),
                    physicalAllMDCheckBox.isSelected());
            duplicatesThread.execute();
        }
    }

    private DataObjectGroup getDog(ArchiveUnit au) {
        DataObject dataObject = au.getDataObjectRefList().getDataObjectList().get(0);
        return (DataObjectGroup) dataObject;
    }


    private void buttonNextAU() {
        if ((auList != null) && (auListPosition < auList.size() - 1)) {
            auListPosition++;
            if (!getDog(auList.get(auListPosition - 1)).equals(getDog(auList.get(auListPosition))))
                dogListPosition = dogList.indexOf(getDog(auList.get(auListPosition)));
            lineResultLabel.setText("rang " + (dogListPosition + 1) + "/" + dogList.size() + " DOG et "
                    + (auListPosition + 1) + "/" + auList.size() + " AU sur la ligne");
            mainWindow.treePane.focusArchiveUnit(auList.get(auListPosition));
        }
    }

    private void buttonPreviousAU() {
        if ((auList != null) && (auListPosition > 0)) {
            auListPosition--;
            if (!getDog(auList.get(auListPosition + 1)).equals(getDog(auList.get(auListPosition))))
                dogListPosition = dogList.indexOf(getDog(auList.get(auListPosition)));
            lineResultLabel.setText("rang " + (dogListPosition + 1) + "/" + dogList.size() + " DOG et "
                    + (auListPosition + 1) + "/" + auList.size() + " AU sur la ligne");
            mainWindow.treePane.focusArchiveUnit(auList.get(auListPosition));
        }
    }

    private void buttonNextDOG() {
        if ((dogList != null) && (dogListPosition < dogList.size() - 1)) {
            dogListPosition++;
            DataObjectPackageTreeNode parentNode = mainWindow.treePane.focusDataObjectGroup(dogList.get(dogListPosition));
            auListPosition = auList.indexOf(parentNode.getArchiveUnit());
            lineResultLabel.setText("rang " + (dogListPosition + 1) + "/" + dogList.size() + " DOG et "
                    + (auListPosition + 1) + "/" + auList.size() + " AU sur la ligne");
        }
    }

    private void buttonPreviousDOG() {
        if ((dogList != null) && (dogListPosition > 0)) {
            dogListPosition--;
            DataObjectPackageTreeNode parentNode = mainWindow.treePane.focusDataObjectGroup(dogList.get(dogListPosition));
            auListPosition = auList.indexOf(parentNode.getArchiveUnit());
            lineResultLabel.setText("rang " + (dogListPosition + 1) + "/" + dogList.size() + " DOG et "
                    + (auListPosition + 1) + "/" + auList.size() + " AU sur la ligne");
        }
    }

    /**
     * Sets duplicates result from the duplicates thread.
     *
     * @param dogByDigestMap the dog by digest map
     * @param auByDigestMap  the au by digest map
     */
    public void setDuplicatesResult(LinkedHashMap<String, List<DataObjectGroup>> dogByDigestMap,
                                    HashMap<String, List<ArchiveUnit>> auByDigestMap) {
        ((DuplicatesTableModel) duplicatesTable.getModel()).setData(dogByDigestMap, auByDigestMap);

        DuplicatesTableModel dtm = ((DuplicatesTableModel) (duplicatesTable.getModel()));
        dtm.setData(dogByDigestMap, auByDigestMap);
        dtm.fireTableDataChanged();
        duplicatesTable.getRowSorter().toggleSortOrder(1);
        duplicatesTable.getRowSorter().toggleSortOrder(1);
        duplicatesTable.getColumnModel().getColumn(0)
                .setPreferredWidth(20);
        duplicatesTable.getColumnModel().getColumn(1)
                .setPreferredWidth(20);
        if (auByDigestMap.size() == 0)
            globalResultLabel.setText("0 lots de DOG doublons/0 AU");
        else
            globalResultLabel.setText("" + dogByDigestMap.size() + " lots de DOG doublons/" +
                    auByDigestMap.entrySet().stream().map(arg -> arg.getValue().size()).reduce(Integer::sum).get() + " AU");
        if (dogByDigestMap.size() > 0)
            allDedupButton.setEnabled(true);
        dogList = null;
        auList = null;
        dogListPosition = 0;
        auListPosition = 0;
        duplicatesThread = null;
    }

    /**
     * Sets blank duplicates result. To be used when duplicates thread is cancelled.
     */
    public void setBlankDuplicatesResult() {
        globalResultLabel.setText("Recherche de doublons abandonnée");
        lineResultLabel.setText("Aucun lot sélectionné");
        dogList = null;
        auList = null;
        dogListPosition = 0;
        auListPosition = 0;
        duplicatesThread = null;
    }

    /**
     * Empty dialog. To be used when the context is changed.
     */
    public void emptyDialog() {
        DuplicatesTableModel dtm = ((DuplicatesTableModel) (duplicatesTable.getModel()));
        dtm.setData(null, null);
        dtm.fireTableDataChanged();
        globalResultLabel.setText("Aucune recherche effectuée");
        lineResultLabel.setText("Aucun lot sélectionné");
        lineDedupButton.setEnabled(false);
        allDedupButton.setEnabled(false);
        dogList = null;
        auList = null;
        dogListPosition = 0;
        auListPosition = 0;
    }
}
