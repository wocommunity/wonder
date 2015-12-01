/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.ercmail;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

public interface ERCoreUserInterface {
    public NSArray preferences();
    public void setPreferences(NSArray array);
    public void newPreference(EOEnterpriseObject pref);
}
