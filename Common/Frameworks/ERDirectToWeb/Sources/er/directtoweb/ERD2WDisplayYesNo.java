/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

import er.extensions.*;

/**
 * Displays a boolean as Yes or No.<br />
 * 
 */

public class ERD2WDisplayYesNo extends D2WDisplayBoolean {

    public ERD2WDisplayYesNo(WOContext context) { super(context); }

    public boolean isYes() {
        return ERXValueUtilities.booleanValue(objectPropertyValue());
    }
}
