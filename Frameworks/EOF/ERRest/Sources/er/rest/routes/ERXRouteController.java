package er.rest.routes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

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
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation._NSUtilities;

import er.extensions.appserver.ERXRequest;
import er.extensions.eof.ERXDatabaseContextDelegate.ObjectNotAvailableException;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRequestFormValues;
import er.rest.ERXRestClassDescriptionFactory;
import er.rest.ERXRestFetchSpecification;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXWORestResponse;
import er.rest.format.IERXRestParser;
import er.rest.routes.jsr311.PathParam;
import er.rest.util.ERXRestSchema;
import er.rest.util.ERXRestTransactionRequestAdaptor;

/**
 * ERXRouteController is equivalent to a Rails controller class. It's actually a direct action, and has the same naming
 * rules as a direct action, so your controller action methods must end in the name "Action". There are several utility
 * methods for manipulating restful requests and responses (update(..), create(..), requestNode(), response(..), etc) ,
 * and it supports multiple formats for you.
 * 
 * @author mschrag
 */
public class ERXRouteController extends WODirectAction {
	protected static Logger log = Logger.getLogger(ERXRouteController.class);

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

	/**
	 * Constructs a new ERXRouteController.
	 * 
	 * @param request
	 *            the request
	 */
	public ERXRouteController(WORequest request) {
		super(request);
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
			_objects = ERXRoute.keysWithObjects(_routeKeys, delegate());
		}
		return _objects;
	}

	/**
	 * Returns all the processed objects from the route keys. For instance, if your route specifies that you have a
	 * {person:Person}, routeObjectForKey("person") will return a Person object. This method does NOT cache the results.
	 * 
	 * @param delegate the delegate to fetch with
	 * @return the processed objects from the route keys
	 */
	public NSDictionary<ERXRoute.Key, Object> routeObjects(IERXRestDelegate delegate) {
		if (_route != null) {
			_objects = ERXRoute.keysWithObjects(_routeKeys, delegate);
		}
		return _objects;
	}

	/**
	 * Returns the default format to use if no other format is found, or if the requested format is invalid.
	 * 
	 * @return the default format to use if no other format is found, or if the requested format is invalid
	 */
	protected ERXRestFormat defaultFormat() {
		String defaultFormatName = ERXProperties.stringForKeyWithDefault("ERXRest.defaultFormat", ERXRestFormat.XML.name());
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
			 * if (type == null) {
			 *   List<String> acceptTypesList = new LinkedList<String>();
			 *   String accept = request().headerForKey("Accept");
			 *   if (accept != null) {
			 *     String[] acceptTypes = accept.split(",");
			 *     for (String acceptType : acceptTypes) {
			 *       int semiIndex = acceptType.indexOf(";");
			 *       if (semiIndex == -1) {
			 *         acceptTypesList.add(acceptType);
			 *       } else { 
			 *         acceptTypesList.add(acceptType.substring(0, semiIndex));
			 *       }
			 *     }
			 *   }
			 * }
			 */
	
			if (type == null) {
				format = defaultFormat();
			}
			else {
				format = ERXRestFormat.formatNamed(type);
			}
		}
		return format;
	}

	/**
	 * Returns the default rest delegate for this controller (an ERXRestRequestNode.EODelegate using the editing context
	 * returned from editingContext()). Override this method to provide a custom delegate implementation for this
	 * controller.
	 * 
	 * @return a default rest delegate
	 */
	protected IERXRestDelegate delegate() {
		return IERXRestDelegate.Factory.delegateForEntityNamed(entityName(), editingContext());
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
				_requestNode = parser.parseRestRequest(request(), format().delegate());
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
		return (T)object(entityName(), filter, delegate());
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
		return (T)object(entityName, filter, delegate());
	}

	/**
	 * Returns the object from the request data that is of the routed entity name and is filtered with the given filter.
	 * 
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @param delegate
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T object(ERXKeyFilter filter, IERXRestDelegate delegate) {
		return (T)requestNode().objectWithFilter(entityName(), filter, delegate);
	}

	/**
	 * Returns the object from the request data that is of the given entity name and is filtered with the given filter.
	 * 
	 * @param entityName
	 *            the entity name of the object in the request
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @param delegate
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T object(String entityName, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return (T)requestNode().objectWithFilter(entityName, filter, delegate);
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
		return (T)create(entityName, filter, delegate());
	}

	/**
	 * Creates a new object from the request data that is of the routed entity name and is filtered with the given
	 * filter.
	 * 
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to just create a blank one)
	 * @param delegate
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(ERXKeyFilter filter, IERXRestDelegate delegate) {
		return (T)requestNode().createObjectWithFilter(entityName(), filter, delegate);
	}

	/**
	 * Creates a new object from the request data that is of the given entity name and is filtered with the given
	 * filter.
	 * 
	 * @param entityName
	 *            the entity name of the object in the request
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to just create a blank one)
	 * @param delegate
	 *            the delegate to use
	 * @return the object from the request data
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(String entityName, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return (T)requestNode().createObjectWithFilter(entityName, filter, delegate);
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
		update(obj, filter, delegate());
	}

	/**
	 * Updates the given object from the request data with the given filter.
	 * 
	 * @param obj
         *            object to update
	 * @param filter
         *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @param delegate
         *            delegate to use
	 */
	public void update(Object obj, ERXKeyFilter filter, IERXRestDelegate delegate) {
		requestNode().updateObjectWithFilter(obj, filter, delegate);
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
		return response(ERXRestFormat.JSON, editingContext(), entityName, values, filter);
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
		return response(ERXRestFormat.JSON, editingContext, entityName, values, filter);
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
		return response(ERXRestFormat.JSON, entity, values, filter);
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
		return response(ERXRestFormat.PLIST, editingContext(), entityName, values, filter);
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
		return response(ERXRestFormat.PLIST, editingContext, entityName, values, filter);
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
		return response(ERXRestFormat.PLIST, entity, values, filter);
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
		return response(ERXRestFormat.XML, editingContext(), entityName, values, filter);
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
		return response(ERXRestFormat.XML, editingContext, entityName, values, filter);
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
		return response(ERXRestFormat.XML, entity, values, filter);
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
		WOActionResults response;
		if (fetchSpec == null) {
			// MS: you probably meant to call response(Object, filter) in this case -- just proxy through
			response = response(format(), null, filter);
		}
		else {
			response = response(format(), editingContext(), fetchSpec.entityName(), fetchSpec.objects(editingContext(), options()), filter);
		}
		return response;
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
			responseNode = ERXRestRequestNode.requestNodeWithObjectAndFilter(entity, values, filter, delegate());
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
		ERXRouteResults results = new ERXRouteResults(context(), format, responseNode);
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
		return response(ERXRestFormat.JSON, value, filter);
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
		return response(ERXRestFormat.PLIST, value, filter);
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
		return response(ERXRestFormat.XML, value, filter);
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
			responseNode = ERXRestRequestNode.requestNodeWithObjectAndFilter(value, filter, delegate());
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
	 * Returns the response from a HEAD call to this controller.
	 * 
	 * @return a head response
	 */
	public WOActionResults headAction() {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		format().writer().appendHeadersToResponse(null, new ERXWORestResponse(response));
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
			String setMethodName = "set" + ERXStringUtilities.capitalize(key.key());
			try {
				Method setStringMethod = resultsClass.getMethod(setMethodName, String.class);
				ERXRouteParameter routeParameter = setStringMethod.getAnnotation(ERXRouteParameter.class);
				if (routeParameter != null) {
					setStringMethod.invoke(results, routeStringForKey(key.key()));
				}
			}
			catch (NoSuchMethodException e) {
				try {
					Class<?> valueType = _NSUtilities.classWithName(key.valueType());
					Method setObjectMethod = resultsClass.getMethod(setMethodName, valueType);
					ERXRouteParameter routeParameter = setObjectMethod.getAnnotation(ERXRouteParameter.class);
					if (routeParameter != null) {
						setObjectMethod.invoke(results, routeObjectForKey(key.key()));
					}
				}
				catch (NoSuchMethodException e2) {
					// SKIP
				}
				catch (IllegalArgumentException e2) {
					e2.printStackTrace();
				}
				catch (IllegalAccessException e2) {
					e2.printStackTrace();
				}
				catch (InvocationTargetException e2) {
					e2.printStackTrace();
				}
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
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
	protected WOActionResults performUnknownAction(String actionName) {
		throw new RuntimeException("There is no action named '" + actionName + "' on '" + getClass().getSimpleName() + ".");
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

			if (results == null && isAutomaticHtmlRoutingEnabled() && format() == ERXRestFormat.HTML) {
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
			}

			if (results == null) {
		        Method actionMethod = _methodForAction(actionName, WODirectAction.actionText);
		        if (actionMethod == null || actionMethod.getParameterTypes().length > 0) {
		        	String actionMethodName = actionName + WODirectAction.actionText;
		        	int bestParameterCount = 0;
		        	Method bestMethod = null;
        			List<PathParam> bestParams = null;
		        	for (Method method : getClass().getDeclaredMethods()) {
		        		if (method.getName().equals(actionMethodName)) {
		        			int parameterCount = 0;
				        	List<PathParam> params = new LinkedList<PathParam>();
		        			for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
		        				for (Annotation parameterAnnotation : parameterAnnotations) {
		        					if (parameterAnnotation instanceof PathParam) {
		        						PathParam pathParam = (PathParam)parameterAnnotation;
		        						params.add(pathParam);
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
		        			if (parameterCount > bestParameterCount) {
		        				bestMethod = method;
		        				bestParameterCount = parameterCount;
		        				bestParams = params;
		        			}
		        		}
		        	}
		        	if (bestMethod == null) {
		        		performUnknownAction(actionName);
		        	}
		        	else {
		        		Object[] params = new Object[bestParameterCount];
		        		for (int paramNum = 0; paramNum < params.length; paramNum ++) {
		        			PathParam param = bestParams.get(paramNum);
		        			params[paramNum] = routeObjectForKey(param.value());
		        		}
		        		results = (WOActionResults)bestMethod.invoke(this, params);
		        	}
		        }
		        else {
	        		results = (WOActionResults)actionMethod.invoke(this, _NSUtilities._NoObjectArray);
		        }
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
			Throwable meaningfulThrowble = ERXExceptionUtilities.getMeaningfulThrowable(t);
			if (meaningfulThrowble instanceof ObjectNotAvailableException) {
				results = errorResponse(meaningfulThrowble, WOMessage.HTTP_STATUS_NOT_FOUND);
			}
			else if (meaningfulThrowble instanceof SecurityException) {
				results = errorResponse(meaningfulThrowble, WOMessage.HTTP_STATUS_FORBIDDEN);
			}
			else {
				results = errorResponse(meaningfulThrowble, WOMessage.HTTP_STATUS_INTERNAL_ERROR);
			}
			// MS: Should we jam the exception in the response userInfo so the transaction adaptor can rethrow the real exception?
		}

		WOContext context = context();
		WOSession session = context._session();
		if (results instanceof WOResponse) {
			WOResponse response = (WOResponse)results;
			if (session != null && session.storesIDsInCookies()) {
				session._appendCookieToResponse(response);
			}
			
			String allowOrigin = accessControlAllowOrigin();
			if (allowOrigin != null) {
				response.setHeader(allowOrigin, "Access-Control-Allow-Origin");
			}
		
			if (allowWindowNameCrossDomainTransport()) {
				String windowNameCrossDomainTransport = request().stringFormValueForKey("windowname");
				if ("true".equals(windowNameCrossDomainTransport)) {
					String content = response.contentString();
					if (content != null) {
						content = content.replaceAll("\n", "");
						content = ERXStringUtilities.escapeJavascriptApostrophes(content);
					}
					response.setContent("<html><script type=\"text/javascript\">window.name='" + content + "';</script></html>");
					response.setHeader("text/html", "Content-Type");
				}
			}
		}
		
		return results;
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
	public WOActionResults optionsAction() {
		WOResponse response = new WOResponse();
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
			controller._route = _route;
			controller._editingContext = _editingContext;
			controller._routeKeys = _routeKeys;
			controller._objects = _objects;
			controller._options = _options;
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
		if (_editingContext != null) {
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
