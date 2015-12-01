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
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXPatcher;

/**
 * Conditional component that tests if an object can be cast to the given class or interface without causing a ClassCastException.
 *
 * <h3>Synopsis:</h3>
 * <blockquote>object=<i>anObject</i>;className=<i>aClassName2</i>;[negate=<i>aBoolean</i>;]</blockquote>
 *
 * @binding object object to test
 * @binding className class or interface name
 * @binding negate Inverts the sense of the conditional.
 */
public class ERXInstanceOfConditional extends ERXWOConditional {

	protected WOAssociation _object;
	protected WOAssociation _className;

	public ERXInstanceOfConditional(String aName, NSDictionary aDict, WOElement aElement) {
		super(aName, aDict, aElement);
	}

	@Override
	protected void pullAssociations(NSDictionary<String, ? extends WOAssociation> dict) {
		_object = dict.objectForKey("object");
		_className = dict.objectForKey("className");
		if (_object == null || _className == null) {
			throw new WODynamicElementCreationException("className and object must be bound");
		}
	}

	/**
	 * Tests if the bound object is an instance of the class. Note: If the class
	 * is not found a ClassNotFoundException will be thrown via an
	 * NSForwardException.
	 */
	@Override
	protected boolean conditionInComponent(WOComponent component) {
		Object o = _object.valueInComponent(component);
		String className = (String) _className.valueInComponent(component);
		Class c = ERXPatcher.classForName(className);
		if (c == null) {
			throw new NSForwardException(new ClassNotFoundException(className));
		}
		return c.isInstance(o);
	}
}
