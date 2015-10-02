/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.embed;

import com.webobjects.appserver.WOContext;

// Only difference between this component and D2WPick is that this one uses ERD2WSwitchComponent
/**
 * Embedded component that can be used for nesting a pick inside another page configuration.
 * 
 * @binding action
 * @binding branchDelegate
 * @binding dataSource
 * @binding entityName
 * @binding pageConfiguration
 * @binding selectedObjects
 * @binding nextPage
 */

public class ERXD2WPick extends D2WPick {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXD2WPick(WOContext context) { super(context); }
}
