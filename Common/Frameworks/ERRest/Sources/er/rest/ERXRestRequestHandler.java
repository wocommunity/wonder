package er.rest;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXApplication;
import er.extensions.ERXEC;

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
 * <h2>Unsafe Development Setup Example</h2>
 * <p>
 * To use the unsafe development example delegates, you can add the following code to your application constructor:
 * 
 * <pre>
 * registerRequestHandler(ERXRestRequestHandler.createUnsafeRequestHandler(), &quot;rest&quot;);
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
 * curl -x PUT -d '&lt;Bulletin&gt;&lt;title&gt;Some random Bulletin!&lt;/title&gt;&lt;/Bulletin&gt;' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins/201.xml?membershipTicket=someAuthToken
 * </pre>
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 * 
 * <h2>Try to break it -- Update a site with a bulletin document</h2>
 * 
 * <pre>
 * curl -X PUT -d '&lt;Bulletin&gt;&lt;title&gt;Some random Bulletin Again!&lt;/title&gt;&lt;/Bulletin&gt;' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100.xml?membershipTicket=someAuthToken
 * </pre>
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
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 * 
 * <h2>Post a bulletin</h2>
 * 
 * <pre>
 * curl -X POST -d '&lt;Bulletin&gt;&lt;title&gt;New Bulletin By Me&lt;/title&gt;&lt;contents&gt;This is the contents of my bulletin&lt;/contents&gt;&lt;/Bulletin&gt;' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins.xml?membershipTicket=someAuthToken
 * </pre>
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
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 * 
 * @author mschrag
 */
public class ERXRestRequestHandler extends WORequestHandler {
	public static final Logger log = Logger.getLogger(ERXRestRequestHandler.class);

	private IERXRestAuthenticationDelegate _authenticationDelegate;
	private IERXRestDelegate _delegate;
	private IERXRestResponseWriter _defaultResponseWriter;
	private IERXRestRequestParser _defaultRequestParser;
	private NSMutableDictionary _responseWriters;
	private NSMutableDictionary _requestParsers;

	/**
	 * Construct an ERXRestRequestHandler with a default response writer of ERXXmlRestResponseWriter.
	 * 
	 * @param authenticationDelegate
	 *            the authentication delegate
	 * @param delegate
	 *            the rest delegate
	 */
	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate) {
		this(authenticationDelegate, delegate, new ERXXmlRestResponseWriter(), new ERXXmlRestRequestParser());
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
	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate, IERXRestResponseWriter defaultResponseWriter, IERXRestRequestParser defaultRequestParser) {
		_authenticationDelegate = authenticationDelegate;
		_delegate = delegate;
		_responseWriters = new NSMutableDictionary();
		_requestParsers = new NSMutableDictionary();
		_defaultResponseWriter = defaultResponseWriter;
		_defaultRequestParser = defaultRequestParser;
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
		IERXRestResponseWriter responseWriter = (IERXRestResponseWriter) _responseWriters.objectForKey(type);
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
	public void setRequestParserForType(IERXRestRequestParser requestParser, String type) {
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
	protected IERXRestRequestParser requestParserForType(String type) {
		IERXRestRequestParser requestParser = (IERXRestRequestParser) _requestParsers.objectForKey(type);
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

		if (woContext._requestSessionID() != null) {
			WOApplication.application().restoreSessionWithID(wosid, woContext);
		}
		try {
			EOEditingContext editingContext = newEditingContext();
			ERXRestContext restContext = new ERXRestContext(woContext, editingContext);
			restContext.setDelegate(_delegate);
			editingContext.lock();
			try {
				if (!_authenticationDelegate.authenticate(restContext)) {
					throw new ERXRestSecurityException("Authenticated failed.");
				}

				IERXRestRequestParser requestParser = requestParserForType(type);
				ERXRestRequest restRequest = requestParser.parseRestRequest(restContext, request, path);
				String method = request.method();
				if ("GET".equalsIgnoreCase(method)) {
					IERXRestResponseWriter restResponseWriter = responseWriterForType(type);
					restResponseWriter.appendToResponse(restContext, response, restRequest.key());
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
					restResponseWriter.appendToResponse(restContext, response, responseKey);
					response.setStatus(201);
				}
			}
			finally {
				editingContext.unlock();
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
			WOApplication.application().saveSessionForContext(woContext);
		}

		return response;
	}

	/**
	 * Creates an unsafe request handler for you to try out in development mode. THIS SHOULD NOT BE DEPLOYED (and in
	 * fact it will throw an exception if development mode is false).
	 * 
	 * @param readOnly if true, the unsafe read-only delegate will be used
	 * @param displayToMany if true, to-many relationships will display by default
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
		ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate(defaultEntityDelegate);
		IERXRestAuthenticationDelegate authenticationDelegate = new ERXUnsafeRestAuthenticationDelegate();
		IERXRestResponseWriter responseWriter = new ERXXmlRestResponseWriter(true, displayToMany);
		IERXRestRequestParser requestParser = new ERXXmlRestRequestParser();
		return new ERXRestRequestHandler(authenticationDelegate, restDelegate, responseWriter, requestParser);
	}
}
