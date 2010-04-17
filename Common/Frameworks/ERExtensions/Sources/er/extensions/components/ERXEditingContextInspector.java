/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Inspects an eo's editing context.*<br />
 * 
 * @binding object
 */

public class ERXEditingContextInspector extends WOComponent {

    public ERXEditingContextInspector(WOContext aContext) {
        super(aContext);
    }

    public EOEditingContext object;
    public EOEnterpriseObject item;
}
