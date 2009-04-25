package er.rest.routes;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

public abstract class ERXDefaultRouteController extends ERXRouteController {
	public ERXDefaultRouteController(WORequest request) {
		super(request);
	}
	
	public abstract WOActionResults newAction() throws Exception;

	public abstract WOActionResults updateAction() throws Exception;

	public abstract WOActionResults destroyAction() throws Exception;

	public abstract WOActionResults showAction();

	public abstract WOActionResults createAction() throws Exception;

	public abstract WOActionResults indexAction() throws Exception;
}
