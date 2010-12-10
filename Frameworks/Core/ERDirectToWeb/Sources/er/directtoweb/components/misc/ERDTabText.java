/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomComponent;

/**
 * Used to display a tab as text.<br />
 * 
 */

public class ERDTabText extends ERDCustomComponent {
    public ERDTabText(WOContext context) { super(context); }

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }
}
