/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.ERXValueUtilities;

/**
 * Default component shown when a D2W list is empty.<br />
 */

public class ERDEmptyListMessage extends ERDCustomComponent {

	public ERDEmptyListMessage(WOContext context) {
		super(context);
	}

	public NSKeyValueCoding bindings() {
		return new NSKeyValueCoding() {
			public void takeValueForKey(Object obj, String s) {
				// nothing
			}

			public Object valueForKey(String s) {
				return valueForBinding(s);
			}
		};
	}

	public final boolean isStateless() {
		return true;
	}

	public final boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
    /**
     * Returns whether the "create new" link should be shown, depends on a rule like:<br><br>
     * pageConfiguration = 'ListEntity' => showCreateObjectLink = true [prio]
     * 
     */
    public boolean showCreateObjectLink () {
    	boolean enabledFromRule = ERXValueUtilities.booleanValue(this.d2wContext().valueForKey("showCreateObjectLink"));
    	boolean entityExists = (this.d2wContext().entity() != null && this.d2wContext().entity().name() != null);
    	
    	return entityExists && enabledFromRule;
    }
    
    /**
     * @return a new create page for the current entity
     */
    public WOComponent createObject () {
        WOComponent nextPage = null;
        try {
            EditPageInterface epi = D2W.factory().editPageForNewObjectWithEntityNamed(this.d2wContext().entity().name(), session());
            epi.setNextPage(context().page());
            nextPage = (WOComponent) epi;
        } catch (IllegalArgumentException e) {
            ErrorPageInterface epf = D2W.factory().errorPage(session());
            epf.setMessage(e.toString());
            epf.setNextPage(context().page());
            nextPage = (WOComponent) epf;
        }
        return nextPage;
    }
}
