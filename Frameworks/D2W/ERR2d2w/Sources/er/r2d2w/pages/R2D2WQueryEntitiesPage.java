package er.r2d2w.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSArray;

import er.directtoweb.pages.ERD2WQueryEntitiesPage;
import er.extensions.foundation.ERXValueUtilities;

public class R2D2WQueryEntitiesPage extends ERD2WQueryEntitiesPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private D2WContext relationshipContext;
	
    public R2D2WQueryEntitiesPage(WOContext context) {
        super(context);
    }
    
    protected D2WContext relationshipContext() {
    	if(relationshipContext == null) {
    		relationshipContext = ERD2WContext.newContext(session());
        	relationshipContext.setTask("query");
    	}
    	return relationshipContext;
    }
    
    public Boolean hasQueryAttributes() {
    	Boolean result = Boolean.FALSE;
    	D2WContext rc = relationshipContext();
       	rc.setEntity(entity());
    	NSArray<String> propertyKeys = ERXValueUtilities.arrayValueWithDefault(rc.valueForKey(D2WModel.DisplayPropertyKeysKey), NSArray.EmptyArray);
		for(Object key: propertyKeys) {
			String attributeName = (String)key;
			EOAttribute attribute = entity().attributeNamed(attributeName);
			if(attribute!=null) {
				result = Boolean.TRUE;
				break;
			}
		}
    	
    	return result;
    }
}