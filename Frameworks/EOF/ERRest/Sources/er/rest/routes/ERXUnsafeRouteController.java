package er.rest.routes;

import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * If you just want to quickly drop in a controller to test your entities, you can use or extend
 * ERXUnsafeRouteController. This provides a default read/write interface to the attributes and to-one relationships of
 * the specified entity.
 * 
 * @author mschrag
 * 
 * @param <T>
 *            the type of your entity
 */
public class ERXUnsafeRouteController<T extends EOEnterpriseObject> extends ERXUnsafeReadOnlyRouteController<T> {
	public ERXUnsafeRouteController(WORequest request) {
		super(request);
	}

	@Override
	protected boolean allowUpdates() {
		return true;
	}
}
