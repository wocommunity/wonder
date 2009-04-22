package er.directtoweb.components.relationships._xhtml;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;

import er.ajax.AjaxUtils;
import er.directtoweb.components.relationships.ERD2WEditToOneRelationship;
import er.diva.ERDIVPageInterface;
import er.diva.pages.ERDIVEditRelationshipPage.Keys;
import er.extensions.foundation.ERXValueUtilities;

public class ERD2WEditToOneRelationship2 extends ERD2WEditToOneRelationship {
    public ERD2WEditToOneRelationship2(WOContext context) {
        super(context);
    }
    
    // accessors
    public String stylesheet() {
    	return (String) d2wContext().valueForKey(ERDIVPageInterface.Keys.Stylesheet);
    }
    
    // actions
    public WOActionResults newAction() {
    	return (WOActionResults) D2W.factory().editPageForNewObjectWithConfigurationNamed("ModalCreate" + destinationEntityName(), session());
    }
    
    private String destinationEntityName() {
    	return (String) d2wContext().valueForKey("destinationEntityName");
    }
    
    /*
     * Using same mechanism as EditRelationship page to opt-in for the New... button
     */
    public boolean isEntityEditable() {
    	D2WContext subContext = new D2WContext(session());
    	EOEntity destinationEntity = EOModelGroup.defaultGroup().entityNamed(destinationEntityName());
    	subContext.setEntity(destinationEntity);
    	
    	return ERXValueUtilities.booleanValueWithDefault(subContext.valueForKey("isEntityEditable"), !super.isEntityReadOnly(destinationEntity));
    }
    
    /*
     * To apply the stylesheet for the modal page
     */
    // R&R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);

    	// add page style sheet
    	if (stylesheet() != null) {
    		AjaxUtils.addStylesheetResourceInHead(context, response, "app", stylesheet());
    	}
    }
}