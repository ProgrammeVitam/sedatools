/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.prefs.Preferences;

/**
 * The Class SIPImportContext.
 */
public class CSVTreeImportContext extends CreationContext {

	/**
	 * Instantiates a new SIP import context.
	 */
	public CSVTreeImportContext() {
		this(null, null);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param onDiskInput the on disk input
	 * @param workDir the work dir
	 */
	public CSVTreeImportContext(String onDiskInput, String workDir) {
		super (onDiskInput, workDir);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param globalNode the global node
	 */
	public CSVTreeImportContext(Preferences globalNode) {
		super(globalNode);
	}
}
