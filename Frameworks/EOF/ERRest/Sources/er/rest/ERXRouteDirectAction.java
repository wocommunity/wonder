package er.rest;

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

import er.extensions.eof.ERXKeyFilter;
import er.extensions.eof.ERXDatabaseContextDelegate.ObjectNotAvailableException;
import er.extensions.foundation.ERXExceptionUtilities;

/**
 * EXPERIMENTAL
 * 
 * @author mschrag
 */
public class ERXRouteDirectAction extends WODirectAction {
	protected static Logger log = Logger.getLogger(ERXRouteDirectAction.class);
	
	private ERXRoute _route;
	private NSDictionary<ERXRoute.Key, String> _keys;

	public ERXRouteDirectAction(WORequest request) {
		super(request);
	}

	public void setRoute(ERXRoute route) {
		_route = route;
	}

	public ERXRoute route() {
		return _route;
	}

	public void setKeys(NSDictionary<ERXRoute.Key, String> keys) {
		_keys = keys;
	}

	public NSDictionary<ERXRoute.Key, String> keys() {
		return _keys;
	}

	public NSDictionary<String, Object> objects(EOEditingContext editingContext) {
		return _route.objects(_keys, editingContext);
	}

	public NSDictionary<ERXRoute.Key, Object> keysWithObjects(EOEditingContext editingContext) {
		return _route.keysWithObjects(_keys, editingContext);
	}

	public ERXRestUtils.ResponseType responseType() {
		String typeKey = (String) request().userInfo().objectForKey(ERXRouteRequestHandler.TypeKey);
		ERXRestUtils.ResponseType responseType = ERXRestUtils.ResponseType.valueOf(typeKey.toUpperCase());
		return responseType;
	}

	public WOResponse stringResponse(String str) {
		WOResponse response = WOApplication.application().createResponseInContext(context());
		response.appendContentString(str);
		return response;
	}

	public WOResponse json(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, ERXRestUtils.ResponseType.JSON);
	}

	public WOResponse plist(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, ERXRestUtils.ResponseType.PLIST);
	}

	public WOResponse xml(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, ERXRestUtils.ResponseType.XML);
	}

	public WOResponse response(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values) {
		return response(filter, editingContext, entityName, values, responseType());
	}

	public WOResponse response(ERXKeyFilter filter, EOEditingContext editingContext, String entityName, NSArray<?> values, ERXRestUtils.ResponseType responseType) {
		try {
			return stringResponse(responseType.defaultWriter(filter).toString(editingContext, entityName, values));
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to generate a " + responseType + " response.", t);
		}
	}

	public WOResponse json(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, ERXRestUtils.ResponseType.JSON);
	}

	public WOResponse plist(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, ERXRestUtils.ResponseType.PLIST);
	}

	public WOResponse xml(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, ERXRestUtils.ResponseType.XML);
	}

	public WOResponse response(ERXKeyFilter filter, EOEnterpriseObject value) {
		return response(filter, value, responseType());
	}

	public WOResponse errorResponse(Throwable t, int status) {
		WOResponse response = stringResponse(ERXExceptionUtilities.toParagraph(t));
		response.setStatus(status);
		log.error("Request failed: " + request().uri(), t);
		return response;
	}

	public WOResponse response(ERXKeyFilter filter, EOEnterpriseObject value, ERXRestUtils.ResponseType responseType) {
		try {
			return stringResponse(responseType.defaultWriter(filter).toString(value));
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
