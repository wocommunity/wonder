package er.attachment.components.viewers;

import com.webobjects.appserver.WOContext;

/**
 * ERAttachmentDefaultViewer is the "there is no viewer" viewer.
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
public class ERAttachmentDefaultViewer extends AbstractERAttachmentViewer {

  public ERAttachmentDefaultViewer(WOContext context) {
    super(context);
  }

}