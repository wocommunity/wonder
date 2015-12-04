/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Tab panel with color backgrounds.
 * 
 * @binding tabs
 * @binding selectedTab
 * @binding tabNameKey
 * @binding nonSelectedBgColor
 * @binding bgcolor
 * @binding submitActionName
 * @binding textColor
 */

public class ERXColoredTabPanel extends ERXTabPanel  {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXColoredTabPanel(WOContext context) {
        super(context);
    }
}