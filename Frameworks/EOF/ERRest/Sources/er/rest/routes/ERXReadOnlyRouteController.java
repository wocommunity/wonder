package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

/**
 * Just like ERXDefaultRouteController but only defines read-only methods.
 * 
 * @author mschrag
 */
public abstract class ERXReadOnlyRouteController extends ERXRouteController {
	public ERXReadOnlyRouteController(WORequest request) {
		super(request);
	}
	
	/**
	 * Called when you want to view a single instance.
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults showAction() throws Throwable;

	/**
	 * Called when you want to list instances.
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults indexAction() throws Throwable;
}
