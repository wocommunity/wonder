/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayString;

/**
 * Displays the url in a hyperlink with target set to "_new"<br />
 * 
 */

public class ERD2WDisplayURL extends D2WDisplayString {

    public ERD2WDisplayURL(WOContext context) { super(context); }

    public String href() {
        String href = objectPropertyValue() != null ? objectPropertyValue().toString() : null;
        if(href != null && href.indexOf("://") < 0) {
            href = "http://" + href;
        }
        return href;
    }
}
