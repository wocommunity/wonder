package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.assignments.ERDAssignment;

public class R2DUserPreferencesAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
    
    private static final NSArray<String> dependentKeys = new NSArray<String>("session.sessionID");
	
	public R2DUserPreferencesAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DUserPreferencesAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DUserPreferencesAssignment(unarchiver);
	}

	public NSArray<String> dependentKeys(String keyPath) {
		return dependentKeys;
	}

	public Object userPreferences(D2WContext c) {
		Object userPreferences = c.valueForKeyPath("session.objectStore.userPreferences");
		if(userPreferences == null) {
			userPreferences = new NSMutableDictionary<String, Object>();
			c.takeValueForKeyPath(userPreferences, "session.objectStore.userPreferences");
		}
		return userPreferences;
	}
		
}
