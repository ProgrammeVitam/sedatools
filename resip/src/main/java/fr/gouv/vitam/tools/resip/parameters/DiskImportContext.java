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

	/**
	 * The ignore pattern list.
	 */
	List<String> ignorePatternList;

	/**
	 * The No link flag.
	 */
	boolean noLinkFlag;

	/**
	 * The model version.
	 */
	int modelVersion;

	/**
	 * Instantiates a new disk import context.
	 */
	public DiskImportContext() {
		this(null, false, null, null);
	}

	/**
	 * Instantiates a new disk import context.
	 *
	 * @param ignorePatternList the ignore pattern list
	 * @param noLinkFlag        the no link flag
	 * @param onDiskInput       the on disk input
	 * @param workDir           the work dir
	 */
	public DiskImportContext(List<String> ignorePatternList, boolean noLinkFlag, String onDiskInput, String workDir) {
		super(onDiskInput, workDir);
		if (ignorePatternList == null)
			this.ignorePatternList = new ArrayList<String>();
		else
			this.ignorePatternList = ignorePatternList;
		this.noLinkFlag=noLinkFlag;
	}

	/**
	 * Instantiates a new disk import context.
	 *
	 * @param globalNode the global node
	 */
	public DiskImportContext(Preferences globalNode) {
		super(globalNode);
		Preferences contextNode = globalNode.node("DiskImportContext");
		String ignorePatternsString = contextNode.get("ignorePatternList", "");
		if (ignorePatternsString.isEmpty())
			ignorePatternList = new ArrayList<String>();
		else
			ignorePatternList = Arrays.asList(ignorePatternsString.split("\\s*\n\\s*"));
		noLinkFlag=contextNode.getBoolean("noLinkFlag", false);
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(java.util.prefs.Preferences)
	 */
	public void toPrefs(Preferences globalNode) throws BackingStoreException {
		super.toPrefs(globalNode);
		Preferences contextNode = globalNode.node("DiskImportContext");
		contextNode.put("ignorePatternList", String.join("\n", ignorePatternList));
		contextNode.putBoolean("noLinkFlag",noLinkFlag);
		contextNode.flush();
	}

	/* (non-Javadoc)
	 * @see CreationContext#setDefaultPrefs()
	 */
	public void setDefaultPrefs() {
		super.setDefaultPrefs();
		ignorePatternList = Arrays.asList("Thumbs.db", "pagefile.sys");
		noLinkFlag=false;
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
	 * Is no link flag boolean.
	 *
	 * @return the boolean
	 */
	public boolean isNoLinkFlag() {
		return noLinkFlag;
	}

	/**
	 * Sets no link flag.
	 *
	 * @param noLinkFlag the no link flag
	 */
	public void setNoLinkFlag(boolean noLinkFlag) {
		this.noLinkFlag = noLinkFlag;
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
