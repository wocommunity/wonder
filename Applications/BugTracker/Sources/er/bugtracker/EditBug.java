/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eocontrol.EOEnterpriseObject;

public class EditBug extends WOComponent implements EditPageInterface {

    public EditBug(WOContext context) {
        super(context);
    }
    
    protected NextPageDelegate nextPageCallback;
    /** @TypeInfo er.bugtracker.Bug */
    protected EOEnterpriseObject currentObject;
    protected WOComponent theNextPage;

    public EOEnterpriseObject object() { return currentObject; }

    public void setNextPage(WOComponent nextPage) { theNextPage=nextPage; }

    public void setObject(EOEnterpriseObject bug) {
        currentObject = bug;
        ((Bug)currentObject).markReadBy((People)((Session)session()).getUser());
    }

    public void setNextPageDelegate(NextPageDelegate Callback) { nextPageCallback= Callback; }

    public WOComponent nextPage() {
        return (nextPageCallback != null) ? nextPageCallback.nextPage(this) :
            (theNextPage!=null) ? theNextPage : null;
    }
}
