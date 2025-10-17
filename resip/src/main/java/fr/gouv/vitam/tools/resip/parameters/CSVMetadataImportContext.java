/*
 *
 */
package fr.gouv.vitam.tools.resip.parameters;

/**
 * The Class CSVMetadataImportContext.
 */
public class CSVMetadataImportContext extends CSVImportContext {

    /**
     * Instantiates a new csv metadata import context.
     */
    public CSVMetadataImportContext() {
        this(null, ';', null, null);
    }

    /**
     * Instantiates a new csv metadata import context.
     *
     * @param csvCharsetName the csv charset name
     * @param delimiter      the delimiter
     * @param onDiskInput    the on disk input
     * @param workDir        the work dir
     */
    public CSVMetadataImportContext(String csvCharsetName, char delimiter, String onDiskInput, String workDir) {
        super(csvCharsetName, delimiter, onDiskInput, workDir);
    }

    /**
     * Instantiates a new csv metadata import context from preferences.
     *
     * @param preferences the prefs
     */
    public CSVMetadataImportContext(Preferences preferences) {
        super(preferences);
    }

    /* (non-Javadoc)
     * @see CreationContext#toPrefs(Prefs)
     */
    public void toPrefs(Preferences preferences) {
    }
}
