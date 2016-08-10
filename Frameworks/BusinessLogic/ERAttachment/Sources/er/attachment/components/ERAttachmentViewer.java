package er.attachment.components;

import com.webobjects.appserver.WOContext;

import er.attachment.components.viewers.AbstractERAttachmentViewer;
import er.attachment.components.viewers.ERAttachmentDefaultViewer;
import er.attachment.model.ERAttachment;
import er.attachment.utils.ERMimeType;
import er.extensions.foundation.ERXProperties;

/**
 * <p>
 * ERAttachmentViewer provides a way to drop in an embedded viewer for
 * attachments.  Viewers can be specified with properties.  For example,
 * if you want to define the viewer for PDF's, you can set 
 * er.attachment.mimeType.image/pdf.viewer=com.mine.PDFViewer or you can set
 * er.attachment.mimeType.image/*.viewer=com.mine.DefaultImageViewer.  To 
 * override the default fallback viewer, set
 * er.attachment.mimeType.default.viewer=com.mine.DefaultViewer.  If an
 * attachment is unavailable (for instance, if it is in the queue to be 
 * sent to S3, but it's not uploaded yet), you can set
 * er.attachment.mimeType.unavailable.viewer=com.mine.UnavailableViewer.
 * </p>
 * <p>
 * There are defaults provided for several attachment types.
 * </p> 
 *  
 * @binding attachment the attachment to display
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding class (optional) the css class
 * @binding id (optional) the html element id
 * @binding style (optional) the embedded css style
 * @binding width (optional) if displaying an image, sets the image width 
 * @binding height (optional) if displaying an image, sets the image height
 *
 * @property er.attachment.mimeType.[mimeType].viewer the class name of the viewer component for the given mime type
 * @property er.attachment.mimeType.[globMimeType].viewer
 * @property er.attachment.mimeType.unavailable.viewer
 * @property er.attachment.mimeType.default.viewer
 *
 * @author mschrag
 */
public class ERAttachmentViewer extends AbstractERAttachmentViewer {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public ERAttachmentViewer(WOContext context) {
    super(context);
  }

  /**
   * @return the class name of the viewer to use for the given mime type.
   */
  public String viewerClassName() {
    String viewerClassName = null;

    ERAttachment attachment = attachment();
    if (attachment != null) {
      if (attachment.available().booleanValue()) {
        ERMimeType mimeType = attachment.erMimeType();
        if (mimeType != null) {
          viewerClassName = ERXProperties.stringForKey("er.attachment.mimeType." +  mimeType.mimeType() +".viewer");
          if (viewerClassName == null) {
            viewerClassName = ERXProperties.stringForKey("er.attachment.mimeType." +  mimeType.globMimeType().mimeType() +".viewer");
          }
        }
      }
      else {
        viewerClassName = ERXProperties.stringForKey("er.attachment.mimeType.unavailable.viewer");
      }
    }

    if (viewerClassName == null) {
      viewerClassName = ERXProperties.stringForKeyWithDefault("er.attachment.mimeType.default.viewer", ERAttachmentDefaultViewer.class.getName());
    }

    return viewerClassName;
  }
}
