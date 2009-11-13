/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.printerfriendly;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WGroupingListPage;

/**
 * Printer friendly version.<br />
 * @d2wKey justification
 * @d2wKey componentName
 * @d2wKey propertyKey
 * @d2wKey displayPropertyKeys
 * @d2wKey displayNameForEntity
 * @d2wKey keyWhenGrouping
 * @d2wKey groupingOrderKey
 * @d2wKey headerComponentName
 * @d2wKey totallingKeys
 * @d2wKey displayNameForProperty
 * @d2wKey formatter
 * @d2wKey pageWrapperName
 */
public class ERD2WGroupingPrinterFriendlyListPageTemplate extends ERD2WGroupingListPage {

    public ERD2WGroupingPrinterFriendlyListPageTemplate(WOContext context) { super(context); }
}
