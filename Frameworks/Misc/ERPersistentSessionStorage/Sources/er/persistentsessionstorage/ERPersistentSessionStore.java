package er.persistentsessionstorage;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver.WOSessionStore;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;
import er.persistentsessionstorage.model.ERSessionInfo;

public class ERPersistentSessionStore extends WOSessionStore {
	private static final Logger log = Logger.getLogger(ERPersistentSessionStore.class);

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
		try {
			/*
			 * An error here can later hang the instance when the session is restored. 
			 * If the session fails to archive, delete it.
			 */
			info.archiveDataFromSession(session);
		} catch (Exception e) {
			log.error("Error archiving session! Deleting session.");
			ERXApplication app = ERXApplication.erxApplication();
			NSMutableDictionary extraInfo = app.extraInformationForExceptionInContext(e, context);
			app.reportException(e, context, extraInfo);
			/*
			 * If the session info is new, just don't save it.
			 * Otherwise, we need to delete the session.
			 */
			if(!info.isNewObject()) {
				removeSessionWithID(session.sessionID());
			}
			return;
		}
		ec.saveChanges();
	}
	
}
