package er.attachment.components;

import java.net.MalformedURLException;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.attachment.model.ERAttachment;
import er.attachment.processors.ERAttachmentProcessor;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXStringUtilities;

/**
 * ERAttachmentLink is like a WOHyperlink that points to an attachment's contents.
 * 
 * @author mschrag
 * @binding attachment the ERAttachment to link to
 * @binding configurationName (optional) the configuration name for this attachment (see top level documentation)
 * @binding class (optional) the css class
 * @binding id (optional) the html element id
 * @binding style (optional) the css inline style
 * @binding download if true, the attachment will be downloaded instead of inlined
 * @binding target (optional) specifies where to open the linked attachment
 */
public class ERAttachmentLink extends WODynamicGroup {
  private WOAssociation _attachment;
  private WOAssociation _target;
  private WOAssociation _configurationName;
  private NSMutableDictionary<String, WOAssociation> _associations;
  private WOAssociation _download;

  public ERAttachmentLink(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
    super(name, associations, template);
    _associations = associations.mutableClone();
    _attachment = _associations.removeObjectForKey("attachment");
    _download = _associations.removeObjectForKey("download");
    _target = _associations.removeObjectForKey("target");
    if (_attachment == null) {
      throw new WODynamicElementCreationException("<ERAttachmentLink> The 'attachment' binding is required.");
    }
    _configurationName = _associations.removeObjectForKey("configurationName");
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    ERAttachment attachment = (ERAttachment) _attachment.valueInComponent(component);
    String attachmentUrl = "#";
    if (attachment != null) {
      attachmentUrl = ERAttachmentProcessor.processorForType(attachment).attachmentUrl(attachment, context.request(), context);
      if (!attachment.available().booleanValue() || attachment.isNewObject()) {
        response.appendContentString("<span");
        ERXComponentUtilities.appendHtmlAttributes(_associations, response, component);
        response.appendContentString(">");
        super.appendToResponse(response, context);
        response.appendContentString("</span>");
      }
      else {
        response.appendContentString("<a href = \"");
        if (_download != null && _download.booleanValueInComponent(component)) {
          try {
            ERXMutableURL attachmentMutableUrl = new ERXMutableURL(attachmentUrl);
            attachmentMutableUrl.addQueryParameter("attachment", "true");
            attachmentUrl = attachmentMutableUrl.toExternalForm();
          }
          catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create attachment URL.", e);
          }
        }
        response.appendContentString(attachmentUrl);
        response.appendContentString("\"");
        
        if (_target != null) {
          String sTarget = (String) _target.valueInComponent(component);
          if(!ERXStringUtilities.stringIsNullOrEmpty(sTarget)) {      
            response.appendContentString(" target=\"" + sTarget +"\"");
          }
        }      
        
        ERXComponentUtilities.appendHtmlAttributes(_associations, response, component);
        response.appendContentString(">");
        super.appendToResponse(response, context);
        response.appendContentString("</a>");
      }
    }
  }
}