package er.extensions;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver.WOStatisticsStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * Enhances the normal stats store with a bunch of usefull things which get
 * displayed in the ERXStatisticsPage.
 * 
 * @author ak
 * 
 */
public class ERXStatisticsStore extends WOStatisticsStore {
	
	protected NSMutableArray sessions = new NSMutableArray<WOSession>();
	
	protected void _applicationCreatedSession(WOSession wosession) {
		synchronized (this) {
			sessions.addObject(wosession);
			super._applicationCreatedSession(wosession);
		}
	}

	protected void _sessionTerminating(WOSession wosession) {
		synchronized (this) {
			super._sessionTerminating(wosession);
			sessions.removeObject(wosession);
		}
	}

	public NSArray activeSession() {
		return sessions;
	}
	
}
