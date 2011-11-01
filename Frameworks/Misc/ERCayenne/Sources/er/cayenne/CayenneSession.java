package er.cayenne;

import org.apache.cayenne.ObjectContext;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.appserver.ERXSession;

/**
 * Adds Cayenne support to WOSession
 * 
 * @author john
 *
 */
public class CayenneSession extends ERXSession {

	private ObjectContext objectContext;

	public CayenneSession() {
	}

	public CayenneSession(String sessionID) {
		super(sessionID);
	}

	/**
	 * Returns the ObjectContext that is tied to the lifespan of the Session. Created on first access.
	 * @return
	 */
	public ObjectContext defaultObjectContext() {
		if (objectContext == null) {
			objectContext = application().newObjectContext();
		}
		return objectContext;
	}
	
	@Override
	public CayenneApplication application() {
		return (CayenneApplication) super.application();
	}
	
	@Deprecated
	@Override
	public EOEditingContext defaultEditingContext() {
		return super.defaultEditingContext();
	}
	
}
