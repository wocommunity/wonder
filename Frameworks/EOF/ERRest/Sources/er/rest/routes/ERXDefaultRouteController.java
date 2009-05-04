package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

public abstract class ERXDefaultRouteController extends ERXRouteController {
	public ERXDefaultRouteController(WORequest request) {
		super(request);
	}
	
	public abstract WOActionResults newAction() throws Throwable;

	public abstract WOActionResults updateAction() throws Throwable;

	public abstract WOActionResults destroyAction() throws Throwable;

	public abstract WOActionResults showAction() throws Throwable;

	public abstract WOActionResults createAction() throws Throwable;

	public abstract WOActionResults indexAction() throws Throwable;
}
