package er.extensions.components.javascript;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;

import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXSession;
import er.extensions.components.ERXStatelessComponent;

/**
 * Keeps a session open by continuously calling a direct action.
 * Drop this into the page wrapper of your app if you want your users
 * to be able to just keep their browser window open without fear of
 * having their next save trigger a session timeout.
 * <p>
 * You can set an interval in seconds at which the action is triggered,
 * by default it is half the session timeout.
 * @author ak
 */
public class ERXJSLifebeat extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected final static Logger log=Logger.getLogger(ERXJSLifebeat.class);
	
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
	
	public String sessionID() {
		return context().session().sessionID();
	}
	
	public static class Action extends WODirectAction {

		public Action(WORequest arg0) {
			super(arg0);
		}
		
		public WOActionResults keepAliveAction() {
			WOApplication application = WOApplication.application();
			WOContext context = context();
			WOResponse response = application.createResponseInContext(context);
			String sessionID = context.request().stringFormValueForKey("erxsid");
			if (!application.isRefusingNewSessions()) {
				WOSession session = application.restoreSessionWithID(sessionID, context);
				if (session != null) {
					log.debug("Pinging " + sessionID);
					// CHECKME TH do we still need that?
					// we give over the session id as we also need to touch the session anyway
					response.setHeader(ERXSession.DONT_STORE_PAGE, sessionID);
					response.setHeader("application/x-empty", ERXResponse.ContentTypeHeaderKey);
				}
				else {
					log.debug("Couldn't ping " + sessionID);
				}
			}
			else {
				log.debug("Application is refusing new sessions. Not pinging " + sessionID);
			}
			return response;
		}
	}
}
