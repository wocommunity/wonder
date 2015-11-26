/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.NextPageDelegate;

/**
 * Generic little delegate. Nice when all you really want to do is return a
 * page, but the interface says you have to use a delegate.
 */

public class ERDPageDelegate implements NextPageDelegate {

	public WOComponent _nextPage;

	public ERDPageDelegate(WOComponent np) {
		_nextPage = np;
	}

	public WOComponent nextPage(WOComponent sender) {
		return _nextPage;
	}
}
