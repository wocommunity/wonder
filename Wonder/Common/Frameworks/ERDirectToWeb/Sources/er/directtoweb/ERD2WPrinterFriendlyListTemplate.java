/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;

public class ERD2WPrinterFriendlyListTemplate extends ERD2WListPageTemplate {

    public ERD2WPrinterFriendlyListTemplate(WOContext context) { super(context); }
    
    // FIXME: This needs to be generalized.
    public String pageTitle() {
        return "NetStruxr - "+d2wContext().valueForKey("displayNameForEntity")+" List";
    }

    // in our case, sort ordering comes from the creation of the page
    public boolean userPreferencesCanSpecifySorting() { return false; }
}
