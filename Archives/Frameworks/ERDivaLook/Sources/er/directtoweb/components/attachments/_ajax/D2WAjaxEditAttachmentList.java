package er.directtoweb.components.attachments._ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WComponent;

import er.attachment.model.ERAttachment;
import er.extensions.appserver.ERXResponse;

/**
 * D2W component for editing toMany ERAttachments
 * The configurationName is computed: 'Entity.propertyKey'
 * 
 * @author mendis
 *
 */
public class D2WAjaxEditAttachmentList extends D2WComponent {
	public ERAttachment attachment;
	
    public D2WAjaxEditAttachmentList(WOContext context) {
        super(context);
    }
    
    // accessors
    public String configurationName() {
    	return object().entityName() + "." + propertyKey();
    }
    
    public void setAttachment(ERAttachment anAttachment) {
    	attachment = anAttachment;
       	object().addObjectToBothSidesOfRelationshipWithKey(attachment, propertyKey());
    }
    
    public String container() {
    	return (String) d2wContext().valueForKey("id") + "_container";
    }
    
    public String onComplete() {
    	return "function() { new Ajax.Updater('" + container() + "', $('" + container() + "').getAttribute('ref'), {evalScripts:true}); }";
    }
    
    // actions    
    public WOActionResults removeAttachment() {
    	object().removeObjectFromBothSidesOfRelationshipWithKey(attachment, propertyKey());		
		return new ERXResponse();
    }
}
