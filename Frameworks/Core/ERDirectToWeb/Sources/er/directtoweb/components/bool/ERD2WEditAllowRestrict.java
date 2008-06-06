/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;


// FIXME: This can be replaced by D2WCustomEditBoolean
/**
 * Edits a boolean with the string Allow/Restrict.  Should use ERD2WCustomEditBoolean instead.<br />
 * 
 */

public class ERD2WEditAllowRestrict extends ERD2WEditYesNo {

    public ERD2WEditAllowRestrict(WOContext context) { super(context); }    
}
