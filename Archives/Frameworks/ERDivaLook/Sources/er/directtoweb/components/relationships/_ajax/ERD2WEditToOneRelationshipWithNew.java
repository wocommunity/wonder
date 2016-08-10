package er.directtoweb.components.relationships._ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;

import er.directtoweb.components.relationships.ERD2WEditToOneRelationship;
import er.extensions.foundation.ERXValueUtilities;

public class ERD2WEditToOneRelationshipWithNew extends ERD2WEditToOneRelationship {
    public ERD2WEditToOneRelationshipWithNew(WOContext context) {
        super(context);
    }
    
    // actions
    public WOActionResults newAction() {
    	EditPageInterface newAction = (EditPageInterface) D2W.factory().editPageForNewObjectWithConfigurationNamed("AjaxCreate" + destinationEntityName(), session());
    	newAction.setNextPage(context().page());
    	return (WOActionResults) newAction;
    }  
    
    // accessors
    private String destinationEntityName() {
    	return (String) d2wContext().valueForKey("destinationEntityName");
    }
    
    public String onChange() {
    	return isAjax() ? _onChange() : null;
    }
    
    private String _onChange() {
    	return "new Ajax.Updater('" + container() + "', $('" + container() + "').getAttribute('ref'), {parameters:Form.serialize(this.form), evalScripts:true});";
    }
    
    public boolean isAjax() {
    	Object b = d2wContext().valueForKey("isAjax");
    	return b != null ? ERXValueUtilities.booleanValue(b) : false;
    }
    
    private String container() {
    	return (String) d2wContext().valueForKey("updateContainerID");
    }
    
    /*
     * Using same mechanism as EditRelationship page to opt-in for the New... button
     */
    public boolean isEntityEditable() {
    	D2WContext subContext = new D2WContext(session());
    	EOEntity destinationEntity = EOModelGroup.defaultGroup().entityNamed(destinationEntityName());
    	subContext.setEntity(destinationEntity);
    	subContext.setTask("editRelationship");
    	
    	return ERXValueUtilities.booleanValueWithDefault(subContext.valueForKey("isEntityEditable"), !super.isEntityReadOnly(destinationEntity));
    }
    
    @Override
    public void setLocalContext(D2WContext aContext) {
    	if (aContext != null) 
    		_localContext = aContext;
    }
}