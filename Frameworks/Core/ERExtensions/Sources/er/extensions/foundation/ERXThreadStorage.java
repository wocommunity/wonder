/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.
 */
package er.extensions.foundation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSSet;

import er.extensions.appserver.ERXApplication;
import er.extensions.concurrency.ERXCloneableThreadLocal;
import er.extensions.eof.ERXEOControlUtilities;
/**
 * Provides a way to store objects for a particular thread. This can be especially handy for storing objects
 * like the current actor or the current form name within the scope of a thread handling a particular request.
 * <p>
 * The system property <code>er.extensions.ERXThreadStorage.useInheritableThreadLocal</code> defines if the
 * thread storage can be either inherited by client threads (default) or get used only by the current thread.
 * The usage of some types of objects inherited from the parent thread can cause problems.
 * </p><p>
 * The system property <code>er.extensions.ERXThreadStorage.logUsageOfProblematicInheritedValues</code>
 * defines, if potential problems should be logged. This defaults to <code>true</code> when running in development mode
 * and to <code>false</code> when running a deployed application.
 */
public class ERXThreadStorage {
	private static final Logger log = LoggerFactory.getLogger(ERXThreadStorage.class);
	public static final String KEYS_ADDED_IN_CURRENT_THREAD_KEY = "ERXThreadStorage.keysAddedInCurrentThread";
    public static final String WAS_CLONED_MARKER = "ERXThreadStorage.wasCloned";

	private static Set<Class<?>> _problematicTypes ;
	private static Set<String> _problematicKeys;
	
    /** Holds the single instance of the thread map. */
    private static ThreadLocal threadMap;
    
    private static Boolean _useInheritableThreadLocal;
    private static Boolean _logUsageOfProblematicInheritedValues;

    static {
    	if(useInheritableThreadLocal()) {
    		threadMap = new ERXThreadStorageCloneableThreadLocal();
    	} else {
    		threadMap = new ThreadLocal();
    	}
    	
    	_problematicTypes = new NSSet<Class<?>>(
    			new Class[] {
    				WOSession.class, 
    				WOContext.class, 
    				EOEnterpriseObject.class, 
    				EOEditingContext.class
    			}
    	);
    	
    	_problematicKeys = new NSSet<String>(
    			new String[] {
    					// already handled by "_problematcTypes"
    					// ERXWOContext.CONTEXT_DICTIONARY_KEY
    			}
    	);
    }

    /**
     * Checks the system property <code>er.extensions.ERXThreadStorage.useInheritableThreadLocal</code> 
     * to decide whether to use inheritable thread variables or not.
     * @return true if set (default)
     */
    private static boolean useInheritableThreadLocal() {
    	if (_useInheritableThreadLocal == null) {
    		_useInheritableThreadLocal = Boolean.valueOf(ERXProperties.booleanForKeyWithDefault("er.extensions.ERXThreadStorage.useInheritableThreadLocal", true));
    	}
    	return _useInheritableThreadLocal.booleanValue();
    }
    
    /**
     * Checks the system property <code>er.extensions.ERXThreadStorage.logUsageOfProblematicInheritedValues</code> 
     * to decide whether to log potential problems when using certain values inherited by the parent thread.
     * Only applies if using inheritable thread variables.
     * @return true if set (default)
     */
	private static boolean logUsageOfProblematicInheritedValues() {
		if (_logUsageOfProblematicInheritedValues == null) {
			boolean devMode = ERXApplication.isDevelopmentModeSafe();
			_logUsageOfProblematicInheritedValues = Boolean.valueOf(useInheritableThreadLocal() && ERXProperties.booleanForKeyWithDefault("er.extensions.ERXThreadStorage.logUsageOfProblematicInheritedValues", devMode));
		}
		return _logUsageOfProblematicInheritedValues.booleanValue();
	}
    
    /** Holds the default initialization value of the hash map. */
    private static int DefaultHashMapSize = 10;

    /**
     * Sets a value for a particular key for a particular thread.
     * @param object value
     * @param key key
     */
    public static void takeValueForKey(Object object, String key) {
    	// log.debug("{} <- {}", key, object);
    	Map map = storageMap(true);
    	map.put(key, object);
    	markKeyAddedInCurrentThread(key);
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
		if (map != null) {
			result = map.get(key);
		}
		
		// warn if the storageMap was inherited from another thread and it is
		// possibly problematic to use an object of this type in a background thread
		if (result != null && logUsageOfProblematicInheritedValues() && !wasKeyAddedInCurrentThread(key)) {
			for(Class<?> type: problematicTypes()) {
				if(type.isAssignableFrom(result.getClass())) {
					log.warn("The object for key '{}' was inherited from the parent thread. " +
							"The usage of inherited objects that are a subclass of '{}' can cause problems.",
							key, type.getSimpleName(), new Exception("DEBUG"));
				}
			}
			if(problematicKeys().contains(key)) {
				log.warn("The object for key '{}' was inherited from the parent thread. " +
						"The usage of inherited objects for this key can cause problems.", key, new Exception("DEBUG"));
			}
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
     * @return the map for the current thread or null
     */
    private static Map storageMap(boolean create) {
        Map map = (Map)threadMap.get();
        if (map == null && create) {
            map = new HashMap(DefaultHashMapSize);
            threadMap.set(map);
        }
        return map;
    }
    
    /**
     * Registers that a key was added in the current thread.
     * Only applies if the storageMap was inherited from the parent thread.
     * @param key to bless
     */
    private static void markKeyAddedInCurrentThread(String key) {
		if (wasInheritedFromParentThread()) {
			Map map = storageMap(false);
			Set blessedKeys = (Set<String>) map.get(KEYS_ADDED_IN_CURRENT_THREAD_KEY);
			if (blessedKeys == null) {
				blessedKeys = new HashSet<String>();
				map.put(KEYS_ADDED_IN_CURRENT_THREAD_KEY, blessedKeys);
			}
			blessedKeys.add(key);
		}
	}
    
    /**
     * Checks if a key was added in the current thread.
     * Only applies if the storageMap was inherited from the parent thread.
     * @param key to check
     * @return boolean indicating if the key was added in the current thread
     */
    private static boolean wasKeyAddedInCurrentThread(String key) {
    	if(!wasInheritedFromParentThread()) {
    		return true;
    	}
    	Map map = storageMap(false);
    	Set blessedKeys = (Set<String>) map.get(KEYS_ADDED_IN_CURRENT_THREAD_KEY);
    	return blessedKeys != null && blessedKeys.contains(key);
    }
    
    /**
     * Checks if the storageMap was inherited from the parent thread.
     * @return boolean indicating if the storageMap was inherited from another thread
     */
    public static boolean wasInheritedFromParentThread() {
		boolean result = false;
		if (useInheritableThreadLocal()) {
			Map map = storageMap(false);
			if (map != null) {
				result = ERXValueUtilities.booleanValue(map.get(ERXThreadStorage.WAS_CLONED_MARKER));
			}
		}
		return result;
	}

	/**
	 * Set the Set of classes for which a warning is issued when the storageMap
	 * was inherited from another Thread and the object retrieved from the map
	 * is a subclass of one of the classes in the set.
	 * 
	 * @param problematicTypes
	 *            a set of classes to check
	 */
	public static void setProblematicTypes(NSSet<Class<?>> problematicTypes) {
		_problematicTypes = problematicTypes == null ? NSSet.EmptySet : problematicTypes;
	}

	/**
	 * Retrieve the Set of classes for which a warning is issued when the
	 * storageMap was inherited from another Thread and the object retrieved
	 * from the map is a subclass of one of the classes in the set. Defaults to
	 * a Set containing WOSession.class, WOContext.class,
	 * EOEnterpriseObject.class and EOEditingContext.class
	 * 
	 * @return the set of classes to check
	 */
	public static Set<Class<?>> problematicTypes() {
		return _problematicTypes;
	}

	/**
	 * Set the Set of keys for which a warning is issued when the storageMap
	 * was inherited from another Thread and the key is accessed. 
	 * 
	 * @param problematicKeys
	 *            a set of keys to check
	 */
	public static void setProblematicKeys(Set<String> problematicKeys) {
		_problematicKeys = problematicKeys == null ? NSSet.EmptySet : problematicKeys;
	}

	/**
	 * Retrieve the Set of keys for which a warning is issued when the storageMap
	 * was inherited from another Thread and the key is accessed. Defaults to a
	 * set containing ERXWOContext.CONTEXT_DICTIONARY_KEY
	 * @return the set of keys to check
	 */
	public static Set<String> problematicKeys() {
		return _problematicKeys;
	}
	
	protected static class ERXThreadStorageCloneableThreadLocal extends ERXCloneableThreadLocal {
		@Override
		protected Object childValue(Object parentValue) {
			Map map = (Map) super.childValue(parentValue);
	        if (map != null) {
				// set marker indicating that the map was inherited from the parent thread
				map.put(ERXThreadStorage.WAS_CLONED_MARKER, Boolean.TRUE);
				// reset set of blessed keys
				map.remove(ERXThreadStorage.KEYS_ADDED_IN_CURRENT_THREAD_KEY);
			}
			return map;
		}
	}
}
