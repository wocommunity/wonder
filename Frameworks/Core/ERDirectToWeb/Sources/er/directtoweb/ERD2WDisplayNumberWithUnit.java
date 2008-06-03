/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.text.Format;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WDisplayNumber;

import er.extensions.formatters.ERXNumberFormatter;

/**
 * Same as ERDDisplayNumberWithUnit only subclass is different.  This should be cleaned up.<br />
 * 
 * @binding key
 * @binding object
 */

public class ERD2WDisplayNumberWithUnit extends D2WDisplayNumber {

    public ERD2WDisplayNumberWithUnit(WOContext context) {
        super(context);
    }

    public Format numberFormatter() {
        return ERXNumberFormatter.numberFormatterForPattern(formatter());
    }
}
