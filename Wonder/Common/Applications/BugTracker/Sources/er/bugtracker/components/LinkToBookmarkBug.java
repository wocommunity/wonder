/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.bugtracker.Bug;
import er.directtoweb.ERDCustomEditComponent;

public class LinkToBookmarkBug extends ERDCustomEditComponent {

	public LinkToBookmarkBug(WOContext c) {
		super(c);
	}

	public boolean isStateless() {
		return true;
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public void reset() {
		super.reset();
		_bug = null;
	}

    public Bug _bug;

	public Bug bug() {
		if (_bug == null)
			_bug = (Bug) (object() instanceof Bug ? object() : objectKeyPathValue());
		return _bug;
	}

	public String href() {
		String url = context().directActionURLForActionNamed("bug", new NSDictionary(bug().bugid(), "number"));
		return url;
	}
}
