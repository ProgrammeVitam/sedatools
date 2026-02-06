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
package fr.gouv.vitam.tools.mailextractlib.store.microsoft;

import fr.gouv.vitam.tools.mailextractlib.core.StoreAttachment;
import fr.gouv.vitam.tools.mailextractlib.core.StoreElement;

import java.util.ArrayList;
import java.util.List;

import static fr.gouv.vitam.tools.mailextractlib.store.microsoft.MicrosoftStoreMessageAttachment.*;

public interface MicrosoftStoreElement {

    String getEmbeddedMessageScheme();

    // try to get the best attachment name
    static String getAttachementFilename(MicrosoftStoreMessageAttachment nativeAttachment) {
        String result;

        result = nativeAttachment.longFilename;
        if (result.isEmpty())
            result = nativeAttachment.filename;
        if (result.isEmpty())
            result = nativeAttachment.displayName;

        return result;
    }

    // utility function to extract attachment from different types of microsoft container (pst message, pst appointment, msg)
    static List<StoreAttachment> getAttachments(StoreElement element, MicrosoftStoreMessageAttachment[] nativeAttachments) throws InterruptedException {
        List<StoreAttachment> result = new ArrayList<StoreAttachment>();
        int attachmentNumber;
        try {
            attachmentNumber = nativeAttachments.length;
        } catch (Exception e) {
            element.logMessageWarning("mailextractlib.microsoft: can't determine attachment list", e);
            attachmentNumber = 0;
        }
        for (int i = 0; i < attachmentNumber; i++) {
            try {
                StoreAttachment attachment;

                switch (nativeAttachments[i].attachMethod) {
                    case ATTACHMENT_METHOD_NONE:
                        break;
                    // TODO OLE case you can access the IStorage object through
                    // IAttach::OpenProperty(PR_ATTACH_DATA_OBJ, ...)
                    case ATTACHMENT_METHOD_OLE:
                        element.logMessageWarning("mailextractlib.microsoft: can't extract OLE attachment", null);
                        break;
                    case ATTACHMENT_METHOD_BY_VALUE:
                        attachment = new StoreAttachment(element,nativeAttachments[i].byteArray, "file",
                                getAttachementFilename(nativeAttachments[i]), nativeAttachments[i].creationTime,
                                nativeAttachments[i].modificationTime, nativeAttachments[i].mimeTag,
                                nativeAttachments[i].contentId, StoreAttachment.INLINE_ATTACHMENT);
                        result.add(attachment);
                        break;
                    case ATTACHMENT_METHOD_BY_REFERENCE:
                    case ATTACHMENT_METHOD_BY_REFERENCE_RESOLVE:
                    case ATTACHMENT_METHOD_BY_REFERENCE_ONLY:
                        // TODO reference cases
                        element.logMessageWarning("mailextractlib.microsoft: can't extract reference attachment", null);
                        break;
                    case ATTACHMENT_METHOD_EMBEDDED:
                        if (element instanceof MicrosoftStoreElement) {
                            attachment = new StoreAttachment(element, nativeAttachments[i].embeddedMessage,
                                    ((MicrosoftStoreElement)element).getEmbeddedMessageScheme(), getAttachementFilename(nativeAttachments[i]), nativeAttachments[i].creationTime,
                                    nativeAttachments[i].modificationTime, nativeAttachments[i].mimeTag,
                                    nativeAttachments[i].contentId, StoreAttachment.STORE_ATTACHMENT);
                            result.add(attachment);
                        }
                        break;
                }
            } catch (Exception e) {
                element.logMessageWarning("mailextractlib.microsoft: can't get attachment number " + Integer.toString(i), e);
            }
        }
       return result;
    }

}
