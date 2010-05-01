/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages.templates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WListPage;

/**
 * Beefed up list page.<br />
 * @d2wKey printerButtonComponentName
 * @d2wKey emptyListComponentName
 * @d2wKey headerComponentName
 * @d2wKey entity
 * @d2wKey allowsFiltering
 * @d2wKey repetitionComponentName
 * @d2wKey batchNavigationBarComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey backgroundColorForTable
 * @d2wKey pageWrapperName
 * @d2wKey returnButtonLabel
 */
public class ERD2WListPageTemplate extends ERD2WListPage {

	public ERD2WListPageTemplate(WOContext context) {
		super(context);
	}
}
