package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

public abstract class ERXDefaultRouteController extends ERXRouteController {
	public ERXDefaultRouteController(WORequest request) {
		super(request);
	}
	
	public abstract WOActionResults newAction();

	public abstract WOActionResults updateAction();

	public abstract WOActionResults destroyAction();

	public abstract WOActionResults showAction();

	public abstract WOActionResults createAction();

	public abstract WOActionResults indexAction();
}
