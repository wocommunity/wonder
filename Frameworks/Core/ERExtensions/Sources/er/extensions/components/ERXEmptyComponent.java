/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * An empty component does not contain any html or wod. This admittedly
 * simple component is useful for having WOSwitchComponents no return
 * anything.
 */
public class ERXEmptyComponent extends ERXStatelessComponent {

    /** Public constructor */
    public ERXEmptyComponent(WOContext aContext) {
        super(aContext);
    }
}
