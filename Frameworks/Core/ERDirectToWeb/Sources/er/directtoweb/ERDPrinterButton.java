/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.eoaccess.EOUtilities;

/**
 * Handles the switching of the current task to print, which uses the print templates.<br />
 * 
 * @binding d2wContext
 * @binding dataSource
 * @binding displayGroup
 * @binding task
 * @binding useSubmitButton" defaults="Boolean
 * @binding doNotUseForm" defaults="Boolean
 */

public class ERDPrinterButton extends ERDActionButton {

    public ERDPrinterButton(WOContext context) { super(context); }
    
    public boolean show() {
        return booleanValueForBinding("show", true);
    }
    
    public WOComponent printerFriendlyVersion() {
        WOComponent result = null;
        if(d2wContext() == null) {
            D2WContext dummyContext = ERD2WContext.newContext();
            dummyContext.takeValueForKey(object(), "object");
            dummyContext.setEntity(EOUtilities.entityNamed(object().editingContext(),object().entityName()));
            dummyContext.setTask("inspect");
            
            result=ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(dummyContext,session());
            ((EditPageInterface)result).setObject(object());
        } else {
            if(task().equals("edit") || task().equals("inspect"))
                result = editPrinterFriendlyVersion();
            else if(task().equals("list") || task().equals("pick"))
                result = listPrinterFriendlyVersion();
        }
        return result;
    }

    public WOComponent editPrinterFriendlyVersion() {
        WOComponent result=ERD2WFactory.erFactory().printerFriendlyPageForD2WContext(d2wContext(),session());
        ((EditPageInterface)result).setObject(object());
        return result;
    }
    
    public WOComponent listPrinterFriendlyVersion() {
        return ERD2WListPage.printerFriendlyVersion(d2wContext(), session(), dataSource(), displayGroup());
    }
}
