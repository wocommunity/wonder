/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayToOne;

import er.extensions.ERXLocalizer;
import er.extensions.ERXValueUtilities;

/**
 * Same as original except allows display of noSelectionString if relationship is null.<br />
 * Also, links are disabled if no object exists.
 */

public class ERD2WDisplayToOne extends D2WDisplayToOne {

    public ERD2WDisplayToOne(WOContext context) { super(context); }
    
    public Object toOneDescription() {
        Object description = super.toOneDescription();
        return description != null ? description : ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault((String) d2wContext().valueForKey("noSelectionString"));
    }
    
    public boolean isDisabled() {
        return objectPropertyValue() == null || ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("disabled"), false);
    }
}
