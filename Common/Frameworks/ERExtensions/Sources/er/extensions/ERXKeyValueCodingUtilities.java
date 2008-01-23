/*
 * Created on 08.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

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
	 * Extends key-value coding to a class. Java arrays and collections are
	 * morphed into NSArrays. The implementation is pretty slow, but I didn't
	 * exactly want to re-implement all of NSKeyValueCoding here.
	 * 
	 * @param clazz
	 * @param key
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
                        	boolean isAccessible = current.isAccessible();
                        	// AK: should have a check for existance of KeyValueCodingProtectedAccessor here
                        	// AK: disabled, only for testing
                        	// current.setAccessible(true);
                        	result = current.get(clazz);
                        	// current.setAccessible(isAccessible);
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
                    NSMutableArray array = new NSMutableArray(((Collection)result).size());
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

    public static Object privateValueForKey(Object target, String key) {
    	Field field = accessibleFieldForKey(target, key);
    	try {
    		if(field != null) {
        		return field.get(target);
    		} else {
    	    	throw new NSKeyValueCoding.UnknownKeyException("Key "+ key + " not found", target, key);
    		}
    	}
    	catch (IllegalArgumentException e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
    	catch (IllegalAccessException e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
    }

    public static void takePrivateValueForKey(Object target, Object value, String key) {
    	Field field = accessibleFieldForKey(target, key);
    	try {
    		if(field != null) {
        		field.set(target, value);
    		} else {
    	    	throw new NSKeyValueCoding.UnknownKeyException("Key "+ key + " not found", target, key);
    		}
    	}
    	catch (IllegalArgumentException e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
    	catch (IllegalAccessException e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
    }

    private static Field accessibleFieldForKey(Object target, String key) {
    	Field f = fieldForKey(target, key);
    	if(f != null) {
    		f.setAccessible(true);
    	}
    	return f;
    }
    

    public static Field fieldForKey(Object target, String key) {
    	Field field = null;
    	Class c = target.getClass();
    	while(field == null && c != null) {
    		try {
    			field = c.getDeclaredField(key);
    			if(field != null) { 
    				return field;
    			}
    		}
    		catch (SecurityException e) {
    			throw NSForwardException._runtimeExceptionForThrowable(e);
    		}
    		catch (NoSuchFieldException e) {
    			c = c.getSuperclass();
    		}
    	}
    	return null;
    }
}
