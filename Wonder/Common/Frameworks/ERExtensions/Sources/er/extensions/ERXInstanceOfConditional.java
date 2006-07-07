/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * Conditional component that tests if an object is an instance of a given
 * class or interface
 * <br/>
 * Synopsis:<br/>
 * object=<i>anObject</i>;className=<i>aClassName2</i>;[negate=<i>aBoolean</i>;]
 * 
 * @binding object object to test
 * @binding className class or interface name
 * @binding negate Inverts the sense of the conditional.
 */
public class ERXInstanceOfConditional extends WOComponent {

    /** Public constructor */
    public ERXInstanceOfConditional(WOContext aContext) {
        super(aContext);
    }

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXInstanceOfConditional.class);

    /** component is stateless */
    public boolean isStateless() { return true; }

    /** resets cached ivars */
    public void reset() {
        super.reset();
        _instanceOf = null;
    }
    /** cached value of comparison */
    private Boolean _instanceOf;

    /**
     * Tests if the bound object is an instance of the class.
     * Note: If the class is not found a ClassNotFoundException
     * will be thrown via an NSForwardException.
     * @return the boolean result of the <code>isInstance</code> test.
     */
    public boolean instanceOf() {
        if (_instanceOf == null) {
            Class instance = null;
            String className = (String)valueForBinding("className");
            if (log.isDebugEnabled())
                log.debug("Resolving class: " + className);
            instance = ERXPatcher.classForName(className);
            if (instance == null)
                throw new NSForwardException(new ClassNotFoundException((String)valueForBinding("className")));
            _instanceOf = instance.isInstance(valueForBinding("object")) ? Boolean.TRUE : Boolean.FALSE;
        }
        return _instanceOf.booleanValue();
    }
}
