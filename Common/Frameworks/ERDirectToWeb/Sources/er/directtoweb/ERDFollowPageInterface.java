/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* FollowPageInterface.java created by angela on Wed 07-Feb-2001 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

public interface ERDFollowPageInterface {
    public WOComponent previousPage();
    public void setPreviousPage(WOComponent existingPageName);
}
