/*
 * WOEventSetup.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;

public class WOEventSetup extends WODirectAction {
    public WOEventSetup(WORequest aRequest)  {
        super(aRequest);
    }

    public WOActionResults defaultAction()
    {
        return pageWithName("WOEventSetupPage");
    }
}