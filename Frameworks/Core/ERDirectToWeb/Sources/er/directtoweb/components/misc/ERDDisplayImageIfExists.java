/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSData;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * Displays an image if it exists.<br />
 * 
 */

public class ERDDisplayImageIfExists extends ERDCustomEditComponent {

    public ERDDisplayImageIfExists(WOContext context) { super(context); }
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    public NSData imageContent() { return (NSData)objectKeyPathValue(); }
}
