/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERD2WStatelessComponent;

// FIXME: This depends on NS specific keypaths for an address.
/**
 * Displays an address.  Needs some cleanup to be more generic.
 */

public class ERD2WDisplayAddress extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayAddress(WOContext context) { super(context); }
    
    public Object location() {
        Object object = null;
        if (object()!=null && propertyKey() != null)  {
            object = object().valueForKeyPath(propertyKey());
        }
        return object;
    }
}
