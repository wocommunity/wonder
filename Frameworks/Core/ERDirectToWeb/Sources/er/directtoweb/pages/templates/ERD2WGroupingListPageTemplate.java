/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages.templates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WGroupingListPage;

/**
 * Displays a groups of objects grouped by a key.<br />
 * Actually, this component uses none of the variables and methods defined here,
 * as all the work is done by the ERDGroupingListPageRepetition that should be set
 * in the rules when a "ListGroupSomeEntity" page configuration is called up.
 * 
 */

public class ERD2WGroupingListPageTemplate extends ERD2WGroupingListPage {

    public ERD2WGroupingListPageTemplate(WOContext context) { super(context); }
}
