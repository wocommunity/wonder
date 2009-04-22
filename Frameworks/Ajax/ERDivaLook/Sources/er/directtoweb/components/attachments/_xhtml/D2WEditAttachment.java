package er.directtoweb.components.attachments._xhtml;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WComponent;

import er.attachment.model.ERAttachment;

/**
 * D2W component for editing ERAttachments
 * 
 * @author mendis
 *
 * @bindings object
 * @bindings propertyKey
 */
public class D2WEditAttachment extends D2WComponent {
    public D2WEditAttachment(WOContext context) {
        super(context);
    }
    
    // accessors
    public String configurationName() {
    	return object().entityName() + "." + propertyKey();
    }
    
    public ERAttachment attachment() {
    	return (ERAttachment) objectPropertyValue();
    }
    
    public void setAttachment(ERAttachment attachment) {
    	object().takeValueForKeyPath(attachment, propertyKey());
    }
    
    // actions
    public WOComponent addAttachment() {
    	WOComponent dialog = pageWithName("EditAttachmentDialog");
    	dialog.takeValueForKey(configurationName(), "configurationName");
    	dialog.takeValueForKey(object(), "object");
    	dialog.takeValueForKey(propertyKey(), "key");
    	dialog.takeValueForKey(context().page(), "returnPage");
    	return dialog;
    }
    
    public void removeAttachment() {
    	//ERAttachment attachment = (ERAttachment) objectPropertyValue();
    	//attachment.delete();
    	object().takeValueForKeyPath(null, propertyKey());
    }
}