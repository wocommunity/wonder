/*
 * WOExceptionPage.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public transient Throwable exception;
    protected NSArray<String> _reasonLines;
    public String currentReasonLine;

    public transient WOExceptionParser error;
    public transient WOParsedErrorLine errorline;

    public WOExceptionPage(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public boolean isEventLoggingEnabled() {
        return false;
    }

    public void setException(Throwable newException) {
        error = new WOExceptionParser(newException);
        exception = newException;
    }

    @Override
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

    public NSArray<String> reasonLines() {
        if (null==_reasonLines) {
            String aMessage = exception.getMessage();
            if (aMessage!=null) {
                _reasonLines = NSArray.componentsSeparatedByString(exception.getMessage(), "\n");
            } else {
                _reasonLines = new NSArray<String>();
            }
        }
        return _reasonLines;
    }

    /*public void appendToResponse(WOResponse aResponse, WOContext aContext)  {
        super.appendToResponse(aResponse, aContext);
        aResponse.disableClientCaching();
    }*/
    
    public String errorMessage() {
        // Construct the error message that should be displayed in ProjectBuilder
        StringBuilder buffer = new StringBuilder(128);
        buffer.append("Error : ");
        buffer.append(exception.getClass().getName());
        buffer.append(" - Reason :");
        buffer.append(exception.getMessage());
        return buffer.toString();
    }
}
