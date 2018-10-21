/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

// TODO: Auto-generated Javadoc
/**
 * The Class DiskImportContext.
 */
public class DiskImportContext extends CreationContext {

	// disk import elements
	/** The ignore pattern list. */
	// prefs elements
	List<String> ignorePatternList;
	
	/** The model version. */
	int modelVersion;

	/**
	 * Instantiates a new disk import context.
	 */
	public DiskImportContext() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new disk import context.
	 *
	 * @param ignorePatternList the ignore pattern list
	 * @param onDiskInput the on disk input
	 * @param workDir the work dir
	 */
	public DiskImportContext(List<String> ignorePatternList, String onDiskInput, String workDir) {
		super(onDiskInput, workDir);
		if (ignorePatternList == null)
			this.ignorePatternList = new ArrayList<String>();
		else
			this.ignorePatternList = ignorePatternList;
	}

	/**
	 * Instantiates a new disk import context.
	 *
	 * @param globalNode the global node
	 */
	public DiskImportContext(Preferences globalNode) {
		super(globalNode);
		Preferences contextNode = globalNode.node("resiptDiskImportContext");
		String ignorePatternsString = contextNode.get("ignorePatternList", "");
		if (ignorePatternsString.isEmpty())
			ignorePatternList = new ArrayList<String>();
		else
			ignorePatternList = Arrays.asList(ignorePatternsString.split("\\s*\n\\s*"));
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(java.util.prefs.Preferences)
	 */
	public void toPrefs(Preferences globalNode) throws BackingStoreException {
		super.toPrefs(globalNode);
		Preferences contextNode = globalNode.node("resiptDiskImportContext");
		contextNode.put("ignorePatternList", String.join("\n", ignorePatternList));
		contextNode.flush();
	}

	/* (non-Javadoc)
	 * @see CreationContext#setDefaultPrefs()
	 */
	public void setDefaultPrefs() {
		super.setDefaultPrefs();
		ignorePatternList = Arrays.asList("Thumbs.db", "pagefile.sys");
	}

	// Getters and setters

	/**
	 * Gets the ignore pattern list.
	 *
	 * @return the ignore pattern list
	 */
	public List<String> getIgnorePatternList() {
		return ignorePatternList;
	}

	/**
	 * Sets the ignore pattern list.
	 *
	 * @param ignorePatternList the new ignore pattern list
	 */
	public void setIgnorePatternList(List<String> ignorePatternList) {
		this.ignorePatternList = ignorePatternList;
	}

	/**
	 * Gets the model version.
	 *
	 * @return the model version
	 */
	public int getModelVersion() {
		return modelVersion;
	}

	/**
	 * Sets the model version.
	 *
	 * @param modelVersion the new model version
	 */
	public void setModelVersion(int modelVersion) {
		this.modelVersion = modelVersion;
	}
}
