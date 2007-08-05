package er.attachment.model;

import java.io.File;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.utils.ERMimeType;
import er.attachment.utils.ERMimeTypeManager;

public abstract class ERAttachment extends _ERAttachment {
  private static Logger log = Logger.getLogger(ERAttachment.class);

  public ERAttachment() {
  }

  public String fileName() {
    return new File(webPath()).getName();
  }
  
  public ERMimeType erMimeType() {
    return ERMimeTypeManager.mimeTypeManager().mimeTypeForMimeTypeString(mimeType(), false);
  }
  
  public static ERAttachment fetchAttachmentWithWebPath(EOEditingContext editingContext, String webPath) {
    ERAttachment attachment = ERAttachment.fetchRequiredERAttachment(editingContext, ERAttachment.WEB_PATH_KEY, webPath);
    return attachment;
  }
}
