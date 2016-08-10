package er.mootools.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEmbeddedComponent;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

public class ERMTD2WEditRelationship extends D2WEmbeddedComponent {

	/**
	 *  100% based upon ERMDEditRelationship
	 * 
	 * @author jlmiller
	 *
	 */
	public ERMTD2WEditRelationship(WOContext context) {
        super(context);
    }

	public NSArray<Object> masterObjectAndRelationshipKey() {
    	return new NSArray<Object>(new Object[]{masterObject(), relationshipKey()});
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