/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.conditionals;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOElement;
import com.webobjects.foundation.NSDictionary;

/**
 * Conditional component that tests if a given object 
 * is null.
 * @binding condition object to test for null-value
 * @binding negate inverts the sense of the conditional.
 */
public class ERXNonNullConditional extends ERXWOConditional {

    public ERXNonNullConditional(String aName, NSDictionary aDict, WOElement aElement) {
		super(aName, aDict, aElement);
	}
    
    @Override
    protected boolean conditionInComponent(WOComponent component) {
    	return _condition.valueInComponent(component) != null;
    }
}
