package er.ajax.json.client;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxUtils;
import er.ajax.json.JSONRequestHandler;

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
 */
public class AjaxJSONClient extends AjaxDynamicElement {
	public AjaxJSONClient(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		String queryString = null;
		if (wocontext.request().sessionID() != null && wocontext.session().storesIDsInURLs()) {
			queryString = "wosid=" + wocontext.request().sessionID();
		}
		String jsonUrl = wocontext.urlWithRequestHandlerKey(JSONRequestHandler.RequestHandlerKey, "", queryString);
		woresponse.appendContentString("new JSONRpcClient(");
		WOComponent component = wocontext.component();
		String callback = (String) valueForBinding("callback", component);
		if (callback != null) {
			woresponse.appendContentString(callback);
			woresponse.appendContentString(",");
		}
		woresponse.appendContentString("'");
		woresponse.appendContentString(jsonUrl);
		woresponse.appendContentString("')");
	}

	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "jsonrpc.js");
	}

	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
}
