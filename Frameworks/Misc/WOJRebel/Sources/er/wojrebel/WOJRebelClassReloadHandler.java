package er.wojrebel;

import java.util.Enumeration;

import org.zeroturnaround.javarebel.ClassEventListener;
import org.zeroturnaround.javarebel.Logger;
import org.zeroturnaround.javarebel.LoggerFactory;
import org.zeroturnaround.javarebel.Reloader;
import org.zeroturnaround.javarebel.ReloaderFactory;

import com.webobjects.appserver.WOAction;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSUtilities;

/**
 * WOJRebelClassReloadHandler manages the clearing of KVC, component definition and class caches
 * when a class is reloaded by JRebel. Any cached ClassNotFound entries are also removed.
 * 
 * @author q
 *
 */
public class WOJRebelClassReloadHandler {

	private static boolean initialized = false;
		
	private boolean resetKVCCaches = false;
	private boolean resetComponentCache = false;
	private boolean resetActionClassCache = false;
	private boolean resetValidationCache = false;

	private static final WOJRebelClassReloadHandler instance = new WOJRebelClassReloadHandler();
	private static final Logger log = LoggerFactory.getInstance();

	private WOJRebelClassReloadHandler() { /* Private */ }
	
	public static WOJRebelClassReloadHandler getInstance() {
		return instance;
	}

	private void doReset() {
		if (resetKVCCaches) {
			resetKVCCaches = false;
			log.echo("JRebel: Resetting KeyValueCoding caches");
			NSKeyValueCoding.DefaultImplementation._flushCaches();
			NSKeyValueCoding._ReflectionKeyBindingCreation._flushCaches();
			NSKeyValueCoding.ValueAccessor._flushCaches();
		}
		if (resetComponentCache) {
		  resetComponentCache = false;
		  log.echo("JRebel: Resetting Component Definition cache");
		  WOApplication.application()._removeComponentDefinitionCacheContents();
		}
		if (resetActionClassCache) {
		  resetActionClassCache = false;
		  log.echo("JRebel: Resetting Action class cache");
		  WOClassCacheAccessor.clearActionClassCache();
		}
		if (resetValidationCache) {
		  resetValidationCache = false;
		  log.echo("JRebel: Resetting NSValidation cache");
		  NSValidation.DefaultImplementation._flushCaches();
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
			System.out.println("JRebel rapid turnaround mode is disabled because JRebel is not running "
					+ "\n    To use JRebel rapid turnaround you must add the following to your "
					+ "Java VM arguments:\n        -noverify -javaagent:<pathtojar>/jrebel.jar");
			System.out.println("    JRebel can be obtained from www.zeroturnaround.com");
			return;
		}

		log.echo("JRebel: WebObjects support enabled");
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
	  if (NSValidation.class.isAssignableFrom(clazz)) {
	    resetValidationCache = true;
	  }
	  doReset();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void updateLoadedClasses(NSNotification notification) {
		Reloader reloader = ReloaderFactory.getInstance();
		if (notification != null) {
		  WORequest request = (WORequest) notification.object();
		  String key = "/" + WOApplication.application().resourceRequestHandlerKey();
		  if (request.uri().indexOf(request.adaptorPrefix()) != 0 || request.uri().indexOf(key) >= 0) {
		    return;
		  }
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
				continue;
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
