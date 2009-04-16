package er.rest;

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

/**
 * EXPERIMENTAL
 * 
 * @author mschrag
 */
public class ERXRouteDirectAction extends WODirectAction {
	private ERXRoute _route;
	private NSDictionary<ERXRoute.Key, String> _keys;

	public ERXRouteDirectAction(WORequest request, ERXRoute route, NSDictionary<ERXRoute.Key, String> keys) {
		super(request);
		_route = route;
		_keys = keys;
	}

	public ERXRoute route() {
		return _route;
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
	
	public WOResponse response(ERXKeyFilter filter, EOEnterpriseObject value, ERXRestUtils.ResponseType responseType) {
		try {
			return stringResponse(responseType.defaultWriter(filter).toString(value));
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to generate a " + responseType + " response.", t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
		return (T) super.pageWithName(componentClass.getName());
	}
}
