package er.rest;

import java.text.ParseException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.eof.ERXDatabaseContextDelegate.ObjectNotAvailableException;
import er.extensions.foundation.ERXExceptionUtilities;

/**
 * EXPERIMENTAL
 * 
 * ERXRouteController is equivalent to a Rails controller class. It's actually a direct action, and has the same naming
 * rules as a direct action, so your controller action methods must end in the name "Action". There are several utility
 * methods for manipulating restful requests and responses (update(..), create(..), requestNode(), response(..), etc) ,
 * and it supports multiple formats for you.
 * 
 * @author mschrag
 */
public class ERXRouteController extends WODirectAction {
	protected static Logger log = Logger.getLogger(ERXRouteController.class);

	private ERXRoute _route;
	private NSDictionary<ERXRoute.Key, String> _keys;
	private EOEditingContext _editingContext;

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
	 * Sets the parsed keys from the route.
	 * 
	 * @param keys the parsed keys from the route
	 */
	public void _setKeys(NSDictionary<ERXRoute.Key, String> keys) {
		_keys = keys;
	}

	/**
	 * Returns the parsed keys from the route.
	 * 
	 * @return the parsed keys from the route
	 */
	public NSDictionary<ERXRoute.Key, String> keys() {
		return _keys;
	}

	public NSDictionary<String, Object> objects() {
		return objects(editingContext());
	}

	public NSDictionary<String, Object> objects(EOEditingContext editingContext) {
		return _route.objects(_keys, editingContext);
	}

	public NSDictionary<ERXRoute.Key, Object> keysWithObjects() {
		return keysWithObjects(editingContext());
	}

	public NSDictionary<ERXRoute.Key, Object> keysWithObjects(EOEditingContext editingContext) {
		return _route.keysWithObjects(_keys, editingContext);
	}

	public ERXRestFormat format() {
		String typeKey = (String) request().userInfo().objectForKey(ERXRouteRequestHandler.TypeKey);
		ERXRestFormat format = ERXRestFormat.valueOf(typeKey.toUpperCase());
		return format;
	}

	public ERXRestRequestNode requestNode() {
		try {
			return format().parser().parseRestRequest(request());
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to parse a " + format() + " request.", t);
		}
	}

	public WOResponse stringResponse(String str) {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		response.appendContentString(str);
		return response;
	}

	public WOResponse json(ERXKeyFilter filter, String entityName, NSArray<?> values) {
		return response(filter, editingContext(), entityName, values, ERXRestFormat.JSON);
	}

	public WOResponse json(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, ERXRestFormat.JSON);
	}

	public WOResponse plist(ERXKeyFilter filter, String entityName, NSArray<?> values) {
		return response(filter, editingContext(), entityName, values, ERXRestFormat.PLIST);
	}

	public WOResponse plist(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, ERXRestFormat.PLIST);
	}

	public WOResponse xml(ERXKeyFilter filter, String entityName, NSArray<?> values) {
		return response(filter, editingContext(), entityName, values, ERXRestFormat.XML);
	}

	public WOResponse xml(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, ERXRestFormat.XML);
	}

	public WOResponse response(ERXKeyFilter filter, String entityName, NSArray<?> values) {
		return response(filter, editingContext(), entityName, values, format());
	}

	public WOResponse response(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, format());
	}

	public WOResponse response(ERXKeyFilter filter, String entityName, NSArray<?> values, ERXRestFormat format) {
		return response(filter, editingContext(), entityName, values, format);
	}

	public WOResponse response(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values, ERXRestFormat format) {
		try {
			return stringResponse(format.writer(filter).toString(editingContext, entityName, values));
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to generate a " + format + " response.", t);
		}
	}

	public WOResponse json(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, ERXRestFormat.JSON);
	}

	public WOResponse plist(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, ERXRestFormat.PLIST);
	}

	public WOResponse xml(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, ERXRestFormat.XML);
	}

	public WOResponse response(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, format());
	}

	public WOResponse errorResponse(Throwable t, int status) {
		WOResponse response = stringResponse(ERXExceptionUtilities.toParagraph(t));
		response.setStatus(status);
		log.error("Request failed: " + request().uri(), t);
		return response;
	}

	public WOResponse response(ERXKeyFilter filter, EOEnterpriseObject value, ERXRestFormat format) {
		try {
			return stringResponse(format.writer(filter).toString(value));
		}
		catch (ObjectNotAvailableException e) {
			return errorResponse(e, WOResponse.HTTP_STATUS_NOT_FOUND);
		}
		catch (ERXRestNotFoundException e) {
			return errorResponse(e, WOResponse.HTTP_STATUS_NOT_FOUND);
		}
		catch (SecurityException e) {
			return errorResponse(e, WOResponse.HTTP_STATUS_FORBIDDEN);
		}
		catch (ERXRestSecurityException e) {
			return errorResponse(e, WOResponse.HTTP_STATUS_FORBIDDEN);
		}
		catch (Throwable t) {
			return errorResponse(t, WOResponse.HTTP_STATUS_INTERNAL_ERROR);
		}
	}

	protected ERXRestRequestNode.Delegate delegate() {
		return new ERXRestRequestNode.EODelegate(editingContext());
	}

	public Object create(String name, ERXKeyFilter filter) throws ParseException, ERXRestException {
		return create(name, filter, delegate());
	}

	public Object create(String name, ERXKeyFilter filter, ERXRestRequestNode.Delegate delegeate) throws ParseException, ERXRestException {
		return requestNode().createObjectWithFilter(name, filter, delegeate);
	}

	public void update(Object obj, ERXKeyFilter filter) throws ParseException, ERXRestException {
		update(obj, filter, delegate());
	}

	public void update(Object obj, ERXKeyFilter filter, ERXRestRequestNode.Delegate delegate) throws ParseException, ERXRestException {
		requestNode().applyToObjectWithFilter(obj, filter, delegate);
	}

	@Override
	public WOActionResults performActionNamed(String s) {
		try {
			return super.performActionNamed(s);
		}
		catch (ObjectNotAvailableException e) {
			return errorResponse(e, WOResponse.HTTP_STATUS_NOT_FOUND);
		}
		catch (SecurityException e) {
			return errorResponse(e, WOResponse.HTTP_STATUS_FORBIDDEN);
		}
		catch (Throwable t) {
			return errorResponse(t, WOResponse.HTTP_STATUS_INTERNAL_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
		return (T) super.pageWithName(componentClass.getName());
	}
}
