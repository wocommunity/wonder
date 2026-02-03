package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.foundation.ERXStringUtilities;

public class R2D2WDisplayString extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2D2WDisplayString(WOContext context) {
        super(context);
    }
    
    /**
     * Overridden to provide proper string values for relationships.  If you
     * are getting a null pointer here, you've probably set keyWhenRelationship
     * incorrectly in your rule file.  Your rule should look something like:
     * <br/>(propertyType = 'r' and relationship.destinationEntity.name = 'MyEntity') => keyWhenRelationship = "someAttribute"
     * @return a value for the current property on the current object
     */
    public Object objectPropertyValue() {
    	Object o = super.objectPropertyValue();
    	if(o instanceof EOEnterpriseObject) {
    		EOEnterpriseObject eo = (EOEnterpriseObject)o;
    		String path = d2wContext().keyWhenRelationship();
    		o = eo.valueForKeyPath(path);
    	}
    	return o;
    }

	public boolean omitWrapper() {
		boolean b = true;
		if(objectPropertyValueIsNonNull()) {
			String val = (String)d2wContext().valueForKey("dipslayStringWrapper");
			b = ERXStringUtilities.stringIsNullOrEmpty(val);
		}
		return b;
	}
	
	// TODO add date/number format support for things like rss pubdate
}