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

// FIXME: References help components, needs to move to look framework.
public class ERDPrinterFriendlyWrapper extends WOComponent {

public ERDPrinterFriendlyWrapper(WOContext context) {super(context);}


    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSTimestamp now() {
        return new NSTimestamp();
    } 
}
