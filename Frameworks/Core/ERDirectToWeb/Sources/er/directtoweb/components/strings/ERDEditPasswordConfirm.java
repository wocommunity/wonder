/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomEditComponent;

/**
 * Used to edit passwords where when changed the changed value must be confirmed.
 *
 * @binding errorMessage
 * @binding password
 * @binding passwordConfirm
 * @binding extraBindings
 * @binding key
 * @binding object
 */
public class ERDEditPasswordConfirm extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = LoggerFactory.getLogger(ERDEditPasswordConfirm.class);

    public int length;

    public ERDEditPasswordConfirm(WOContext context) { super(context); }
    public boolean passwordExists() { return objectKeyPathValue() != null ? true : false; }
}