/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eocontrol.*;

public class ERDObjectWasCreatedDelegate implements NextPageDelegate {

    public EOEnterpriseObject eoToCreate;
    public EOEditingContext editingContext;
    public NextPageDelegate yesDelegate;
    public NextPageDelegate noDelegate;

    public ERDObjectWasCreatedDelegate(EOEnterpriseObject eo,
                                    NextPageDelegate yes,
                                    NextPageDelegate no) {
        eoToCreate=eo;
        // we make sure the EC is retained
        if (eoToCreate!=null) editingContext=eoToCreate.editingContext();
        yesDelegate=yes;
        noDelegate=no;
    }

    public WOComponent nextPage(WOComponent sender) {
        return eoToCreate!=null && eoToCreate.editingContext()!=null ? yesDelegate.nextPage(sender) : noDelegate.nextPage(sender);       
    }
    
}
