/*
 * WOMethodInvocation.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

public class WOMethodInvocation extends WODynamicElement {
    protected WOAssociation _invoke;

    public WOMethodInvocation(String aName, NSDictionary associations, WOElement template)  {
        super(aName, null, null);
        _invoke = (WOAssociation)associations.objectForKey("invoke");
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        WOComponent aComponent = aContext.component();
        _invoke.valueInComponent(aComponent);
    }

    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext)  {
        WOComponent aComponent = aContext.component();
        _invoke.valueInComponent(aComponent);
    }

    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext)  {
        WOComponent aComponent = aContext.component();
        _invoke.valueInComponent(aComponent);
        return null;
    }
}
