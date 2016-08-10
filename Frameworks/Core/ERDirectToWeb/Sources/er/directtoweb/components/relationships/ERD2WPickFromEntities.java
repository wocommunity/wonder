/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.components.ERDCustomQueryComponent;

/**
 * Custom query component that let's the user select from a set of shared eos.
 */
public class ERD2WPickFromEntities extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WPickFromEntities(WOContext context) {
        super(context);
    }
        
    // lets you pick from either an arbitrary list or a pool of shared EOs  
    public Object item; 

    // can't be stateless!
    @Override
    public boolean isStateless() { return false; }
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSArray list() { return (NSArray)valueForBinding("list"); }

    public String displayString() {
        String result=null;
        if (item!=null) {
            result=(String)ERDirectToWeb.d2wContextValueForKey("displayNameForEntity", (String)item, null);
        }
        return result;
    }
}
