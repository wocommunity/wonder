package er.ajax;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.appserver.ERXResponse;
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

	private static NSMutableDictionary<String, KeepAliveResponse> responses = ERXMutableDictionary.synchronizedDictionary();

	/**
	 * Special response that keeps the connection alive and pushes the data to
	 * the client. It does this by opening a stream that has small buffer but
	 * huge length.
	 * 
	 * @author ak
	 */
	public static class KeepAliveResponse extends ERXResponse {

		/**
		 * Queue to push the items into.
		 */
		protected Queue<byte[]> queue = new ConcurrentLinkedQueue<byte[]>();

		/**
		 * Current data to write to client.
		 */
		protected byte current[] = null;

		/**
		 * Current index in
		 */
		protected int currentIndex = 0;

		public KeepAliveResponse() {
			setHeader("keep-alive", "connection");
			setContentStream(new InputStream() {
				public int read() throws IOException {
					synchronized (queue) {
						if (current != null && currentIndex >= current.length) {
							current = null;
							currentIndex = 0;
						}
						if (current == null) {
							try {
								if (log.isDebugEnabled()) {
									log.debug("waiting: " + queue.hashCode());
								}
								queue.wait();
								if (log.isDebugEnabled()) {
									log.debug("got data: " + queue.hashCode());
								}
							}
							catch (InterruptedException e) {
								return -1;
							}
							current = queue.poll();
						}
					}
					if (current == null) {
						return -1;
					}
					if (log.isDebugEnabled()) {
						log.debug("writing: " + currentIndex);
					}
					return current[currentIndex++];
				}

			}, 1, 2000000000); // this is 2 GB... should be enough.
		}

		/**
		 * Enqueues the data.
		 * 
		 * @param data
		 */
		public void push(byte data[]) {
			if (log.isDebugEnabled()) {
				log.debug("pushing: " + queue.hashCode());
			}
			synchronized (queue) {
				queue.offer(data);
				queue.notify();
			}
		}

		/**
		 * Resets the response by clearing out the current item and notifying
		 * the queue.
		 */
		public void reset() {
			synchronized (queue) {
				current = null;
				currentIndex = 0;
				queue.notify();
			}
		}
	}

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
		KeepAliveResponse response = responseForSessionID(id);
		response.reset();
		responses.removeObjectForKey(id);
	}

	/**
	 * Get/Create the current request for the session and return it.
	 */
	public WOResponse handleRequest(WORequest request) {
		String sessionID = request.sessionID();
		KeepAliveResponse response = responseForSessionID(sessionID);
		response.reset();
		return response;
	}

	/**
	 * Return or create the correct response for the session ID.
	 * 
	 * @param sessionID
	 * @return response for ID
	 */
	private static KeepAliveResponse responseForSessionID(String sessionID) {
		KeepAliveResponse response = null;
		if (sessionID != null) {
			response = responses.objectForKey(sessionID);
			if (response == null) {
				response = new KeepAliveResponse();
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
		responseForSessionID(sessionID).push(message.getBytes());
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
