package er.mootools.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.ajax.AjaxSortOrder;
import er.extensions.foundation.ERXValueUtilities;

public class ERMTD2WCombinedTableHeader extends AjaxSortOrder {
    
	public ERMTD2WCombinedTableHeader(WOContext context) {
        super(context);
    }
    
    public D2WContext d2wContext() {
    	return (D2WContext)valueForBinding("d2wContext");
    }    

	public String toggleLinkClass() {
		String stateLabel = "Uns";
		int state = currentState();
		if (state == SortedAscending) {
			stateLabel = "Asc";
		} else if (state == SortedDescending){
			stateLabel = "Des";
		}
		return stateLabel;
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
	
	@Override
	public boolean caseInsensitive() {
		return ERXValueUtilities.booleanValue(d2wContext().valueForKey("sortCaseInsensitive"));
	}
    
    
}