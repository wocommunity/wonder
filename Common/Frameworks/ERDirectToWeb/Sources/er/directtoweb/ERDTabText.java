/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSKeyValueCoding;
import er.extensions.*;

public class ERDTabText extends ERXStatelessComponent {
    String tabText;

    public ERDTabText(WOContext context) { super(context); }

    public void reset() { tabText = null; }

    public String tabText() {
        if(tabText == null) {
            tabText = (String)((NSKeyValueCoding)valueForBinding("d2wContext")).valueForKey("tabName");
            tabText = ((ERXSession)session()).localizer().localizedStringForKeyWithDefault(tabText);
        }
        return tabText;
    }
}
