/*
 * WOExceptionPage.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

public class WOExceptionPage extends WOComponent {
    public Throwable exception;
    protected NSArray _reasonLines;
    public String currentReasonLine;

    public WOExceptionParser error;
    public WOParsedErrorLine errorline;

    public WOExceptionPage(WOContext aContext)  {
        super(aContext);
    }

    public boolean isEventLoggingEnabled() {
        return false;
    }

    public void setException(Throwable newException) {
        error = new WOExceptionParser(newException);
        exception = newException;
    }
    
    public boolean showDetails() {
        return WOApplication.application().isDebuggingEnabled();
    }

    public NSArray reasonLines() {
        if (null==_reasonLines) {
            String aMessage = exception.getMessage();
            if (aMessage!=null) {
                _reasonLines = NSArray.componentsSeparatedByString(exception.getMessage(), "\n");
            } else {
                _reasonLines = new NSArray();
            }
        }
        return _reasonLines;
    }

    /*public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        super.appendToResponse(aResponse, aContext);
        aResponse.disableClientCaching();
    }*/
    
    public String errorMessage() {
        // Construct the error message that should be display in ProjectBuilder
        StringBuffer buffer = new StringBuffer(128);
        buffer.append("Error : ");
        buffer.append(exception.getClass().getName());
        buffer.append(" - Reason :");
        buffer.append(exception.getMessage());
        return new String(buffer);
    }
}