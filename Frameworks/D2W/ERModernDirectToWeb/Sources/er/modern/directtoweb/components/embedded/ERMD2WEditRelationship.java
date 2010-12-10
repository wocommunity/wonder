package er.modern.directtoweb.components.embedded;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEmbeddedComponent;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

/**
 * Use ERMDEditRelationship
 * 
 * @author davidleber
 *
 */
public class ERMD2WEditRelationship extends D2WEmbeddedComponent {
	
    public ERMD2WEditRelationship(WOContext context) {
        super(context);
    }
    
	public NSArray masterObjectAndRelationshipKey() {
    	return new NSArray(new Object[]{masterObject(), relationshipKey()});
    }
    
    @SuppressWarnings("unchecked")
	public void setMasterObjectAndRelationshipKey(NSArray a) {}
    
    public EOEnterpriseObject masterObject() {
    	EOEnterpriseObject obj = (EOEnterpriseObject) valueForBinding("masterObject");
    	return obj;
    }
    
    public String relationshipKey() {
    	String obj = (String) valueForBinding("relationshipKey");
    	return obj;
    }
    
}