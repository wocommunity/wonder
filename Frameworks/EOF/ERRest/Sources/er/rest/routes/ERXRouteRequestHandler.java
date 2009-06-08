package er.rest.routes;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAction;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WODirectActionRequestHandler;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

import er.extensions.appserver.ERXRequest;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * ERXRouteRequestHandler is the request handler that can process rails-style route mappings and convert them to
 * ERXRestController action methods.
 * 
 * in Application:
 * 
 * <pre>
 * ERXRouteRequestHandler routeRequestHandler = new ERXRouteRequestHandler();
 * routeRequestHandler.addRoute(new ERXRoute(&quot;/people/{action}&quot;, PeopleController.class));
 * routeRequestHandler.addRoute(new ERXRoute(&quot;/person/{person:Person}&quot;, PeopleController.class, &quot;view&quot;));
 * ERXRouteRequestHandler.register(routeRequestHandler);
 * </pre>
 * 
 * or
 * 
 * <pre>
 * ERXRouteRequestHandler routeRequestHandler = new ERXRouteRequestHandler();
 * routeRequestHandler.addDefaultRoutes(Person.ENTITY_NAME);
 * ERXRouteRequestHandler.register(routeRequestHandler);
 * </pre>
 * 
 * in PeopleController:
 * 
 * <pre>
 * public class PeopleController extends ERXRouteController {
 * 	public PeopleController(WORequest request) {
 * 		super(request);
 * 	}
 * 
 * 	public Person person() {
 * 		Person person = (Person) routeObjectForKey(&quot;person&quot;);
 * 		return person;
 * 	}
 * 
 * 	public ERXKeyFilter showFilter() {
 * 		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
 * 		filter.include(Person.COMPANY).includeAttributes();
 * 		return filter;
 * 	}
 * 
 * 	public ERXKeyFilter updateFilter() {
 * 		ERXKeyFilter filter = ERXKeyFilter.filterWithAttributes();
 * 		filter.include(Person.COMPANY);
 * 		return filter;
 * 	}
 * 
 * 	public WOActionResults createAction() {
 * 		Person person = (Person) create(Person.ENTITY_NAME, updateFilter());
 * 		editingContext().saveChanges();
 * 		return response(person, showFilter());
 * 	}
 * 
 * 	public WOActionResults updateAction() {
 * 		Person person = person();
 * 		update(person, updateFilter());
 * 		editingContext().saveChanges();
 * 		return response(person, showFilter());
 * 	}
 * 
 * 	public WOActionResults showAction() {
 * 		return response(person(), showFilter());
 * 	}
 * 
 * 	public WOActionResults indexAction() {
 * 		NSArray&lt;Person&gt; people = Person.fetchPersons(editingContext(), null, Person.LAST_NAME.asc().then(Person.FIRST_NAME.asc()));
 * 		return response(editingContext(), Person.ENTITY_NAME, people, showFilter());
 * 	}
 * }
 * </pre>
 * 
 * in browser:
 * 
 * <pre>
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/ra/people.xml
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/ra/people.json
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/ra/people.plist
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/ra/person/100.json
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/ra/person/100/edit.json
 * </pre>
 * 
 * @author mschrag
 */
public class ERXRouteRequestHandler extends WODirectActionRequestHandler {
	public static final Logger log = Logger.getLogger(ERXRouteRequestHandler.class);

	public static final String Key = "ra";
	public static final String TypeKey = "ERXRouteRequestHandler.type";
	public static final String PathKey = "ERXRouteRequestHandler.path";
	public static final String RouteKey = "ERXRouteRequestHandler.route";
	public static final String KeysKey = "ERXRouteRequestHandler.keys";

	private NSMutableArray<ERXRoute> _routes;

	/**
	 * Constructs a new ERXRouteRequestHandler.
	 */
	public ERXRouteRequestHandler() {
		_routes = new NSMutableArray<ERXRoute>();
	}

	/**
	 * Inserts a route at the beginning of the route list.
	 * 
	 * @param route
	 *            the route to insert
	 */
	public void insertRoute(ERXRoute route) {
		_routes.insertObjectAtIndex(route, 0);
	}

	/**
	 * Adds a new route to this request handler.
	 * 
	 * @param route
	 *            the route to add
	 */
	public void addRoute(ERXRoute route) {
		_routes.addObject(route);
	}

	/**
	 * Removes the given route from this request handler.
	 * 
	 * @param route
	 *            the route to remove
	 */
	public void removeRoute(ERXRoute route) {
		_routes.removeObject(route);
	}

	/**
	 * Returns the routes for this request handler.
	 * 
	 * @return the routes for this request handler.
	 */
	public NSArray<ERXRoute> routes() {
		return _routes.immutableClone();
	}

	/**
	 * Adds default routes and maps them to a controller named "[plural entity name]Controller". For instance, if the
	 * entity name is "Person" it would make a controller named "PeopleController".
	 * 
	 * @param entityName
	 *            the name of the entity to create routes for
	 */
	@SuppressWarnings("unchecked")
	public void addDefaultRoutes(String entityName) {
		String pluralEntityName = ERXLocalizer.defaultLocalizer().plurifiedString(entityName, 2);
		String controllerName = pluralEntityName + "Controller";
		Class controllerClass = _NSUtilities.classWithName(controllerName);
		if (controllerClass == null) {
			throw new IllegalArgumentException("There is controller named '" + controllerName + "'.");
		}
		addDefaultRoutes(entityName, controllerClass.asSubclass(ERXRouteController.class));
	}

	/**
	 * Adds list and view routes for the given entity. For instance, if you provide the entity name "Reminder" you will
	 * get the routes:
	 * 
	 * <pre>
	 * /reminders
	 * /reminders/{action}
	 * /reminder/{reminder:Reminder}
	 * /reminder/{reminder:Reminder}/{action}
	 * </pre>
	 * 
	 * @param entityName
	 *            the entity name to route with
	 * @param controllerClass
	 *            the controller class
	 */
	public void addDefaultRoutes(String entityName, Class<? extends ERXRouteController> controllerClass) {
		addDefaultRoutes(entityName, true, controllerClass);
	}

	/**
	 * Adds list and view routes for the given entity. For instance, if you provide the entity name "Reminder" you will
	 * get the routes:
	 * 
	 * <pre>
	 * /reminders
	 * /reminders/{action}
	 * /reminder/{reminder:Reminder}
	 * /reminder/{reminder:Reminder}/{action}
	 * </pre>
	 * 
	 * @param entityName
	 *            the entity name to route with
	 * @param numericPKs
	 *            if true, routes can assume numeric PK's and add some extra convenience routes
	 * @param controllerClass
	 *            the controller class
	 */
	public void addDefaultRoutes(String entityName, boolean numericPKs, Class<? extends ERXRouteController> controllerClass) {
		boolean lowercaseEntityNames = ERXProperties.booleanForKeyWithDefault("ERXRest.lowercaseEntityNames", true);
		String singularEntityName = lowercaseEntityNames ? ERXStringUtilities.uncapitalize(entityName) : entityName;
		String pluralEntityName = ERXLocalizer.defaultLocalizer().plurifiedString(singularEntityName, 2);

		addRoute(new ERXRoute(".*", ERXRoute.Method.Head, controllerClass, "head"));
		addRoute(new ERXRoute("/" + pluralEntityName, ERXRoute.Method.Post, controllerClass, "create"));
		addRoute(new ERXRoute("/" + singularEntityName, ERXRoute.Method.Post, controllerClass, "create"));
		addRoute(new ERXRoute("/" + pluralEntityName, ERXRoute.Method.All, controllerClass, "index"));

		if (numericPKs) {
			addRoute(new ERXRoute("/" + pluralEntityName + "/{action:identifier}", ERXRoute.Method.Get, controllerClass)); // MS:
			// this only works with numeric ids
		}
		else {
			addRoute(new ERXRoute("/" + pluralEntityName + "/new", ERXRoute.Method.All, controllerClass, "new"));
		}

		addRoute(new ERXRoute("/" + pluralEntityName + "/{" + singularEntityName + ":" + entityName + "}", ERXRoute.Method.Get, controllerClass, "show"));
		addRoute(new ERXRoute("/" + singularEntityName + "/{" + singularEntityName + ":" + entityName + "}", ERXRoute.Method.Get, controllerClass, "show"));
		addRoute(new ERXRoute("/" + pluralEntityName + "/{" + singularEntityName + ":" + entityName + "}", ERXRoute.Method.Put, controllerClass, "update"));
		addRoute(new ERXRoute("/" + singularEntityName + "/{" + singularEntityName + ":" + entityName + "}", ERXRoute.Method.Put, controllerClass, "update"));
		addRoute(new ERXRoute("/" + pluralEntityName + "/{" + singularEntityName + ":" + entityName + "}", ERXRoute.Method.Delete, controllerClass, "destroy"));
		addRoute(new ERXRoute("/" + singularEntityName + "/{" + singularEntityName + ":" + entityName + "}", ERXRoute.Method.Delete, controllerClass, "destroy"));
		addRoute(new ERXRoute("/" + pluralEntityName + "/{" + singularEntityName + ":" + entityName + "}/{action:identifier}", ERXRoute.Method.All, controllerClass));
		addRoute(new ERXRoute("/" + singularEntityName + "/{" + singularEntityName + ":" + entityName + "}/{action:identifier}", ERXRoute.Method.All, controllerClass));
	}

	@Override
	public NSArray getRequestHandlerPathForRequest(WORequest request) {
		NSMutableArray<Object> requestHandlerPath = new NSMutableArray<Object>();

		try {
			String path = request._uriDecomposed().requestHandlerPath();
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			int dotIndex = path.lastIndexOf('.');
			String type = "xml";
			if (dotIndex >= 0) {
				type = path.substring(dotIndex + 1);
				path = path.substring(0, dotIndex);
			}
			@SuppressWarnings("unchecked")
			NSMutableDictionary<String, Object> userInfo = ((ERXRequest) request).mutableUserInfo();
			userInfo.setObjectForKey(type, ERXRouteRequestHandler.TypeKey);
			userInfo.setObjectForKey(path, ERXRouteRequestHandler.PathKey);

			ERXRoute matchingRoute = null;
			NSDictionary<ERXRoute.Key, String> keys = null;
			for (ERXRoute route : _routes) {
				keys = route.keys(path, ERXRoute.Method.valueOf(ERXStringUtilities.capitalize(request.method().toLowerCase())));
				if (keys != null) {
					matchingRoute = route;
					break;
				}
			}

			if (matchingRoute != null) {
				String controller = keys.objectForKey(ERXRoute.ControllerKey);
				String actionName = keys.objectForKey(ERXRoute.ActionKey);
				requestHandlerPath.addObject(controller);
				requestHandlerPath.addObject(actionName);
				userInfo.setObjectForKey(matchingRoute, ERXRouteRequestHandler.RouteKey);
				userInfo.setObjectForKey(keys, ERXRouteRequestHandler.KeysKey);
			}
			else {
				throw new FileNotFoundException("There is no controller for the route '" + path + "'.");
			}
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to process the requested route.", t);
		}

		return requestHandlerPath;
	}

	@Override
	public Object[] getRequestActionClassAndNameForPath(NSArray requestHandlerPath) {
		String requestActionClassName = (String) requestHandlerPath.objectAtIndex(0);
		String requestActionName = (String) requestHandlerPath.objectAtIndex(1);
		return new Object[] { requestActionClassName, requestActionName, _NSUtilities.classWithName(requestActionClassName) };
	}

	@Override
	public WOAction getActionInstance(Class class1, Class[] aclass, Object[] aobj) {
		ERXRouteController actionInstance = (ERXRouteController) super.getActionInstance(class1, aclass, aobj);
		WORequest request = (WORequest) aobj[0];
		ERXRoute route = (ERXRoute) request.userInfo().objectForKey(ERXRouteRequestHandler.RouteKey);
		actionInstance._setRoute(route);
		@SuppressWarnings("unchecked")
		NSDictionary<ERXRoute.Key, String> keys = (NSDictionary<ERXRoute.Key, String>) request.userInfo().objectForKey(ERXRouteRequestHandler.KeysKey);
		actionInstance._setRouteKeys(keys);
		return actionInstance;
	}

	/**
	 * Registers an ERXRestRequestHandler with the WOApplication for the handler key "rest".
	 * 
	 * @param requestHandler
	 *            the rest request handler to register
	 */
	public static void register(ERXRouteRequestHandler requestHandler) {
		WOApplication.application().registerRequestHandler(requestHandler, ERXRouteRequestHandler.Key);
	}
}
