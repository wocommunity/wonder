package er.rest.routes.components;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXRequest;
import er.extensions.components.ERXComponent;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEnterpriseObject;
import er.rest.ERXRestClassDescriptionFactory;
import er.rest.ERXRestContext;
import er.rest.IERXRestDelegate;
import er.rest.routes.ERXRouteUrlUtils;

/**
 * Generate a URL that points to a particular ERXRouteController route (this is a quicky impl and not totally thought out yet).
 * 
 * @author mschrag
 * @binding entityName (optional) the name of the entity to link to
 * @binding id (optional) the id of the entity to link to
 * @binding record (optional) the record to link to
 * @binding action (optional) the rest action to perform (defaults to "show" when an id or record is set, "index" if only an entityName is set)
 * @binding secure (optional) whether or not to generate a secure url (defaults to the same as the current request)
 * @binding queryDictionary (optional) additional query parameters dictionary
 * @binding format (optional) the format to link to (defaults to "html")
 */
public class ERXRouteURL extends ERXComponent {
	public ERXRouteURL(WOContext context) {
		super(context);
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	public Object record() {
		return valueForBinding("record");
	}

	protected void addQueryParameterForKey(Object value, String key, NSMutableDictionary<String, Object> queryParameters) {
		if (value instanceof ERXEnterpriseObject) {
			queryParameters.setObjectForKey(((ERXEnterpriseObject) value).primaryKeyInTransaction(), key);
		}
		else if (value != null) {
			queryParameters.setObjectForKey(value, key);
		}
	}

	@SuppressWarnings("unchecked")
	public String linkURL() {
		String linkUrl;
		String action = (String) valueForBinding("action");

		boolean secure = ERXComponentUtilities.booleanValueForBinding(this, "secure", ERXRequest.isRequestSecure(context().request()));
		boolean includeSessionID = context().hasSession() && context().session().storesIDsInURLs();

		NSMutableDictionary<String, Object> queryParameters = new NSMutableDictionary<>();
		for (String bindingKey : bindingKeys()) {
			if (bindingKey.startsWith("?")) {
				Object value = valueForBinding(bindingKey);
				String key = bindingKey.substring(1);
				if (value != null) {
					String sessionIdKey = WOApplication.application().sessionIdKey();
					if (sessionIdKey.equals(key) && (Boolean.FALSE.equals(value) || "false".equals(value))) {
						includeSessionID = false;
					}
					else {
						addQueryParameterForKey(value, key, queryParameters);
					}
				}
			}
		}

		NSDictionary<String, Object> existingQueryParameters = (NSDictionary<String, Object>) valueForBinding("queryDictionary");
		if (existingQueryParameters != null) {
			for (String key : existingQueryParameters.allKeys()) {
				Object value = existingQueryParameters.objectForKey(key);
				addQueryParameterForKey(value, key, queryParameters);
			}
		}

		boolean absolute = booleanValueForBinding("absolute");
		if (absolute) {
			context().generateCompleteURLs();
		}
		String format = stringValueForBinding("format", "html");
		Object record = record();
		if (record != null) {
			String entityName = (String) valueForBinding("entityName");
			if (entityName == null) {
				entityName = ERXRestClassDescriptionFactory.entityNameForObject(record);
			}
			EOEditingContext editingContext = ERXEC.newEditingContext();
			Object entityID = IERXRestDelegate.Factory.delegateForEntityNamed(entityName).primaryKeyForObject(record, new ERXRestContext(editingContext));
			editingContext.dispose();
			linkUrl = ERXRouteUrlUtils.actionUrlForEntity(context(), entityName, entityID, action, format, queryParameters, secure, includeSessionID);
		}
		else {
			String entityName = (String) valueForBinding("entityName");
			String id = (String) valueForBinding("id");
			if (entityName != null) {
				if (id != null) {
					linkUrl = ERXRouteUrlUtils.actionUrlForEntity(context(), entityName, id, action, format, queryParameters, secure, includeSessionID);
				}
				else {
					linkUrl = ERXRouteUrlUtils.actionUrlForEntityType(context(), entityName, action, format, queryParameters, secure, includeSessionID);
				}
			}
			else {
				linkUrl = null;
			}
		}
		if (absolute) {
			context().generateRelativeURLs();
		}
		return linkUrl;
	}
}
