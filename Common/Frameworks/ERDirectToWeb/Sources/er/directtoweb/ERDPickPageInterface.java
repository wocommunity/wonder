/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.foundation.NSArray;
import com.webobjects.appserver.WOComponent;
import com.webobjects.eocontrol.EODataSource;

// PickPageInterface - Used for selecting multiple objects from a select page.
public interface ERDPickPageInterface {

    // Data source
    public void setDataSource(EODataSource source);

    public NextPageDelegate nextPageDelegate();
    public void setNextPageDelegate(NextPageDelegate npd);

    public WOComponent cancelPage();
    public void setCancelPage(WOComponent cp);
    
    public NSArray selectedObjects();
    public void setSelectedObjects(NSArray selectedObjects);

    public void setChoices(NSArray choices);
}
