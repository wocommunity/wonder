package er.rest.routes;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAction;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.eof.ERXDatabaseContextDelegate.ObjectNotAvailableException;
import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXEORestDelegate;
import er.rest.ERXRequestFormValues;
import er.rest.ERXRestClassDescriptionFactory;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXWORestResponse;
import er.rest.format.IERXRestParser;

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
	protected ERXKeyFilter includeOptional(ERXKey key, ERXKeyFilter filter) {
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
			_editingContext = ERXEC.newEditingContext();
		}
		return _editingContext;
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
	public Object routeObjectForKey(String key) {
		return routeObjects().objectForKey(new ERXRoute.Key(key));
	}

	/**
	 * Returns all the processed objects from the route keys. For instance, if your route specifies that you have a
	 * {person:Person}, routeObjectForKey("person") will return a Person object.
	 * 
	 * @return the processed objects from the route keys
	 */
	public NSDictionary<ERXRoute.Key, Object> routeObjects() {
		if (_objects == null) {
			_objects = _route.keysWithObjects(_routeKeys, delegate());
		}
		return _objects;
	}

	/**
	 * Returns all the processed objects from the route keys. For instance, if your route specifies that you have a
	 * {person:Person}, routeObjectForKey("person") will return a Person object. This method does NOT cache the results.
	 * 
	 * @parmam delegate the delegate to fetch with
	 * @return the processed objects from the route keys
	 */
	public NSDictionary<ERXRoute.Key, Object> routeObjects(IERXRestDelegate delegate) {
		_objects = _route.keysWithObjects(_routeKeys, delegate);
		return _objects;
	}

	/**
	 * Returns the default format to use if no other format is found, or if the requested format is invalid.
	 * 
	 * @return the default format to use if no other format is found, or if the requested format is invalid
	 */
	protected ERXRestFormat defaultFormat() {
		return ERXRestFormat.XML;
	}

	/**
	 * Returns the format that the user requested (usually based on the request file extension).
	 * 
	 * @return the format that the user requested
	 */
	public ERXRestFormat format() {
		String type = (String) request().userInfo().objectForKey(ERXRouteRequestHandler.TypeKey);
		/*
		 * if (type == null) { List<String> acceptTypesList = new LinkedList<String>(); String accept =
		 * request().headerForKey("Accept"); if (accept != null) { String[] acceptTypes = accept.split(","); for (String
		 * acceptType : acceptTypes) { int semiIndex = acceptType.indexOf(";"); if (semiIndex == -1) {
		 * acceptTypesList.add(acceptType); } else { acceptTypesList.add(acceptType.substring(0, semiIndex)); } } } }
		 */

		ERXRestFormat format;
		if (type == null) {
			format = defaultFormat();
		}
		else {
			format = ERXRestFormat.formatNamed(type);
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
		return new ERXEORestDelegate(editingContext());
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
	 * Returns the object from the request data that is of the given entity name and is filtered with the given filter.
	 * This will use the delegate returned from this controller's delegate() method.
	 * 
	 * @param entityName
	 *            the entity name of the object in the request
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @return the object from the request data
	 */
	public Object object(String entityName, ERXKeyFilter filter) {
		return object(entityName, filter, delegate());
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
	public Object object(String entityName, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return requestNode().objectWithFilter(entityName, filter, delegate);
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
	public Object create(String entityName, ERXKeyFilter filter) {
		return create(entityName, filter, delegate());
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
	public Object create(String entityName, ERXKeyFilter filter, IERXRestDelegate delegate) {
		return requestNode().createObjectWithFilter(entityName, filter, delegate);
	}

	/**
	 * Updates the given object from the request data with the given filter. This will use the delegate returned from
	 * this controller's delegate() method.
	 * 
	 * @param obj
	 *            the object to update
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @return the object from the request data
	 */
	public void update(Object obj, ERXKeyFilter filter) {
		update(obj, filter, delegate());
	}

	/**
	 * Updates the given object from the request data with the given filter.
	 * 
	 * @param obj
	 *            the object to update
	 * @param filter
	 *            the filter to apply to the object for the purposes of updating (or null to not update)
	 * @param delegate
	 *            the delegate to use
	 * @return the object from the request data
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
	 * @param entity
	 *            the entity type of the array
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
		WOResponse response = stringResponse(ERXExceptionUtilities.toParagraph(t));
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
		WOResponse response = stringResponse(errorMessage);
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

	@Override
	public WOActionResults performActionNamed(String s) {
		try {
			checkAccess();
			WOActionResults results = super.performActionNamed(s);
			if (results == null) {
				results = response(null, ERXKeyFilter.filterWithAttributes());
			}
			else if (results instanceof IERXRouteComponent) {
				_takeRouteParametersFromRequest(results);
			}

			WOContext context = context();
			WOSession session = context._session();
			if (session != null && session.storesIDsInCookies() && results instanceof WOResponse) {
				WOResponse response = (WOResponse) results;
				session._appendCookieToResponse(response);
			}

			return results;
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
			T controller = controllerClass.getConstructor(WORequest.class).newInstance(request());
			controller._route = _route;
			controller._editingContext = _editingContext;
			controller._routeKeys = _routeKeys;
			controller._objects = _objects;
			controller._options = _options;
			controller._requestHandler = _requestHandler;
			Field contextField = WOAction.class.getDeclaredField("_context");
			contextField.setAccessible(true);
			contextField.set(controller, context());
			return controller;
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
}
