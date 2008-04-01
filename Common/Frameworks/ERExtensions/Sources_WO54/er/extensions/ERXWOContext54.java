package er.extensions;

import com.webobjects.appserver.WORequest;

public class ERXWOContext54 extends ERXWOContext {
	public ERXWOContext54(WORequest worequest) {
		super(worequest);
	}

	@Override
	public String _urlWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString, boolean isSecure, int somePort) {
		String url = super._urlWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString, isSecure, somePort);
		url = ERXApplication.erxApplication()._rewriteURL(url);
		return url;
	}
}
