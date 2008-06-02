/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

/*
 This component takes a list of tabs, the same as ERXTabPanel
 to find images, the naming convention is:

 /nsi/tab<tabName>.gif : tab is not selected
 /nsi/tab<tabName>Selected.gif : tab is selected

 where tabName is the name of the tab, minus spaces
 */

/**
 * Tab panel using images.<br />
 * 
 * @binding bgcolor
 * @binding nonSelectedBgColor
 * @binding selectedTab
 * @binding submitActionName
 * @binding tabNameKey
 * @binding tabs
 * @binding textColor
 * @binding useFormSubmit" defaults="Boolean
 */

public class ERXImageTabPanel extends ERXTabPanel  {

    public ERXImageTabPanel(WOContext context) {
        super(context);
    }

    public String currentImage() {
        // FIXME: could use a few more bindings to more naming more generic here!
        String name = currentTabName();
        name = NSArray.componentsSeparatedByString(name, " ").componentsJoinedByString("");
        return "/nsi/tab"+(name)+""+((isCellShaded()) ? "" : "Selected")+".gif";
    }
}