package er.extensions.components._xhtml;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXToOneRelationship;

public class ERXToOneRelationship2 extends ERXToOneRelationship {
    public ERXToOneRelationship2(WOContext context) {
        super(context);
    }
    
    // accessors
    /*
     * support for Prototype and Selenium
     */
    @Override
    public Object theCurrentValue() {
    	Object theCurrentValue = null;
    	
    	try {
    		theCurrentValue = super.theCurrentValue();
    	} catch (Exception e) {
    		theCurrentValue = "Not found";
    		log.error("" + e.getMessage());
    	} return theCurrentValue;
    }
}