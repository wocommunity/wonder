package er.extensions.appserver;

import java.net.MalformedURLException;
import java.net.URL;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.appserver.ajax.ERXAjaxContext;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXMutableUserInfoHolderInterface;
import er.extensions.foundation.ERXProperties;
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
	private boolean _generateCompleteResourceURLs;
	
	private static final boolean IS_DEV = ERXApplication.isDevelopmentModeSafe();

	public static final String CONTEXT_KEY = "wocontext";
	public static final String CONTEXT_DICTIONARY_KEY = "ERXWOContext.dict";

	public static class Observer {
		public void applicationDidHandleRequest(NSNotification n) {
			ERXWOContext.setCurrentContext(null);
			ERXThreadStorage.removeValueForKey(ERXWOContext.CONTEXT_DICTIONARY_KEY);
		}
	}

	/**
	 * Returns the existing session if any is given in the form values or URL.
	 * 
	 * @return session for this request or <code>null</code>
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
	 * Implemented so that the thread checks if it should get interrupted.
	 * 
	 * @param component the current component
	 */
	@Override
	public void _setCurrentComponent(WOComponent component) {
		ERXRuntimeUtilities.checkThreadInterrupt();
		super._setCurrentComponent(component);
	}
	
	@Override
	public Object clone() {
		ERXWOContext context = (ERXWOContext)super.clone();
		context._setGenerateCompleteResourceURLs(_generateCompleteResourceURLs);
		return context;
	}
	
	/**
	 * Turn on complete resource URL generation.
	 * 
	 * @param generateCompleteResourceURLs if true, resources will generate complete URLs.
	 */
	public void _setGenerateCompleteResourceURLs(boolean generateCompleteResourceURLs) {
		_generateCompleteResourceURLs = generateCompleteResourceURLs;
	}

	/**
	 * Returns whether or not resources generate complete URLs.
	 * 
	 * @return whether or not resources generate complete URLs
	 */
	public boolean _generatingCompleteResourceURLs() {
		return _generateCompleteResourceURLs;
	}
	
	@Override
	public void generateCompleteURLs() {
		super.generateCompleteURLs();
		_generateCompleteURLs = true; 
	}

	@Override
	public void generateRelativeURLs() {
		super.generateRelativeURLs();
		_generateCompleteURLs = false;
	}
	
	@Override
	public boolean doesGenerateCompleteURLs() {
		return _generateCompleteURLs;
	}

	/**
	 * Creates a WOContext using a dummy WORequest.
	 * @return the new WOContext
	 */
	public static ERXWOContext newContext() {
		ERXApplication app = ERXApplication.erxApplication();
		// Try to create a URL with a relative path into the application to mimic a real request.
		// We must create a request with a relative URL, as using an absolute URL makes the new 
		// WOContext's URL absolute, and it is then unable to render relative paths. (Long story short.)
		//
		// Note: If you configured the adaptor's WebObjectsAlias to something other than the default, 
		// make sure to also set your WOAdaptorURL property to match.  Otherwise, asking the new context 
		// the path to a direct action or component action URL will give an incorrect result.
		String requestUrl = app.cgiAdaptorURL() + "/" + app.name() + app.applicationExtension();
		try {
			URL url = new URL(requestUrl);
			requestUrl = url.getPath(); // Get just the part of the URL that is relative to the server root.
		} catch (MalformedURLException mue) {
			// The above should never fail.  As a last resort, using the empty string will 
			// look funny in the request, but still allow the context to use a relative url.
			requestUrl = "";
		}
		WORequest dummyRequest = app.createRequest("GET", requestUrl, "HTTP/1.1", null, null, null);
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.ERXApplication.publicHostIsSecure", false)) {
			dummyRequest.setHeader("on", "https");
		}
		return (ERXWOContext) app.createContextForRequest(dummyRequest);
	}
	
	public <T extends ERXRequest> T erxRequest() {
		return (T) request();
	}

	public NSMutableDictionary mutableUserInfo() {
		return contextDictionary();
	}

	public void setMutableUserInfo(NSMutableDictionary userInfo) {
		ERXThreadStorage.takeValueForKey(userInfo, ERXWOContext.CONTEXT_DICTIONARY_KEY);
	}

	@Override
	public NSDictionary userInfo() {
		return mutableUserInfo();
	}

	/**
	 * If er.extensions.ERXWOContext.forceRemoveApplicationNumber is true, then always remove the 
	 * application number from the generated URLs.  You have to be aware of how your app is written
	 * to know if this is something you can do without causing problems.  For instance, you MUST be
	 * using cookies, and you must not use WOImages with data bindings -- anything that requires a 
	 * per-instance cache has the potential to fail when this is enabled (if you have more than
	 * one instance of your app deployed). 
	 */
	protected void _preprocessURL() {
		if (ERXProperties.booleanForKey("er.extensions.ERXWOContext.forceRemoveApplicationNumber")) {
			_url().setApplicationNumber(null);
		}
	}

	protected String _postprocessURL(String url) {
		if (WOApplication.application() instanceof ERXApplication) {
			return ERXApplication.erxApplication()._rewriteURL(url);
		}
		return url;
	}
	
	@Override
	public String _urlWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString, boolean secure) {
		_preprocessURL();
		return super._urlWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString, secure);
	}
	
	@Override
	public String _urlWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString, boolean isSecure, int somePort) {
		_preprocessURL();
		String url = super._urlWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString, isSecure, somePort);
		url = _postprocessURL(url);
		return url;
	}

	/**
	 * Returns a complete URL for the specified action. Works like
	 * {@link WOContext#directActionURLForActionNamed} but has one extra
	 * parameter to specify whether or not to include the current session ID
	 * in the URL. Convenient if you embed the link for the direct
	 * action into an email message and don't want to keep the session ID in it.
	 * <p>
	 * <code>actionName</code> can be either an action -- "ActionName" -- or
	 * an action on a class -- "ActionClass/ActionName". You can also specify
	 * <code>queryDict</code> to be an NSDictionary which contains form values
	 * as key/value pairs. <code>includeSessionID</code> indicates if you want
	 * to include the session ID in the URL.
	 * 
	 * @param actionName
	 *            String action name
	 * @param queryDict
	 *            NSDictionary containing query key/value pairs
	 * @param includeSessionID
	 *            <code>true</code>: to include the session ID (if has one), <br>
	 *            <code>false</code>: not to include the session ID
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
	 * Removes session ID query key/value pair from the given URL
	 * string.
	 * 
	 * @param url
	 *            String URL
	 * @return a String with the session ID removed
	 */
	public static String stripSessionIDFromURL(String url) {
		if (url == null)
			return null;
		String sessionIdKey = WOApplication.application().sessionIdKey();
		int len = 1;
		int startpos = url.indexOf("?" + sessionIdKey);
		if (startpos < 0) {
			startpos = url.indexOf("&" + sessionIdKey);
		}
		if (startpos < 0) {
			startpos = url.indexOf("&amp;" + sessionIdKey);
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
	 * Debugging help, returns the path to current component as a list of component names.
	 * 
	 * @param context the current context
	 * @return an array of component names
	 */
	public static NSArray<String> componentPath(WOContext context) {
		NSMutableArray<String> result = new NSMutableArray<>();
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
	 * Debugging help, returns the path to current component as WOComponent objects.
	 * 
	 * @param context the current context
	 * @return an array of components
	 */
	public static NSArray<WOComponent> _componentPath(WOContext context) {
		NSMutableArray<WOComponent> result = new NSMutableArray<>();
		if (context != null) {
			WOComponent component = context.component();
			while (component != null) {
				if (component.name() != null) {
					result.insertObjectAtIndex(component, 0);
				}
				component = component.parent();
			}
		}
		return result;
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
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param directActionName
	 *            the direct action name
	 * @param secure
	 *            <code>true</code> = https, <code>false</code> = http, <code>null</code> = same as request
	 * @param includeSessionID
	 *            if <code>false</code>, removes session ID from query parameters
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
	 *            the query parameter key to add (or <code>null</code> to skip)
	 * @param value
	 *            the query parameter value to add (or <code>null</code> to skip)
	 * @param secure
	 *            <code>true</code> = https, <code>false</code> = http, <code>null</code> = same as request
	 * @param includeSessionID
	 *            if <code>false</code>, removes session ID from query parameters
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
	 *            the query parameters to append (or <code>null</code>)
	 * @param secure
	 *            <code>true</code> = https, <code>false</code> = http, <code>null</code> = same as request
	 * @param includeSessionID
	 *            if <code>false</code>, removes session ID from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String directActionName, NSDictionary<String, Object> queryParameters, Boolean secure, boolean includeSessionID) {
		return ERXWOContext.directActionUrl(context, null, null, null, directActionName, queryParameters, secure, includeSessionID);
	}

	/**
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param host
	 *            the host name for the URL (or <code>null</code> for default)
	 * @param port
	 *            the port number of the URL (or <code>null</code> for default)
	 * @param path
	 *            the custom path prefix (or <code>null</code> for none)
	 * @param directActionName
	 *            the direct action name
	 * @param key
	 *            the query parameter key to add (or <code>null</code> to skip)
	 * @param value
	 *            the query parameter value to add (or <code>null</code> to skip)
	 * @param secure
	 *            <code>true</code> = https, <code>false</code> = http, <code>null</code> = same as request
	 * @param includeSessionID
	 *            if <code>false</code>, removes session ID from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String host, Integer port, String path, String directActionName, String key, Object value, Boolean secure, boolean includeSessionID) {
		NSDictionary<String, Object> queryParameters = null;
		if (key != null && value != null) {
			queryParameters = new NSDictionary<>(value, key);
		}
		return ERXWOContext.directActionUrl(context, host, port, path, directActionName, queryParameters, secure, includeSessionID);
	}

	/**
	 * Generates direct action URLs with support for various overrides.
	 * 
	 * @param context
	 *            the context to generate the URL within
	 * @param host
	 *            the host name for the URL (or <code>null</code> for default)
	 * @param port
	 *            the port number of the URL (or <code>null</code> for default)
	 * @param path
	 *            the custom path prefix (or <code>null</code> for none)
	 * @param directActionName
	 *            the direct action name
	 * @param queryParameters
	 *            the query parameters to append (or <code>null</code>)
	 * @param secure
	 *            <code>true</code> = https, <code>false</code> = http, <code>null</code> = same as request
	 * @param includeSessionID
	 *            if <code>false</code>, removes session ID from query parameters
	 * @return the constructed direct action URL
	 */
	public static String directActionUrl(WOContext context, String host, Integer port, String path, String directActionName, NSDictionary<String, Object> queryParameters, Boolean secure, boolean includeSessionID) {
		boolean completeUrls;

		boolean currentlySecure = ERXRequest.isRequestSecure(context.request());
		boolean secureBool = (secure == null) ? currentlySecure : secure.booleanValue();

		if (host == null && currentlySecure == secureBool && port == null) {
			completeUrls = true;
		}
		else {
			completeUrls = context.doesGenerateCompleteURLs();
		}

		if (!completeUrls) {
			context.generateCompleteURLs();
		}

		String url;
		try {
			ERXMutableURL mu = new ERXMutableURL();
			boolean customPath = (path != null && path.length() > 0);
			if (!customPath) {
				mu.setURL(context._directActionURL(directActionName, queryParameters, secureBool, 0, false));
				if (!includeSessionID) {
					mu.removeQueryParameter(WOApplication.application().sessionIdKey());
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
					mu.setQueryParameter(WOApplication.application().sessionIdKey(), context.session().sessionID());
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
				context.generateRelativeURLs();
			}
		}
		return url;
	}
	
	public String safeElementID() {
		return ERXStringUtilities.safeIdentifierName(elementID());
	}

	@Override
	protected String relativeURLWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString) {
		String result = super.relativeURLWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString);
		if(IS_DEV && !WOApplication.application().isDirectConnectEnabled()) {
			String extension = "." + WOApplication.application().applicationExtension();
			String replace = extension + "/-" + WOApplication.application().port();
			if(!result.contains(replace) && result.contains(extension)) {
				result = result.replace(extension, replace);
			}
		}
		return result;
	}
}
