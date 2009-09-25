package er.rest.routes;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.foundation.ERXStringUtilities;

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

	public static String actionUrlForRecord(WOContext context, ERXGenericRecord record, String action, NSDictionary<String, Object> queryParameters, boolean secure, boolean includeSessionID) {
		String url = ERXWOContext.directActionUrl(context, ERXRouteUrlUtils.actionUrlPathForEntity(record.entityName(), record.primaryKeyInTransaction(), action), queryParameters, Boolean.valueOf(secure), includeSessionID);
		url = ERXRouteUrlUtils.changeDirectActionRequestHandlerTo(url, ERXRouteRequestHandler.Key);
		return url;
	}

	public static String actionUrlForEntity(WOContext context, String entityName, Object entityID, String action, NSDictionary<String, Object> queryParameters, boolean secure, boolean includeSessionID) {
		String url = ERXWOContext.directActionUrl(context, ERXRouteUrlUtils.actionUrlPathForEntity(entityName, entityID, action), queryParameters, Boolean.valueOf(secure), includeSessionID);
		url = ERXRouteUrlUtils.changeDirectActionRequestHandlerTo(url, ERXRouteRequestHandler.Key);
		return url;
	}

	public static String actionUrlForEntityType(WOContext context, String entityName, String action, NSDictionary<String, Object> queryParameters, boolean secure, boolean includeSessionID) {
		String url = ERXWOContext.directActionUrl(context, ERXRouteUrlUtils.actionUrlPathForEntity(entityName, null, action), queryParameters, Boolean.valueOf(secure), includeSessionID);
		url = ERXRouteUrlUtils.changeDirectActionRequestHandlerTo(url, ERXRouteRequestHandler.Key);
		return url;
	}

	public static String actionUrlPathForEntity(String entityName, Object entityID, String action) {
		StringBuffer sb = new StringBuffer();
		ERXRouteRequestHandler requestHandler = (ERXRouteRequestHandler) WOApplication.application().requestHandlerForKey(ERXRouteRequestHandler.Key);
		sb.append(requestHandler.controllerPathForEntityNamed(entityName));
		if (entityID != null) {
			sb.append("/");
			sb.append(ERXStringUtilities.escapeNonXMLChars(String.valueOf(entityID)));
			if (action != null && !"show".equals(action)) {
				sb.append("/");
				sb.append(action);
			}
		}
		else if (action != null && !"index".equals(action)) {
			sb.append("/");
			sb.append(action);
		}
		sb.append(".html");
		return sb.toString();
	}
}
