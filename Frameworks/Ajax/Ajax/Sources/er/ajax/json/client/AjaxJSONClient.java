package er.ajax.json.client;

import java.util.UUID;

import com.webobjects.appserver.WOContext;

/**
 * AjaxJSONClient renders a "new JSONRpcClient('...')" with a URL back to your application (along with a session ID if
 * there is one).
 * 
 * <code>
 * var jsonClient = &lt;wo:AjaxJSONClient/&gt;;
 * </code>
 * 
 * @author mschrag
 * @binding callback the initialization callback
 * @binding componentName the JSON component to use
 * @binding global if true, a single component instance will be shared for the session (defaults false)
 * @binding instance if global is false, you can set a specific instance identifier (leave out for a generated value)
 */
public class AjaxJSONClient extends AjaxStatelessJSONClient {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String _instance;

	public AjaxJSONClient(WOContext context) {
		super(context);
	}

	@Override
	public boolean isStateless() {
		return false;
	}

	@Override
	public boolean global() {
		return booleanValueForBinding("global", false);
	}

	@Override
	public String jsonComponent() {
		return stringValueForBinding("component");
	}

	@Override
	public String jsonInstance() {
		String instance = stringValueForBinding("instance");
		if (instance == null) {
			if (_instance == null && !global()) {
				_instance = UUID.randomUUID().toString();
				instance = _instance;
			}
			instance = _instance;
		}
		return instance;
	}
}
