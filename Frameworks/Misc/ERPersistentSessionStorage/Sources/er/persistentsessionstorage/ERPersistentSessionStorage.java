package er.persistentsessionstorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver.WOSessionStore;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXFrameworkPrincipal;

public class ERPersistentSessionStorage extends ERXFrameworkPrincipal {

	public static final Class<?>[] REQUIRES = new Class[] {};

	protected static volatile ERPersistentSessionStorage sharedInstance;

	private static final Logger log = LoggerFactory.getLogger(ERPersistentSessionStorage.class);

	// Registers the class as the framework principal
	static {
		log.debug("Static Initializer for ERR2d2w");
		setUpFrameworkPrincipalClass(ERPersistentSessionStorage.class);
	}

	public static ERPersistentSessionStorage sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ERPersistentSessionStorage.class) {
				if (sharedInstance == null) {
					sharedInstance = sharedInstance(ERPersistentSessionStorage.class);
				}
			}
		}
		return sharedInstance;
	}

	@Override
	public void finishInitialization() {
		log.info("Initializing persistent session store.");
		WOSessionStore store = new ERPersistentSessionStore();
		
		//Create the persistent session store
		WOApplication.application().setSessionStore(store);
		
		//Set up notifications for newly created sessions
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
		NSSelector<Void> sel = new NSSelector<Void>("enableSessionDistribution", new Class[] {NSNotification.class});
		nc.addObserver(this, sel, WOSession.SessionDidCreateNotification, null);
	}
	
	/**
	 * Sets distribution enabled on new sessions
	 * @param n a session created notification
	 */
	public void enableSessionDistribution(NSNotification n) {
		WOSession session = (WOSession) n.object();
		session.setDistributionEnabled(true);
	}

}
