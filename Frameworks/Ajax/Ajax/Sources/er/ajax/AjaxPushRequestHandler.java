package er.ajax;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXMutableDictionary;

public class AjaxPushRequestHandler extends WORequestHandler {

	public static final String AjaxCometRequestHandlerKey = "push";

	protected static Logger log = Logger.getLogger(AjaxPushRequestHandler.class);
	
	private static NSMutableDictionary<String, KeepAliveResponse> responses = ERXMutableDictionary.synchronizedDictionary(); 
	
	public static class KeepAliveResponse extends ERXResponse {
		
		protected Queue<String> queue =  new ConcurrentLinkedQueue<String>();
		protected String current = null;
		protected int currentIndex = 0;
		
		public KeepAliveResponse() {
			setHeader("keep-alive", "connection");
			setContentStream(new InputStream() {
				public int read() throws IOException {
					synchronized (queue) {
						if (current != null && currentIndex >= current.length()) {
							current = null;
							currentIndex = 0;
						}
						if (current == null) {
							try {
								log.info("waiting: " + queue.hashCode());
								queue.wait();
								log.info("got data: " + queue.hashCode());
							}
							catch (InterruptedException e) {
								log.error(e, e);
							}
							current = queue.poll();
						}
					}
					log.info("writing: " + currentIndex);
					return current.getBytes()[currentIndex++];
				}
				
			}, 1, 1000000000);
		}
		
		public void push(String string) {
			log.info("pushing: " + queue.hashCode());
			synchronized (queue) {
				queue.offer(string);
				queue.notify();
			}
		}
	}
	
	
	public WOResponse handleRequest(WORequest request) {
		String sessionID = request.sessionID();
		KeepAliveResponse response = responseForSessionID(sessionID);
		return response;
	}

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

	public static void push(String sessionID, String string) {
		responseForSessionID(sessionID).push(string);
	}
}
