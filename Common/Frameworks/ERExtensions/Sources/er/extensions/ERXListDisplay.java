/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * Useful for displaying a list of eos. Ex. a list of person eos could be displayed as "Fred, Mark and Max".<br />
 * 
 * @binding list
 * @binding attribute
 * @binding nullArrayDisplay
 * @binding escapeHTML" defaults="Boolean
 */

public class ERXListDisplay extends WOComponent {

    public ERXListDisplay(WOContext aContext) {
        super(aContext);
    }
    
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    public boolean escapeHTML() {
        return ERXValueUtilities.booleanValueWithDefault(valueForBinding("escapeHTML"), true);
    }
    
    public String displayString() {
        return ERXArrayUtilities.friendlyDisplayForKeyPath((NSArray)valueForBinding("list"),
                                                   (String)valueForBinding("attribute"),
                                                   (String)valueForBinding("nullArrayDisplay"), ", ", " and ");
    }
}
