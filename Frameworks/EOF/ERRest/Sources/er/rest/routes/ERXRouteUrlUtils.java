package er.rest.routes;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestContext;
import er.rest.IERXRestDelegate;

/**
 * Utilities for generating URLs to ERXRouteController routes (quicky impl).
 * 
 * @author mschrag
 */
public class ERXRouteUrlUtils {
	protected static String changeDirectActionRequestHandlerTo(String url, String newRequestHandler) {
		String newUrl = url.replaceFirst("/" + WOApplication.application().directActionRequestHandlerKey() + "(/|$)", "/" + newRequestHandler + "$1");
		return newUrl;
	}

	public static String actionUrlForRecord(WOContext context, ERXGenericRecord record, String action, String format, NSDictionary<String, Object> queryParameters, boolean secure, boolean includeSessionID) {
		Object entityID = IERXRestDelegate.Factory.delegateForEntityNamed(record.entityName()).primaryKeyForObject(record, new ERXRestContext(record.editingContext()));
		String url = ERXWOContext.directActionUrl(context, ERXRouteUrlUtils.actionUrlPathForEntity(record.entityName(), entityID, action, format), queryParameters, Boolean.valueOf(secure), includeSessionID);
		url = ERXRouteUrlUtils.changeDirectActionRequestHandlerTo(url, ERXRouteRequestHandler.Key);
		return url;
	}

	public static String actionUrlForEntity(WOContext context, String entityName, Object entityID, String action, String format, NSDictionary<String, Object> queryParameters, boolean secure, boolean includeSessionID) {
		String url = ERXWOContext.directActionUrl(context, ERXRouteUrlUtils.actionUrlPathForEntity(entityName, entityID, action, format), queryParameters, Boolean.valueOf(secure), includeSessionID);
		url = ERXRouteUrlUtils.changeDirectActionRequestHandlerTo(url, ERXRouteRequestHandler.Key);
		return url;
	}

	public static String actionUrlForEntityType(WOContext context, String entityName, String action, String format, NSDictionary<String, Object> queryParameters, boolean secure, boolean includeSessionID) {
		String url = ERXWOContext.directActionUrl(context, ERXRouteUrlUtils.actionUrlPathForEntity(entityName, null, action, format), queryParameters, Boolean.valueOf(secure), includeSessionID);
		url = ERXRouteUrlUtils.changeDirectActionRequestHandlerTo(url, ERXRouteRequestHandler.Key);
		return url;
	}

	public static String actionUrlPathForEntity(String entityName, Object entityID, String action, String format) {
		StringBuilder sb = new StringBuilder();
		ERXRouteRequestHandler requestHandler = (ERXRouteRequestHandler) WOApplication.application().requestHandlerForKey(ERXRouteRequestHandler.Key);
		sb.append(requestHandler.controllerPathForEntityNamed(entityName));
		if (entityID != null) {
			sb.append('/');
			sb.append(ERXStringUtilities.escapeNonXMLChars(String.valueOf(entityID)));
			if (action != null && !"show".equals(action)) {
				sb.append('/');
				sb.append(action);
			}
		}
		else if (action != null && !"index".equals(action)) {
			if ((entityName != null) && (entityName.length() > 0)) {
				sb.append('/');				
			}
			sb.append(action);
		}
		sb.append('.');
		if (format == null) {
			sb.append("html");
		}
		else {
			sb.append(format);
		}
		return sb.toString();
	}
}
