package er.attachment.components;

import com.webobjects.appserver.WOContext;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.attachment.utils.ERMimeType;
import er.extensions.components.ERXStatelessComponent;

/**
 * ERAttachmentIcon displays a linked icon image that represents the file
 * type of the attachment.
 *  
 * @author mschrag
 * @binding size the icon size - 16, 32, or 64
 * @binding attachment the attachment to display
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding target (optional) specifies where to open the linked attachment
 */
public class ERAttachmentIcon extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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