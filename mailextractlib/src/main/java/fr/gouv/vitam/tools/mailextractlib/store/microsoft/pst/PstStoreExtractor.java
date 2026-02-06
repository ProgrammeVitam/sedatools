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
package fr.gouv.vitam.tools.mailextractlib.store.microsoft.pst;

import fr.gouv.vitam.tools.javalibpst.PSTException;
import fr.gouv.vitam.tools.javalibpst.PSTFile;
import fr.gouv.vitam.tools.javalibpst.PSTFolder;
import fr.gouv.vitam.tools.mailextractlib.core.StoreElement;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor;
import fr.gouv.vitam.tools.mailextractlib.core.StoreExtractorOptions;
import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * StoreExtractor sub-class for mail boxes extracted through libpst library.
 */
public class PstStoreExtractor extends StoreExtractor {

    /**
     * Subscribes at StoreExtractor level all schemes treated by this specific store extractor.
     * <p>
     * This is in default list.
     */
    public static void subscribeStoreExtractor() {
        addExtractionRelation("application/vnd.ms-outlook-pst", "x-fmt/248","pst", true, PstStoreExtractor.class);
        addExtractionRelation("application/vnd.ms-outlook-pst", "x-fmt/249","pst", true, PstStoreExtractor.class);
    }

    // Attachment to complete with decoded form
    private StoreAttachment attachment;

    /**
     * The store file.
     */
    private File storeFile;

    /**
     * The PST File object.
     */
    private PSTFile pstFile;

    /**
     * Instantiates a new LP store extractor.
     *
     * @param urlString          the url string
     * @param storeFolder        Path of the extracted folder in the account mail box, can be
     *                           null if default root folder
     * @param destPathString     the dest path string
     * @param options            Options (flag composition of CONST_)
     * @param rootStoreExtractor the creating store extractor in nested extraction, or null if
     *                           root one
     * @param logger             logger used
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major
     *                             format problems...)
     */
    public PstStoreExtractor(String urlString, String storeFolder, String destPathString, StoreExtractorOptions options,
                             StoreExtractor rootStoreExtractor, MailExtractProgressLogger logger) throws MailExtractLibException {
        super(urlString, storeFolder, destPathString, options, rootStoreExtractor, null, logger);

        try {
            pstFile = new PSTFile(path);
        } catch (Exception e) {
            throw new MailExtractLibException(
                    "mailextractlib.pst: can't open " + path + ", doesn't exist or is not a pst file", e);
        }

        pstFile.setGlobalCodepage(options.defaultCharsetName);
        PSTFile.setPrintErrors(logger.getDebugFlag());

        ArchiveUnit rootNode = new ArchiveUnit(this, destRootPath, destName);
        PstStoreFolder lPRootMailBoxFolder;

        try {
            PSTFolder pstFolder = findChildFolder(pstFile.getRootFolder(), storeFolder);

            if (pstFolder == null)
                throw new MailExtractLibException(
                        "mailextractlib.pst: can't find the root folder " + storeFolder + " in pst file", null);

            lPRootMailBoxFolder = PstStoreFolder.createRootFolder(this, pstFolder, rootNode);

            setRootFolder(lPRootMailBoxFolder);
        } catch (IOException e) {
            throw new MailExtractLibException("mailextractlib.pst: can't use " + path + " pst file", e);
        } catch (PSTException e) {
            throw new MailExtractLibException("mailextractlib.pst: can't find extraction root folder " + storeFolder, e);
        }
    }

    // create a store temporary file
    private static File writeStoreFile(String dirPath, byte[] byteContent) throws MailExtractLibException {
        File storeFile;

        OutputStream output = null;
        try {
            Files.createDirectories(Paths.get(dirPath));
            storeFile = getStoreTemporaryFile(dirPath);
            output = new BufferedOutputStream(new FileOutputStream(storeFile));
            if (byteContent != null)
                output.write(byteContent);
        } catch (IOException ex) {
            if (dirPath.length() + 8 > 250)
                throw new MailExtractLibException(
                        "mailextractlib.pst: store file extraction illegal destination file (may be too long pathname), extracting unit in path "
                                + dirPath, ex);
            else
                throw new MailExtractLibException(
                        "mailextractlib.pst: store file extraction illegal destination file, extracting unit in path "
                                + dirPath, ex);
        } finally {
            if (output != null)
                try {
                    output.close();
                } catch (IOException e) {
                    throw new MailExtractLibException(
                            "mailextractlib.pst: can't close store file extraction, extracting unit in path " + dirPath, e);
                }
        }

        return (storeFile);
    }

    // fix a store temporary file name
    private static File getStoreTemporaryFile(String destPathString) {
        return new File(destPathString + File.separator + "tmpStore");
    }

    // generate temporary file and create the url to it
    private static String generateFileAndUrl(StoreAttachment attachment, ArchiveUnit rootNode)
            throws MailExtractLibException {
        String result = null;
        File storeFile = writeStoreFile(rootNode.getFullName(), attachment.getRawAttachmentContent());
        try {
            result = "pst://localhost/" + URLEncoder.encode(storeFile.getAbsolutePath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // not possible
        }
        return result;
    }

    private void deleteStoreFileAndContainerAndThrowException(String message, Exception e) throws MailExtractLibException {
        if (storeFile != null) {
            try {
                Files.deleteIfExists(storeFile.toPath());
                File parentDirectory = storeFile.getParentFile();
                if (parentDirectory != null && parentDirectory.isDirectory()) {
                    Files.deleteIfExists(parentDirectory.toPath());
                }
            } catch (IOException ee) {
                throw new MailExtractLibException(message + ", and unable to suppress temporary files ("+ee.getMessage()+")", e);
            }
        }
        throw new MailExtractLibException(message, e);
    }

    /**
     * Instantiates a new LP embedded pst store extractor.
     *
     * @param attachment         the attachment
     * @param rootNode           the ArchiveUnit node representing this container
     * @param options            Options (flag composition of CONST_)
     * @param rootStoreExtractor the creating store extractor in nested extraction, or null if root one
     * @param fatherElement      the father element in nested extraction, or null if root one
     * @param logger             logger used
     * @throws MailExtractLibException Any unrecoverable extraction exception (access trouble, major format problems...)
     */
    public PstStoreExtractor(StoreAttachment attachment, ArchiveUnit rootNode, StoreExtractorOptions options,
                             StoreExtractor rootStoreExtractor, StoreElement fatherElement, MailExtractProgressLogger logger) throws MailExtractLibException {
        super(generateFileAndUrl(attachment, rootNode), "", rootNode.getFullName(), options, rootStoreExtractor, fatherElement, logger);

        this.attachment = attachment;
        this.storeFile = new File(path);

        try {
            pstFile = new PSTFile(path);
        } catch (Exception e) {
            deleteStoreFileAndContainerAndThrowException("mailextractlib.pst: can't open " + path + ", doesn't exist or is not a pst file",e);
        }

        PstStoreFolder lPRootMailBoxFolder;

        try {
            PSTFolder pstFolder = findChildFolder(pstFile.getRootFolder(), "");

            if (pstFolder == null) {
                deleteStoreFileAndContainerAndThrowException("mailextractlib.pst: Can't find the root folder in pst file", null);
            }

            lPRootMailBoxFolder = PstStoreFolder.createRootFolder(this, pstFolder, rootNode);

            setRootFolder(lPRootMailBoxFolder);
        } catch (IOException e) {
            deleteStoreFileAndContainerAndThrowException("mailextractlib.pst: Can't use " + path + " pst file", e);
        } catch (PSTException e) {
            deleteStoreFileAndContainerAndThrowException("mailextractlib.pst: Can't find extraction root folder", e);
        }
    }

    private PSTFolder getNamedSubFolder(PSTFolder father, String folderName) throws PSTException, IOException {
        PSTFolder result = null;
        Vector<PSTFolder> pstFolderChilds = father.getSubFolders();

        for (PSTFolder p : pstFolderChilds) {
            if (p.getDisplayName().equals(folderName)) {
                result = p;
                break;
            }
        }

        return result;
    }

    private PSTFolder findChildFolder(PSTFolder father, String folderFullName) throws PSTException, IOException {
        String regex;
        PSTFolder result = father;

        if ((folderFullName == null) || (folderFullName.isEmpty()))
            return result;
        else {
            regex = File.separator;
            if (regex.equals("\\"))
                regex = "\\\\";
            String[] folderHierarchy = folderFullName.split(regex);
            for (int i = 0; i < folderHierarchy.length; i++) {
                if (!folderHierarchy[i].isEmpty()) {
                    result = getNamedSubFolder(result, folderHierarchy[i]);
                    if (result == null)
                        break;
                }
            }
            return result;
        }
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor#endStoreExtractor()
     */
    @Override
    public void endStoreExtractor() throws MailExtractLibException {
        super.endStoreExtractor();
        try {
            if (pstFile!=null)
                pstFile.close();
        } catch (IOException e) {
            throw new MailExtractLibException("mailextractlib.pst: Can't close temporary file tmpstore", e);
        }
        if ((storeFile != null) &&
                !storeFile.delete())
            throw new MailExtractLibException("mailextractlib.pst: Can't delete temporary file tmpstore", null);
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor#getAttachment()
     */
    @Override
    public StoreAttachment getAttachment() {
        return attachment;
    }

    /* (non-Javadoc)
     * @see fr.gouv.vitam.tools.mailextractlib.core.StoreExtractor#canExtractObjectsLists()
     */
    @Override
    public boolean canExtractObjectsLists() {
        return true;
    }

    /**
     * The Constant PST_MN.
     */
    static final byte[] PST_MN = new byte[]{0x21, 0x42, 0x44, 0x4e};

    /**
     * Gets the verified scheme.
     *
     * @param content the content
     * @return the verified scheme
     */
    public static String getVerifiedScheme(byte[] content) {
        if (hasMagicNumber(content, PST_MN)) {
            return "pst";
        } else
            return null;
    }

}
