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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import fr.gouv.vitam.tools.resip.app.ResipGraphicApp;
import fr.gouv.vitam.tools.resip.utils.ResipLogger;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

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
	 * @throws SEDALibException the resipt exception
	 */
	void createDefaultPrefs() throws SEDALibException {
//		File f = new File(DEFAULTCONTEXT_FILENAME);
//
//		if (f.exists() && f.isFile())
//		try {
//			ObjectMapper mapper = new ObjectMapper();
//			GlobalMetadata sipContext=mapper.readValue(f, GlobalMetadata.class);
//			try{prefs.putInt("PrefsVersion", version);
//			Preferences context=prefs.node("SIPContext").node("Default");
//			sipContext.toPrefs(context);
//			context.flush();
//			}
//			catch (Exception e) {
//				throw new SEDALibException("Panic: Can't create a default preferences file, stop");
//			}
//				
//		} catch (Exception e) {
//			throw new SEDALibException("Panic: Can't extract a default preferences file, stop");
//		}
//		else {
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
			globalNode.flush();
		} catch (Exception e) {
			throw new SEDALibException("Panic: Can't create a default preferences file, stop");
		}
//		}
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
			JOptionPane.showMessageDialog(null,
					"Erreur fatale, impossible de manipuler les préférences \n->" + e.getMessage());
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
}
