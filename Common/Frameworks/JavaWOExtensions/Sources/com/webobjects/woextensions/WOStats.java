/*
 * WOStats.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;

public class WOStats extends WODirectAction {

    public WOStats(WORequest aRequest)  {
        super(aRequest);
    }
    
    public WOActionResults defaultAction()  {
        return pageWithName("WOStatsPage");
    }
}