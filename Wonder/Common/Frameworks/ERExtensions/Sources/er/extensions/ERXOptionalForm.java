/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

/**
 * 
 * Synopsis:<br/>
  *
 * @binding hasForm
 * @binding action
 * @binding name
 * @binding enctype
 * @binding directActionName
 * @binding actionClass
 */
public class ERXOptionalForm extends WOComponent {

    /**
     * Public constructor
     * @param aContext current context
     */
    public ERXOptionalForm(WOContext aContext) {
        super(aContext);
    }

    /**
     * Component is stateless
     * @return true
     */
    public boolean isStateless() { return true; }
}
