/*
 *
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The Class CSVMetadataImportContext.
 */
public class CSVMetadataImportContext extends CreationContext {

   /**
     * The csv charset name.
     */
    private String csvCharsetName;

    /**
     * The delimiter.
     */
    private char delimiter;

    /**
     * Instantiates a new SIP import context.
     */
    public CSVMetadataImportContext() {
        this(null, ';', null, null);
    }

    /**
     * Instantiates a new SIP import context.
     *
     * @param csvCharsetName the csv charset name
     * @param delimiter      the delimiter
     * @param onDiskInput    the on disk input
     * @param workDir        the work dir
     */
    public CSVMetadataImportContext(String csvCharsetName, char delimiter, String onDiskInput, String workDir) {
        super(onDiskInput, workDir);
        this.csvCharsetName = csvCharsetName;
        this.delimiter = delimiter;
    }

    /**
     * Instantiates a new SIP import context.
     *
     * @param globalNode the global node
     */
    public CSVMetadataImportContext(Preferences globalNode) {
        super(globalNode);
        Preferences contextNode = globalNode.node("CSVMetadataImportContext");
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            this.csvCharsetName = contextNode.get("csvCharsetName", "windows-1252");
        else
            this.csvCharsetName = contextNode.get("csvCharsetName", "UTF-8");
        this.delimiter = contextNode.get("delimiter", ";").charAt(0);
    }

    /* (non-Javadoc)
     * @see CreationContext#toPrefs(java.util.prefs.Preferences)
     */
    public void toPrefs(Preferences globalNode) throws BackingStoreException {
        super.toPrefs(globalNode);
        Preferences contextNode = globalNode.node("CSVMetadataImportContext");
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            contextNode.put("csvCharsetName", (csvCharsetName == null ? "windows-1252" : csvCharsetName));
        else
            contextNode.put("csvCharsetName", (csvCharsetName == null ? "UTF-8" : csvCharsetName));
        contextNode.put("delimiter", (delimiter == '\0' ? ";" : Character.toString(delimiter)));
        contextNode.flush();
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
