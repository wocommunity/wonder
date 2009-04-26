package er.rest.routes;

import java.text.ParseException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.eof.ERXDatabaseContextDelegate.ObjectNotAvailableException;
import er.extensions.foundation.ERXExceptionUtilities;
import er.rest.ERXRestException;
import er.rest.ERXRestRequestNode;
import er.rest.format.ERXRestFormat;
import er.rest.routes.model.IERXEntity;

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
	private ERXRestRequestNode _requestNode;

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
	 * Override to provide custom security checks. It is not necessary to call
	 * super on this method.
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
	 * Sets the parsed keys from the route.
	 * 
	 * @param keys
	 *            the parsed keys from the route
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

	public String stringForKey(String key) {
		return _keys.objectForKey(new ERXRoute.Key(key));
	}
	
	public boolean hasKey(String key) {
		return _keys.containsKey(new ERXRoute.Key(key));
	}
	
	public Object objectForKey(String key) {
		return objects().objectForKey(key);
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
		ERXRestFormat format = ERXRestFormat.formatNamed(typeKey);
		return format;
	}

	public ERXRestRequestNode requestNode() {
		if (_requestNode == null) {
			try {
				_requestNode = format().parser().parseRestRequest(request());
			}
			catch (Throwable t) {
				throw new RuntimeException("Failed to parse a " + format() + " request.", t);
			}
		}
		return _requestNode;
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

	public WOResponse json(ERXKeyFilter filter, IERXEntity entity, NSArray<?> values) {
		return response(filter, entity, values, ERXRestFormat.JSON);
	}

	public WOResponse plist(ERXKeyFilter filter, String entityName, NSArray<?> values) {
		return response(filter, editingContext(), entityName, values, ERXRestFormat.PLIST);
	}

	public WOResponse plist(ERXKeyFilter filter, IERXEntity entity, NSArray<?> values) {
		return response(filter, entity, values, ERXRestFormat.PLIST);
	}

	public WOResponse plist(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, ERXRestFormat.PLIST);
	}

	public WOResponse xml(ERXKeyFilter filter, String entityName, NSArray<?> values) {
		return response(filter, editingContext(), entityName, values, ERXRestFormat.XML);
	}

	public WOResponse xml(ERXKeyFilter filter, IERXEntity entity, NSArray<?> values) {
		return response(filter, entity, values, ERXRestFormat.XML);
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
		return response(filter, IERXEntity.Factory.entityNamed(editingContext, entityName), values, format);
	}

	public WOResponse response(ERXKeyFilter filter, IERXEntity entity, NSArray<?> values, ERXRestFormat format) {
		return stringResponse(ERXRestRequestNode.requestNodeWithObjectAndFilter(entity, values, filter).toString(format.writer()));
	}

	public WOResponse json(ERXKeyFilter filter, Object value) {
		return response(filter, value, ERXRestFormat.JSON);
	}

	public WOResponse plist(ERXKeyFilter filter, Object value) {
		return response(filter, value, ERXRestFormat.PLIST);
	}

	public WOResponse xml(ERXKeyFilter filter, Object value) {
		return response(filter, value, ERXRestFormat.XML);
	}

	public WOResponse response(ERXKeyFilter filter, Object value) {
		return response(filter, value, format());
	}

	public WOResponse response(ERXKeyFilter filter, Object value, ERXRestFormat format) {
		try {
			return stringResponse(ERXRestRequestNode.requestNodeWithObjectAndFilter(value, filter).toString(format.writer()));
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

	public WOResponse errorResponse(Throwable t, int status) {
		WOResponse response = stringResponse(ERXExceptionUtilities.toParagraph(t));
		response.setStatus(status);
		log.error("Request failed: " + request().uri(), t);
		return response;
	}

	public Object object(String entityName, ERXKeyFilter filter) throws ParseException, ERXRestException {
		return object(entityName, filter, delegate());
	}

	public Object object(String entityName, ERXKeyFilter filter, ERXRestRequestNode.Delegate delegeate) throws ParseException, ERXRestException {
		return requestNode().objectWithFilter(entityName, filter, delegeate);
	}

	public Object create(String entityName, ERXKeyFilter filter) throws ParseException, ERXRestException {
		return create(entityName, filter, delegate());
	}

	public Object create(String entityName, ERXKeyFilter filter, ERXRestRequestNode.Delegate delegeate) throws ParseException, ERXRestException {
		return requestNode().createObjectWithFilter(entityName, filter, delegeate);
	}

	public void update(Object obj, ERXKeyFilter filter) throws ParseException, ERXRestException {
		update(obj, filter, delegate());
	}

	public void update(Object obj, ERXKeyFilter filter, ERXRestRequestNode.Delegate delegate) throws ParseException, ERXRestException {
		requestNode().updateObjectWithFilter(obj, filter, delegate);
	}

	protected ERXRestRequestNode.Delegate delegate() {
		return new ERXRestRequestNode.EODelegate(editingContext());
	}

	@Override
	public WOActionResults performActionNamed(String s) {
		try {
			checkAccess();
			WOActionResults results = super.performActionNamed(s);
			if (results == null) {
				results = response(ERXKeyFilter.filterWithAttributes(), null);
			}
			return results;
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
