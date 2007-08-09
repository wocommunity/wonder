package er.attachment.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.attachment.utils.ERMimeType;

/**
 * ERAttachmentIcon displays a linked icon image that represents the file
 * type of the attachment.
 *  
 * @author mschrag
 * @binding size the icon size - 16, 32, or 64
 * @binding attachment the attachment to display
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 */
public class ERAttachmentIcon extends WOComponent {
  public ERAttachmentIcon(WOContext context) {
    super(context);
  }

  public ERAttachment attachment() {
    return (ERAttachment) valueForBinding("attachment");
  }

  public String attachmentUrl() {
    WOContext context = context();
    ERAttachment attachment = attachment();
    return ERAttachmentProcessor.processorForType(attachment).attachmentUrl(attachment, context.request(), context);
  }

  public String iconPath() {
    return ERAttachmentIcon.iconPath(attachment(), valueForBinding("size"));
  }

  public static String iconPath(ERAttachment attachment, Object size) {
    String sizeStr = "16";
    if (size != null) {
      sizeStr = size.toString();
    }
    ERMimeType erMimeType = null;
    if (attachment != null) {
      erMimeType = attachment.erMimeType();
    }
    String mimeType;
    if (erMimeType == null) {
      mimeType = "application/x-octet-stream";
    }
    else {
      mimeType = erMimeType.mimeType();
    }
    String iconPath = "icons/" + sizeStr + "/" + mimeType + ".png";
    return iconPath;
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}