//
// ERXWOContext.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//
package er.extensions.appserver;

import java.lang.reflect.Method;
import java.net.MalformedURLException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.appserver.ERXResponseRewriter.TagMissingBehavior;
import er.extensions.appserver.ajax.ERXAjaxContext;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXMutableUserInfoHolderInterface;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXThreadStorage;

/**
 * Replacement of WOContext. This subclass is installed when the frameworks
 * loads.
 */
public class ERXWOContext extends ERXAjaxContext implements ERXMutableUserInfoHolderInterface {
	private static Observer observer;
	private boolean _generateCompleteURLs;

	public static final String CONTEXT_KEY = "wocontext";
	private static final String CONTEXT_DICTIONARY_KEY = "ERXWOContext.dict";

	public static class Observer {
		public void applicationDidHandleRequest(NSNotification n) {
			ERXWOContext.setCurrentContext(null);
			ERXThreadStorage.removeValueForKey(ERXWOContext.CONTEXT_DICTIONARY_KEY);
		}
	}

	/**
	 * Returns the existing session if any is given in the form values or url.
	 */
	public WOSession existingSession() {
		String sessionID = _requestSessionID();
		if (!super.hasSession() && sessionID != null)
			WOApplication.application().restoreSessionWithID(sessionID, this);
		return _session();
	}

	/**
	 * Returns true if there is an existing session.
	 */
	@Override
	public boolean hasSession() {
		if (super.hasSession()) {
			return true;
		}
		return existingSession() != null;
	}

	/**
	 * Public constructor
	 */
	public static NSMutableDictionary contextDictionary() {
		if (observer == null) {
			synchronized (ERXWOContext.class) {
				if (observer == null) {
					observer = new Observer();

					NSNotificationCenter.defaultCenter().addObserver(observer, ERXSelectorUtilities.notificationSelector("applicationDidHandleRequest"), WOApplication.ApplicationDidDispatchRequestNotification, null);
				}
			}
		}
		NSMutableDictionary contextDictionary = ERXWOContext._contextDictionary();
		if (contextDictionary == null) {
			contextDictionary = new NSMutableDictionary();
			ERXThreadStorage.takeValueForKey(contextDictionary, ERXWOContext.CONTEXT_DICTIONARY_KEY);
		}
		return contextDictionary;
	}

	public static WOContext currentContext() {
		return (WOContext) ERXThreadStorage.valueForKey(CONTEXT_KEY);
	}

	public static void setCurrentContext(Object object) {
		ERXThreadStorage.takeValueForKey(object, CONTEXT_KEY);
	}

	protected static NSMutableDictionary _contextDictionary() {
		NSMutableDictionary contextDictionary = (NSMutableDictionary) ERXThreadStorage.valueForKey(ERXWOContext.CONTEXT_DICTIONARY_KEY);
		return contextDictionary;
	}

	public ERXWOContext(WORequest worequest) {
		super(worequest);
	}

	/**
	 * Implemented so the the thread checks if it should get interrupted.
	 * 
	 * @param eoadaptorchannel
	 * @param nsmutabledictionary
	 */
	@Override
	public void _setCurrentComponent(WOComponent wocomponent) {
		ERXRuntimeUtilities.checkThreadInterrupt();
		super._setCurrentComponent(wocomponent);
	}

	@Override
	public void _generateCompleteURLs() {
		super._generateCompleteURLs();
		_generateCompleteURLs = true;
	}

	@Override
	public void _generateRelativeURLs() {
		super._generateRelativeURLs();
		_generateCompleteURLs = false;
	}

	public boolean _generatingCompleteURLs() {
		return _generateCompleteURLs;
	}

	public static WOContext newContext() {
		WOApplication app = WOApplication.application();
		return app.createContextForRequest(app.createRequest("GET", app.cgiAdaptorURL() + "/" + app.name(), "HTTP/1.1", null, null, null));
	}

	public NSMutableDictionary mutableUserInfo() {
		return contextDictionary();
	}

	public void setMutableUserInfo(NSMutableDictionary userInfo) {
		ERXThreadStorage.takeValueForKey(userInfo, ERXWOContext.CONTEXT_DICTIONARY_KEY);
	}

	public NSDictionary userInfo() {
		return mutableUserInfo();
	}

	@Override
	public String _urlWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString, boolean secure) {
		String url = super._urlWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString, secure);
		if (!ERXApplication.isWO54()) {
			url = ERXApplication.erxApplication()._rewriteURL(url);
		}
		return url;
	}

	/**
	 * Returns a complete URL for the specified action. Works like
	 * {@link WOContext#directActionURLForActionNamed} but has one extra
	 * parameter to specify whether or not to include the current Session ID
	 * (wosid) in the URL. Convenient if you embed the link for the direct
	 * action into an email message and don't want to keep the Session ID in it.
	 * <p>
	 * <code>actionName</code> can be either an action -- "ActionName" -- or
	 * an action on a class -- "ActionClass/ActionName". You can also specify
	 * <code>queryDict</code> to be an NSDictionary which contains form values
	 * as key/value pairs. <code>includeSessionID</code> indicates if you want
	 * to include the Session ID (wosid) in the URL.
	 * 
	 * @param actionName
	 *            String action name
	 * @param queryDict
	 *            NSDictionary containing query key/value pairs
	 * @param includeSessionID
	 *            true: to include the Session ID (if has one), <br>
	 *            false: not to include the Session ID
	 * @return a String containing the URL for the specified action
	 * @see WODirectAction
	 */
	public String directActionURLForActionNamed(String actionName, NSDictionary queryDict, boolean includeSessionID) {
		String url = super.directActionURLForActionNamed(actionName, queryDict);
		if (!includeSessionID) {
			url = stripSessionIDFromURL(url);
		}
		return url;
	}

	/**
	 * Removes Session ID (wosid) query key/value pair from the given URL
	 * string.
	 * 
	 * @param url
	 *            String URL
	 * @return a String with the Session ID removed
	 */
	public static String stripSessionIDFromURL(String url) {
		if (url == null)
			return null;
		int len = 1;
		int startpos = url.indexOf("?wosid");
		if (startpos < 0) {
			startpos = url.indexOf("&wosid");
		}
		if (startpos < 0) {
			startpos = url.indexOf("&amp;wosid");
			len = 5;
		}

		if (startpos >= 0) {
			int endpos = url.indexOf('&', startpos + len);
			if (endpos < 0)
				url = url.substring(0, startpos);
			else {
				int endLen = len;
				if (len == 1 && url.indexOf("&amp;") >= 0) {
					endLen = 5;
				}
				url = url.substring(0, startpos + len) + url.substring(endpos + endLen);
			}
		}
		return url;
	}

	/**
	 * Debugging help, returns the path to current component.
	 * 
	 * @param context
	 */
	public static NSArray<String> componentPath(WOContext context) {
		NSMutableArray<String> result = new NSMutableArray<String>();
		if (context != null) {
			WOComponent component = context.component();
			while (component != null) {
				if (component.name() != null) {
					result.insertObjectAtIndex(component.name(), 0);
				}
				component = component.parent();
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static String _htmlCloseHeadTag() {
		return ERXResponseRewriter._htmlCloseHeadTag(); 
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static void insertInResponseBeforeTag(WOContext context, WOResponse response, String content, String tag, TagMissingBehavior tagMissingBehavior) {
		ERXResponseRewriter.insertInResponseBeforeTag(response, context, content, tag, tagMissingBehavior);
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static void addScriptResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		ERXResponseRewriter.addScriptResourceInHead(response, context, framework, fileName);
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static void addStylesheetResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
		ERXResponseRewriter.addStylesheetResourceInHead(response, context, framework, fileName);
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static void addScriptCodeInHead(WOContext context, WOResponse response, String script) {
		ERXResponseRewriter.addScriptCodeInHead(response, context, script);
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static void addScriptCodeInHead(WOContext context, WOResponse response, String script, String scriptName) {
		ERXResponseRewriter.addScriptCodeInHead(response, context, script, scriptName);
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static void addResourceInHead(WOContext context, WOResponse response, String framework, String fileName, String startTag, String endTag) {
		ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, startTag, endTag);
	}
	
	/**
	 * 
	 * @deprecated replaced by ERXResponseRewriter
	 */
	public static void addResourceInHead(WOContext context, WOResponse response, String framework, String fileName, String startTag, String endTag, TagMissingBehavior tagMissingBehavior) {
		ERXResponseRewriter.addResourceInHead(response, context, framework, fileName, startTag, endTag, tagMissingBehavior);
	}

	private static final String SAFE_IDENTIFIER_NAME_KEY = "ERXWOContext.safeIdentifierName";
	/**
	 * Returns a safe identifier for the current component.  If willCache is true, your
	 * component should cache the identifier name so that it does not change.  In this case,
	 * your component will be given an incrementing counter value that is unique on the 
	 * current page.  If willCache is false (because you cannot cache the value), the 
	 * identifier returned will be based on the context.elementID().  While unique on the
	 * page at any point in time, be aware that structural changes to the page can 
	 * cause the elementID of your component to change. 
	 * 
	 * @param context the WOContext
	 * @param willCache if true, you should cache the resulting value in your component
	 * @return a safe identifier name
	 */
	public static String safeIdentifierName(WOContext context, boolean willCache) {
		String safeIdentifierName;
		if (willCache) {
			NSMutableDictionary<String, Object> pageUserInfo = ERXResponseRewriter.pageUserInfo(context);
			Integer counter = (Integer) pageUserInfo.objectForKey(ERXWOContext.SAFE_IDENTIFIER_NAME_KEY);
			if (counter == null) {
				counter = Integer.valueOf(0);
			}
			else {
				counter = Integer.valueOf(counter.intValue() + 1);
			}
			pageUserInfo.setObjectForKey(counter, ERXWOContext.SAFE_IDENTIFIER_NAME_KEY);
			safeIdentifierName = ERXStringUtilities.safeIdentifierName(counter.toString());
		}
		else {
			safeIdentifierName = ERXStringUtilities.safeIdentifierName("e_" + context.elementID());	
		}
		return safeIdentifierName;
	}
	
	/**
	 * Returns a javascript-safe version of the given element ID.
	 * 
	 * @see ERXStringUtilities#safeIdentifierName(String, String, char)
	 * @param elementID
	 *            the element ID
	 * @return a javascript-safe version (i.e. "_1_2_3_10")
	 * @deprecated for ERXStringUtilities.safeIdentifierName(String)
	 */
	public static String toSafeElementID(String elementID) {
		return ERXStringUtilities.safeIdentifierName(elementID);
	}

	/**
	 * Call this anywhere you would have called _directActionURL in 5.3 if you
	 * want to be 5.4 compatible.
	 * 
	 * @param context
	 *            the WOContext to operate on
	 * @param actionName
	 *            the name of the direct action to lookup
	 * @param queryParams
	 *            the query parameters to use
	 * @param secure
	 *            whether or not the URL should be HTTPS
	 * @return the URL to the given direct action
	 */
	public static String _directActionURL(WOContext context, String actionName, NSDictionary queryParams, boolean secure) {
		try {
			String directActionURL;
			if (ERXApplication.isWO54()) {
				Method _directActionURLMethod = context.getClass().getMethod("_directActionURL", new Class[] { String.class, NSDictionary.class, boolean.class, int.class, boolean.class });
				directActionURL = (String) _directActionURLMethod.invoke(context, new Object[] { actionName, queryParams, Boolean.valueOf(secure), new Integer(0), Boolean.FALSE });
			}
			else {
				Method _directActionURLMethod = context.getClass().getMethod("_directActionURL", new Class[] { String.class, NSDictionary.class, boolean.class });
				directActionURL = (String) _directActionURLMethod.invoke(context, new Object[] { actionName, queryParams, Boolean.valueOf(secure) });
			}
			return directActionURL;
		}
		catch (Exception e) {
			throw new NSForwardException(e);
		}
	}

	/**
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param directActionName
	 *            the direct action name
	 * @param secure
	 *            true = https, false = http, null = same as request
	 * @param includeSessionID
	 *            if false, removes wosid from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String directActionName, Boolean secure, boolean includeSessionID) {
		return ERXWOContext.directActionUrl(context, null, null, null, directActionName, null, secure, includeSessionID);
	}

	/**
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param directActionName
	 *            the direct action name
	 * @param key
	 *            the query parameter key to add (or null to skip)
	 * @param value
	 *            the query parameter value to add (or null to skip)
	 * @param secure
	 *            true = https, false = http, null = same as request
	 * @param includeSessionID
	 *            if false, removes wosid from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String directActionName, String key, String value, Boolean secure, boolean includeSessionID) {
		return ERXWOContext.directActionUrl(context, null, null, null, directActionName, key, value, secure, includeSessionID);
	}

	/**
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param directActionName
	 *            the direct action name
	 * @param queryParameters
	 *            the query parameters to append (or null)
	 * @param secure
	 *            true = https, false = http, null = same as request
	 * @param includeSessionID
	 *            if false, removes wosid from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String directActionName, NSDictionary<String, ? extends Object> queryParameters, Boolean secure, boolean includeSessionID) {
		return ERXWOContext.directActionUrl(context, null, null, null, directActionName, queryParameters, secure, includeSessionID);
	}

	/**
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param host
	 *            the host name for the URL (or null for default)
	 * @param port
	 *            the port number of the URL (or null for default)
	 * @param path
	 *            the custom path prefix (or null for none)
	 * @param directActionName
	 *            the direct action name
	 * @param key
	 *            the query parameter key to add (or null to skip)
	 * @param value
	 *            the query parameter value to add (or null to skip)
	 * @param secure
	 *            true = https, false = http, null = same as request
	 * @param includeSessionID
	 *            if false, removes wosid from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String host, Integer port, String path, String directActionName, String key, Object value, Boolean secure, boolean includeSessionID) {
		NSDictionary<String, ? extends Object> queryParameters = null;
		if (key != null && value != null) {
			queryParameters = new NSDictionary<String, Object>(value, key);
		}
		return ERXWOContext.directActionUrl(context, host, port, path, directActionName, queryParameters, secure, includeSessionID);
	}

	/**
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param host
	 *            the host name for the URL (or null for default)
	 * @param port
	 *            the port number of the URL (or null for default)
	 * @param path
	 *            the custom path prefix (or null for none)
	 * @param directActionName
	 *            the direct action name
	 * @param queryParameters
	 *            the query parameters to append (or null)
	 * @param secure
	 *            true = https, false = http, null = same as request
	 * @param includeSessionID
	 *            if false, removes wosid from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String host, Integer port, String path, String directActionName, NSDictionary<String, ? extends Object> queryParameters, Boolean secure, boolean includeSessionID) {
		boolean completeUrls;

		boolean currentlySecure = ERXRequest.isRequestSecure(context.request());
		boolean secureBool = (secure == null) ? currentlySecure : secure.booleanValue();

		if (host == null && currentlySecure == secureBool && port == null) {
			completeUrls = true;
		}
		else if (context instanceof ERXWOContext) {
			completeUrls = ((ERXWOContext) context)._generatingCompleteURLs();
		}
		else {
			completeUrls = false;
		}

		if (!completeUrls) {
			context._generateCompleteURLs();
		}

		String url;
		try {
			ERXMutableURL mu = new ERXMutableURL();
			boolean customPath = (path != null && path.length() > 0);
			if (!customPath) {
				mu.setURL(ERXWOContext._directActionURL(context, directActionName, queryParameters, secureBool));
				if (!includeSessionID) {
					mu.removeQueryParameter("wosid");
				}
			}
			else {
				if (secureBool) {
					mu.setProtocol("https");
				}
				else {
					mu.setProtocol("http");
				}
				mu.setHost(context.request()._serverName());
				mu.setPath(path + directActionName);
				mu.setQueryParameters(queryParameters);
				if (includeSessionID && context.session().storesIDsInURLs()) {
					mu.setQueryParameter("wosid", context.session().sessionID());
				}
			}

			if (port != null) {
				mu.setPort(port);
			}

			if (host != null && host.length() > 0) {
				mu.setHost(host);
				if (mu.protocol() == null) {
					if (secureBool) {
						mu.setProtocol("https");
					}
					else {
						mu.setProtocol("http");
					}
				}
			}

			url = mu.toExternalForm();
		}
		catch (MalformedURLException e) {
			throw new RuntimeException("Failed to create url for direct action '" + directActionName + "'.", e);
		}
		finally {
			if (!completeUrls) {
				context._generateRelativeURLs();
			}
		}
		return url;
	}
	
	public String safeElementID() {
		return ERXStringUtilities.safeIdentifierName(elementID());
	}
	
}
