/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;

/**
 * Allows custom components to be used to display the eos from a toMany.
 * 
 * @d2wKey customComponentName
 * @d2wKey componentBorder
 * @d2wKey numCols
 */
public class ERD2WDisplayToManyCustom extends ERD2WDisplayToManyTable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

     public ERD2WDisplayToManyCustom(WOContext context) { super(context); }
}
