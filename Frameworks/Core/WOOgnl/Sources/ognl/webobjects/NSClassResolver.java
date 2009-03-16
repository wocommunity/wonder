/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* NSClassResolver.java created by max on Sat 29-Sep-2001 */
package ognl.webobjects;

import java.util.Map;

import ognl.ClassResolver;

import com.webobjects.foundation._NSUtilities;

public class NSClassResolver implements ClassResolver {

    protected static NSClassResolver _sharedInstance;
    public static NSClassResolver sharedInstance() {
        if (_sharedInstance == null)
            _sharedInstance = new NSClassResolver();
        return _sharedInstance;
    }
    
    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Class c1 = _NSUtilities.classWithName(className);
        if (c1 == null)
            throw new ClassNotFoundException(className);
        return c1;
    }
}
