/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * This simple stateless component adds a javascript function
 * 'RandomizeLink' that will either add a dummy=0 or
 * change the previous value of a dummy=<some number>
 * parameter of a hyperlink. This can be very useful
 * for making sure that the browser does not cache the return
 * value of a dynamic link.
 */
//FIXME: Should make the parameter 'dummy' configurable
//FIXME: Should rename ERXJSLinkRandomizer
//ENHANCEME: Might want to wrap this component in an only once per request conditional
public class ERXLinkRandomizer extends ERXStatelessComponent {

    /**
     * Public constructor
     * @param context the context
     */
    public ERXLinkRandomizer(WOContext context) {
        super(context);
    }
}
