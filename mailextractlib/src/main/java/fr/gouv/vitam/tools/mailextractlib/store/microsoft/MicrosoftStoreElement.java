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
