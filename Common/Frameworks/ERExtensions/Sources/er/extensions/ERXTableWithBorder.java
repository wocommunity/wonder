/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

public class ERXTableWithBorder extends WOComponent {

    public ERXTableWithBorder(WOContext aContext) {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }      


    // renderBorder false can be used to not output a table at all
    // this is useful since NetScape 4.7 and earlier get extremely slow
    // when table nesting gets past a certain level
    
    public boolean renderBorder() {
        return ERXUtilities.booleanValueForBindingOnComponentWithDefault("renderBorder", this, true);
    }

    public Object border() {
        Object result = (hasBinding("border") ? valueForBinding("border") : null);
        if (result==null) result=ERXConstant.OneInteger;
        return result;
    }
    
}
