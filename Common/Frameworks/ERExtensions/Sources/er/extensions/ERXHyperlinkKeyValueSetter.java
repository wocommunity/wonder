/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class ERXHyperlinkKeyValueSetter extends WOComponent {

    public ERXHyperlinkKeyValueSetter(WOContext aContext) {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }
    
    public WOComponent action() {
        setValueForBinding(valueForBinding("value"),"binding");
        return null;
    }
}
