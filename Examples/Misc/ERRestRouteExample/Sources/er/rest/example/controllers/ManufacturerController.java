package er.rest.example.controllers;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.eof.ERXKeyFilter;
import er.rest.example.model.Manufacturer;
import er.rest.routes.ERXRouteController;
import er.rest.routes.jsr311.PathParam;

/**
 * Manufacturer is a non-EO class and ManufacturerController just shows a simple example of exposing it. This works
 * because there is a ManufacturerRestDelegate class that is magically loaded by name. See the Application constructor
 * for more details.
 * 
 * @author mschrag
 */
public class ManufacturerController extends ERXRouteController {
	public ManufacturerController(WORequest request) {
		super(request);
	}

	public WOActionResults indexAction() throws Throwable {
		return response(Manufacturer.manufacturers(), ERXKeyFilter.filterWithAttributes());
	}

	public WOActionResults showAction(@PathParam("manufacturer") Manufacturer manufacturer) throws Throwable {
		return response(manufacturer, ERXKeyFilter.filterWithAttributes());
	}
}
