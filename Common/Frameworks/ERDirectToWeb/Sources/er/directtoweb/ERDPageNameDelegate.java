/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* PageNameDelegate.java created by jim on Wed 22-Aug-2001 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

public class ERDPageNameDelegate implements NextPageDelegate {

    public String _pageName;
    public void PageDelegate(String pageName) { _pageName=pageName; }
    public WOComponent nextPage(WOComponent sender) { return WOApplication.application().pageWithName(_pageName,sender.context()) /* JC_WARNING - Please check: in WO 5.1 the method pageWithName(String, WORequest) on WOApplication has been removed. If this invocation has a WORequest parameter, please replace it with WOApplication.application().createContextForRequest(WORequest). */; }    
}
