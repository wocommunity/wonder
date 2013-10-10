package er.directtoweb.components.relationships._ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WCustomQueryComponent;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Adds a filter for the select list.
 * How to use: Set a delayed boolean assignment on restrictedChoiceKey or restrictedFetchSpecification based on the condition isFiltered
 * 
 * @see ERD2WQueryToOneRelationship
 * 
 * 
 * @author mendis
 *
 */
public class ERD2WQueryToOneRelationshipWithFilter extends D2WCustomQueryComponent {
	
	public ERD2WQueryToOneRelationshipWithFilter(WOContext context) {
		super(context);
	}
	
	public String filterKey() {
		return (String) d2wContext().valueForKey("filterKey");
	}

	private boolean isFiltered() {
    	return ERXValueUtilities.booleanValue(d2wContext().valueForKey(filterKey()));
	}
	
	private void setIsFiltered(boolean flag) {
		d2wContext().takeValueForKey(flag, filterKey());
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
	
	public String buttonName() {
		return d2wContext().valueForKey("name") + "_filter_button";
	}

	// actions (ajax)
	public WOComponent toggleFilter() {
		setIsFiltered(!isFiltered());
		return this;
	}
}
