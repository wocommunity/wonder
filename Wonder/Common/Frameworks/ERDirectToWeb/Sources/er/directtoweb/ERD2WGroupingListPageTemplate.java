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

public class ERD2WGroupingListPageTemplate extends ERD2WListPage {

    public ERD2WGroupingListPageTemplate(WOContext context) { super(context); }
    
    protected NSArray sublist;
    protected Object sublistSection;

    // the sorting will come only from the rules
    public boolean userPreferencesCanSpecifySorting() { return false; }
    public String groupingKey() { return (String)d2wContext().valueForKey("groupingKey"); }
    public String groupingComponentName() { return (String)d2wContext().valueForKey("groupingComponentName"); }
    public int colspanForNavBar() { return 2*displayPropertyKeys().count()+2; }
    public Object section() { return (Object)object().valueForKeyPath(groupingKey()); }
    public Object sumForSublist() { return sublist.valueForKey("@sum."+propertyKey()); }
}
