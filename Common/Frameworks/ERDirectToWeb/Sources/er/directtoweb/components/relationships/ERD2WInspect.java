/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * !!Don't use, use ERDInspect instead!! Embedded component that can be used for nesting page configurations, ie ERD2WInspect can be a customComponentName.<br />
 * 
 */

public class ERD2WInspect extends ERDCustomEditComponent {

    public ERD2WInspect(WOContext context) { super(context); }
    
    public boolean synchronizesVariablesWithBindings() { return false; }
}
