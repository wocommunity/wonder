package er.ajax;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.appserver.ERXKeepAliveResponse;
import er.extensions.foundation.ERXSelectorUtilities;

/**
 * Request handler that offers push-style notifications. <br>
 * Gets registered under "/push/" on framework load.<br>
 * You should open an Ajax.Request, implement onInteractive: and the do
 * something useful when you get new data. Changes should be pushed with
 * push(sessionID, someString);
 * <h3>TODO:</h3>
 * <ul>
 * <li>currently the request stays open even when the client closed it (which is bad)
 * <li>implement a boundary scheme to tell when a "message" is complete. This
 * means we need a special Ajax.Request that does it.
 * <li>implement various client-side stuff to be actually useful (chats, EO
 * notifications).
 * <li>ask Frank about his EO layer
 * <li>use the request handler path as a "topic", so we can have more than one on a page.
 * </ul>
 * 
 * @author ak
 */
public class AjaxPushRequestHandler extends WORequestHandler {

	public static final String AjaxCometRequestHandlerKey = "push";

	private static ConcurrentHashMap<String, ConcurrentHashMap<String, ERXKeepAliveResponse>> responses = new ConcurrentHashMap<String, ConcurrentHashMap<String, ERXKeepAliveResponse>>();

	public AjaxPushRequestHandler() {
		NSNotificationCenter.defaultCenter().addObserver(this, ERXSelectorUtilities.notificationSelector("sessionDidTimeOut"), WOSession.SessionDidTimeOutNotification, null);
	}

	/**
	 * Remove stale responses when a session times out.
	 * 
	 * @param n the session timeout notification
	 */
	public void sessionDidTimeOut(NSNotification n) {
		String id = (String) n.object();
		ConcurrentHashMap<String, ERXKeepAliveResponse> sessionResponses = responses.get(id);
		if (sessionResponses != null) {
			for (ERXKeepAliveResponse response : sessionResponses.values()) {
				response.reset();
			}
			responses.remove(id);
		}
	}

	/**
	 * Get/Create the current request for the session and return it.
	 * 
	 * @param request the request
	 */
	@Override
	public WOResponse handleRequest(WORequest request) {
		String sessionID = request.sessionID();
		String name = request.requestHandlerPath();
		ERXKeepAliveResponse response = responseForSessionIDNamed(sessionID, name);
		response.reset();
		return response;
	}

	/**
	 * Return or create the correct response for the session ID.
	 * 
	 * @param sessionID the session id of the response
	 * @param name the name of the response
	 * @return response for ID
	 */
	private static ERXKeepAliveResponse responseForSessionIDNamed(String sessionID, String name) {
		ERXKeepAliveResponse response = null;
		if (sessionID != null) {
			if(name == null)  {
				name = "";
			}
			ConcurrentHashMap<String, ERXKeepAliveResponse> sessionResponses = responses.get(sessionID);
			if (sessionResponses == null) {
				ConcurrentHashMap<String, ERXKeepAliveResponse> newSessionResponses = new ConcurrentHashMap<>();
				ConcurrentHashMap<String, ERXKeepAliveResponse> prevSessionResponses = responses.putIfAbsent(sessionID, newSessionResponses);
				sessionResponses = (prevSessionResponses == null) ? newSessionResponses : prevSessionResponses;
			}
			response = sessionResponses.get(name);
			if (response == null) {
				ERXKeepAliveResponse newResponse = new ERXKeepAliveResponse();
				ERXKeepAliveResponse prevResponse = sessionResponses.putIfAbsent(name, newResponse);
				response = (prevResponse == null) ? newResponse : prevResponse;
			}
		}
		return response;
	}

	/**
	 * Returns whether or not there is a response open for the given session id and name.
	 * 
	 * @param sessionID the session id of the push response
	 * @param name the name of the push response
	 * @return whether or not there is still a response open
	 */
	public static boolean isResponseOpen(String sessionID, String name) {
		ERXKeepAliveResponse response = responseForSessionIDNamed(sessionID, name);
		return response != null; 
	}
	
	/**
	 * Push a string message to the client. At the moment, there is no boundary
	 * handling, so be aware that you could get only half of a message.
	 * 
	 * @param sessionID the session id of the push response
	 * @param name the name of the push response
	 */
	public static void stop(String sessionID, String name) {
		Map<String, ERXKeepAliveResponse> sessionResponses = responses.get(sessionID);
		if (sessionResponses != null) {
			ERXKeepAliveResponse response = sessionResponses.get(name);
			if (response != null) {
				response.reset();
				sessionResponses.remove(name);
			}
			// not going to do an empty check on sessionResponses, because we'd have to synchronize on
			// the top-level responses to do it safely
		}
	}
	
	/**
	 * Push a string message to the client. At the moment, there is no boundary
	 * handling, so be aware that you could get only half of a message.
	 * 
	 * @param sessionID the session id of the push response
	 * @param name the name of the push response
	 * @param message the message to push
	 */
	public static void push(String sessionID, String name, String message) {
		ERXKeepAliveResponse response = responseForSessionIDNamed(sessionID, name);
		if (response != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(message.length());
			sb.append(':');
			response.push(sb.toString());
			response.push(message);
		}
	}

	/**
	 * Push a data message to the client. At the moment, there is no boundary
	 * handling, so be aware that you could get only half of a message.
	 * 
	 * @param sessionID the session id of the push response
	 * @param name the name of the push response
	 * @param message the message to push
	 */
	public static void push(String sessionID, String name, NSData message) {
		ERXKeepAliveResponse response = responseForSessionIDNamed(sessionID, name);
		if (response != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(message.length());
			sb.append(':');
			response.push(sb.toString());
			response.push(message.bytes());
		}
	}
}
