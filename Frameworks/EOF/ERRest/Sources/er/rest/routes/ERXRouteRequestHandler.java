package er.rest.routes;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;

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

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;
import er.rest.ERXRestNameRegistry;

/**
 * ERXRouteRequestHandler is the request handler that can process rails-style route mappings and convert them to
 * ERXRestController action methods.
 * 
 * in Application:
 * 
 * <pre>
 * ERXRouteRequestHandler routeRequestHandler = new ERXRouteRequestHandler();
 * routeRequestHandler.addDefaultRoutes(Person.ENTITY_NAME);
 * ERXRouteRequestHandler.register(routeRequestHandler);
 * </pre>
 * 
 * or
 * 
 * <pre>
 * ERXRouteRequestHandler routeRequestHandler = new ERXRouteRequestHandler();
 * routeRequestHandler.addRoute(new ERXRoute(&quot;/people/{action}&quot;, PeopleController.class));
 * routeRequestHandler.addRoute(new ERXRoute(&quot;/person/{person:Person}&quot;, PeopleController.class, &quot;show&quot;));
 * ...
 * ERXRouteRequestHandler.register(routeRequestHandler);
 * </pre>
 * 
 * Note that addDefaultRoutes sets up many routes automatically (not just the 2 that are shown above), and for most
 * cases should be your starting point for adding new entities rather than manually adding them.
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
	/**
	 * NameFormat specifies how routes and controller names should be capitalized by default.
	 * 
	 * @author mschrag
	 */
	public static class NameFormat {
		private boolean _pluralControllerName;
		private boolean _pluralRouteName;
		private boolean _lowercaseRouteName;

		/**
		 * Creates a new NameFormat.
		 * 
		 * @param pluralControllerName if true, controller names with be pluralized ("CompaniesController")
		 * @param pluralRouteName if true, routes will be pluralizd ("/Companies.xml")
		 * @param lowercaseRouteName if true, routes will be lowercased ("/companies.xml")
		 */
		public NameFormat(boolean pluralControllerName, boolean pluralRouteName, boolean lowercaseRouteName) {
			_pluralControllerName = pluralControllerName;
			_pluralRouteName = pluralRouteName;
			_lowercaseRouteName = lowercaseRouteName;
		}
		
		/**
		 * Returne whether or not controller names should be pluralizd.
		 * 
		 * @return whether or not controller names should be pluralizd
		 */
		public boolean pluralControllerName() {
			return _pluralControllerName;
		}
		
		/**
		 * Returne whether or not routes should be pluralizd.
		 * 
		 * @return whether or not routes should be pluralizd
		 */
		public boolean pluralRouteName() {
			return _pluralRouteName;
		}
		
		/**
		 * Returne whether or not routes should be capitalized.
		 * 
		 * @return whether or not routes should be capitalized
		 */
		public boolean lowercaseRouteName() {
			return _lowercaseRouteName;
		}
	}
	
	public static final Logger log = Logger.getLogger(ERXRouteRequestHandler.class);

	public static final String Key = "ra";
	public static final String TypeKey = "ERXRouteRequestHandler.type";
	public static final String PathKey = "ERXRouteRequestHandler.path";
	public static final String RouteKey = "ERXRouteRequestHandler.route";
	public static final String KeysKey = "ERXRouteRequestHandler.keys";

	private NameFormat _entityNameFormat;
	private NSMutableArray<ERXRoute> _routes;

	/**
	 * Constructs a new ERXRouteRequestHandler with the default entity name format.
	 */
	public ERXRouteRequestHandler() {
		this(new NameFormat(ERXProperties.booleanForKeyWithDefault("ERXRest.pluralEntityNames", true), ERXProperties.booleanForKeyWithDefault("ERXRest.pluralEntityNames", true), ERXProperties.booleanForKeyWithDefault("ERXRest.lowercaseEntityNames", true)));
	}

	/**
	 * Constructs a new ERXRouteRequestHandler.
	 * 
	 * @parma entityNameFormat the format to use for entity names in URLs
	 */
	public ERXRouteRequestHandler(NameFormat entityNameFormat) {
		_entityNameFormat = entityNameFormat;
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
	 * Returns the default route controller class for the given entity name.
	 * 
	 * @param entityName the name of the entity
	 * @return the corresponding route controller
	 */
	protected Class<? extends ERXRouteController> routeControllerClassForEntityNamed(String entityName) {
		String controllerEntityName = _entityNameFormat.pluralControllerName() ? ERXLocalizer.englishLocalizer().plurifiedString(entityName, 2) : entityName;
		String controllerName = controllerEntityName + "Controller";
		Class<?> controllerClass = _NSUtilities.classWithName(controllerName);
		if (controllerClass == null) {
			throw new IllegalArgumentException("There is no controller named '" + controllerName + "'.");
		}
		return controllerClass.asSubclass(ERXRouteController.class);
	}

	/**
	 * Calls the static method 'addRoutes(entityName, routeRequetHandler)' on the route
	 * controller for the given entity name, giving it the opportunity to add routes for 
	 * this entity. If no addRoutes method is found, it will log a warning and add default
	 * routes instead.
	 * 
	 * @param entityName the name of the entity
	 */
	public void addRoutes(String entityName) {
		addRoutes(entityName, routeControllerClassForEntityNamed(entityName));
	}
	
	/**
	 * Calls the static method 'addRoutes(entityName, routeRequetHandler)' on the given route
	 * controller class, giving it the opportunity to add routes for the given entity. If no
	 * addRoutes method is found, it will log a warning and add default routes instead.
	 * 
	 * @param entityName the name of the entity
	 * @param routeControllerClass the name of the route controller
	 */
	public void addRoutes(String entityName, Class<? extends ERXRouteController> routeControllerClass) {
		try {
			Method addRoutesMethod = routeControllerClass.getMethod("addRoutes", String.class, ERXRouteRequestHandler.class);
			addRoutesMethod.invoke(null, entityName, this);
		}
		catch (NoSuchMethodError e) {
			ERXRouteRequestHandler.log.warn("No 'addRoutes(entityName, routeRequetHandler)' method found on '" + routeControllerClass.getSimpleName() + "'. Registering default routes instead.");
			addDefaultRoutes(entityName, routeControllerClass);
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to add routes for " + routeControllerClass + ".", t);
		}
	}
	
	/**
	 * Adds default routes and maps them to a controller named "[plural entity name]Controller". For instance, if the
	 * entity name is "Person" it would make a controller named "PeopleController".
	 * 
	 * @param entityName
	 *            the name of the entity to create routes for
	 */
	public void addDefaultRoutes(String entityName) {
		addDefaultRoutes(entityName, routeControllerClassForEntityNamed(entityName));
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
		String singularInternalName = _entityNameFormat.lowercaseRouteName() ? ERXStringUtilities.uncapitalize(entityName) : entityName;

		String externalName = ERXRestNameRegistry.registry().externalNameForInternalName(entityName);
		String singularExternalName = _entityNameFormat.lowercaseRouteName() ? ERXStringUtilities.uncapitalize(externalName) : externalName;
		String pluralExternalName = ERXLocalizer.englishLocalizer().plurifiedString(singularExternalName, 2);

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute("/" + pluralExternalName, ERXRoute.Method.Head, controllerClass, "head"));
		}
		addRoute(new ERXRoute("/" + singularExternalName, ERXRoute.Method.Head, controllerClass, "head"));
		
		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute("/" + pluralExternalName, ERXRoute.Method.Post, controllerClass, "create"));
		}
		addRoute(new ERXRoute("/" + singularExternalName, ERXRoute.Method.Post, controllerClass, "create"));
		
		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute("/" + pluralExternalName, ERXRoute.Method.All, controllerClass, "index"));
		}
		else {
			addRoute(new ERXRoute("/" + singularExternalName, ERXRoute.Method.All, controllerClass, "index"));
		}

		if (numericPKs) {
			addRoute(new ERXRoute("/" + singularExternalName + "/{action:identifier}", ERXRoute.Method.Get, controllerClass)); // MS: this only works with numeric ids
			if (_entityNameFormat.pluralRouteName()) {
				addRoute(new ERXRoute("/" + pluralExternalName + "/{action:identifier}", ERXRoute.Method.Get, controllerClass)); // MS: this only works with numeric ids
			}
		}
		else {
			addRoute(new ERXRoute("/" + singularExternalName + "/new", ERXRoute.Method.All, controllerClass, "new"));
			if (_entityNameFormat.pluralRouteName()) {
				addRoute(new ERXRoute("/" + pluralExternalName + "/new", ERXRoute.Method.All, controllerClass, "new"));
			}
		}

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute("/" + pluralExternalName + "/{" + singularInternalName + ":" + entityName + "}", ERXRoute.Method.Get, controllerClass, "show"));
		}
		addRoute(new ERXRoute("/" + singularExternalName + "/{" + singularInternalName + ":" + entityName + "}", ERXRoute.Method.Get, controllerClass, "show"));
		
		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute("/" + pluralExternalName + "/{" + singularInternalName + ":" + entityName + "}", ERXRoute.Method.Put, controllerClass, "update"));
		}
		addRoute(new ERXRoute("/" + singularExternalName + "/{" + singularInternalName + ":" + entityName + "}", ERXRoute.Method.Put, controllerClass, "update"));
		
		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute("/" + pluralExternalName + "/{" + singularInternalName + ":" + entityName + "}", ERXRoute.Method.Delete, controllerClass, "destroy"));
		}
		addRoute(new ERXRoute("/" + singularExternalName + "/{" + singularInternalName + ":" + entityName + "}", ERXRoute.Method.Delete, controllerClass, "destroy"));
		
		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute("/" + pluralExternalName + "/{" + singularInternalName + ":" + entityName + "}/{action:identifier}", ERXRoute.Method.All, controllerClass));
		}
		addRoute(new ERXRoute("/" + singularExternalName + "/{" + singularInternalName + ":" + entityName + "}/{action:identifier}", ERXRoute.Method.All, controllerClass));
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
			String requestedType = null;
			if (dotIndex >= 0) {
				String type = path.substring(dotIndex + 1);
				if (type.length() > 0) {
					requestedType = type;
				}
				path = path.substring(0, dotIndex);
			}

			@SuppressWarnings("unchecked")
			NSDictionary<String, Object> userInfo = request.userInfo();
			NSMutableDictionary<String, Object> mutableUserInfo;
			if (userInfo instanceof NSMutableDictionary) {
				mutableUserInfo = (NSMutableDictionary<String, Object>) userInfo;
			}
			else if (userInfo != null) {
				mutableUserInfo = userInfo.mutableClone();
			}
			else {
				mutableUserInfo = new NSMutableDictionary<String, Object>();
			}
			if (requestedType != null) {
				mutableUserInfo.setObjectForKey(requestedType, ERXRouteRequestHandler.TypeKey);
			}
			mutableUserInfo.setObjectForKey(path, ERXRouteRequestHandler.PathKey);

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
				mutableUserInfo.setObjectForKey(matchingRoute, ERXRouteRequestHandler.RouteKey);
				mutableUserInfo.setObjectForKey(keys, ERXRouteRequestHandler.KeysKey);
			}
			else {
				throw new FileNotFoundException("There is no controller for the route '" + path + "'.");
			}
			
			if (mutableUserInfo != userInfo) {
				request.setUserInfo(mutableUserInfo);
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
