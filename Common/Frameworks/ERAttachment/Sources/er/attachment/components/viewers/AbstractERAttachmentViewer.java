package er.attachment.components.viewers;

import com.webobjects.appserver.WOContext;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.ERXStatelessComponent;

/**
 * AbstractERAttachmentViewer is the superclass of all viewer plugins.
 *  
 * @author mschrag
 * @binding attachment the attachment to display
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding class (optional) the css class
 * @binding id (optional) the html element id
 * @binding style (optional) the embedded css style
 * @binding width (optional) if displaying an image, sets the image width 
 * @binding height (optional) if displaying an image, sets the image height
 */
public abstract class AbstractERAttachmentViewer extends ERXStatelessComponent {

  public AbstractERAttachmentViewer(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public ERAttachment attachment() {
    return (ERAttachment) valueForBinding("attachment");
  }

  public String attachmentUrl() {
    WOContext context = context();
    ERAttachment attachment = attachment();
    return ERAttachmentProcessor.processorForType(attachment).attachmentUrl(attachment, context.request(), context);
  }
}
