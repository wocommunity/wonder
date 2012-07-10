/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.numbers;

import java.text.Format;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayNumber;

import er.extensions.formatters.ERXNumberFormatter;

/**
 * Same as ERDDisplayNumberWithUnit only subclass is different.  This should be cleaned up.<br />
 * 
 * @binding key
 * @binding object
 * @d2wKey resolvedUnit
 * @d2wKey displayValueForNull
 */
public class ERD2WDisplayNumberWithUnit extends D2WDisplayNumber {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayNumberWithUnit(WOContext context) {
        super(context);
    }

    public Format numberFormatter() {
        return ERXNumberFormatter.numberFormatterForPattern(formatter());
    }
}
