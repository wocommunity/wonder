/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;

import er.extensions.*;

/**
 * Correctly handles validation exceptions, plus a bunch of other stuff.<br />
 * 
 */

public class ERDCustomQueryComponentWithArgs extends ERDCustomQueryComponent {

    public ERDCustomQueryComponentWithArgs(WOContext context) {
        super(context);
    }
    
    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERDCustomQueryComponentWithArgs.class);
}
