package er.extensions.components.javascript;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXSession;
import er.extensions.components.ERXStatelessComponent;

/**
 * Keeps a session open by continuously calling a direct action.
 * Drop this into the page wrapper of your app if you want your users
 * to be able to just keep their browser window open without fear of
 * having their next save trigger a session timeout. <br />
 * You can set an interval in seconds at which the action is triggered,
 * by default it is half the session timeout.
 * @author ak
 */
public class ERXJSLifebeat extends ERXStatelessComponent {

	public ERXJSLifebeat(WOContext arg0) {
		super(arg0);
	}

	public long interval() {
		long interval = session().timeOutMillis() / 2;
		Number value = (Number)valueForBinding("interval");
		if(value != null) {
			interval = value.longValue() * 1000;
		}
		return interval;
	}
	
	public static class Action extends WODirectAction {

		public Action(WORequest arg0) {
			super(arg0);
		}
		
		public WOActionResults keepAliveAction() {
            WOResponse response = WOApplication.application().createResponseInContext(context());
            // we give over the session id as we also need to touch the session anyway
            response.setHeader(ERXSession.DONT_STORE_PAGE, session().sessionID());
		    return response;
		}
	}
}
