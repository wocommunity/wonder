package er.persistentsessionstorage;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver.WOSessionStore;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXEC;
import er.persistentsessionstorage.model.ERSessionInfo;

public class ERPersistentSessionStore extends WOSessionStore {

	@Override
	public WOSession removeSessionWithID(String s) {
		EOEditingContext ec = ERXEC.newEditingContext();
		ERSessionInfo info = ERSessionInfo.clazz.objectMatchingKeyAndValue(ec, ERSessionInfo.SESSION_ID_KEY, s);
		if(info != null) {
			WOSession session = info.session();
			info.delete();
			ec.saveChanges();
			return session;
		}
		return null;
	}

	@Override
	public WOSession restoreSessionWithID(String s, WORequest request) {
		EOEditingContext ec = ERXEC.newEditingContext();
		ERSessionInfo info = ERSessionInfo.clazz.objectMatchingKeyAndValue(ec, ERSessionInfo.SESSION_ID_KEY, s);
		return info == null || info.expirationDate().getTime() < System.currentTimeMillis()?null:info.session();
	}

	@Override
	public void saveSessionForContext(WOContext context) {
		WOSession session = context.session();
		EOEditingContext ec = ERXEC.newEditingContext();
		ERSessionInfo info = ERSessionInfo.clazz.objectMatchingKeyAndValue(ec, ERSessionInfo.SESSION_ID_KEY, session.sessionID());
		if(info == null) {
			info = ERSessionInfo.clazz.createAndInsertObject(ec);
			info.setSessionID(session.sessionID());
		}
		NSTimestamp expires = new NSTimestamp(System.currentTimeMillis() + session.timeOutMillis());
		info.setExpirationDate(expires);
		info.archiveDataFromSession(session);
		ec.saveChanges();
	}
	
}
