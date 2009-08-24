/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.bool;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WEditBoolean;
import com.webobjects.foundation.NSValidation;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Edits a boolean with radio buttons and Yes/No<br />
 * You should use ERD2WCustomEditBoolean with the choicesNames d2w key instead.
 */

@Deprecated
public class ERD2WEditYesNo extends ERD2WCustomEditBoolean {
    
    public static Logger log = Logger.getLogger(ERD2WEditYesNo.class);

    public ERD2WEditYesNo(WOContext context) {
        super(context);
    }
}
