/*
 * Created on 08.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import java.lang.reflect.*;
import java.util.*;

import com.webobjects.foundation.*;

/**
 * Utilities for use with key value coding. You could instantiate one of these in your app-startup:
 * <code>
 *
 * public NSKeyValueCodingUtilities statics = ERXKeyValueCodingUtilities.Statics;
 * 
 * ERXKeyValueCodingUtilities.registerClass(SomeClass.class); 
 * myValue = valueForKeyPath("statics.SomeClass.SOME_FIELD");
 * </code>
 * 
 * @author ak
 */
public class ERXKeyValueCodingUtilities {
    
    private static Hashtable _classes = new Hashtable();
    
    /**
     * Registers the class in the KVC resolving system, so you can use 
     * <code>valueForKeyPath("MyClass.SOME_KEY")</code>. Inner classes
     * are registered with a "$", i.e. <code>MyClass$InnerClass</code>
     * @param clazz
     * @param name
     */
    public static void registerClass(Class clazz) {
        _classes.put(ERXStringUtilities.lastPropertyKeyInKeyPath(clazz.getName()), clazz);
    }
    
    /**
     * Extends key-value coding to a class. You can currently only
     * get the static fields of the class, not method results.
     * Java arrays and collections are morphed into NSArrays. The implementation is pretty slow,
     * but I didn't exactly want to re-implement all of NSKeyValueCoding here.
     * @param clazz
     * @param key
     * @return
     */
    public static Object classValueForKey(Class clazz, String key) {
        Object result = null;
        if(key != null) {
            try {
                String getKey = "get" + ERXStringUtilities.capitalize(key);
                Method methods[] = clazz.getDeclaredMethods();
                boolean found = false;
                for (int i = 0; i < methods.length && !found; i++) {
                    Method current = methods[i];
                    if(current.getParameterTypes().length == 0) {
                        if(current.getName().equals(key) ||
                                current.getName().equals(getKey)) {
                            result = current.invoke(clazz, ERXConstant.EmptyObjectArray);
                            found = true;
                        }
                    }
                }
                if(!found) {
                    Field fields[] = clazz.getDeclaredFields();
                    for (int i = 0; i < fields.length && !found; i++) {
                        Field current = fields[i];
                        if(current.getName().equals(key)) {
                            result = current.get(clazz);
                            found = true;
                        }
                    }
                }
                if(!found) {
                    throw new NSKeyValueCoding.UnknownKeyException("Key + " + key + " not found in class " + clazz.getName(), clazz, key);
                }
            } catch (Exception e) {
                throw new NSForwardException(e);
            }
            if(result != null) {
                if(result.getClass().getComponentType() != null) {
                    result = new NSArray((Object[])result);
                } else if(result instanceof Collection) {
                    NSMutableArray array = new NSMutableArray();
                    for (Iterator iter = ((Collection)result).iterator(); iter.hasNext();) {
                        array.addObject(iter.next());
                    }
                    result = array;
                }
            }
        }
        return result;
    }
    
    
    public static final NSKeyValueCodingAdditions Statics = new NSKeyValueCodingAdditions() {
        /**
         * @see com.webobjects.foundation.NSKeyValueCodingAdditions#valueForKeyPath(java.lang.String)
         */
        public Object valueForKeyPath(String arg0) {
            String name = ERXStringUtilities.firstPropertyKeyInKeyPath(arg0);
            String rest = ERXStringUtilities.keyPathWithoutFirstProperty(arg0);
            Class clazz = (Class) _classes.get(name);
            if(clazz == null) {
                throw new IllegalArgumentException("Class not found: " + arg0);
            }
            String field = ERXStringUtilities.keyPathWithoutFirstProperty(arg0);
            Object result = ERXKeyValueCodingUtilities.classValueForKey(clazz, field);
            
            if(field.length() > rest.length()) {
                rest = ERXStringUtilities.keyPathWithoutFirstProperty(arg0);
                result = NSKeyValueCodingAdditions.Utility.valueForKeyPath(result, rest);
            }
            return result;
        }
        
        /**
         * @see com.webobjects.foundation.NSKeyValueCodingAdditions#takeValueForKeyPath(java.lang.Object, java.lang.String)
         */
        public void takeValueForKeyPath(Object arg0, String arg1) {
            throw new UnsupportedOperationException("Can't set values on class yet");
        }
        
        /**
         * @see com.webobjects.foundation.NSKeyValueCoding#valueForKey(java.lang.String)
         */
        public Object valueForKey(String arg0) {
            return valueForKeyPath(arg0);
        }
        
        /**
         * @see com.webobjects.foundation.NSKeyValueCoding#takeValueForKey(java.lang.Object, java.lang.String)
         */
        public void takeValueForKey(Object arg0, String arg1) {
            takeValueForKey(arg0, arg1);
        }
    };
}
