/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.numbers;

import com.webobjects.appserver.WOContext;

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.ERXExtensions;

/**
 * Cool class.  Specify a 'unit' in the userInfo dictionary of an EOAttribute and this component will display the number plus the unit.
 */
public class ERDDisplayNumberWithUnit extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDDisplayNumberWithUnit(WOContext context) { super(context); }

    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    @Override
    public boolean isStateless() { return true; }

    public String unit() {
        return valueForBinding("unit") != null ? (String)valueForBinding("unit") :
        ERDirectToWeb.resolveUnit(ERXExtensions.userInfoUnit(object(), key()), object(), key());
    }
}
