/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.conditionals;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import er.extensions.ERXConstant;

/**
 * Conditional component that tests if a given Number
 * object is non-zero.
 * <br/>
 * Synopsis:<br/>
 * condition=<i>aCondition</i>;[negate=<i>aBoolean</i>;]
 * 
 * @binding condition numeric condition to test.
 * @binding negate inverts the sense of the conditional.
 */
public class ERXNonZeroConditional extends WOComponent {

    /**
     * Public constructor
     * @param aContext a context
     */
    public ERXNonZeroConditional(WOContext aContext) {
        super(aContext);
    }
    
    /**
     * Component is stateless.
     * @return true
     */
    public boolean isStateless() { return true; }
    
    /**
     * Tests if the condition not equal to zero or equal to
     * null.
     * @return if the condition is non-zero
     */
     // FIXME: Should be using intValue to test if it is equal to 0
     //		so that this can handle all the types of Number subclasses
     //		also could handle the string 0.
    public boolean isNonZero() {
        Object binding=valueForBinding("condition");
        return binding!=null && !binding.equals(ERXConstant.ZeroInteger);
    }
}
