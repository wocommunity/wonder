/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;

/**
 * Displays a string with escape html set to false.<br />
 * 
 */

public class ERD2WDisplayHTML extends D2WDisplayString {

    public ERD2WDisplayHTML(WOContext context) { super(context); }

    public String htmlString() {
        String string =null;
        if (object() !=null && propertyKey() != null)
            string = (String)object().valueForKeyPath(propertyKey());
        return string;
    }
}
