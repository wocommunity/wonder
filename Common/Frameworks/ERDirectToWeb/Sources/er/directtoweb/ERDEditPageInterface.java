/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* EREditPageInterface.java created by max on Thu 15-Mar-2001 */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eocontrol.*;

public interface ERDEditPageInterface extends EditPageInterface {
    // Not having this one is very annoying.
    public EOEnterpriseObject object();
    
}
