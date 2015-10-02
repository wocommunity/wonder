/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.conditionals;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXEOControlUtilities;

/**
 * Conditional component that compares two objects using the <code>equals</code> method.
 * 
 * @binding value1 first object to compare
 * @binding value2 second object to compare
 * @binding negate Inverts the sense of the conditional.
 */
public class ERXEqualConditional extends ERXWOConditional {

	private WOAssociation _value1;
	private WOAssociation _value2;

    public ERXEqualConditional(String aS, NSDictionary aNsdictionary, WOElement aWoelement) {
		super(aS, aNsdictionary, aWoelement);
	}

    @Override
	protected void pullAssociations(NSDictionary<String, ? extends WOAssociation> dict) {
    	_value1 = dict.objectForKey("value1");
    	_value2 = dict.objectForKey("value2");
    	if(_value1 == null || _value2 == null) {
    		throw new WODynamicElementCreationException("value1 and value2 must both be bound");
    	}
    }
    
    /**
     * Tests for the equality of the two value bindings. First tests a direct
     * <code>==</code> comparison then tests with an <code>equals</code> comparison.
     * @return equality of the two bindings.
     */
    @Override
    public boolean conditionInComponent(WOComponent component) {
        Object v1= _value1.valueInComponent(component);
        Object v2= _value2.valueInComponent(component);
        boolean result;
        if((v1 instanceof EOEnterpriseObject) && (v2 instanceof EOEnterpriseObject)) {
        	result = ERXEOControlUtilities.eoEquals((EOEnterpriseObject)v1, (EOEnterpriseObject)v2);
        } else {
        	result = (v1==v2 || (v1!=null && v2!=null && v1.equals(v2)));
        }
        
        return result;
    }
}
