/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSKeyValueCoding;

/**
 * Default component shown when a D2W list is empty.<br />
 */

public class ERDEmptyListMessage extends ERDCustomComponent {

	public ERDEmptyListMessage(WOContext context) {
		super(context);
	}

	public NSKeyValueCoding bindings() {
		return new NSKeyValueCoding() {
			public void takeValueForKey(Object obj, String s) {
				// nothing
			}

			public Object valueForKey(String s) {
				return valueForBinding(s);
			}
		};
	}

	public final boolean isStateless() {
		return true;
	}

	public final boolean synchronizesVariablesWithBindings() {
		return false;
	}
}
