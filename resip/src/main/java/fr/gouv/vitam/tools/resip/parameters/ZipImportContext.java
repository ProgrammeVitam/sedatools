/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.ArrayList;
import java.util.Arrays;
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
	 * @param prefs the prefs
	 */
	public ZipImportContext(Prefs prefs) {
		super(prefs);
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
	public void toPrefs(Prefs prefs) {
	}

	/* (non-Javadoc)
	 * @see CreationContext#setDefaultPrefs()
	 */
	public void setDefaultPrefs() {
		super.setDefaultPrefs();
		noLinkFlag=true;
	}
}
