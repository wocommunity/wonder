package er.ajax;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.appserver.ERXKeepAliveResponse;
import er.extensions.foundation.ERXMutableDictionary;
import er.extensions.foundation.ERXSelectorUtilities;

/**
 * Request handler that offers push-style notifications. <br>
 * Gets registered under "/push/" on framework load.<br>
 * You should open an Ajax.Request, implement onInteractive: and the do
 * something useful when you get new data. Changes should be pushed with
 * push(sessionID, someString);
 * <p>
 * TODO:
 * <li>currently the request stays open even when the client closed it (which is bad)
 * <li>implement a boundary scheme to tell when a "message" is complete. This
 * means we need a special Ajax.Request that does it.
 * <li>implement various client-side stuff to be actually useful (chats, EO
 * notifications).
 * <li>ask Frank about his EO layer
 * <li>use the request handler path as a "topic", so we can have more than one on a page.
 * 
 * @author ak
 */
public class AjaxPushRequestHandler extends WORequestHandler {

	public static final String AjaxCometRequestHandlerKey = "push";

	protected static Logger log = Logger.getLogger(AjaxPushRequestHandler.class);

	private static NSMutableDictionary<String, ERXKeepAliveResponse> responses = ERXMutableDictionary.synchronizedDictionary();

	public AjaxPushRequestHandler() {
		NSNotificationCenter.defaultCenter().addObserver(this, ERXSelectorUtilities.notificationSelector("sessionDidTimeOut"), WOSession.SessionDidTimeOutNotification, null);
	}

	/**
	 * Remove stale responses when a session times out.
	 * 
	 * @param n
	 */
	public void sessionDidTimeOut(NSNotification n) {
		String id = (String) n.object();
		ERXKeepAliveResponse response = responseForSessionID(id);
		response.reset();
		responses.removeObjectForKey(id);
	}

	/**
	 * Get/Create the current request for the session and return it.
	 */
	public WOResponse handleRequest(WORequest request) {
		String sessionID = request.sessionID();
		ERXKeepAliveResponse response = responseForSessionID(sessionID);
		response.reset();
		return response;
	}

	/**
	 * Return or create the correct response for the session ID.
	 * 
	 * @param sessionID
	 * @return response for ID
	 */
	private static ERXKeepAliveResponse responseForSessionID(String sessionID) {
		ERXKeepAliveResponse response = null;
		if (sessionID != null) {
			response = responses.objectForKey(sessionID);
			if (response == null) {
				response = new ERXKeepAliveResponse();
				responses.setObjectForKey(response, sessionID);
			}
		}
		return response;
	}

	/**
	 * Push a string message to the client. At the moment, there is no boundary
	 * handling, so be aware that you could get only half of a message.
	 * 
	 * @param sessionID
	 * @param message
	 */
	public static void push(String sessionID, String message) {
		responseForSessionID(sessionID).push(message);
	}

	/**
	 * Push a data message to the client. At the moment, there is no boundary
	 * handling, so be aware that you could get only half of a message.
	 * 
	 * @param sessionID
	 * @param message
	 */
	public static void push(String sessionID, NSData message) {
		responseForSessionID(sessionID).push(message.bytes());
	}
}
