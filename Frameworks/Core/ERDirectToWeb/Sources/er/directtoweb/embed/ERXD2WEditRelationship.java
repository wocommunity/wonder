package er.directtoweb.embed;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEmbeddedComponent;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.directtoweb.delegates.ERD2WEmbeddedComponentActionDelegate;

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

	/**
     * Overridden to support serialization
     */
    @Override
    public NextPageDelegate newPageDelegate() {
    	return ERD2WEmbeddedComponentActionDelegate.instance;
    }
    
    /**
     * Causes errors when using deserialized components in 5.4.3
     */
    public void awake() {}
}