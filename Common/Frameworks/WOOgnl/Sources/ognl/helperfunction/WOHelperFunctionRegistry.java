package ognl.helperfunction;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation._NSUtilities;
import com.webobjects.foundation.NSKeyValueCoding._KeyBinding;

/**
 * HelperFunctionRegistry provides a central point for registering and resolving helper functions.
 * 
 * @author mschrag
 */
public class WOHelperFunctionRegistry {
	public static final String APP_FRAMEWORK_NAME = "app";

	private static WOHelperFunctionRegistry _instance;
	private Map _applicationHelperInstanceCache;

	private WOHelperFunctionRegistry() {
		_applicationHelperInstanceCache = new HashMap();
	}

	public static synchronized WOHelperFunctionRegistry registry() {
		if (_instance == null) {
			_instance = new WOHelperFunctionRegistry();
		}
		return _instance;
	}

	/**
	 * Sets the helper object to use for the given class.
	 * 
	 * @param helperInstance
	 *            an instance of the helper class (i.e. PersonHelper)
	 * @param targetObjectClass
	 *            the class that maps to this helper instance (i.e. Person.class)
	 * @param frameworkName
	 *            the scoping of the helper instance (null, or "app" = global)
	 */
	public synchronized void setHelperInstanceForClassInFrameworkNamed(Object helperInstance, Class targetObjectClass, String frameworkName) {
		if (frameworkName == null) {
			frameworkName = WOHelperFunctionRegistry.APP_FRAMEWORK_NAME;
		}
		Map frameworkHelperInstanceCache = (Map) _applicationHelperInstanceCache.get(frameworkName);
		if (frameworkHelperInstanceCache == null) {
			frameworkHelperInstanceCache = new HashMap();
			_applicationHelperInstanceCache.put(frameworkName, frameworkHelperInstanceCache);
		}
		frameworkHelperInstanceCache.put(targetObjectClass, helperInstance);
	}

	protected synchronized Object _cachedHelperInstanceForFrameworkNamed(Class targetClass, String frameworkName) {
		Object helperInstance = null;
		Map frameworkHelperInstanceCache = (Map) _applicationHelperInstanceCache.get(frameworkName);
		if (frameworkHelperInstanceCache != null) {
			helperInstance = frameworkHelperInstanceCache.get(targetClass);
		}
		return helperInstance;
	}

	public synchronized Object _helperInstanceForFrameworkNamed(Object targetObject, String keyPath, String frameworkName) throws SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException {
		if (frameworkName == null) {
			frameworkName = WOHelperFunctionRegistry.APP_FRAMEWORK_NAME;
		}

		if (targetObject == null) {
			throw new IllegalArgumentException("The target of a helper keypath must not be null.");
		}
		if (keyPath == null) {
			throw new NullPointerException("You must specify a keypath to use helper functions.");
		}

		Object helpedObject = NSKeyValueCodingAdditions.Utility.valueForKeyPath(targetObject, keyPath);
		Class helpedClass;
		if (helpedObject != null) {
			helpedClass = helpedObject.getClass();
		}
		else {
			Object penultimateObject;
			String ultimateKey;
			int lastKeyPathDotIndex = keyPath.lastIndexOf('.');
			if (lastKeyPathDotIndex == -1) {
				penultimateObject = targetObject;
				ultimateKey = keyPath;
			}
			else {
				String penultimateKeyPath = keyPath.substring(0, lastKeyPathDotIndex);
				ultimateKey = keyPath.substring(lastKeyPathDotIndex + 1);
				penultimateObject = NSKeyValueCodingAdditions.Utility.valueForKeyPath(targetObject, penultimateKeyPath);
			}
			_KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(penultimateObject, ultimateKey);
			helpedClass = binding.valueType();
		}

		Object helperInstance = null;
		helperInstance = _cachedHelperInstanceForFrameworkNamed(helpedClass, frameworkName);
		if (helperInstance == null && !WOHelperFunctionRegistry.APP_FRAMEWORK_NAME.equals(frameworkName)) {
			helperInstance = _cachedHelperInstanceForFrameworkNamed(helpedClass, WOHelperFunctionRegistry.APP_FRAMEWORK_NAME);
		}
		if (helperInstance == null) {
			String targetClassName = helpedClass.getName();
			int lastDotIndex = targetClassName.lastIndexOf('.');
			if (lastDotIndex != -1) {
				targetClassName = targetClassName.substring(lastDotIndex + 1);
			}
			String targetHelperName = targetClassName + "Helper";
			Class targetHelperClass = _NSUtilities.classWithName(targetHelperName);
			if (targetHelperClass == null) {
				throw new NoSuchElementException("There is no helper class named '" + targetHelperName + "'.");
			}
			helperInstance = targetHelperClass.newInstance();
			setHelperInstanceForClassInFrameworkNamed(helperInstance, helpedClass, frameworkName);
		}

		return helperInstance;
	}
}
