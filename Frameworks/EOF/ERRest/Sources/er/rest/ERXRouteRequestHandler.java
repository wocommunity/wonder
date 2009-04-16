package er.rest;

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

/**
 * <b>EXPERIMENTAL</b>
 * 
 * in Application:
 * <code>
 * ERXRouteRequestHandler routeRequestHandler = new ERXRouteRequestHandler();
 * routeRequestHandler.addRoute(new ERXRoute("/reminders/{action}", RemindersController.class.getName()));
 * routeRequestHandler.addRoute(new ERXRoute("/reminder/{reminder:Reminder}", RemindersController.class.getName(), "view"));
 * ERXRouteRequestHandler.register(routeRequestHandler);
 * </code>
 * 
 * in RemindersController:
 * <code>
 * public class RemindersController extends ERXRouteDirectAction {
 *   public RemindersController(WORequest request, ERXRoute route, NSDictionary<Key, String> keys) {
 *     super(request, route, keys);
 *   }
 *   
 *   public WOActionResults viewAction() {
 *     Reminder reminder = (Reminder) objects(ERXEC.newEditingContext()).objectForKey("reminder");
 *     return response(ERXKeyFilter.attributes(), reminder);
 *   }
 *   
 *   public WOActionResults listAction() throws Exception {
 *     Day day = Day.todayDay();
 *     EOQualifier qualifier = day.qualifier(Reminder.CREATION_DATE_KEY);
 *     EOEditingContext editingContext = ERXEC.newEditingContext();
 *     NSArray<Reminder> reminders = Reminder.fetchReminders(editingContext, qualifier, EOSort.descs(Reminder.CREATION_DATE_KEY));
 *     return response(ERXKeyFilter.attributes(), editingContext, Reminder.ENTITY_NAME, reminders);
 *   }
 * }
 * </code>
 * 
 * in browser:
 * <code>
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/reminders/list.xml
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/reminders/list.json
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/reminders/list.plist
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/reminders/list
 * http://localhost/cgi-bin/WebObjects/YourApp.woa/reminder/100.json
 * </code>
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

	public ERXRouteRequestHandler() {
		_routes = new NSMutableArray<ERXRoute>();
	}

	public void addRoute(ERXRoute route) {
		_routes.addObject(route);
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
			@SuppressWarnings("unchecked") NSMutableDictionary<String, Object> userInfo = ((ERXRequest) request).mutableUserInfo();
			userInfo.setObjectForKey(type, ERXRouteRequestHandler.TypeKey);
			userInfo.setObjectForKey(path, ERXRouteRequestHandler.PathKey);

			ERXRoute matchingRoute = null;
			NSDictionary<ERXRoute.Key, String> keys = null;
			for (ERXRoute route : _routes) {
				keys = route.keys(path);
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
		}
		catch (Throwable t) {
			throw new RuntimeException("Failed to compute request handler path.", t);
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
		ERXRouteDirectAction actionInstance = (ERXRouteDirectAction)super.getActionInstance(class1, aclass, aobj);
		WORequest request = (WORequest) aobj[0];
		ERXRoute route = (ERXRoute) request.userInfo().objectForKey(ERXRouteRequestHandler.RouteKey);
		actionInstance.setRoute(route);
		@SuppressWarnings("unchecked") NSDictionary<ERXRoute.Key, String> keys = (NSDictionary<ERXRoute.Key, String>) request.userInfo().objectForKey(ERXRouteRequestHandler.KeysKey);
		actionInstance.setKeys(keys);
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
