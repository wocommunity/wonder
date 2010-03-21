package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;

/**
 * ERXMissingRouteController is the controller that is used when no route can be found. It's "missing" action is loaded.
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

	/**
	 * Returns an error response of 404.
	 * 
	 * @return an error response of 404
	 */
	public WOActionResults missingAction() {
		return errorResponse("There is no route for the path '" + request()._uriDecomposed().requestHandlerPath() + "'.", WOMessage.HTTP_STATUS_NOT_FOUND);
	}
}
