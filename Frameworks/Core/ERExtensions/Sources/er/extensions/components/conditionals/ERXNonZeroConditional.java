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
 * Conditional component that tests if a given Number
 * object is non-zero.
 * @binding condition numeric condition to test.
 * @binding negate inverts the sense of the conditional.
 */
public class ERXNonZeroConditional extends ERXWOConditional {

	public ERXNonZeroConditional(String aName, NSDictionary aDict, WOElement aElement) {
		super(aName, aDict, aElement);
	}
	
	@Override
	protected boolean conditionInComponent(WOComponent component) {
		Object value = _condition.valueInComponent(component);
		if (value instanceof Number) {
			Number num = (Number) value;
			return num.intValue() != 0;
		}
		return false;
	}
}
