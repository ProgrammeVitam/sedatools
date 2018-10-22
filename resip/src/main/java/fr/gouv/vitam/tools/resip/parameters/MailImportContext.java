/*
 *
 */
package fr.gouv.vitam.tools.resip.parameters;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

// TODO: Auto-generated Javadoc

/**
 * The Class MailImportContext.
 */
public class MailImportContext extends CreationContext {

    // mail import elements
    /**
     * The extract message text file.
     */
    // prefs elements
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

    /**
     * The protocol.
     */
    String protocol;

    /**
     * The mail folder.
     */
    // session elements
    String mailFolder;

    /**
     * Instantiates a new mail import context.
     */
    public MailImportContext() {
        this(false, false, false, false, null, null, null);
    }

    /**
     * Instantiates a new mail import context.
     *
     * @param extractMessageTextFile     the extract message text file
     * @param extractMessageTextMetadata the extract message text metadata
     * @param extractAttachmentTextFile  the extract attachment text file
     * @param extractAttachementMetadata the extract attachement metadata
     * @param protocol                   the protocol
     * @param onDiskInput                the on disk input
     * @param workDir                    the work dir
     */
    public MailImportContext(boolean extractMessageTextFile,
                             boolean extractMessageTextMetadata,
                             boolean extractAttachmentTextFile,
                             boolean extractAttachementMetadata, String protocol, String onDiskInput, String workDir) {
        super(onDiskInput, workDir);
        this.extractMessageTextFile = extractMessageTextFile;
        this.extractMessageTextMetadata = extractMessageTextMetadata;
        this.extractAttachmentTextFile = extractAttachmentTextFile;
        this.extractAttachmentTextMetadata = extractAttachementMetadata;
        this.protocol = protocol;
        this.mailFolder = "";
    }

    /**
     * Instantiates a new mail import context.
     *
     * @param globalNode the global node
     */
    public MailImportContext(Preferences globalNode) {
        super(globalNode);
        Preferences contextNode = globalNode.node("resiptMailImportContext");
        this.extractMessageTextFile = contextNode.getBoolean("extractMessageTextFile", false);
        this.extractMessageTextMetadata = contextNode.getBoolean("extractMessageTextMetadata", true);
        this.extractAttachmentTextFile = contextNode.getBoolean("extractAttachmentTextFile", true);
        this.extractAttachmentTextMetadata = contextNode.getBoolean("extractAttachmentTextMetadata", false);
        this.protocol = contextNode.get("protocol", "thunderbird");
        this.mailFolder = "";
    }

    /* (non-Javadoc)
     * @see CreationContext#toPrefs(java.util.prefs.Preferences)
     */
    public void toPrefs(Preferences globalNode) throws BackingStoreException {
        super.toPrefs(globalNode);
        Preferences contextNode = globalNode.node("resiptMailImportContext");
        contextNode.putBoolean("extractMessageTextFile", extractMessageTextFile);
        contextNode.putBoolean("extractMessageTextMetadata", extractMessageTextMetadata);
        contextNode.putBoolean("extractAttachmentTextFile", extractAttachmentTextFile);
        contextNode.putBoolean("extractAttachmentTextMetadata", extractAttachmentTextMetadata);
        contextNode.put("protocol", (protocol == null ? "" : protocol));
        contextNode.flush();
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
        this.protocol = "thunderbird";
        this.mailFolder = "";
    }

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

    // Getters and setters

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
