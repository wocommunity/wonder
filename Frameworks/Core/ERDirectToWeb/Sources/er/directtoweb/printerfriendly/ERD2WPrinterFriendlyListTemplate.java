/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.printerfriendly;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WListPage;

/**
 * Printer friendly list page.<br />
 * @d2wKey componentName
 * @d2wKey propertyKey
 * @d2wKey displayPropertyKeys
 * @d2wKey backgroundColorForTable
 * @d2wKey displayNameForEntity
 * @d2wKey justification
 * @d2wKey pageWrapperName
 */
public class ERD2WPrinterFriendlyListTemplate extends ERD2WListPage {

    public ERD2WPrinterFriendlyListTemplate(WOContext context) { super(context); }
}
