/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.prefs.Preferences;

/**
 * The Class SIPImportContext.
 */
public class CSVMetadataImportContext extends CreationContext {

	/**
	 * Instantiates a new SIP import context.
	 */
	public CSVMetadataImportContext() {
		this(null, null);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param onDiskInput the on disk input
	 * @param workDir the work dir
	 */
	public CSVMetadataImportContext(String onDiskInput, String workDir) {
		super (onDiskInput, workDir);
	}

	/**
	 * Instantiates a new SIP import context.
	 *
	 * @param globalNode the global node
	 */
	public CSVMetadataImportContext(Preferences globalNode) {
		super(globalNode);
	}
}
