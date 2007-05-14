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
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.ERXEC;

public class ERXRestRequestHandler extends WORequestHandler {
	public static final Logger log = Logger.getLogger(ERXRestRequestHandler.class);

	private IERXRestAuthenticationDelegate _authenticationDelegate;
	private IERXRestDelegate _delegate;

	public ERXRestRequestHandler(IERXRestAuthenticationDelegate authenticationDelegate, IERXRestDelegate delegate) {
		_authenticationDelegate = authenticationDelegate;
		_delegate = delegate;
	}

	public WOResponse handleRequest(WORequest request) {
		WOApplication application = WOApplication.application();
		WOContext woContext = application.createContextForRequest(request);
		WOResponse response = application.createResponseInContext(woContext);

		String uri = request._uriDecomposed().requestHandlerPath();
		int dotIndex = uri.lastIndexOf('.');
		String type = "xml";
		if (dotIndex >= 0) {
			type = uri.substring(dotIndex + 1);
			uri = uri.substring(0, dotIndex);
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

				ERXRestResult initialResult = new ERXRestResult(uri);
				String method = request.method();
				if ("GET".equalsIgnoreCase(method)) {
					ERXRestResult restResult = initialResult.lastResult(restContext);

					ERXRestWriter restHandler = new ERXRestWriter();
					restHandler.appendToResponse(restContext, response, restResult.entity(), restResult.value(), type);
					editingContext.saveChanges();
				}
				else if ("DELETE".equalsIgnoreCase(method)) {
					ERXRestResult restResult = initialResult.lastResult(restContext);
					_delegate.delete(restResult.entity(), restResult.value(), restContext);
					editingContext.saveChanges();
				}
				else if ("PUT".equalsIgnoreCase(method)) {
					String contentStr = request.contentString();
					Document updateDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contentStr)));
					updateDocument.normalize();

					ERXRestResult nextToLastResult = initialResult.nextToLastResult(restContext);
					_delegate.update(nextToLastResult, updateDocument, restContext);
					editingContext.saveChanges();
				}
				else if ("POST".equalsIgnoreCase(method)) {
					String contentStr = request.contentString();
					Document insertDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(contentStr)));
					insertDocument.normalize();
					ERXRestResult restResult = initialResult.nextToLastResult(restContext);

					ERXRestResult nextToLastResult = initialResult.nextToLastResult(restContext);
					ERXRestResult insertedResult = _delegate.insert(nextToLastResult, insertDocument, restContext);
					editingContext.saveChanges();

					ERXRestWriter restHandler = new ERXRestWriter();
					restHandler.appendToResponse(restContext, response, insertedResult.entity(), insertedResult.value(), type);
				}
			}
			finally {
				editingContext.unlock();
			}
		}
		catch (ERXRestNotFoundException e) {
			response.setStatus(404);
			response.setContent(e.getMessage());
			ERXRestRequestHandler.log.error("Request failed.", e);
		}
		catch (ERXRestSecurityException e) {
			response.setStatus(403);
			response.setContent(e.getMessage());
			ERXRestRequestHandler.log.error("Request failed.", e);
		}
		catch (Exception e) {
			response.setStatus(500);
			response.setContent(e.getMessage());
			ERXRestRequestHandler.log.error("Request failed.", e);
		}
		finally {
			WOApplication.application().saveSessionForContext(woContext);
		}

		return response;
	}
}
