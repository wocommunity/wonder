/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class ERXMailTo extends WOComponent {

    public ERXMailTo(WOContext aContext) {
        super(aContext);
    }

    public boolean isStateless() { return true; }

    public String href() {
        String result=null;
        String email=(String)valueForBinding("email");
        if (email!=null) result="mailto:"+email;
        return result;
    }
}
