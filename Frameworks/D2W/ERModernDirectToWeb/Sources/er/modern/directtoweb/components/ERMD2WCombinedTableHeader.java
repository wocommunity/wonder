package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.ajax.AjaxSortOrder;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Combined AjaxSortOrder and propertyKey display for list table header cells.
 * Displays the propertyKey as a link that when clicked sorts the column
 * alternating between not sorted, ascending, and descending.
 * 
 * @binding displayGroup
 * @binding updateContainerID
 * 
 * @d2wKey localContext
 * @d2wKey displayNameForProperty
 * @d2wKey sortKeyForList
 * @d2wKey sortCaseInsensitive
 * @d2wKey propertyIsSortable
 * 
 * @author davidleber
 *
 */
public class ERMD2WCombinedTableHeader extends AjaxSortOrder {
	
	
    public ERMD2WCombinedTableHeader(WOContext context) {
        super(context);
    }
    
    public D2WContext localContext() {
    	return (D2WContext)valueForBinding("localContext");
    }
    
    public D2WContext d2wContext() {
    	return localContext();
    }
    
	public String toggleLinkClass() {
		String stateLabel = "Uns";
		int state = currentState();
		if (state == SortedAscending) {
			stateLabel = "Asc";
		} else if (state == SortedDescending){
			stateLabel = "Des";
		}
		return "ComboTHLink ComboTHLink" + stateLabel;
	}
	
	
	
	// OVERRIDES
	
	@Override
	public String displayKey() {
		return (String)d2wContext().valueForKey("displayNameForProperty");
	}
	
	@Override
	public String key() {
		return (String)d2wContext().valueForKey("sortKeyForList");
	}

	//@Override
	public boolean caseInsensitive() {
		return ERXValueUtilities.booleanValue(d2wContext().valueForKey("sortCaseInsensitive"));
	}
    
}