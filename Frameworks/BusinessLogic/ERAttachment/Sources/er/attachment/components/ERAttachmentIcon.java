package er.attachment.components;

import com.webobjects.appserver.WOContext;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.attachment.utils.ERMimeType;
import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXProperties;

/**
 * ERAttachmentIcon displays a linked icon image that represents the file
 * type of the attachment. Icon files path is generated using this pattern:  "icons/" + size + "/" + mimeType + ".png
 * To provide a custom icon file add a property named using this pattern : "er.attachment.icons/" + size + "/" + mimeType + ".png"
 * with a value defined as frameworkName + "." + imagePath like this for an image stored in the app bundle :
 * er.attachment.icons/64/image/jpeg.png=app.images/icons/jpg64.png
 * 
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

	public String iconUrl() {
		String framework = "ERAttachment";
		String iconPath = ERAttachmentIcon.iconPath(attachment(), valueForBinding("size"));
		String replacementIconPath = ERXProperties.stringForKey("er.attachment."+iconPath);
		if (replacementIconPath != null) {
			int dotIndex = replacementIconPath.indexOf('.');
			framework = replacementIconPath.substring(0, dotIndex);
			iconPath = replacementIconPath.substring(dotIndex + 1);
		}  
		String anImageURL = context()._urlForResourceNamed(iconPath, framework, true);
		return anImageURL;
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
}