/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WList;

// Only difference between this component and D2WList is that this one uses ERD2WSwitchComponent
public class ERXD2WList extends D2WList {

    public ERXD2WList(WOContext context) { super(context); }
}
