/*
 *
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The Class MailImportContext.
 */
public class MailImportContext extends CreationContext {

// prefs elements
    /**
     * The extract message text file.
     */
    boolean extractMessageTextFile;

    /**
     * The extract message text metadata.
     */
    boolean extractMessageTextMetadata;

    /**
     * The extract attachment text file.
     */
    boolean extractAttachmentTextFile;

    /**
     * The extract attachment text metadata.
     */
    boolean extractAttachmentTextMetadata;

    /** Set to true to allow external tools for apache-tika text extraction (tesseract, ffmpeg...)  */
    boolean allowsExternalToolsForTextExtraction;

    /**
     * The protocol.
     */
    String protocol;

    /**
     * The default charset name.
     */
    String defaultCharsetName;

// session elements
    /**
     * The mail folder.
     */
    String mailFolder;

    /**
     * Instantiates a new mail import context.
     */
    public MailImportContext() {
        this(false, false, false, false, false, null, null, null, null);
    }

    /**
     * Instantiates a new mail import context.
     *
     * @param extractMessageTextFile     the extract message text file
     * @param extractMessageTextMetadata the extract message text metadata
     * @param extractAttachmentTextFile  the extract attachment text file
     * @param extractAttachementMetadata the extract attachement metadata
     * @param allowsExternalToolsForTextExtraction allow/prevent external tools for apache-tika text extraction (tesseract, ffmpeg...)
     * @param protocol                   the protocol
     * @param defaultCharsetName         the default charset name
     * @param onDiskInput                the on disk input
     * @param workDir                    the work dir
     */
    public MailImportContext(boolean extractMessageTextFile,
                             boolean extractMessageTextMetadata,
                             boolean extractAttachmentTextFile,
                             boolean extractAttachementMetadata,
                             boolean allowsExternalToolsForTextExtraction,
                             String protocol, String defaultCharsetName, String onDiskInput, String workDir) {
        super(onDiskInput, workDir);
        this.extractMessageTextFile = extractMessageTextFile;
        this.extractMessageTextMetadata = extractMessageTextMetadata;
        this.extractAttachmentTextFile = extractAttachmentTextFile;
        this.extractAttachmentTextMetadata = extractAttachementMetadata;
        this.allowsExternalToolsForTextExtraction = allowsExternalToolsForTextExtraction;
        this.protocol = protocol;
        this.mailFolder = "";
        this.defaultCharsetName=defaultCharsetName;
    }

    /**
     * Instantiates a new mail import context from preferences.
     *
     * @param preferences the prefs
     */
    public MailImportContext(Preferences preferences) {
        super(preferences);
        this.extractMessageTextFile = Boolean.parseBoolean(
            preferences.getPrefProperties().getProperty("importContext.mail.extractMessageTextFile", "false"));
        this.extractMessageTextMetadata = Boolean.parseBoolean(
            preferences.getPrefProperties().getProperty("importContext.mail.extractMessageTextMetadata", "true"));
        this.extractAttachmentTextFile = Boolean.parseBoolean(
            preferences.getPrefProperties().getProperty("importContext.mail.extractAttachmentTextFile", "true"));
        this.extractAttachmentTextMetadata = Boolean.parseBoolean(
            preferences.getPrefProperties().getProperty("importContext.mail.extractAttachmentTextMetadata", "false"));
        this.allowsExternalToolsForTextExtraction = Boolean.parseBoolean(preferences.getPrefProperties().getProperty("importContext.mail.allowsExternalToolsForTextExtraction", "false"));
        this.protocol = preferences.getPrefProperties().getProperty("importContext.mail.protocol", "thunderbird");
        this.mailFolder = "";
        this.defaultCharsetName = preferences.getPrefProperties().getProperty("importContext.mail.defaultCharsetName", "windows-1252");
    }

    /* (non-Javadoc)
     * @see CreationContext#toPrefs(Prefs)
     */
    public void toPrefs(Preferences preferences) {
        preferences.getPrefProperties().setProperty("importContext.mail.extractMessageTextFile", Boolean.toString(extractMessageTextFile));
        preferences.getPrefProperties().setProperty("importContext.mail.extractMessageTextMetadata", Boolean.toString(extractMessageTextMetadata));
        preferences.getPrefProperties().setProperty("importContext.mail.extractAttachmentTextFile", Boolean.toString(extractAttachmentTextFile));
        preferences.getPrefProperties().setProperty("importContext.mail.extractAttachmentTextMetadata", Boolean.toString(extractAttachmentTextMetadata));
        preferences.getPrefProperties().setProperty("importContext.mail.allowsExternalToolsForTextExtraction", Boolean.toString(allowsExternalToolsForTextExtraction));
        preferences.getPrefProperties().setProperty("importContext.mail.protocol", (protocol == null ? "" : protocol));
        preferences.getPrefProperties().setProperty("importContext.mail.defaultCharsetName", (defaultCharsetName == null ? "" : defaultCharsetName));
    }

    /* (non-Javadoc)
     * @see CreationContext#setDefaultPrefs()
     */
    public void setDefaultPrefs() {
        super.setDefaultPrefs();
        this.extractMessageTextFile = false;
        this.extractMessageTextMetadata = true;
        this.extractAttachmentTextFile = true;
        this.extractAttachmentTextMetadata = false;
        this.allowsExternalToolsForTextExtraction = false;
        this.protocol = "thunderbird";
        this.mailFolder = "";
        this.defaultCharsetName="windows-1252";
    }

    // Getters and setters

    /**
     * Checks if is extract message text file.
     *
     * @return true, if is extract message text file
     */
    public boolean isExtractMessageTextFile() {
        return extractMessageTextFile;
    }

    /**
     * Sets the extract message text file.
     *
     * @param extractMessageTextFile the new extract message text file
     */
    public void setExtractMessageTextFile(boolean extractMessageTextFile) {
        this.extractMessageTextFile = extractMessageTextFile;
    }

    /**
     * Checks if is extract message text metadata.
     *
     * @return true, if is extract message text metadata
     */
    public boolean isExtractMessageTextMetadata() {
        return extractMessageTextMetadata;
    }

    /**
     * Sets the extract message text metadata.
     *
     * @param extractMessageTextMetadata the new extract message text metadata
     */
    public void setExtractMessageTextMetadata(boolean extractMessageTextMetadata) {
        this.extractMessageTextMetadata = extractMessageTextMetadata;
    }

    /**
     * Checks if is extract attachment text file.
     *
     * @return true, if is extract attachment text file
     */
    public boolean isExtractAttachmentTextFile() {
        return extractAttachmentTextFile;
    }

    /**
     * Sets the extract attachment text file.
     *
     * @param extractAttachmentTextFile the new extract attachment text file
     */
    public void setExtractAttachmentTextFile(boolean extractAttachmentTextFile) {
        this.extractAttachmentTextFile = extractAttachmentTextFile;
    }

    /**
     * Checks if is extract attachment text metadata.
     *
     * @return true, if is extract attachment text metadata
     */
    public boolean isExtractAttachmentTextMetadata() {
        return extractAttachmentTextMetadata;
    }

    /**
     * Sets the extract attachment text metadata.
     *
     * @param extractAttachementTextMetadata the new extract attachment text metadata
     */
    public void setExtractAttachmentTextMetadata(boolean extractAttachementTextMetadata) {
        this.extractAttachmentTextMetadata = extractAttachementTextMetadata;
    }

    /**
     * Checks if external tools (tesseract, ffmpeg...) can be executed on host by tika for text extraction.
     * @return true is external tools are allowed, false otherwise.
     */
    public boolean isAllowsExternalToolsForTextExtraction() {
        return allowsExternalToolsForTextExtraction;
    }

    /**
     * Sets whether external tools (tesseract, ffmpeg...) can be executed on host by tika for text extraction.
     *
     * @param allowsExternalToolsForTextExtraction true is external tools are allowed, false otherwise.
     */
    public void setAllowsExternalToolsForTextExtraction(boolean allowsExternalToolsForTextExtraction) {
        this.allowsExternalToolsForTextExtraction = allowsExternalToolsForTextExtraction;
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol the new protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the mail folder.
     *
     * @return the mail folder
     */
    public String getMailFolder() {
        return mailFolder;
    }

    /**
     * Sets the mail folder.
     *
     * @param mailFolder the new mail folder
     */
    public void setMailFolder(String mailFolder) {
        this.mailFolder = mailFolder;
    }


    /**
     * Gets default charset name.
     *
     * @return the default charset name
     */
    public String getDefaultCharsetName() {
        return defaultCharsetName;
    }

    /**
     * Sets default charset name.
     *
     * @param defaultCharsetName the default charset name
     */
    public void setDefaultCharsetName(String defaultCharsetName) {
        this.defaultCharsetName = defaultCharsetName;
    }

    @Override
    public void setOnDiskInput(String onDiskInput) {
        this.onDiskInput = onDiskInput;
        if (onDiskInput != null) {
            try {
                if (Files.isDirectory(Paths.get(onDiskInput)))
                    setProtocol("thunderbird");
                else if (onDiskInput.endsWith(".pst"))
                    setProtocol("pst");
                else if (onDiskInput.endsWith(".msg"))
                    setProtocol("msg");
                else if (onDiskInput.endsWith(".eml"))
                    setProtocol("eml");
                else
                    setProtocol("mbox");
            } catch (Exception ignored) {
            }
        }
    }
}
