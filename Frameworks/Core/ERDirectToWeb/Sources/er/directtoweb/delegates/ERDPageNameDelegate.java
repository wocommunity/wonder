/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.NextPageDelegate;

/**
 * NextPageDelegate that takes a given page name and when called creates and returns the given named page.
 */

public class ERDPageNameDelegate implements NextPageDelegate {

    protected String _pageName;
    public ERDPageNameDelegate(String pageName) { _pageName=pageName; }
    
    public WOComponent nextPage(WOComponent sender) {
        return sender.pageWithName(_pageName);
    }    
}
