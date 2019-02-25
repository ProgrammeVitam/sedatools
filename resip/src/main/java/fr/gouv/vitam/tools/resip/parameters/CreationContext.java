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

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The Class CreationContext.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @Type(value = DiskImportContext.class, name = "DiskImportContext"),
	@Type(value = SIPImportContext.class, name = "SIPImportContext"),
	@Type(value = DIPImportContext.class, name = "DIPImportContext"),
	@Type(value = MailImportContext.class, name = "MailImportContext") })
public class CreationContext {

	// general elements
	/** The work dir. */
	// prefs elements
	String workDir;

	/** The on disk input. */
	// session elements
	String onDiskInput;
	
	/** The summary. */
	String summary;
	
	/** The structure changed. */
	boolean structureChanged;
	
	/**
	 * Instantiates a new creation context.
	 */
	public CreationContext() {
		this(null, null);
	}

	/**
	 * Instantiates a new creation context.
	 *
	 * @param onDiskInput the on disk input
	 * @param workDir the work dir
	 */
	public CreationContext(String onDiskInput, String workDir) {
		this.onDiskInput = onDiskInput;
		this.workDir = workDir;
		this.summary = null;
		this.structureChanged = false;
	}

	/**
	 * Instantiates a new creation context.
	 *
	 * @param globalNode the global node
	 */
	public CreationContext(Preferences globalNode) {
		Preferences contextNode = globalNode.node("CreationContext");
		workDir = contextNode.get("workDir", "");
		if (workDir.isEmpty()) {
			if (System.getProperty("os.name").toLowerCase().contains("win"))
				workDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Resip";
			else
				workDir = System.getProperty("user.home") + File.separator + ".Resip";
		}
		this.onDiskInput = null;
		this.summary = null;
		this.structureChanged = false;
	}

	/**
	 * To prefs.
	 *
	 * @param globalNode the global node
	 * @throws BackingStoreException the backing store exception
	 */
	public void toPrefs(Preferences globalNode) throws BackingStoreException {
		Preferences contextNode = globalNode.node("CreationContext");
		contextNode.put("workDir", (workDir == null ? "" : workDir));
		contextNode.flush();
	}

	/**
	 * Sets the default prefs.
	 */
	public void setDefaultPrefs() {
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			workDir = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "Resip";
		else
			workDir = System.getProperty("user.home") + File.separator + ".Resip";

	}

	// Getters and setters

	/**
	 * Gets the work dir.
	 *
	 * @return the work dir
	 */
	public String getWorkDir() {
		return workDir;
	}

	/**
	 * Sets the work dir.
	 *
	 * @param workDir the new work dir
	 */
	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	/**
	 * Gets the on disk input.
	 *
	 * @return the on disk input
	 */
	public String getOnDiskInput() {
		return onDiskInput;
	}

	/**
	 * Sets the on disk input.
	 *
	 * @param onDiskInput the new on disk input
	 */
	public void setOnDiskInput(String onDiskInput) {
		this.onDiskInput = onDiskInput;
	}

	/**
	 * Gets the actualised summary.
	 *
	 * @return the actualised summary
	 */
	@JsonIgnore
	public String getActualisedSummary(DataObjectPackage at) {
		if (structureChanged) {
			String result=(summary==null?"":summary+"\n") + "Structure modifiée\n";
			result += at.getDescription();
			return result;
		}
		else
			return summary + "\nPas de modification";
	}

	/**
	 * Gets the summary.
	 *
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Sets the summary.
	 *
	 * @param summary the new summary
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Checks if is structure changed.
	 *
	 * @return true, if is structure changed
	 */
	public boolean isStructureChanged() {
		return structureChanged;
	}

	/**
	 * Sets the structure changed.
	 *
	 * @param structureChanged the new structure changed
	 */
	public void setStructureChanged(boolean structureChanged) {
		this.structureChanged = structureChanged;
	}

//	/**
//	 * Adds the counts.
//	 *
//	 * @param archiveUnitCount the archive unit count
//	 * @param dataObjectGroupCount the data object group count
//	 * @param binaryDataObjectCount the binary data object count
//	 * @param physicalDataObjectCount the physical data object count
//	 * @param dataObjectsTotalSize the data objects total size
//	 */
//	public void addCounts(int archiveUnitCount, int dataObjectGroupCount, int binaryDataObjectCount,
//			int physicalDataObjectCount, long dataObjectsTotalSize) {
//		this.archiveUnitCount+=archiveUnitCount;
//		this.dataObjectGroupCount+=dataObjectGroupCount;
//		this.binaryDataObjectCount+=binaryDataObjectCount;
//		this.physicalDataObjectCount+=physicalDataObjectCount;
//		this.dataObjectsTotalSize+=dataObjectsTotalSize;
//	}
}