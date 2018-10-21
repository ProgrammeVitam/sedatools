/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.prefs.Preferences;

/**
 * The Class SIPImportContext.
 */
public class DIPImportContext extends CreationContext {

	/**
	 * Instantiates a new SIP import context.
	 */
	public DIPImportContext() {
		this(null, null);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param onDiskInput the on disk input
	 * @param workDir the work dir
	 */
	public DIPImportContext(String onDiskInput, String workDir) {
		super (onDiskInput, workDir);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param globalNode the global node
	 */
	public DIPImportContext(Preferences globalNode) {
		super(globalNode);
	}
}
