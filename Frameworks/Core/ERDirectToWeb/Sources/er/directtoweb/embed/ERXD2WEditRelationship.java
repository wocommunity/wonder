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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXD2WEditRelationship(WOContext context) {
		super(context);
	}
	
	public NSArray<Object> masterObjectAndRelationshipKey() {
		return new NSArray<>(masterObject(), relationshipKey());
	}

    public void setMasterObjectAndRelationshipKey(NSArray<Object> a) {}
		
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
    @Override
    public void awake() {}
    
    /**
     * Prevent {@link com.webobjects.foundation.NSKeyValueCoding$UnknownKeyException}
     */
    @SuppressWarnings("unchecked")
    public void handleTakeValueForUnboundKey(Object value, String key) {
        // DO NOTHING
    }
 
}