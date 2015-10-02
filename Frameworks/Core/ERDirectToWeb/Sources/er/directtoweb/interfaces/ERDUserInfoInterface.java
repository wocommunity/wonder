/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.interfaces;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Interface implemented by templates to allow stuff and retriving of transient information.
 */

public interface ERDUserInfoInterface {
    public NSMutableDictionary userInfo();
}
