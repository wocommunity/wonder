package ognl.helperfunction;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSKeyValueCoding._KeyBinding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation._NSUtilities;

/**
 * HelperFunctionRegistry provides a central point for registering and resolving helper functions.
 * 
 * @author mschrag
 */
public class WOHelperFunctionRegistry {
	private static final Logger log = LoggerFactory.getLogger(WOHelperFunctionRegistry.class);

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
		setHelperInstanceForClassInFrameworkNamed(helperInstance, null, targetObjectClass, frameworkName);
	}
	
	/**
	 * Sets the helper object to use for the given class.
	 * 
	 * @param helperInstance
	 *            an instance of the helper class (i.e. PersonHelper)
	 * @param helperFunction
	 * 			  the helper function being requested (i.e. formattedName)
	 * @param targetObjectClass
	 *            the class that maps to this helper instance (i.e. Person.class)
	 * @param frameworkName
	 *            the scoping of the helper instance (null, or "app" = global)
	 */
	protected synchronized void setHelperInstanceForClassInFrameworkNamed(Object helperInstance, String helperFunction, Class targetObjectClass, String frameworkName) {
		if (frameworkName == null) {
			frameworkName = WOHelperFunctionRegistry.APP_FRAMEWORK_NAME;
		}
		Map frameworkHelperInstanceCache = (Map) _applicationHelperInstanceCache.get(frameworkName);
		if (frameworkHelperInstanceCache == null) {
			frameworkHelperInstanceCache = new HashMap();
			_applicationHelperInstanceCache.put(frameworkName, frameworkHelperInstanceCache);
		}
		frameworkHelperInstanceCache.put(targetObjectClass, helperInstance);
		
		if (helperFunction != null) {
			frameworkHelperInstanceCache.put(targetObjectClass.getName() + helperFunction, helperInstance);
		}
	}

	protected synchronized Object _cachedHelperInstanceForFrameworkNamed(Class targetClass, String frameworkName) {
		return __cachedHelperInstanceForFrameworkNamed(targetClass, frameworkName);
	}

	protected synchronized Object _cachedHelperInstanceForFrameworkNamed(Class targetClass, String helperFunction, String frameworkName) {
		return __cachedHelperInstanceForFrameworkNamed(targetClass.getName() + "." + helperFunction, frameworkName);
	}
	
	protected synchronized Object __cachedHelperInstanceForFrameworkNamed(Object key, String frameworkName)	{
		Object helperInstance = null;
		Map frameworkHelperInstanceCache = (Map) _applicationHelperInstanceCache.get(frameworkName);
		if (frameworkHelperInstanceCache != null) {
			helperInstance = frameworkHelperInstanceCache.get(key);
		}
		return helperInstance;
	}
	
	public synchronized Object _helperInstanceForFrameworkNamed(Object targetObject, String helperFunction, String keyPath, String frameworkName) throws SecurityException, IllegalArgumentException, InstantiationException, IllegalAccessException {
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
			_KeyBinding keyBinding = WOHelperFunctionClassKeyValueCoding.DefaultImplementation.keyGetBindingForKeyPath(targetObject.getClass(), keyPath);
			if (keyBinding != null) {
				helpedClass = keyBinding.valueType();
			}
			else {
				log.warn("Unable to determine the value class of the keypath '{}' for the object {}", keyPath, targetObject);
				helpedClass = Object.class;
			}
		}

		Object helperInstance = null;
		helperInstance = _cachedHelperInstanceForFrameworkNamed(helpedClass, helperFunction, frameworkName);
		if (helperInstance == null && !WOHelperFunctionRegistry.APP_FRAMEWORK_NAME.equals(frameworkName)) {
			helperInstance = _cachedHelperInstanceForFrameworkNamed(helpedClass, helperFunction, WOHelperFunctionRegistry.APP_FRAMEWORK_NAME);
		}
		if (helperInstance == null) {
			//see if we have a cached helper, but we haven't cached it for the helperFunction
			helperInstance = _cachedHelperInstanceForFrameworkNamed(helpedClass, frameworkName);
			if (helperInstance == null && !WOHelperFunctionRegistry.APP_FRAMEWORK_NAME.equals(frameworkName)) {
				helperInstance = _cachedHelperInstanceForFrameworkNamed(helpedClass, WOHelperFunctionRegistry.APP_FRAMEWORK_NAME);
			}
			if (helperInstance != null) {
				if (classImplementsMethod(helperInstance.getClass(), helperFunction)) {
					setHelperInstanceForClassInFrameworkNamed(helperInstance, helperFunction, helpedClass, frameworkName);
				} else {
					helperInstance = null;
				}
			}
		}
		
		if (helperInstance == null) {
			Class targetHelperClass = helperClassForClass(helpedClass, helperFunction);
			if (targetHelperClass == null) {
				throw new NoSuchElementException("Could not find a helper class for '" + helpedClass.getName() + " implementing " + helperFunction + "'.");
			}
			helperInstance = targetHelperClass.newInstance();
			setHelperInstanceForClassInFrameworkNamed(helperInstance, helperFunction, helpedClass, frameworkName);
		}

		return helperInstance;
	}
	
	/**
	 * Attempts to locate a helper class for helpedClass that implements helperFunction.
	 * If it cannot find a class called &lt;className&gt;Helper implementing helperFunction, 
	 * it looks for a helper for each of the interfaces implemented by the class, and starts the 
	 * process over with the superclass if that fails.
	 * @param helpedClass
	 * @param helperFunction
	 * @return
	 */
	protected Class helperClassForClass(Class helpedClass, String helperFunction) {
		String targetClassName = helpedClass.getName();
		int lastDotIndex = targetClassName.lastIndexOf('.');
		if (lastDotIndex != -1) {
			targetClassName = targetClassName.substring(lastDotIndex + 1);
		}
		
		String targetHelperName = targetClassName + "Helper";
		Class helperClass = _NSUtilities.classWithName(targetHelperName);
		if (helperClass != null && classImplementsMethod(helperClass, helperFunction)) {
			return helperClass;
		}
		
		//check for a helper for the interfaces
		Class[] interfaces = helpedClass.getInterfaces();
		for(int i = 0; i < interfaces.length; i++) {
			helperClass = helperClassForClass(interfaces[i], helperFunction);
			if ( helperClass != null && classImplementsMethod(helperClass, helperFunction)) {
				return helperClass;
			}
		}
		
		//if that fails, try the super class
		Class superClass = helpedClass.getSuperclass();
		if (superClass != null) {
			return helperClassForClass(superClass, helperFunction);
		}
		
		return null;
	}
	
	protected boolean classImplementsMethod(Class theClass, String methodName) {
		Method[] methods = theClass.getMethods();
		for(int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)) {
				return true;
			}
		}
		return false;
	}
}
