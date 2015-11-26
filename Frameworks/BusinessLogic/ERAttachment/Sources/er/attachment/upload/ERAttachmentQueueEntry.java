package er.attachment.upload;

import java.io.File;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;

import er.attachment.model.ERAttachment;

/**
 * The <code>ERAttachmentQueueEntry</code> is a wrapper that keeps a reference
 * to the <code>ERAttachment</code> and the file being enqueued for uploading.
 *
 * @author <a href="mailto:hprange@gmail.com.br">Henrique Prange</a>
 *
 * @param <T>
 *            the type of the attachment that can queued for uploading.
 *
 * @see ERRemoteAttachment
 * @see ERAttachmentUploadQueue
 */
public class ERAttachmentQueueEntry<T extends ERAttachment & ERRemoteAttachment> {
    private File _uploadedFile;
    private EOGlobalID _attachmentID;

    public ERAttachmentQueueEntry(File uploadedFile, EOGlobalID attachmentID) {
        _uploadedFile = uploadedFile;
        _attachmentID = attachmentID;
    }

    public File uploadedFile() {
        return _uploadedFile;
    }

    @SuppressWarnings("unchecked")
    public T attachment(EOEditingContext editingContext) {
        return (T) editingContext.faultForGlobalID(_attachmentID, editingContext);
    }
}
