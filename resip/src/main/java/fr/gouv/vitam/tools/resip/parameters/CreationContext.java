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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * The Class CreationContext.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = DiskImportContext.class, name = "DiskImportContext"),
		@JsonSubTypes.Type(value = ZipImportContext.class, name = "ZipImportContext"),
		@JsonSubTypes.Type(value = CSVImportContext.class, name = "CSVImportContext"),
		@JsonSubTypes.Type(value = CSVTreeImportContext.class, name = "CSVTreeImportContext"),
		@JsonSubTypes.Type(value = CSVMetadataImportContext.class, name = "CSVMetadataImportContext"),
		@JsonSubTypes.Type(value = SIPImportContext.class, name = "SIPImportContext"),
		@JsonSubTypes.Type(value = DIPImportContext.class, name = "DIPImportContext"),
		@JsonSubTypes.Type(value = MailImportContext.class, name = "MailImportContext") })
public class CreationContext {

// prefs elements
    /**
     * The work dir.
     */
    String workDir;

// session elements
    /**
     * The on disk input.
     */
    String onDiskInput;

    /**
     * The summary.
     */
    String summary;

    /**
     * The structure changed.
     */
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
     * @param workDir     the work dir
     */
    public CreationContext(String onDiskInput, String workDir) {
		this.onDiskInput = onDiskInput;
		this.workDir = workDir;
		this.summary = null;
		this.structureChanged = false;
	}

    /**
     * Instantiates a new creation context from preferences.
     *
     * @param prefs the prefs
     */
    public CreationContext(Prefs prefs) {
		workDir = prefs.getPrefProperties().getProperty("importContext.workDir", "");
		try {
			 Paths.get(workDir);
		}
		catch (InvalidPathException e){
			workDir="";
		}
		if (workDir.isEmpty())
				workDir = Prefs.getDefaultWorkDir();
		onDiskInput = null;
		summary = null;
		structureChanged = false;
	}

    /**
     * Put in preferences the values specific of this context class.
     * Values from the upper class are not put in preferences.
     *
     * @param prefs the prefs
     */
    public void toPrefs(Prefs prefs) {
		prefs.getPrefProperties().setProperty("importContext.workDir", (workDir == null ? "" : workDir));
	}

    /**
     * Sets the default prefs.
     */
    public void setDefaultPrefs() {
		workDir = Prefs.getDefaultWorkDir();
		onDiskInput = null;
		summary = null;
		structureChanged = false;
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
     * @param at the at
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

}