/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

import er.extensions.ERXConfigurationManager;
import er.extensions.ERXStatelessComponent;

public class ERCMailableExceptionPage extends ERXStatelessComponent {

    public EOEnterpriseObject actor;
    public Throwable exception;
    public NSArray _reasonLines;
    public String currentReasonLine;
    public NSDictionary extraInfo;

    public String currentUserInfoKey;
    public Object currentUserInfoValue;    
    
    public ERCMailableExceptionPage(WOContext aContext) {
        super(aContext);
    }

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

    public void reset() {
        super.reset();
        _reasonLines=null;
    }
    
    public NSArray reasonLines() {
        if (_reasonLines==null && exception!=null && exception.getMessage()!=null) {
            _reasonLines=NSArray.componentsSeparatedByString(exception.getMessage(), "\n");
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
