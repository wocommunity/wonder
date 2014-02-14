package er.attachment.upload;

import java.io.File;

import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.concurrency.ERXAsyncQueue;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXExceptionUtilities;

/**
 * The <code>ERAttachmentUploadQueue</code> is a queue to upload attachments.
 * <p>
 * Only <code>ERRemoteAttachment</code>s can be upload using this queue.
 *
 * @author <a href="mailto:hprange@gmail.com.br">Henrique Prange</a>
 *
 * @param <T>
 *            the type of the attachment that can queued for uploading.
 *
 * @see ERRemoteAttachment
 */
public abstract class ERAttachmentUploadQueue<T extends ERAttachment & ERRemoteAttachment> extends ERXAsyncQueue<ERAttachmentQueueEntry<T>> {
    protected final EOEditingContext _editingContext;
    protected final ERAttachmentProcessor<T> _processor;

    public ERAttachmentUploadQueue(String name, ERAttachmentProcessor<T> processor) {
        super(name);

        _processor = processor;
        _editingContext = ERXEC.newEditingContext();
    }

    /**
     * Adds an attachment to the end of the queue.
     *
     * @param attachment
     *            the attachment to upload
     */
    public void enqueue(T attachment) {
        _editingContext.lock();

        try {
            T localAttachment = ERXEOControlUtilities.localInstanceOfObject(_editingContext, attachment);
            ERAttachmentQueueEntry<T> entry = new ERAttachmentQueueEntry<T>(attachment._pendingUploadFile(), localAttachment);

            enqueue(entry);
        } finally {
            _editingContext.unlock();
        }
    }

    @Override
    public void process(ERAttachmentQueueEntry<T> entry) {
        T attachment = entry.attachment();
        File uploadedFile = entry.uploadedFile();

        if (uploadedFile != null && uploadedFile.exists()) {
            try {
                performUpload(attachment, uploadedFile);

                _editingContext.lock();

                try {
                    attachment.setAvailable(Boolean.TRUE);

                    _editingContext.saveChanges();
                } finally {
                    _editingContext.unlock();
                }

                if (_processor.delegate() != null) {
                    _processor.delegate().attachmentAvailable(_processor, attachment);
                }
            } catch (Throwable t) {
                if (_processor.delegate() != null) {
                    _processor.delegate().attachmentNotAvailable(_processor, attachment, ERXExceptionUtilities.toParagraph(t));
                }

                ERAttachmentProcessor.log.error("Failed to upload '" + uploadedFile + "' to the remote server.", t);
            } finally {
                if (attachment._isPendingDelete()) {
                    uploadedFile.delete();
                }
            }
        } else {
            if (_processor.delegate() != null) {
                _processor.delegate().attachmentNotAvailable(_processor, attachment, "Missing attachment file '" + uploadedFile + "'.");
            }

            ERAttachmentProcessor.log.error("Missing attachment file '" + uploadedFile + "'.");
        }
    }

    /**
     * Perform the upload of the attachment to the remote server.
     *
     * @param attachment
     *            the attachment to upload
     * @param uploadedFile
     *            the file to upload
     */
    protected abstract void performUpload(T attachment, File uploadedFile) throws Exception;
}
