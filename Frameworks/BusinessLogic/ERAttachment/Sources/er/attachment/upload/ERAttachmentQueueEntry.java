package er.attachment.upload;

import java.io.File;

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
    private T _attachment;

    public ERAttachmentQueueEntry(File uploadedFile, T attachment) {
        _uploadedFile = uploadedFile;
        _attachment = attachment;
    }

    public File uploadedFile() {
        return _uploadedFile;
    }

    public T attachment() {
        return _attachment;
    }
}
