/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.*;

/**
 * Collection of simple utility methods used to get and set properties
 * in the system properties. The only reason this class is needed is
 * because all of the methods in NSProperties have been deprecated.
 * This is a wee bit annoying. The usual method is to have a method
 * like <code>getBoolean</code> off of Boolean which would resolve
 * the System property as a Boolean object.
 */
public class ERXProperties {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERXProperties.class);

    /**
     * Cover method for returning an NSArray for a
     * given system property.
     * @param s system property
     * @return array de-serialized from the string in
     *		the system properties
     */
    public static NSArray arrayForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? (NSArray)NSPropertyListSerialization.propertyListFromString(s1) : null;
    }

    /**
     * Cover method for returning a boolean for a
     * given system property. This method uses the
     * method <code>booleanValue</code> from
     * {@link ERXUtilities}.
     * @param s system property
     * @return boolean value of the string in the
     *		system properties.
     */    
    public static boolean booleanForKey(String s) {
        return ERXUtilities.booleanValue(System.getProperty(s));
    }

    /**
     * Cover method for returning an NSDictionary for a
     * given system property.
     * @param s system property
     * @return dictionary de-serialized from the string in
     *		the system properties
     */    
    public static NSDictionary dictionaryForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? (NSDictionary)NSPropertyListSerialization.propertyListFromString(s1) : null;
    }

    /**
     * Cover method for returning an int for a
     * given system property.
     * @param s system property
     * @return int value of the system property or 0
     */
    public static int intForKey(String s) {
        String s1 = System.getProperty(s);
        return s1 != null ? NSPropertyListSerialization.intForString(s1) : 0;
    }

    // DELETEME: This isn't needed any more
    public static String stringForKey(String s) { return System.getProperty(s); }

    /**
     * Sets an array in the System properties for
     * a particular key.
     * @param array to be set in the System properties
     * @param key to be used to get the value
     */
    public static void setArrayForKey(NSArray array, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(array), key);
    }

    /**
     * Sets a dictionary in the System properties for
     * a particular key.
     * @param dictionary to be set in the System properties
     * @param key to be used to get the value
     */    
    public static void setDictionaryForKey(NSDictionary dictionary, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(dictionary), key);
    }

    /**
     * Sets a string in the System properties for
     * another string.
     * @param string to be set in the System properties
     * @param key to be used to get the value
     */
    public static void setStringForKey(String string, String key) {
        Properties properties = System.getProperties();
        properties.put(key, string);
    }
}
