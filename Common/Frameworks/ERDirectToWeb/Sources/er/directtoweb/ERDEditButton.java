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
import er.extensions.*;

/**
 * Nice edit button for editing a toMany relationship in another page.<br />
 * 
 */

public class ERDEditButton extends ERDCustomEditComponent {

   public final static ERXLogger log = ERXLogger.getERXLogger("er.directtoweb.components.ERDEditButton");


    public ERDEditButton(WOContext context) {super(context);}
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    protected EOEnterpriseObject localInstanceOfObject() {
        return ERD2WUtilities.localInstanceFromObjectWithD2WContext(object(), d2wContext());
    }
    
    // Assuming that object() is the eo
    public WOComponent edit() {
        EOEnterpriseObject localObject = localInstanceOfObject();
        String configuration = (String)valueForBinding("editConfigurationNameForEntity");
        if(log.isDebugEnabled()){
           log.debug("configuration = "+configuration);
        }
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(configuration, session());
        epi.setObject(localObject);
        epi.setNextPage(context().page());
        localObject.editingContext().hasChanges(); // Ensuring it survives.
        return (WOComponent)epi;
    }
}
