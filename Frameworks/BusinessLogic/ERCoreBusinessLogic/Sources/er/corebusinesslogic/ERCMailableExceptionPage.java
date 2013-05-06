/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXUtilities;

public class ERCMailableExceptionPage extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public String errorMessage;
    public EOEnterpriseObject actor;
    public Throwable exception;
    public NSArray _reasonLines;
    public String currentReasonLine;
    public String formattedMessage;
    public NSDictionary extraInfo;

    public String currentUserInfoKey;
    public Object currentUserInfoValue;    
    
    public ERCMailableExceptionPage(WOContext aContext) {
        super(aContext);
    }

    @Override
    public boolean isEventLoggingEnabled() {
        return false;
    }

    public void setException(Throwable value) {
        exception = value;
    }

    public void setActor(EOEnterpriseObject value) {
        actor = value;
    }

    public void setExtraInfo(NSDictionary value) {
        extraInfo = value;
    }

    public void setFormattedMessage(String value) {
        formattedMessage = value;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String shortErrorMessage() {
        return exception != null ? exception.getClass().getName() : errorMessage;
    }
    
    public void setReasonLines(NSArray reasonLines) {
        _reasonLines = reasonLines;
    }
    
    public NSArray reasonLines() {
        if (_reasonLines==null && exception!=null) {
            _reasonLines = NSArray.componentsSeparatedByString(ERXUtilities.stackTrace(exception), "\n\t");
        }
        return _reasonLines;
    }

    public NSTimestamp now() {
        return new NSTimestamp();
    }
    
    public String hostName() {
        return ERXConfigurationManager.defaultManager().hostName();
    }
}
