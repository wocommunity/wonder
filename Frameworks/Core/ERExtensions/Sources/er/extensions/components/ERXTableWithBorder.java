/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;

import er.extensions.eof.ERXConstant;

/**
 * Allows turning the border on and off of a table. Useful for Netscape which doesn't handle nested tables very well.
 * 
 * @binding color
 * @binding width
 * @binding renderBorder" defaults="Boolean
 * @binding bgcolor
 * @binding doNotRenderTop" defaults="Boolean
 */

public class ERXTableWithBorder extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXTableWithBorder(WOContext aContext) {
        super(aContext);
    }

    // renderBorder false can be used to not output a table at all
    // this is useful since NetScape 4.7 and earlier get extremely slow
    // when table nesting gets past a certain level
    
    public boolean renderBorder() {
        return booleanValueForBinding("renderBorder", true);
    }

    public Object border() {
        Object result = (hasBinding("border") ? valueForBinding("border") : null);
        if (result==null) result=ERXConstant.OneInteger;
        return result;
    }
    
}
