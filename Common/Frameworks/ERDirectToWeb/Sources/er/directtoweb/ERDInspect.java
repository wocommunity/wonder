/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;
import com.webobjects.appserver.WOContext;

/**
 * Embedded component that can be used for nesting page configurations, ie ERDInspect can be a customComponentName.<br />
 * 
 */

public class ERDInspect extends ERDCustomEditComponent {
    public ERDInspect(WOContext context) {
        super(context);
    }
    public boolean synchronizesVariablesWithBindings() { return false; }
}
