/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* NSFoundationElementsAccessor.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import java.util.Enumeration;

import ognl.ElementsAccessor;

import com.webobjects.foundation.NSSelector;

public class NSFoundationElementsAccessor implements ElementsAccessor {
    private static NSSelector sel = new NSSelector( "objectEnumerator" );

    public Enumeration getElements(Object target) {
        try {
            return (Enumeration) sel.invoke( target );    
        } catch( Exception e ) {
            throw new RuntimeException("NSFoundationElementsAccessor being used with a non-foundation class: " + target.getClass().getName());
        }
    }
    
}
