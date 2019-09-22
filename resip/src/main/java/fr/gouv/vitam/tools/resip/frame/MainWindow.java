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

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.*;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeViewer;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackageIdElement;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

import static fr.gouv.vitam.tools.resip.app.ResipGraphicApp.OK_DIALOG;

/**
 * The Class MainWindow.
 * <p>
 * Class for the main window
 */
public class MainWindow extends JFrame {

    /**
     * The constant LABEL_FONT.
     */
    public static Font LABEL_FONT = UIManager.getFont("Label.font");
    /**
     * The constant CLICK_FONT.
     */
    public static Font CLICK_FONT = UIManager.getFont("Button.font");
    /**
     * The constant BOLD_LABEL_FONT.
     */
    public static Font ITALIC_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getStyle() | Font.ITALIC);
    /**
     * The constant BOLD_LABEL_FONT.
     */
    public static Font BOLD_LABEL_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getStyle() | Font.BOLD);
    /**
     * The constant TREE_FONT.
     */
    public static Font TREE_FONT = LABEL_FONT;
    /**
     * The constant DETAILS_FONT.
     */
    public static Font DETAILS_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getSize() + (float) 2.0);
    /**
     * The constant DETAILS_FONT.
     */
    public static Font MINI_DETAILS_FONT = LABEL_FONT.deriveFont(LABEL_FONT.getSize() - (float) 2.0);
    /**
     * The constant GENERAL_BACKGROUND.
     */
    public static Color GENERAL_BACKGROUND = UIManager.getColor("Label.background");

    /**
     * The app.
     */
    private ResipGraphicApp app;

    /**
     * The actions components.
     */
    public JSplitPane itemPane;
    public ArchiveUnitEditorPanel auMetadataPane;
    public DataObjectGroupEditorPanel dogMetadataPane;
    public TreeDataObjectPackageEditorPanel treePane;

    /**
     * The entry point of window test.
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
    }

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
        JScrollPane dataObjectDetailScrollPane;

        JMenuBar menuBar = app.createMenu();
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

        treePane= new TreeDataObjectPackageEditorPanel();
        generalSplitPaneHoriz.setLeftComponent(treePane);

        itemPane = new JSplitPane();
        itemPane.setResizeWeight(0.7);
        itemPane.setPreferredSize(new Dimension(600, 800));
        itemPane.setMinimumSize(new Dimension(200, 200));
        generalSplitPaneHoriz.setRightComponent(itemPane);
        itemPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        if (ResipGraphicApp.getTheApp().interfaceParameters.isStructuredMetadataEditionFlag()) {
            StructuredArchiveUnitEditorPanel structuredArchiveUnitEditorPanel = new StructuredArchiveUnitEditorPanel();
            itemPane.setTopComponent(structuredArchiveUnitEditorPanel);
            auMetadataPane = structuredArchiveUnitEditorPanel;
        } else {
            XMLArchiveUnitEditorPanel xmlArchiveUnitEditorPanel = new XMLArchiveUnitEditorPanel();
            itemPane.setTopComponent(xmlArchiveUnitEditorPanel);
            auMetadataPane = xmlArchiveUnitEditorPanel;
        }

        if (ResipGraphicApp.getTheApp().interfaceParameters.isStructuredMetadataEditionFlag()) {
            StructuredDataObjectGroupEditorPanel structuredDataObjectGroupEditorPanel = new StructuredDataObjectGroupEditorPanel();
            itemPane.setBottomComponent(structuredDataObjectGroupEditorPanel);
            dogMetadataPane = structuredDataObjectGroupEditorPanel;
        }
        else
        {
            XMLDataObjectGroupEditorPanel xmlDataObjectGroupEditorPanel = new XMLDataObjectGroupEditorPanel();
            itemPane.setBottomComponent(xmlDataObjectGroupEditorPanel);
            dogMetadataPane = xmlDataObjectGroupEditorPanel;
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ResipGraphicApp.getTheApp().exit();
            }
        });

        pack();
    }

    /**
     * Load.
     */
    public void load() {
        try {
            treePane.editDataObjectPackage(app.currentWork == null ? null : app.currentWork.getDataObjectPackage());
            auMetadataPane.editArchiveUnit(null);
        } catch (SEDALibException ignored) {
        }
    }
}
