package er.websocketexample;

import org.apache.log4j.Logger;

import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(Session.class);
	
	private static Integer davidCount = Integer.valueOf(0);
	
	private final String username;
	
	public Session() {
		log.debug("Session created: " + sessionID());
		
		setStoresIDsInCookies(true);
		setStoresIDsInURLs(false);
		_javaScriptEnabled = Boolean.FALSE;
		
		synchronized (davidCount) {
			username = "David" + ++davidCount;
		}
	}
	
	public String username() {
		return username;
	}
}
