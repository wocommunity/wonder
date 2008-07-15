/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOContext;

/**
 * Abstract non-synchronizing component used as the super class for a number of
 * components within the ER frameworks. Adds a number of nice binding resolution
 * methods.
 */
public abstract class ERXNonSynchronizingComponent extends ERXComponent {

	/** Public constructor */
	public ERXNonSynchronizingComponent(WOContext context) {
		super(context);
	}

	/** component does not synchronize variables */
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	/** component is not stateless */
	public boolean isStateless() {
		return false;
	}

}
