package er.rest.routes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

import er.rest.ERXRestContext;
import er.rest.ERXRestUtils;

/**
 * <p>
 * ERXRoute encapsulates a URL path with matching values inside of it. For instance, the route
 * "/company/{company:Company}/employees/{Person}/name/{name:String}" would yield an objects(..) dictionary with a
 * Company EO mapped to the key "company," a Person EO mapped to the key "Person" and a String mapped to the key "name".
 * ERXRoutes do not enforce any security -- they simply represent a way to map URL patterns onto objects.
 * </p>
 * 
 * @author mschrag
 */
public class ERXRoute {
	public static enum Method {
		All, Get, Put, Post, Delete, Head, Options, Trace, Connect
	}

	public static final ERXRoute.Key ControllerKey = new ERXRoute.Key("controller");
	public static final ERXRoute.Key ActionKey = new ERXRoute.Key("action");

	private final String _entityName;
	private final Pattern _routePattern;
	private ERXRoute.Method _method;
	private final NSMutableArray<ERXRoute.Key> _keys;
	private final Class<? extends ERXRouteController> _controller;
	private final String _action;

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 */
	public ERXRoute(String entityName, String urlPattern, ERXRoute.Method method) {
		this(entityName, urlPattern, method, (Class<? extends ERXRouteController>) null, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 */
	public ERXRoute(String entityName, String urlPattern) {
		this(entityName, urlPattern, ERXRoute.Method.All, (Class<? extends ERXRouteController>) null, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String entityName, String urlPattern, String controller) {
		this(entityName, urlPattern, ERXRoute.Method.All, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String entityName, String urlPattern, ERXRoute.Method method, String controller) {
		this(entityName, urlPattern, method, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 */
	public ERXRoute(String entityName, String urlPattern, Class<? extends ERXRouteController> controller) {
		this(entityName, urlPattern, ERXRoute.Method.All, controller, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 */
	public ERXRoute(String entityName, String urlPattern, ERXRoute.Method method, Class<? extends ERXRouteController> controller) {
		this(entityName, urlPattern, method, controller, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 * @param action
	 *            the action name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String entityName, String urlPattern, String controller, String action) {
		this(entityName, urlPattern, ERXRoute.Method.All, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), action);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 * @param action
	 *            the action name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String entityName, String urlPattern, ERXRoute.Method method, String controller, String action) {
		this(entityName, urlPattern, method, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), action);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 * @param action
	 *            the action name
	 */
	public ERXRoute(String entityName, String urlPattern, Class<? extends ERXRouteController> controller, String action) {
		this(entityName, urlPattern, ERXRoute.Method.All, controller, action);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param entityName
	 *            the name of the entity this route points to
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 * @param action
	 *            the action name
	 */
	public ERXRoute(String entityName, String urlPattern, ERXRoute.Method method, Class<? extends ERXRouteController> controller, String action) {
		_entityName = entityName;
		_method = method;
		_controller = controller;
		_action = action;

		Matcher keyMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(urlPattern);
		_keys = new NSMutableArray<ERXRoute.Key>();
		StringBuffer routeRegex = new StringBuffer();
		if (!urlPattern.startsWith("^")) {
			routeRegex.append('^');
		}
		while (keyMatcher.find()) {
			String keyStr = keyMatcher.group(1);
			String replacement = "([^/]+)";
			ERXRoute.Key key = new ERXRoute.Key();
			int colonIndex = keyStr.indexOf(':');
			if (colonIndex == -1) {
				key._key = keyStr;
				if (Character.isUpperCase(keyStr.charAt(0))) {
					key._valueType = keyStr;
				}
				else {
					key._valueType = String.class.getName();
				}
			}
			else {
				String[] segments = keyStr.split(":");
				key._key = segments[0];
				key._valueType = segments[1];
				if (segments.length == 3) {
					replacement = "(" + segments[2].replaceAll("[\\\\$]", "\\\\$0") + ")";
				}
			}

			if ("identifier".equals(key._valueType)) {
				key._valueType = String.class.getName();
				replacement = "(\\\\D[^/-]*)";
			}

			_keys.addObject(key);
			keyMatcher.appendReplacement(routeRegex, replacement);
		}
		keyMatcher.appendTail(routeRegex);
		if (!urlPattern.endsWith("$")) {
			if (routeRegex.lastIndexOf(".") == -1) {
				routeRegex.append("/?(\\..*)?");
			}
			routeRegex.append('$');
		}
		_routePattern = Pattern.compile(routeRegex.toString());
	}

	/**
	 * Returns the entity name of the target of this route (can be null).
	 * 
	 * @return the entity name of the target of this route
	 */
	public String entityName() {
		return _entityName;
	}
	
	/**
	 * Returns the controller class for this route.
	 * 
	 * @return the controller class for this route
	 */
	public Class<? extends ERXRouteController> controller() {
		return _controller;
	}
	
	/**
	 * @return the controller action name for this route.
	 */
	public String action() {
		return _action;
	}
	
	/**
	 * Returns the Pattern used to match this route.
	 * 
	 * @return the Pattern used to match this route
	 */
	public Pattern routePattern() {
		return _routePattern;
	}

	/**
	 * Returns the method of this request.
	 * 
	 * @return the method of this request
	 */
	public ERXRoute.Method method() {
		return _method;
	}

	/**
	 * Sets the method of this request.
	 * 
	 * @param method
	 *            the method of this request
	 */
	public void setMethod(ERXRoute.Method method) {
		_method = method;
	}

	/**
	 * Clears any caches that may exist on ERXRoutes (probably only useful to JRebel, to clear the route parameter method cache).
	 */
	public void _clearCaches() {
		for (ERXRoute.Key key : _keys) {
			key._clearRouteParameterMethodCache();
		}
		ERXRoute.ControllerKey._clearRouteParameterMethodCache();
		ERXRoute.ActionKey._clearRouteParameterMethodCache();
	}
	
	/**
	 * Returns the route keys for the given URL.
	 * 
	 * @param url
	 *            the URL to parse
	 */
	public NSDictionary<ERXRoute.Key, String> keys(String url, ERXRoute.Method method) {
		NSMutableDictionary<ERXRoute.Key, String> keys = null;

		if (_method == ERXRoute.Method.All || _method == null || method == null || method.equals(_method)) {
			Matcher routeMatcher = _routePattern.matcher(url);
			if (routeMatcher.matches()) {
				keys = new NSMutableDictionary<ERXRoute.Key, String>();
				int keyCount = _keys.count();
				for (int keyNum = 0; keyNum < keyCount; keyNum++) {
					ERXRoute.Key key = _keys.objectAtIndex(keyNum);
					String value = routeMatcher.group(keyNum + 1);
					keys.setObjectForKey(value, key);
				}

				if (!keys.containsKey(ERXRoute.ControllerKey) && _controller != null) {
					keys.setObjectForKey(_controller.getName(), ERXRoute.ControllerKey);
				}

				if (!keys.containsKey(ERXRoute.ActionKey) && _action != null) {
					keys.setObjectForKey(_action, ERXRoute.ActionKey);
				}
			}
		}

		return keys;
	}

	/**
	 * Returns a dictionary mapping the route's keys to their resolved objects.
	 * 
	 * @param url
	 *            the URL to process
	 * @param method
	 * @param context
	 *            the delegate to use to, for instance, fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's keys to their resolved objects
	 */
	public NSDictionary<ERXRoute.Key, Object> keysWithObjects(String url, ERXRoute.Method method, ERXRestContext context) {
		return keysWithObjects(keys(url, method), context);
	}

	/**
	 * Returns a dictionary mapping the route's key names to their resolved objects.
	 * 
	 * @param url
	 *            the URL to process
	 * @param context
	 *            the delegate to use to, for instance, fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's key names to their resolved objects
	 */
	public NSDictionary<String, Object> objects(String url, ERXRoute.Method method, ERXRestContext context) {
		return objects(keys(url, method), context);
	}

	/**
	 * Returns a dictionary mapping the route's keys to their resolved objects.
	 * 
	 * @param keys
	 *            the parsed keys to process
	 * @param context
	 *            the delegate to use to, for instance, fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's keys to their resolved objects
	 */
	public static NSDictionary<ERXRoute.Key, Object> keysWithObjects(NSDictionary<ERXRoute.Key, String> keys, ERXRestContext context) {
		NSMutableDictionary<ERXRoute.Key, Object> objects = null;
		if (keys != null) {
			objects = new NSMutableDictionary<ERXRoute.Key, Object>();
			for (Map.Entry<ERXRoute.Key, String> entry : keys.entrySet()) {
				ERXRoute.Key key = entry.getKey();
				String valueStr = entry.getValue();
				Object value = ERXRestUtils.coerceValueToTypeNamed(valueStr, key.valueType(), context, true);
				if (value != null) {
					objects.setObjectForKey(value, key);
				} else {
					objects = new NSMutableDictionary<ERXRoute.Key, Object>();
				}
			}
		}
		else {
			objects = new NSMutableDictionary<ERXRoute.Key, Object>();
		}
		return objects;
	}

	/**
	 * Returns a dictionary mapping the route's key names to their resolved objects.
	 * 
	 * @param keys
	 *            the parsed keys to process
	 * @param context
	 *            the delegate to use to, for instance, fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's key names to their resolved objects
	 */
	public NSDictionary<String, Object> objects(NSDictionary<ERXRoute.Key, String> keys, ERXRestContext context) {
		NSMutableDictionary<String, Object> objects = null;
		if (keys != null) {
			objects = new NSMutableDictionary<String, Object>();
			for (Map.Entry<ERXRoute.Key, String> entry : keys.entrySet()) {
				ERXRoute.Key key = entry.getKey();
				String valueStr = entry.getValue();
				Object value = ERXRestUtils.coerceValueToTypeNamed(valueStr, key.valueType(), context, true);
				objects.setObjectForKey(value, key._key);
			}
		}
		return objects;
	}

	@Override
	public String toString() {
		return "[ERXRoute: pattern=" + _routePattern + "; method=" + _method + "; controller=" + _controller + "; action=" + _action + "; keys=" + _keys.valueForKey("key") + "]";
	}

	/**
	 * ERXRoute.Key encapsulates a key name and an expected value type.
	 * 
	 * @author mschrag
	 */
	public static class Key {
		protected String _valueType;
		protected String _key;
		private final Map<Class<?>, RouteParameterMethod> _routeParameterMethodCache;

		public Key(String key) {
			this(key, String.class.getName());
		}

		public Key(String key, String valueType) {
			this();
			_key = key;
			_valueType = valueType;
		}

		protected Key() {
			_routeParameterMethodCache = new ConcurrentHashMap<Class<?>, RouteParameterMethod>();
		}

		public String key() {
			return _key;
		}

		public String valueType() {
			return _valueType;
		}
		
		public void _clearRouteParameterMethodCache() {
			_routeParameterMethodCache.clear();
		}
		
		public RouteParameterMethod _routeParameterMethodForClass(Class<?> resultsClass) {
			return _routeParameterMethodCache.get(resultsClass);
		}
		
		public void _setRouteParameterMethodForClass(RouteParameterMethod routeParameter, Class<?> resultsClass) {
			_routeParameterMethodCache.put(resultsClass, routeParameter);
		}

		@Override
		public int hashCode() {
			return _key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ERXRoute.Key && ((ERXRoute.Key) obj)._key.equals(_key);
		}

		@Override
		public String toString() {
			return "[ERXRoute.Key: " + _key + "]";
		}
	}
	
	public static class RouteParameterMethod {
		private final java.lang.reflect.Method _method;
		private boolean _stringParameter;
		
		public RouteParameterMethod(java.lang.reflect.Method method) {
			_method = method;
			if (_method != null) {
				Class<?>[] parameterTypes = _method.getParameterTypes();
				if (parameterTypes.length != 1) {
					throw new IllegalArgumentException("The route parameter method '" + method + "' must take a single parameter.");
				}
				_stringParameter = String.class.isAssignableFrom(parameterTypes[0]);
			}
		}
		
		public boolean isStringParameter() {
			return _stringParameter;
		}
		
		public boolean hasMethod() {
			return _method != null;
		}
		
		public java.lang.reflect.Method method() {
			return _method;
		}
		
		@Override
		public String toString() {
			return "[ERXRoute.RouteParameterMethod: method=" + (_method == null ? "(none)" : _method.toString()) + "]";
		}
	}
}
