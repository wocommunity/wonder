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

/**
 * Exactly the same as {@link ERXDefaultEditingContextDelegate},
 * except validation has been disabled.
 */
public class ERXECNoValidationDelegate extends ERXDefaultEditingContextDelegate {

    /**
     * Validation is disabled.
     * @param anEditingContext an editing context
     * @return false
     */
    public boolean editingContextShouldValidateChanges(EOEditingContext anEditingContext) {
        return false;
    }
}
