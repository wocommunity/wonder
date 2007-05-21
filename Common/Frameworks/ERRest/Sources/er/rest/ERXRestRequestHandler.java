package er.rest;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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
 * ERXRestRequestHandler is the entry point for REST requests.  It
 * provides support for registering an authentication delegate, a 
 * processing delegate, and a per-entity response writer.
 * </p>
 * 
 * <p>
 * If you just want to play around with REST, you can use the "unsafe" 
 * delegates.  These delegates ARE NOT suitable for deployment (and in
 * fact will not function in deployment).  They are a major security 
 * hole, but exist just to provide wide-open access to developers who 
 * are interested in exploring the features of the ERRest framework.
 * </p>
 * 
 * <h2>Unsafe Development Setup Example</h2>
 * <p>
 * To use the unsafe development example delegates, you can add
 * the following code to your application constructor:
 * 
 * <pre>
 * registerRequestHandler(ERXRestRequestHandler.createUnsafeRequestHandler(), "rest");
 * </pre>
 * </p>
 * 
 * <h2>Real-world Setup Example</h2>
 * <p>
 * In a real scenario you will not want to use the unsafe variants of the various 
 * delegates.  Instead, you will want to provide custom implementations.
 * 
 * <pre>
 * ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate();
 * restDelegate.addDelegateForEntityNamed(new CompanyRestEntityDelegate(), Company.ENTITY_NAME);
 * restDelegate.addDelegateForEntityNamed(new PersonRestEntityDelegate(), Person.ENTITY_NAME);
 * IERXRestAuthenticationDelegate authenticationDelegate = new MyCustomRestAuthenticationDelegate();
 * IERXRestResponseWriter responseWriter = new ERXXmlRestResponseWriter();
 * registerRequestHandler(new ERXRestRequestHandler(authenticationDelegate, restDelegate), "rest");
 * </pre>
 * 
 * <p>
 * Once you have the request handler registered, you can explore the rest interface using
 * an HTTP client like 'curl'.  Note that the examples below will not work on your own
 * application unless you provide entities with the same design. 
 * </p>
 * 
 * <h2>Get a list of all the Site objects</h2>
 * <pre>curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 200
 * &lt;Sites type = "Site"&gt;
 *   &lt;Site id = "100"&gt;
 *     &lt;title&gt;Site #1&lt;/title&gt;
 *     &lt;bulletins type = "Bulletin"&gt;
 *       &lt;Bulletin id = "200"/&gt;
 *       &lt;Bulletin id = "201"/&gt;
 *       &lt;Bulletin id = "202"/&gt;
 *     &lt;/bulletins&gt;
 *   &lt;/Site&gt;
 *   &lt;Site id = "101"&gt;
 *     &lt;title&gt;Site #2&lt;/title&gt;
 *     &lt;bulletins type = "Bulletin"&gt;
 *       &lt;Bulletin id = "215"/&gt;
 *       &lt;Bulletin id = "230"/&gt;
 *       &lt;Bulletin id = "243"/&gt;
 *     &lt;/bulletins&gt;
 *   &lt;/Site&gt;
 * &lt;/Sites&gt;  
 * </pre>
 *   
 * <h2>Get a single site from the Site list</h2>
 * <pre>curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 200
 * &lt;Site id = "100"&gt;
 *   &lt;title&gt;Site #1&lt;/title&gt;
 *   &lt;bulletins type = "Bulletin"&gt;
 *     &lt;Bulletin id = "200"/&gt;
 *     &lt;Bulletin id = "201"/&gt;
 *     &lt;Bulletin id = "202"/&gt;
 *   &lt;/bulletins&gt;
 * &lt;/Site&gt;
 * </pre>
 *   
 * <h2>Get a single site from the Site list (that doesn't exist)</h2>
 * <pre>curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/112.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 404
 * There is no Site with the id '112'.
 * </pre>
 *   
 * <h2>Get a single site from the Site list (that we can't see)</h2>
 * <pre>curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/114.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 403
 * You are not allowed to view the Site with the id '112'.
 * </pre>
 *   
 * <h2>Get a list of the bulletins for that Site</h2>
 * <pre>curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 200
 * &lt;Bulletins type = "Bulletin"&gt;
 *   &lt;Bulletin id = "200"&gt;
 *     &lt;author type = "Person" id = "500"/&gt;
 *     &lt;title&gt;Bulletin 1&lt;/title&gt;
 *     &lt;contents&gt;Bulletin 1 Contents&lt;/title&gt;
 *   &lt;/Bulletin&gt;
 *   &lt;Bulletin id = "201"&gt;
 *     &lt;author type = "Person" id = "600"/&gt;
 *     &lt;title&gt;Bulletin 2&lt;/title&gt;
 *     &lt;contents&gt;Bulletin 2 Contents&lt;/title&gt;
 *   &lt;/Bulletin&gt;
 *   &lt;Bulletin id = "202"&gt;
 *     &lt;author type = "Person" id = "700"/&gt;
 *     &lt;title&gt;Bulletin 3&lt;/title&gt;
 *     &lt;contents&gt;Bulletin 3 Contents&lt;/title&gt;
 *   &lt;/Bulletin&gt;
 * &lt;/Bulletins&gt;
 * </pre>
 *   
 * <h2>Get a single bulletin from the Bulletin list</h2>
 * <pre>curl -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins/201.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 200
 * &lt;Bulletin id = "201"&gt;
 *   &lt;author type = "Person" id = "600"/&gt;
 *   &lt;title&gt;Bulletin 2&lt;/title&gt;
 *   &lt;contents&gt;Bulletin 2 Contents&lt;/title&gt;
 * &lt;/Bulletin&gt;
 * </pre>
 *   
 * <h2>Update the title of a bulletin</h2>
 * <pre>curl -x PUT -d '<Bulletin><title>Some random Bulletin!</title></Bulletin>' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins/201.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 *   
 * <h2>Try to break it -- Update a site with a bulletin document</h2>
 * <pre>curl -X PUT -d '<Bulletin><title>Some random Bulletin Again!</title></Bulletin>' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 403
 * You tried to put a Bulletin into a Site.
 * </pre>
 *   
 * <h2>Update the title of a site</h2>
 * <pre>curl -X PUT -d '<Site><title>My Personal Site!</title></Site>' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 200
 * </pre>
 *   
 * <h2>Post a bulletin</h2>
 * <pre>curl -X POST -d '<Bulletin><title>New Bulletin By Me</title><contents>This is the contents of my bulletin</contents></Bulletin>' -s http://127.0.0.1/cgi-bin/WebObjects/YourApp.woa/rest/Site/100/bulletins.xml?membershipTicket=someAuthToken</pre>
 * <pre>
 * HTTP Status Code: 201
 * &lt;Bulletin id = "7324"&gt;
 *   &lt;title&gt;New Bulletin By Me&lt;/title&gt;
 *   &lt;contents&gt;This is the contents of my bulletin&lt;/contents&gt;
 * &lt;/Bulletin&gt;
 * </pre>
 *  
 * @author mschrag
 */
public class ERXRestRequestHandler extends WORequestHandler {
	public static final Logger log = Logger.getLogger(ERXRestRequestHandler.class);

	private IERXRestAuthenticationDelegate _authenticationDelegate;
	private IERXRestDelegate _delegate;
	private IERXRestResponseWriter _defaultResponseWriter;
	private NSMutableDictionary _entityResponseWriter;

	/**
	 * Construct an ERXRestRequestHandler with a default response writer of ERXXmlRestResponseWriter.
	 * 
	 * @param authenticationDelegate the authentication delegate
	 * @param delegate the rest delegate
	 */
	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate) {
		this(authenticationDelegate, delegate, new ERXXmlRestResponseWriter());
	}

	/**
	 * Construct an ERXRestRequestHandler.
	 * 
	 * @param authenticationDelegate the authentication delegate
	 * @param delegate the rest delegate
	 * @param defaultResponseWriter the default response writer to use
	 */
	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate, IERXRestResponseWriter defaultResponseWriter) {
		_authenticationDelegate = authenticationDelegate;
		_delegate = delegate;
		_entityResponseWriter = new NSMutableDictionary();
		_defaultResponseWriter = defaultResponseWriter;
	}

	/**
	 * Sets the response writer for the given entity.
	 * 
	 * @param responseWriter the response writer to use
	 * @param entityName the entity name to associate it with
	 */
	public void setResponseWriterForEntityNamed(IERXRestResponseWriter responseWriter, String entityName) {
		_entityResponseWriter.setObjectForKey(responseWriter, entityName);
	}

	/**
	 * Removes the response writer for the given entity.
	 * 
	 * @param entityName the entity name to disassociate
	 */
	public void removeResponseWriterForEntityNamed(String entityName) {
		_entityResponseWriter.removeObjectForKey(entityName);
	}

	/**
	 * Returns the response writer for the given entity name.
	 * 
	 * @param entityName the entity name to lookup
	 * @return the response writer to use
	 */
	protected IERXRestResponseWriter responseWriterForEntityNamed(String entityName) {
		IERXRestResponseWriter responseWriter = (IERXRestResponseWriter) _entityResponseWriter.objectForKey(entityName);
		if (responseWriter == null) {
			responseWriter = _defaultResponseWriter;
		}
		return responseWriter;
	}

	/**
	 * Returns a new editing context.  If you want to override how 
	 * an editing context is created, extend ERXRestRequestHandler
	 * and override this method.
	 * 
	 * @return a new editing context
	 */
	protected EOEditingContext newEditingContext() {
		return ERXEC.newEditingContext();
	}

	/**
	 * Handle the incoming REST request.  REST requests can have
	 * session ids associated with them as cookies or wosid 
	 * query string parameters.  Right now rendering type is not
	 * supported, but ultimately the file extension of the request
	 * will determine which renderer is used to render the
	 * response.
	 * 
	 * @param request the request
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
		if (wosid == null) {
			wosid = request.cookieValueForKey("wosid");
		}
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

				ERXRestKey requestKey = ERXRestKey.parse(restContext, path);
				String method = request.method();
				if ("GET".equalsIgnoreCase(method)) {
					IERXRestResponseWriter restResponseWriter = responseWriterForEntityNamed(requestKey.entity().name());
					restResponseWriter.appendToResponse(restContext, response, requestKey);
					editingContext.saveChanges();
				}
				else if ("DELETE".equalsIgnoreCase(method)) {
					if (requestKey.isKeyAll()) {
						throw new ERXRestException("You are not allowed to delete all the objects for any entity.");
					}
					_delegate.delete(requestKey.entity(), requestKey.value(), restContext);
					editingContext.saveChanges();
				}
				else if ("PUT".equalsIgnoreCase(method)) {
					String contentStr = request.contentString();
					Document updateDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contentStr)));
					updateDocument.normalize();

					_delegate.update(requestKey, updateDocument, restContext);
					editingContext.saveChanges();
				}
				else if ("POST".equalsIgnoreCase(method)) {
					String contentStr = request.contentString();
					Document insertDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contentStr)));
					insertDocument.normalize();

					ERXRestKey responseKey = _delegate.insert(requestKey, insertDocument, restContext);
					editingContext.saveChanges();

					IERXRestResponseWriter restResponseWriter = responseWriterForEntityNamed(responseKey.entity().name());
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
	 * Creates an unsafe request handler for you to try out in development mode.  THIS SHOULD NOT
	 * BE DEPLOYED (and in fact it will throw an exception if development mode is false).
	 * 
	 * @return an unsafe request handler
	 */
	public static final ERXRestRequestHandler createUnsafeRequestHandler() {
		if (!ERXApplication.erxApplication().isDevelopmentMode()) {
			throw new RuntimeException("You attempted to create an unsafe request handler when you were not in development mode!");
		}
        IERXRestEntityDelegate defaultEntityDelegate = new ERXUnsafeRestEntityDelegate();
        ERXDefaultRestDelegate restDelegate = new ERXDefaultRestDelegate(defaultEntityDelegate);
        IERXRestAuthenticationDelegate authenticationDelegate = new ERXUnsafeRestAuthenticationDelegate();
        IERXRestResponseWriter responseWriter = new ERXXmlRestResponseWriter(true);
        return new ERXRestRequestHandler(authenticationDelegate, restDelegate, responseWriter);
	}
}
