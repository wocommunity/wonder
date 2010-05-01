/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.pages.templates;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WQueryPage;

/**
 * Beefed up query page.<br />
 * @d2wKey headerComponentName
 * @d2wKey showListInSamePage
 * @d2wKey listConfigurationName
 * @d2wKey clearButtonLabel
 * @d2wKey findButtonLabel
 * @d2wKey returnButtonLabel
 * @d2wKey actionBarComponentName
 * @d2wKey controllerButtonComponentName 
 */
public class ERD2WQueryPageTemplate extends ERD2WQueryPage {

    public ERD2WQueryPageTemplate(WOContext context) {
        super(context);
    }
}
