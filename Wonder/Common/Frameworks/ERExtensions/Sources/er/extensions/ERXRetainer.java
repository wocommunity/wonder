/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import org.apache.log4j.Category;

// This is used to to retain pure Java objects on the objC side of things so that they can recieve notifications.
public class ERXRetainer {

    ///////////////////////////////////  log4j category  ///////////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXRetainer.class);

    private static NSMutableSet _retainerSet = new NSMutableSet();

    public static void retain(Object object) { _retainerSet.addObject(object); }
    public static void release(Object object) { _retainerSet.removeObject(object); }
    public static boolean isObjectRetained(Object object) { return _retainerSet.containsObject(object); }
}
