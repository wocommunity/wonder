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

public class ERXBatchNavigationBarInForm extends ERXBatchNavigationBar {
    
    public ERXBatchNavigationBarInForm(WOContext context) {
        super(context);
    }

    public String formTarget() {
        return ((ERXMutableUserInfoHolderInterface)context()).mutableUserInfo().objectForKey("formName") + ".target=_self;";
    }
}
