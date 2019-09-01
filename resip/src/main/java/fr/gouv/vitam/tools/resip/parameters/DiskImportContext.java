/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class DiskImportContext.
 */
public class DiskImportContext extends CreationContext {

// prefs elements
    /**
     * The ignore pattern list.
     */
    List<String> ignorePatternList;

    /**
     * The No link flag.
     */
    boolean noLinkFlag;

// session element
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
	 * @param prefs the prefs
	 */
	public DiskImportContext(Prefs prefs) {
		super(prefs);
		String ignorePatternsString = prefs.getPrefProperties().getProperty("importContext.disk.ignorePatternList", "");
		if (ignorePatternsString.isEmpty())
			ignorePatternList = new ArrayList<String>();
		else
			ignorePatternList = Arrays.asList(ignorePatternsString.split("\\s*\n\\s*"));
		noLinkFlag=Boolean.parseBoolean(prefs.getPrefProperties().getProperty("importContext.disk.noLinkFlag", "false"));
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(Prefs)
	 */
	public void toPrefs(Prefs prefs) {
		prefs.getPrefProperties().setProperty("importContext.disk.ignorePatternList", String.join("\n", ignorePatternList));
		prefs.getPrefProperties().setProperty("importContext.disk.noLinkFlag",Boolean.toString(noLinkFlag));
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
