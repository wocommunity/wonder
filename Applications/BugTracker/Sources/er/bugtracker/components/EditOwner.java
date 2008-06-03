/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WComponent;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.bugtracker.Bug;

/**
 * Edits the owner in an more process oriented way, by offering the option to
 * assign if to self, originator or previous owner. This is an example of a composed D2WComponent.
 */
public class EditOwner extends D2WComponent {

	public EditOwner(WOContext c) {
		super(c);
	}

	/** @TypeInfo People */
	public EOEnterpriseObject localOriginator() {
		if (object() != null && ((Bug) object()).originator() != null)
			return EOUtilities.localInstanceOfObject(session().defaultEditingContext(), ((Bug) object()).originator());
		return null;
	}
}
