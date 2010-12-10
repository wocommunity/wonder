package er.ajax.json.client;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.ajax.json.JSONRequestHandler;
import er.extensions.components.ERXComponent;

/**
 * StatelessJSONClient renders a "new JSONRpcClient('...')" with a URL back to your application (along with a session ID if
 * there is one).
 * 
 * <code>
 * var jsonClient = <wo:StatelessJSONClient/>;
 * </code>
 * 
 * @author mschrag
 * @binding callback the initialization callback
 */
public class AjaxStatelessJSONClient extends ERXComponent {
	public AjaxStatelessJSONClient(WOContext context) {
		super(context);
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public boolean global() {
		return booleanValueForBinding("global", false);
	}

	public boolean isStateless() {
		return true;
	}

	public String jsonComponent() {
		return null;
	}

	public String jsonInstance() {
		return null;
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
