package er.wojrebel;

import java.lang.reflect.Field;
import java.util.Map;

import com.webobjects.appserver.WOAction;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSThreadsafeMutableDictionary;
import com.webobjects.foundation._NSUtilities;

/**
 * WOClassCacheAccessor provides direct access to _NSUtilities private class 
 * lookup cache and a mechanism for the direct retrieval and removal of entries.  
 * 
 * @author qdolan
 *
 */
public class WOClassCacheAccessor {
	private static boolean initialized = false;
  private static Object classesByPartialName = null;
  private static _NSThreadsafeMutableDictionary actionClassCache = null;

	public static void setClassForName(Class<?> objectClass, String className) {
		if (classesByPartialName instanceof _NSThreadsafeMutableDictionary) {
			((_NSThreadsafeMutableDictionary)classesByPartialName).setObjectForKey(objectClass, className);
		} else {
			((Map)classesByPartialName).put(className, objectClass);
		}
	}

	public static void removeClassForName(Object className) {
		if (classesByPartialName instanceof _NSThreadsafeMutableDictionary) {
			((_NSThreadsafeMutableDictionary)classesByPartialName).removeObjectForKey(className);
		} else {
			((Map)classesByPartialName).remove(className);
		}
	}

	public static Class<?> classForName(String className) {
		if (classesByPartialName instanceof _NSThreadsafeMutableDictionary) {
			return (Class<?>) ((_NSThreadsafeMutableDictionary)classesByPartialName).objectForKey(className);
		}
		return (Class<?>) ((Map)classesByPartialName).get(className); 
	}

	public static NSDictionary getClassCache() {
		if (classesByPartialName instanceof _NSThreadsafeMutableDictionary) {
			return new NSDictionary(((_NSThreadsafeMutableDictionary)classesByPartialName).immutableClone());
		}
		return new NSDictionary((Map)classesByPartialName);
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
	    classesByPartialName = f.get(null);
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
