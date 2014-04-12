package er.attachment.upload;

import java.io.File;

/**
 * <code>ERRemoteAttachment</code> represents an attachment that should be
 * stored in a remote server (service). This kind of attachment can be uploaded
 * using an <code>ERAttachmentUploadQueue</code>
 *
 * @author <a href="mailto:hprange@gmail.com.br">Henrique Prange</a>
 *
 * @see ERAttachmentUploadQueue
 */
public interface ERRemoteAttachment {
    public File _pendingUploadFile();

    public boolean _isPendingDelete();
}
