package er.modern.directtoweb.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.directtoweb.InspectPageInterface;

import er.extensions.foundation.ERXStringUtilities;


/**
 * Simple component to wrap a string attribute with a hyperlinke to take the
 * clicker to an inspect page. Takes the place of an inspect button in a list
 * 
 * @d2wKey propertyKey
 * @d2wKey object
 * 
 * @author davidleber
 *
 */
public class ERMD2WInspectLink extends D2WDisplayString {
	
    public ERMD2WInspectLink(WOContext context) {
        super(context);
    }

	public WOActionResults inspectAction() {
		InspectPageInterface ipi = D2W.factory().inspectPageForEntityNamed(object().entityName(), session());
		ipi.setObject(object());
		ipi.setNextPage(context().page());
		return (WOActionResults)ipi;
	}

	public String inspectLinkClass() {
		return "InspectLink " + ERXStringUtilities.capitalize(propertyKey()) + "InspectLink";
	}
    
}