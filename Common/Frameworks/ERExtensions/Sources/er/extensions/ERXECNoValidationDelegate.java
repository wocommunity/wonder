/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

public class ERXECNoValidationDelegate extends ERXDefaultEditingContextDelegate {

    ///////////////////////////  log4j category  ///////////////////////////
    public static final Category cat = Category.getInstance(ERXECNoValidationDelegate.class);

    // Turns off validation
    public boolean editingContextShouldValidateChanges(EOEditingContext anEditingContext) {
        return false;
    }
}
