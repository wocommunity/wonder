/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

// needed for nested page configs as well as pick pages
// Removes forms from WOComponent
import com.webobjects.appserver.WOContext;

/**
 * Better navigation bar without a form.<br />
 * 
 * @binding d2wContext the D2W context that this component is in
 * @binding displayGroup the WODisplayGroup that is being controlled
 * @binding width the width of the navigation bar table (there is a minimum 500 pixel width if tableClass is not specified)
 * @binding objectName the name of the type of object that is contained in the WODisplayGroup
 * @binding border the border width of the navigation bar table
 * @binding bgcolor the background color of the navigation bar table
 * @binding textColor no longer used?
 * @binding sortKeyList an NSArray of sort key paths that will be displayed in a popup button
 * @binding tableClass the CSS class for the navigation table (overrides minimum 500 pixel width when set)
 * @binding imageFramework the name of the framework that contains the navigation arrow images
 * @binding leftArrowImage the name of the left navigation arrow image
 * @binding rightArrowImage the name of the right navigation arrow image
 */
public class ERXBatchNavigationBarInForm extends ERXBatchNavigationBar {
    
    public ERXBatchNavigationBarInForm(WOContext context) {
        super(context);
    }

    public String formTarget() {
        return ERXWOForm.formName(context(), "EditForm") + ".target='_self';";
    }
}
