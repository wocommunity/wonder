package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXHttpStatusCodes;
import er.extensions.foundation.ERXProperties;

/**
 * ERXMissingRouteController is the controller that is used when no route can be found. It's "missing" action is loaded.
 * 
 * @property ERXRest.strictMode (default "true") If set to true, status code in the response will be 405 Not Allowed, if set to false, status code will be 404 Not Found
 *
 * @author mschrag
 */
public class ERXMissingRouteController extends ERXRouteController {
	/**
	 * Constructs a new ERXMissingRouteController.
	 * 
	 * @param request
	 *            the current request
	 */
	public ERXMissingRouteController(WORequest request) {
		super(request);
	}
	
	@Override
	protected String entityName() {
		return "Unknown";
	}

	/**
	 * Returns an error response of 404 or an error response of 405 if strict mode is set to true
	 * 
	 * @return an error response of 404 or 405
	 */
	public WOActionResults missingAction() {
	   boolean isStrictMode = ERXProperties.booleanForKeyWithDefault("ERXRest.strictMode", true);
	    if (isStrictMode) {
	      return errorResponse(ERXHttpStatusCodes.METHOD_NOT_ALLOWED);
	    } else {
	      return errorResponse("There is no route for the path '" + request()._uriDecomposed().requestHandlerPath() + "'.", WOMessage.HTTP_STATUS_NOT_FOUND);
	    }
	}
}
