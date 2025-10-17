/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.List;

/**
 * The Class ZipImportContext.
 */
public class ZipImportContext extends DiskImportContext {

    /**
     * Instantiates a new zip import context.
     */
    public ZipImportContext() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new zip import context.
	 *
	 * @param preferences the prefs
	 */
	public ZipImportContext(Preferences preferences) {
		super(preferences);
		this.noLinkFlag=true;
	}

	/**
     * Instantiates a new zip import context.
     *
     * @param ignorePatternList the ignore pattern list
     * @param onDiskInput       the on disk input
     * @param workDir           the work dir
     */
    public ZipImportContext(List<String> ignorePatternList, String onDiskInput, String workDir) {
    	super(ignorePatternList, true, onDiskInput, workDir);
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(Prefs)
	 */
	public void toPrefs(Preferences preferences) {
	}

	/* (non-Javadoc)
	 * @see CreationContext#setDefaultPrefs()
	 */
	public void setDefaultPrefs() {
		super.setDefaultPrefs();
		noLinkFlag=true;
	}
}
