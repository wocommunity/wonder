/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import org.apache.log4j.*;

public class ERDDefaultCustomComponent extends WOComponent {

public ERDDefaultCustomComponent(WOContext context) {super(context);}

    //////////////////////////////////////////  log4j category  /////////////////////////////////////////////////
    public static final Category cat = Category.getInstance("er.directtoweb.components.ERDDefaultCustomComponent");
    
    public boolean isStateless() { return true; }

    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding("object"); }
    public String key() { return (String)valueForBinding("key"); }

    public void appendToResponse(WOResponse response, WOContext context) {
        cat.warn("Using default custom component for object: " + object() + " and key: " + key());
        super.appendToResponse(response, context);
    }
    
}
