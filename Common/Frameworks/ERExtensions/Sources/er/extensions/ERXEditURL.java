/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.net.*;

public class ERXEditURL extends WOComponent {

    public ERXEditURL(WOContext aContext) {
        super(aContext);
    }

    public boolean isStateless() { return true; }
    public final static String DEFAULT="http://www.";

    public Object value() {
        Object result=(Object)valueForBinding("value");
        if(result instanceof URL)
            result = result.toString();
        if (result==null || ((String)result).length()==0)
            result=DEFAULT;
        return result;
    }

    public void setValue(Object newValue) {
        if (newValue!=null && newValue.equals(DEFAULT))
            newValue=null;
        setValueForBinding(newValue,"value");
    }
}
