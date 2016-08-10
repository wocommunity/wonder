/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayBoolean;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Allows the display of strings instead of Yes/No/Unset or checkboxes for boolean values.
 * @d2wKey choicesNames
 */
public class ERD2WCustomDisplayBoolean extends D2WDisplayBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

   public ERD2WCustomDisplayBoolean(WOContext context) {
        super(context);
    }
    
    protected NSArray<String> _choicesNames;
    
    @SuppressWarnings("unchecked")
	public NSArray<String> choicesNames() {
         if (_choicesNames == null)
             _choicesNames = (NSArray<String>)d2wContext().valueForKey("choicesNames");
         return _choicesNames;
     }

    public Object displayString() {
        Object o = objectPropertyValue();
        if(o == null && choicesNames().count() > 2)
            return choicesNames().objectAtIndex(2);
        if(ERXValueUtilities.booleanValue(o))
            return choicesNames().objectAtIndex(0);
        return choicesNames().objectAtIndex(1);
    }

    @Override
    public void reset(){
        super.reset();
        _choicesNames = null;
    }
}
