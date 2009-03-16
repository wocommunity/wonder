package er.attachment.processors;

import er.attachment.model.ERAttachment;

/**
 * A delegate for ERAttachmentProcessors.
 * 
 * @author mschrag
 */
public interface IERAttachmentProcessorDelegate {
  /**
   * Called when an attachment is created (if you want to sneak in and modify the instance).
   *  
   * @param processor the attachment processor
   * @param attachment the attachment
   */
  public void attachmentCreated(ERAttachmentProcessor processor, ERAttachment attachment);
  
  /**
   * Called when an attachment is made available.
   *  
   * @param processor the attachment processor
   * @param attachment the attachment
   */
  public void attachmentAvailable(ERAttachmentProcessor processor, ERAttachment attachment);

  /**
   * Called when an attachment is determined to be unavailable.  This provides the opportunity to clean up
   * the attachment in whatever way is appropriate for your application.  Note: There is currently a failure mode with 
   * this method where it will not be called if the application crashes.  If it is essential that you process all
   * unavailable attachments, you may want to handle that at application startup by selecting all of the available = false
   * attachments and running your custom processing on them.
   *  
   * @param processor the attachment processor
   * @param attachment the attachment
   * @param failureReason the reason why the attachment is not available
   */
  public void attachmentNotAvailable(ERAttachmentProcessor processor, ERAttachment attachment, String failureReason);
}
