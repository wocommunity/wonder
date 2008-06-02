/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import com.webobjects.eocontrol.EOEditingContext;


/**
 * Exactly the same as {@link ERXDefaultEditingContextDelegate},
 * except validation has been disabled.
 */
// ENHANCEME: This functionality could be folded into the super class with a 
public class ERXECNoValidationDelegate extends ERXEditingContextDelegate {

    //	===========================================================================
    //	Instance Constructor(s)
    //	---------------------------------------------------------------------------

    /**
     * Constructor needed for Serialable interface
     */
    public ERXECNoValidationDelegate() {}    

    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------    
    
    /**
     * Validation is disabled.
     * @param anEditingContext an editing context
     * @return false
     */
    public boolean editingContextShouldValidateChanges(EOEditingContext anEditingContext) {
        return false;
    }
}
