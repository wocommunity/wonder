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

public class ERDEditButton extends ERDCustomEditComponent {

    public ERDEditButton(WOContext context) {super(context);}
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public D2WContext d2wContext() { return (D2WContext)valueForBinding("d2wContext"); }
    
    // Assuming that object() is the eo
    public WOComponent edit() {
        EOEditingContext context = er.extensions.ERXExtensions.newEditingContext();
        EOEnterpriseObject localObject = EOUtilities.localInstanceOfObject(context, object());
        String configuration = (String)d2wContext().valueForKey("editConfigurationNameForEntity");
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(configuration, session());
        epi.setObject(localObject);
        epi.setNextPage(context().page());
        context.hasChanges(); // Ensuring it survives.
        return (WOComponent)epi;
    }
}
