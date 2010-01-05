package er.rest.routes;

import java.lang.annotation.Annotation;
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
import er.rest.routes.jsr311.DELETE;
import er.rest.routes.jsr311.GET;
import er.rest.routes.jsr311.HttpMethod;
import er.rest.routes.jsr311.POST;
import er.rest.routes.jsr311.PUT;
import er.rest.routes.jsr311.Path;
import er.rest.routes.jsr311.Paths;

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
	 * A NameFormat that behaves like Rails -- plural entities, plural routes, lowercase names. 
	 */
	public static NameFormat RAILS = new NameFormat(true, true, true);

	/**
	 * A NameFormat that behaves like WO -- singular entities, singular routes, camel names. 
	 */
	public static NameFormat WO = new NameFormat(false, false, false);

	/**
	 * A NameFormat that behaves like WO -- singular entities, singular routes, lowercase names. 
	 */
	public static NameFormat WO_LOWER = new NameFormat(false, false, true);
	
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
		 * @param pluralControllerName
		 *            if true, controller names with be pluralized ("CompaniesController")
		 * @param pluralRouteName
		 *            if true, routes will be pluralizd ("/Companies.xml")
		 * @param lowercaseRouteName
		 *            if true, routes will be lowercased ("/companies.xml")
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

		/**
		 * Formats the given entity name based on the rules of this format.
		 * 
		 * @param entityName
		 *            the entity name to format
		 * @return the formatted entity name
		 */
		public String formatEntityNamed(String entityName) {
			String singularEntityName = lowercaseRouteName() ? ERXStringUtilities.camelCaseToUnderscore(entityName, true) : entityName;
			String controllerPath;
			if (pluralRouteName()) {
				controllerPath = ERXLocalizer.englishLocalizer().plurifiedString(singularEntityName, 2);
			}
			else {
				controllerPath = singularEntityName;
			}
			return controllerPath;
		}
	}

	public static final Logger log = Logger.getLogger(ERXRouteRequestHandler.class);

	public static final String Key = "ra";
	public static final String TypeKey = "ERXRouteRequestHandler.type";
	public static final String ExtensionKey = "ERXRouteRequestHandler.extension";
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
	 * @param entityNameFormat the format to use for entity names in URLs
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
	 * @param entityName
	 *            the name of the entity
	 * @return the corresponding route controller
	 */
	public Class<? extends ERXRouteController> routeControllerClassForEntityNamed(String entityName) {
		String controllerEntityName = _entityNameFormat.pluralControllerName() ? ERXLocalizer.englishLocalizer().plurifiedString(entityName, 2) : entityName;
		String controllerName = controllerEntityName + "Controller";
		Class<?> controllerClass = _NSUtilities.classWithName(controllerName);
		if (controllerClass == null) {
			throw new IllegalArgumentException("There is no controller named '" + controllerName + "'.");
		}
		return controllerClass.asSubclass(ERXRouteController.class);
	}

	/**
	 * Calls the static method 'addRoutes(entityName, routeRequetHandler)' on the route controller for the given entity
	 * name, giving it the opportunity to add routes for this entity. Additionally, this method looks for all methods annotated
	 * with @Path or @Paths annotations and adds the corresponding routes. If no addRoutes method is found and no
	 * @Path annotated methods exist, it will log a warning and add default routes instead.
	 * 
	 * @param entityName
	 *            the name of the entity
	 */
	public void addRoutes(String entityName) {
		addRoutes(entityName, routeControllerClassForEntityNamed(entityName));
	}

	/**
	 * Calls the static method 'addRoutes(entityName, routeRequetHandler)' on the given route controller class, giving
	 * it the opportunity to add routes for the given entity. Additionally, this method looks for all methods annotated
	 * with @Path or @Paths annotations and adds the corresponding routes. If no addRoutes method is found and no
	 * @Path annotated methods exist, it will log a warning and add default routes instead.
	 * 
	 * @param entityName
	 *            the name of the entity
	 * @param routeControllerClass
	 *            the name of the route controller
	 */
	public void addRoutes(String entityName, Class<? extends ERXRouteController> routeControllerClass) {
		boolean addDefaultRoutes = false;
		try {
			Method addRoutesMethod = routeControllerClass.getMethod("addRoutes", String.class, ERXRouteRequestHandler.class);
			addRoutesMethod.invoke(null, entityName, this);
		}
		catch (NoSuchMethodException e) {
			addDefaultRoutes = true;
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to add routes for " + routeControllerClass + ".", t);
		}
		
		for (Method routeMethod : routeControllerClass.getDeclaredMethods()) {
			String routeMethodName = routeMethod.getName();
			if (routeMethodName.endsWith("Action")) {
				String actionName = routeMethodName.substring(0, routeMethodName.length() - "Action".length());
				
				Path pathAnnotation = routeMethod.getAnnotation(Path.class);
				Paths pathsAnnotation = routeMethod.getAnnotation(Paths.class);
				if (pathAnnotation != null || pathsAnnotation != null) {
					addDefaultRoutes = false;
					
					ERXRoute.Method method = null;
					for (Annotation annotation : routeMethod.getAnnotations()) {
						HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
						if (httpMethod != null) {
							if (method == null) {
								method = httpMethod.value();
							}
							else {
								throw new IllegalArgumentException(routeControllerClass.getSimpleName() + "." + routeMethod.getName() + " is annotated as more than one http method.");
							}
						}
					}
					if (method == null) {
						method = ERXRoute.Method.Get;
					}
					
					Annotation methodAnnotation = routeMethod.getAnnotation(GET.class);
					if (methodAnnotation == null) {
						methodAnnotation = routeMethod.getAnnotation(POST.class);
						if (methodAnnotation == null) {
							methodAnnotation = routeMethod.getAnnotation(PUT.class);
							if (methodAnnotation == null) {
								methodAnnotation = routeMethod.getAnnotation(DELETE.class);
							}
						}
					}
					if (methodAnnotation != null) {
						method = methodAnnotation.annotationType().getAnnotation(HttpMethod.class).value();
					}
					if (pathAnnotation != null) {
						addRoute(new ERXRoute(entityName, pathAnnotation.value(), method, routeControllerClass, actionName));
					}
					if (pathsAnnotation != null) {
						for (Path path : pathsAnnotation.value()) {
							addRoute(new ERXRoute(entityName, path.value(), method, routeControllerClass, actionName));
						}
					}
				}
			}
		}
		
		if (addDefaultRoutes) {
			ERXRouteRequestHandler.log.warn("No 'addRoutes(entityName, routeRequetHandler)' method and no @Path designations found on '" + routeControllerClass.getSimpleName() + "'. Registering default routes instead.");
			addDefaultRoutes(entityName, routeControllerClass);
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
	 * Return the controller path name for an entity name based on the entity name format.
	 * 
	 * @param entityName
	 *            the entity name
	 * @return the controller identifier part of the path (the "companies" part in "/companies/1000");
	 */
	public String controllerPathForEntityNamed(String entityName) {
	  return _entityNameFormat.formatEntityNamed(ERXRestNameRegistry.registry().externalNameForInternalName(entityName));
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
		addDefaultRoutes(entityName, entityName, numericPKs, controllerClass);
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
	 * @param entityType
	 *            the type of the enity
	 * @param numericPKs
	 *            if true, routes can assume numeric PK's and add some extra convenience routes
	 * @param controllerClass
	 *            the controller class
	 */
	public void addDefaultRoutes(String entityName, String entityType, boolean numericPKs, Class<? extends ERXRouteController> controllerClass) {
		String singularInternalName = _entityNameFormat.lowercaseRouteName() ? ERXStringUtilities.camelCaseToUnderscore(entityName, true) : entityName;

		String externalName = ERXRestNameRegistry.registry().externalNameForInternalName(entityName);
		String singularExternalName = _entityNameFormat.lowercaseRouteName() ? ERXStringUtilities.camelCaseToUnderscore(externalName, true) : externalName;
		String pluralExternalName = ERXLocalizer.englishLocalizer().plurifiedString(singularExternalName, 2);

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute(entityName, "/" + pluralExternalName, ERXRoute.Method.Head, controllerClass, "head"));
		}
		addRoute(new ERXRoute(entityName, "/" + singularExternalName, ERXRoute.Method.Head, controllerClass, "head"));

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute(entityName, "/" + pluralExternalName, ERXRoute.Method.Post, controllerClass, "create"));
		}
		addRoute(new ERXRoute(entityName, "/" + singularExternalName, ERXRoute.Method.Post, controllerClass, "create"));

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute(entityName, "/" + pluralExternalName, ERXRoute.Method.All, controllerClass, "index"));
		}
		else {
			addRoute(new ERXRoute(entityName, "/" + singularExternalName, ERXRoute.Method.All, controllerClass, "index"));
		}

		if (numericPKs) {
			// MS: this only works with numeric ids
			addRoute(new ERXRoute(entityName, "/" + singularExternalName + "/{action:identifier}", ERXRoute.Method.Get, controllerClass));
			if (_entityNameFormat.pluralRouteName()) {
				// MS: this only works with numeric ids
				addRoute(new ERXRoute(entityName, "/" + pluralExternalName + "/{action:identifier}", ERXRoute.Method.Get, controllerClass));
			}
		}
		else {
			addRoute(new ERXRoute(entityName, "/" + singularExternalName + "/new", ERXRoute.Method.All, controllerClass, "new"));
			if (_entityNameFormat.pluralRouteName()) {
				addRoute(new ERXRoute(entityName, "/" + pluralExternalName + "/new", ERXRoute.Method.All, controllerClass, "new"));
			}
		}

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute(entityName, "/" + pluralExternalName + "/{" + singularInternalName + ":" + entityType + "}", ERXRoute.Method.Get, controllerClass, "show"));
		}
		addRoute(new ERXRoute(entityName, "/" + singularExternalName + "/{" + singularInternalName + ":" + entityType + "}", ERXRoute.Method.Get, controllerClass, "show"));

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute(entityName, "/" + pluralExternalName + "/{" + singularInternalName + ":" + entityType + "}", ERXRoute.Method.Put, controllerClass, "update"));
		}
		addRoute(new ERXRoute(entityName, "/" + singularExternalName + "/{" + singularInternalName + ":" + entityType + "}", ERXRoute.Method.Put, controllerClass, "update"));

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute(entityName, "/" + pluralExternalName + "/{" + singularInternalName + ":" + entityType + "}", ERXRoute.Method.Delete, controllerClass, "destroy"));
		}
		addRoute(new ERXRoute(entityName, "/" + singularExternalName + "/{" + singularInternalName + ":" + entityType + "}", ERXRoute.Method.Delete, controllerClass, "destroy"));

		if (_entityNameFormat.pluralRouteName()) {
			addRoute(new ERXRoute(entityName, "/" + pluralExternalName + "/{" + singularInternalName + ":" + entityType + "}/{action:identifier}", ERXRoute.Method.All, controllerClass));
		}
		addRoute(new ERXRoute(entityName, "/" + singularExternalName + "/{" + singularInternalName + ":" + entityType + "}/{action:identifier}", ERXRoute.Method.All, controllerClass));
	}

	/**
	 * Returns the route that matches the request method and path, storing metadata about the route in the given
	 * userInfo dictionary.
	 * 
	 * @param method
	 *            the request method
	 * @param path
	 *            the request path
	 * @param userInfo
	 *            a mutable userInfo
	 * @return the matching route (or null if one is not found)
	 */
	public ERXRoute routeForMethodAndPath(String method, String path, NSMutableDictionary<String, Object> userInfo) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		int dotIndex = path.lastIndexOf('.');
		String requestedType = null;
		if (dotIndex >= 0) {
			String type = path.substring(dotIndex + 1);
			if (type.length() > 0) {
				requestedType = type;
				userInfo.setObjectForKey(type, ERXRouteRequestHandler.ExtensionKey);
			}
			path = path.substring(0, dotIndex);
		}

		ERXRoute.Method routeMethod = ERXRoute.Method.valueOf(ERXStringUtilities.capitalize(method.toLowerCase()));
		ERXRoute matchingRoute = null;
		NSDictionary<ERXRoute.Key, String> keys = null;
		for (ERXRoute route : _routes) {
			keys = route.keys(path, routeMethod);
			if (keys != null) {
				matchingRoute = route;
				break;
			}
		}

		if (matchingRoute != null) {
			if (requestedType != null) {
				userInfo.setObjectForKey(requestedType, ERXRouteRequestHandler.TypeKey);
			}
			userInfo.setObjectForKey(path, ERXRouteRequestHandler.PathKey);
			userInfo.setObjectForKey(matchingRoute, ERXRouteRequestHandler.RouteKey);
			userInfo.setObjectForKey(keys, ERXRouteRequestHandler.KeysKey);
		}

		return matchingRoute;
	}

	/**
	 * Sets up the request userInfo for the given request for a request of the given method and path.
	 * 
	 * @param request
	 *            the request to configure the userInfo on
	 * @param method
	 *            the request method
	 * @param path
	 *            the request path
	 * @return the matching route for this method and path
	 */
	public ERXRoute setupRequestWithRouteForMethodAndPath(WORequest request, String method, String path) {
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

		ERXRoute matchingRoute = routeForMethodAndPath(method, path, mutableUserInfo);

		if (matchingRoute != null && mutableUserInfo != userInfo) {
			request.setUserInfo(mutableUserInfo);
		}

		return matchingRoute;
	}

	/**
	 * Sets up a route controller based on a request userInfo that came from routeForMethodAndPath.
	 * 
	 * @param controller
	 *            the controller to setup
	 * @param userInfo
	 *            the request userInfo
	 */
	public void setupRouteControllerFromUserInfo(ERXRouteController controller, NSDictionary<String, Object> userInfo) {
		controller._setRequestHandler(this);

		if (userInfo != null) {
			ERXRoute route = (ERXRoute) userInfo.objectForKey(ERXRouteRequestHandler.RouteKey);
			controller._setRoute(route);
			@SuppressWarnings("unchecked")
			NSDictionary<ERXRoute.Key, String> keys = (NSDictionary<ERXRoute.Key, String>) userInfo.objectForKey(ERXRouteRequestHandler.KeysKey);
			controller._setRouteKeys(keys);
		}
	}

	@Override
	public NSArray getRequestHandlerPathForRequest(WORequest request) {
		NSMutableArray<Object> requestHandlerPath = new NSMutableArray<Object>();

		try {
			String path = request._uriDecomposed().requestHandlerPath();

			ERXRoute matchingRoute = setupRequestWithRouteForMethodAndPath(request, request.method(), path);
			if (matchingRoute != null) {
				@SuppressWarnings("unchecked")
				NSDictionary<ERXRoute.Key, String> keys = (NSDictionary<ERXRoute.Key, String>) request.userInfo().objectForKey(ERXRouteRequestHandler.KeysKey);
				String controller = keys.objectForKey(ERXRoute.ControllerKey);
				String actionName = keys.objectForKey(ERXRoute.ActionKey);
				requestHandlerPath.addObject(controller);
				requestHandlerPath.addObject(actionName);
			}
			else {
				requestHandlerPath.addObject(ERXProperties.stringForKeyWithDefault("ERXRest.missingControllerName", "ERXMissingRouteController"));
				requestHandlerPath.addObject("missing");
				// throw new FileNotFoundException("There is no controller for the route '" + path + "'.");
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
	@SuppressWarnings("unchecked")
	public WOAction getActionInstance(Class class1, Class[] aclass, Object[] aobj) {
		ERXRouteController controller = (ERXRouteController) super.getActionInstance(class1, aclass, aobj);
		WORequest request = (WORequest) aobj[0];
		setupRouteControllerFromUserInfo(controller, request.userInfo());
		return controller;
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
