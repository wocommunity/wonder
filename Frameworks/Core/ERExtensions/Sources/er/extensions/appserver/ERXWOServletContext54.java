package er.extensions.appserver;

import com.webobjects.appserver.WORequest;

/**
 * Overridden to allow WO54 servlet deployments to utilise {@link ERXApplication#_rewriteURL(String)}.
 * 
 * @author ldeck
 * @deprecated use {@link ERXWOServletContext} instead
 */
@Deprecated
public class ERXWOServletContext54 extends ERXWOServletContext {
	public ERXWOServletContext54(WORequest request) {
		super(request);
	}
}
