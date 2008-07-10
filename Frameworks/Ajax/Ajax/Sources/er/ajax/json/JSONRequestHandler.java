package er.ajax.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCResult;
import org.jabsorb.serializer.Serializer;
import org.json.JSONException;
import org.json.JSONObject;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation._NSUtilities;

import er.extensions.components.ERXDynamicURL;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXProperties;

/**
 * JSONRequestHandler provides support for JSON RPC services that can be both 
 * stateless or stateful (using JSON Components). 
 *   
 * @author mschrag
 * @property er.ajax.json.globalBacktrackCacheSize the maximum number of global components that can be in the session (defaults to backtrack cache size) 
 * @property er.ajax.json.backtrackCacheSize the maximum number of non-global components that can be in the session  (defaults to backtrack cache size)
 */
public class JSONRequestHandler extends WORequestHandler {
	public static final String RequestHandlerKey = "json";

	private JSONRPCBridge _sharedBridge;

	/**
	 * Registers the JSONRequestHandler with your application using the default key.
	 * 
	 * @return the request handler instance 
	 */
	public static JSONRequestHandler register() {
		JSONRequestHandler requestHandler = new JSONRequestHandler();
		WOApplication.application().registerRequestHandler(requestHandler, JSONRequestHandler.RequestHandlerKey);
		return requestHandler;
	}

	/**
	 * Creates a new JSONRequestHandler.
	 */
	public JSONRequestHandler() {
		_sharedBridge = JSONBridge.createBridge();
	}

	/**
	 * Returns the shared JSON Bridge for this request handler.
	 * 
	 * @return the shared JSON Bridge for this request handler
	 */
	public JSONRPCBridge getJSONBridge() {
		return _sharedBridge;
	}

	/**
	 * Registers a custom serializer into the global JSON serializers (see JSONRPCBridge).
	 *  
	 * @param serializer the serializer to register
	 * @throws Exception if the registration fails
	 */
	public static void registerSerializer(Serializer serializer) throws Exception {
		JSONRPCBridge.getSerializer().registerSerializer(serializer);
	}

	/**
	 * Registers all of the methods of the given class to be available for services to call (see JSONRPCBridge).
	 * 
	 * @param clazz the class to register
	 * @throws Exception if the registration fails
	 */
	public static void registerClass(Class clazz) throws Exception {
		JSONRequestHandler.registerClass(clazz.getSimpleName(), clazz);
	}

	/**
	 * Registers all of the methods of the given class to be available for services to call (see JSONRPCBridge).
	 *
	 * @param name the namespace to register the methods under
	 * @param clazz the class to register
	 * @throws Exception if the registration fails
	 */
	public static void registerClass(String name, Class clazz) throws Exception {
		JSONRPCBridge.getGlobalBridge().registerClass(name, clazz);
	}

	/**
	 * Registers the given object in the shared JSON bridge.  The shared JSON
	 * bridge is used for stateless JSON services.  As an example, if you call
	 * registerService("myExampleService", new ExampleService()) you can then 
	 * call json.myExampleService.someMethodInExampleService from your Javascript.
	 * The same instance is shared across all of your service users, so you should
	 * not store any state in this class.
	 * 
	 * @param name the name to register the object as 
	 * @param serviceObject the instance to register
	 */
	public void registerService(String name, Object serviceObject) {
		_sharedBridge.registerObject(name, serviceObject);
	}

	/**
	 * Returns a URL pointing to the JSON request handler.  This variant
	 * should be used for the shared web service endpoint.
	 * 
	 * @param context the current WOContext
	 * @param queryString the query string to append
	 * @return a JSON request handler URL
	 */
	public static String jsonUrl(WOContext context, String queryString) {
		return JSONRequestHandler.jsonUrl(context, JSONRequestHandler.RequestHandlerKey, null, null, queryString);
	}

	/**
	 * Returns a URL pointing to the JSON request handler.  This variant
	 * should be used for the shared web service endpoint.
	 * 
	 * @param context the current WOContext
	 * @param requestHandlerKey if you registered a custom JSON request handler key
	 * @param queryString the query string to append
	 * @return a JSON request handler URL
	 */
	public static String jsonUrl(WOContext context, String requestHandlerKey, String queryString) {
		return JSONRequestHandler.jsonUrl(context, requestHandlerKey, null, null, queryString);
	}

	/**
	 * Returns a URL pointing to the JSON request handler for a JSON component.
	 * 
	 * @param context the current WOContext
	 * @param componentName the name of the component to lookup
	 * @param instance the instance identifier (any value) to create a unique instance (or null for a session-global)
	 * @param queryString the query string to append
	 * @return a JSON request handler URL
	 */
	public static String jsonUrl(WOContext context, String componentName, String instance, String queryString) {
		return JSONRequestHandler.jsonUrl(context, JSONRequestHandler.RequestHandlerKey, componentName, instance, queryString);
	}

	/**
	 * Returns a URL pointing to the JSON request handler.
	 * 
	 * @param context the current WOContext
	 * @param requestHandlerKey if you registered a custom JSON request handler key
	 * @param componentName the name of the component to lookup (or null for the shared bridge)
	 * @param instance the instance identifier (any value) to create a unique instance (or null for a session-global)
	 * @param queryString the query string to append
	 * @return a JSON request handler URL
	 */
	public static String jsonUrl(WOContext context, String requestHandlerKey, String componentName, String instance, String queryString) {
		String requestHandlerPath;
		if (componentName == null) {
			requestHandlerPath = "";
		}
		else {
			if (instance == null) {
				requestHandlerPath = componentName;
			}
			else {
				requestHandlerPath = componentName + "/" + instance;
			}
		}

		String jsonUrl = context.urlWithRequestHandlerKey(JSONRequestHandler.RequestHandlerKey, requestHandlerPath, queryString);
		return jsonUrl;
	}

	@SuppressWarnings("unchecked")
	@Override
	public WOResponse handleRequest(WORequest request) {
		WOApplication application = WOApplication.application();
		application.awake();
		try {
			WOContext context = application.createContextForRequest(request);
			WOResponse response = application.createResponseInContext(context);

			Object output;
			try {
				String inputString = request.contentString();
				JSONObject input = new JSONObject(inputString);
				String wosid = request.cookieValueForKey("wosid");
				if (wosid == null) {
					ERXMutableURL url = new ERXMutableURL();
					url.setQueryParameters(request.queryString());
					wosid = url.queryParameter("wosid");
					if (wosid == null && input.has("wosid")) {
						wosid = input.getString("wosid");
					}
				}
				context._setRequestSessionID(wosid);
				WOSession session = null;
				if (context._requestSessionID() != null) {
					session = WOApplication.application().restoreSessionWithID(wosid, context);
				}
				if (session != null) {
					session.awake();
				}
				try {
					ERXDynamicURL url = new ERXDynamicURL(request._uriDecomposed());
					String requestHandlerPath = url.requestHandlerPath();
					JSONRPCBridge jsonBridge;
					if (requestHandlerPath != null && requestHandlerPath.length() > 0) {
						String componentNameAndInstance = requestHandlerPath;
						String componentName;
						String componentInstance;
						int slashIndex = componentNameAndInstance.indexOf('/');
						if (slashIndex == -1) {
							componentName = componentNameAndInstance;
							componentInstance = null;
						}
						else {
							componentName = componentNameAndInstance.substring(0, slashIndex);
							componentInstance = componentNameAndInstance.substring(slashIndex + 1);
						}

						if (session == null) {
							session = context.session();
						}

						String bridgesKey = (componentInstance == null) ? "_JSONGlobalBridges" : "_JSONInstanceBridges";
						Map<String, JSONRPCBridge> componentBridges = (Map<String, JSONRPCBridge>) session.objectForKey(bridgesKey);
						if (componentBridges == null) {
							int limit = ERXProperties.intForKeyWithDefault((componentInstance == null) ? "er.ajax.json.globalBacktrackCacheSize" : "er.ajax.json.backtrackCacheSize", WOApplication.application().pageCacheSize());
							componentBridges = new LRUMap<String, JSONRPCBridge>(limit);
							session.setObjectForKey(componentBridges, bridgesKey);
						}
						jsonBridge = componentBridges.get(componentNameAndInstance);
						if (jsonBridge == null) {
							Class componentClass = _NSUtilities.classWithName(componentName);
							JSONComponent component;
							if (JSONComponent.class.isAssignableFrom(componentClass)) {
								component = (JSONComponent) _NSUtilities.instantiateObject(componentClass, new Class[] { WOContext.class }, new Object[] { context }, true, false);
							}
							else {
								throw new SecurityException("There is no JSON component named '" + componentName + "'.");
							}
							jsonBridge = JSONBridge.createBridge();
							jsonBridge.registerObject("component", component);
							componentBridges.put(componentNameAndInstance, jsonBridge);
						}
						JSONComponent component = (JSONComponent) jsonBridge.lookupObject("component");
						component.checkAccess();
						component._setContext(context);
					}
					else {
						jsonBridge = _sharedBridge;
					}

					output = jsonBridge.call(new Object[] { request, response, context }, input);
					if (context._session() != null) {
						WOSession contextSession = context._session();
						// If this is a new session, then we have to force it to be a cookie session
						if (wosid == null) {
							boolean storesIDsInCookies = contextSession.storesIDsInCookies();
							try {
								contextSession.setStoresIDsInCookies(true);
								contextSession._appendCookieToResponse(response);
							}
							finally {
								contextSession.setStoresIDsInCookies(storesIDsInCookies);
							}
						}
						else {
							contextSession._appendCookieToResponse(response);
						}
					}
					if (output != null) {
						response.appendContentString(output.toString());
					}

					if (response != null) {
						response._finalizeInContext(context);
						response.disableClientCaching();
					}
				}
				finally {
					try {
						if (session != null) {
							session.sleep();
						}
					}
					finally {
						if (context._session() != null) {
							WOApplication.application().saveSessionForContext(context);
						}
					}
				}
			}
			catch (NoSuchElementException e) {
				e.printStackTrace();
				output = new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, null, JSONRPCResult.MSG_ERR_NOMETHOD);
			}
			catch (JSONException e) {
				e.printStackTrace();
				output = new JSONRPCResult(JSONRPCResult.CODE_ERR_PARSE, null, JSONRPCResult.MSG_ERR_PARSE);
			}
			catch (Throwable t) {
				t.printStackTrace();
				output = new JSONRPCResult(JSONRPCResult.CODE_ERR_PARSE, null, t.getMessage());
			}

			return response;
		}
		finally {
			application.sleep();
		}
	}

	protected static class LRUMap<U, V> extends LinkedHashMap<U, V> {
		private int _maxSize;

		public LRUMap(int maxSize) {
			super(16, 0.75f, true);
			_maxSize = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<U, V> eldest) {
			return size() > _maxSize;
		}
	}
}
