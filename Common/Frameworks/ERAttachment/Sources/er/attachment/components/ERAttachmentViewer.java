package er.attachment.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;

/**
 * ERAttachmentViewer provides a way to drop in an embedded viewer for
 * attachments.  Currently only image/* mime types are supported, but
 * more will be coming.  Additionally, this will eventually add support
 * for requesting variations of images (like "small").
 *  
 * @author mschrag
 * @binding attachment the attachment to display
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding class (optional) the css class
 * @binding id (optional) the html element id
 * @binding style (optional) the embedded css style
 * @binding width (optional) if displaying an image, sets the image width 
 * @binding height (optional) if displaying an image, sets the image height
 * 
 */
public class ERAttachmentViewer extends WOComponent {
  public ERAttachmentViewer(WOContext context) {
    super(context);
  }

  public ERAttachment attachment() {
    return (ERAttachment) valueForBinding("attachment");
  }

  public String attachmentUrl() {
    WOContext context = context();
    ERAttachment attachment = attachment();
    String configurationName = (String) valueForBinding("configurationName");
    return ERAttachmentProcessor.processorForType(attachment).attachmentUrl(attachment, context.request(), context, configurationName);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}