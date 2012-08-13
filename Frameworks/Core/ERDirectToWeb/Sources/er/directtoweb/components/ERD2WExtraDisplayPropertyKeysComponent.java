/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.foundation.NSArray;

import er.directtoweb.ERDirectToWeb;

/**
 * A component to encapsulate the repetition over "extra" display property keys.  The property keys
 * are used to resolve the names of components that are to be displayed.  The common examples are
 * <code>aboveDisplayPropertyKeys</code> and <code>belowDisplayPropertyKeys</code>.
 * @binding extraPropertiesKey - the property key property key to use when asking the D2W context 
 * which extra property keys to display
 * @author Travis Cripps
 * @d2wKey componentName
 * @d2wKey propertyKey
 */
public class ERD2WExtraDisplayPropertyKeysComponent extends D2WComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
    
    private String _extraPropertiesKey;
    
    public ERD2WExtraDisplayPropertyKeysComponent(WOContext context) {
        super(context);
    }
    
    /**
     * Determines if D2W component name debugging is enabled.
     * @return true if D2W component name debugging is enabled
     */
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }
    
    /**
     * Gets the property key to use when asking the D2W context which extra property keys to display.
     * @return the property key
     */
    public String extraPropertiesKey() {
        return _extraPropertiesKey;
    }
    
    /**
     * Sets the property key to use when asking the D2W context which extra property keys to display.
     * @param value of the property key to use
     */
    public void setExtraPropertiesKey(String value) {
        _extraPropertiesKey = value;
    }
    
    /**
     * Gets the array of the extra property keys to display.
     * @return the array of property keys
     */
    public NSArray extraDisplayPropertyKeys() {
        return (NSArray)d2wContext().valueForKey(extraPropertiesKey());
    }
    
}