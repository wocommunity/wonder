package er.extensions.foundation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.lang3.ObjectUtils;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXConstant;

/**
 * Utilities for use with key value coding. You could instantiate one of these in your app-startup:
 * <pre><code>
 * ERXKeyValueCodingUtilities.registerClass(SomeClass.class); 
 * NSKeyValueCodingAdditions statics = ERXKeyValueCodingUtilities.Statics;
 * myValue = statics.valueForKeyPath("SomeClass.SOME_FIELD");
 * </code></pre>
 * Also has utilities for getting and private fields and methods on an object. 
 * @author ak
 */
public class ERXKeyValueCodingUtilities {
    
    private static Hashtable _classes = new Hashtable();
    
    /**
     * Registers the class in the KVC resolving system, so you can use 
     * <code>valueForKeyPath("MyClass.SOME_KEY")</code>. Inner classes
     * are registered with a "$", i.e. <code>MyClass$InnerClass</code>
     *
     * @param clazz
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
     * @return value object
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
                        	// AK: should have a check for existence of KeyValueCodingProtectedAccessor here
                        	// AK: disabled, only for testing
                        	// boolean isAccessible = current.isAccessible();
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

    /**
     * Returns final strings constants from an interface or class. Useful in particular when you want to create
     * selection lists from your interfaces automatically. 
     * @param c
     * @return list of final string constants
     */
    public static NSArray<ERXKeyValuePair> staticStringsForClass(Class c) {
		NSMutableArray<ERXKeyValuePair> result = new NSMutableArray();
		if(c.getSuperclass() != null) {
			result.addObjectsFromArray(staticStringsForClass(c.getSuperclass()));
		}
		Field[] fields = c.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			try {
				if(Modifier.isFinal(field.getModifiers()) && field.getType().equals(String.class)) {
					String key = field.getName();
					String value = (String) field.get(c);
					result.addObject(new ERXKeyValuePair(key, value));
				}
			}
			catch (IllegalArgumentException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			catch (IllegalAccessException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
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
        	takeValueForKeyPath(arg0, arg1);
        }
    };

    public static Object privateValueForKey(Object target, String key) {
    	Field field = accessibleFieldForKey(target, key);
    	try {
    		if(field != null) {
        		return field.get(target);
    		}
			Method method = accessibleMethodForKey(target, key);
			if(method != null) {
				return method.invoke(target, (Object[]) null);
			}
			throw new NSKeyValueCoding.UnknownKeyException("Key "+ key + " not found", target, key);
    	}
    	catch (IllegalArgumentException e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
    	catch (IllegalAccessException e) {
    		throw NSForwardException._runtimeExceptionForThrowable(e);
    	}
		catch (InvocationTargetException e) {
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

    private static Method accessibleMethodForKey(Object target, String key) {
    	Method f = methodForKey(target, key);
    	if(f != null) {
    		f.setAccessible(true);
    	}
    	return f;
    }

    public static Field fieldForKey(Object target, String key) {
    	Field result = null;
    	Class c = target.getClass();
    	while (c != null) {
    		try {
    			result = c.getDeclaredField(key);
    			if(result != null) { 
    				return result;
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

    public static Method methodForKey(Object target, String key) {
    	Method result = null;
    	Class c = target.getClass();
    	while (c != null) {
    		try {
    			result = c.getDeclaredMethod(key);
    			if(result != null) { 
    				return result;
    			}
    		}
    		catch (SecurityException e) {
    			throw NSForwardException._runtimeExceptionForThrowable(e);
    		}
    		catch (NoSuchMethodException e) {
    			c = c.getSuperclass();
    		}
    	}
    	return null;
    }
    
    /**
     * Works like takeValuesFromDictionary, except that it only calls takeValueForKey if the current value
     * is different than the new value.
     *
     * @param object the object on which to operate
     * @param dictionary the key-value pairs to set
     */
    public static void takeChangedValuesFromDictionary(Object object, NSDictionary<String, ?> dictionary) {
    	if (dictionary == null) {
    		return;
    	}
    	
    	// do this check only once in the beginning instead of using NSKeyValueCoding.Utility below
    	NSKeyValueCoding keyValueCodingObject = (object instanceof NSKeyValueCoding) ? (NSKeyValueCoding)object : null;
    
    	NSArray<String> keys = dictionary.allKeys();
    	int count = keys.count();
    	for (int i = 0; i < count; i++) {
    		String key = keys.objectAtIndex(i);
    		Object value = dictionary.objectForKey(key);
    		if (value == NSKeyValueCoding.NullValue) {
    			value = null;
    		}
    		
    		if (keyValueCodingObject != null) {
    			if (ObjectUtils.notEqual(value, keyValueCodingObject.valueForKey(key))) {
    				keyValueCodingObject.takeValueForKey(value, key);
    			}
    		}
    		else {
    			if (ObjectUtils.notEqual(value, NSKeyValueCoding.DefaultImplementation.valueForKey(object, key))) {
    				NSKeyValueCoding.DefaultImplementation.takeValueForKey(object, value, key);
    			}
    		}
    	}
    }
}
