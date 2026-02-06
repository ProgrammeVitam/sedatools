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
package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.formattools.TikaExtractor;
import fr.gouv.vitam.tools.mailextractlib.nodes.ArchiveUnit;
import fr.gouv.vitam.tools.mailextractlib.utils.DateRange;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static fr.gouv.vitam.tools.mailextractlib.utils.MailExtractProgressLogger.doProgressLog;

/**
 * Utility class to encapsulate an attachment file with content and metadata.
 */
public class StoreAttachment {

    /**
     * Father Store Element of the attachment
     **/
    protected StoreElement fatherElement;

    /**
     * Attachment content.
     */
    protected Object attachmentContent;

    /**
     * Attachment store scheme or null if only an attachment file.
     */
    protected String attachmentStoreScheme;

    /**
     * Name.
     */
    protected String name;

    /**
     * File dates.
     */
    protected Date creationDate, modificationDate;

    /**
     * Type of attachment *.
     */
    protected String mimeType;

    /**
     * Content-ID *.
     */
    protected String contentID;

    /**
     * Attachment type.
     */
    protected int attachmentType;

    // /** Macro types of attachment. */

    /**
     * The Constant FILE_ATTACHMENT.
     */
    public static final int FILE_ATTACHMENT = 0x00;

    /**
     * The Constant INLINE_ATTACHMENT.
     */
    public static final int INLINE_ATTACHMENT = 0x01;

    /**
     * The Constant STORE_ATTACHMENT.
     */
    public static final int STORE_ATTACHMENT = 0x02;

    /**
     * Instantiates a new attachment with binary content.
     *
     * <p>
     * The MimeType is normalized to application/* if type unknown and application/octet-stream if all unknown
     * </p>
     *
     * @param fatherElement         the father element
     * @param storeContent          Object to be used by the store extractor or byte[] if simple            binary
     * @param attachmentStoreScheme Store scheme defining store extractor or "file" if simple            binary
     * @param name                  Name
     * @param creationDate          Creation Date
     * @param modificationDate      Last modification Date
     * @param mimeType              MimeType
     * @param contentID             Mime multipart content ID useful for inline
     * @param attachmentType        Type of attachment (inline, simple file, another store...)
     */
    public StoreAttachment(
        StoreElement fatherElement,
        Object storeContent,
        String attachmentStoreScheme,
        String name,
        Date creationDate,
        Date modificationDate,
        String mimeType,
        String contentID,
        int attachmentType
    ) {
        this.fatherElement = fatherElement;
        this.attachmentContent = storeContent;
        this.attachmentStoreScheme = attachmentStoreScheme;
        this.name = name;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.contentID = contentID;
        this.attachmentType = attachmentType;
        setMimeType(mimeType);
    }

    /**
     * Gets the raw attachment content.
     *
     * @return the raw attachment content
     * @throws MailExtractLibException the extraction exception
     */
    public byte[] getRawAttachmentContent() throws MailExtractLibException {
        if (attachmentContent instanceof byte[]) return (byte[]) attachmentContent;
        else throw new MailExtractLibException("mailextractlib: this attachment has no binary form", null);
    }

    /**
     * Gets the scheme.
     *
     * @return the scheme
     */
    public String getScheme() {
        return attachmentStoreScheme;
    }

    /**
     * Gets the store content, either byte[] or a specific objet treated by the extractor.
     *
     * @return the store content
     */
    public Object getStoreContent() {
        return attachmentContent;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the store content, either byte[] or a specific objet treated by the extractor.
     *
     * @param attachmentContent the new store content
     */
    public void setStoreContent(Object attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    /**
     * Sets the mime type.
     *
     * @param mimeType the new mime type
     */
    public void setMimeType(String mimeType) {
        // verify valid MimeType and replace if not
        try {
            new MimeType(mimeType);
            this.mimeType = mimeType;
        } catch (MimeTypeParseException e) {
            int i = mimeType.lastIndexOf('/');
            if ((i != -1) && (i < mimeType.length())) this.mimeType = "application/" + mimeType.substring(i + 1);
            else this.mimeType = "application/octet-stream";
        }
    }

    /**
     * Sets store scheme and qualify the attachment as a store which can be extracted.
     *
     * @param scheme the scheme
     */
    public void setStoreScheme(String scheme) {
        attachmentStoreScheme = scheme;
        attachmentType = StoreAttachment.STORE_ATTACHMENT;
    }

    /**
     * Detect embedded store attachments not identified during parsing.
     * <p>
     * It use for this, the list of mimetypes that can be treated by known store
     * extractors. This list is constructed using
     * {@link StoreExtractor#addExtractionRelation
     * StoreExtractor.addExtractionRelation}**, and a default one is set calling
     * {@link StoreExtractor#initDefaultExtractors
     * StoreExtractor.initDefaultExtractors}**
     *
     * @param attachments the attachments
     */
    protected static void detectStoreAttachments(List<StoreAttachment> attachments) {
        String mimeType;

        if (attachments != null && !attachments.isEmpty()) {
            for (StoreAttachment a : attachments) {
                if (
                    (a.attachmentType != StoreAttachment.STORE_ATTACHMENT) &&
                    (a.attachmentContent instanceof byte[]) &&
                    // special case for ms-tnef attachments "winmail.dat" because tika can identify them as rfc822
                    // when part of it is mail
                    (!a.mimeType.toLowerCase().equals("application/ms-tnef") &&
                        (!a.mimeType.toLowerCase().equals("application/vnd.ms-tnef")))
                ) {
                    try {
                        mimeType = TikaExtractor.getInstance().getMimeType(a.getRawAttachmentContent());
                        if (mimeType == null) continue;
                        for (String mt : StoreExtractor.mimeTypeSchemeMap.keySet()) {
                            if (mimeType.equals(mt)) {
                                a.setStoreScheme(StoreExtractor.mimeTypeSchemeMap.get(mt));
                                break;
                            }
                        }
                    } catch (MailExtractLibException e) {
                        // forget it
                    }
                }
            }
        }
    }

    /**
     * Extract all attachments as children node of a specific ArchiveUnit node.
     * <p>
     * It also extract recursively store attachment if any.
     *
     * @param attachments the attachments
     * @param messageNode the message node
     * @param writeFlag   the write flag
     * @throws MailExtractLibException the mail extract lib exception
     * @throws InterruptedException    the interrupted exception
     */
    public static void extractAttachments(
        List<StoreAttachment> attachments,
        ArchiveUnit messageNode,
        boolean writeFlag
    ) throws MailExtractLibException, InterruptedException {
        if (attachments != null) {
            for (StoreAttachment a : attachments) {
                // message identification
                if (a.attachmentType == StoreAttachment.STORE_ATTACHMENT) {
                    // recursive extraction of a message in attachment...
                    doProgressLog(
                        a.fatherElement.getProgressLogger(),
                        MailExtractProgressLogger.MESSAGE_DETAILS,
                        "mailextractlib: attached message extraction",
                        null
                    );
                    if (a.extractAsStoreAttachment(messageNode, writeFlag)) continue;
                    doProgressLog(
                        a.fatherElement.getProgressLogger(),
                        MailExtractProgressLogger.MESSAGE_DETAILS,
                        "mailextractlib: can't extract as an attached message, extract as standard attachment",
                        null
                    );
                }
                // standard attachment file
                a.extractAsFileOrInlineAttachment(messageNode, writeFlag);
            }
        }
    }

    /**
     * Extract a file or inline message attachment.
     */
    private void extractAsFileOrInlineAttachment(ArchiveUnit node, boolean writeFlag)
        throws MailExtractLibException, InterruptedException {
        ArchiveUnit attachmentNode;
        StoreExtractor fatherExtractor = fatherElement.getStoreExtractor();

        if ((name == null) || name.isEmpty()) name = "[Vide]";
        attachmentNode = new ArchiveUnit(fatherExtractor, node, "Attachment", name);
        attachmentNode.addMetadata("DescriptionLevel", "Item", true);
        attachmentNode.addMetadata("Title", name, true);
        attachmentNode.addMetadata("Description", "Document \"" + name + " joint", true);

        // get the max of creation and modification date which define the
        // creation date of the present file
        // (max for correcting a current confusion between theese two dates)
        Date date = null;
        if (creationDate != null) {
            if (modificationDate != null) date = (creationDate.compareTo(modificationDate) > 0
                    ? creationDate
                    : modificationDate);
            else date = creationDate;
        } else if (modificationDate != null) date = modificationDate;
        if (date != null) attachmentNode.addMetadata("CreatedDate", DateRange.getISODateString(creationDate), true);

        // Raw object extraction
        if (name.endsWith(".lnk")) name = name + ".txt"; // break windows shortcuts
        attachmentNode.addObject(getRawAttachmentContent(), name, "BinaryMaster", 1);

        // Text object extraction
        String textExtract = null;
        if (fatherExtractor.options.extractFileTextFile || fatherExtractor.options.extractFileTextMetadata) try {
            textExtract = TikaExtractor.getInstance().extractTextFromBinary(getRawAttachmentContent());
        } catch (MailExtractLibException ee) {
            doProgressLog(
                fatherExtractor.getProgressLogger(),
                MailExtractProgressLogger.MESSAGE_DETAILS,
                "mailextractlib: can't extract text content from attachment " + name,
                ee
            );
        }
        // put in file
        if (fatherExtractor.options.extractFileTextFile && (!((textExtract == null) || textExtract.trim().isEmpty()))) {
            attachmentNode.addObject(textExtract.getBytes(StandardCharsets.UTF_8), name + ".txt", "TextContent", 1);
        }
        // put in metadata
        if (fatherExtractor.options.extractFileTextMetadata && (!((textExtract == null) || textExtract.isEmpty()))) {
            attachmentNode.addLongMetadata("TextContent", textExtract, true);
        }

        if (writeFlag) attachmentNode.write();
    }

    /**
     * Extract a store attachment
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean extractAsStoreAttachment(ArchiveUnit node, boolean writeFlag)
        throws MailExtractLibException, InterruptedException {
        StoreExtractor fatherExtractor = fatherElement.getStoreExtractor();
        Boolean isContainerScheme = false;
        StoreExtractor extractor = null;

        doProgressLog(
            fatherExtractor.getProgressLogger(),
            MailExtractProgressLogger.MESSAGE_DETAILS,
            "mailextractlib: begin " +
            attachmentStoreScheme +
            " store content extraction from an attachment in " +
            fatherElement.getLogDescription(),
            null
        );
        Class storeExtractorClass = StoreExtractor.schemeStoreExtractorClassMap.get(attachmentStoreScheme);
        if (storeExtractorClass == null) {
            fatherElement.logMessageWarning(
                "mailextractlib: unknown embedded store type=" +
                attachmentStoreScheme +
                " , extracting unit in path " +
                node.getFullName(),
                null
            );
        } else {
            isContainerScheme = StoreExtractor.schemeContainerMap.get(attachmentStoreScheme);
            if (isContainerScheme) {
                node = new ArchiveUnit(fatherExtractor, node, "Container", (name == null ? "Infile" : name));
                node.addMetadata("DescriptionLevel", "Item", true);
                node.addMetadata(
                    "Title",
                    "Conteneur " + attachmentStoreScheme + (name == null ? "" : " " + name),
                    true
                );
                node.addMetadata(
                    "Description",
                    "Extraction d'un conteneur " + attachmentStoreScheme + (name == null ? "" : " " + name),
                    true
                );
            }
            try {
                extractor = (StoreExtractor) storeExtractorClass
                    .getConstructor(
                        StoreAttachment.class,
                        ArchiveUnit.class,
                        StoreExtractorOptions.class,
                        StoreExtractor.class,
                        StoreElement.class,
                        MailExtractProgressLogger.class
                    )
                    .newInstance(
                        this,
                        node,
                        fatherExtractor.options,
                        fatherExtractor,
                        fatherElement,
                        fatherElement.getProgressLogger()
                    );
            } catch (InvocationTargetException e) {
                Throwable te = e.getCause();
                fatherElement.logMessageWarning(
                    "mailextractlib: dysfonctional embedded store type=" +
                    attachmentStoreScheme +
                    " , extracting unit in path " +
                    node.getFullName(),
                    te
                );
            } catch (Exception e) {
                fatherElement.logMessageWarning(
                    "mailextractlib: dysfonctional embedded store type=" +
                    attachmentStoreScheme +
                    " , extracting unit in path " +
                    node.getFullName(),
                    e
                );
            }
        }
        if (extractor != null) {
            try {
                extractor.writeTargetLog();
                extractor.getRootFolder().extractFolderAsRoot(writeFlag);
                fatherExtractor.accumulateSubElements(extractor);
                extractor.endStoreExtractor();
                if (extractor.getRootFolder().getDateRange().isDefined() && isContainerScheme) {
                    node.addMetadata(
                        "StartDate",
                        DateRange.getISODateString(extractor.getRootFolder().getDateRange().getStart()),
                        true
                    );
                    node.addMetadata(
                        "EndDate",
                        DateRange.getISODateString(extractor.getRootFolder().getDateRange().getEnd()),
                        true
                    );
                }
                if (writeFlag) node.write();
                doProgressLog(
                    fatherExtractor.getProgressLogger(),
                    MailExtractProgressLogger.MESSAGE_DETAILS,
                    "mailextractlib: end " +
                    attachmentStoreScheme +
                    " store content extraction from an attachment in " +
                    fatherElement.getLogDescription(),
                    null
                );
            } catch (MailExtractLibException ee) {
                doProgressLog(
                    fatherExtractor.getProgressLogger(),
                    MailExtractProgressLogger.MESSAGE_DETAILS,
                    "mailextractlib: can't extract " +
                    attachmentStoreScheme +
                    " store content from an attachment in " +
                    fatherElement.getLogDescription(),
                    ee
                );
            }
        }
        return (extractor != null);
    }
}
