/*
 * 
 */
package fr.gouv.vitam.tools.resip.parameters;

/**
 * The Class CSVTreeImportContext.
 */
public class CSVTreeImportContext extends CSVImportContext {

	/**
	 * Instantiates a new csv tree import context.
	 */
	public CSVTreeImportContext() {
		this(null, ';', null, null);
	}

	/**
	 * Instantiates a new csv tree import context.
	 *
	 * @param csvCharsetName the csv charset name
	 * @param delimiter      the delimiter
	 * @param onDiskInput    the on disk input
	 * @param workDir        the work dir
	 */
	public CSVTreeImportContext(String csvCharsetName, char delimiter, String onDiskInput, String workDir) {
		super(csvCharsetName, delimiter, onDiskInput, workDir);
	}

	/**
	 * Instantiates a new csv tree import context from preferences.
	 *
	 * @param prefs the prefs
	 */
	public CSVTreeImportContext(Prefs prefs) {
		super(prefs);
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(Prefs)
	 */
	public void toPrefs(Prefs prefs) {
	}
}
