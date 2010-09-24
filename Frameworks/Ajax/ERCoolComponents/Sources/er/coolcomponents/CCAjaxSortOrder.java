package er.coolcomponents;

import com.webobjects.appserver.WOContext;

import er.ajax.AjaxSortOrder;

/**
 * Modern AjaxSortOrder derivative, designed to be styled via CSS
 * 
 * @binding updateContainerID the container to refresh after sorting 
 * @binding d2wContext
 * @binding displayGroup
 * @binding key
 * 
 * @author davidleber
 *
 */
public class CCAjaxSortOrder extends AjaxSortOrder {
	
    public CCAjaxSortOrder(WOContext context) {
        super(context);
    }
    
	public String toggleLinkClass() {
		String stateLabel = "Uns";
		int state = currentState();
		if (state == SortedAscending) {
			stateLabel = "Asc";
		} else if (state == SortedDescending){
			stateLabel = "Des";
		}
		return "SrtOrderLink SrtOrder" + stateLabel;
	}

	public String toggleStringValue() {
		String result = "-";
		int state = currentState();
		if (state == SortedAscending) {
			result = "&darr;";
		} else if (state == SortedDescending){
			result = "&uarr;";
		}
		return result;
	}
}