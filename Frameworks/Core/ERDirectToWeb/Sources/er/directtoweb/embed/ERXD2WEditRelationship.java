package er.directtoweb.embed;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEmbeddedComponent;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

/**
 * A clone of David LeBer's ERMD2WEditRelationship component.
 */
public class ERXD2WEditRelationship extends D2WEmbeddedComponent {
	public ERXD2WEditRelationship(WOContext context) {
		super(context);
	}
	
	public NSArray<Object> masterObjectAndRelationshipKey() {
		return new NSArray<Object>(masterObject(), relationshipKey());
	}
		
	public EOEnterpriseObject masterObject() {
		EOEnterpriseObject obj = (EOEnterpriseObject) valueForBinding("masterObject");
		return obj;
	}
	
	public String relationshipKey() {
		String obj = (String) valueForBinding("relationshipKey");
		return obj;
	}
}