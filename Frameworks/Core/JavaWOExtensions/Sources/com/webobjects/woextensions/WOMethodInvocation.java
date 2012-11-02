/*
 * WOMethodInvocation.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class WOMethodInvocation extends WODynamicElement {
    protected WOAssociation _invoke;

    public WOMethodInvocation(String aName, NSDictionary associations, WOElement template)  {
        super(aName, null, null);
        _invoke = (WOAssociation)associations.objectForKey("invoke");
    }

    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        WOComponent aComponent = aContext.component();
        _invoke.valueInComponent(aComponent);
    }

    @Override
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext)  {
        WOComponent aComponent = aContext.component();
        _invoke.valueInComponent(aComponent);
    }

    @Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext)  {
        WOComponent aComponent = aContext.component();
        _invoke.valueInComponent(aComponent);
        return null;
    }
}
