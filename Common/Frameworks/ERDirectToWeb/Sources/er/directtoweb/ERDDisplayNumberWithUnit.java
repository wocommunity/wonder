/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class ERDDisplayNumberWithUnit extends ERDCustomEditComponent {

    public ERDDisplayNumberWithUnit(WOContext context) { super(context); }
    
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }

    public String unit() {
        return valueForBinding("unit") != null ? (String)valueForBinding("unit") :
        ERDirectToWeb.resolveUnit(ERDirectToWeb.userInfoUnit(object(),key()),object(),key());
    }
}
