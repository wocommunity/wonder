/*
 * WOEventDisplay.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

public class WOEventDisplay extends WODirectAction {
    public WOEventDisplay(WORequest aRequest)  {
        super(aRequest);
    }

    public WOActionResults defaultAction()
    {
        return pageWithName("WOEventDisplayPage");
    }
}