/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitam.tools.mailextractlib.core;

import fr.gouv.vitam.tools.mailextractlib.utils.ExtractionException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.util.Date;

/**
 * Utility class to encapsulate an attachment file with content and metadata.
 */
public class StoreMessageAttachment {

    /** Attachment content. */
    Object attachmentContent;

    /** Attachment store scheme or null if only an attachment file. */
    String attachmentStoreScheme;

    /** Name. */
    String name;

    /** File dates. */
    Date creationDate, modificationDate;

    /** Type of attachment *. */
    String mimeType;

    /** Content-ID *. */
    String contentID;

    /** Attachment type. */
    int attachmentType;

    // /** Macro types of attachment. */

    /** The Constant FILE_ATTACHMENT. */
    public static final int FILE_ATTACHMENT = 0x00;

    /** The Constant INLINE_ATTACHMENT. */
    public static final int INLINE_ATTACHMENT = 0x01;

    /** The Constant STORE_ATTACHMENT. */
    public static final int STORE_ATTACHMENT = 0x02;

    /**
     * Instantiates a new attachment with binary content.
     *
     * <p>
     * The MimeType is normalized to application/* if type unknown and application/octet-stream if all unknown
     * </p>
     *
     * @param storeContent
     *            Object to be used by the store extractor or byte[] if simple
     *            binary
     * @param attachmentStoreScheme
     *            Store scheme defining store extractor or "file" if simple
     *            binary
     * @param name
     *            Name
     * @param creationDate
     *            Creation Date
     * @param modificationDate
     *            Last modification Date
     * @param mimeType
     *            MimeType
     * @param contentID
     *            Mime multipart content ID useful for inline
     * @param attachmentType
     *            Type of attachment (inline, simple file, another store...)
     */

    public StoreMessageAttachment(Object storeContent, String attachmentStoreScheme, String name, Date creationDate,
                                  Date modificationDate, String mimeType, String contentID, int attachmentType) {
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
     * @throws ExtractionException
     *             the extraction exception
     */
    public byte[] getRawAttachmentContent() throws ExtractionException {
        if (attachmentContent instanceof byte[])
            return (byte[]) attachmentContent;
        else
            throw new ExtractionException("mailextract: this attachment has no binary form");
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
     * @param name
     *            the new name
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
     * @param attachmentContent
     *            the new store content
     */
    public void setStoreContent(Object attachmentContent) {
        this.attachmentContent = attachmentContent;
    }

    /**
     * Sets the mime type.
     *
     * @param mimeType
     *            the new mime type
     */
    public void setMimeType(String mimeType) {
        // verify valid MimeType and replace if not
        try {
            new MimeType(mimeType);
            this.mimeType = mimeType;
        } catch (MimeTypeParseException e) {
            int i = mimeType.lastIndexOf('/');
            if ((i != -1) && (i < mimeType.length()))
                this.mimeType = "application/" + mimeType.substring(i + 1);
            else
                this.mimeType = "application/octet-stream";
        }
    }

}
