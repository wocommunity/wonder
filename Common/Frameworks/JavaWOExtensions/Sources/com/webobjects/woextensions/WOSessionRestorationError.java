/*
 * WOSessionRestorationError.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;

public class WOSessionRestorationError extends WOComponent {
    public WOSessionRestorationError(WOContext aContext)  {
        super(aContext);
    }

    public boolean isEventLoggingEnabled() {
        return false;
    }

    public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        super.appendToResponse(aResponse, aContext);
        aResponse.disableClientCaching();
    }
}