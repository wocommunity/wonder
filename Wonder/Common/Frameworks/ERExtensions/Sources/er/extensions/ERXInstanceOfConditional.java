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
import java.lang.*;
import org.apache.log4j.Category;

////////////////////////////////////////////////////////////////////////////////////////////////////////
// Basic conditional that given an object and a class name (fully qualified) will evaluate true if the
// object is an instance of that class.
////////////////////////////////////////////////////////////////////////////////////////////////////////
public class ERXInstanceOfConditional extends WOComponent {

    public ERXInstanceOfConditional(WOContext aContext) {
        super(aContext);
    }

    ////////////////////////////////////////  log4j category  //////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXInstanceOfConditional.class);
    
    public boolean isStateless() { return true; }
    public void reset() { _instanceOf = null; }
    
    private Boolean _instanceOf;
    public boolean instanceOf() {
        if (_instanceOf == null) {
            Class instance = null;
            try {
                _NSUtilities.classWithName((String)valueForBinding("className"));
            } catch (Exception e) {
                cat.error("Invalid Class Name: " + valueForBinding("className") + " exception: " + e);
                throw new RuntimeException("Invalid Class Name: " + valueForBinding("className") + " exception: " + e);
            }
            _instanceOf = instance.isInstance(valueForBinding("object")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _instanceOf.booleanValue();
    }
}
