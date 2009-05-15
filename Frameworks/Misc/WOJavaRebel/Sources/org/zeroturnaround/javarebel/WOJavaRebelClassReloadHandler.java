package org.zeroturnaround.javarebel;

import java.lang.reflect.Field;
import java.util.Enumeration;

import com.webobjects.appserver.WOAction;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver._private.WODirectActionRequestHandler;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

/**
 * WOJavaRebelClassReloadHandler manages the clearing of KVC, component definition and class caches
 * when a class is reloaded by JavaRebel. Any cached ClassNotFound entries are also removed.
 * 
 * @author q
 *
 */
public class WOJavaRebelClassReloadHandler {

	private static boolean initialized = false;
		
	private boolean resetKVCCaches = false;
    private boolean resetComponentCache = false;
    private boolean resetActionClassCache = false;

	private static final WOJavaRebelClassReloadHandler handler = new WOJavaRebelClassReloadHandler();

	private WOJavaRebelClassReloadHandler() { /* Private */ }
	
	public static WOJavaRebelClassReloadHandler getClassHandler() {
		return handler;
	}

	private void doReset() {
		if (resetKVCCaches) {
			resetKVCCaches = false;
			System.out.println("JavaRebel: Resetting KeyValueCoding caches");
			NSKeyValueCoding.DefaultImplementation._flushCaches();
			NSKeyValueCoding._ReflectionKeyBindingCreation._flushCaches();
			NSKeyValueCoding.ValueAccessor._flushCaches();
		}
		if (resetComponentCache) {
		    resetComponentCache = false;
		    System.out.println("JavaRebel: Resetting Component Definition cache");
		    WOApplication.application()._removeComponentDefinitionCacheContents();
		}
		if(resetActionClassCache) {
		    resetActionClassCache = false;
            System.out.println("JavaRebel: Resetting Action class cache");
		    WOClassCacheAccessor.clearActionClassCache();
		}
	}

	public void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;

		if (WOApplication.application() != null && WOApplication.application().isCachingEnabled()) {
			System.out.println("Running in deployment mode. Rapid turnaround is disabled");
			return;
		}

		if (!isReloadEnabled()) {
			System.out.println("JavaRebel rapid turnaround mode is disabled because JavaRebel is not running "
					+ "\n    To use JavaRebel rapid turnaround you must add the following to your "
					+ "Java VM arguments:\n        -noverify -javaagent:<pathtojar>/javarebel.jar");
			System.out.println("    JavaRebel can be obtained from www.zeroturnaround.com");
			return;
		}

		System.out.println("JavaRebel: WebObjects support enabled");
		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("updateLoadedClasses", 
				new Class[] { NSNotification.class }),
				WOApplication.ApplicationWillDispatchRequestNotification, null);
		WOEventClassListener listener = new WOEventClassListener();
		Reloader reloader = ReloaderFactory.getInstance();
		reloader.addClassReloadListener(listener);
		reloader.addClassLoadListener(listener);
	}

	@SuppressWarnings("all")
	public void reloaded(Class clazz) {
		resetKVCCaches = true;
        if (WOComponent.class.isAssignableFrom(clazz)) {
            resetComponentCache = true;
        }
        if (WOAction.class.isAssignableFrom(clazz)) {
            resetActionClassCache = true;
        }
		doReset();
	}
	
	@SuppressWarnings("unchecked")
	public void updateLoadedClasses(NSNotification n) {
		Reloader reloader = ReloaderFactory.getInstance();
		WORequest request = (WORequest) n.object();
		String key = "/" + WOApplication.application().resourceRequestHandlerKey();
		if (request.uri().indexOf(request.adaptorPrefix()) != 0 || request.uri().indexOf(key) >= 0) {
			return;
		}
		NSDictionary classList = WOClassCacheAccessor.getClassCache();
		String unknownClassName = "com.webobjects.foundation._NSUtilities$_NoClassUnderTheSun";
		Class<?> unknownClass = WOClassCacheAccessor.classForName(unknownClassName);
		Enumeration<String> en = classList.keyEnumerator();
		while(en.hasMoreElements()) {
			String className = en.nextElement();
			if (className.equals(unknownClassName)) {
				continue;
			}
			Class<?> clazz = WOClassCacheAccessor.classForName(className);

			if (clazz != null && clazz.isPrimitive()) {
				continue;
			}

			if (clazz == null || clazz.equals(unknownClass)) {
				WOClassCacheAccessor.removeClassForName(className);
			}
			reloader.checkAndReload(clazz);
		}
		doReset();
	}
	
	public boolean isReloadEnabled() {
		return ReloaderFactory.getInstance().isReloadEnabled();
	}

	private class WOEventClassListener implements ClassEventListener {
		@SuppressWarnings("unchecked")
		public void onClassEvent(int eventType, Class clazz) {
			if (eventType == ClassEventListener.EVENT_RELOADED) {
				reloaded(clazz);
			}
		}

    public int priority() {
      return 0;
    }
	}
}
