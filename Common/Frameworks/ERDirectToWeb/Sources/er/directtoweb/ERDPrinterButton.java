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
import com.webobjects.directtoweb.*;

public class ERDPrinterButton extends ERDCustomEditComponent {

    public ERDPrinterButton(WOContext context) { super(context); }
    
    public String task() { return (String)valueForBinding("task");  }
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }
    public D2WContext d2wContext() { return (D2WContext)valueForBinding("d2wContext"); }
    public EODataSource dataSource() { return (EODataSource)valueForBinding("dataSource"); }
    public WODisplayGroup displayGroup() { return (WODisplayGroup)valueForBinding("displayGroup"); }

    public WOComponent printerFriendlyVersion() {
        WOComponent result = null;
        if(task().equals("edit") || task().equals("inspect"))
            result = editPrinterFriendlyVersion();
        else if(task().equals("list") || task().equals("pick"))
            result = listPrinterFriendlyVersion();
        return result;
    }

        public WOComponent editPrinterFriendlyVersion() {
        WOComponent result=ERDirectToWeb.printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }

    public WOComponent listPrinterFriendlyVersion() {
        D2WListPage result=(D2WListPage)ERDirectToWeb.printerFriendlyPageForD2WContext(d2wContext(),session());
        result.setDataSource(dataSource());
        result.displayGroup().setSortOrderings(displayGroup().sortOrderings());
        result.displayGroup().setNumberOfObjectsPerBatch(displayGroup().allObjects().count());
        result.displayGroup().updateDisplayedObjects();
        return result;
    }
}
