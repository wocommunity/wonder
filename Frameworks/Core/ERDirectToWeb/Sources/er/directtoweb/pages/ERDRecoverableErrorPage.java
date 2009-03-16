/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages;

import com.webobjects.appserver.WOContext;


/**
 * A recoverable error page.<br />
 * @deprecated subclass ERD2WMessagePage instead
 */

public class ERDRecoverableErrorPage extends ERD2WMessagePage {

    public ERDRecoverableErrorPage(WOContext context) {
        super(context);
        log.info("ERDRecoverableErrorPage is deprecated, subclass ERD2WMessagePage instead");
    }
    
    private String _wrapperName=null;
    public void setWrapperName(String wrapperName) { _wrapperName=wrapperName; }

    public String wrapperName() {
        return _wrapperName!=null ? _wrapperName : (String)d2wContext().valueForKey("pageWrapperName");
    }
}

