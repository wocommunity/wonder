/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;

public class ERD2WDisplayConstant extends D2WStatelessComponent {

    public ERD2WDisplayConstant(WOContext context) {super(context);}
    
    public String displayConstant() {
        return (String)d2wContext().valueForKey(propertyKey());
    }
}
