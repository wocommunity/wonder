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
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private ObjectContext objectContext;

	public CayenneSession() {
	}

	public CayenneSession(String sessionID) {
		super(sessionID);
	}

	/**
	 * Returns the ObjectContext that is tied to the lifespan of the Session. Created on first access.
	 * @return a org.apache.cayenne.ObjectContext object
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
