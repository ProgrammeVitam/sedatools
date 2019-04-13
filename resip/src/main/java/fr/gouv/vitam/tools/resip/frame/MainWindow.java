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
 * circulated by CEA, CNRS and INRIA dataObjectPackage the following URL "http://www.cecill.info".
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

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.data.AddMetadataItem;
import fr.gouv.vitam.tools.resip.parameters.Prefs;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeViewer;
import fr.gouv.vitam.tools.resip.viewer.DataObjectListCellRenderer;
import fr.gouv.vitam.tools.resip.viewer.DataObjectListViewer;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackageIdElement;
import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectGroup;
import fr.gouv.vitam.tools.sedalib.core.PhysicalDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.ArchiveUnitProfile;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.management.Management;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.sedalib.xml.IndentXMLTool;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * The Class MainWindow.
 * <p>
 * Class for the main window
 */
public class MainWindow extends JFrame {

    public static Font LABEL_FONT=UIManager.getFont("Label.font");
    public static Font CLICK_FONT=UIManager.getFont("Button.font");
    public static Font BOLD_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getStyle() | Font.BOLD);
    public static Font TREE_FONT=LABEL_FONT;
    public static Font DETAILS_FONT=LABEL_FONT.deriveFont(LABEL_FONT.getSize()+(float)2.0);
    public static Color GENERAL_BACKGROUND=UIManager.getColor("Label.background");

    /**
     * The app.
     */
    private ResipGraphicApp app;

    /**
     * The data.
     */
    public DataObjectPackageTreeNode dataObjectPackageTreeItemDisplayed;
    private DataObject dataObjectListItemDisplayed;

    /**
     * The actions components.
     */
    private JPanel dataObjectPackageTreePane;
    private JCheckBox longDataObjectPackageTreeItemNameCheckBox;
    private JPanel auMetadataPane;
    private JTextPane auMetadataText;
    private JLabel auMetadataPaneLabel;
    private JButton openSipItemButton;
    private JButton openObjectButton;
    private JTextPane dataObjectDetailText;
    private DataObjectListViewer dataObjectListViewer;
    private JLabel dataObjectPackageTreePaneLabel;
    private DataObjectPackageTreeModel dataObjectPackageTreePaneModel;
    private DataObjectPackageTreeViewer dataObjectPackageTreePaneViewer;
    private JButton editArchiveUnitButton;
    private JButton editObjectButton;
    private JButton changeObjectButton;
    private JComboBox<String> metadataComboBox;
    private JButton addMetadataButton;
    private JScrollPane dataObjectDetailScrollPane;

    /**
     * Gets the app.
     *
     * @return the app
     */
    public ResipGraphicApp getApp() {
        return app;
    }

    /**
     * Create the application.
     *
     * @param app the app
     */
    public MainWindow(ResipGraphicApp app) {
        super();
        this.app = app;
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        JMenuBar menuBar=app.createMenu();
        setJMenuBar(menuBar);

        java.net.URL imageURL = getClass().getClassLoader().getResource("VitamIcon96.png");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            setIconImage(icon.getImage());
        }
        this.setTitle(ResipGraphicApp.getAppName());

        getContentPane().setPreferredSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

        JSplitPane generalSplitPaneHoriz = new JSplitPane();
        generalSplitPaneHoriz.setResizeWeight(0.3);
        getContentPane().add(generalSplitPaneHoriz);

        dataObjectPackageTreePane = new JPanel();
        GridBagLayout gbl_dataObjectPackageTreePane = new GridBagLayout();
        gbl_dataObjectPackageTreePane.rowWeights = new double[]{0.0, 1.0, 0.0};
        gbl_dataObjectPackageTreePane.columnWeights = new double[]{1.0, 1.0};
        dataObjectPackageTreePane.setLayout(gbl_dataObjectPackageTreePane);
        dataObjectPackageTreePane.setPreferredSize(new Dimension(400, 800));
        dataObjectPackageTreePane.setMinimumSize(new Dimension(100, 200));
        generalSplitPaneHoriz.setLeftComponent(getDataObjectPackageTreePane());

        dataObjectPackageTreePaneLabel = new JLabel("Arbre du SIP");
        dataObjectPackageTreePaneLabel.setFont(BOLD_LABEL_FONT);
        dataObjectPackageTreePaneLabel.setMinimumSize(new Dimension(300, 15));
        GridBagConstraints gbc_dataObjectPackageTreePaneLabel = new GridBagConstraints();
        gbc_dataObjectPackageTreePaneLabel.anchor = GridBagConstraints.WEST;
        gbc_dataObjectPackageTreePaneLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_dataObjectPackageTreePaneLabel.insets = new Insets(5, 5, 5, 0);
        gbc_dataObjectPackageTreePaneLabel.gridwidth = 2;
        gbc_dataObjectPackageTreePaneLabel.gridx = 0;
        gbc_dataObjectPackageTreePaneLabel.gridy = 0;
        getDataObjectPackageTreePane().add(dataObjectPackageTreePaneLabel, gbc_dataObjectPackageTreePaneLabel);

        dataObjectPackageTreePaneModel = new DataObjectPackageTreeModel(null);
        dataObjectPackageTreePaneViewer = new DataObjectPackageTreeViewer(this, dataObjectPackageTreePaneModel);
        dataObjectPackageTreePaneViewer.setFont(TREE_FONT);
        JScrollPane dataObjectPackageTreePaneViewerScrollPane = new JScrollPane(getDataObjectPackageTreePaneViewer());
        GridBagConstraints gbc_dataObjectPackageTreePaneViewerScrollPane = new GridBagConstraints();
        gbc_dataObjectPackageTreePaneViewerScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataObjectPackageTreePaneViewerScrollPane.insets = new Insets(0, 5, 5, 5);
        gbc_dataObjectPackageTreePaneViewerScrollPane.gridwidth = 2;
        gbc_dataObjectPackageTreePaneViewerScrollPane.gridx = 0;
        gbc_dataObjectPackageTreePaneViewerScrollPane.gridy = 1;
        getDataObjectPackageTreePane().add(dataObjectPackageTreePaneViewerScrollPane,
                gbc_dataObjectPackageTreePaneViewerScrollPane);

        longDataObjectPackageTreeItemNameCheckBox = new JCheckBox("+ (direct subAU/total subAU) - xmlID");
        longDataObjectPackageTreeItemNameCheckBox.setFont(CLICK_FONT);
        longDataObjectPackageTreeItemNameCheckBox.setVerticalAlignment(SwingConstants.TOP);
        longDataObjectPackageTreeItemNameCheckBox.addActionListener(e -> checkBoxLongDataObjectPackageTreeItemName());
        GridBagConstraints gbc_longDataObjectPackageTreeItemNameCheckBox = new GridBagConstraints();
        gbc_longDataObjectPackageTreeItemNameCheckBox.gridwidth = 2;
        gbc_longDataObjectPackageTreeItemNameCheckBox.insets = new Insets(0, 5, 5, 0);
        gbc_longDataObjectPackageTreeItemNameCheckBox.anchor = GridBagConstraints.WEST;
        gbc_longDataObjectPackageTreeItemNameCheckBox.gridx = 0;
        gbc_longDataObjectPackageTreeItemNameCheckBox.gridy = 2;
        getDataObjectPackageTreePane().add(longDataObjectPackageTreeItemNameCheckBox,
                gbc_longDataObjectPackageTreeItemNameCheckBox);

        openSipItemButton = new JButton("Ouvrir dossier AU/OG");
        openSipItemButton.setFont(CLICK_FONT);
        openSipItemButton.setEnabled(false);
        openSipItemButton.addActionListener(e -> buttonOpenDataObjectPackageItemDirectory());
        GridBagConstraints gbc_OpenSipItemButton = new GridBagConstraints();
        gbc_OpenSipItemButton.gridwidth = 2;
        gbc_OpenSipItemButton.insets = new Insets(0, 5, 5, 5);
        gbc_OpenSipItemButton.anchor = GridBagConstraints.CENTER;
        gbc_OpenSipItemButton.gridx = 0;
        gbc_OpenSipItemButton.gridy = 3;
        getDataObjectPackageTreePane().add(openSipItemButton, gbc_OpenSipItemButton);

        JSplitPane ItemPane = new JSplitPane();
        ItemPane.setResizeWeight(0.5);
        ItemPane.setPreferredSize(new Dimension(400, 800));
        ItemPane.setMinimumSize(new Dimension(100, 200));
        generalSplitPaneHoriz.setRightComponent(ItemPane);
        ItemPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        auMetadataPane = new JPanel();
        ItemPane.setTopComponent(auMetadataPane);
        GridBagLayout gbl_auMetadataPane = new GridBagLayout();
        gbl_auMetadataPane.rowHeights = new int[]{15, 196, 30};
        gbl_auMetadataPane.columnWeights = new double[]{0.5, 1.0, 0.0};
        gbl_auMetadataPane.rowWeights = new double[]{0.0, 1.0, 0.0};
        auMetadataPane.setLayout(gbl_auMetadataPane);

        JScrollPane auMetadataPaneScrollPane = new JScrollPane();
        GridBagConstraints gbc_auMetadataPaneScrollPane = new GridBagConstraints();
        gbc_auMetadataPaneScrollPane.gridwidth = 3;
        gbc_auMetadataPaneScrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_auMetadataPaneScrollPane.gridy = 1;
        gbc_auMetadataPaneScrollPane.gridx = 0;
        gbc_auMetadataPaneScrollPane.fill = GridBagConstraints.BOTH;
        auMetadataPane.add(auMetadataPaneScrollPane, gbc_auMetadataPaneScrollPane);
        auMetadataText=new JTextPane();
        auMetadataText.setFont(DETAILS_FONT);
        changeLineSpacing(auMetadataText, 0.10);
        auMetadataText.setEditable(false);
        auMetadataPaneScrollPane.setViewportView(auMetadataText);
        auMetadataPaneLabel = new JLabel("AU Metadata");
        auMetadataPaneLabel.setFont(BOLD_LABEL_FONT);
        GridBagConstraints gbc_auMetadataPaneLabel = new GridBagConstraints();
        gbc_auMetadataPaneLabel.gridwidth = 2;
        gbc_auMetadataPaneLabel.insets = new Insets(5, 5, 5, 0);
        gbc_auMetadataPaneLabel.anchor = GridBagConstraints.WEST;
        gbc_auMetadataPaneLabel.gridx = 0;
        gbc_auMetadataPaneLabel.gridy = 0;
        gbl_auMetadataPane.columnWeights = new double[]{0.5, 0.5};
        auMetadataPane.add(auMetadataPaneLabel, gbc_auMetadataPaneLabel);

        editArchiveUnitButton = new JButton("Editer l'ArchiveUnit");
        editArchiveUnitButton.setFont(CLICK_FONT);
        editArchiveUnitButton.setEnabled(false);
        editArchiveUnitButton.addActionListener(e -> buttonEditArchiveUnit());
        GridBagConstraints gbc_editArchiveUnitButton = new GridBagConstraints();
        gbc_editArchiveUnitButton.weightx = 1.0;
        gbc_editArchiveUnitButton.insets = new Insets(0, 0, 5, 5);
        gbc_editArchiveUnitButton.gridx = 0;
        gbc_editArchiveUnitButton.gridy = 2;
        auMetadataPane.add(editArchiveUnitButton, gbc_editArchiveUnitButton);

        JPanel ogInformationPane = new JPanel();
        ItemPane.setBottomComponent(ogInformationPane);
        GridBagLayout gbl_ogInformationPane = new GridBagLayout();
        ogInformationPane.setLayout(gbl_ogInformationPane);


        metadataComboBox = new JComboBox<String>(AddMetadataItem.getAddContentMetadataArray());
        metadataComboBox.setFont(CLICK_FONT);
        metadataComboBox.setEnabled(false);
        GridBagConstraints gbc_metadataComboBox = new GridBagConstraints();
        gbc_metadataComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_metadataComboBox.weightx = 1.0;
        gbc_metadataComboBox.insets = new Insets(0, 0, 5, 5);
        gbc_metadataComboBox.gridx = 2;
        gbc_metadataComboBox.gridy = 2;
        auMetadataPane.add(metadataComboBox, gbc_metadataComboBox);

        addMetadataButton = new JButton("Ajouter...");
        addMetadataButton.setFont(CLICK_FONT);
        addMetadataButton.setEnabled(false);
        GridBagConstraints gbc_addMetadataButton = new GridBagConstraints();
        gbc_addMetadataButton.anchor = GridBagConstraints.EAST;
        gbc_addMetadataButton.insets = new Insets(0, 0, 5, 5);
        gbc_addMetadataButton.gridx = 1;
        gbc_addMetadataButton.gridy = 2;
        auMetadataPane.add(addMetadataButton, gbc_addMetadataButton);
        addMetadataButton.addActionListener(e -> buttonAddMetadata());

        JSplitPane ogInformationSplitPane = new JSplitPane();
        ogInformationSplitPane.setResizeWeight(0.2);
        GridBagConstraints gbc_ogInformationSplitPane = new GridBagConstraints();
        gbc_ogInformationSplitPane.insets = new Insets(5, 5, 0, 0);
        gbc_ogInformationSplitPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_ogInformationSplitPane.gridx = 0;
        gbc_ogInformationSplitPane.gridwidth = 2;
        gbc_ogInformationSplitPane.gridy = 0;
        gbc_ogInformationSplitPane.weightx = 1.0;
        gbc_ogInformationSplitPane.weighty = 1.0;
        gbc_ogInformationSplitPane.fill = GridBagConstraints.BOTH;
        ogInformationPane.add(ogInformationSplitPane, gbc_ogInformationSplitPane);

        JPanel binaryObjectsPane = new JPanel();
        GridBagLayout gbl_binaryObjectsPane = new GridBagLayout();
        binaryObjectsPane.setLayout(gbl_binaryObjectsPane);

        JLabel binaryObjectsPaneLabel = new JLabel("Objets");
        binaryObjectsPaneLabel.setFont(BOLD_LABEL_FONT);
        GridBagConstraints gbc_binaryObjectsPaneLabel = new GridBagConstraints();
        gbc_binaryObjectsPaneLabel.insets = new Insets(5, 5, 5, 0);
        gbc_binaryObjectsPaneLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_binaryObjectsPaneLabel.weightx = 0.5;
        gbc_binaryObjectsPaneLabel.weighty = 0.0;
        gbc_binaryObjectsPaneLabel.gridy = 0;
        gbc_binaryObjectsPaneLabel.gridx = 0;
        binaryObjectsPane.add(binaryObjectsPaneLabel, gbc_binaryObjectsPaneLabel);

        DefaultListModel<DataObject> dataObjectsListModel = new DefaultListModel<DataObject>();
        dataObjectListViewer = new DataObjectListViewer(this, dataObjectsListModel);
        dataObjectListViewer.setFont(DETAILS_FONT);
        dataObjectListViewer.setLayoutOrientation(JList.VERTICAL);
        dataObjectListViewer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DataObjectListCellRenderer dataObjectListCellRenderer = new DataObjectListCellRenderer();
        dataObjectListViewer.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                DataObjectListViewer dolv =(DataObjectListViewer) me.getSource();
                if (me.getClickCount() == 2)
                    buttonOpenObject();
            }
        });
        dataObjectListViewer.setCellRenderer(dataObjectListCellRenderer);

        openObjectButton = new JButton("Ouvrir l'objet");
        openObjectButton.setFont(CLICK_FONT);
        openObjectButton.setMinimumSize(new Dimension(20, 25));
        openObjectButton.setEnabled(false);
        openObjectButton.addActionListener(e -> buttonOpenObject());

        GridBagConstraints gbc_openObjectButton = new GridBagConstraints();
        gbc_openObjectButton.weightx = 1.0;
        gbc_openObjectButton.fill = GridBagConstraints.BOTH;
        gbc_openObjectButton.anchor = GridBagConstraints.LINE_END;
        gbc_openObjectButton.insets = new Insets(5, 5, 5, 5);
        gbc_openObjectButton.gridx = 0;
        gbc_openObjectButton.gridy = 2;
        gbc_openObjectButton.weighty = 0.0;
        binaryObjectsPane.add(openObjectButton, gbc_openObjectButton);

        changeObjectButton = new JButton("Changer l'objet");
        changeObjectButton.setFont(CLICK_FONT);
        changeObjectButton.setMinimumSize(new Dimension(20, 25));
        changeObjectButton.setEnabled(false);
        changeObjectButton.addActionListener(e -> buttonChangeObject());

        GridBagConstraints gbc_changeObjectButton = new GridBagConstraints();
        gbc_changeObjectButton.weightx = 1.0;
        gbc_changeObjectButton.fill = GridBagConstraints.BOTH;
        gbc_changeObjectButton.anchor = GridBagConstraints.LINE_END;
        gbc_changeObjectButton.insets = new Insets(5, 5, 5, 5);
        gbc_changeObjectButton.gridx = 0;
        gbc_changeObjectButton.gridy = 3;
        gbc_changeObjectButton.weighty = 0.0;
        binaryObjectsPane.add(changeObjectButton, gbc_changeObjectButton);

        ogInformationSplitPane.setLeftComponent(binaryObjectsPane);

        JScrollPane binaryObjectsListViewerScrollPane = new JScrollPane(dataObjectListViewer);
        GridBagConstraints gbc_binaryObjectsListViewerScrollPane = new GridBagConstraints();
        gbc_binaryObjectsListViewerScrollPane.insets = new Insets(0, 5, 0, 0);
        gbc_binaryObjectsListViewerScrollPane.weightx = 1.0;
        gbc_binaryObjectsListViewerScrollPane.weighty = 1.0;
        gbc_binaryObjectsListViewerScrollPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_binaryObjectsListViewerScrollPane.fill = GridBagConstraints.BOTH;
        gbc_binaryObjectsListViewerScrollPane.gridy = 1;
        gbc_binaryObjectsListViewerScrollPane.gridx = 0;
        binaryObjectsPane.add(binaryObjectsListViewerScrollPane, gbc_binaryObjectsListViewerScrollPane);

        JPanel ogBinaryObjectDetailPane = new JPanel();
        GridBagLayout gbl_ogInformationSplitPane = new GridBagLayout();
        gbl_ogInformationSplitPane.rowWeights = new double[]{0.0, 0.0, 0.0};
        gbl_ogInformationSplitPane.columnWeights = new double[]{1.0};
        ogBinaryObjectDetailPane.setLayout(gbl_ogInformationSplitPane);

        JLabel ogBinaryObjectDetailPaneLabel = new JLabel("Détails");
        ogBinaryObjectDetailPaneLabel.setFont(BOLD_LABEL_FONT);
        GridBagConstraints gbc_ogBinaryObjectDetailPaneLabel = new GridBagConstraints();
        gbc_ogBinaryObjectDetailPaneLabel.insets = new Insets(5, 5, 5, 0);
        gbc_ogBinaryObjectDetailPaneLabel.anchor = GridBagConstraints.NORTHWEST;
        gbc_ogBinaryObjectDetailPaneLabel.fill = GridBagConstraints.HORIZONTAL;
        gbc_ogBinaryObjectDetailPaneLabel.gridy = 0;
        gbc_ogBinaryObjectDetailPaneLabel.gridx = 0;
        ogBinaryObjectDetailPane.add(ogBinaryObjectDetailPaneLabel, gbc_ogBinaryObjectDetailPaneLabel);

        editObjectButton = new JButton("Editer le DataObject");
        editObjectButton.setFont(CLICK_FONT);
        editObjectButton.setEnabled(false);
        editObjectButton.addActionListener(e -> buttonEditDataObject());
        GridBagConstraints gbc_editObjectButton = new GridBagConstraints();
        gbc_editObjectButton.insets = new Insets(5, 5, 5, 0);
        gbc_editObjectButton.gridwidth = 1;
        gbc_editObjectButton.gridx = 0;
        gbc_editObjectButton.gridy = 2;
        ogBinaryObjectDetailPane.add(editObjectButton, gbc_editObjectButton);

        ogInformationSplitPane.setRightComponent(ogBinaryObjectDetailPane);

        dataObjectDetailScrollPane = new JScrollPane();
        GridBagConstraints gbc_dataObjectDetailScrollPane = new GridBagConstraints();
        gbc_dataObjectDetailScrollPane.insets = new Insets(0, 5, 5, 0);
        gbc_dataObjectDetailScrollPane.fill = GridBagConstraints.BOTH;
        gbc_dataObjectDetailScrollPane.weightx = 0.7;
        gbc_dataObjectDetailScrollPane.weighty = 1.0;
        gbc_dataObjectDetailScrollPane.anchor = GridBagConstraints.NORTHWEST;
        gbc_dataObjectDetailScrollPane.gridy = 1;
        gbc_dataObjectDetailScrollPane.gridx = 0;
        ogBinaryObjectDetailPane.add(dataObjectDetailScrollPane, gbc_dataObjectDetailScrollPane);

        dataObjectDetailText = new JTextPane();
        changeLineSpacing(dataObjectDetailText,0.1);
        dataObjectDetailText.setFont(DETAILS_FONT);
        dataObjectDetailScrollPane.setViewportView(dataObjectDetailText);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ResipGraphicApp.getTheApp().exit();
            }
        });

        pack();
    }

    // long name checkbox

    /**
     * Sets the long archive transfer tree item name.
     *
     * @param fileName the new long archive transfer tree item name
     */
    void setLongDataObjectPackageTreeItemName(boolean fileName) {
        getDataObjectPackageTreePaneViewer().activateLongDataObjectPackageTreeItemName(fileName);
        getDataObjectPackageTreePane().repaint();
    }

    /**
     * All child nodes changed.
     *
     * @param attn the attn
     */
    private void allChildNodesChanged(DataObjectPackageTreeNode attn) {
        int[] allChilds = new int[attn.getChildCount()];
        for (int i = 0; i < attn.getChildCount(); i++) {
            allChilds[i] = i;
            allChildNodesChanged((DataObjectPackageTreeNode) attn.getChildAt(i));
        }
        dataObjectPackageTreePaneModel.nodesChanged(attn, allChilds);
    }

    /**
     * All tree changed.
     */
    public void allTreeChanged() {
        DataObjectPackageTreeNode root = (DataObjectPackageTreeNode) dataObjectPackageTreePaneModel.getRoot();
        if (root != null)
            allChildNodesChanged(root);
    }

    /**
     * Check box long archive transfer tree item name.
     */
    public void checkBoxLongDataObjectPackageTreeItemName() {
        boolean selected = longDataObjectPackageTreeItemNameCheckBox.isSelected();
        setLongDataObjectPackageTreeItemName(selected);
        allTreeChanged();
    }

    // open tree item directory

    /**
     * Button open archive transfer item directory.
     */
    private void buttonOpenDataObjectPackageItemDirectory() {
        Path path = null;
        try {
            DataObjectPackageTreeNode stn = getDataObjectPackageTreeItemDisplayed();
            if (stn != null) {
                if (stn.getArchiveUnit() != null) {
                    // is ArchiveUnit
                    path = stn.getArchiveUnit().getOnDiskPath();
                } else if (stn.getDataObject() != null) {
                    // is DataObject
                    path = ((DataObjectPackageIdElement) stn.getDataObject()).getOnDiskPath();
                }
            }
            if (path != null)
                Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            // too bad
        }
    }

    // open list item object

    /**
     * Button open object.
     */
    private void buttonOpenObject() {
        Path path;
        try {
            DataObject dataObject = getDataObjectListItemDisplayed();
            if ((dataObject instanceof BinaryDataObject)) {
                path = ((BinaryDataObject) dataObject).getOnDiskPath();
                if (path != null)
                    Desktop.getDesktop().open(path.toFile());
            }
        } catch (IOException e) {
            // too bad
        }
    }

    /**
     * Utility function to choose a file.
     */
    private String chooseNewObject() {
        File tmp = null;
        try {
            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                tmp = fileChooser.getSelectedFile();
                if (!tmp.exists() || !tmp.isFile()) {
                    throw new ResipException("Le nom choisi ne correspond pas à un fichier existant");
                }
                return tmp.toString();
            } else
                return null;
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(this,
                    "Le fichier choisi "+ (tmp != null ? "[" + tmp.getAbsolutePath() + "]" : "") + " ne peut être pris en compte",
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,"Erreur fatale, impossible de choisir le fichier "
                    + (tmp != null ? "[" + tmp.getAbsolutePath() + "]" : "") + "\n->" + e.getMessage());
            return null;
        }
    }

    /**
     * Button change object.
     */
    private void buttonChangeObject() {
        String newBinary = chooseNewObject();
        if (newBinary != null) {
            DataObject dataObject = getDataObjectListItemDisplayed();
            if ((dataObject instanceof BinaryDataObject)) {
                BinaryDataObject bdo = (BinaryDataObject) dataObject;
                bdo.setOnDiskPath(Paths.get(newBinary));
                // FIXME
                try {
                    bdo.extractTechnicalElements(null);
                } catch (SEDALibException e) {
                    UserInteractionDialog.getUserAnswer(this,
                            "Les informations techniques du fichier choisi [" + newBinary
                                    + "] n'ont pas pu être toutes extraites, la mise à jour est partielle.",
                            "Erreur", UserInteractionDialog.ERROR_DIALOG,
                            null);
                    ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,"Les informations techniques du fichier choisi [" + newBinary
                                    + "] n'ont pas pu être toutes extraites, la mise à jour est partielle.\n->"
                                    + e.getMessage());
                }
                dataObjectListItemClick(dataObject);
                ResipGraphicApp.getTheApp().setModifiedContext(true);
            }
        }
    }

    // edit AU

    /**
     * Button edit archive unit.
     */
    void buttonEditArchiveUnit() {
        XmlEditDialog xmlEditDialog = new XmlEditDialog(this, dataObjectPackageTreeItemDisplayed);
        xmlEditDialog.setVisible(true);
        if (xmlEditDialog.getReturnValue()) {
            ((DataObjectPackageTreeModel) dataObjectPackageTreePaneViewer.getModel())
                    .nodeChanged(dataObjectPackageTreeItemDisplayed);
            getArchiveUnitMetadataText().setText(xmlEditDialog.getResult());
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        }
    }

    AddMetadataItem getAddMetadataItem(String definition) throws SEDALibException {
        AddMetadataItem result;
        String macroMetadata = definition.substring(0, 3);
        String elementName = definition.substring(3, definition.indexOf(' '));
        if (macroMetadata.equals("[A]")) {
            result = new AddMetadataItem(ArchiveUnitProfile.class, "ArchiveUnitProfile");
        } else {
            HashMap<String, ComplexListMetadataKind> metadataMap;
            if (macroMetadata.equals("[C]")) {
                Content c = new Content();
                metadataMap = c.getMetadataMap();
            } else {
                Management m = new Management();
                metadataMap = m.getMetadataMap();
            }
            result = new AddMetadataItem(metadataMap.get(elementName).metadataClass, elementName);
        }
        return result;
    }

    /**
     * Button add metadata.
     */
    void buttonAddMetadata() {
        try{
            AddMetadataItem ami=getAddMetadataItem((String) metadataComboBox.getSelectedItem());
            XmlEditDialog xmlEditDialog = new XmlEditDialog(this, ami);
            xmlEditDialog.setVisible(true);
            if (xmlEditDialog.getReturnValue()) {
                //noinspection ConstantConditions
                String macroMetadata = ((String) metadataComboBox.getSelectedItem()).substring(0, 3);
                switch(macroMetadata) {
                    case "[A]":
                        dataObjectPackageTreeItemDisplayed.getArchiveUnit().setArchiveUnitProfile((ArchiveUnitProfile) ami.skeleton);
                        break;
                    case "[C]":
                        Content c=dataObjectPackageTreeItemDisplayed.getArchiveUnit().getContent();
                        c.addMetadata(ami.skeleton);
                        break;
                    case "[M]":
                        Management m=dataObjectPackageTreeItemDisplayed.getArchiveUnit().getManagement();
                        if (m==null){
                            m=new Management();
                            dataObjectPackageTreeItemDisplayed.getArchiveUnit().setManagement(m);
                        }
                        m.addMetadata(ami.skeleton);
                        break;
                }
                ((DataObjectPackageTreeModel) dataObjectPackageTreePaneViewer.getModel())
                        .nodeChanged(dataObjectPackageTreeItemDisplayed);
                getArchiveUnitMetadataText().setText(IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT)
                        .indentString(dataObjectPackageTreeItemDisplayed.getArchiveUnit().toSedaXmlFragments()));
                ResipGraphicApp.getTheApp().setModifiedContext(true);
            }
        } catch (SEDALibException e) {
            UserInteractionDialog.getUserAnswer(this,
                    "L'édition des métadonnées de l'ArchiveUnit n'a pas été possible.\n->"
                            + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,"L'édition des métadonnées de l'ArchiveUnit n'a pas été possible.\n->"
                            + e.getMessage());
        }
    }
    // edit DataObject

    /**
     * Button edit data object.
     */
    void buttonEditDataObject() {
        DataObject dataObject = getDataObjectListItemDisplayed();
        XmlEditDialog xmlEditDialog = new XmlEditDialog(this, dataObject);
        xmlEditDialog.setVisible(true);
        if (xmlEditDialog.getReturnValue()) {
            ((DefaultListModel<DataObject>) dataObjectListViewer.getModel()).set(0,
                    ((DefaultListModel<DataObject>) dataObjectListViewer.getModel()).get(0));
            dataObjectListItemClick(dataObject);
            ResipGraphicApp.getTheApp().setModifiedContext(true);
        }
    }

    // click on tree

    /**
     * Archive transfer tree item click.
     *
     * @param path the path
     */
    public void dataObjectPackageTreeItemClick(TreePath path) {
        DataObjectPackageTreeNode node = (DataObjectPackageTreeNode) path.getLastPathComponent();
        if (node.getArchiveUnit() == null)
            node = (DataObjectPackageTreeNode) path.getParentPath().getLastPathComponent();
        String tmp = null;
        try {
            tmp = node.getArchiveUnit().toSedaXmlFragments();
            tmp = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT).indentString(tmp);
        } catch (Exception e) {
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP,"Resip.InOut: Erreur à l'indentation de l'ArchiveUnit ["
                    + node.getArchiveUnit().getInDataObjectPackageId() + "]");
        }
        getArchiveUnitMetadataText().setText(tmp);
        getArchiveUnitMetadataText().setCaretPosition(0);
        auMetadataPaneLabel.setText(
                "Métadonnées AU: " + node.getArchiveUnit().getInDataObjectPackageId() + " - " + node.getTitle());
        auMetadataPane.repaint();
        dataObjectPackageTreeItemDisplayed = node;

        DefaultListModel<DataObject> model = (DefaultListModel<DataObject>) dataObjectListViewer.getModel();
        model.removeAllElements();
        for (int i = 0; i < node.getArchiveUnit().getDataObjectRefList().getCount(); i++) {
            DataObject dataObject = node.getArchiveUnit().getDataObjectRefList().getDataObjectList().get(i);
            if (dataObject instanceof DataObjectGroup) {
                DataObjectGroup dog = (DataObjectGroup) dataObject;
                for (BinaryDataObject bdo : dog.getBinaryDataObjectList()) {
                    model.addElement(bdo);
                }
                for (PhysicalDataObject pdo : dog.getPhysicalDataObjectList()) {
                    model.addElement(pdo);
                }
            } else
                model.addElement(dataObject);
        }
        dataObjectDetailText.setText("");
        openObjectButton.setEnabled(false);
        changeObjectButton.setEnabled(false);
        setDataObjectListItemDisplayed(null);

        if (node.getArchiveUnit().getOnDiskPath() != null)
            openSipItemButton.setEnabled(true);
        else
            openSipItemButton.setEnabled(false);

        editArchiveUnitButton.setEnabled(true);
        metadataComboBox.setEnabled(true);
        addMetadataButton.setEnabled(true);

        editObjectButton.setEnabled(false);
    }

    /**
     * Data object list item click.
     *
     * @param dataObject the data object
     */
    public void dataObjectListItemClick(DataObject dataObject) {
        if (dataObject == null) {
            dataObjectDetailText.setText("");
            openObjectButton.setEnabled(false);
            changeObjectButton.setEnabled(false);
            setDataObjectListItemDisplayed(null);
            editObjectButton.setEnabled(false);
        } else if (dataObject instanceof BinaryDataObject) {
            BinaryDataObject bo = (BinaryDataObject) dataObject;
            dataObjectDetailText.setText(bo.toString());
            dataObjectDetailText.setCaretPosition(0);
            openObjectButton.setEnabled(true);
            changeObjectButton.setEnabled(true);
            editObjectButton.setEnabled(true);
            setDataObjectListItemDisplayed(bo);
        } else if (dataObject instanceof PhysicalDataObject) {
            PhysicalDataObject pdo = (PhysicalDataObject) dataObject;
            String tmp = "";
            try {
                tmp = pdo.toSedaXmlFragments();
                tmp = IndentXMLTool.getInstance(IndentXMLTool.STANDARD_INDENT).indentString(tmp);
            } catch (Exception e) {
                ResipLogger.getGlobalLogger().log(ResipLogger.STEP,"Resip.InOut: Erreur à l'indentation du PhysicalDataObject ["
                        + pdo.getInDataObjectPackageId() + "]");
            }
            dataObjectDetailText.setText(tmp);
            dataObjectDetailText.setCaretPosition(0);
            openObjectButton.setEnabled(false);
            changeObjectButton.setEnabled(false);
            editObjectButton.setEnabled(true);
            setDataObjectListItemDisplayed(pdo);
        }
    }

    // load new information and refresh all panes

    /**
     * Load.
     */
    public void load() {
        DefaultTreeModel tmauRG = (DefaultTreeModel) getDataObjectPackageTreePaneViewer().getModel();
        if ((app.currentWork != null) && (app.currentWork.getDataObjectPackage() != null)) {
            DataObjectPackageTreeNode top = ((DataObjectPackageTreeModel) tmauRG)
                    .generateDataObjectPackageNodes(app.currentWork.getDataObjectPackage());
            tmauRG.setRoot(top);
            tmauRG.reload();
            dataObjectPackageTreePaneLabel
                    .setText("Arbre du SIP (" + app.currentWork.getDataObjectPackage().getArchiveUnitCount()
                            + " archiveUnit/" + app.currentWork.getDataObjectPackage().getDataObjectGroupCount()
                            + " dog/" + app.currentWork.getDataObjectPackage().getBinaryDataObjectCount() + " bdo/"
                            + app.currentWork.getDataObjectPackage().getPhysicalDataObjectCount() + " pdo)");
            dataObjectPackageTreeItemDisplayed = null;
            auMetadataPaneLabel.setText("Métadonnées AU");
            auMetadataText.setText("");
            DefaultListModel<DataObject> model = (DefaultListModel<DataObject>) dataObjectListViewer.getModel();
            model.removeAllElements();
            dataObjectListItemDisplayed = null;
            dataObjectDetailText.setText("");
            openObjectButton.setEnabled(false);
            editArchiveUnitButton.setEnabled(false);
            metadataComboBox.setEnabled(false);
            addMetadataButton.setEnabled(false);
            editObjectButton.setEnabled(false);
        } else {
            tmauRG.setRoot(null);
            tmauRG.reload();
            dataObjectPackageTreePaneLabel.setText("Arbre du SIP");
            dataObjectPackageTreeItemDisplayed = null;
            auMetadataPaneLabel.setText("Métadonnées AU");
            auMetadataText.setText("");
            DefaultListModel<DataObject> model = (DefaultListModel<DataObject>) dataObjectListViewer.getModel();
            model.removeAllElements();
            dataObjectListItemDisplayed = null;
            dataObjectDetailText.setText("");
            openObjectButton.setEnabled(false);
            editArchiveUnitButton.setEnabled(false);
            metadataComboBox.setEnabled(false);
            addMetadataButton.setEnabled(false);
            editObjectButton.setEnabled(false);
        }
    }

    /**
     * Update AU metadata.
     *
     * @param node the node
     */
    public void updateAUMetadata(DataObjectPackageTreeNode node) {
        auMetadataPaneLabel.setText(
                "Métadonnées AU: " + node.getArchiveUnit().getInDataObjectPackageId() + " - " + node.getTitle());
        auMetadataPane.repaint();
    }

    /**
     * Refresh informations.
     */
    public void refreshInformations() {
        dataObjectPackageTreePaneLabel
                .setText("Arbre du SIP (" + app.currentWork.getDataObjectPackage().getArchiveUnitCount()
                        + " archiveUnit/" + app.currentWork.getDataObjectPackage().getDataObjectGroupCount()
                        + " dog/" + app.currentWork.getDataObjectPackage().getBinaryDataObjectCount() + " bdo/"
                        + app.currentWork.getDataObjectPackage().getPhysicalDataObjectCount() + " pdo)");
        dataObjectPackageTreeItemDisplayed = null;
        auMetadataPaneLabel.setText("Métadonnées AU");
        auMetadataText.setText("");
        DefaultListModel<DataObject> model = (DefaultListModel<DataObject>) dataObjectListViewer.getModel();
        model.removeAllElements();
        dataObjectListItemDisplayed = null;
        dataObjectDetailText.setText("");
        openObjectButton.setEnabled(false);
        editArchiveUnitButton.setEnabled(false);
        metadataComboBox.setEnabled(false);
        addMetadataButton.setEnabled(false);
        editObjectButton.setEnabled(false);
    }

    /**
     * Refresh TreePaneLabel informations.
     */
    public void refreshTreePaneLabel() {
        dataObjectPackageTreePaneLabel
                .setText("Arbre du SIP (" + app.currentWork.getDataObjectPackage().getArchiveUnitCount()
                        + " archiveUnit/" + app.currentWork.getDataObjectPackage().getDataObjectGroupCount()
                        + " dog/" + app.currentWork.getDataObjectPackage().getBinaryDataObjectCount() + " bdo/"
                        + app.currentWork.getDataObjectPackage().getPhysicalDataObjectCount() + " pdo)");
    }

    /**
     * Gets the archive transfer tree item displayed.
     *
     * @return the archive transfer tree item displayed
     */
    public DataObjectPackageTreeNode getDataObjectPackageTreeItemDisplayed() {
        return dataObjectPackageTreeItemDisplayed;
    }

    /**
     * Gets the data object list item displayed.
     *
     * @return the data object list item displayed
     */
    public DataObject getDataObjectListItemDisplayed() {
        return dataObjectListItemDisplayed;
    }

    /**
     * Sets the data object list item displayed.
     *
     * @param dataObjectListItemDisplayed the new data object list item displayed
     */
    public void setDataObjectListItemDisplayed(DataObject dataObjectListItemDisplayed) {
        this.dataObjectListItemDisplayed = dataObjectListItemDisplayed;
    }

    /**
     * Change line spacing in a JTextPane.
     *
     * @param pane    the pane
     * @param factor  the factor
     */
    private void changeLineSpacing(JTextPane pane, double factor) {
        pane.selectAll();
        MutableAttributeSet set = new SimpleAttributeSet(pane.getParagraphAttributes());
        StyleConstants.setLineSpacing(set, (float)factor);
        pane.setParagraphAttributes(set, true);
    }

    /**
     * Gets the archive unit metadata text.
     *
     * @return the archive unit metadata text
     */
    public JTextPane getArchiveUnitMetadataText() {
        return auMetadataText;
    }


     /**
     * Gets the archive transfer tree pane.
     *
     * @return the archive transfer tree pane
     */
    public JPanel getDataObjectPackageTreePane() {
        return dataObjectPackageTreePane;
    }

    /**
     * Sets the archive transfer tree pane.
     *
     * @param dataObjectPackageTreePane the new archive transfer tree pane
     */
    public void setDataObjectPackageTreePane(JPanel dataObjectPackageTreePane) {
        this.dataObjectPackageTreePane = dataObjectPackageTreePane;
    }

    /**
     * Gets the archive transfer tree pane viewer.
     *
     * @return the archive transfer tree pane viewer
     */
    public DataObjectPackageTreeViewer getDataObjectPackageTreePaneViewer() {
        return dataObjectPackageTreePaneViewer;
    }

    /**
     * Sets the archive transfer tree pane viewer.
     *
     * @param dataObjectPackageTreePaneViewer the new archive transfer tree pane
     *                                        viewer
     */
    public void setDataObjectPackageTreePaneViewer(DataObjectPackageTreeViewer dataObjectPackageTreePaneViewer) {
        this.dataObjectPackageTreePaneViewer = dataObjectPackageTreePaneViewer;
    }

    /**
     * Gets the data object list viewer.
     *
     * @return the data object list viewer
     */
    public DataObjectListViewer getDataObjectListViewer() {
        return dataObjectListViewer;
    }

}
