package er.rest.example;

import er.extensions.appserver.ERXApplication;
import er.rest.ERXRestNameRegistry;
import er.rest.example.controllers.CarController;
import er.rest.example.controllers.ManufacturerController;
import er.rest.example.controllers.PersonController;
import er.rest.example.model.Animal;
import er.rest.example.model.Company;
import er.rest.example.model.Person;
import er.rest.routes.ERXRoute;
import er.rest.routes.ERXRouteRequestHandler;

/**
 * This sets up a simple ERRest application with three entities -- Company, Person, and Animal. Note that this example app does not demonstrate any authentication checks. Authentication with rest can be achieved through any of the normal approaches --
 * basic auth, token.
 * 
 * @author mschrag
 */
public class Application extends ERXApplication {
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		// They entity is "Animal" internally, but we want to name it "Pet" to the rest of the world
		ERXRestNameRegistry.registry().setExternalNameForInternalName("Pet", "Animal");

		// Register a route request handler and use the WO URL naming conventions (capitalized entity names, singular form, camel case -- i.e. /Company.plist)
		ERXRouteRequestHandler routeRequestHandler = new ERXRouteRequestHandler(ERXRouteRequestHandler.WO);

		// Add the default routes for Company -- CompanyController does some JSR-311 annotations
		routeRequestHandler.addDefaultRoutes(Company.ENTITY_NAME);

		// Add the default routes for Animal -- AnimalController is a ERXDefaultRouteController, and is exposed as "Pet" externally
		routeRequestHandler.addDefaultRoutes(Animal.ENTITY_NAME);

		// This is showing what addDefaultRoutes actually does and that you add your own custom routes this way as well.
		// routeRequestHandler.addDefaultRoutes(Person.ENTITY_NAME);
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person", ERXRoute.Method.Head, PersonController.class, "head"));
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person", ERXRoute.Method.Post, PersonController.class, "create"));
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person", ERXRoute.Method.All, PersonController.class, "index"));
		// MS: this only works with numeric ids
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person/{action:identifier}", ERXRoute.Method.Get, PersonController.class));
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person/{person:Person}", ERXRoute.Method.Get, PersonController.class, "show"));
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person/{person:Person}", ERXRoute.Method.Put, PersonController.class, "update"));
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person/{person:Person}", ERXRoute.Method.Delete, PersonController.class, "destroy"));
		routeRequestHandler.addRoute(new ERXRoute(Person.ENTITY_NAME, "/Person/{person:Person}/{action:identifier}", ERXRoute.Method.All, PersonController.class));

		// Car and Manufacturer are two non-EO classes. For this to work properly, you need to either manually register
		// delegates for them via IERXRestDelegate.Factory or create classes named <EntityName>RestDelegate, which is
		// how this project works -- CarRestDelegate and ManufacturerRestDelegate.
		routeRequestHandler.addDefaultRoutes("Car", false, CarController.class);
		routeRequestHandler.addDefaultRoutes("Manufacturer", false, ManufacturerController.class);

		// Register the request handler with the application -- it becomse the "ra" request handler
		ERXRouteRequestHandler.register(routeRequestHandler);
	}
}
