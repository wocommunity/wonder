/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;

/**
 * Conditional component that tests if a given object 
 * is null.
 * <br/>
 * Synopsis:<br/>
 * condition=<i>aCondition</i>;[negate=<i>aBoolean</i>;]
 * 
 * @binding condtion object to test for nullality
 * @binding negate inverts the sense of the conditional.
 */
public class ERXNonNullConditional extends WOComponent {

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXNonNullConditional(WOContext aContext) {
        super(aContext);
    }
    
    /**
     * Component is stateless
     * @return true
     */
    public boolean isStateless() { return true; }
    
    /**
     * tests if the object returned from the binding 
     * condition is not null.
     * @return result of comparison.
     */
    public boolean isNonNull() { return valueForBinding("condition") != null; }
}
