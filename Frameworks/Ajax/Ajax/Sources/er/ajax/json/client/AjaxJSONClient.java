package er.ajax.json.client;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.ajax.json.JSONRequestHandler;
import er.extensions.components.ERXComponent;
import er.extensions.foundation.ERXRandomGUID;

/**
 * AjaxJSONClient renders a "new JSONRpcClient('...')" with a URL back to your application (along with a session ID if
 * there is one).
 * 
 * <code>
 * var jsonClient = <wo:AjaxJSONClient/>;
 * </code>
 * 
 * @author mschrag
 * @binding callback the initialization callback
 * @binding componentName the JSON component to use
 * @binding global if true, a single component instance will be shared for the session (defaults false)
 * @binding instance if global is false, you can set a specific instance identifier (leave out for a generated value)
 */
public class AjaxJSONClient extends ERXComponent {
	private String _instance;

	public AjaxJSONClient(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public boolean global() {
		return booleanValueForBinding("global", false);
	}

	public String jsonComponent() {
		return stringValueForBinding("component");
	}

	public String jsonInstance() {
		String instance = stringValueForBinding("instance");
		if (instance == null) {
			if (_instance == null && !global()) {
				_instance = ERXRandomGUID.newGid();
				instance = _instance;
			}
			instance = _instance;
		}
		return instance;
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		AjaxUtils.addScriptResourceInHead(wocontext, woresponse, "jsonrpc.js");

		String queryString = null;
		if (wocontext.request().sessionID() != null && wocontext.session().storesIDsInURLs()) {
			queryString = "wosid=" + wocontext.request().sessionID();
		}

		String componentName = jsonComponent();
		String instance;
		if (componentName == null) {
			instance = null;
		}
		else {
			instance = jsonInstance();
		}

		String jsonUrl = JSONRequestHandler.jsonUrl(wocontext, componentName, instance, queryString);
		woresponse.appendContentString("new JSONRpcClient(");
		String callback = stringValueForBinding("callback");
		if (callback != null) {
			woresponse.appendContentString(callback);
			woresponse.appendContentString(",");
		}
		woresponse.appendContentString("'");
		woresponse.appendContentString(jsonUrl);
		woresponse.appendContentString("')");
	}
}
