package er.directtoweb.components.relationships._ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.directtoweb.components.relationships._xhtml.ERD2WEditToOneRelationship2;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Adds a filter for the select list.
 * How to use: Set a delayed boolean assignment on restrictedChoiceKey or restrictedFetchSpecification based on the condition isFiltered
 * 
 * @see ERD2WEditToOneRelationship2
 * 
 * @author mendis
 *
 */
public class ERD2WEditToOneRelationshipWithFilter extends ERD2WEditToOneRelationship2 {

	public ERD2WEditToOneRelationshipWithFilter(WOContext context) {
		super(context);
	}
	
	// accessors
	private boolean isFiltered() {
    	return ERXValueUtilities.booleanValue(d2wContext().valueForKey("isFiltered"));
	}
	
	private void setIsFiltered(boolean flag) {
		d2wContext().takeValueForKey(flag, "isFiltered");
	}
	
	public String container() {
		return d2wContext().valueForKey("id") + "_container";
	}
	
	/*
	 * button
	 */
	public String value() {
		return isFiltered() ? "Show All" : "Filter";
	}

	// actions (ajax)
	public WOComponent toggleFilter() {
		setIsFiltered(!isFiltered());
		return this;
	}
}
