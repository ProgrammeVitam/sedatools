/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@programmevitam.fr
 * 
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives 
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high 
 * volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.frame.UserInteractionDialog;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageDeserializer;
import fr.gouv.vitam.tools.sedalib.core.json.DataObjectPackageSerializer;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.io.IOUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Prefs.
 */
public class Prefs {

	/** The Constant CURRENT_SERIALIZATION_VERSION. */
	static final String CURRENT_SERIALIZATION_VERSION = "1.0";

	/** The prefs. */
	private Preferences prefs;

	/** The instance. */
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
     * Creates the default prefs.
     *
     * @throws SEDALibException the resip exception
     */
    public void createDefaultPrefs() throws SEDALibException {
        try {
            Preferences globalNode = getPrefsContextNode();
            globalNode.put("serilizationVersion", CURRENT_SERIALIZATION_VERSION);
            CreationContext oic = new CreationContext();
            oic.setDefaultPrefs();
            oic.toPrefs(globalNode);
            ExportContext gmc = new ExportContext();
            gmc.setDefaultPrefs();
            gmc.toPrefs(globalNode);
            DiskImportContext dic = new DiskImportContext();
            dic.setDefaultPrefs();
            dic.toPrefs(globalNode);
            MailImportContext mic = new MailImportContext();
            mic.setDefaultPrefs();
            mic.toPrefs(globalNode);
            CSVMetadataImportContext cmic= new CSVMetadataImportContext();
            cmic.setDefaultPrefs();
            cmic.toPrefs(globalNode);
            CSVTreeImportContext ctic= new CSVTreeImportContext();
            ctic.setDefaultPrefs();
            ctic.toPrefs(globalNode);
            TreatmentParameters tsp=new TreatmentParameters();
            tsp.setDefaultPrefs();
            tsp.toPrefs(globalNode);
            globalNode.flush();
        } catch (Exception e) {
            throw new SEDALibException("Panic: Can't create a default preferences file, stop");
        }
    }

	/**
	 * Save the prefs in a file.
	 *
	 * @param filename the filename
	 * @throws SEDALibException the resip exception
	 */
	public void savePrefs(String filename) throws SEDALibException {
		try(FileOutputStream fos=new FileOutputStream(filename)) {
			Preferences globalNode = getPrefsContextNode();
			CreationContext oic = new CreationContext(globalNode);
			DiskImportContext dic = new DiskImportContext(globalNode);
			MailImportContext mic = new MailImportContext(globalNode);
			CSVMetadataImportContext cmic= new CSVMetadataImportContext(globalNode);
			CSVTreeImportContext ctic= new CSVTreeImportContext(globalNode);
			ExportContext gmc = new ExportContext(globalNode);
			TreatmentParameters tsp=new TreatmentParameters(globalNode);

			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
			mapper.writeValue(fos,CURRENT_SERIALIZATION_VERSION);
			mapper.writeValue(fos, oic);
			mapper.writeValue(fos, dic);
			mapper.writeValue(fos, mic);
			mapper.writeValue(fos, cmic);
			mapper.writeValue(fos, ctic);
			mapper.writeValue(fos, gmc);
			mapper.writeValue(fos, tsp);
		} catch (Exception e) {
			throw new SEDALibException("Impossible de sauvegarder les préférences\n-> "+e.getMessage());
		}
	}

	/**
	 * Import the prefs from a file.
	 *
	 * @param filename the filename
	 * @throws SEDALibException the resip exception
	 */
	public void importPrefs(String filename) throws SEDALibException {
		String prefSerializationVersion;
		try(FileInputStream fis=new FileInputStream(filename)) {
			ObjectMapper mapper = new ObjectMapper();
			JsonParser jsonParser = mapper.getFactory().createParser(fis);
			jsonParser.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

			prefSerializationVersion=mapper.readValue(jsonParser,String.class);
			if (!prefSerializationVersion.equals("1.0"))
				throw new SEDALibException("Version de sauvegarde inconnue");

			CreationContext oic = mapper.readValue(jsonParser,CreationContext.class);
			DiskImportContext dic = mapper.readValue(jsonParser,DiskImportContext.class);
			MailImportContext mic = mapper.readValue(jsonParser,MailImportContext.class);
			CSVMetadataImportContext cmic= mapper.readValue(jsonParser,CSVMetadataImportContext.class);
			CSVTreeImportContext ctic= mapper.readValue(jsonParser,CSVTreeImportContext.class);
			ExportContext gmc = mapper.readValue(jsonParser,ExportContext.class);
			TreatmentParameters tsp=mapper.readValue(jsonParser,TreatmentParameters.class);

			Preferences globalNode = getPrefsContextNode();
			globalNode.put("serilizationVersion", prefSerializationVersion);
			oic.toPrefs(globalNode);
			gmc.toPrefs(globalNode);
			dic.toPrefs(globalNode);
			mic.toPrefs(globalNode);
			cmic.toPrefs(globalNode);
			ctic.toPrefs(globalNode);
			tsp.toPrefs(globalNode);
			globalNode.flush();
		} catch (Exception e) {
			throw new SEDALibException("Impossible d'importer les préférences\n-> "+e.getMessage());
		}
	}

	/**
	 * Instantiates a new prefs.
	 */
	private Prefs() {
		try {
			prefs = Preferences.userRoot().node(ResipGraphicApp.getAppName());
			if (prefs.childrenNames().length == 0) {
				createDefaultPrefs();
			}
		} catch (BackingStoreException | SEDALibException e) {
			UserInteractionDialog.getUserAnswer(null,
					"Erreur fatale, impossible de manipuler les préférences \n->" + e.getMessage(),"Erreur",
					UserInteractionDialog.ERROR_DIALOG,null);
			ResipLogger.getGlobalLogger().log(ResipLogger.ERROR,
					"Resip.GraphicApp: Erreur fatale, impossible de manipuler les préférences \n->" + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Gets the prefs.
	 *
	 * @return the prefs
	 */
	public Preferences getPrefs() {
		return prefs;
	}

	/**
	 * Gets the prefs context node.
	 *
	 * @return the prefs context node
	 */
	public Preferences getPrefsContextNode() {
		return prefs.node("Work").node("Default");
	}

	/**
	 * Gets the prefs load dir.
	 *
	 * @return the prefs load dir
	 */
	public String getPrefsLoadDir() {
		String result = prefs.get("loadDir", "");
		if (result.isEmpty())
			result = System.getProperty("user.home");
		return result;
	}

	/**
	 * Sets the prefs load dir from child.
	 *
	 * @param loadDir the new prefs load dir from child
	 */
	public void setPrefsLoadDirFromChild(String loadDir) {
		Path tmp = Paths.get(loadDir).toAbsolutePath().normalize().getParent();
		if (tmp != null)
			prefs.put("loadDir", tmp.toString());
	}

	/**
	 * Gets the prefs import dir.
	 *
	 * @return the prefs import dir
	 */
	public String getPrefsImportDir() {
		String result = prefs.get("importDir", "");
		if (result.isEmpty())
			result = System.getProperty("user.home");
		return result;
	}

	/**
	 * Sets the prefs import dir from child.
	 *
	 * @param importDir the new prefs import dir from child
	 */
	public void setPrefsImportDirFromChild(String importDir) {
		Path tmp = Paths.get(importDir).toAbsolutePath().normalize().getParent();
		if (tmp != null)
			prefs.put("importDir", tmp.toString());
	}
	
	/**
	 * Gets the prefs export dir.
	 *
	 * @return the prefs export dir
	 */
	public String getPrefsExportDir() {
		String result = prefs.get("exportDir", "");
		if (result.isEmpty())
			result = System.getProperty("user.home");
		return result;
	}

	/**
	 * Sets the prefs export dir from child.
	 *
	 * @param exportDir the new prefs export dir from child
	 */
	public void setPrefsExportDirFromChild(String exportDir) {
		Path tmp = Paths.get(exportDir).toAbsolutePath().normalize().getParent();
		if (tmp != null)
			prefs.put("exportDir", tmp.toString());
	}

	/**
	 * Reinitialise prefs.
	 *
	 * @throws SEDALibException the seda lib exception
	 */
	public void reinitialisePrefs() throws SEDALibException {
		try {
			Preferences.userRoot().node(ResipGraphicApp.getAppName()).clear();
		} catch (BackingStoreException e) {
			throw new SEDALibException("Suppression des preférences existantes impossible");
		}
		createDefaultPrefs();
	}

}
