package er.ajax.json;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation._NSUtilities;

/**
 * <p>
 * The base class for JSON "Components".  JSONComponents provide a framework
 * for implementing stateful JSON services.  If you are providing a JSON
 * service that is only used on a page, you can use AjaxProxy to provide
 * a stateful JSON interface to a page.  However, if you would like to
 * provide stateful services to non-page-based clients, AjaxProxy isn't
 * sufficient.  By extending JSONComponent, all of your public methods
 * are exposed as RPC calls to your clients that can be accessed by 
 * constructing a URL to a JSON Request Handler.  For instance, 
 * /json/MyComponentName will give you a stateful service endpoint     
 * </p>
 * 
 * <p>
 * If a method returns another JSONComponent, the client will be
 * sent a JSONRedirect that contains the URL to communicate
 * with the instance you returned.
 * </p>
 * 
 * <b>THIS API IS STILL EXPERIMENTAL AND SUBJECT TO CHANGE</b>
 * 
 * @author mschrag
 */
public class JSONComponent {
	private WOContext _context;

	/**
	 * Constructs a new JSONComponent.
	 * 
	 * @param context the context that created the component
	 */
	public JSONComponent(WOContext context) {
		_context = context;
	}

	/**
	 * Returns the current context.
	 * 
	 * @return the current context.
	 */
	protected WOContext context() {
		return _context;
	}

	/**
	 * Sets the current context.
	 * 
	 * @param context the current context.
	 */
	protected void _setContext(WOContext context) {
		_context = context;
	}

	/**
	 * Called prior to issuing any calls to
	 * the component.  Throw a SecurityException
	 * if the Session user's credentials are
	 * insufficient. 
	 */
	protected void checkAccess() {
		// override to provide an implementation
	}

	/**
	 * Returns a JSONComponent of the given type.
	 * 
	 * @param <T> the JSONComponent type
	 * @param componentClass the class to instantiate
	 * @return an instance of the given JSONComponent
	 */
	public <T extends JSONComponent> T componentWithName(Class<T> componentClass) {
		return JSONComponent.componentWithName(componentClass, context());
	}

	/**
	 * Returns a JSONComponent of the given type.
	 * 
	 * @param <T> the JSONComponent type
	 * @param componentClass the class to instantiate
	 * @param context the WOContext to create within
	 * @return an instance of the given JSONComponent
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JSONComponent> T componentWithName(Class<T> componentClass, WOContext context) {
		T jsonComponent = (T) _NSUtilities.instantiateObject(componentClass, new Class[] { WOContext.class }, new Object[] { context }, true, false);
		return jsonComponent;
	}

	/**
	 * Returns a JSONComponent of the given type.
	 * 
	 * @param <T> the JSONComponent type
	 * @param componentClassName the name of the class to instantiate
	 * @param context the WOContext to create within
	 * @return an instance of the given JSONComponent
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JSONComponent> T componentWithName(String componentClassName, WOContext context) {
		Class<T> componentClass = _NSUtilities.classWithName(componentClassName);
		T jsonComponent = (T) _NSUtilities.instantiateObject(componentClass, new Class[] { WOContext.class }, new Object[] { context }, true, false);
		return jsonComponent;
	}
}
