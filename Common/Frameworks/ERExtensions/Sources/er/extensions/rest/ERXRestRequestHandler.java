package er.extensions.rest;

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
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXApplication;
import er.extensions.ERXEC;

/**
 * Don't use this yet.
 * 
 * @author mschrag
 */
public class ERXRestRequestHandler extends WORequestHandler {
	public static final Logger log = Logger.getLogger(ERXRestRequestHandler.class);

	private IERXRestAuthenticationDelegate _authenticationDelegate;
	private IERXRestDelegate _delegate;
	private IERXRestResponseWriter _defaultResponseWriter;
	private NSMutableDictionary _entityResponseWriter;

	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate) {
		this(authenticationDelegate, delegate, new ERXXmlRestResponseWriter());
	}

	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate, IERXRestResponseWriter defaultResponseWriter) {
		_authenticationDelegate = authenticationDelegate;
		_delegate = delegate;
		_entityResponseWriter = new NSMutableDictionary();
		_defaultResponseWriter = defaultResponseWriter;
		if (!ERXApplication.erxApplication().isDevelopmentMode()) {
			throw new RuntimeException("You don't want to use this unless you're in development mode.");
		}
	}

	public void setResponseWriterForEntity(IERXRestResponseWriter responseWriter, EOEntity entity) {
		_entityResponseWriter.setObjectForKey(responseWriter, entity);
	}

	public void removeResponseWriterForEntity(EOEntity entity) {
		_entityResponseWriter.removeObjectForKey(entity);
	}

	protected IERXRestResponseWriter responseWriterForEntity(EOEntity entity) {
		IERXRestResponseWriter responseWriter = (IERXRestResponseWriter) _entityResponseWriter.objectForKey(entity);
		if (responseWriter == null) {
			responseWriter = _defaultResponseWriter;
		}
		return responseWriter;
	}

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
			EOEditingContext editingContext = ERXEC.newEditingContext();
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
					IERXRestResponseWriter restResponseWriter = responseWriterForEntity(requestKey.entity());
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

					IERXRestResponseWriter restResponseWriter = responseWriterForEntity(responseKey.entity());
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
}
