/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.printerfriendly;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WInspectPage;

/**
 * Printer friendly inspect page.
 * 
 * @d2wKey displayNameForEntity
 * @d2wKey repetitionComponentName
 * @d2wKey pageWrapperName
 */
public class ERD2WPrinterFriendlyInspectPageTemplate extends ERD2WInspectPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WPrinterFriendlyInspectPageTemplate(WOContext context) { super(context); }
}
