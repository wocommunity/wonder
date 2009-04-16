package er.rest;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * ERXRoute encapsulates a URL path with matching values inside of it. For instance, the route
 * "/company/{company:Company}/employees/{Person}/name/{name:String}" would yield an objects(..) dictionary with a
 * Company EO mapped to the key "company," a Person EO mapped to the key "Person" and a String mapped to the key "name".
 * ERXRoutes do not enforce any security -- they simply represent a way to map URL patterns onto objects.
 * 
 * @author mschrag
 */
public class ERXRoute {
	public static final ERXRoute.Key ControllerKey = new ERXRoute.Key("controller");
	public static final ERXRoute.Key ActionKey = new ERXRoute.Key("action");

	private Pattern _routePattern;
	private NSMutableArray<ERXRoute.Key> _keys;
	private Class<? extends ERXRouteDirectAction> _controller;
	private String _action;

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 */
	public ERXRoute(String urlPattern) {
		this(urlPattern, (Class<? extends ERXRouteDirectAction>) null, null);
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
		this(urlPattern, _NSUtilities.classWithName(controller).asSubclass(ERXRouteDirectAction.class), null);
	}

	/**
	 * Constructs a new route with the given URL pattern.
	 * 
	 * @param urlPattern
	 *            the url pattern to use
	 * @param controller
	 *            the default controller class
	 */
	public ERXRoute(String urlPattern, Class<? extends ERXRouteDirectAction> controller) {
		this(urlPattern, controller, null);
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
		this(urlPattern, _NSUtilities.classWithName(controller).asSubclass(ERXRouteDirectAction.class), action);
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
	public ERXRoute(String urlPattern, Class<? extends ERXRouteDirectAction> controller, String action) {
		_controller = controller;
		_action = action;

		Matcher keyMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(urlPattern);
		_keys = new NSMutableArray<ERXRoute.Key>();
		StringBuffer routeRegex = new StringBuffer();
		while (keyMatcher.find()) {
			String keyStr = keyMatcher.group(1);
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
			_keys.addObject(key);
			keyMatcher.appendReplacement(routeRegex, "([^/]+)");
		}
		keyMatcher.appendTail(routeRegex);
		_routePattern = Pattern.compile(routeRegex.toString());
	}

	/**
	 * Returns the route keys for the given URL.
	 * 
	 * @param url
	 *            the URL to parse
	 * @return
	 */
	public NSDictionary<ERXRoute.Key, String> keys(String url) {
		NSMutableDictionary<ERXRoute.Key, String> keys = null;

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
				keys.setObjectForKey(_controller.getClass().getName(), ERXRoute.ControllerKey);
			}

			if (!keys.containsKey(ERXRoute.ActionKey) && _action != null) {
				keys.setObjectForKey(_action, ERXRoute.ActionKey);
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
	public NSDictionary<ERXRoute.Key, Object> keysWithObjects(String url, EOEditingContext editingContext) {
		return keysWithObjects(keys(url), editingContext);
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
	public NSDictionary<String, Object> objects(String url, EOEditingContext editingContext) {
		return objects(keys(url), editingContext);
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
				Object value = objectValue(valueStr, key, editingContext);
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
				Object value = objectValue(valueStr, key, editingContext);
				objects.setObjectForKey(value, key._key);
			}
		}
		return objects;
	}

	/**
	 * Returns the given object coerced into the desired value as defined by the value type of the given route key.
	 * 
	 * @param obj
	 *            the object to convert
	 * @param key
	 *            the ERXRoute.Key to convert with
	 * @param editingContext
	 *            the editing context to fault EO's in from (or null to not fault EO's)
	 * @return the coerced value
	 */
	public Object objectValue(Object obj, ERXRoute.Key key, EOEditingContext editingContext) {
		Object value;
		if (ERXValueUtilities.isNull(obj)) {
			value = null;
		}
		else if ("String".equals(key._valueType) || java.lang.String.class.getName().equals(key._valueType)) {
			value = obj.toString();
		}
		else if ("Boolean".equals(key._valueType) || java.lang.Boolean.class.getName().equals(key._valueType)) {
			value = ERXValueUtilities.BooleanValueWithDefault(obj, null);
		}
		// else if ("Byte".equals(valueType) || java.lang.Byte.class.getName().equals(valueType)) {
		// value = ERXValueUtilities.ByteValueWithDefault(obj, null);
		// }
		// else if ("Short".equals(valueType) || java.lang.Short.class.getName().equals(valueType)) {
		// value = ERXValueUtilities.ShortValueWithDefault(obj, null);
		// }
		else if ("Integer".equals(key._valueType) || java.lang.Integer.class.getName().equals(key._valueType)) {
			value = ERXValueUtilities.IntegerValueWithDefault(obj, null);
		}
		else if ("Long".equals(key._valueType) || java.lang.Long.class.getName().equals(key._valueType)) {
			value = ERXValueUtilities.LongValueWithDefault(obj, null);
		}
		else if ("Float".equals(key._valueType) || java.lang.Float.class.getName().equals(key._valueType)) {
			value = ERXValueUtilities.FloatValueWithDefault(obj, null);
		}
		else if ("Double".equals(key._valueType) || java.lang.Double.class.getName().equals(key._valueType)) {
			value = ERXValueUtilities.DoubleValueWithDefault(obj, null);
		}
		else if ("BigDecimal".equals(key._valueType) || java.math.BigDecimal.class.getName().equals(key._valueType)) {
			value = ERXValueUtilities.DoubleValueWithDefault(obj, null);
		}
		else if (editingContext != null) {
			EOEntity entity = ERXEOAccessUtilities.entityNamed(editingContext, key._valueType);
			if (entity != null) {
				Object pkValue = ((EOAttribute) entity.primaryKeyAttributes().objectAtIndex(0)).validateValue(obj);
				value = ERXEOControlUtilities.objectWithPrimaryKeyValue(editingContext, entity.name(), pkValue, null, false);
			}
			else {
				throw new IllegalArgumentException("Unknown value type '" + key._valueType + "'.");
			}
		}
		else {
			value = obj;
		}
		return value;
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
