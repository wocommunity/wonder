package er.ajax.json;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxUtils;

public class AjaxJSONClient extends AjaxDynamicElement {
	public AjaxJSONClient(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		String queryString = null;
		if (wocontext.request().sessionID() != null) {
			queryString = "wosid=" + wocontext.request().sessionID();
		}
		String jsonUrl = wocontext.urlWithRequestHandlerKey(JSONRequestHandler.RequestHandlerKey, "", queryString);
		woresponse.appendContentString("new JSONRpcClient('" + jsonUrl + "')");
	}

	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "jsonrpc.js");
	}

	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
}
