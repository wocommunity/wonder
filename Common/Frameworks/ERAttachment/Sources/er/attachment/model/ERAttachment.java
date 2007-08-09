package er.attachment.model;

import java.io.File;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.processors.ERAttachmentProcessor;
import er.attachment.utils.ERMimeType;
import er.attachment.utils.ERMimeTypeManager;
import er.extensions.ERXFileUtilities;

/**
 * ERAttachment is the superclass of all attachment types.  An attachment object
 * encapsulates a small amount of metadata and the information necessary to
 * construct a url or a stream onto the attachment data.
 * 
 * @author mschrag
 */
public abstract class ERAttachment extends _ERAttachment {
  private static Logger log = Logger.getLogger(ERAttachment.class);

  public ERAttachment() {
  }

  /**
   * Returns the file name portion of the webPath.
   * 
   * @return the file name portion of the webPath
   */
  public String fileName() {
    return new File(webPath()).getName();
  }
  
  /**
   * Returns the ERMimeType that corresponds to the mimeType.
   * 
   * @return the ERMimeType that corresponds to the mimeType
   */
  public ERMimeType erMimeType() {
    return ERMimeTypeManager.mimeTypeManager().mimeTypeForMimeTypeString(mimeType(), false);
  }
  
  /**
   * Returns the file extension of this attachment, first checking the mime type, 
   * then returning the actual extension.
   *  
   * @return the file extension of this attachment
   */
  public String extension() {
    String ext;
    ERMimeType mimeType = erMimeType();
    if (mimeType == null) {
      ext = ERXFileUtilities.fileExtension(originalFileName()).toLowerCase();
    }
    else {
      ext = mimeType.primaryExtension();
    }
    return ext;
  }
  
  /**
   * Fetches the required attachment associated with the given web path.
   * 
   * @param editingContext the editing context to load in
   * @param webPath the web path of the attachment
   * @return the attachment
   * @throws NoSuchElementException if there is no attachment with the given web path
   */
  public static ERAttachment fetchRequiredAttachmentWithWebPath(EOEditingContext editingContext, String webPath) {
    ERAttachment attachment = ERAttachment.fetchRequiredERAttachment(editingContext, ERAttachment.WEB_PATH_KEY, webPath);
    return attachment;
  }
  
  @Override
  public void didDelete(EOEditingContext ec) {
    super.didDelete(ec);
    try {
      ERAttachmentProcessor.processorForType(this).deleteAttachment(this);
    }
    catch (Throwable e) {
      ERAttachment.log.error("Failed to delete attachment '" + primaryKey() + "'.", e);
    }
  }
}
