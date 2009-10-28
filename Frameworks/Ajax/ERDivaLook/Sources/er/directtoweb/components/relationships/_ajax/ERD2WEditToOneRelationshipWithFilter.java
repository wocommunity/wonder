package er.directtoweb.components.relationships._ajax;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WComponent;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Adds a filter for the select list.
 * How to use: Set a delayed boolean assignment on restrictedChoiceKey or restrictedFetchSpecification based on the condition key defined by filterKey
 * 
 * @see ERD2WEditToOneRelationshipWithNew
 * 
 * @author mendis
 *
 */
public class ERD2WEditToOneRelationshipWithFilter extends D2WComponent {

	public ERD2WEditToOneRelationshipWithFilter(WOContext context) {
		super(context);
	}
	
	// accessors
	public String filterKey() {
		return (String) _localContext.valueForKey("filterKey");
	}
	
	private boolean isFiltered() {
    	return ERXValueUtilities.booleanValue(_localContext.valueForKey(filterKey()));
	}
	
	private void setIsFiltered(boolean flag) {
		_localContext.takeValueForKey(flag, filterKey());
	}

	public String container() {
		return _localContext.valueForKey("id") + "_container";
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
