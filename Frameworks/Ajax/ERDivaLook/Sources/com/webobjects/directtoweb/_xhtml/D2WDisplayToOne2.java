package com.webobjects.directtoweb._xhtml;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayToOne;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

public class D2WDisplayToOne2 extends D2WDisplayToOne {
    public D2WDisplayToOne2(WOContext context) {
        super(context);
    }
    
    // accessors 
    public String classString() {
    	String classString = (String) d2wContext().valueForKey("class");
    	return classString != null ? ERXStringUtilities.safeIdentifierName(classString) : null;
    }
}
