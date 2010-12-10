/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.embed;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WSelect;

// Only difference between this component and D2WSelect is that this one uses ERD2WSwitchComponent
/**
 * Embedded component that can be used for nesting a pick inside another page configuration.<br />
 * 
 * @binding action
 * @binding branchDelegate
 * @binding dataSource
 * @binding entityName
 * @binding pageConfiguration
 * @binding selectedObject
 * @binding nextPage
 */

public class ERXD2WSelect extends D2WSelect {

    public ERXD2WSelect(WOContext context) { super(context); }
}
