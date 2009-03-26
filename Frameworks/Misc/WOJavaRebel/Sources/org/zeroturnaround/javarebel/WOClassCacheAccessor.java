package org.zeroturnaround.javarebel;

import java.lang.reflect.Field;

import com.webobjects.appserver.WOAction;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;
import com.webobjects.foundation._NSUtilities;

/**
 * WOClassCacheAccessor provides direct access to _NSUtilities private class 
 * lookup cache and a mechanism for the direct retrieval and removal of entries.  
 * 
 * @author q
 *
 */
public class WOClassCacheAccessor {
	private static boolean initialized = false;
    private static _NSThreadsafeMutableDictionary classesByPartialName = null;
    private static _NSThreadsafeMutableDictionary actionClassCache = null;

	public static void setClassForName(Class<?> objectClass, String className) {
		classesByPartialName.setObjectForKey(objectClass, className);
	}

	public static void removeClassForName(Object className) {
		classesByPartialName.removeObjectForKey(className);
	}

	public static Class<?> classForName(String className) {
		return (Class<?>) classesByPartialName.objectForKey(className);
	}

    public static NSDictionary getClassCache() {
        return classesByPartialName.immutableClone();
    }

    public static void clearActionClassCache() {
        actionClassCache.removeAllObjects();
    }

	private static void initialize() {
		if (initialized)
			return;
		initialized = true;
		try {
			Field f = _NSUtilities.class.getDeclaredField("_classesByPartialName");
			f.setAccessible(true);
			classesByPartialName = (_NSThreadsafeMutableDictionary) f.get(null);
			f = WOAction.class.getDeclaredField("_actionClasses");
            f.setAccessible(true);
            actionClassCache = (_NSThreadsafeMutableDictionary) f.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static {
		initialize();
	}
}
