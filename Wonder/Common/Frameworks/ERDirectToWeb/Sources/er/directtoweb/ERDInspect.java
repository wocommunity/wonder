/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;
import com.webobjects.appserver.WOContext;
import er.extensions.*;

public class ERDInspect extends ERDCustomEditComponent {
    static final ERXLogger log = ERXLogger.getLogger(ERDInspect.class,"components");
	
    public ERDInspect(WOContext context) {
        super(context);
    }
    public boolean synchronizesVariablesWithBindings() { return false; }
}
