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
     * @param preferences the prefs
     */
    public CSVTreeImportContext(Preferences preferences) {
		super(preferences);
	}

	/* (non-Javadoc)
	 * @see CreationContext#toPrefs(Prefs)
	 */
	public void toPrefs(Preferences preferences) {
	}
}
