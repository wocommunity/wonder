/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Component;

public class ComponentReport extends WOComponent {

	public ComponentReport(WOContext c) {
		super(c);
	}

	public Component component;

	public NSArray componentList() {
		return Component.clazz.orderedComponents(session().defaultEditingContext());
	}
}
