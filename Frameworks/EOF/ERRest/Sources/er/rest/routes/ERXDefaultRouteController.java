package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

/**
 * ERXDefaultRouteController defines abstract methods for the standard default routes that are registered.
 * 
 * @author mschrag
 */
public abstract class ERXDefaultRouteController extends ERXRouteController {
	/**
	 * Constructs a new default route controller.
	 * 
	 * @param request the current request
	 */
	public ERXDefaultRouteController(WORequest request) {
		super(request);
	}

	/**
	 * Called when you need an uncommitted blank new instance.
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults newAction() throws Throwable;

	/**
	 * Called when you want to update an instance (or instances).
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults updateAction() throws Throwable;

	/**
	 * Called when you want to delete an instance.
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults destroyAction() throws Throwable;

	/**
	 * Called when you want to view a single instance.
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults showAction() throws Throwable;

	/**
	 * Called when you want to commit a new instance.
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults createAction() throws Throwable;

	/**
	 * Called when you want to list instances.
	 * 
	 * @return the response
	 * @throws Throwable if something goes wrong
	 */
	public abstract WOActionResults indexAction() throws Throwable;
}
