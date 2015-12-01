/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.buttons;

import com.webobjects.appserver.WOContext;

/**
 * A better trashcan.  As this component is named inconsistently with the
 * other buttons that operate on lists, {@link ERDDeleteButton} should be used instead.
 * 
 * @binding object
 * @binding dataSource
 * @binding d2wContext
 * @binding trashcanExplanation
 * @binding noTrashcanExplanation
 */

public class ERDTrashcan extends ERDDeleteButton {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDTrashcan(WOContext context) { super(context); }
}
