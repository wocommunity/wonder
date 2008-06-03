/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * page wrapper in xml.<br />
 * 
 */

public class ERDXMLPageWrapper extends WOComponent {

    public ERDXMLPageWrapper(WOContext context) { super(context); }

    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }
}
