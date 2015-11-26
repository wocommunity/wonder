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
 * Displays a string with escape html set to false.
 */

public class ERD2WDisplayHTML extends D2WDisplayString {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayHTML(WOContext context) { super(context); }

    public String htmlString() {
        String string =null;
        if (object() !=null && propertyKey() != null)
            string = (String)object().valueForKeyPath(propertyKey());
        return string;
    }
}
