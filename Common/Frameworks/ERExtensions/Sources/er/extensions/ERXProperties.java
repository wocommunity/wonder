/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERProperties.java created by max on Sun 06-May-2001 */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import org.apache.log4j.Category;
import java.util.*;

///////////////////////////////////////////////////////////////////////////////////////////////////////
// This is a part transistional part needed object.  NSProperties in WO5 does not have any set methods
// 	so those will stay all of the get methods will be replaced by NSPropoperties.*ForKey
///////////////////////////////////////////////////////////////////////////////////////////////////////
public class ERXProperties {

    ////////////////////////////////////////////////  log4j category  ////////////////////////////////////////////
    public final static Category cat = Category.getInstance(ERXProperties.class);
    
    public static NSArray arrayForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? (NSArray)NSPropertyListSerialization.propertyListFromString(s1) : null;
    }

    public static boolean booleanForKey(String s) {
        return ERXUtilities.booleanValue(System.getProperty(s));
    }

    public static NSDictionary dictionaryForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? (NSDictionary)NSPropertyListSerialization.propertyListFromString(s1) : null;
    }

    public static int intForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? NSPropertyListSerialization.intForString(s1) : 0;
    }
    
    public static String stringForKey(String s) { return System.getProperty(s); }
    
    public static void setArrayForKey(NSArray array, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(array), key);
    }

    public static void setDictionaryForKey(NSDictionary array, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(array), key);
    }
    
    public static void setStringForKey(String string, String key) {
        Properties properties = System.getProperties();
        properties.put(key, string);
    }
}
