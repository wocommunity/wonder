/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

public class ERD2WQueryPage extends D2WQueryPage {

    public ERD2WQueryPage(WOContext context) { super(context); }
    
    // add the ability to AND the existing qualifier from the DG
    public EOQualifier qualifier() {
        EOQualifier q=displayGroup.qualifier();
        EOQualifier q2=super.qualifier();
        return q==null ? q2 : (q2==null ? q : new EOAndQualifier(new NSArray(new Object[]{q,q2})));
    }

    // Used with branching delegates.
    protected NSDictionary branch;
    public String branchName() { return (String)branch.valueForKey("branchName"); }

    // returning a null query data source if cancel was clicked
    private boolean _wasCancelled;
    public WOComponent cancelAction() {
        WOComponent result=null;
        try {
            _wasCancelled=true;
            result=nextPageDelegate().nextPage(this);
        } finally {
            _wasCancelled=false;
        }
        return result;
    }

    public EODataSource queryDataSource() {
        return !_wasCancelled ? super.queryDataSource() : null;
    }    
}
