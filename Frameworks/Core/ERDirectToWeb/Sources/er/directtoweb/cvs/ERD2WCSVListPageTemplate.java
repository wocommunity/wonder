/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.cvs;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WListPage;

/**
 * Standard list in CSV format.
 * 
 * @d2wKey propertyKey
 * @d2wKey displayPropertyKeys
 * @d2wKey displayNameForProperty
 */
public class ERD2WCSVListPageTemplate extends ERD2WListPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WCSVListPageTemplate(WOContext context) { super(context); }
}
