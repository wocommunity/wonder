/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* D2WOgnlAssignment.java created by max on Tue 16-Oct-2001 */
package ognl.webobjects;

import java.lang.reflect.Method;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSForwardException;

public class D2WOgnlAssignment extends Assignment {

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver) {
		return new D2WOgnlAssignment(eokeyvalueunarchiver);
	}

	public D2WOgnlAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public D2WOgnlAssignment(String key, Object value) {
		super(key, value);
	}

	public Object fire(D2WContext c) {
		// MS: This used to be in WOOGNL, but it required everyone who used WOOGNL to
		// bring in all the D2W frameworks.  I've moved this over to ERD2W instead,
		// return WOOgnl.factory().getValue((String) value(), c);
		try {
			Class ognlClass = Class.forName("ognl.webobjects.WOOgnl");
			Method ognlFactoryMethod = ognlClass.getMethod("factory", (Class[]) null);
			Object ognlFactory = ognlFactoryMethod.invoke(null, (Object[]) null);
			Method getValueMethod = ognlClass.getMethod("getValue", new Class[] { String.class, Object.class });
			return getValueMethod.invoke(ognlFactory, new Object[] { (String) value(), c });
		}
		catch (Throwable e) {
			throw new NSForwardException(e);
		}
	}
}
