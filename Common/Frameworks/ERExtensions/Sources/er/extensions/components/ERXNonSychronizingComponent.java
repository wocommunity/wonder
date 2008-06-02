/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * @deprecated use ERXNonSynchronizingComponent instead
 */
public abstract class ERXNonSychronizingComponent extends ERXNonSynchronizingComponent {

    /** Public constructor */
    public ERXNonSychronizingComponent(WOContext context) {
        super(context);
    }
}
