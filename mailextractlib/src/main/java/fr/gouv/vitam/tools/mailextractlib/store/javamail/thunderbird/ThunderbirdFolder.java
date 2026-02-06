/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.mailextractlib.store.javamail.thunderbird;

import fr.gouv.vitam.tools.mailextractlib.store.javamail.JMMimeMessage;
import fr.gouv.vitam.tools.mailextractlib.store.javamail.mbox.MboxReader;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import jakarta.mail.*;
import org.eclipse.angus.mail.imap.protocol.BASE64MailboxDecoder;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLogWithoutInterruption;

/**
 * JavaMail Folder for Thunderbird mbox directory/file structure.
 * <p>
 * This is the main class for folder analysis and message slicing.
 * <p>
 * <b>Warning:</b>Only for reading and without file locking or new messages
 * management.
 */
public class ThunderbirdFolder extends Folder {

    private String folderFullName; // is root if name is null
    private int total; // total number of messages in mailbox
    private volatile boolean opened = false;
    private int holdsFlags;
    private List<MessageFork> messages;
    private ThunderbirdStore mstore;
    private File folderFile;
    private MboxReader mboxfilereader;
    private MailExtractProgressLogger logger;

    private class MessageFork {

        long beg, end;

        MessageFork(long beg, long end) {
            this.beg = beg;
            this.end = end;
        }
    }

    /**
     * Sets the logger
     * <p>
     * This method is directly called from MailExtract library to enable this
     * class to log
     *
     * @param logger
     *            Store extractor logger
     */
    public void setLogger(MailExtractProgressLogger logger) {
        this.logger = logger;
    }

    /**
     * Instantiates a new thunder mbox folder.
     *
     * @param store
     *            Store
     * @param folderFullName
     *            Folder full name
     * @throws MessagingException
     *             Messaging exception from inner JavaMail calls
     */
    // constructors
    public ThunderbirdFolder(ThunderbirdStore store, String folderFullName) throws MessagingException {
        super(store);
        this.mstore = store;

        this.folderFullName = folderFullName;
        holdsFlags = (isBoxHoldingMessages(folderFullName) ? HOLDS_MESSAGES : 0) |
        (isBoxHoldingFolders(folderFullName) ? HOLDS_FOLDERS : 0);
        if (folderFullName != null) {
            folderFile = new File(getFilePathFromFolderFullName(folderFullName));
        }
    }

    /**
     * Instantiates a new thunder mbox folder.
     *
     * @param store
     *            Store
     * @param folderFullName
     *            Folder full name
     * @param holdsFlags
     *            Flags composition of HOLDS_MESSAGES and HOLDS_FOLDERS
     * @throws MessagingException
     *             Messaging exception from inner JavaMail calls
     */
    public ThunderbirdFolder(ThunderbirdStore store, String folderFullName, int holdsFlags) throws MessagingException {
        super(store);
        this.mstore = store;

        this.folderFullName = folderFullName;
        this.holdsFlags = holdsFlags;
        if (folderFullName != null) {
            folderFile = new File(getFilePathFromFolderFullName(folderFullName));
        }
    }

    // folder name/fullname and file name/path conversion functions
    private String getFilePathFromFolderFullName(String folderFullName) {
        return mstore.getContainer() + File.separator + folderFullName.replace(File.separator, ".sbd" + File.separator);
    }

    private String getSubFolderDirectoryPathFromFolderFullName(String folderPath) {
        return (
            mstore.getContainer() +
            File.separator +
            folderPath.replace(File.separator, ".sbd" + File.separator) +
            ".sbd"
        );
    }

    private String getIndexFilePathFromFolderFullName(String folderPath) {
        return (
            mstore.getContainer() +
            File.separator +
            folderPath.replace(File.separator, ".sbd" + File.separator) +
            ".msf"
        );
    }

    private String getFolderNameFromFileName(String fileName) {
        return fileName;
    }

    private String getFolderNameFromIndexFileName(String fileName) {
        if (fileName.endsWith(".msf")) return fileName.substring(0, fileName.length() - 4);
        else return fileName;
    }

    private String getFolderNameFromSubFolderDirectoryName(String fileName) {
        if (fileName.endsWith(".sbd")) return fileName.substring(0, fileName.length() - 4);
        else return fileName;
    }

    private String getFolderFullNameFromPath(String filePath) {
        String result = filePath.substring(mstore.getContainer().length() + 1);
        result = result.replace(".sbd" + File.separator, File.separator);

        return result;
    }

    private String getSubFolderFullName(String folderName) {
        return (this.folderFullName == null ? folderName : this.folderFullName + File.separator + folderName);
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getName()
     */
    @Override
    public String getName() {
        if (folderFullName == null) return "";
        else return BASE64MailboxDecoder.decode(folderFile.getName()); // XXXX
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getFullName()
     */
    @Override
    public String getFullName() {
        if (folderFullName == null) return "";
        else return BASE64MailboxDecoder.decode(folderFullName);
    }

    // test functions used to determine if a folder hold messages and/or
    // subfolders
    private boolean isSubFoldersDirectory(File test) {
        if (!test.exists() || !test.isDirectory()) return false;
        if (!test.getName().endsWith(".sbd")) {
            doProgressLogWithoutInterruption(
                logger,
                MailExtractProgressLogger.WARNING,
                "ThunderMBox: Maybe sub folders directory " + test.getPath() + " without .sbd suffix is ignored",
                null
            );
            return false;
        }
        return true;
    }

    private boolean isThundebirdMboxFile(File test) {
        // first is it a file
        if (!test.exists() || !test.isFile()) return false;
        // then verify that the file content is beginning with "From "
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(test)));
            String firstline = "";
            while (firstline.isEmpty()) {
                firstline = reader.readLine();
                if (firstline == null) break;
                firstline = firstline.trim();
            }
            if (firstline == null || !firstline.startsWith("From")) return false;
        } catch (IOException e) {
            doProgressLogWithoutInterruption(
                logger,
                MailExtractProgressLogger.WARNING,
                "ThunderMBox: Maybe mailbox file " + test.getPath() + " can't be opened and is ignored",
                e
            );
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                // forget it
            }
        }
        // then verify that an index file exists "fileName".msf
        test = new File(test.getPath() + ".msf");
        if (!test.exists()) doProgressLogWithoutInterruption(
            logger,
            MailExtractProgressLogger.WARNING,
            "ThunderMBox: Maybe mailbox file " + test.getPath() + " don't have an index file but is analyzed",
            null
        );
        return true;
    }

    private boolean isThunderIndexFile(File test) {
        if (!test.exists() || !test.isFile()) return false;
        return (test.getName().endsWith(".msf"));
    }

    private boolean isBoxHoldingMessages(String folderFullName) {
        if (folderFullName == null) return false;
        else return isThundebirdMboxFile(new File(getFilePathFromFolderFullName(folderFullName)));
    }

    private boolean isBoxHoldingFolders(String folderFullName) {
        if (folderFullName == null) return true;
        else return isSubFoldersDirectory(new File(getSubFolderDirectoryPathFromFolderFullName(folderFullName)));
    }

    private boolean isBox(String folderPath) {
        if (isThundebirdMboxFile(new File(getFilePathFromFolderFullName(folderPath)))) return true;
        else if (isSubFoldersDirectory(new File(getSubFolderDirectoryPathFromFolderFullName(folderPath)))) return true;
        else if (isThunderIndexFile(new File(getIndexFilePathFromFolderFullName(folderPath)))) return true;
        else return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getSeparator()
     */
    // implement inherited abstract method Folder.getSeparator()
    @Override
    public char getSeparator() {
        return File.separatorChar;
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public Folder[] list(String pattern) throws MessagingException {
        throw new MethodNotSupportedException("ThunderMBox: list with pattern not supported");
    }

    private static void addFlagHashMap(HashMap<String, Integer> hashMap, String name, int flag) {
        Integer oldFlag = hashMap.get(name);
        hashMap.put(name, (oldFlag == null ? flag : oldFlag | flag));
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#list()
     */
    @Override
    public Folder[] list() throws MessagingException {
        String sbdPath;
        Folder[] result = new Folder[0];

        if (folderFullName == null) {
            sbdPath = mstore.getContainer();
        } else {
            sbdPath = folderFile.getPath() + ".sbd";
        }

        File folder = new File(sbdPath);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) return result;

        HashMap<String, Integer> boxes = new HashMap<String, Integer>();
        for (int i = 0; i < listOfFiles.length; i++) {
            // first case directory for sub folders
            if (isSubFoldersDirectory(listOfFiles[i])) {
                addFlagHashMap(boxes, getFolderNameFromSubFolderDirectoryName(listOfFiles[i].getName()), HOLDS_FOLDERS);
                continue;
            }
            // second case thunder mbox file
            if (isThundebirdMboxFile(listOfFiles[i])) {
                addFlagHashMap(boxes, getFolderNameFromFileName(listOfFiles[i].getName()), HOLDS_MESSAGES);
                continue;
            }
            // third case thunder index file
            // the only file existing when the folder is empty
            if (isThunderIndexFile(listOfFiles[i])) {
                addFlagHashMap(boxes, getFolderNameFromIndexFileName(listOfFiles[i].getName()), 0);
                continue;
            }
            // fourth case empty file
            if (listOfFiles[i].isFile() && listOfFiles[i].length() == 0) continue;
            // then garbage and warning
            doProgressLogWithoutInterruption(
                logger,
                MailExtractProgressLogger.WARNING,
                "ThunderMBox: Wrong mailbox file " +
                listOfFiles[i].getName() +
                " in " +
                (folderFullName == null ? "root folder" : "folder " + folderFullName) +
                " is ignored",
                null
            );
        }

        ArrayList<Folder> folders = new ArrayList<Folder>();
        for (Entry<String, Integer> entry : boxes.entrySet()) {
            folders.add(new ThunderbirdFolder(mstore, getSubFolderFullName(entry.getKey()), entry.getValue()));
        }

        result = folders.toArray(result);

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getParent()
     */
    @Override
    public Folder getParent() throws MessagingException {
        if (folderFullName == null) return this;
        else return getFolder(getFolderFullNameFromPath(folderFile.getParent()));
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#exists()
     */
    @Override
    public boolean exists() {
        if (folderFullName == null) return true;
        else {
            return isBox(folderFullName);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getType()
     */
    @Override
    public int getType() {
        return holdsFlags;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getPermanentFlags()
     */
    @Override
    public Flags getPermanentFlags() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#hasNewMessages()
     */
    @Override
    public boolean hasNewMessages() {
        // only read static mbox hierarchy
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getFolder(java.lang.String)
     */
    @Override
    public Folder getFolder(String name) throws MessagingException {
        return new ThunderbirdFolder(mstore, getSubFolderFullName(name));
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean create(int type) throws MessagingException {
        throw new MethodNotSupportedException("ThunderMBox: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        throw new MethodNotSupportedException("ThunderMBox: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public boolean renameTo(Folder f) throws MessagingException {
        throw new MethodNotSupportedException("ThunderMBox: no writing supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#isOpen()
     */
    @Override
    public boolean isOpen() {
        return opened;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#open(int)
     */
    @Override
    public void open(int mode) throws MessagingException {
        if (opened) throw new IllegalStateException("ThunderMBox: Folder " + folderFullName + " is already open");

        if ((getType() & HOLDS_MESSAGES) == 0) throw new MessagingException(
            "Folder " + folderFullName + " cannot contain messages"
        );
        if (!folderFile.exists()) throw new FolderNotFoundException(
            this,
            "ThunderMBox: Folder " + folderFullName + " doesn't exist"
        );
        this.mode = mode;
        switch (mode) {
            case READ_WRITE:
                throw new MethodNotSupportedException("ThunderMBox: no writing supported");
            case READ_ONLY:
            default:
                if (!folderFile.canRead()) throw new MessagingException(
                    "ThunderMBox: open Failure, can't read: " + folderFile.getPath()
                );
                break;
        }

        messages = new ArrayList<MessageFork>();
        MessageFork mf;

        try {
            mboxfilereader = new MboxReader(logger, folderFile);
            opened = true; // now really opened
            long beg, end;

            mboxfilereader.getNextFromLineBeg();
            beg = mboxfilereader.getLastFromLineEnd();
            while (beg != -1) {
                end = mboxfilereader.getNextFromLineBeg();
                mf = new MessageFork(beg, end);
                messages.add(mf);
                beg = mboxfilereader.getLastFromLineEnd();
            }
        } catch (IOException e) {
            throw new MessagingException("ThunderMBox: open failure, can't read: " + folderFile.getPath());
        }

        total = messages.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#close(boolean)
     */
    @Override
    public void close(boolean expunge) throws MessagingException {
        if (!opened) throw new IllegalStateException("ThunderMBox: Folder " + folderFullName + " is not Open");
        messages = null;
        opened = false;
        try {
            mboxfilereader.close();
        } catch (IOException e) {
            // forget it
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getMessageCount()
     */
    @Override
    public int getMessageCount() throws MessagingException {
        if (!opened) return -1;

        return total;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getMessage(int)
     */
    @Override
    public Message getMessage(int msgno) throws MessagingException {
        if (msgno < 1) throw new IndexOutOfBoundsException("message number " + msgno + " < 1"); // message-numbers start at 1
        else if (msgno > total) throw new IndexOutOfBoundsException("message number " + msgno + " > " + total); // Still out of range ? Throw up ...
        Message m;
        // each get regenerate a message with no strong link so that it can be
        // GC
        // optimal for the extraction usage with only one get by message
        m = new JMMimeMessage(
            this,
            mboxfilereader.newStream(messages.get(msgno - 1).beg, messages.get(msgno - 1).end),
            msgno
        );

        return m;
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
        throw new MethodNotSupportedException("ThunderMBox: no writing supported");
    }

    /*
     * Not implemented, cause of no use in mail extract
     */
    @Override
    public Message[] expunge() throws MessagingException {
        throw new MethodNotSupportedException("ThunderMBox: no writing supported");
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.mail.Folder#getURLName()
     */
    @Override
    public URLName getURLName() {
        URLName storeURL = getStore().getURLName();

        return new URLName(
            storeURL.getProtocol(),
            storeURL.getHost(),
            storeURL.getPort(),
            mstore.getContainer() + File.separator + getFullName(),
            storeURL.getUsername(),
            null
            /* no password */
        );
    }
}
