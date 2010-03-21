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
 * Compact list page.  Doesn't have any of the navigation at the top.<br />
 * 
 * @d2wKey emptyListComponentName
 * @d2wKey repetitionComponentName
 * @d2wKey displayNameForEntity
 * @d2wKey batchNavigationBarComponentName
 * @d2wKey showBatchNavigation
 * @d2wKey displayNameForEntity
 */
public class ERD2WCompactListPageTemplate extends ERD2WListPage {

    public ERD2WCompactListPageTemplate(WOContext context) {
        super(context);
    }
}
