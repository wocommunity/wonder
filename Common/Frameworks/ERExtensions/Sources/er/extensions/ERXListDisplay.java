/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class ERXListDisplay extends WOComponent {

    public ERXListDisplay(WOContext aContext) {
        super(aContext);
    }
    
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    public boolean escapeHTML() {
        return ERXUtilities.booleanValueForBindingOnComponentWithDefault("escapeHTML", this, true);
    }
    
    public String displayString() {
        return ERXArrayUtilities.friendlyDisplayForKeyPath((NSArray)valueForBinding("list"),
                                                   (String)valueForBinding("attribute"),
                                                   (String)valueForBinding("nullArrayDisplay"), ", ", " and ");
    }
}
