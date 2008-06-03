/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

// Subclasses in look frameworks provide custom images for manditory fields.
/**
 * Displays a mandatory field marker. Extended in look frameworks to provide custom images.<br />
 * 
 * @binding condition
 */

public class ERXMandatoryFieldMarker extends WOComponent {

    public ERXMandatoryFieldMarker(WOContext aContext) {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }
}
