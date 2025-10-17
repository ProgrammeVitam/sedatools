/*
 *
 */
package fr.gouv.vitam.tools.resip.parameters;

/**
 * The Class CSVImportContext.
 */
public class CSVImportContext extends CreationContext {

// prefs elements
    /**
     * The csv charset name.
     */
    private String csvCharsetName;

    /**
     * The delimiter.
     */
    private char delimiter;

    /**
     * Instantiates a new CSV import context.
     */
    public CSVImportContext() {
        this(null, ';', null, null);
    }

    /**
     * Instantiates a new CSV import context.
     *
     * @param csvCharsetName the csv charset name
     * @param delimiter      the delimiter
     * @param onDiskInput    the on disk input
     * @param workDir        the work dir
     */
    public CSVImportContext(String csvCharsetName, char delimiter, String onDiskInput, String workDir) {
        super(onDiskInput, workDir);
        this.csvCharsetName = csvCharsetName;
        this.delimiter = delimiter;
    }

    /**
     * Instantiates a new CSV import context from preferences.
     *
     * @param preferences the prefs
     */
    public CSVImportContext(Preferences preferences) {
        super(preferences);
        csvCharsetName = preferences.getPrefProperties().getProperty("importContext.csv.charsetName", "");
        if (csvCharsetName.isEmpty()) {
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                this.csvCharsetName = "windows-1252";
            else
                this.csvCharsetName = "UTF-8";
        }

        String tmp = preferences.getPrefProperties().getProperty("importContext.csv.delimiter", "");
        if (tmp.isEmpty())
            delimiter = ';';
        else
            delimiter = tmp.charAt(0);
    }

    /* (non-Javadoc)
     * @see CreationContext#toPrefs(Prefs)
     */
    public void toPrefs(Preferences preferences) {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            preferences.getPrefProperties().setProperty("importContext.csv.charsetName", (csvCharsetName == null ? "windows-1252" : csvCharsetName));
        else
            preferences.getPrefProperties().setProperty("importContext.csv.charsetName", (csvCharsetName == null ? "UTF-8" : csvCharsetName));
        preferences.getPrefProperties().setProperty("importContext.csv.delimiter", (delimiter == '\0' ? ";" : Character.toString(delimiter)));
    }

    /* (non-Javadoc)
     * @see CreationContext#setDefaultPrefs()
     */
    public void setDefaultPrefs() {
        super.setDefaultPrefs();
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            this.csvCharsetName = "windows-1252";
        else
            this.csvCharsetName = "UTF-8";
        this.delimiter = ';';
    }

    /**
     * Gets delimiter.
     *
     * @return the delimiter
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Sets delimiter.
     *
     * @param delimiter the delimiter
     */
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Gets csv charset name.
     *
     * @return the csv charset name
     */
    public String getCsvCharsetName() {
        return csvCharsetName;
    }

    /**
     * Sets csv charset name.
     *
     * @param csvCharsetName the csv charset name
     */
    public void setCsvCharsetName(String csvCharsetName) {
        this.csvCharsetName = csvCharsetName;
    }
}
