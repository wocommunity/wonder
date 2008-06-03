/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WStatelessComponent;

// FIXME: This depends on NS specific keypaths for an address.
/**
 * Displays an address.  Needs some cleanup to be more generic.<br />
 * 
 */

public class ERD2WDisplayAddress extends ERD2WStatelessComponent {

    public ERD2WDisplayAddress(WOContext context) { super(context); }
    
    public Object location() {
        Object object = null;
        if (object()!=null && propertyKey() != null)  {
            object = object().valueForKeyPath(propertyKey());
        }
        return object;
    }
}
