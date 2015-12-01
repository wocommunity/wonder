/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.dates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.eof.ERXConstant;

/**
 * Displays a number as say 5 years 2 months.
 */

public class ERDDisplayYearsMonths extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDDisplayYearsMonths(WOContext context) {super(context);}

    @Override
    public boolean isStateless() { return true; }
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }

    public Number totalNumberOfMonths() { return objectKeyPathValue()!=null ?(Number)objectKeyPathValue(): ERXConstant.ZeroInteger; }

    public Integer numberOfYears() { return ERXConstant.integerForInt(totalNumberOfMonths().intValue()/12); }

    public Integer numberOfMonths() { return ERXConstant.integerForInt(totalNumberOfMonths().intValue() % 12); }
}
