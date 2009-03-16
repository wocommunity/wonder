/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker.components;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.bugtracker.Markable;

public class ReadMarker extends D2WComponent {

    public ReadMarker(WOContext c) {
        super(c);
    }
    
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
        if(context().page() instanceof InspectPageInterface) {
            EOEnterpriseObject eo = object();
            if (eo instanceof Markable) {
                Markable markable = (Markable) eo;
                markable.markAsRead();
            }
        }
        super.appendToResponse(aResponse, aContext);
    }
}
