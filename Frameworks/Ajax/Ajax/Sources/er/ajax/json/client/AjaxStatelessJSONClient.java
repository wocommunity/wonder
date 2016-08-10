package er.ajax.json.client;

import com.webobjects.appserver.WOApplication;
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
 * var jsonClient = &lt;wo:StatelessJSONClient/&gt;;
 * </code>
 * 
 * @author mschrag
 * @binding callback the initialization callback
 */
public class AjaxStatelessJSONClient extends ERXComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public AjaxStatelessJSONClient(WOContext context) {
		super(context);
	}

	public boolean global() {
		return booleanValueForBinding("global", false);
	}

	@Override
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
			String sessionIdKey = WOApplication.application().sessionIdKey();
			queryString = sessionIdKey + "=" + wocontext.request().sessionID();
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
