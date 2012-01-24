package er.rest.entityDelegates;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;
import er.rest.ERXRestRequestNode;
import er.rest.format.ERXRestFormatDelegate;
import er.rest.format.ERXWORestRequest;
import er.rest.format.ERXWORestResponse;
import er.rest.format.ERXXmlRestParser;
import er.rest.format.IERXRestParser;

/**
 * <p>
 * ERXRestRequestHandler is the entry point for REST requests. It provides support for registering an authentication
 * delegate, a processing delegate, and a per-entity response writer.
 * </p>
 * 
 * <p>
 * If you just want to play around with REST, you can use the "unsafe" delegates. These delegates ARE NOT suitable for
 * deployment (and in fact will not function in deployment). They are a major security hole, but exist just to provide
 * wide-open access to developers who are interested in exploring the features of the ERRest framework.
 * </p>
 * 
 * <p>
 * Please note that the stock Apache adaptor shipped with WebObjects 5.3 is not capable of handling the PUT and 
 * DELETE HTTP methods, you will have to use the adaptor from WebObjects 5.4 or the one from Project Wonder.
 * </p>
 * 
 * <h2>Unsafe Development Setup Example</h2>
 * <p>
 * To use the unsafe development example delegates, you can add the following code to your application constructor:
 * 
 * <pre>
 * registerRequestHandler(ERXRestRequestHandler.createUnsafeRequestHandler(true, false), &quot;rest&quot;);
 * </pre>
 * 
 * </p>
 * 
 * <h2>Real-world Setup Example</h2>
 * <p>
 * In a real scenario you will not want to use the unsafe variants of the various delegates. Instead, you will want to
 * provide custom implementations.
 * 
 * <pre>
 * ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate();
 * restDelegate.addDelegateForEntityNamed(new CompanyRestEntityDelegate(), Company.ENTITY_NAME);
 * restDelegate.addDelegateForEntityNamed(new PersonRestEntityDelegate(), Person.ENTITY_NAME);
 * IERXRestAuthenticationDelegate authenticationDelegate = new MyCustomRestAuthenticationDelegate();
 * IERXRestResponseWriter responseWriter = new ERXXmlRestResponseWriter();
 * registerRequestHandler(new ERXRestRequestHandler(authenticationDelegate, restDelegate), &quot;rest&quot;);
 * </pre>
 * 
 * <p>
 * Once you have the request handler registered, you can explore the rest interface using an HTTP client like 'curl'.
 * Note that the examples below will not work on your own application unless you provide entities with the same design.
 * </p>
 * 
 * <h2>Get a list of all the Site objects</h2>
 * 
 * <pre>
 * curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 200
 * &lt;Sites type = &quot;Site&quot;&gt;
 *   &lt;Site id = &quot;100&quot;&gt;
 *     &lt;title&gt;Site #1&lt;/title&gt;
 *     &lt;bulletins type = &quot;Bulletin&quot;&gt;
 *       &lt;Bulletin id = &quot;200&quot;/&gt;
 *       &lt;Bulletin id = &quot;201&quot;/&gt;
 *       &lt;Bulletin id = &quot;202&quot;/&gt;
 *     &lt;/bulletins&gt;
 *   &lt;/Site&gt;
 *   &lt;Site id = &quot;101&quot;&gt;
 *     &lt;title&gt;Site #2&lt;/title&gt;
 *     &lt;bulletins type = &quot;Bulletin&quot;&gt;
 *       &lt;Bulletin id = &quot;215&quot;/&gt;
 *       &lt;Bulletin id = &quot;230&quot;/&gt;
 *       &lt;Bulletin id = &quot;243&quot;/&gt;
 *     &lt;/bulletins&gt;
 *   &lt;/Site&gt;
 * &lt;/Sites&gt;
 * </pre>
 * 
 * <h2>Get a single site from the Site list</h2>
 * 
 * <pre>
 * curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 200
 * &lt;Site id = &quot;100&quot;&gt;
 *   &lt;title&gt;Site #1&lt;/title&gt;
 *   &lt;bulletins type = &quot;Bulletin&quot;&gt;
 *     &lt;Bulletin id = &quot;200&quot;/&gt;
 *     &lt;Bulletin id = &quot;201&quot;/&gt;
 *     &lt;Bulletin id = &quot;202&quot;/&gt;
 *   &lt;/bulletins&gt;
 * &lt;/Site&gt;
 * </pre>
 * 
 * <h2>Get a single site from the Site list (that doesn't exist)</h2>
 * 
 * <pre>
 * curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/112.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 404
 * There is no Site with the id '112'.
 * </pre>
 * 
 * <h2>Get a single site from the Site list (that we can't see)</h2>
 * 
 * <pre>
 * curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/114.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 403
 * You are not allowed to view the Site with the id '112'.
 * </pre>
 * 
 * <h2>Get a list of the bulletins for that Site</h2>
 * 
 * <pre>
 * curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 200
 * &lt;Bulletins type = &quot;Bulletin&quot;&gt;
 *   &lt;Bulletin id = &quot;200&quot;&gt;
 *     &lt;author type = &quot;Person&quot; id = &quot;500&quot;/&gt;
 *     &lt;title&gt;Bulletin 1&lt;/title&gt;
 *     &lt;contents&gt;Bulletin 1 Contents&lt;/title&gt;
 *   &lt;/Bulletin&gt;
 *   &lt;Bulletin id = &quot;201&quot;&gt;
 *     &lt;author type = &quot;Person&quot; id = &quot;600&quot;/&gt;
 *     &lt;title&gt;Bulletin 2&lt;/title&gt;
 *     &lt;contents&gt;Bulletin 2 Contents&lt;/title&gt;
 *   &lt;/Bulletin&gt;
 *   &lt;Bulletin id = &quot;202&quot;&gt;
 *     &lt;author type = &quot;Person&quot; id = &quot;700&quot;/&gt;
 *     &lt;title&gt;Bulletin 3&lt;/title&gt;
 *     &lt;contents&gt;Bulletin 3 Contents&lt;/title&gt;
 *   &lt;/Bulletin&gt;
 * &lt;/Bulletins&gt;
 * </pre>
 * 
 * <h2>Get a single bulletin from the Bulletin list</h2>
 * 
 * <pre>
 * curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins/201.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 200
 * &lt;Bulletin id = &quot;201&quot;&gt;
 *   &lt;author type = &quot;Person&quot; id = &quot;600&quot;/&gt;
 *   &lt;title&gt;Bulletin 2&lt;/title&gt;
 *   &lt;contents&gt;Bulletin 2 Contents&lt;/title&gt;
 * &lt;/Bulletin&gt;
 * </pre>
 * 
 * <h2>Update the title of a bulletin</h2>
 * 
 * <pre>
 * curl -X PUT -d '&lt;Bulletin&gt;&lt;title&gt;Some random Bulletin!&lt;/title&gt;&lt;/Bulletin&gt;' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins/201.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 * 
 * <h2>Try to break it -- Update a site with a bulletin document</h2>
 * 
 * <pre>
 * curl -X PUT -d '&lt;Bulletin&gt;&lt;title&gt;Some random Bulletin Again!&lt;/title&gt;&lt;/Bulletin&gt;' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 403
 * You tried to put a Bulletin into a Site.
 * </pre>
 * 
 * <h2>Update the title of a site</h2>
 * 
 * <pre>
 * curl -X PUT -d '&lt;Site&gt;&lt;title&gt;My Personal Site!&lt;/title&gt;&lt;/Site&gt;' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 * 
 * <h2>Post a bulletin</h2>
 * 
 * <pre>
 * curl -X POST -d '&lt;Bulletin&gt;&lt;title&gt;New Bulletin By Me&lt;/title&gt;&lt;contents&gt;This is the contents of my bulletin&lt;/contents&gt;&lt;/Bulletin&gt;' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 201
 * &lt;Bulletin id = &quot;7324&quot;&gt;
 *   &lt;title&gt;New Bulletin By Me&lt;/title&gt;
 *   &lt;contents&gt;This is the contents of my bulletin&lt;/contents&gt;
 * &lt;/Bulletin&gt;
 * </pre>
 * 
 * <h2>Delete a bulletin</h2>
 * 
 * <pre>
 * curl -X DELETE -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins/7324.xml?membershipTicket=someAuthToken
 * </pre>
 * 
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 * 
 * <h2>Using it Programmatically</h2> If you want to process the results of a REST call to a remote server on your local
 * system (i.e. simple EO syncing), you can execute something like:
 * 
 * <pre>
 * EOEditingContext editingContext = ERXEC.newEditingContext();
 * 
 * ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate();
 * restDelegate.addDelegateForEntityNamed(new ERXUnsafeRestEntityDelegate(true), Manufacturer.ENTITY_NAME);
 * ERXRestContext restContext = new ERXRestContext(context(), editingContext, restDelegate);
 * 
 * String contentStr = ERXFileUtilities.stringFromURL(new URL(&quot;http://someserver/rest/Manufacturer.xml&quot;));
 * ERXRestRequest restRequest = new ERXXmlRestRequestParser().parseRestRequest(restContext, contentStr, &quot;Manufacturer&quot;);
 * ERXRestKey restResponse = restDelegate.process(restRequest, restContext);
 * 
 * editingContext.saveChanges();
 * </pre>
 * 
 * This assumes your PK match across systems, which means you should probably be using UUID PK's for syncing unless it's
 * only one-way and read-only on the client.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXRestRequestHandler extends WORequestHandler {
	public static final Logger log = Logger.getLogger(ERXRestRequestHandler.class);

	public static final String Key = "rest";

	private IERXRestAuthenticationDelegate _authenticationDelegate;
	private IERXRestDelegate _delegate;
	private IERXRestResponseWriter _defaultResponseWriter;
	private IERXRestParser _defaultRequestParser;
	private NSMutableDictionary<String, IERXRestResponseWriter> _responseWriters;
	private NSMutableDictionary<String, IERXRestParser> _requestParsers;

	/**
	 * Construct an ERXRestRequestHandler with a default response writer of ERXXmlRestResponseWriter.
	 * 
	 * @param authenticationDelegate
	 *            the authentication delegate
	 * @param delegate
	 *            the rest delegate
	 */
	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate) {
		this(authenticationDelegate, delegate, new ERXXmlRestResponseWriter(), new ERXXmlRestParser());
	}

	/**
	 * Construct an ERXRestRequestHandler.
	 * 
	 * @param authenticationDelegate
	 *            the authentication delegate
	 * @param delegate
	 *            the rest delegate
	 * @param defaultResponseWriter
	 *            the default response writer to use
	 * @param defaultRequestParser
	 *            the default request parser to use
	 */
	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate, IERXRestResponseWriter defaultResponseWriter, IERXRestParser defaultRequestParser) {
		_authenticationDelegate = authenticationDelegate;
		_delegate = delegate;
		_responseWriters = new NSMutableDictionary<String, IERXRestResponseWriter>();
		_requestParsers = new NSMutableDictionary<String, IERXRestParser>();
		_defaultResponseWriter = defaultResponseWriter;
		_defaultRequestParser = defaultRequestParser;
	}

	public IERXRestDelegate delegate() {
		return _delegate;
	}

	/**
	 * Sets the response writer for the request type.
	 * 
	 * @param responseWriter
	 *            the response writer to use
	 * @param type
	 *            the type of request ("xml", "json", etc)
	 */
	public void setResponseWriterForType(IERXRestResponseWriter responseWriter, String type) {
		_responseWriters.setObjectForKey(responseWriter, type);
	}

	/**
	 * Removes the response writer for the given entity.
	 * 
	 * @param type
	 *            the type to disassociate
	 */
	public void removeResponseWriterForType(String type) {
		_responseWriters.removeObjectForKey(type);
	}

	/**
	 * Returns the response writer for the given entity name.
	 * 
	 * @param type
	 *            the type of request ("xml", "json", etc)
	 * @return the response writer to use
	 */
	protected IERXRestResponseWriter responseWriterForType(String type) {
		IERXRestResponseWriter responseWriter = _responseWriters.objectForKey(type);
		if (responseWriter == null) {
			responseWriter = _defaultResponseWriter;
		}
		return responseWriter;
	}

	/**
	 * Sets the request parser for the request type.
	 * 
	 * @param requestParser
	 *            the request parser to use
	 * @param type
	 *            the type of request ("xml", "json", etc)
	 */
	public void setRequestParserForType(IERXRestParser requestParser, String type) {
		_requestParsers.setObjectForKey(requestParser, type);
	}

	/**
	 * Removes the request parser for the given entity.
	 * 
	 * @param type
	 *            the type to disassociate
	 */
	public void removeRequestParserForType(String type) {
		_requestParsers.removeObjectForKey(type);
	}

	/**
	 * Returns the request parser for the given entity name.
	 * 
	 * @param type
	 *            the type of request ("xml", "json", etc)
	 * @return the request parser to use
	 */
	protected IERXRestParser requestParserForType(String type) {
		IERXRestParser requestParser = _requestParsers.objectForKey(type);
		if (requestParser == null) {
			requestParser = _defaultRequestParser;
		}
		return requestParser;
	}

	/**
	 * Returns a new editing context. If you want to override how an editing context is created, extend
	 * ERXRestRequestHandler and override this method.
	 * 
	 * @return a new editing context
	 */
	protected EOEditingContext newEditingContext() {
		return ERXEC.newEditingContext();
	}

	/**
	 * Handle the incoming REST request. REST requests can have session ids associated with them as cookies or wosid
	 * query string parameters. Right now rendering type is not supported, but ultimately the file extension of the
	 * request will determine which renderer is used to render the response.
	 * 
	 * @param request
	 *            the request
	 */
	@Override
	public WOResponse handleRequest(WORequest request) {

		WOApplication application = WOApplication.application();
		WOContext woContext = application.createContextForRequest(request);
		WOResponse response = application.createResponseInContext(woContext);

		String path = request._uriDecomposed().requestHandlerPath();
		int dotIndex = path.lastIndexOf('.');
		String type = "xml";
		if (dotIndex >= 0) {
			type = path.substring(dotIndex + 1);
			path = path.substring(0, dotIndex);
		}

		String wosid = null;
		// if (wosid == null) {
		wosid = request.cookieValueForKey("wosid");
		// }
		woContext._setRequestSessionID(wosid);

		WOSession session = null;
		if (woContext._requestSessionID() != null) {
			session = WOApplication.application().restoreSessionWithID(wosid, woContext);
		}
		if (session != null) {
			session.awake();
		}
		try {
			if (woContext._session() != null) {
				WOSession contextSession = woContext._session();
				// If this is a new session, then we have to force it to be a cookie session
				if (wosid == null) {
					boolean storesIDsInCookies = contextSession.storesIDsInCookies();
					try {
						contextSession.setStoresIDsInCookies(true);
						contextSession._appendCookieToResponse(response);
					}
					finally {
						contextSession.setStoresIDsInCookies(storesIDsInCookies);
					}
				}
				else {
					contextSession._appendCookieToResponse(response);
				}
			}

			EOEditingContext editingContext = newEditingContext();
			ERXRestContext restContext = new ERXRestContext(woContext, editingContext, _delegate);
			editingContext.lock();
			try {
				if (!_authenticationDelegate.authenticate(restContext)) {
					throw new ERXRestSecurityException("Authenticated failed.");
				}

				IERXRestParser requestParser = requestParserForType(type);
				ERXWORestRequest woRestRequest = new ERXWORestRequest(request);
				ERXRestRequestNode rootNode = requestParser.parseRestRequest(woRestRequest, new ERXRestFormatDelegate(), new er.rest.ERXRestContext(editingContext));
				ERXRestRequest restRequest = new ERXRestRequest(restContext, rootNode, path);
				String method = request.method();
				if ("GET".equalsIgnoreCase(method)) {
					ERXRestKey responseKey = _delegate.view(restRequest, restContext);

					IERXRestResponseWriter restResponseWriter = responseWriterForType(type);
					restResponseWriter.appendToResponse(restContext, new ERXWORestResponse(response), responseKey);
					editingContext.saveChanges();
				}
				else if ("DELETE".equalsIgnoreCase(method)) {
					_delegate.delete(restRequest, restContext);
					editingContext.saveChanges();
				}
				else if ("PUT".equalsIgnoreCase(method)) {
					_delegate.update(restRequest, restContext);
					editingContext.saveChanges();
				}
				else if ("POST".equalsIgnoreCase(method)) {
					ERXRestKey responseKey = _delegate.insert(restRequest, restContext);
					editingContext.saveChanges();

					IERXRestResponseWriter restResponseWriter = responseWriterForType(type);
					restResponseWriter.appendToResponse(restContext, new ERXWORestResponse(response), responseKey);
					response.setStatus(201);
				}
			}
			finally {
				editingContext.unlock();
			}

			if (response != null) {
				response._finalizeInContext(woContext);
				response.disableClientCaching();
			}
		}
		catch (ERXRestNotFoundException e) {
			response.setStatus(404);
			response.setContent(e.getMessage() + "\n");
			ERXRestRequestHandler.log.error("Request failed.", e);
		}
		catch (ERXRestSecurityException e) {
			response.setStatus(403);
			response.setContent(e.getMessage() + "\n");
			ERXRestRequestHandler.log.error("Request failed.", e);
		}
		catch (Exception e) {
			response.setStatus(500);
			response.setContent(e.getMessage() + "\n");
			ERXRestRequestHandler.log.error("Request failed.", e);
		}
		finally {
			try {
				if (session != null) {
					session.sleep();
				}
			}
			finally {
				if (woContext._session() != null) {
					WOApplication.application().saveSessionForContext(woContext);
				}
			}
		}

		return response;
	}

	/**
	 * Creates an unsafe request handler for you to try out in development mode. THIS SHOULD NOT BE DEPLOYED (and in
	 * fact it will throw an exception if development mode is false).
	 * 
	 * @param readOnly
	 *            if true, the unsafe read-only delegate will be used
	 * @param displayToMany
	 *            if true, to-many relationships will display by default
	 * @return an unsafe request handler
	 */
	public static final ERXRestRequestHandler createUnsafeRequestHandler(boolean readOnly, boolean displayToMany) {
		if (!ERXApplication.isDevelopmentModeSafe()) {
			throw new RuntimeException("You attempted to create an unsafe request handler when you were not in development mode!");
		}
		IERXRestEntityDelegate defaultEntityDelegate;
		if (readOnly) {
			defaultEntityDelegate = new ERXUnsafeReadOnlyRestEntityDelegate();
		}
		else {
			defaultEntityDelegate = new ERXUnsafeRestEntityDelegate();
		}
		ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate(defaultEntityDelegate, true);
		IERXRestAuthenticationDelegate authenticationDelegate = new ERXUnsafeRestAuthenticationDelegate();
		// IERXRestResponseWriter responseWriter = new ERXJSONRestResponseWriter(true, displayToMany); // DON'T COMMIT
		// YET
		IERXRestResponseWriter responseWriter = new ERXXmlRestResponseWriter(true, displayToMany);
		IERXRestParser requestParser = new ERXXmlRestParser();
		return new ERXRestRequestHandler(authenticationDelegate, restDelegate, responseWriter, requestParser);
	}

	/**
	 * Registers an ERXRestRequestHandler with the WOApplication for the handler key "rest".
	 * 
	 * @param requestHandler
	 *            the rest request handler to register
	 */
	public static void register(ERXRestRequestHandler requestHandler) {
		WOApplication.application().registerRequestHandler(requestHandler, ERXRestRequestHandler.Key);
	}

	/**
	 * Registers an ERXRestRequestHandler with the WOApplication for the handler key "rest".
	 * 
	 * @param authenticationDelegate
	 *            the authentication delegate
	 * @param delegate
	 *            the rest delegate
	 */
	public static void register(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate) {
		ERXRestRequestHandler requestHandler = new ERXRestRequestHandler(authenticationDelegate, delegate);
		requestHandler.setResponseWriterForType(new ERXJSONRestResponseWriter(), "json");
		requestHandler.setResponseWriterForType(new ERXPListRestResponseWriter(), "plist");
		requestHandler.setResponseWriterForType(new ERXXmlRestResponseWriter(), "xml");
		ERXRestRequestHandler.register(requestHandler);
	}

	/**
	 * Registers an ERXRestRequestHandler with the WOApplication for the handler key "rest" and an
	 * ERXDefaultRestDelegate.
	 * 
	 * @param authenticationDelegate
	 *            the authentication delegate
	 */
	public static ERXDefaultRestDelegate register(IERXRestAuthenticationDelegate authenticationDelegate) {
		ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate();
		ERXRestRequestHandler.register(authenticationDelegate, restDelegate);
		return restDelegate;
	}

	/**
	 * Registers an ERXRestRequestHandler with the WOApplication for the handler key "rest" using an
	 * ERXDefaultRestDelegate, ERXXmlRestResponseWriter, and ERXXmlRestRequestParser.
	 * 
	 * @param authenticationDelegate
	 *            the authentication delegate
	 * @param displayAllProperties
	 *            if true, by default all properties are eligible to be displayed (probably should only be true in
	 *            development, but it won't really hurt anything). Note that entity delegates will still control
	 *            permissions on the properties, it just defaults to checking all of them.
	 * @param displayAllToMany
	 *            if true, all to-many relationships will be displayed
	 */
	public static final ERXRestRequestHandler register(IERXRestAuthenticationDelegate authenticationDelegate, boolean displayAllProperties, boolean displayAllToMany) {
		ERXRestRequestHandler requestHandler = new ERXRestRequestHandler(authenticationDelegate, new ERXDefaultRestDelegate(), new ERXXmlRestResponseWriter(displayAllProperties, displayAllToMany), new ERXXmlRestParser());
		requestHandler.setResponseWriterForType(new ERXJSONRestResponseWriter(displayAllProperties, displayAllToMany), "json");
		requestHandler.setResponseWriterForType(new ERXPListRestResponseWriter(displayAllProperties, displayAllToMany), "plist");
		requestHandler.setResponseWriterForType(new ERXXmlRestResponseWriter(displayAllProperties, displayAllToMany), "xml");
		ERXRestRequestHandler.register(requestHandler);
		return requestHandler;
	}
}
