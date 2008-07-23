/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.HashMap;
import java.util.Map;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
/**
 * <code>ERXThreadStorage</code> provides a way to store objects for
 * a particular thread. This can be especially handy for storing objects
 * like the current actor or the current form name within the scope of
 * a thread handling a particular request. <br />
 * The system property <code>er.extensions.ERXThreadStorage.useInheritableThreadLocal</code> 
 * defines if the thread storage can be either inherited by client threads (default)
 * or get used only by the current thread. 
 */
public class ERXThreadStorage {

    /** Holds the single instance of the thread map. */
    private static ThreadLocal threadMap;
    
    static {
    	if(useInheritableThreadLocal()) {
    		threadMap = new ERXCloneableThreadLocal();
    	} else {
    		threadMap = new ThreadLocal();
    	}
    }

    /**
     * Checks the system property <code>er.extensions.ERXThreadStorage.useInheritableThreadLocal</code> 
     * to decide whether to use inheritable thread variables or not.
     * @return true if set (default)
     */
	private static boolean useInheritableThreadLocal() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXThreadStorage.useInheritableThreadLocal", true);
	}
    
    /** Holds the default initialization value of the hash map. */
    private static int DefaultHashMapSize = 10;

    /**
     * Sets a value for a particular key for a particular thread.
     * @param object value
     * @param key key
     */
    public static void takeValueForKey(Object object, String key) {
    	Map map = storageMap(true);
    	map.put(key, object);
    }

    /**
     * Removes the value in the map for a given key.
     * @param key key to be removed from the map.
     * @return the object corresponding to the key that
     *         was removed, null if nothing is found.
     */
    public static Object removeValueForKey(String key) {
        Map map = storageMap(false);
        return map != null ? map.remove(key) : null;
    }

    /**
     * Gets the object associated with the keypath in the storage
     * map off of the current thread.
     *
     * @param keyPath key path to be used to retrieve value from map.
     * @return the value stored in the map for the given key.
     */
    public static Object valueForKeyPath(String keyPath) {
        int dot = keyPath.indexOf(".");
        Object value = null;
        if(dot > 1) {
            value = valueForKey(keyPath.substring(0, dot));
            if(value != null) {
                value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(value, keyPath.substring(dot+1));
            }
        } else {
            value = valueForKey(keyPath);
        }
        return value;
    }
    
    /**
     * Gets the object associated with the key in the storage
     * map off of the current thread.
     * @param key key to be used to retrieve value from map.
     * @return the value stored in the map for the given key.
     */
    public static Object valueForKey(String key) {
        Map map = storageMap(false);
        Object result = null;
        if(map != null) {
        	result =  map.get(key);
        }
        return result;
    }
    
    
    /**
     * Gets the object associated with the key in the storage
     * map off of the current thread in the given editing context.
	 * Throws a ClassCastException when the value is not an EO.
     * @param ec editing context to retrieve the value into
     * @param key key to be used to retrieve value from map.
     * @return the value stored in the map for the given key.
     */
    public static Object valueForKey(EOEditingContext ec, String key) {
        Object result = valueForKey(key);
        if(result != null) {
            if (result instanceof EOEnterpriseObject) {
                EOEnterpriseObject eo = (EOEnterpriseObject) result;
                if(eo.editingContext() != null && eo.editingContext() != ec) {
                	eo.editingContext().lock();
                	try {
                		result = ERXEOControlUtilities.localInstanceOfObject(ec, eo);
                	} finally {
                		eo.editingContext().unlock();
                	}
                }
            } else {
               throw new ClassCastException("Expected EO, got : " + result.getClass().getName() + ", " + result);
            }
        }
        return result;
    }

    /**
     * Gets the storage map from the current thread.
     * At the moment this Map is syncronized for thread
     * safety. This might not be necessary in which case
     * users of this method would need to make sure that
     * they take the appropriate precautions.
     * @return Map object associated with this particular
     *         thread.
     */
    public static Map map() {
        return storageMap(true);
    }

    /**
     * Removes all of the keys from the current Map.
     */
    public static void reset() {
        Map map = storageMap(false);
        if (map != null)
            map.clear();
    }

    /**
     * Gets the {@link Map} from the thread map. Has the option to
     * to create the map if it hasn't been created yet for this thread.
     * Only used internally.
     * @param create should create the map storage if it isn't found.
     */
    private static Map storageMap(boolean create) {
        Map map = (Map)threadMap.get();
        if (map == null && create) {
            map = new HashMap(DefaultHashMapSize);
            threadMap.set(map);
        }
        return map;
    }
}
