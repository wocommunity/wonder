/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Conditional component that compares two objects using the <code>equals</code> method.<br/>
 * <br/>
 * Synopsis:<br/>
 * value1=<i>anObject1</i>;value2=<i>anObject2</i>;[negate=<i>aBoolean</i>;] 
 *
 * @binding value1 first object to compare
 * @binding value2 second object to compare
 * @binding negate Inverts the sense of the conditional.
 */
public class ERXEqualConditional extends WOComponent {

    /** 
     * Public constructor
     * @param aContext current WOContext
     */
    public ERXEqualConditional(WOContext aContext) {
        super(aContext);
    }

    /** component is stateless */
    public boolean isStateless() { return true; }
    
    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    /**
     * Tests for the equality of the two value bindings. First tests a direct
     * <code>==</code> comparision then tests with an <code>equals</code> comparision.
     * @return equality of the two bindings.
     */
    public boolean areEqual() {
        Object v1=valueForBinding("value1");
        Object v2=valueForBinding("value2");
        if((v1 instanceof EOEnterpriseObject) && (v2 instanceof EOEnterpriseObject)) {
        	return ERXEOControlUtilities.eoEquals((EOEnterpriseObject)v1, (EOEnterpriseObject)v2);
        }
        return v1==v2 || (v1!=null && v2!=null && v1.equals(v2));
    }
}
