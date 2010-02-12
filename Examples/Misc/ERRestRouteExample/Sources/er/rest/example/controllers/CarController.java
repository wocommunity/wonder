package er.rest.example.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.rest.example.model.Car;
import er.rest.example.model.Manufacturer;
import er.rest.routes.ERXRouteController;
import er.rest.routes.jsr311.PathParam;

/**
 * CarController is the controller for Cars, which is a non-EO class. This works
 * because there is a CarRestDelegate class that is magically loaded by name. See the Application constructor
 * for more details.
 * 
 * @author mschrag
 */
public class CarController extends ERXRouteController {
	public CarController(WORequest request) {
		super(request);
	}

	protected ERXKeyFilter showFilter() {
		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
		filter.include(new ERXKey<Manufacturer>("manufacturer"));
		return filter;
	}

	public WOActionResults indexAction() throws Throwable {
		return response(Car.cars(), showFilter());
	}

	/**
	 * showAction is using the JSR-311 annotation @PathParam, which will automatically
	 * have the "car" variable passed in as the parameter (from the URL of the form /Car/{car}). The 
	 * variable names are determined by your routes. The default routes add a bunch of common forms for
	 * you. See ERXRouteRequestHandler for more information on which default routes are added.
	 * 
	 * @param car
	 * @return
	 * @throws Throwable
	 */
	public WOActionResults showAction(@PathParam("car") Car car) throws Throwable {
		return response(car, showFilter());
	}
}
