/*
 * WOExceptionPage.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

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

    public WOResponse generateResponse() {
        WOResponse response = super.generateResponse();
        // we don't need the exception to stick around if we leave the page
        error = null;
        errorline = null;
        return response;
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
