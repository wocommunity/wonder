/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.NSData;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class ERXDisplayDataAsString extends WOComponent {

    public ERXDisplayDataAsString(WOContext aContext) {
        super(aContext);
    }

    public String _string;
    public boolean synchronizesVariablesWithBindings() { return false; }

    public String string() {
        if (_string==null) {
            NSData d=(NSData)valueForBinding("data");
            if (d!=null) _string=new String(d.bytes(0,d.length()));
        }
        return _string;
    }
    
}
