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
    public final static Category cat = Category.getInstance("er.extensions.ERProperties");

    public static String stringForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? s1 : NSProperties.stringForKey(s);
    }
    
    public static NSArray arrayForKey(String s) {
        String s1 = System.getProperty(s);
        NSArray nsarray;
        if(s1 == null) {
            nsarray = NSProperties.arrayForKey(s);
            nsarray = nsarray != null ? nsarray : ERXConstant.EmptyArray;            
        } else
            nsarray = (NSArray)NSPropertyListSerialization.propertyListFromString(s1);
        return nsarray;
    }

    public static boolean booleanForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? ERXUtilities.booleanValue(s1) : NSProperties.booleanForKey(s);
    }

    public static NSDictionary dictionaryForKey(String s) {
        String s1 = System.getProperty(s);
        NSDictionary nsdictionary;
        if (s1 == null) {
            nsdictionary = NSProperties.dictionaryForKey(s);
            nsdictionary = nsdictionary != null ? nsdictionary : ERXConstant.EmptyDictionary;            
        } else
            nsdictionary = (NSDictionary)NSPropertyListSerialization.propertyListFromString(s1);
        return nsdictionary;
    }
    
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
