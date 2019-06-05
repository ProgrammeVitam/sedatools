/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

/**
 * The Class DIPImportContext.
 */
public class DIPImportContext extends CreationContext {

	/**
	 * Instantiates a new DIP import context.
	 */
	public DIPImportContext() {
		this(null, null);
	}

	/**
	 * Instantiates a new DIP import context.
	 *
	 * @param onDiskInput the on disk input
	 * @param workDir the work dir
	 */
	public DIPImportContext(String onDiskInput, String workDir) {
		super (onDiskInput, workDir);
	}

	/**
	 * Instantiates a new DIP import context.
	 *
	 * @param prefs the prefs
	 */
	public DIPImportContext(Prefs prefs) {
		super(prefs);
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(Prefs)
	 */
	public void toPrefs(Prefs prefs) {
	}
}
