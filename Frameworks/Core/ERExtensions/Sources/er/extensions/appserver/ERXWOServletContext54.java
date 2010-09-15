/**
 * 
 */
package er.extensions.appserver;

import com.webobjects.appserver.WORequest;

/**
 * Overridden to allow WO54 servlet deployments to utilise {@link ERXApplication#_rewriteURL(String)}.
 * 
 * @author ldeck
 */
public class ERXWOServletContext54 extends ERXWOServletContext {
	
	public ERXWOServletContext54(WORequest request) {
		super(request);
	}
	
	/**
	 * @return the (optionally) rewritten url.
	 */
	public String _urlWithRequestHandlerKey(String requestHandlerKey, String requestHandlerPath, String queryString, boolean isSecure, int somePort) {
		String url = super._urlWithRequestHandlerKey(requestHandlerKey, requestHandlerPath, queryString, isSecure, somePort);
		url = ERXApplication.erxApplication()._rewriteURL(url);
		return url;
	}

}
