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
package fr.gouv.vitam.tools.resip.parameters;

import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The Class Prefs.
 * <p>
 *     The preferences are in a properties file named ResipPreferences.properties. It is first searched in the
 *     application directory. If not found it's searched in the personal work directory
 *     (in windows Documents/Resip in linux ~\.resip). If not found it's created with default values in personal work
 *     directory.
 * </p>
 */
public class Prefs {

    /**
     * The preferences file name.
     */
    static public final String PREFERENCES_FILENAME = "ResipPreferences.properties";

    /** The preferences properties. */
    private Properties prefProperties;

    /** The preferences properties filename. */
    private String prefPropertiesFilename;

    /**
     * The instance.
     */
    static Prefs instance;


    /**
     * Gets the single instance of Prefs.
     *
     * @return single instance of Prefs
     */
    public static Prefs getInstance() {
        if (instance == null)
            instance = new Prefs();
        return instance;
    }

    /**
     * Instantiates a new prefProperties.
     */
    private Prefs() {
        try {
            // to get the properties sorted by key when stored work for java 8/9/10
            prefProperties = new Properties(){

                private static final long serialVersionUID = 1L;

                @Override
                public Set<Object> keySet() {
                    return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
                }

                @Override
                public Set<Map.Entry<Object, Object>> entrySet() {

                    Set<Map.Entry<Object, Object>> set1 = super.entrySet();
                    Set<Map.Entry<Object, Object>> set2 = new LinkedHashSet<Map.Entry<Object, Object>>(set1.size());

                    Iterator<Map.Entry<Object, Object>> iterator = set1.stream().sorted(new Comparator<Map.Entry<Object, Object>>() {

                        @Override
                        public int compare(java.util.Map.Entry<Object, Object> o1, java.util.Map.Entry<Object, Object> o2) {
                            return o1.getKey().toString().compareTo(o2.getKey().toString());
                        }
                    }).iterator();

                    while (iterator.hasNext())
                        set2.add(iterator.next());

                    return set2;
                }

                @Override
                public synchronized Enumeration<Object> keys() {
                    return Collections.enumeration(new TreeSet<Object>(super.keySet()));
                }
            };
            try {
                prefPropertiesFilename=PREFERENCES_FILENAME;
                load();
            } catch (SEDALibException e) {
                ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL,
                        "Resip.GraphicApp: Le fichier de préférences global \"" + prefPropertiesFilename + "\" n'a pas " +
                                "été trouvé. Recherche de la version personnelle.\n->" + e.getMessage());

                try {
                    prefPropertiesFilename=Prefs.getDefaultWorkDir()+File.separator+PREFERENCES_FILENAME;
                    load();
                } catch (SEDALibException ee) {
                    ResipLogger.getGlobalLogger().log(ResipLogger.GLOBAL,
                            "Resip.GraphicApp: Le fichier de préférences personnel \"" + prefPropertiesFilename + "\" n'a pas non plus " +
                                    "été trouvé. Ce fichier de préférences va être créé avec les valeurs par défaut.\n->" + ee.getMessage());
                    createDefaultPrefs();
                }
            }
        } catch (Exception e) {
            ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,
                    "Resip.GraphicApp: Erreur fatale, impossible de manipuler les préférences \n->" + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Creates the default prefProperties.
     *
     * @throws SEDALibException the resip exception
     */
    public void createDefaultPrefs() throws SEDALibException {
        try {
            CreationContext oic = new CreationContext();
            oic.setDefaultPrefs();
            oic.toPrefs(this);
            ExportContext gmc = new ExportContext();
            gmc.setDefaultPrefs();
            gmc.toPrefs(this);
            DiskImportContext dic = new DiskImportContext();
            dic.setDefaultPrefs();
            dic.toPrefs(this);
            MailImportContext mic = new MailImportContext();
            mic.setDefaultPrefs();
            mic.toPrefs(this);
            CSVImportContext cic = new CSVImportContext();
            cic.setDefaultPrefs();
            cic.toPrefs(this);
            CSVMetadataImportContext cmic = new CSVMetadataImportContext();
            cmic.setDefaultPrefs();
            cmic.toPrefs(this);
            CSVTreeImportContext ctic = new CSVTreeImportContext();
            ctic.setDefaultPrefs();
            ctic.toPrefs(this);
            TreatmentParameters tsp = new TreatmentParameters();
            tsp.setDefaultPrefs();
            tsp.toPrefs(this);
            save();
        } catch (Exception e) {
            throw new SEDALibException("Panic: Can't create a default preferences file, stop");
        }
    }

    /**
     * Save the prefProperties in the PREFERENCES_FILENAME file.
     *
     * @throws SEDALibException the resip exception
     */
    public void save() throws SEDALibException {
        save(prefPropertiesFilename);
    }

    /**
     * Save the prefProperties in the PREFERENCES_FILENAME file.
     *
     * @param filename the filename
     * @throws SEDALibException the resip exception
     */
    public void save(String filename) throws SEDALibException {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            prefProperties.store(fos, "Resip preferences");
        } catch (Exception e) {
            throw new SEDALibException("Impossible de sauvegarder les préférences\n-> " + e.getMessage());
        }
    }

    /**
     * Import the prefProperties from a file.
     *
     * @throws SEDALibException the resip exception
     */
    public void load() throws SEDALibException {
        load(prefPropertiesFilename);
    }

    /**
     * Import the prefProperties from a file.
     *
     * @param filename the filename
     * @throws SEDALibException the resip exception
     */
    public void load(String filename) throws SEDALibException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            prefProperties.load(fis);
        } catch (Exception e) {
            throw new SEDALibException("Impossible d'importer les préférences\n-> " + e.getMessage());
        }
    }

    /**
     * Gets default work dir, OS dependent.
     *
     * @return the default work dir
     */
    public static String getDefaultWorkDir() {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            return System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Resip";
        else
            return System.getProperty("user.home") + File.separator + ".Resip";
    }

    /**
     * Gets the prefProperties.
     *
     * @return the prefProperties
     */
    public Properties getPrefProperties() {
        return prefProperties;
    }

    /**
     * Gets the prefProperties load dir.
     *
     * @return the prefProperties load dir
     * @throws SEDALibException the seda lib exception
     */
    public String getPrefsLoadDir() throws SEDALibException {
        String result = prefProperties.getProperty("global.loadDir", "");
        if (result.isEmpty())
            result = System.getProperty("user.home");

        return result;
    }

    /**
     * Sets the prefProperties load dir from child.
     *
     * @param loadDir the new prefProperties load dir from child
     * @throws SEDALibException the seda lib exception
     */
    public void setPrefsLoadDirFromChild(String loadDir) throws SEDALibException {
        Path tmp = Paths.get(loadDir).toAbsolutePath().normalize().getParent();
        if (tmp != null) {
            prefProperties.setProperty("global.loadDir", tmp.toString());
            save();
        }
    }

    /**
     * Gets the prefProperties import dir.
     *
     * @return the prefProperties import dir
     * @throws SEDALibException the seda lib exception
     */
    public String getPrefsImportDir() throws SEDALibException {
        String result = prefProperties.getProperty("global.importDir", "");
        if (result.isEmpty())
            result = System.getProperty("user.home");

        return result;
    }

    /**
     * Sets the prefProperties import dir from child.
     *
     * @param importDir the new prefProperties import dir from child
     * @throws SEDALibException the seda lib exception
     */
    public void setPrefsImportDirFromChild(String importDir) throws SEDALibException {
        Path tmp = Paths.get(importDir).toAbsolutePath().normalize().getParent();
        if (tmp != null) {
            prefProperties.setProperty("global.importDir", tmp.toString());
            save();
        }
    }

    /**
     * Gets the prefProperties export dir.
     *
     * @return the prefProperties export dir
     * @throws SEDALibException the seda lib exception
     */
    public String getPrefsExportDir() throws SEDALibException {
        String result = prefProperties.getProperty("global.exportDir", "");
        if (result.isEmpty())
            result = System.getProperty("user.home");

         return result;
    }

    /**
     * Sets the prefProperties export dir from child.
     *
     * @param exportDir the new prefProperties export dir from child
     * @throws SEDALibException the seda lib exception
     */
    public void setPrefsExportDirFromChild(String exportDir) throws SEDALibException {
        Path tmp = Paths.get(exportDir).toAbsolutePath().normalize().getParent();
        if (tmp != null){
            prefProperties.setProperty("global.exportDir", tmp.toString());
            save();
        }
    }

    /**
     * Reinitialise prefProperties.
     *
     * @throws SEDALibException the seda lib exception
     */
    public void reinitialisePrefs() throws SEDALibException {
        createDefaultPrefs();
    }
}
