/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.interfaces;

import com.webobjects.appserver.WOComponent;

/**
 * Interface used for follow page configurations, ie the first page config is an edit and we want an inspect to follow, maybe for the user to look at it before saving.
 */

public interface ERDFollowPageInterface {
    public WOComponent previousPage();
    public void setPreviousPage(WOComponent existingPageName);
}
