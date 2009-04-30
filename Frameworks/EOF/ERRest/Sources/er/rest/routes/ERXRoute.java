package er.rest.routes;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

import er.rest.ERXRestUtils;

/**
 * ERXRoute encapsulates a URL path with matching values inside of it. For instance, the route
 * "/company/{company:Company}/employees/{Person}/name/{name:String}" would yield an objects(..) dictionary with a
 * Company EO mapped to the key "company," a Person EO mapped to the key "Person" and a String mapped to the key "name".
 * ERXRoutes do not enforce any security -- they simply represent a way to map URL patterns onto objects.
 * 
 * @author mschrag
 */
public class ERXRoute {
	public static enum Method { All, Get, Put, Post, Delete, Head }
	
	public static final ERXRoute.Key ControllerKey = new ERXRoute.Key("controller");
	public static final ERXRoute.Key ActionKey = new ERXRoute.Key("action");

	private Pattern _routePattern;
	private ERXRoute.Method _method;
	private NSMutableArray<ERXRoute.Key> _keys;
	private Class<? extends ERXRouteController> _controller;
	private String _action;

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 */
	public ERXRoute(String urlPattern, ERXRoute.Method method) {
		this(urlPattern, method, (Class<? extends ERXRouteController>) null, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 */
	public ERXRoute(String urlPattern) {
		this(urlPattern, ERXRoute.Method.All, (Class<? extends ERXRouteController>) null, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String urlPattern, String controller) {
		this(urlPattern, ERXRoute.Method.All, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String urlPattern, ERXRoute.Method method, String controller) {
		this(urlPattern, method, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 */
	public ERXRoute(String urlPattern, Class<? extends ERXRouteController> controller) {
		this(urlPattern, ERXRoute.Method.All, controller, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 */
	public ERXRoute(String urlPattern, ERXRoute.Method method, Class<? extends ERXRouteController> controller) {
		this(urlPattern, method, controller, null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 * @param action
	 *            the action name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String urlPattern, String controller, String action) {
		this(urlPattern, ERXRoute.Method.All, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), action);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class name
	 * @param action
	 *            the action name
	 */
	@SuppressWarnings("unchecked")
	public ERXRoute(String urlPattern, ERXRoute.Method method, String controller, String action) {
		this(urlPattern, method, _NSUtilities.classWithName(controller).asSubclass(ERXRouteController.class), action);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 * @param action
	 *            the action name
	 */
	public ERXRoute(String urlPattern, Class<? extends ERXRouteController> controller, String action) {
		this(urlPattern, ERXRoute.Method.All, controller, action);
	}
	
	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 * @param action
	 *            the action name
	 */
	public ERXRoute(String urlPattern, ERXRoute.Method method, Class<? extends ERXRouteController> controller, String action) {
		_method = method;
		_controller = controller;
		_action = action;

		Matcher keyMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(urlPattern);
		_keys = new NSMutableArray<ERXRoute.Key>();
		StringBuffer routeRegex = new StringBuffer();
		if (!urlPattern.startsWith("^")) {
			routeRegex.append("^");
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
				key._key = keyStr.substring(0, colonIndex);
				key._valueType = keyStr.substring(colonIndex + 1);
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
			routeRegex.append("$");
		}
		_routePattern = Pattern.compile(routeRegex.toString());
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
	 * @param method the method of this request
	 */
	public void setMethod(ERXRoute.Method method) {
		_method = method;
	}

	/**
	 * Returns the route keys for the given URL.
	 * 
	 * @param url
	 *            the URL to parse
	 * @return
	 */
	public NSDictionary<ERXRoute.Key, String> keys(String url, ERXRoute.Method method) {
		NSMutableDictionary<ERXRoute.Key, String> keys = null;

		if (_method == ERXRoute.Method.All || _method == null || method == null || method.equals(_method)) {
			Matcher routeMatcher = _routePattern.matcher(url);
			if (routeMatcher.matches()) {
				keys = new NSMutableDictionary<ERXRoute.Key, String>();
				int groupCount = routeMatcher.groupCount();
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
	 * @param editingContext
	 *            the editing context to fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's keys to their resolved objects
	 */
	public NSDictionary<ERXRoute.Key, Object> keysWithObjects(String url, ERXRoute.Method method, EOEditingContext editingContext) {
		return keysWithObjects(keys(url, method), editingContext);
	}

	/**
	 * Returns a dictionary mapping the route's key names to their resolved objects.
	 * 
	 * @param url
	 *            the URL to process
	 * @param editingContext
	 *            the editing context to fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's key names to their resolved objects
	 */
	public NSDictionary<String, Object> objects(String url, ERXRoute.Method method, EOEditingContext editingContext) {
		return objects(keys(url, method), editingContext);
	}

	/**
	 * Returns a dictionary mapping the route's keys to their resolved objects.
	 * 
	 * @param keys
	 *            the parsed keys to process
	 * @param editingContext
	 *            the editing context to fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's keys to their resolved objects
	 */
	public NSDictionary<ERXRoute.Key, Object> keysWithObjects(NSDictionary<ERXRoute.Key, String> keys, EOEditingContext editingContext) {
		NSMutableDictionary<ERXRoute.Key, Object> objects = null;
		if (keys != null) {
			objects = new NSMutableDictionary<ERXRoute.Key, Object>();
			for (Map.Entry<ERXRoute.Key, String> entry : keys.entrySet()) {
				ERXRoute.Key key = entry.getKey();
				String valueStr = entry.getValue();
				Object value = ERXRestUtils.coerceValueToTypeNamed(valueStr, key.valueType(), editingContext);
				objects.setObjectForKey(value, key);
			}
		}
		return objects;
	}

	/**
	 * Returns a dictionary mapping the route's key names to their resolved objects.
	 * 
	 * @param keys
	 *            the parsed keys to process
	 * @param editingContext
	 *            the editing context to fault EO's with (or null to not fault EO's)
	 * @return a dictionary mapping the route's key names to their resolved objects
	 */
	public NSDictionary<String, Object> objects(NSDictionary<ERXRoute.Key, String> keys, EOEditingContext editingContext) {
		NSMutableDictionary<String, Object> objects = null;
		if (keys != null) {
			objects = new NSMutableDictionary<String, Object>();
			for (Map.Entry<ERXRoute.Key, String> entry : keys.entrySet()) {
				ERXRoute.Key key = entry.getKey();
				String valueStr = entry.getValue();
				Object value = ERXRestUtils.coerceValueToTypeNamed(valueStr, key.valueType(), editingContext);
				objects.setObjectForKey(value, key._key);
			}
		}
		return objects;
	}
	
	@Override
	public String toString() {
		return "[ERXRoute: pattern=" + _routePattern + "]";
	}

	/**
	 * ERXRoute.Key encapsulates a key name and an expected value type.
	 * 
	 * @author mschrag
	 */
	public static class Key {
		protected String _valueType;
		protected String _key;

		protected Key() {
		}

		public Key(String key) {
			this(key, String.class.getName());
		}

		public Key(String key, String valueType) {
			_key = key;
			_valueType = valueType;
		}

		public String key() {
			return _key;
		}

		public String valueType() {
			return _valueType;
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
}
