package er.rest.routes;

import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAction;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOPageNotFoundException;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSValidation;
import com.webobjects.foundation._NSUtilities;

import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXResponse;
import er.extensions.eof.ERXDatabaseContextDelegate.ObjectNotAvailableException;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.validation.ERXValidationException;
import er.rest.ERXNotAllowedException;
import er.rest.ERXRequestFormValues;
import er.rest.ERXRestClassDescriptionFactory;
import er.rest.ERXRestContext;
import er.rest.ERXRestFetchSpecification;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXWORestRequest;
import er.rest.format.ERXWORestResponse;
import er.rest.format.IERXRestParser;
import er.rest.routes.jsr311.CookieParam;
import er.rest.routes.jsr311.HeaderParam;
import er.rest.routes.jsr311.Path;
import er.rest.routes.jsr311.PathParam;
import er.rest.routes.jsr311.Paths;
import er.rest.routes.jsr311.QueryParam;
import er.rest.util.ERXRestSchema;
import er.rest.util.ERXRestTransactionRequestAdaptor;

/**
 * ERXRouteController is equivalent to a Rails controller class. It's actually a direct action, and has the same naming
 * rules as a direct action, so your controller action methods must end in the name "Action". There are several utility
 * methods for manipulating restful requests and responses (update(..), create(..), requestNode(), response(..), etc) ,
 * and it supports multiple formats for you.
 * 
 * @property ERXRest.accessControlAllowRequestHeaders See https://developer.mozilla.org/En/HTTP_access_control#Access-Control-Allow-Headers
 * @property ERXRest.accessControlAllowRequestMethods See https://developer.mozilla.org/En/HTTP_access_control#Access-Control-Allow-Methods
 * @property ERXRest.defaultFormat (default "xml") Allow you to set the default format for all of your REST controllers
 * @property ERXRest.strictMode (default "true") If set to true, status code in the response will be 405 Not Allowed, if set to false, status code will be 404 Not Found
 * @property ERXRest.allowWindowNameCrossDomainTransport
 * @property ERXRest.accessControlMaxAge (default 1728000) This header indicates how long the results of a preflight request can be cached. See https://developer.mozilla.org/En/HTTP_access_control#Access-Control-Max-Age
 * @property ERXRest.accessControlAllowOrigin Set the value to '*' to enable all origins. See https://developer.mozilla.org/En/HTTP_access_control#Access-Control-Allow-Origin
 *
 * @author mschrag
 */
public class ERXRouteController extends WODirectAction {
	protected static final Logger log = Logger.getLogger(ERXRouteController.class);

	private ERXRouteRequestHandler _requestHandler;
	private ERXRoute _route;
	private String _entityName;
	private ERXRestFormat _format;
	private NSDictionary<ERXRoute.Key, String> _routeKeys;
	private NSDictionary<ERXRoute.Key, Object> _objects;
	private EOEditingContext _editingContext;
	private ERXRestRequestNode _requestNode;
	private NSKeyValueCoding _options;
	private NSSet<String> _prefetchingKeyPaths;
	private boolean _shouldDisposeEditingContext;
	private ERXRestContext _restContext;

	/**
	 * Constructs a new ERXRouteController.
	 * 
	 * @param request
	 *            the request
	 */
	public ERXRouteController(WORequest request) {
		super(request);
		_shouldDisposeEditingContext = true;
		ERXRouteController._registerControllerForRequest(this, request);
	}

	/**
	 * Includes the key in the given filter if isKeyPathRequested returns true.
	 * 
	 * @param key
	 *            the key to lookup
	 * @param filter
	 *            the filter to include into
	 * @return the nested filter (or null if the key was not requested)
	 */
	protected ERXKeyFilter includeOptional(ERXKey<?> key, ERXKeyFilter filter) {
		if (isKeyPathRequested(key)) {
			return filter.include(key);
		}
		return ERXKeyFilter.filterWithNone(); // prevent NPE's -- just return an unrooted filter
	}

	/**
	 * Returns whether or not the prefetchingKeyPaths option includes the given keypath (meaning, the client requested
	 * to include the given keypath).
	 * 
	 * @param key
	 *            the ERXKey to check on
	 * @return true if the keyPath is in the prefetchingKeyPaths option
	 */
	protected boolean isKeyPathRequested(ERXKey<?> key) {
		return isKeyPathRequested(key.key());
	}

	/**
	 * Returns whether or not the prefetchingKeyPaths option includes the given keypath (meaning, the client requested
	 * to include the given keypath).
	 * 
	 * @param keyPath
	 *            the keyPath to check on
	 * @return true if the keyPath is in the prefetchingKeyPaths option
	 */
	protected boolean isKeyPathRequested(String keyPath) {
		if (_prefetchingKeyPaths == null) {
			NSMutableSet<String> prefetchingKeyPaths = new NSMutableSet<String>();
			NSKeyValueCoding options = options();
			if (options != null) {
				String prefetchingKeyPathsStr = (String) options.valueForKey("prefetchingKeyPaths");
				if (prefetchingKeyPathsStr != null) {
					for (String prefetchingKeyPath : prefetchingKeyPathsStr.split(",")) {
						prefetchingKeyPaths.addObject(prefetchingKeyPath);
					}
				}
			}
			_prefetchingKeyPaths = prefetchingKeyPaths;
		}
		return _prefetchingKeyPaths.containsObject(keyPath);
	}

	/**
	 * Sets the options for this controller.
	 * 
	 * @param options
	 *            options for this controller
	 */
	public void setOptions(NSKeyValueCoding options) {
		_options = options;
	}

	/**
	 * Returns the options for this controller. Options are an abstraction on request form values.
	 * 
	 * @return the options for this controller (default to be ERXRequestFormValues)
	 */
	public NSKeyValueCoding options() {
		if (_options == null) {
			_options = new ERXRequestFormValues(request());
		}
		return _options;
	}
	
	/**
	 * WODirectAction doesn't expose API for setting the context, which can be useful for passing data between controller.
	 *  
	 * @param context the new context
	 */
	public void _setContext(WOContext context) {
		try {
			Field contextField = WOAction.class.getDeclaredField("_context");
			contextField.setAccessible(true);
			contextField.set(this, context);
		}
		catch (Throwable t) {
			throw NSForwardException._runtimeExceptionForThrowable(t);
		}
	}

	/**
	 * Sets the request handler that processed this route.
	 * 
	 * @param requestHandler
	 *            the request handler that processed this route
	 */
	public void _setRequestHandler(ERXRouteRequestHandler requestHandler) {
		_requestHandler = requestHandler;
	}

	/**
	 * Returns the request handler that processed this route.
	 * 
	 * @return the request handler that processed this route
	 */
	public ERXRouteRequestHandler requestHandler() {
		return _requestHandler;
	}

	/**
	 * Override to provide custom security checks. It is not necessary to call super on this method.
	 * 
	 * @throws SecurityException
	 *             if the security check fails
	 */
	protected void checkAccess() throws SecurityException {
	}

	public void _setEditingContent(EOEditingContext ec) {
		_editingContext = ec;
	}

	/**
	 * The controller maintains an editing context for the duration of the request. The first time you call this method,
	 * you will get a new EOEditingContext. Subsequent calls will return the same instance. This makes it a little more
	 * convenient when you're using update, create, etc methods.
	 * 
	 * @return an EOEditingContext
	 */
	public EOEditingContext editingContext() {
		if (_editingContext == null) {
			ERXRestTransactionRequestAdaptor transactionAdaptor = ERXRestTransactionRequestAdaptor.defaultAdaptor();
			if (transactionAdaptor.transactionsEnabled() && transactionAdaptor.isExecutingTransaction(context(), request())) {
				_editingContext = newEditingContext(transactionAdaptor.executingTransaction(context(), request()).editingContext());
			}
			else {
				_editingContext = newEditingContext();
			}
		}
		return _editingContext;
	}
	
	/**
	 * Creates a new editing context.
	 * 
	 * @return a new editing context
	 */
	protected EOEditingContext newEditingContext() {
		return ERXEC.newEditingContext();
	}
	
	/**
	 * Creates a new editing context with a parent object store.
	 * 
	 * @param objectStore the parent object store
	 * @return a new editing context
	 */
	protected EOEditingContext newEditingContext(EOObjectStore objectStore) {
		return ERXEC.newEditingContext(objectStore);
	}

	/**
	 * Sets the route that is associated with this request. This is typically only set by the request handler.
	 * 
	 * @param route
	 *            the route that is associated with this controller
	 */
	public void _setRoute(ERXRoute route) {
		_route = route;
	}

	/**
	 * Returns the route associated with this request.
	 * 
	 * @return the route associated with this request
	 */
	public ERXRoute route() {
		return _route;
	}

	/**
	 * Sets the unprocessed keys from the route.
	 * 
	 * @param routeKeys
	 *            the parsed keys from the route
	 */
	public void _setRouteKeys(NSDictionary<ERXRoute.Key, String> routeKeys) {
		_routeKeys = routeKeys;
		if (_routeKeys != routeKeys) {
			_objects = null;
		}
	}

	/**
	 * Returns the unprocessed keys from the route (the values are the original value from the URL).
	 * 
	 * @return the unprocessed keys from the route
	 */
	public NSDictionary<ERXRoute.Key, String> routeKeys() {
		return _routeKeys;
	}

	/**
	 * Returns the unprocessed value from the route with the given key name.
	 * 
	 * @param key
	 *            the key name to lookup
	 * @return the unprocessed value from the route with the given key name
	 */
	public String routeStringForKey(String key) {
		return _routeKeys.objectForKey(new ERXRoute.Key(key));
	}

	/**
	 * Returns whether or not there is a route key with the given name.
	 * 
	 * @param key
	 *            the key name to lookup
	 * @return whether or not there is a route key with the given name
	 */
	public boolean containsRouteKey(String key) {
		return _routeKeys.containsKey(new ERXRoute.Key(key));
	}

	/**
	 * Returns the processed object from the route keys with the given name. For instance, if your route specifies that
	 * you have a {person:Person}, routeObjectForKey("person") will return a Person object.
	 * 
	 * @param key
	 *            the key name to lookup
	 * @return the processed object from the route keys with the given name
	 */
	@SuppressWarnings("unchecked")
	public <T> T routeObjectForKey(String key) {
		return (T)routeObjects().objectForKey(new ERXRoute.Key(key));
	}

	/**
	 * Sets the processed objects for the current route. For instance, if your route specifies that you have a
	 * {person:Person}, this dictionary should contain a mapping from that route key to a person instance.
	 * 
	 * @param objects the route objects to override
	 */
	public void _setRouteObjects(NSDictionary<ERXRoute.Key, Object> objects) {
		_objects = objects;
	}
	
	/**
	 * Returns all the processed objects from the route keys. For instance, if your route specifies that you have a
	 * {person:Person}, routeObjectForKey("person") will return a Person object.
	 * 
	 * @return the processed objects from the route keys
	 */
	public NSDictionary<ERXRoute.Key, Object> routeObjects() {
		if (_objects == null) {
			_objects = ERXRoute.keysWithObjects(_routeKeys, restContext());
		}
		return _objects;
	}

	/**
	 * Returns all the processed objects from the route keys. For instance, if your route specifies that you have a
	 * {person:Person}, routeObjectForKey("person") will return a Person object. This method does NOT cache the results.
	 * 
	 * @param restContext the delegate to fetch with
	 * @return the processed objects from the route keys
	 */
	public NSDictionary<ERXRoute.Key, Object> routeObjects(ERXRestContext restContext) {
		if (_route != null) {
			_objects = ERXRoute.keysWithObjects(_routeKeys, restContext);
		}
		return _objects;
	}

	/**
	 * Returns the default format to use if no other format is found, or if the requested format is invalid.
	 * 
	 * @return the default format to use if no other format is found, or if the requested format is invalid
	 */
	protected ERXRestFormat defaultFormat() {
		String defaultFormatName = ERXProperties.stringForKeyWithDefault("ERXRest.defaultFormat", ERXRestFormat.xml().name());
		return ERXRestFormat.formatNamed(defaultFormatName);
	}

	/**
	 * Sets the format that will be used by this route controller.
	 * 
	 * @param format the format to be used by this route controller
	 */
	public void _setFormat(ERXRestFormat format) {
		_format = format;
	}
	
	/**
	 * Returns the format that the user requested (usually based on the request file extension).
	 * 
	 * @return the format that the user requested
	 */
	public ERXRestFormat format() {
		ERXRestFormat format = _format;
		if (format == null) {
			String type = null;
			@SuppressWarnings("unchecked")
			NSDictionary<String, Object> userInfo = request().userInfo();
			if (userInfo != null) {
				type = (String) request().userInfo().objectForKey(ERXRouteRequestHandler.TypeKey);
			}
			
			/*
			 * To trap things like this: 
			 *   Content-Type: application/json
			 * JBoss's RestEasy use this header
			 */
			if (type == null) {
				String contentType = request().headerForKey("Content-Type");
				if (contentType != null) {
					String[] types = contentType.split("/");
					if (types.length == 2) {
						type = types[1];
						String[] charsets = type.split(";");
						if (charsets.length >0) {
							type = charsets[0];
						}
					}
				}
			}

			if (type == null) {
				format = defaultFormat();
			}
			else {
				format = formatNamed(type);
			}
		}
		return format;
	}
	
	/**
	 * Returns the format to use for the given type (see ERXRestFormat constants).
	 * 
	 * @param type the type of format to use
	 * @return the corresponding format
	 */
	protected ERXRestFormat formatNamed(String type) {
		return ERXRestFormat.formatNamed(type);
	}

	/**
 	 * Creates a new rest context for the controller.
	 * 
	 * @return a new rest context for the controller
	*/
	protected ERXRestContext createRestContext() {
		return new ERXRestContext(editingContext());
	}
    
	/**
	 * Returns the cached rest context for this controller. If a rest context doesn't yet
	 * exist, this calls {{@link #createRestContext()} to create a new instance. 
	 * 
	 * @return the rest context for this controller
	*/
	public ERXRestContext restContext() {
		if (_restContext == null) {
			_restContext = createRestContext();
		}
		return _restContext;
	}
	
	/**
	 * Sets the rest context for this controller.
	 * 
	 * @param restContext the rest context for this controller
	 */
	public void setRestContext(ERXRestContext restContext) {
		_restContext = restContext;
	}

	/**
	 * Sets the request content that this controller will use for processing.
	 * @param format the requested format
	 * @param requestContent the content of the incoming request
	 */
	public void _setRequestContent(ERXRestFormat format, String requestContent) {
		_setFormat(format);
		_setRequestContent(requestContent);
	}

	/**
	 * Sets the request content that this controller will use for processing -- this requires that a format() is specified. 
	 * @param requestContent the content of the incoming request
	 */
	public void _setRequestContent(String requestContent) {
		_requestNode = format().parse(requestContent);
	}

	/**
	 * Sets the request node that this controller will use for processing.
	 * @param requestNode the node reprsenting the incoming request
	 */
	public void _setRequestNode(ERXRestRequestNode requestNode) {
		_requestNode = requestNode;
	}
	
	/**
	 * Returns the default format delegate to use for the given format (defaults to format.delegate()).
	 * 
	 * @param format the format to lookup
	 * @return the delegate to use for this format
	 */
	protected ERXRestFormat.Delegate formatDelegateForFormat(ERXRestFormat format) {
		return format.delegate();
	}
	
	/**
	 * Returns the request data in the form of an ERXRestRequestNode (which is a format-independent wrapper around
	 * hierarchical data).
	 * 
	 * @return the request data as an ERXRestRequestNode
	 */
	public ERXRestRequestNode requestNode() {
		if (_requestNode == null) {
			try {
				ERXRestFormat format = format();
				IERXRestParser parser = format.parser();
				if (parser == null) {
					throw new IllegalStateException("There is no parser for the format '" + format.name() + "'.");
				}
				_requestNode = parser.parseRestRequest(new ERXWORestRequest(request()), formatDelegateForFormat(format), restContext());
			}
			catch (Throwable t) {
				throw new RuntimeException("Failed to parse a " + format() + " request.", t);
			}
		}
		return _requestNode;
	}

	/**
	 * Returns the object from the request data that is of the routed entity name and is filtered with the given filter.
	 * This will use the delegate returned from this controller's delegate() method.
	 * 
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T object(ERXKeyFilter filter) {
		return (T)object(entityName(), filter, restContext());
	}

	/**
	 * Returns the object from the request data that is of the given entity name and is filtered with the given filter.
	 * This will use the delegate returned from this controller's delegate() method.
	 * 
	 * @param entityName
	 *            the entity name of the object in the request
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T object(String entityName, ERXKeyFilter filter) {
		return (T)object(entityName, filter, restContext());
	}

	/**
	 * Returns the object from the request data that is of the routed entity name and is filtered with the given filter.
	 * 
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @param restContext
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T object(ERXKeyFilter filter, ERXRestContext restContext) {
		return (T)requestNode().objectWithFilter(entityName(), filter, restContext);
	}

	/**
	 * Returns the object from the request data that is of the given entity name and is filtered with the given filter.
	 * 
	 * @param entityName
	 *            the entity name of the object in the request
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @param restContext
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T object(String entityName, ERXKeyFilter filter, ERXRestContext restContext) {
		return (T)requestNode().objectWithFilter(entityName, filter, restContext);
	}
	
	/**
	 * Creates a new object from the request data that is of the routed entity name and is filtered with the given
	 * filter. This will use the delegate returned from this controller's delegate() method.
	 * 
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to just create a blank one)
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(ERXKeyFilter filter) {
		return (T)create(entityName(), filter);
	}

	/**
	 * Creates a new object from the request data that is of the given entity name and is filtered with the given
	 * filter. This will use the delegate returned from this controller's delegate() method.
	 * 
	 * @param entityName
	 *            the entity name of the object in the request
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to just create a blank one)
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(String entityName, ERXKeyFilter filter) {
		return (T)create(entityName, filter, restContext());
	}

	/**
	 * Creates a new object from the request data that is of the routed entity name and is filtered with the given
	 * filter.
	 * 
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to just create a blank one)
	 * @param restContext
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(ERXKeyFilter filter, ERXRestContext restContext) {
		return (T)requestNode().createObjectWithFilter(entityName(), filter, restContext);
	}

	/**
	 * Creates a new object from the request data that is of the given entity name and is filtered with the given
	 * filter.
	 * 
	 * @param entityName
	 *            the entity name of the object in the request
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to just create a blank one)
	 * @param restContext
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(String entityName, ERXKeyFilter filter, ERXRestContext restContext) {
		return (T)requestNode().createObjectWithFilter(entityName, filter, restContext);
	}

	/**
	 * Updates the given object from the request data with the given filter. This will use the delegate returned from
	 * this controller's delegate() method.
	 * 
	 * @param obj
	 *            the object to update
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 */
	public void update(Object obj, ERXKeyFilter filter) {
		update(obj, filter, restContext());
	}

	/**
	 * Updates the given object from the request data with the given filter.
	 * 
	 * @param obj
         *            object to update
	 * @param filter
         *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @param restContext
         *            delegate to use
	 */
	public void update(Object obj, ERXKeyFilter filter, ERXRestContext restContext) {
		requestNode().updateObjectWithFilter(obj, filter, restContext);
	}

	/**
	 * Returns the given string wrapped in a WOResponse.
	 * 
	 * @param str
	 *            the string to return
	 * @return a WOResponse
	 */
	public WOResponse stringResponse(String str) {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		response.appendContentString(str);
		return response;
	}

	/**
	 * Returns the given array as a JSON response. This uses the editing context returned by editingContext().
	 * 
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a JSON WOResponse
	 */
	public WOActionResults json(String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.json(), editingContext(), entityName, values, filter);
	}

	/**
	 * Returns the given array as a JSON response.
	 * 
	 * @param editingContext
	 *            the editing context to use
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a JSON WOResponse
	 */
	public WOActionResults json(EOEditingContext editingContext, String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.json(), editingContext, entityName, values, filter);
	}

	/**
	 * Returns the given array as a JSON response.
	 * 
	 * @param entity
	 *            the entity type of the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a JSON WOResponse
	 */
	public WOActionResults json(EOClassDescription entity, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.json(), entity, values, filter);
	}

	/**
	 * Returns the given array as a PList response. This uses the editing context returned by editingContext().
	 * 
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a PList WOResponse
	 */
	public WOActionResults plist(String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.plist(), editingContext(), entityName, values, filter);
	}

	/**
	 * Returns the given array as a JSON response.
	 * 
	 * @param editingContext
	 *            the editing context to use
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a JSON WOResponse
	 */
	public WOActionResults plist(EOEditingContext editingContext, String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.plist(), editingContext, entityName, values, filter);
	}

	/**
	 * Returns the given array as a JSON response.
	 * 
	 * @param entity
	 *            the entity type of the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a JSON WOResponse
	 */
	public WOActionResults plist(EOClassDescription entity, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.plist(), entity, values, filter);
	}

	/**
	 * Returns the given array as an XML response. This uses the editing context returned by editingContext().
	 * 
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return an XML WOResponse
	 */
	public WOActionResults xml(String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.xml(), editingContext(), entityName, values, filter);
	}

	/**
	 * Returns the given array as an XML response.
	 * 
	 * @param editingContext
	 *            the editing context to use
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return an XML WOResponse
	 */
	public WOActionResults xml(EOEditingContext editingContext, String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.xml(), editingContext, entityName, values, filter);
	}

	/**
	 * Returns the given array as an XML response.
	 * 
	 * @param entity
	 *            the entity type of the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return an XML WOResponse
	 */
	public WOActionResults xml(EOClassDescription entity, NSArray<?> values, ERXKeyFilter filter) {
		return response(ERXRestFormat.xml(), entity, values, filter);
	}

	/**
	 * Returns whether or not headers can be added to the given action results.
	 * 
	 * @param results the results to test
	 * @return whether or not headers can be added to the given action results
	 */
	protected boolean _canSetHeaderForActionResults(WOActionResults results) {
		return results instanceof WOResponse || results instanceof ERXRouteResults;
	}
	
	/**
	 * Attempt to set the header for the given results object.
	 * 
	 * @param value the value
	 * @param key the key
	 * @param results the results object
	 */
	protected void _setHeaderForActionResults(String value, String key, WOActionResults results) {
		if (results instanceof WOResponse) {
			((WOResponse)results).setHeader(value, key);
		}
		else if (results instanceof ERXRouteResults) {
			((ERXRouteResults)results).setHeaderForKey(value, key);
		}
		else {
			ERXRouteController.log.info("Unable to set a header on an action results of type '" + results.getClass().getName() + "'.");
		}
	}
	
	/**
	 * Returns the results of the rest fetch spec as an response in the format returned from the format() method. 
	 * This uses the editing context returned by editingContext().
	 * 
	 * @param fetchSpec
	 *            the rest fetch specification to execute
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a WOResponse of the format returned from the format() method
	 */
	public WOActionResults response(ERXRestFetchSpecification<?> fetchSpec, ERXKeyFilter filter) {
		WOActionResults results;
		if (fetchSpec == null) {
			// MS: you probably meant to call response(Object, filter) in this case -- just proxy through
			results = response(format(), null, filter);
		}
		else {
			ERXRestFetchSpecification.Results<?> fetchResults = fetchSpec.results(editingContext(), options());
			results = response(format(), editingContext(), fetchSpec.entityName(), fetchResults.objects(), filter);
			if (fetchResults.batchSize() > 0 && options().valueForKey("Range") != null && _canSetHeaderForActionResults(results)) {
				String contentRangeValue = "items " + fetchResults.startIndex() + "-" + (fetchResults.startIndex() + fetchResults.batchSize() - 1) + "/" + fetchResults.totalCount();
				_setHeaderForActionResults(contentRangeValue, "Content-Range", results);
			}
		}
		return results;
	}

	/**
	 * Returns the given array as an response in the format returned from the format() method. This uses the editing
	 * context returned by editingContext().
	 * 
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a WOResponse of the format returned from the format() method
	 */
	public WOActionResults response(String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(format(), editingContext(), entityName, values, filter);
	}

	/**
	 * Returns the given array as an response in the format returned from the format() method.
	 * 
	 * @param editingContext
	 *            the editing context to use
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a WOResponse of the format returned from the format() method
	 */
	public WOActionResults response(EOEditingContext editingContext, String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(format(), editingContext, entityName, values, filter);
	}

	/**
	 * Returns the given array as an response in the format returned from the format() method.
	 * 
	 * @param entity
	 *            the entity type of the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a WOResponse of the format returned from the format() method
	 */
	public WOActionResults response(EOClassDescription entity, NSArray<?> values, ERXKeyFilter filter) {
		return response(format(), entity, values, filter);
	}

	/**
	 * Returns the given array as a response in the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @param entityName
	 *            the name of the entity type of the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a WOResponse in the given format
	 */
	public WOActionResults response(ERXRestFormat format, String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(format, editingContext(), entityName, values, filter);
	}

	/**
	 * Returns the given array as a response in the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @param editingContext
	 *            the editing context to use
	 * @param entityName
	 *            the name of the entities in the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a WOResponse in the given format
	 */
	public WOActionResults response(ERXRestFormat format, EOEditingContext editingContext, String entityName, NSArray<?> values, ERXKeyFilter filter) {
		return response(format, ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName), values, filter);
	}

	/**
	 * Returns the given array as a response in the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @param entity
	 *            the entity type of the array
	 * @param values
	 *            the values in the array
	 * @param filter
	 *            the filter to apply to the objects
	 * @return a WOResponse in the given format
	 */
	public WOActionResults response(ERXRestFormat format, EOClassDescription entity, NSArray<?> values, ERXKeyFilter filter) {
		ERXRestRequestNode responseNode;
		try {
			responseNode = ERXRestRequestNode.requestNodeWithObjectAndFilter(entity, values, filter, restContext());
		}
		catch (ObjectNotAvailableException e) {
			return errorResponse(e, WOMessage.HTTP_STATUS_NOT_FOUND);
		}
		catch (SecurityException e) {
			return errorResponse(e, WOMessage.HTTP_STATUS_FORBIDDEN);
		}
		catch (Throwable t) {
			return errorResponse(t, WOMessage.HTTP_STATUS_INTERNAL_ERROR);
		}
		return response(format, responseNode);
	}

	/**
	 * Returns the given ERXRestRequestNode as a response in the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @param responseNode
	 *            the request node to render
	 * @return a WOResponse in the given format
	 */
	public WOActionResults response(ERXRestFormat format, ERXRestRequestNode responseNode) {
		ERXRouteResults results = new ERXRouteResults(context(), restContext(), format, responseNode);
		return results;
	}

	/**
	 * Returns the given object as a JSON response.
	 * 
	 * @param value
	 *            the value to return
	 * @param filter
	 *            the filter to apply
	 * @return a WOResponse in JSON format
	 */
	public WOActionResults json(Object value, ERXKeyFilter filter) {
		return response(ERXRestFormat.json(), value, filter);
	}

	/**
	 * Returns the given object as a PList response.
	 * 
	 * @param value
	 *            the value to return
	 * @param filter
	 *            the filter to apply
	 * @return a WOResponse in PList format
	 */
	public WOActionResults plist(Object value, ERXKeyFilter filter) {
		return response(ERXRestFormat.plist(), value, filter);
	}

	/**
	 * Returns the given object as an XML response.
	 * 
	 * @param value
	 *            the value to return
	 * @param filter
	 *            the filter to apply
	 * @return a WOResponse in XML format
	 */
	public WOActionResults xml(Object value, ERXKeyFilter filter) {
		return response(ERXRestFormat.xml(), value, filter);
	}

	/**
	 * Returns the given object as a response in the format returned from the format() method.
	 * 
	 * @param value
	 *            the value to return
	 * @param filter
	 *            the filter to apply
	 * @return a WOResponse in the format returned from the format() method.
	 */
	public WOActionResults response(Object value, ERXKeyFilter filter) {
		return response(format(), value, filter);
	}

	/**
	 * Returns the given object as a WOResponse in the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @param value
	 *            the value to return
	 * @param filter
	 *            the filter to apply
	 * @return a WOResponse in the given format
	 */
	public WOActionResults response(ERXRestFormat format, Object value, ERXKeyFilter filter) {
		ERXRestRequestNode responseNode;
		try {
			responseNode = ERXRestRequestNode.requestNodeWithObjectAndFilter(value, filter, restContext());
		}
		catch (ObjectNotAvailableException e) {
			return errorResponse(e, WOMessage.HTTP_STATUS_NOT_FOUND);
		}
		catch (SecurityException e) {
			return errorResponse(e, WOMessage.HTTP_STATUS_FORBIDDEN);
		}
		catch (Throwable t) {
			return errorResponse(t, WOMessage.HTTP_STATUS_INTERNAL_ERROR);
		}
		return response(format, responseNode);
	}
	
	/**
	 * Returns an response with the given HTTP status and without any body content.
	 * Useful to return HTTP codes like 410 (Gone) or 304 (Not Modified)
	 * @param status
	 *            the HTTP status code
	 * @return an error WOResponse
	 */
	public WOActionResults response(int status) {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		response.setStatus(status);
		return response;
	}

	/**
	 * Returns an error response with the given HTTP status.
	 * 
	 * @param t
	 *            the exception
	 * @param status
	 *            the HTTP status code
	 * @return an error WOResponse
	 */
	public WOActionResults errorResponse(Throwable t, int status) {
		String errorMessage = ERXLocalizer.defaultLocalizer().localizedStringForKey("ERXRest." + entityName() + ".errorMessage." + status);
		if (errorMessage == null) {
			errorMessage = ERXLocalizer.defaultLocalizer().localizedStringForKey("ERXRest.errorMessage." + status);
			if (errorMessage == null) {
				errorMessage = ERXExceptionUtilities.toParagraph(t, false);
			}
		}
		String str = format().toString(errorMessage, null, null);
		WOResponse response = stringResponse(str);
		response.setStatus(status);	
		if (format().equals(ERXRestFormat.json())) {
			response.setHeader("application/json", "Content-Type");
		} else if (format().equals(ERXRestFormat.xml())) { 
			response.setHeader("text/xml", "Content-Type");
		} else if (format().equals(ERXRestFormat.plist())) { 
			response.setHeader("text/plist", "Content-Type");
		} else if (format().equals(ERXRestFormat.bplist())) { 
			response.setHeader("application/x-plist", "Content-Type");
		} else {
			response.setHeader("application/json", "Content-Type");			
		}
		log.error("Request failed: " + request().uri(), t);
		return response;
	}

	/**
	 * Returns an error response with the given HTTP status.
	 * 
	 * @param errorMessage
	 *            the error message
	 * @param status
	 *            the HTTP status code
	 * @return an error WOResponse
	 */
	public WOActionResults errorResponse(String errorMessage, int status) {
		String formattedErrorMessage = format().toString(errorMessage, null, null);
		WOResponse response = stringResponse(formattedErrorMessage);
		response.setStatus(status);
		log.error("Request failed: " + request().uri() + ", " + errorMessage);
		return response;
	}
	
	/**
	 * Returns an error response with the given HTTP status and without any body content
	 * @param status
	 *            the HTTP status code
	 * @return an error WOResponse
	 */
	public WOActionResults errorResponse(int status) {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		response.setStatus(status);
		log.error("Request failed: " + request().uri() + ", " + status);
		return response;
	}

	/**
	 * Returns the response from a HEAD call to this controller.
	 * 
	 * @return a head response
	 */
	public WOActionResults headAction() {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		format().writer().appendHeadersToResponse(null, new ERXWORestResponse(response), restContext());
		return response;
	}

	/**
	 * Enumerates the route keys, looks for @ERXRouteParameter annotated methods, and sets the value of the routeKey
	 * with the corresponding method if it exists.
	 * 
	 * @param results
	 *            the results to apply route parameter to
	 */
	protected void _takeRouteParametersFromRequest(WOActionResults results) {
		Class<?> resultsClass = results.getClass();
		for (ERXRoute.Key key : _routeKeys.allKeys()) {
			ERXRoute.RouteParameterMethod routeParameterMethod = key._routeParameterMethodForClass(resultsClass);
			String keyName = key.key();
			
			if (routeParameterMethod == null) {
				// MS: because we lowercase SPPerson into spPerson, the default capitalization would be SpPerson.
				// We want to do a first pass where we check for entities that equalsIgnoreCase match the
				// keyName and guess that as the capitalization first. If that fails, THEN we fall back to a
				// simple capitalization.
				String capitalizedKeyName = ERXRestClassDescriptionFactory._guessMismatchedCaseEntityName(keyName);
				if (capitalizedKeyName == null) {
					capitalizedKeyName = ERXStringUtilities.capitalize(keyName);
				}
				String setMethodName = "set" + capitalizedKeyName;
				Method matchingMethod = null;
				Method[] possibleMethods = resultsClass.getMethods();
				for (Method possibleMethod : possibleMethods) {
					ERXRouteParameter routeParameter = possibleMethod.getAnnotation(ERXRouteParameter.class);
					// IK : in SnapshotExplorer the er.snapshotexplorer.components.pages.setEOModelGroup get never called because
					// setMethodName will be "setEoModelGroup" and with the equals compare never hits.
					// Now with changeds to equalsIgnoreCase it works again. Also I belive there will be no Problme except somebody
					// has two Methods with the same Name but different capitalizations.
					if (routeParameter != null && (keyName.equals(routeParameter.value()) || possibleMethod.getName().equalsIgnoreCase(setMethodName))) {
						matchingMethod = possibleMethod;
						break;
					}
				}
				routeParameterMethod = new ERXRoute.RouteParameterMethod(matchingMethod);
				key._setRouteParameterMethodForClass(routeParameterMethod, resultsClass);
			}
			
			if (routeParameterMethod.hasMethod()) {
				try {
					if (routeParameterMethod.isStringParameter()) {
						routeParameterMethod.method().invoke(results, routeStringForKey(keyName));
					}
					else {
						Object routeObject = routeObjectForKey(keyName);
						if (routeObject instanceof EOEnterpriseObject && ((EOEnterpriseObject)routeObject).editingContext() == _editingContext) {
							_shouldDisposeEditingContext = false;
						}
						routeParameterMethod.method().invoke(results, routeObject);
					}
				}
				catch (Throwable t) {
					throw NSForwardException._runtimeExceptionForThrowable(t);
				}
			}
		}
	}

	/**
	 * If this method returns true, all HTML format requests will be automatically routed to the corresponding
	 * IERXRouteComponent implementation based on the name returned by pageNameForAction(String).
	 * 
	 * @return true if HTML format requests should be automatically routed to the corresponding page component
	 */
	protected boolean isAutomaticHtmlRoutingEnabled() {
		return false;
	}

	/**
	 * If automatic html routing is enabled and there is no page component found that matches the current route,
	 * should that result in a 404?
	 * 
	 * @return whether or not a missing page is a failure
	 */
	protected boolean shouldFailOnMissingHtmlPage() {
		return false;
	}
	
	/**
	 * Sets the entity name for this controller.
	 * 
	 * @param entityName this controller's entity name
	 */
	public void _setEntityName(String entityName) {
		_entityName = entityName;
	}
	
	/**
	 * Returns the name of the entity that this controller is currently handling. The default implementation retrieves
	 * the entity name from the ERXRoute.
	 * 
	 * @return the entity name for the current route
	 */
	protected String entityName() {
		String entityName = _entityName;
		if (entityName == null) {
			ERXRoute route = route();
			if (route != null) {
				entityName = route.entityName();
			}
		}
		if (entityName == null) {
			throw new IllegalStateException("Unable to determine the entity name for the controller '" + getClass().getSimpleName() + "'. Please override entityName().");
		}
		return entityName;
	}

	/**
	 * Returns the name of the page component for this entity and the given action. The default implementation of this
	 * returns entityName + Action + Page ("PersonEditPage", "PersonViewPage", etc).
	 * 
	 * @param actionName
	 *            the name of the action
	 * @return the name of the page component for this action
	 */
	protected String pageNameForAction(String actionName) {
		return entityName() + ERXStringUtilities.capitalize(actionName) + "Page";
	}

	/**
	 * Called when no standard action method can be found to handle the requested route. The default
	 * implementation just throws an exception.
	 *  
	 * @param actionName the unknown action name
	 * @return WOActionResults
	 */
	protected WOActionResults performUnknownAction(String actionName) throws Exception {
		boolean isStrictMode = ERXProperties.booleanForKeyWithDefault("ERXRest.strictMode", true);
		if (isStrictMode) {
			throw new ERXNotAllowedException();
		} else {
			throw new FileNotFoundException("There is no action named '" + actionName + "Action' on '" + getClass().getSimpleName() + "'.");			
		}
	}
	
	@Override
	public WOActionResults performActionNamed(String actionName) {
		return performActionNamed(actionName, false);
	}

	/**
	 * Returns the response node generated from performing the action with the given name.
	 * 
	 * @param actionName the name of the action to perform
	 * @return the response node
	 */
	public ERXRestRequestNode responseNodeForActionNamed(String actionName) {
		String contentString = performActionNamed(actionName, true).generateResponse().contentString();
		return format().parse(contentString);
	}

	/**
	 * Returns the response content generated from performing the action with the given name.
	 * 
	 * @param actionName the name of the action to perform
	 * @return the response content
	 */
	public String responseContentForActionNamed(String actionName) {
		return performActionNamed(actionName, true).generateResponse().contentString();
	}

	/**
	 * Performs the given action, optionally throwing exceptions instead of converting to http response codes.
	 *  
	 * @param actionName the name of the action to perform
	 * @param throwExceptions whether or not to throw exceptions
	 * @return the action results
	 * @throws RuntimeException if a failure occurs
	 */
	public WOActionResults performActionNamed(String actionName, boolean throwExceptions) throws RuntimeException {
		WOActionResults results = null;
		
		try {
			ERXRestTransactionRequestAdaptor transactionAdaptor = ERXRestTransactionRequestAdaptor.defaultAdaptor();
			if (transactionAdaptor.transactionsEnabled() && !transactionAdaptor.isExecutingTransaction(context(), request())) {
				if (!transactionAdaptor.willHandleRequest(context(), request())) {
					if (transactionAdaptor.didHandleRequest(context(), request())) {
						results = stringResponse("Transaction request enqueued.");
					}
					else {
						results = stringResponse("Transaction executed.");
					}
				}
			}
			
			if (results == null) {
				checkAccess();
			}

			if (results == null && isAutomaticHtmlRoutingEnabled() && format() == ERXRestFormat.html()) {
				results = performHtmlActionNamed(actionName);
			}

			if (results == null) {
				results = performRouteActionNamed(actionName);
			}

			if (results == null) {
				results = response(null, ERXKeyFilter.filterWithAttributes());
			}
			else if (results instanceof IERXRouteComponent) {
				_takeRouteParametersFromRequest(results);
			}
		}
		catch (Throwable t) {
			if (throwExceptions) {
				throw NSForwardException._runtimeExceptionForThrowable(t);
			}
			results = performActionNamedWithError(actionName, t);
		}

		results = processActionResults(results);
		
		return results;
	}
	
	/**
	 * If automatic HTML routing is enabled and this request used an HTML format, this method is called
	 * to dispatch the HTML action.
	 * 
	 * @param actionName the name of the HTML action
	 * @return the results of the action
	 * @throws Exception if anything fails
	 */
	protected WOActionResults performHtmlActionNamed(String actionName) throws Exception {
		WOActionResults results = null;
		
		String pageName = pageNameForAction(actionName);
		if (_NSUtilities.classWithName(pageName) != null) {
			try {
				results = pageWithName(pageName);
				if (!(results instanceof IERXRouteComponent)) {
					log.error(pageName + " does not implement IERXRouteComponent, so it will be ignored.");
					results = null;
				}
			}
			catch (WOPageNotFoundException e) {
				log.info(pageName + " does not exist, falling back to route controller.");
				results = null;
			}
		}
		else {
			log.info(pageName + " does not exist, falling back to route controller.");
		}
		
		if (results == null && shouldFailOnMissingHtmlPage()) {
			results = performUnknownAction(actionName);
		}
		
		return results;
	}
	
	/**
	 * If this request is for a normal route action, this method is called to dispatch it.
	 * 
	 * @param actionName the name of the action to perform
	 * @return the results of the action
	 * @throws Exception if anything fails
	 */
	protected WOActionResults performRouteActionNamed(String actionName) throws Exception {
		WOActionResults results = null;
		
		String actionMethodName = actionName + WODirectAction.actionText;
        Method actionMethod = _methodForAction(actionMethodName, "");
        if (actionMethod == null) {
	        actionMethod = _methodForAction(actionName, "");
	        if (actionMethod == null || (actionMethod.getAnnotation(Path.class) == null && actionMethod.getAnnotation(Paths.class) == null)) {
	        	actionMethod = null;
	        }
        }
        
        if (actionMethod == null || actionMethod.getParameterTypes().length > 0) {
        	actionMethod = null;
        	
        	int bestMatchParameterCount = 0;
			List<Annotation> bestMatchAnnotations = null;
        	for (Method method : getClass().getDeclaredMethods()) {
        		String methodName = method.getName();
        		boolean nameMatches = methodName.equals(actionMethodName);
        		if (!nameMatches && methodName.equals(actionName) && (method.getAnnotation(Path.class) != null || method.getAnnotation(Paths.class) != null)) {
        			nameMatches = true;
        		}
        		if (nameMatches) {
        			int parameterCount = 0;
		        	List<Annotation> params = new LinkedList<Annotation>();
        			for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
        				for (Annotation parameterAnnotation : parameterAnnotations) {
        					if (parameterAnnotation instanceof PathParam || parameterAnnotation instanceof QueryParam || parameterAnnotation instanceof CookieParam || parameterAnnotation instanceof HeaderParam) {
        						params.add(parameterAnnotation);
        						parameterCount ++;
        					}
        					else {
        						parameterCount = -1;
        						break;
        					}
        				}
        				if (parameterCount == -1) {
        					break;
        				}
        			}
        			if (parameterCount > bestMatchParameterCount) {
        				actionMethod = method;
        				bestMatchParameterCount = parameterCount;
        				bestMatchAnnotations = params;
        			}
        		}
        	}
        	
        	if (actionMethod == null) {
        		results = performUnknownAction(actionName);
        	}
        	else if (bestMatchParameterCount == 0) {
	    		results = performActionWithArguments(actionMethod, _NSUtilities._NoObjectArray);
        	}
        	else {
        		results = performActionWithAnnotations(actionMethod, bestMatchAnnotations);
        	}
        }
        else {
    		results = performActionWithArguments(actionMethod, _NSUtilities._NoObjectArray);
        }
        return results;
	}
	
	/**
	 * Called when performRouteAction dispatches a method that uses parameter annotations.
	 * 
	 * @param actionMethod the action method to dispatch
	 * @param parameterAnnotations the list of annotations
	 * @return the results of the action
	 * @throws Exception if anything fails
	 */
	protected WOActionResults performActionWithAnnotations(Method actionMethod, List<Annotation> parameterAnnotations) throws Exception {
		Class<?>[] parameterTypes = actionMethod.getParameterTypes();
		Object[] params = new Object[parameterAnnotations.size()];
		for (int paramNum = 0; paramNum < params.length; paramNum ++) {
			Annotation param = parameterAnnotations.get(paramNum);
			if (param instanceof PathParam) {
				params[paramNum] = routeObjectForKey(((PathParam)param).value());
			}
			else if (param instanceof QueryParam) {
				String value = request().stringFormValueForKey(((QueryParam)param).value());
				params[paramNum] = ERXRestUtils.coerceValueToTypeNamed(value, parameterTypes[paramNum].getName(), restContext(), true);
			}
			else if (param instanceof CookieParam) {
				String value = request().cookieValueForKey(((CookieParam)param).value());
				params[paramNum] = ERXRestUtils.coerceValueToTypeNamed(value, parameterTypes[paramNum].getName(), restContext(), true);
			}
			else if (param instanceof HeaderParam) {
				String value = request().headerForKey(((HeaderParam)param).value());
				params[paramNum] = ERXRestUtils.coerceValueToTypeNamed(value, parameterTypes[paramNum].getName(), restContext(), true);
			}
			else {
				throw new IllegalArgumentException("Unknown parameter #" + paramNum + " of " + actionMethod.getName() + ".");
			}
		}
		return performActionWithArguments(actionMethod, params);
	}
	
	/**
	 * Called when an action method is dispatched by performRouteAction in any form.
	 *  
	 * @param actionMethod the method to invoke
	 * @param args the arguments to pass to the method
	 * @return the results of the method
	 * @throws Exception if anything fails
	 */
	protected WOActionResults performActionWithArguments(Method actionMethod, Object... args) throws Exception {
		return (WOActionResults)actionMethod.invoke(this, args);
	}
	
	/**
	 * Called when performing an action fails, giving a chance to return an appropriate error result.
	 * 
	 * @param actionName the name of the action that attempted to perform
	 * @param t the error that occurred
	 * @return an appropriate error result
	 */
	protected WOActionResults performActionNamedWithError(String actionName, Throwable t) {
		WOActionResults results = null;
		Throwable meaningfulThrowble = ERXExceptionUtilities.getMeaningfulThrowable(t);
		boolean isStrictMode = ERXProperties.booleanForKeyWithDefault("ERXRest.strictMode", true);
		if (meaningfulThrowble instanceof ObjectNotAvailableException || meaningfulThrowble instanceof FileNotFoundException || meaningfulThrowble instanceof NoSuchElementException) {
			results = errorResponse(meaningfulThrowble, ERXHttpStatusCodes.NOT_FOUND);
		}
		else if (meaningfulThrowble instanceof SecurityException) {
			results = errorResponse(meaningfulThrowble, ERXHttpStatusCodes.STATUS_FORBIDDEN);
		}
		else if (meaningfulThrowble instanceof ERXNotAllowedException) {
			results = errorResponse(ERXHttpStatusCodes.METHOD_NOT_ALLOWED);
		}
		else if ((isStrictMode) && (meaningfulThrowble instanceof ERXValidationException || meaningfulThrowble instanceof NSValidation.ValidationException)) {
			results = errorResponse(meaningfulThrowble, ERXHttpStatusCodes.BAD_REQUEST);
		}
		else {
			results = errorResponse(meaningfulThrowble,ERXHttpStatusCodes.INTERNAL_ERROR);
		}
		// MS: Should we jam the exception in the response userInfo so the transaction adaptor can rethrow the real exception?
		return results;
	}
	
	/**
	 * Before returning the action results, this method is called to perform any last minute processing.
	 * 
	 * @param results
	 */
	protected WOActionResults processActionResults(WOActionResults results) {
		WOContext context = context();
		WOSession session = context._session();
		// MS: This is sketchy -- should this be done in the request handler after we generate the response?
		if (results instanceof WOResponse) {
			WOResponse response = (WOResponse)results;
			if (session != null && session.storesIDsInCookies()) {
				session._appendCookieToResponse(response);
			}
		}
		
		if (_canSetHeaderForActionResults(results)) {
			String allowOrigin = accessControlAllowOrigin();
			if (allowOrigin != null) {
				_setHeaderForActionResults(allowOrigin, "Access-Control-Allow-Origin", results);
			}
		}
		
		WOActionResults processedResults = results;
		if (allowWindowNameCrossDomainTransport()) {
			String windowNameCrossDomainTransport = request().stringFormValueForKey("windowname");
			if ("true".equals(windowNameCrossDomainTransport)) {
				WOResponse response = results.generateResponse();
				String content = response.contentString();
				if (content != null) {
					content = content.replaceAll("\n", "");
					content = ERXStringUtilities.escapeJavascriptApostrophes(content);
				}
				response.setContent("<html><script type=\"text/javascript\">window.name='" + content + "';</script></html>");
				response.setHeader("text/html", "Content-Type");
				processedResults = response;
			}
		}
		if (allowJSONP()) {
			if (format().equals(ERXRestFormat.json())) {
				String callbackMethodName = request().stringFormValueForKey("callback");
				if (callbackMethodName != null) {
					WOResponse response = results.generateResponse();
					String content = response.contentString();
					if (content != null) {
						content = content.replaceAll("\n", "");
						content = ERXStringUtilities.escapeJavascriptApostrophes(content);
					}
					response.setContent(callbackMethodName + "(" + content + ");");
					response.setHeader("text/javascript", "Content-Type");
					processedResults = response;				
				}
			}
		}
		return processedResults;
	}
	
	/**
	 * Returns whether or not the window.name cross-domain transport is allowed.
	 * 
	 * @return whether or not the window.name cross-domain transport is allowed
	 */
	protected boolean allowWindowNameCrossDomainTransport() {
		return ERXProperties.booleanForKeyWithDefault("ERXRest.allowWindowNameCrossDomainTransport", false);
	}
	
	/**
	 * Returns whether or not JSONP (JSON with Padding) is allowed.
	 * 
	 * @return whether or not JSONP (JSON with Padding) is allowed
	 */
	protected boolean allowJSONP() {
		return ERXProperties.booleanForKeyWithDefault("ERXRest.allowJSONP", false);
	}
	
	/**
	 * Returns the allowed origin for cross-site requests. Set the property ERXRest.accessControlAllowOrigin=* to enable all origins.
	 * 
	 * @return the allowed origin for cross-site requests
	 */
	protected String accessControlAllowOrigin() {
		return ERXProperties.stringForKeyWithDefault("ERXRest.accessControlAllowOrigin", null);
	}

	/**
	 * Returns the allowed request methods given the requested method. Set the property ERXRest.accessControlAllowRequestMethods to override
	 * the default of returning OPTIONS,GET,HEAD,POST,PUT,DELETE,TRACE,CONNECT.
	 * 
	 * @param requestMethod the requested method
	 * @return the array of allowed request methods
	 */
	protected NSArray<String> accessControlAllowRequestMethods(String requestMethod) {
		String accessControlAllowRequestMethodsStr = ERXProperties.stringForKeyWithDefault("ERXRest.accessControlAllowRequestMethods", "OPTIONS,GET,HEAD,POST,PUT,DELETE,TRACE,CONNECT");
		if (accessControlAllowRequestMethodsStr == null || accessControlAllowRequestMethodsStr.length() == 0) {
			accessControlAllowRequestMethodsStr = requestMethod;
		}
		NSArray<String> accessControlAllowRequestMethods = null;
		if (accessControlAllowRequestMethodsStr != null) {
			accessControlAllowRequestMethods = new NSArray<String>(accessControlAllowRequestMethodsStr.split(","));
		}
		return accessControlAllowRequestMethods;
	}

	/**
	 * Returns the allowed request headers given the requested headers.Set the property ERXRest.accessControlAllowRequestHeaders to override
	 * the default of just returning the requested headers.
	 * 
	 * @param requestHeaders the requested headers
	 * @return the array of allowed request headers
	 */
	protected NSArray<String> accessControlAllowRequestHeaders(NSArray<String> requestHeaders) {
		String requestHeadersStr = requestHeaders == null ? null : requestHeaders.componentsJoinedByString(",");
		String accessControlAllowRequestHeadersStr = ERXProperties.stringForKeyWithDefault("ERXRest.accessControlAllowRequestHeaders", requestHeadersStr);
		NSArray<String> accessControlAllowRequestHeaders = null;
		if (accessControlAllowRequestHeadersStr != null) {
			accessControlAllowRequestHeaders = new NSArray<String>(accessControlAllowRequestHeadersStr.split(","));
		}
		return accessControlAllowRequestHeaders;
	}
	
	/**
	 * Returns the maximum age in seconds for the preflight options cache.
	 * 
	 * @return the maximum age for the preflight options cache
	 */
	protected long accessControlMaxAage() {
		return ERXProperties.longForKeyWithDefault("ERXRest.accessControlMaxAge", 1728000);
	}
	
	/**
	 * A default options action that implements access control policy.
	 * 
	 * @return the response
	 */
	public WOActionResults optionsAction() throws Throwable {
		ERXResponse response = new ERXResponse();
		String accessControlAllowOrigin = accessControlAllowOrigin();
		if (accessControlAllowOrigin != null) {
			response.setHeader(accessControlAllowOrigin, "Access-Control-Allow-Origin");
			
			NSArray<String> accessControlAllowRequestMethods = accessControlAllowRequestMethods(request().headerForKey("Access-Control-Request-Method"));
			if (accessControlAllowRequestMethods != null) {
				response.setHeader(accessControlAllowRequestMethods.componentsJoinedByString(","), "Access-Control-Allow-Methods");
			}
			
			String requestHeadersStr = request().headerForKey("Access-Control-Request-Headers");
			NSArray<String> requestHeaders = (requestHeadersStr == null) ? null : NSArray.componentsSeparatedByString(requestHeadersStr, ","); 
			NSArray<String> accessControlAllowRequestHeaders = accessControlAllowRequestHeaders(requestHeaders);
			if (accessControlAllowRequestHeaders != null) {
				response.setHeader(accessControlAllowRequestHeaders.componentsJoinedByString(","), "Access-Control-Allow-Headers");
			}
			
			long accessControlMaxAge = accessControlMaxAage();
			if (accessControlMaxAge >= 0) {
				response.setHeader(String.valueOf(accessControlMaxAge), "Access-Control-Max-Age");
			}
		}
		return response;
	}

	/**
	 * Calls pageWithName.
	 * 
	 * @param <T>
	 *            the type of component to return
	 * @param componentClass
	 *            the component class to lookup
	 * @return the created component
	 */
	@SuppressWarnings("unchecked")
	public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
		return (T) super.pageWithName(componentClass.getName());
	}

	/**
	 * Returns another controller, passing the required state on.
	 * 
	 * @param <T>
	 *            the type of controller to return
	 * @param entityName
	 *            the entity name of the controller to lookup
	 * @return the created controller
	 */
	@SuppressWarnings("unchecked")
	public <T extends ERXRouteController> T controller(String entityName) {
		return controller((Class<T>) requestHandler().routeControllerClassForEntityNamed(entityName));
	}

	/**
	 * Returns another controller, passing the required state on.
	 * 
	 * @param <T>
	 *            the type of controller to return
	 * @param controllerClass
	 *            the controller class to lookup
	 * @return the created controller
	 */
	public <T extends ERXRouteController> T controller(Class<T> controllerClass) {
		try {
			T controller = requestHandler().controller(controllerClass, request(), context());
			controller._setRoute(_route);
			controller._setEditingContent(_editingContext);
			controller._setRouteKeys(_routeKeys);
			controller._setRouteObjects(_objects);
			controller.setOptions(_options);
			return controller;
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
	
	/**
	 * Disposes any resources the route controller may be holding onto (like its editing context).
	 */
	public void dispose() {
		if (_shouldDisposeEditingContext && _editingContext != null) {
			if(_editingContext instanceof ERXEC && ((ERXEC) _editingContext).isAutoLocked()) {
				_editingContext.unlock();
			}

			_editingContext.dispose();
			_editingContext = null;
		}
	}
	
	/**
	 * Returns whether or not this request is for a schema.
	 * 
	 * @return whether or not this request is for a schema
	 */
	protected boolean isSchemaRequest() {
		return request().stringFormValueForKey("schema") != null;
	}
	
	/**
	 * Returns the schema response for the current entity with the given filter.
	 * 
	 * @param filter the filter to apply
	 * @return the schema response for the current entity with the given filter
	 */
	protected WOActionResults schemaResponse(ERXKeyFilter filter) {
		return schemaResponseForEntityNamed(entityName(), filter);
	}
	
	/**
	 * Returns the schema response for the given entity with the given filter.
	 * 
	 * @param entityName the entity name
	 * @param filter the filter to apply
	 * @return the schema response for the given entity with the given filter
	 */
	protected WOActionResults schemaResponseForEntityNamed(String entityName, ERXKeyFilter filter) {
		NSDictionary<String, Object> properties = ERXRestSchema.schemaForEntityNamed(entityName, filter);
		return response(properties, ERXKeyFilter.filterWithAllRecursive());
	}
	
	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + ": " + request().uri() + "]";
	}
	
	private static final String REQUEST_CONTROLLERS_KEY = "ERRest.controllersForRequest";
	
	/**
	 * Registers the given controller with the given request, so it can be later disposed. This can be a
	 * very useful performance optimization for apps that gets a large number of requests.
	 * 
	 * @param controller the controller to register
	 * @param request the request to register with
	 */
	protected static void _registerControllerForRequest(ERXRouteController controller, WORequest request) {
		NSMutableArray<ERXRouteController> controllers = _controllersForRequest(request);
		if (controllers == null) {
			controllers = new NSMutableArray<ERXRouteController>();
			if (request != null) {
				NSMutableDictionary<String, Object> userInfo = ((ERXRequest)request).mutableUserInfo();
				userInfo.setObjectForKey(controllers, ERXRouteController.REQUEST_CONTROLLERS_KEY);
			}
		}
		controllers.addObject(controller);
	}
	
	/**
	 * Returns the controllers that have been used on the given request.
	 * 
	 * @param request the request
	 */
	@SuppressWarnings("unchecked")
	public static NSMutableArray<ERXRouteController> _controllersForRequest(WORequest request) {
		NSDictionary<String, Object> userInfo = request != null ? request.userInfo() : null;
		NSMutableArray<ERXRouteController> controllers = null;
		if (userInfo != null) {
			controllers = (NSMutableArray<ERXRouteController>)userInfo.objectForKey(ERXRouteController.REQUEST_CONTROLLERS_KEY);
		}
		return controllers;
	}
	
	/**
	 * Disposes all of the controllers that were used on the given request.
	 * 
	 * @param request the request
	 */
	public static void _disposeControllersForRequest(WORequest request) {
		NSArray<ERXRouteController> controllers = ERXRouteController._controllersForRequest(request);
		if (controllers != null) {
			for (ERXRouteController controller : controllers) {
				controller.dispose();
			}
		}
	}
}
