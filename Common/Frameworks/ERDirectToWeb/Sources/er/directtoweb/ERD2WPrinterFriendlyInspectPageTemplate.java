/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;


public class ERD2WPrinterFriendlyInspectPageTemplate extends ERD2WInspectPage {

    public ERD2WPrinterFriendlyInspectPageTemplate(WOContext context) {super(context);}
    
    public String pageTitle() {
        //System.out.println("********** in ERPrinterFriendlyInspectPageTemplate, pageTitle().");
        return "NetStruxr - "+d2wContext().valueForKey("displayNameForEntity")+" View";
    }

    public NSTimestamp now() {
        return new NSTimestamp();
    } 
}
