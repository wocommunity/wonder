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


public class ERDList extends ERDCustomEditComponent {

    public ERDList(WOContext context) {super(context);}
    
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSArray list() { return (NSArray)(hasBinding("list") ? valueForBinding("list") : objectKeyPathValue()); }

    // This is fine because we only use the D2WList if we have at least one element in the list.
    public String entityName() { return list().count() > 0 ? ((EOEnterpriseObject)list().objectAtIndex(0)).entityName() : null; }

    // FIXME: This sucks.
    public boolean isTargetXML(){
        String listPageConfiguration = (String)valueForBinding("listPageConfiguration");
        return listPageConfiguration != null && listPageConfiguration.indexOf("XML") > -1;
    }
    
    public boolean erD2WListOmitCenterTag() {
        return hasBinding("erD2WListOmitCenterTag") ? booleanForBinding("erD2WListOmitCenterTag") : false;
    }
}
