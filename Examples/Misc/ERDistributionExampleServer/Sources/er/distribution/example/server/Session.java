package er.distribution.example.server;

import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;

import er.distribution.ERDistributionSession;
import er.extensions.eof.ERXEOGlobalIDUtilities;

public class Session extends ERDistributionSession {
	private static final long serialVersionUID = 1L;
	private EOKeyGlobalID userGID;

	public Session() {
	}
	
	@Override
	public EOGlobalID clientSideRequestLogin(String username, String password) {
		if ("john".equals(username) && "password".equals(password)) {
			// for this example, we'll just return something that is not null
			// in your you should authenticate the user and return the EOGlobalID for the User's record in the database.
			userGID = ERXEOGlobalIDUtilities.createGlobalID("Movie", new Object[] {1});
			return userGID;
		} else {
			return null;
		}
	}
	
	@Override
	public boolean isUserAuthenticated() {
		return userGID != null;
	}
	
}
