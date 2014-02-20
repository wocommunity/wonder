package er.attachment.components.viewers;

import com.webobjects.appserver.WOContext;

/**
 * ERAttachmentUnavailableViewer is viewer used when the attachment is unavailable.  An
 * attachment may be unavailable if it has not yet been uploaded to the data store.
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
public class ERAttachmentUnavailableViewer extends AbstractERAttachmentViewer {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public ERAttachmentUnavailableViewer(WOContext context) {
    super(context);
  }

}