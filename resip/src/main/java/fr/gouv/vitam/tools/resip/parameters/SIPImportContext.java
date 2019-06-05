/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

/**
 * The Class SIPImportContext.
 */
public class SIPImportContext extends CreationContext {

	/**
	 * Instantiates a new SIP import context.
	 */
	public SIPImportContext() {
		this(null, null);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param onDiskInput the on disk input
	 * @param workDir the work dir
	 */
	public SIPImportContext(String onDiskInput, String workDir){
		super (onDiskInput, workDir);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param prefs the prefs
	 */
	public SIPImportContext(Prefs prefs) {
		super(prefs);
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(Prefs)
	 */
	public void toPrefs(Prefs prefs) {
	}
}
