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

public class ERDSectionText extends ERXStatelessComponent {
    String sectionText;

    public ERDSectionText(WOContext context) { super(context); }

    public void reset() { sectionText = null; }
    
    public String sectionText() {
        if(sectionText == null) {
            sectionText = (String)((NSKeyValueCoding)valueForBinding("d2wContext")).valueForKey("sectionKey");
            sectionText = ((ERXSession)session()).localizer().localizedStringForKeyWithDefault(sectionText);
        }
        return sectionText;
    }
}
