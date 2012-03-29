/*
 * WXAccessControl.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

public class WXAccessControl extends WOComponent {

	//********************************************************************
	//	Constructor
	//********************************************************************

	public WXAccessControl(WOContext aContext)  {
		super(aContext);
	}

	//********************************************************************
	//	Overwrite
	//********************************************************************

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	//********************************************************************
	//	Methods
	//********************************************************************

	public boolean shouldShow() {
		NSDictionary permissions = (NSDictionary)session().valueForKey("permissions");
		if (permissions!=null) {
			return ((Boolean)permissions.valueForKey((String)valueForBinding("key"))).booleanValue();
		}
		return true;
	}
}