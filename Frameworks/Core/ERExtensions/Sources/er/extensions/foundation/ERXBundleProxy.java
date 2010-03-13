package er.extensions.foundation;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarFile;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Provides proxying of the NSBundle class so as to not kick off NSBundle's
 * static constructor which immediately loads all available bundles on the
 * classpath. This is undesirable if you're interested in influencing the
 * environment settings prior to all bundles being loaded at runtime. e.g., unit
 * testing or application setup.
 * 
 * @author ldeck
 */
public class ERXBundleProxy {

	/**
	 * Interface to proxy NSBundle's instance methods.
	 * 
	 * @author ldeck
	 */
	public static interface INSBundle {
		/**
		 * @see com.webobjects.foundation.NSBundle#_bundleURLPrefix()
		 */
		String _bundleURLPrefix();

		/**
		 * @see com.webobjects.foundation.NSBundle#_classWithName(String)
		 */
		Class<?> _classWithName(String className);

		/**
		 * @see com.webobjects.foundation.NSBundle#_directoryExistsInJar(String)
		 */
		boolean _directoryExistsInJar(String path);

		/**
		 * @see com.webobjects.foundation.NSBundle#_infoDictionary()
		 */
		NSDictionary<String, String> _infoDictionary();

		/**
		 * @see com.webobjects.foundation.NSBundle#_isCFBundle()
		 */
		boolean _isCFBundle();

		/**
		 * @see com.webobjects.foundation.NSBundle#_jarFile()
		 */
		JarFile _jarFile();

		/**
		 * @see com.webobjects.foundation.NSBundle#_jarFileLayout()
		 */
		NSDictionary<String, String> _jarFileLayout();

		/**
		 * @see com.webobjects.foundation.NSBundle#_pathURLForResourcePath(String,
		 *      boolean)
		 */
		URL _pathURLForResourcePath(String resourcePath, boolean returnDirectories);

		/**
		 * @see com.webobjects.foundation.NSBundle#_simplePathsInDirectoryInJar(String,
		 *      String, NSMutableArray, String, NSMutableArray)
		 */
		void _simplePathsInDirectoryInJar(String startPath, String dirExtension, NSMutableArray<String> dirs, String fileExtension, NSMutableArray<String> files);

		/**
		 * @see com.webobjects.foundation.NSBundle#_urlForRelativePath(String)
		 */
		URL _urlForRelativePath(String path);

		/**
		 * @see com.webobjects.foundation.NSBundle#bundleClassNames()
		 */
		NSArray<String> bundleClassNames();

		/**
		 * @see com.webobjects.foundation.NSBundle#bundleClassPackageNames()
		 */
		NSArray<String> bundleClassPackageNames();

		/**
		 * @see com.webobjects.foundation.NSBundle#bundlePathURL()
		 */
		URL bundlePathURL();

		/**
		 * @see com.webobjects.foundation.NSBundle#bytesForResourcePath(String)
		 */
		byte[] bytesForResourcePath(String resourcePath);

		/**
		 * @see com.webobjects.foundation.NSBundle#equals(Object)
		 */
		boolean equals(Object object);

		/**
		 * @return the bundle hash code
		 * @see com.webobjects.foundation.NSBundle#hashCode()
		 */
		int hashCode();

		/**
		 * @see com.webobjects.foundation.NSBundle#inputStreamForResourcePath(String)
		 */
		InputStream inputStreamForResourcePath(String resourcePath);

		/**
		 * @see com.webobjects.foundation.NSBundle#isFramework()
		 */
		boolean isFramework();

		/**
		 * @see com.webobjects.foundation.NSBundle#isJar()
		 */
		boolean isJar();

		/**
		 * @see com.webobjects.foundation.NSBundle#name()
		 */
		String name();

		/**
		 * @see com.webobjects.foundation.NSBundle#pathURLForResourcePath(String)
		 */
		URL pathURLForResourcePath(String resourcePath);

		/**
		 * @see com.webobjects.foundation.NSBundle#principalClass()
		 */
		Class<?> principalClass();

		/**
		 * @see com.webobjects.foundation.NSBundle#properties()
		 */
		Properties properties();

		/**
		 * @see com.webobjects.foundation.NSBundle#resourcePathForLocalizedResourceNamed(String,
		 *      String)
		 */
		String resourcePathForLocalizedResourceNamed(String name, String subDirPath);

		/**
		 * @see com.webobjects.foundation.NSBundle#resourcePathsForDirectories(String,
		 *      String)
		 */
		NSArray<String> resourcePathsForDirectories(String extension, String subDirPath);

		/**
		 * @see com.webobjects.foundation.NSBundle#resourcePathsForLocalizedResources(String,
		 *      String)
		 */
		NSArray<String> resourcePathsForLocalizedResources(String extension, String subDirPath);

		/**
		 * @see com.webobjects.foundation.NSBundle#resourcePathsForResources(String,
		 *      String)
		 */
		NSArray<String> resourcePathsForResources(String extension, String subDirPath);
	}

	/**
	 * An interface to proxy NSBundle's static methods and fields.
	 * 
	 * @author ldeck
	 */
	public static interface IStaticNSBundle {
		/**
		 * @return all currently known bundles
		 * @see com.webobjects.foundation.NSBundle#_allBundlesReally()
		 */
		NSArray<INSBundle> _allBundlesReally();

		/**
		 * @param name
		 *            - the name of the bundle
		 * @return the bundle if found
		 * @see com.webobjects.foundation.NSBundle#_appBundleForName(String)
		 */
		INSBundle _appBundleForName(String name);

		/**
		 * @param path
		 *            - the path to the bundle
		 * @param shouldCreateBundle
		 *            - should initialise if not already found
		 * @param newIsJar
		 *            - is it a jar bundle
		 * @return the bundle if found, initialising if shouldCreateBundle is
		 *         true
		 * @see com.webobjects.foundation.NSBundle#_bundleWithPathShouldCreateIsJar(String,
		 *      boolean, boolean)
		 */
		INSBundle _bundleWithPathShouldCreateIsJar(String path, boolean shouldCreateBundle, boolean newIsJar);

		/**
		 * @param bundle
		 *            - the bundle to set
		 * @see com.webobjects.foundation.NSBundle#_setMainBundle(com.webobjects.foundation.NSBundle)
		 */
		void _setMainBundle(INSBundle bundle);

		/**
		 * Alias for field <code>BundleDidLoadNotification</code>
		 * 
		 * @return the notification key
		 * @see com.webobjects.foundation.NSBundle#BundleDidLoadNotification
		 */
		String bundleDidLoadNotificationString();

		/**
		 * @param clazz
		 *            - the class to look up
		 * @return the bundle containing the class if found or null
		 * @see com.webobjects.foundation.NSBundle#bundleForClass(Class)
		 */
		INSBundle bundleForClass(Class<?> clazz);

		/**
		 * @param name
		 *            - the name of the bundle
		 * @return the bundle if found or null
		 */
		INSBundle bundleForName(String name);

		/**
		 * Alias for static field <code>CFBUNDLESHORTVERSIONSTRINGKEY</code>.
		 * 
		 * @return the value of the static field referenced
		 * @see com.webobjects.foundation.NSBundle#CFBUNDLESHORTVERSIONSTRINGKEY
		 */
		String cfBundleShortVersionStringKey();

		/**
		 * @return the currently known framework bundles
		 * @see com.webobjects.foundation.NSBundle#frameworkBundles()
		 */
		NSArray<INSBundle> frameworkBundles();

		/**
		 * Alias for static field <code>LoadedClassesNotification</code>.
		 * 
		 * @return the notification key referenced
		 * @see com.webobjects.foundation.NSBundle#LoadedClassesNotification
		 */
		String loadedClassesNotificationString();

		/**
		 * @return the currently set main bundle or null
		 * @see com.webobjects.foundation.NSBundle#mainBundle()
		 */
		INSBundle mainBundle();

		/**
		 * Alias for static field <code>MANIFESTIMPLEMENTATIONVERSIONKEY</code>.
		 * 
		 * @return the field's value
		 * @see com.webobjects.foundation.NSBundle#MANIFESTIMPLEMENTATIONVERSIONKEY
		 */
		String manifestImplementationVersionKey();
	}

	/**
	 * Proxy implementation for an NSBundle instance.
	 * 
	 * @author ldeck
	 */
	static class NSBundleImpl implements INSBundle {

		private static NSMutableDictionary<String, Method> METHOD_CACHE = new NSMutableDictionary<String, Method>();
		private static final Lock METHOD_LOCK = new ReentrantLock();

		private static Method __method(String methodName) {
			Method reflection = null;

			METHOD_LOCK.lock();
			try {
				reflection = METHOD_CACHE.objectForKey(methodName);
				if (reflection == null) {
					for (Method method : StaticNSBundleImpl.__classObject().getDeclaredMethods()) {
						if (method.getName().equals(methodName)) {
							reflection = method;
							METHOD_CACHE.setObjectForKey(reflection, methodName);
							break;
						}
					}
				}
			}
			finally {
				METHOD_LOCK.unlock();
			}
			return reflection;
		}

		private final Object bundle;

		public NSBundleImpl(final Object object) {
			this.bundle = object;
			new StaticNSBundleImpl();
			if (!StaticNSBundleImpl.__classObject().isAssignableFrom(__bundle().getClass())) {
				throw new IllegalArgumentException("The given object is not an instanceof com.webobjects.foundation.NSBundle");
			}
		}

		Object __bundle() {
			return this.bundle;
		}

		@SuppressWarnings("unchecked")
		private <T> T __invokeNSBundle(String methodName, Object... args) {
			try {
				return (T) __method(methodName).invoke(__bundle(), args);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to invoke method " + methodName + " on NSBundle instance", e);
			}
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_bundleURLPrefix()
		 */
		public String _bundleURLPrefix() {
			return __invokeNSBundle("_bundleURLPrefix");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_classWithName(java.lang.String)
		 */
		public Class<?> _classWithName(String className) {
			return __invokeNSBundle("_classWithName", className);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_directoryExistsInJar(java.lang.String)
		 */
		public boolean _directoryExistsInJar(String path) {
			return (Boolean) __invokeNSBundle("_directoryExistsInJar", path);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_infoDictionary()
		 */
		public NSDictionary<String, String> _infoDictionary() {
			return __invokeNSBundle("_infoDictionary");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_isCFBundle()
		 */
		public boolean _isCFBundle() {
			return (Boolean) __invokeNSBundle("_isCFBundle");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_jarFile()
		 */
		public JarFile _jarFile() {
			return __invokeNSBundle("_jarFile");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_jarFileLayout()
		 */
		public NSDictionary<String, String> _jarFileLayout() {
			return __invokeNSBundle("_jarFileLayout");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_pathURLForResourcePath(java.lang.String,
		 *      boolean)
		 */
		public URL _pathURLForResourcePath(String resourcePath, boolean returnDirectories) {
			return __invokeNSBundle("_pathURLForResourcePath", resourcePath, returnDirectories);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_simplePathsInDirectoryInJar(java.lang.String,
		 *      java.lang.String, com.webobjects.foundation.NSMutableArray,
		 *      java.lang.String, com.webobjects.foundation.NSMutableArray)
		 */
		public void _simplePathsInDirectoryInJar(String startPath, String dirExtension, NSMutableArray<String> dirs, String fileExtension, NSMutableArray<String> files) {
			__invokeNSBundle("_simplePathsInDirectoryInJar", startPath, dirExtension, dirs, fileExtension, files);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#_urlForRelativePath(java.lang.String)
		 */
		public URL _urlForRelativePath(String path) {
			return __invokeNSBundle("_urlForRelativePath", path);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#bundleClassNames()
		 */
		public NSArray<String> bundleClassNames() {
			return __invokeNSBundle("bundleClassNames");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#bundleClassPackageNames()
		 */
		public NSArray<String> bundleClassPackageNames() {
			return __invokeNSBundle("bundleClassPackageNames");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#bundlePathURL()
		 */
		public URL bundlePathURL() {
			return __invokeNSBundle("bundlePathURL");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#bytesForResourcePath(java.lang.String)
		 */
		public byte[] bytesForResourcePath(String resourcePath) {
			return __invokeNSBundle("bytesForResourcePath", resourcePath);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#inputStreamForResourcePath(java.lang.String)
		 */
		public InputStream inputStreamForResourcePath(String resourcePath) {
			return __invokeNSBundle("inputStreamForResourcePath", resourcePath);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#isFramework()
		 */
		public boolean isFramework() {
			return (Boolean) __invokeNSBundle("isFramework");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#isJar()
		 */
		public boolean isJar() {
			return (Boolean) __invokeNSBundle("isJar");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#name()
		 */
		public String name() {
			return __invokeNSBundle("name");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#pathURLForResourcePath(java.lang.String)
		 */
		public URL pathURLForResourcePath(String resourcePath) {
			return __invokeNSBundle("pathURLForResourcePath", resourcePath);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#principalClass()
		 */
		public Class<?> principalClass() {
			return __invokeNSBundle("principalClass");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#properties()
		 */
		public Properties properties() {
			return __invokeNSBundle("properties");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#resourcePathForLocalizedResourceNamed(java.lang.String,
		 *      java.lang.String)
		 */
		public String resourcePathForLocalizedResourceNamed(String name, String subDirPath) {
			return __invokeNSBundle("resourcePathForLocalizedResourceNamed", name, subDirPath);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#resourcePathsForDirectories(java.lang.String,
		 *      java.lang.String)
		 */
		public NSArray<String> resourcePathsForDirectories(String extension, String subDirPath) {
			return __invokeNSBundle("resourcePathsForDirectories", extension, subDirPath);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#resourcePathsForLocalizedResources(java.lang.String,
		 *      java.lang.String)
		 */
		public NSArray<String> resourcePathsForLocalizedResources(String extension, String subDirPath) {
			return __invokeNSBundle("resourcePathsForLocalizedResources", extension, subDirPath);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.INSBundle#resourcePathsForResources(java.lang.String,
		 *      java.lang.String)
		 */
		public NSArray<String> resourcePathsForResources(String extension, String subDirPath) {
			return __invokeNSBundle("resourcePathsForResources", extension, subDirPath);
		}
	}

	/**
	 * Proxy implementation for NSBundle static methods and fields.
	 * 
	 * @author ldeck
	 * @see IStaticNSBundle
	 */
	static class StaticNSBundleImpl implements IStaticNSBundle {

		private static Class<?> __class = null;
		private static final NSMutableDictionary<String, Field> FIELD_CACHE = new NSMutableDictionary<String, Field>();
		private static final Lock LOCK = new ReentrantLock();
		private static final NSMutableDictionary<String, Method> METHOD_CACHE = new NSMutableDictionary<String, Method>();

		static Class<?> __classObject() {
			if (__class == null) {
				__initClassObject();
			}
			return __class;
		}

		private static Class<?> __initClassObject() {
			LOCK.lock();
			try {
				if (__class == null) {
					try {
						__class = Class.forName("com.webobjects.foundation.NSBundle");
					}
					catch (ClassNotFoundException e) {
						throw new RuntimeException("Failed to load NSBundle!", e);
					}
				}
				return __class;
			}
			finally {
				LOCK.unlock();
			}
		}

		private static Field __initFieldNamed(String fieldName) {
			LOCK.lock();
			Field result = null;
			try {
				result = __classObject().getDeclaredField(fieldName);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to invoke static field " + fieldName + " on NSBundle", e);
			}
			finally {
				LOCK.unlock();
			}
			return result;
		}

		private static Method __initMethodNamed(String methodName, int argsLength) {
			LOCK.lock();
			Method result = null;
			try {
				for (Method method : __classObject().getDeclaredMethods()) {
					if (method.getName().equals(methodName) && method.getParameterTypes().length == argsLength) {
						METHOD_CACHE.setObjectForKey(method, methodName);
						break;
					}
				}
			}
			finally {
				LOCK.unlock();
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		private <T> T __invokeNSBundle(String methodName, Object... args) {
			Method reflection = METHOD_CACHE.objectForKey(methodName);
			if (reflection == null) {
				reflection = __initMethodNamed(methodName, args.length);
			}
			try {
				return (T) reflection.invoke(__classObject(), args);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to invoke static method " + methodName + " on NSBundle", e);
			}
		}

		@SuppressWarnings("unchecked")
		private <T> T __invokeNSBundleField(String fieldName) {
			Field reflection = FIELD_CACHE.objectForKey(fieldName);
			if (reflection == null) {
				reflection = __initFieldNamed(fieldName);
			}
			try {
				return (T) reflection.get(__classObject());
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to invoke static field " + fieldName + " on NSBundle", e);
			}
		}

		private INSBundle __wrappedBundle(Object bundle) {
			return new NSBundleImpl(bundle);
		}

		private NSArray<INSBundle> __wrappedBundles(NSArray<?> bundles) {
			NSMutableArray<INSBundle> results = new NSMutableArray<INSBundle>();
			for (Object bundle : bundles) {
				results.addObject(__wrappedBundle(bundle));
			}
			return results.immutableClone();
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#_allBundlesReally()
		 */
		public NSArray<INSBundle> _allBundlesReally() {
			return __invokeNSBundle("_allBundlesReally");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#_appBundleForName(java.lang.String)
		 */
		public INSBundle _appBundleForName(String name) {
			return __wrappedBundle(__invokeNSBundle("_appBundleForName", name));
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#_bundleWithPathShouldCreateIsJar(java.lang.String,
		 *      boolean, boolean)
		 */
		public INSBundle _bundleWithPathShouldCreateIsJar(String path, boolean shouldCreateBundle, boolean newIsJar) {
			return __wrappedBundle(__invokeNSBundle("_bundleWithPathShouldCreateIsJar", path, shouldCreateBundle, newIsJar));
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#_setMainBundle(ish.erxtest.NSBundleProxy.INSBundle)
		 */
		public void _setMainBundle(INSBundle bundle) {
			__invokeNSBundle("_setMainBundle", bundle);
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#bundleDidLoadNotificationString()
		 */
		public String bundleDidLoadNotificationString() {
			return __invokeNSBundleField("BundleDidLoadNotification");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#bundleForClass(java.lang.Class)
		 */
		public INSBundle bundleForClass(Class<?> clazz) {
			return __wrappedBundle(__invokeNSBundle("bundleForClass", clazz));
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#bundleForName(java.lang.String)
		 */
		public INSBundle bundleForName(String name) {
			return __wrappedBundle(__invokeNSBundle("bundleForName", name));
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#cfBundleShortVersionStringKey()
		 */
		public String cfBundleShortVersionStringKey() {
			return __invokeNSBundleField("CFBUNDLESHORTVERSIONSTRINGKEY");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#frameworkBundles()
		 */
		public NSArray<INSBundle> frameworkBundles() {
			return __wrappedBundles((NSArray<?>) __invokeNSBundle("frameworkBundles"));
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#loadedClassesNotificationString()
		 */
		public String loadedClassesNotificationString() {
			return __invokeNSBundleField("LoadedClassesNotification");
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#mainBundle()
		 */
		public INSBundle mainBundle() {
			return __wrappedBundle(__invokeNSBundle("mainBundle"));
		}

		/**
		 * @see ish.erxtest.NSBundleProxy.IStaticNSBundle#manifestImplementationVersionKey()
		 */
		public String manifestImplementationVersionKey() {
			return __invokeNSBundleField("MANIFESTIMPLEMENTATIONVERSIONKEY");
		}

	}

	/**
	 * Factory for creating a concrete instance of IStaticNSBundle
	 * 
	 * @return a proxy for NSBundle static methods and fields
	 */
	public static IStaticNSBundle newStaticBundle() {
		return new StaticNSBundleImpl();
	}
}
