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
package fr.gouv.vitam.tools.resip.app;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.BackingStoreException;
import java.util.zip.ZipEntry;
import javax.swing.*;
import javax.swing.tree.TreePath;

import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.*;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeModel;
import fr.gouv.vitam.tools.resip.viewer.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.sedalib.core.*;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ResipGraphicApp implements ActionListener, Runnable {

    // Uniq instance. */
    static ResipGraphicApp theApp = null;

    // Data elements. */
    public CreationContext launchCreationContext;
    public Work currentWork;
    public boolean modifiedWork;
    public String filenameWork;

    // GUI elements. */
    public MainWindow mainWindow;

    // MainWindow menu elements dis/enabled depending on work state and used by controller. */
    private JMenuItem saveMenuItem, saveAsMenuItem, closeMenuItem;
    private JMenu treatMenu, contextMenu, exportMenu;
    private Map<JMenuItem, String> actionByMenuItem = new HashMap<JMenuItem, String>();

    // Thread control elements. */
    public boolean importThreadRunning;
    public boolean addThreadRunning;
    public boolean exportThreadRunning;

    public ResipGraphicApp(CreationContext creationContext) throws ResipException {
        if (theApp != null)
            throw new ResipException("L'application a déjà été lancée");
        theApp = this;

        // inner variables
        this.launchCreationContext = creationContext;
        this.currentWork = null;
        this.modifiedWork = false;
        this.filenameWork = null;
        this.importThreadRunning = false;
        this.exportThreadRunning = false;
        this.addThreadRunning = false;

        // prefs init
        Prefs.getInstance();

        // identification objects initialization
        try {
            DroidIdentifier.getInstance();
        } catch (Exception e) {
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de créer les outils Droid");
            System.exit(1);
        }
        EventQueue.invokeLater(this);
    }

    public void run() {
        try {
            mainWindow = new MainWindow(this);
            mainWindow.setVisible(true);
            currentWork = null;

            if ((launchCreationContext instanceof DiskImportContext) ||
                    (launchCreationContext instanceof SIPImportContext))
                importWork(launchCreationContext);
            else
                mainWindow.load();

            StoreExtractor.initDefaultExtractors();
        } catch (Exception e) {
            System.err.println("Resip.Graphic: Erreur fatale, exécution interrompue (" + e.getMessage() + ")");
            System.exit(1);
        }

    }

    @SuppressWarnings("SameReturnValue")
    public static String getAppName() {
        return "Resip";
    }

    // Menu controller

    public JMenuBar createMenu() {
        JMenuBar menuBar;
        JMenu importMenu, fileMenu, infoMenu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        fileMenu = new JMenu("Fichier");
        menuBar.add(fileMenu);

        menuItem = new JMenuItem("Charger...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "LoadWork");
        fileMenu.add(menuItem);

        saveMenuItem = new JMenuItem("Sauver");
        saveMenuItem.addActionListener(this);
        saveMenuItem.setEnabled(false);
        actionByMenuItem.put(saveMenuItem, "SaveWork");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem = new JMenuItem("Sauver sous...");
        saveAsMenuItem.addActionListener(this);
        saveAsMenuItem.setEnabled(false);
        actionByMenuItem.put(saveAsMenuItem, "SaveAsWork");
        fileMenu.add(saveAsMenuItem);

        closeMenuItem = new JMenuItem("Fermer");
        closeMenuItem.addActionListener(this);
        closeMenuItem.setEnabled(false);
        actionByMenuItem.put(closeMenuItem, "CloseWork");
        fileMenu.add(closeMenuItem);

        fileMenu.add(new JSeparator());

        menuItem = new JMenuItem("Préférences...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "EditPrefs");
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Nettoyer le répertoire de travail...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "EmptyWorkDir");
        fileMenu.add(menuItem);

        contextMenu = new JMenu("Contexte");
        menuBar.add(contextMenu);
        contextMenu.setEnabled(false);

        menuItem = new JMenuItem("Voir les informations d'import...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "SeeImportContext");
        contextMenu.add(menuItem);

        menuItem = new JMenuItem("Editer les informations d'export...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "EditExportContext");
        contextMenu.add(menuItem);

        treatMenu = new JMenu("Traiter");
        menuBar.add(treatMenu);
        treatMenu.setEnabled(false);

        menuItem = new JMenuItem("Chercher...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "Search");
        treatMenu.add(menuItem);

        menuItem = new JMenuItem("Trier l'arbre de visualisation");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "SortTreeViewer");
        treatMenu.add(menuItem);

        menuItem = new JMenuItem("Régénérer des ID continus");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "RegenerateContinuousIds");
        treatMenu.add(menuItem);

        importMenu = new JMenu("Import");
        menuBar.add(importMenu);

        menuItem = new JMenuItem("Importer depuis un répertoire...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ImportFromDisk");
        importMenu.add(menuItem);

        menuItem = new JMenuItem("Importer depuis un SIP...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ImportFromSIP");
        importMenu.add(menuItem);

        menuItem = new JMenuItem("Importer depuis un DIP...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ImportFromDIP");
        importMenu.add(menuItem);

        menuItem = new JMenuItem("Importer depuis un csv d'arbre de classement...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ImportFromCSVTree");
        importMenu.add(menuItem);

        menuItem = new JMenuItem("Importer depuis un csv de métadonnées...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ImportFromCSVMetadata");
        importMenu.add(menuItem);

        menuItem = new JMenuItem("Importer depuis un conteneur courriels...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ImportFromMail");
        importMenu.add(menuItem);

        exportMenu = new JMenu("Export");
        exportMenu.setEnabled(false);
        menuBar.add(exportMenu);

        menuItem = new JMenuItem("Exporter le SIP SEDA...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ExportToSEDASIP");
        exportMenu.add(menuItem);

        menuItem = new JMenuItem("Exporter le manifest Xml SEDA...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ExportToSEDAXMLManifest");
        exportMenu.add(menuItem);

        menuItem = new JMenuItem("Exporter la hiérarchie sur disque...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ExportToDisk");
        exportMenu.add(menuItem);

        infoMenu = new JMenu("?");
        menuBar.add(infoMenu);

        menuItem = new JMenuItem("A propos de Resip");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "APropos");
        infoMenu.add(menuItem);

        return menuBar;
    }

    public void actionPerformed(final ActionEvent actionEvent) {
        Object source = actionEvent.getSource();

        if (source instanceof JMenuItem) {
            String action = actionByMenuItem.get(source);
            if (action == null)
                System.err.println("unknown menu action");
            else
                switch (action) {
                    // File Menu
                    case "LoadWork":
                        loadWork();
                        break;
                    case "SaveWork":
                        saveWork();
                        break;
                    case "SaveAsWork":
                        saveAsWork();
                        break;
                    case "CloseWork":
                        closeWork();
                        break;
                    case "EditPrefs":
                        editPrefs();
                        break;
                    case "EmptyWorkDir":
                        emptyWorkDir();
                        break;
                    // Treat Menu
                    case "Search":
                        search();
                        break;
                    case "RegenerateContinuousIds":
                        doRegenerateContinuousIds();
                        break;
                    case "SortTreeViewer":
                        doSortTreeViewer();
                        break;
                    // Context Menu
                    case "SeeImportContext":
                        seeImportContext();
                        break;
                    case "EditExportContext":
                        editExportContext();
                        break;
                    // Import Menu
                    case "ImportFromSIP":
                        importFromSIP();
                        break;
                    case "ImportFromDIP":
                        importFromDIP();
                        break;
                    case "ImportFromDisk":
                        importFromDisk();
                        break;
                    case "ImportFromCSVTree":
                        importFromCSVTree();
                        break;
                    case "ImportFromCSVMetadata":
                        importFromCSVMetadata();
                        break;
                    case "ImportFromMail":
                        importFromMail();
                        break;
                    // Export Menu
                    case "ExportToSEDASIP":
                        exportWork(ExportThread.SIP_EXPORT);
                        break;
                    case "ExportToSEDAXMLManifest":
                        exportWork(ExportThread.MANIFEST_EXPORT);
                        break;
                    case "ExportToDisk":
                        exportWork(ExportThread.DISK_EXPORT);
                        break;
                    case "APropos":
                        aPropos();
                        break;
                }
        }
    }

    // Utils

    public static ResipGraphicApp getTheApp() {
        return theApp;
    }

    public void setModifiedContext(boolean isModified) {
        modifiedWork = isModified;
        saveMenuItem.setEnabled(modifiedWork && (filenameWork != null));
    }

    public void setContextLoaded(boolean isLoaded) {
        treatMenu.setEnabled(isLoaded);
        contextMenu.setEnabled(isLoaded);
        exportMenu.setEnabled(isLoaded);
        saveAsMenuItem.setEnabled(isLoaded);
        closeMenuItem.setEnabled(isLoaded);
    }

    public void setFilenameWork(String fileName) {
        filenameWork = fileName;
        saveMenuItem.setEnabled((filenameWork != null) && modifiedWork);
    }

    // File Menu

    // MenuItem Load

    private void loadWork() {
        String filename = "Non défini";
        try {
            if (importThreadRunning) {
                JOptionPane.showMessageDialog(mainWindow,
                        "Un import est en cours vous devez l'annuler ou attendre la fin avant de faire un chargement.",
                        "Alerte", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if ((currentWork != null) && modifiedWork
                    && (JOptionPane.showConfirmDialog(mainWindow,
                    "Vous avez un contexte en cours non sauvegardé, un chargement l'écrasera.\n"
                            + "Voulez-vous continuer?",
                    "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION))
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsLoadDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fileChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                filename = fileChooser.getSelectedFile().getCanonicalPath();
                currentWork = Work.createFromFile(filename);
                ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL, "Fichier [" + filename + "] chargé");
                mainWindow.load();
                setFilenameWork(filename);
                setContextLoaded(true);
                setModifiedContext(false);
                Prefs.getInstance().setPrefsLoadDirFromChild(filename);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur de chargement de [" + filename + "]\n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Erreur de chargement de [" + filename + "]\n->" + e.getMessage());
        }
    }

    // MenuItem Save

    private void saveWork() {
        if (filenameWork != null)
            try {
                currentWork.save(filenameWork);
                setModifiedContext(false);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainWindow,
                        "Erreur de sauvegarde de [" + filenameWork + "]\n->" + e.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                ResipLogger.getGlobalLogger().log(ResipLogger.STEP,
                        "Resip.Graphic: Erreur de sauvegarde de [" + filenameWork + "]\n->" + e.getMessage());
            }
    }

    // MenuItem SaveAs

    private void saveAsWork() {
        String filename = "Non défini";
        try {
            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsLoadDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                filename = fileChooser.getSelectedFile().getCanonicalPath();
                if (new File(filename).exists()) {
                    int dialogResult = JOptionPane.showConfirmDialog(mainWindow,
                            "Le fichier [" + filename + "] existe. Voulez-vous le remplacer?", "Confirmation",
                            JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.NO_OPTION)
                        return;
                }
                currentWork.save(filename);
                ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL, "Resip.Graphic: Fichier [" + filename + "] sauvegardé");
                setModifiedContext(false);
                filenameWork = filename;
                Prefs.getInstance().setPrefsLoadDirFromChild(filename);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur de sauvegarde de [" + filename + "]\n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Erreur de sauvegarde de [" + filename + "]\n->" + e.getMessage());
        }
    }

    // MenuItem Close
    private void closeWork() {
        try {
            if ((currentWork != null) && modifiedWork
                    && JOptionPane.showConfirmDialog(mainWindow,
                    "Vous avez un contexte en cours non sauvegardé, la fermeture perdra les modifications.\n"
                            + "Voulez-vous continuer?",
                    "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;

            currentWork = null;
            setFilenameWork(null);
            setModifiedContext(false);
            setContextLoaded(false);
            mainWindow.load();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur de fermeture du contexte en cours [" + filenameWork + "]\n->" + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Resip.Graphic: Erreur de fermeture du contexte en cours ["
                    + filenameWork + "]\n->" + e.getMessage());
        }
    }

    // MenuItem Edit Preferences

    void editPrefs() {
        try {
            PrefsDialog prefsDialog = new PrefsDialog(mainWindow);
            prefsDialog.setVisible(true);
            if (prefsDialog.returnValue == JOptionPane.OK_OPTION) {
                prefsDialog.cc.toPrefs(Prefs.getInstance().getPrefsContextNode());
                prefsDialog.dic.toPrefs(Prefs.getInstance().getPrefsContextNode());
                prefsDialog.mic.toPrefs(Prefs.getInstance().getPrefsContextNode());
                prefsDialog.gmc.toPrefs(Prefs.getInstance().getPrefsContextNode());
                ResipLogger.createGlobalLogger(prefsDialog.cc.getWorkDir()+ File.separator + "log.txt",
                        ResipLogger.getGlobalLogger().getProgressLogLevel());
            }
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible d'éditer les préférences \n->" + e.getMessage());
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,
                    "Resip.GraphicApp: Erreur fatale, impossible d'éditer les préférences \n->" + e.getMessage());

        }
    }

    // MenuItem Empty WorkDir

    private void emptyWorkDir() {
        CreationContext cc = null;
        try {
            if (currentWork != null) {
                JOptionPane.showMessageDialog(mainWindow,
                        "Vous devez fermer tout travail en cours avant de\n" +
                                "procéder au nettoyage du répertoire de travail", "Alerte", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cc = new CreationContext(Prefs.getInstance().getPrefsContextNode());
            if (JOptionPane.showConfirmDialog(mainWindow,
                    "Vous allez effacer tous les répertoires temporaires " +
                            "d'extraction (finissant par \"-tmpdir\")\ndans le répertoire de travail\n" +
                            cc.getWorkDir() + "\nCeux-ci servent à " +
                            "stocker les fichiers avant génération du SIP.\n\n" +
                            "Voulez-vous continuer?",
                    "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;
            InOutDialog inOutDialog = new InOutDialog(mainWindow, "Nettoyage");
            CleanThread cleanThread = new CleanThread(cc.getWorkDir(), inOutDialog);
            cleanThread.execute();
            inOutDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire le nettoyage \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire le nettoyage \n->" + e.getMessage());
        }
    }

    // Treat Menu

    // MenuItem Search

    void search(){
        SearchDialog searchDialog = new SearchDialog(mainWindow, "Chercher");
        searchDialog.setVisible(true);
    }

    // MenuItem Regenerate continuous ids


    void doRegenerateContinuousIds() {
        if (currentWork != null) {
            currentWork.getDataObjectPackage().regenerateContinuousIds();
            mainWindow.allTreeChanged();
        }
    }

    // MenuItem Sort tree viewer

    class SortByTitle implements Comparator<ArchiveUnit>
    {
        DataObjectPackageTreeModel treeModel;

        public int compare(ArchiveUnit a, ArchiveUnit b)
        {
            String titleA=treeModel.findTreeNode(a).getTitle();
            String titleB=treeModel.findTreeNode(b).getTitle();
            return titleA.compareTo(titleB);
        }

        SortByTitle(DataObjectPackageTreeModel treeModel){
            this.treeModel=treeModel;
        }
    }

    Map<TreePath,Boolean> getExpansionState(DataObjectPackageTreeModel treeModel){
        Map<TreePath,Boolean> expansionState=new HashMap<TreePath,Boolean>();
        for ( int i = 0; i < mainWindow.getDataObjectPackageTreePaneViewer().getRowCount(); i++ ){
            TreePath treePath=mainWindow.getDataObjectPackageTreePaneViewer().getPathForRow(i);
            expansionState.put(treePath,mainWindow.getDataObjectPackageTreePaneViewer().isExpanded(i));
        }
        return expansionState;
    }

    void setExpansionState(Map<TreePath,Boolean> expansionState){
        for (Map.Entry<TreePath,Boolean> e:expansionState.entrySet()){
            if (e.getValue())
                mainWindow.getDataObjectPackageTreePaneViewer().expandPath(e.getKey());
        }
    }

    void doSortTreeViewer() {
        if (currentWork != null) {
            DataObjectPackageTreeModel treeModel=(DataObjectPackageTreeModel)mainWindow.getDataObjectPackageTreePaneViewer().getModel();
            Map<TreePath,Boolean> expansionState=getExpansionState(treeModel);
            SortByTitle sortByTitle=new SortByTitle(treeModel);
            for (Map.Entry<String, ArchiveUnit> pair :
                    currentWork.getDataObjectPackage().getAuInDataObjectPackageIdMap().entrySet()) {
                Collections.sort(pair.getValue().getChildrenAuList().getArchiveUnitList(),sortByTitle);
            }
            Collections.sort(currentWork.getDataObjectPackage().getGhostRootAu().getChildrenAuList().getArchiveUnitList(),
                    sortByTitle);
            treeModel.reload();
            setExpansionState(expansionState);
        }
    }

    // Context Menu

    // MenuItem See CreationContext

    void seeImportContext() {
        if (currentWork != null) {
            CreationContext oic = currentWork.getCreationContext();
            if (oic != null) {
                CreationContextDialog creationContextDialog = new CreationContextDialog(mainWindow, oic,
                        currentWork.getDataObjectPackage());
                creationContextDialog.setVisible(true);
            } else
                JOptionPane.showMessageDialog(mainWindow, "Pas de contexte de création défini", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else
            JOptionPane.showMessageDialog(mainWindow, "Pas de contexte ouvert", "Alerte", JOptionPane.WARNING_MESSAGE);
    }

    // MenuItem Edit ExportContext

    void editExportContext() {
        if (currentWork != null) {
            if (currentWork.getExportContext() == null) {
                currentWork.setExportContext(new ExportContext(Prefs.getInstance().getPrefsContextNode()));
            }
            ExportContextDialog exportContextDialog = new ExportContextDialog(mainWindow, currentWork.getExportContext());
            exportContextDialog.setVisible(true);
            if (exportContextDialog.returnValue == JOptionPane.OK_OPTION)
                exportContextDialog.setExportContextFromDialog(currentWork.getExportContext());
        } else
            JOptionPane.showMessageDialog(mainWindow, "Pas de contexte ouvert", "Alerte", JOptionPane.WARNING_MESSAGE);
    }

    // Import Menu

    private boolean isImportActionWrong() {
        if (importThreadRunning) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Un import est en cours vous devez l'annuler ou attendre la fin avant de faire un autre import.",
                    "Alerte", JOptionPane.WARNING_MESSAGE);
            return true;
        }

        if ((currentWork != null) && modifiedWork
                && (JOptionPane.showConfirmDialog(mainWindow,
                "Vous avez un contexte en cours non sauvegardé, un import l'écrasera.\n"
                        + "Voulez-vous continuer?",
                "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION))
            return true;
        return false;
    }

    private void importWork(CreationContext cc) {
        try {
            InOutDialog inOutDialog = new InOutDialog(mainWindow, "Import");
            ImportThread importThread = new ImportThread(cc, inOutDialog);
            importThread.execute();
            inOutDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import \n->" + e.getMessage());
        }
    }

    // MenuItem Import SIP

    void importFromSIP() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CreationContext oic = new SIPImportContext(Prefs.getInstance().getPrefsContextNode());
                oic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(oic);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import \n->" + e.getMessage());
        }
    }

    // MenuItem Import DIP

    void importFromDIP() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CreationContext oic = new DIPImportContext(Prefs.getInstance().getPrefsContextNode());
                oic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(oic);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import \n->" + e.getMessage());
        }
    }

    // MenuItem Import from disk

    void importFromDisk() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CreationContext oic = new DiskImportContext(Prefs.getInstance().getPrefsContextNode());
                oic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(oic);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import \n->" + e.getMessage());
        }
    }

    // MenuItem Import from mail container

    void importFromMail() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                MailImportContext mic = new MailImportContext(Prefs.getInstance().getPrefsContextNode());
                mic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                MailImportContextDialog micd = new MailImportContextDialog(mainWindow, mic);
                micd.setVisible(true);
                if (micd.returnValue != JOptionPane.OK_OPTION)
                    return;
                micd.setMailImportContextFromDialog(mic);
                importWork(mic);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import \n->" + e.getMessage());
        }
    }

    // MenuItem Import from csv tree

    void importFromCSVTree() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CSVTreeImportContext ctic = new CSVTreeImportContext(Prefs.getInstance().getPrefsContextNode());
                ctic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(ctic);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import \n->" + e.getMessage());
        }
    }

    // MenuItem Import from csv metadata

    void importFromCSVMetadata() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CSVMetadataImportContext cmic = new CSVMetadataImportContext(Prefs.getInstance().getPrefsContextNode());
                cmic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(cmic);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de faire l'import \n->" + e.getMessage());
        }
    }

    // Export Menu

    private boolean completeResipWork() {
        if (currentWork == null)
            return true;
        if (currentWork.getExportContext() == null) {
            ExportContext newExportContext = new ExportContext(Prefs.getInstance().getPrefsContextNode());
            ExportContextDialog exportContextDialog = new ExportContextDialog(mainWindow, newExportContext);
            exportContextDialog.setVisible(true);
            if (exportContextDialog.returnValue == JOptionPane.CANCEL_OPTION)
                return true;
            exportContextDialog.setExportContextFromDialog(newExportContext);
            currentWork.setExportContext(newExportContext);
        }
        return false;
    }

    // MenuItem Export Manifest, SIP or disk

    private void exportWork(int exportType) {
        try {
            if (completeResipWork())
                return;
            JFileChooser fileChooser;
            if (currentWork.getExportContext().getOnDiskOutput() != null)
                fileChooser = new JFileChooser(currentWork.getExportContext().getOnDiskOutput());
            else
                fileChooser = new JFileChooser(Prefs.getInstance().getPrefsExportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showSaveDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                InOutDialog inOutDialog = new InOutDialog(mainWindow, "Export");
                currentWork.getExportContext().setOnDiskOutput(fileChooser.getSelectedFile().getAbsolutePath());
                ExportThread exportThread = new ExportThread(currentWork, exportType, inOutDialog);
                exportThread.execute();
                inOutDialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainWindow, "Erreur fatale, impossible de faire l'export \n->" + e.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            ResipLogger.getGlobalLogger().log(ResipLogger.STEP, "Erreur fatale, impossible de faire l'export \n->" + e.getMessage());
        }
    }

    // Info Menu

    private void aPropos(){
        try {
            String version="non définie";
            String builddate="non définie";
            try {
                Properties p = new Properties();
                InputStream is = getClass().getResourceAsStream("/buildinfo.txt");
                if (is != null) {
                    p.load(is);
                    version = p.getProperty("version", "");
                    builddate = p.getProperty("builddate", "");
                }
            } catch (Exception e) {
                // ignore
            }
            JOptionPane.showMessageDialog(mainWindow,
                    "Application Resip (Version : " + version + " Date : " + builddate+")", "A propos de Resip...",
                    JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ignored) {
        }
    }
}
