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

import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.resip.data.Work;
import fr.gouv.vitam.tools.resip.frame.*;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.StructuredArchiveUnitEditorPanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.StructuredDataObjectGroupEditorPanel;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.XMLArchiveUnitEditorPanel;
import fr.gouv.vitam.tools.resip.parameters.*;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.highlevelcomponents.XMLDataObjectGroupEditorPanel;
import fr.gouv.vitam.tools.resip.threads.CheckProfileThread;
import fr.gouv.vitam.tools.resip.threads.CleanThread;
import fr.gouv.vitam.tools.resip.threads.ExportThread;
import fr.gouv.vitam.tools.resip.threads.ImportThread;
import fr.gouv.vitam.tools.resip.utils.ResipException;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.resip.sedaobjecteditor.components.viewers.DataObjectPackageTreeNode;
import fr.gouv.vitam.tools.sedalib.core.SEDA2Version;
import fr.gouv.vitam.tools.sedalib.droid.DroidIdentifier;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static fr.gouv.vitam.tools.resip.threads.SeeManifestThread.launchSeeManifestThread;
import static fr.gouv.vitam.tools.resip.utils.ResipLogger.getGlobalLogger;

/**
 * The type Resip graphic app.
 */
public class ResipGraphicApp implements ActionListener, Runnable {

    /**
     * The Ok dialog.
     */
    static public final int OK_DIALOG = 1;
    /**
     * The Ko dialog.
     */
    static public final int KO_DIALOG = 2;

    /**
     * The constant theApp.
     */
// Uniq instance. */
    static ResipGraphicApp theApp = null;

    /**
     * The Launch creation context.
     */
// Data elements. */
    public CreationContext launchCreationContext;
    /**
     * The Current work.
     */
    public Work currentWork;
    /**
     * The Modified work.
     */
    public boolean modifiedWork;
    /**
     * The Filename work.
     */
    public String filenameWork;
    /**
     * The Treatment parameters.
     */
    public TreatmentParameters treatmentParameters;
    /**
     * The Interface parameters.
     */
    public InterfaceParameters interfaceParameters;

    /**
     * The Main window.
     */
// GUI elements. */
    static public MainWindow mainWindow;

    // MainWindow menu elements dis/enabled depending on work state and used by controller. */
    private JMenuItem saveMenuItem, saveAsMenuItem, closeMenuItem;
    private JMenuItem sedaValidationMenuItem, sedaProfileValidationMenuItem;
    private JCheckBoxMenuItem structuredMenuItem, debugMenuItem;
    private JMenu treatMenu, contextMenu, exportMenu;
    private Map<JMenuItem, String> actionByMenuItem = new HashMap<>();

    /**
     * The Import thread running.
     */
// Thread control elements. */
    public boolean importThreadRunning;
    /**
     * The Add thread running.
     */
    public boolean addThreadRunning;
    /**
     * The Export thread running.
     */
    public boolean exportThreadRunning;

    /**
     * The Search dialog.
     */
// Dialogs elements. */
    public SearchDialog searchDialog;
    /**
     * The Technical search dialog.
     */
    public TechnicalSearchDialog technicalSearchDialog;
    /**
     * The Clean dialog.
     */
    public CleanDialog cleanDialog;
    /**
     * The Statistic window.
     */
    public StatisticWindow statisticWindow;
    /**
     * The Duplicates window.
     */
    public DuplicatesWindow duplicatesWindow;

    /**
     * Instantiates a new Resip graphic app.
     *
     * @param creationContext the creation context
     * @throws ResipException the resip exception
     */
    public ResipGraphicApp(CreationContext creationContext) throws ResipException {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            else
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (IllegalAccessException | InstantiationException |
                UnsupportedLookAndFeelException | ClassNotFoundException e) {
            getGlobalLogger().logIfDebug("Graphic env error",null);
        }

        System.out.println("Resip GraphicApp launched");

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
        this.interfaceParameters = new InterfaceParameters(Prefs.getInstance());

        getGlobalLogger().setDebugFlag(interfaceParameters.isDebugFlag());
        getGlobalLogger().logIfDebug("Resip prefs accessed from "+ Prefs.getInstance().getPrefPropertiesFilename(),null);

        // identification objects initialization
        try {
            DroidIdentifier.getInstance();
        } catch (Exception e) {
            getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, impossible de créer les outils Droid",e);
            System.exit(1);
        }
        EventQueue.invokeLater(this);
    }

    public void run() {
        try {
            mainWindow = new MainWindow(this);
            this.treatmentParameters = new TreatmentParameters(Prefs.getInstance());
            useSeda2Version(treatmentParameters.getSeda2Version());
            mainWindow.setVisible(true);
            mainWindow.setLocationRelativeTo(null);
            this.searchDialog = new SearchDialog(mainWindow);
            this.technicalSearchDialog = new TechnicalSearchDialog(mainWindow);
            this.cleanDialog = new CleanDialog(mainWindow);
            this.statisticWindow = new StatisticWindow();
            this.duplicatesWindow = new DuplicatesWindow();
            currentWork = null;

            if ((launchCreationContext instanceof DiskImportContext) ||
                    (launchCreationContext instanceof SIPImportContext))
                importWork(launchCreationContext);
            else
                mainWindow.load();

            StoreExtractor.initDefaultExtractors();
        } catch (Exception e) {
            getGlobalLogger().log(ResipLogger.ERROR, "Erreur fatale, exécution interrompue",e);
            System.exit(1);
        }

    }

    /**
     * Gets app name.
     *
     * @return the app name
     */
    @SuppressWarnings("SameReturnValue")
    public static String getAppName() {
        return "Resip";
    }

    // Menu controller

    /**
     * Create menu j menu bar.
     *
     * @return the j menu bar
     */
    public JMenuBar createMenu() {
        JMenuBar menuBar;
        JMenu importMenu, fileMenu, infoMenu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        fileMenu = new JMenu("Fichier");
        menuBar.add(fileMenu);

        menuItem = new JMenuItem("Charger...");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "LoadWork");
        fileMenu.add(menuItem);

        saveMenuItem = new JMenuItem("Sauver");
        saveMenuItem.addActionListener(this);
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveMenuItem.setEnabled(false);
        actionByMenuItem.put(saveMenuItem, "SaveWork");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem = new JMenuItem("Sauver sous...");
        saveAsMenuItem.addActionListener(this);
        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.SHIFT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveAsMenuItem.setEnabled(false);
        actionByMenuItem.put(saveAsMenuItem, "SaveAsWork");
        fileMenu.add(saveAsMenuItem);

        closeMenuItem = new JMenuItem("Fermer");
        closeMenuItem.addActionListener(this);
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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

        fileMenu.add(new JSeparator());

        menuItem = new JMenuItem("Quitter");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "Exit");
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
        menuItem.setAccelerator(KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "EditExportContext");
        contextMenu.add(menuItem);

        treatMenu = new JMenu("Traiter");
        menuBar.add(treatMenu);
        treatMenu.setEnabled(false);

        menuItem = new JMenuItem("Chercher des unités d'archives...");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "Search");
        treatMenu.add(menuItem);

        menuItem = new JMenuItem("Chercher des objets...");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('F', InputEvent.SHIFT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "TechnicalSearch");
        treatMenu.add(menuItem);

        treatMenu.add(new JSeparator());

        menuItem = new JMenuItem("Trier l'arbre de visualisation");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "SortTreeViewer");
        treatMenu.add(menuItem);

        menuItem = new JMenuItem("Traiter les doublons...");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('U', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "Duplicates");
        treatMenu.add(menuItem);

        menuItem = new JMenuItem("Nettoyer les inutiles...");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "Clean");
        treatMenu.add(menuItem);

        menuItem = new JMenuItem("Voir les statistiques...");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('Y', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "Statistics");
        treatMenu.add(menuItem);

        menuItem = new JMenuItem("Voir le manifest...");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "SeeManifest");
        treatMenu.add(menuItem);

        sedaValidationMenuItem = new JMenuItem("Vérifier la conformité "+ SEDA2Version.getSeda2VersionString()+"...");
        sedaValidationMenuItem.addActionListener(this);
        sedaValidationMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(sedaValidationMenuItem, "CheckSEDASchema");
        treatMenu.add(sedaValidationMenuItem);

        sedaProfileValidationMenuItem = new JMenuItem("Vérifier la conformité à un profil "+ SEDA2Version.getSeda2VersionString()+"...");
        sedaProfileValidationMenuItem.addActionListener(this);
        sedaProfileValidationMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.SHIFT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(sedaProfileValidationMenuItem, "CheckSpecificSEDASchemaProfile");
        treatMenu.add(sedaProfileValidationMenuItem);

        menuItem = new JMenuItem("Vérifier EndDate > StartDate");
        menuItem.addActionListener(this);
        menuItem.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.ALT_DOWN_MASK + Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        actionByMenuItem.put(menuItem, "CheckEndDate");
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

        menuItem = new JMenuItem("Importer depuis un zip...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ImportFromZip");
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

        menuItem = new JMenuItem("Exporter la hiérarchie simplifiée et le csv sur disque...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ExportToCSVDisk");
        exportMenu.add(menuItem);

        menuItem = new JMenuItem("Exporter la hiérarchie simplifiée et le csv en zip...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ExportToCSVZip");
        exportMenu.add(menuItem);

        menuItem = new JMenuItem("Exporter le csv des métadonnées...");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "ExportToCSVMetadataFile");
        exportMenu.add(menuItem);

        infoMenu = new JMenu("?");
        menuBar.add(infoMenu);

        menuItem = new JMenuItem("A propos de Resip");
        menuItem.addActionListener(this);
        actionByMenuItem.put(menuItem, "About");
        infoMenu.add(menuItem);

        structuredMenuItem = new JCheckBoxMenuItem("Editeur structuré");
        structuredMenuItem.setState(interfaceParameters.isStructuredMetadataEditionFlag());
        structuredMenuItem.addActionListener(this);
        actionByMenuItem.put(structuredMenuItem, "ToggleStructuredEdition");
        infoMenu.add(structuredMenuItem);

        debugMenuItem = new JCheckBoxMenuItem("Mode débug");
        debugMenuItem.setState(interfaceParameters.isDebugFlag());
        debugMenuItem.addActionListener(this);
        actionByMenuItem.put(debugMenuItem, "ToggleDebugMode");
        infoMenu.add(debugMenuItem);
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
                    case "Exit":
                        exit();
                        break;
                    case "EmptyWorkDir":
                        emptyWorkDir();
                        break;
                    // Treat Menu
                    case "Search":
                        search();
                        break;
                    case "TechnicalSearch":
                        technicalSearch();
                        break;
                    case "Duplicates":
                        duplicates();
                        break;
                    case "Clean":
                        clean();
                        break;
                    case "Statistics":
                        generateStatistics();
                        break;
                    case "SeeManifest":
                        seeManifest();
                        break;
                    case "CheckSEDASchema":
                        checkSEDASchema();
                        break;
                    case "CheckSpecificSEDASchemaProfile":
                        checkSpecificSEDASchemaProfile();
                        break;
                    case "CheckEndDate":
                        checkEndDate();
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
                    case "ImportFromZip":
                        importFromZip();
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
                        exportWork(ExportThread.SIP_ALL_EXPORT);
                        break;
                    case "ExportToSEDAXMLManifest":
                        exportWork(ExportThread.SIP_MANIFEST_EXPORT);
                        break;
                    case "ExportToDisk":
                        exportWork(ExportThread.DISK_EXPORT);
                        break;
                    case "ExportToCSVDisk":
                        exportWork(ExportThread.CSV_ALL_DISK_EXPORT);
                        break;
                    case "ExportToCSVZip":
                        exportWork(ExportThread.CSV_ALL_ZIP_EXPORT);
                        break;
                    case "ExportToCSVMetadataFile":
                        exportWork(ExportThread.CSV_METADATA_FILE_EXPORT);
                        break;
                    case "About":
                        about();
                        break;
                    case "ToggleStructuredEdition":
                        toggleStructuredEdition();
                        break;
                    case "ToggleDebugMode":
                        toggleDebugMode();
                        break;
                }
        }
    }

    // Utils

    /**
     * Gets the app.
     *
     * @return the app
     */
    public static ResipGraphicApp getTheApp() {
        return theApp;
    }

    /**
     * Gets the main window.
     *
     * @return the main window
     */
    public static MainWindow getTheWindow() {
        return mainWindow;
    }

    /**
     * Sets modified context.
     *
     * @param isModified the is modified
     */
    public void setModifiedContext(boolean isModified) {
        modifiedWork = isModified;
        saveMenuItem.setEnabled(modifiedWork && (filenameWork != null));
    }

    /**
     * Sets context loaded.
     *
     * @param isLoaded the is loaded
     */
    public void setContextLoaded(boolean isLoaded) {
        treatMenu.setEnabled(isLoaded);
        contextMenu.setEnabled(isLoaded);
        exportMenu.setEnabled(isLoaded);
        saveAsMenuItem.setEnabled(isLoaded);
        closeMenuItem.setEnabled(isLoaded);

        statisticWindow.setVisible(false);

        duplicatesWindow.setVisible(false);
        duplicatesWindow.emptyDialog();

        cleanDialog.setVisible(false);
        cleanDialog.emptyDialog();

        technicalSearchDialog.setVisible(false);
        technicalSearchDialog.emptyDialog();

        searchDialog.setVisible(false);
        searchDialog.emptyDialog();
    }

    /**
     * Sets filename work.
     *
     * @param fileName the file name
     */
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
                UserInteractionDialog.getUserAnswer(mainWindow,
                        "Un import est en cours vous devez l'annuler ou attendre la fin avant de faire un chargement.",
                        "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                        null);
                return;
            }

            if ((currentWork != null) && modifiedWork
                    && (UserInteractionDialog.getUserAnswer(mainWindow,
                    "Vous avez un contexte en cours non sauvegardé, un chargement l'écrasera.\n"
                            + "Voulez-vous continuer?",
                    "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                    null) != OK_DIALOG))
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsLoadDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (fileChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                filename = fileChooser.getSelectedFile().getCanonicalPath();
                currentWork = Work.createFromFile(filename);
                getGlobalLogger().log(ResipLogger.GLOBAL, "Fichier [" + filename + "] chargé",null);
                mainWindow.load();
                mainWindow.setCursor(Cursor.getDefaultCursor());
                setFilenameWork(filename);
                setContextLoaded(true);
                setModifiedContext(false);
                Prefs.getInstance().setPrefsLoadDirFromChild(filename);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur de chargement de [" + filename + "]\n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.STEP, "Erreur de chargement de [" + filename + "]",e);
            mainWindow.setCursor(Cursor.getDefaultCursor());
        }
    }

    // MenuItem Save

    private void saveWork() {
        if (filenameWork != null)
            try {
                mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                currentWork.save(filenameWork);
                mainWindow.setCursor(Cursor.getDefaultCursor());
                setModifiedContext(false);
            } catch (Exception e) {
                UserInteractionDialog.getUserAnswer(mainWindow,
                        "Erreur de sauvegarde de [" + filenameWork + "]\n->" + e.getMessage(),
                        "Erreur", UserInteractionDialog.ERROR_DIALOG,
                        null);
                getGlobalLogger().log(ResipLogger.STEP,
                        "Resip.Graphic: Erreur de sauvegarde de [" + filenameWork + "]",e);
                mainWindow.setCursor(Cursor.getDefaultCursor());
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
                    if (UserInteractionDialog.getUserAnswer(mainWindow,
                            "Le fichier [" + filename + "] existe. Voulez-vous le remplacer ?",
                            "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                            null) != OK_DIALOG)
                        return;
                }
                mainWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                currentWork.save(filename);
                mainWindow.setCursor(Cursor.getDefaultCursor());
                getGlobalLogger().log(ResipLogger.GLOBAL, "Resip.Graphic: Fichier [" + filename + "] sauvegardé",null);
                setModifiedContext(false);
                filenameWork = filename;
                Prefs.getInstance().setPrefsLoadDirFromChild(filename);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur de sauvegarde de [" + filename + "]\n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.STEP, "Erreur de sauvegarde de [" + filename + "]",e);
            mainWindow.setCursor(Cursor.getDefaultCursor());
        }
    }

    // MenuItem Close
    private void closeWork() {
        try {
            if ((currentWork != null) && modifiedWork
                    && UserInteractionDialog.getUserAnswer(mainWindow,
                    "Vous avez un contexte en cours non sauvegardé, la fermeture entrainera la perte des modifications.\n"
                            + "Voulez-vous continuer?",
                    "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                    null) != OK_DIALOG)
                return;

            currentWork = null;
            setFilenameWork(null);
            setModifiedContext(false);
            setContextLoaded(false);
            mainWindow.load();
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur de fermeture du contexte en cours [" + filenameWork + "]\n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.STEP, "Resip.Graphic: Erreur de fermeture du contexte en cours ["
                    + filenameWork + "]",e);
        }
    }

    private void useSeda2Version(int version) throws SEDALibException {
        SEDA2Version.setSeda2Version(version);
        sedaValidationMenuItem.setText("Vérifier la conformité "+ SEDA2Version.getSeda2VersionString()+"...");
        sedaProfileValidationMenuItem.setText("Vérifier la conformité à un profil "+ SEDA2Version.getSeda2VersionString()+"...");
    }

    // MenuItem Edit Preferences

    /**
     * Edit prefs.
     */
    void editPrefs() {
        try {
            PrefsDialog prefsDialog = new PrefsDialog(mainWindow);
            prefsDialog.setVisible(true);
            if (prefsDialog.getReturnValue() == OK_DIALOG) {
                prefsDialog.cc.toPrefs(Prefs.getInstance());
                prefsDialog.dic.toPrefs(Prefs.getInstance());
                prefsDialog.mic.toPrefs(Prefs.getInstance());
                prefsDialog.gmc.toPrefs(Prefs.getInstance());
                prefsDialog.cic.toPrefs(Prefs.getInstance());
                prefsDialog.tp.toPrefs(Prefs.getInstance());
                prefsDialog.ip.toPrefs(Prefs.getInstance());
                Prefs.getInstance().save();
                treatmentParameters = prefsDialog.tp;
                useSeda2Version(treatmentParameters.getSeda2Version());
                ResipLogger.createGlobalLogger(prefsDialog.cc.getWorkDir() + File.separator + "log.txt",
                        getGlobalLogger().getProgressLogLevel());
            }
        } catch (ResipException | SEDALibException e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible d'éditer les préférences \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR,
                    "resip.graphicapp: erreur fatale, impossible d'éditer les préférences",e);

        }
    }

    // MenuItem Empty WorkDir

    private void emptyWorkDir() {
        CreationContext cc;
        try {
            if (currentWork != null) {
                UserInteractionDialog.getUserAnswer(mainWindow,
                        "Vous devez fermer tout travail en cours avant de\n" +
                                "procéder au nettoyage du répertoire de travail",
                        "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                        null);
                return;
            }
            cc = new CreationContext(Prefs.getInstance());
            if (UserInteractionDialog.getUserAnswer(mainWindow,
                    "Vous allez effacer tous les répertoires temporaires " +
                            "d'extraction (finissant par \"-tmpdir\")\ndans le répertoire de travail\n" +
                            cc.getWorkDir() + "\n" +
                            "Voulez-vous continuer?",
                    "Confirmation",
                    UserInteractionDialog.WARNING_DIALOG,
                    "Ces répertoires temporaires sont utilisés pour stocker les fichiers issus " +
                            "des extraction de conteneurs de messageries ou de SIP, DIP...\nAssurez-vous que vous n'avez" +
                            " pas de sauvegarde de travail en cours qui utilise ces fichiers, avant de les supprimer.") != OK_DIALOG)
                return;
            InOutDialog inOutDialog = new InOutDialog(mainWindow, "Nettoyage");
            CleanThread cleanThread = new CleanThread(cc.getWorkDir(), inOutDialog);
            cleanThread.execute();
            inOutDialog.setVisible(true);
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire le nettoyage \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire le nettoyage",e);
        }
    }

    // MenuItem Close

    /**
     * Exit to system.
     */
    public void exit() {
        try {
            if ((currentWork != null) && modifiedWork
                    && UserInteractionDialog.getUserAnswer(mainWindow,
                    "Vous avez un contexte en cours non sauvegardé, la fermeture de l'application entrainera la perte des modifications.\n"
                            + "Voulez-vous continuer?",
                    "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                    null) != OK_DIALOG)
                return;

            System.exit(0);
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur de fermeture de l'application\n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.STEP, "resip.graphicapp: erreur de fermeture de l'application",e);
        }
    }

    // Treat Menu

    // MenuItem Search

    /**
     * Search.
     */
    void search() {
        searchDialog.setVisible(true);
    }

    // MenuItem TechnicalSearch

    /**
     * Technical search.
     */
    void technicalSearch() {
        technicalSearchDialog.setVisible(true);
    }

    // MenuItem Duplicates

    /**
     * Duplicates.
     */
    void duplicates() {
        duplicatesWindow.setVisible(true);
    }

    /**
     * Clean.
     */
    void clean() {
        cleanDialog.setVisible(true);
    }

    /**
     * See the manifest.
     */
    void seeManifest() {
        launchSeeManifestThread(currentWork);
    }

    /**
     * Check seda schema.
     */
    void checkSEDASchema() {
        InOutDialog inOutDialog = new InOutDialog(mainWindow, "Vérification SEDA "+ SEDA2Version.getSeda2VersionString());
        CheckProfileThread checkProfileThread = new CheckProfileThread(null, inOutDialog);
        completeResipWork();
        checkProfileThread.execute();
        inOutDialog.setVisible(true);
    }

    // MenuItem Check specific SEDA 21 Profile compliance

    /**
     * Check specific seda schema profile.
     */
    void checkSpecificSEDASchemaProfile() {
        try {
            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                InOutDialog inOutDialog = new InOutDialog(mainWindow, "Vérification Profil SEDA "+ SEDA2Version.getSeda2VersionString());
                CheckProfileThread checkProfileThread = new CheckProfileThread(fileChooser.getSelectedFile()
                        .getAbsolutePath(), inOutDialog);
                completeResipWork();
                checkProfileThread.execute();
                inOutDialog.setVisible(true);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire la vérification de confirmité au profil SEDA "+ SEDA2Version.getSeda2VersionString()+"\n->"
                            + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire la " +
                    "vérification de confirmité au profil SEDA  "+ SEDA2Version.getSeda2VersionString(),e);
        }
    }

    /**
     * Check end date > start date
     */
    void checkEndDate() {
        VerifyDateDialog verifyDateDialog = new VerifyDateDialog(mainWindow);
        verifyDateDialog.setVisible(true);
    }

    // MenuItem Regenerate continuous ids

    /**
     * Do regenerate continuous ids.
     */
    void doRegenerateContinuousIds() {
        if (currentWork != null) {
            currentWork.getDataObjectPackage().regenerateContinuousIds();
            mainWindow.treePane.allTreeChanged();
        }
    }

    // MenuItem Sort tree viewer

    /**
     * Do sort tree viewer.
     */
    void doSortTreeViewer() {
        if (currentWork != null) {
            mainWindow.treePane.doSortTree();
        }
    }

    // Menu item statistics

    /**
     * Generate statistics.
     */
    void generateStatistics() {
        statisticWindow.refresh();
        statisticWindow.setVisible(true);
    }

    // Context Menu

    // MenuItem See CreationContext

    /**
     * See import context.
     */
    void seeImportContext() {
        if (currentWork != null) {
            CreationContext oic = currentWork.getCreationContext();
            if (oic != null) {
                CreationContextDialog creationContextDialog = new CreationContextDialog(mainWindow, oic,
                        currentWork.getDataObjectPackage());
                creationContextDialog.setVisible(true);
            } else
                UserInteractionDialog.getUserAnswer(mainWindow, "Pas de contexte de création défini", "Erreur", UserInteractionDialog.ERROR_DIALOG,
                        null);
        } else
            UserInteractionDialog.getUserAnswer(mainWindow, "Pas de contexte ouvert", "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
    }

    // MenuItem Edit ExportContext

    /**
     * Edit export context.
     */
    void editExportContext() {
        if (currentWork != null) {
            if (currentWork.getExportContext() == null) {
                currentWork.setExportContext(new ExportContext(Prefs.getInstance()));
            }
            ExportContextDialog exportContextDialog = new ExportContextDialog(mainWindow, currentWork.getExportContext());
            exportContextDialog.setVisible(true);
            if (exportContextDialog.getReturnValue() == OK_DIALOG)
                currentWork.setExportContext(exportContextDialog.gmc);
        } else
            UserInteractionDialog.getUserAnswer(mainWindow, "Pas de contexte ouvert", "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
    }

    // Import Menu

    private boolean isImportActionWrong() {
        if (importThreadRunning) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Un import est en cours vous devez l'annuler ou attendre la fin avant de faire un autre import.",
                    "Information", UserInteractionDialog.IMPORTANT_DIALOG,
                    null);
            return true;
        }

        if ((currentWork != null) && modifiedWork
                && (UserInteractionDialog.getUserAnswer(mainWindow,
                "Vous avez un contexte en cours non sauvegardé, un import l'écrasera.\n"
                        + "Voulez-vous continuer?",
                "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                null) != OK_DIALOG))
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
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // MenuItem Import SIP

    /**
     * Import from sip.
     */
    void importFromSIP() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CreationContext oic = new SIPImportContext(Prefs.getInstance());
                oic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(oic);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // MenuItem Import DIP

    /**
     * Import from dip.
     */
    void importFromDIP() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CreationContext oic = new DIPImportContext(Prefs.getInstance());
                oic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(oic);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // MenuItem Import from disk

    /**
     * Import from disk.
     */
    void importFromDisk() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CreationContext oic = new DiskImportContext(Prefs.getInstance());
                oic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(oic);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // MenuItem Import from zip

    /**
     * Import from zip.
     */
    void importFromZip() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CreationContext oic = new ZipImportContext(Prefs.getInstance());
                oic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(oic);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // MenuItem Import from mail container

    /**
     * Import from mail.
     */
    void importFromMail() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                MailImportContext mic = new MailImportContext(Prefs.getInstance());
                mic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                MailImportContextDialog micd = new MailImportContextDialog(mainWindow, mic);
                micd.setVisible(true);
                if (micd.returnValue != OK_DIALOG)
                    return;
                micd.setMailImportContextFromDialog(mic);
                importWork(mic);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // MenuItem Import from csv tree

    /**
     * Import from csv tree.
     */
    void importFromCSVTree() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CSVTreeImportContext ctic = new CSVTreeImportContext(Prefs.getInstance());
                ctic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(ctic);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // MenuItem Import from csv metadata

    /**
     * Import from csv metadata.
     */
    void importFromCSVMetadata() {
        try {
            if (isImportActionWrong())
                return;

            JFileChooser fileChooser = new JFileChooser(Prefs.getInstance().getPrefsImportDir());
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                CSVMetadataImportContext cmic = new CSVMetadataImportContext(Prefs.getInstance());
                cmic.setOnDiskInput(fileChooser.getSelectedFile().toString());
                importWork(cmic);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow,
                    "Erreur fatale, impossible de faire l'import \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.ERROR, "resip.graphicapp: erreur fatale, impossible de faire l'import",e);
        }
    }

    // Export Menu

    private boolean completeResipWork() {
        if (currentWork == null)
            return true;
        if (currentWork.getExportContext() == null) {
            ExportContext newExportContext = new ExportContext(Prefs.getInstance());
            ExportContextDialog exportContextDialog = new ExportContextDialog(mainWindow, newExportContext);
            exportContextDialog.setVisible(true);
            if (exportContextDialog.getReturnValue() == KO_DIALOG)
                return true;
            currentWork.setExportContext(exportContextDialog.gmc);
        }
        return false;
    }

    // MenuItem Export Manifest, SIP or disk

    private void exportWork(int exportType) {
        String defaultFilename;
        try {
            if (completeResipWork())
                return;
            JFileChooser fileChooser;
            if (currentWork.getExportContext().getOnDiskOutput() != null) {
                fileChooser = new JFileChooser(currentWork.getExportContext().getOnDiskOutput());
                defaultFilename = currentWork.getExportContext().getOnDiskOutput() + File.separator;
            } else {
                fileChooser = new JFileChooser(Prefs.getInstance().getPrefsExportDir());
                defaultFilename = Prefs.getInstance().getPrefsExportDir() + File.separator;
            }
            if ((exportType != ExportThread.DISK_EXPORT) && (exportType != ExportThread.CSV_ALL_DISK_EXPORT)) {
                switch (exportType) {
                    case ExportThread.SIP_ALL_EXPORT:
                        defaultFilename += "SIP.zip";
                        break;
                    case ExportThread.SIP_MANIFEST_EXPORT:
                        defaultFilename += "manifest.xml";
                        break;
                    case ExportThread.CSV_ALL_ZIP_EXPORT:
                        defaultFilename += "FilesCsv.zip";
                        break;
                    case ExportThread.CSV_METADATA_FILE_EXPORT:
                        defaultFilename += "metadata.csv";
                        break;
                }
                fileChooser.setSelectedFile(new File(defaultFilename));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            } else
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showSaveDialog(this.mainWindow) == JFileChooser.APPROVE_OPTION) {
                Path path = Paths.get(fileChooser.getSelectedFile().getAbsolutePath());
                if (Files.exists(path)) {
                    if (!Files.isDirectory(path) && UserInteractionDialog.getUserAnswer(mainWindow,
                            "Le fichier [" + path.toString() + "] existe. Voulez-vous le remplacer ?",
                            "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                            null) != OK_DIALOG)
                        return;
                    else if (Files.isDirectory(path) && Files.list(path).findAny().isPresent() && UserInteractionDialog.getUserAnswer(mainWindow,
                            "Le répertoire [" + path.toString() + "] existe. Les fichiers exportés sur le disque vont " +
                                    "se mélanger avec ceux déjà existants. Voulez-vous continuer? ?",
                            "Confirmation", UserInteractionDialog.WARNING_DIALOG,
                            null) != OK_DIALOG)
                        return;
                }
                InOutDialog inOutDialog = new InOutDialog(mainWindow, "Export");
                currentWork.getExportContext().setOnDiskOutput(path.toString());
                ExportThread exportThread = new ExportThread(currentWork, exportType, inOutDialog);
                exportThread.execute();
                inOutDialog.setVisible(true);
            }
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow, "Erreur fatale, impossible de faire l'export \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.STEP, "resip.graphicapp: erreur fatale, impossible de faire l'export",e);
        }
    }

    // Info Menu

    private void about() {
        try {
            String version = "non définie";
            String builddate = "non définie";
            try {
                Properties p = new Properties();
                InputStream is = getClass().getResourceAsStream("/buildinfo.txt");
                if (is != null) {
                    p.load(is);
                    version = p.getProperty("version", "");
                    builddate = p.getProperty("builddate", "");
                }
            } catch (Exception ignored) {
            }
            AboutDialog dialog = new AboutDialog(mainWindow, "Application Resip\n  - Version : " + version + "\n  - Date : " + builddate);
            dialog.setVisible(true);
        } catch (Exception ignored) {
        }
    }

    private void toggleStructuredEdition() {
        try {
            interfaceParameters.setStructuredMetadataEditionFlag(structuredMenuItem.getState());
            if (ResipGraphicApp.getTheApp().interfaceParameters.isStructuredMetadataEditionFlag()) {
                StructuredArchiveUnitEditorPanel structuredArchiveUnitEditorPanel = new StructuredArchiveUnitEditorPanel();
                ResipGraphicApp.getTheWindow().itemPane.setTopComponent(structuredArchiveUnitEditorPanel);
                ResipGraphicApp.getTheWindow().auMetadataPane = structuredArchiveUnitEditorPanel;
                StructuredDataObjectGroupEditorPanel structuredDataObjectGroupEditorPanel = new StructuredDataObjectGroupEditorPanel();
                ResipGraphicApp.getTheWindow().itemPane.setBottomComponent(structuredDataObjectGroupEditorPanel);
                ResipGraphicApp.getTheWindow().dogMetadataPane = structuredDataObjectGroupEditorPanel;
            } else {
                XMLArchiveUnitEditorPanel xmlArchiveUnitEditorPanel = new XMLArchiveUnitEditorPanel();
                ResipGraphicApp.getTheWindow().itemPane.setTopComponent(xmlArchiveUnitEditorPanel);
                ResipGraphicApp.getTheWindow().auMetadataPane = xmlArchiveUnitEditorPanel;
                XMLDataObjectGroupEditorPanel xmlDataObjectGroupEditorPanel = new XMLDataObjectGroupEditorPanel();
                ResipGraphicApp.getTheWindow().itemPane.setBottomComponent(xmlDataObjectGroupEditorPanel);
                ResipGraphicApp.getTheWindow().dogMetadataPane = xmlDataObjectGroupEditorPanel;
            }
            DataObjectPackageTreeNode node=ResipGraphicApp.getTheWindow().treePane.getDisplayedTreeNode();
            if (node!=null)
                ResipGraphicApp.getTheWindow().auMetadataPane.editArchiveUnit(node.getArchiveUnit());
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow, "Erreur fatale, impossible de changer de type d'édition \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.STEP, "resip.graphicapp: erreur fatale, impossible de changer de type d'édition",e);
        }
    }

    private void toggleDebugMode() {
        try {
            interfaceParameters.setDebugFlag(debugMenuItem.getState());
        } catch (Exception e) {
            UserInteractionDialog.getUserAnswer(mainWindow, "Erreur fatale, impossible de changer de type de mode de débug \n->" + e.getMessage(),
                    "Erreur", UserInteractionDialog.ERROR_DIALOG,
                    null);
            getGlobalLogger().log(ResipLogger.STEP, "resip.graphicapp: erreur fatale, impossible de changer de type de mode de débug",e);
        }
    }
}
