/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.foundation.NSKeyValueCoding;

import er.directtoweb.components.ERDCustomComponent;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Default component shown when a D2W list is empty.
 * 
 * @d2wKey showCreateObjectLink
 */
public class ERDEmptyListMessage extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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

	@Override
	public final boolean isStateless() {
		return true;
	}

	@Override
	public final boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
    /**
     * Returns whether the "create new" link should be shown, depends on a rule like:<br><br>
     * pageConfiguration = 'ListEntity' =&gt; showCreateObjectLink = true [prio]
     * 
     */
    public boolean showCreateObjectLink () {
    	boolean enabledFromRule = ERXValueUtilities.booleanValue(d2wContext().valueForKey("showCreateObjectLink"));
    	boolean entityExists = (d2wContext().entity() != null && d2wContext().entity().name() != null);
    	
    	return entityExists && enabledFromRule;
    }
    
    /**
     * @return a new create page for the current entity
     */
    public WOComponent createObject () {
        WOComponent nextPage = null;
        try {
            EditPageInterface epi = D2W.factory().editPageForNewObjectWithEntityNamed(d2wContext().entity().name(), session());
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
