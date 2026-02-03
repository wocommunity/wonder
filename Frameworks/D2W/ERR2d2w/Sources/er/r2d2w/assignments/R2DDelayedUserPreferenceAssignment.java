package er.r2d2w.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSKeyValueCoding;

import er.directtoweb.assignments.delayed.ERDDelayedAssignment;
import er.extensions.ERXExtensions;

public class R2DDelayedUserPreferenceAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String UserPreferencesKey = "userPreferences";
	private static final String default_key = "default";

	public R2DDelayedUserPreferenceAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDelayedUserPreferenceAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDelayedUserPreferenceAssignment(unarchiver);
	}
	
	/**
	 * The rhsKey is the preference key. Default values are defined using a 
	 * similarly named default key in the D2WContext. The preference key 
	 * and the default key should be named using a xxx, defaultXxx naming 
	 * convention. Examples are batchSize, defaultBatchSize and sortOrdering, 
	 * defaultSortOrdering.
	 * @return the user preference value for the preference key or the default 
	 * value if no user preference value is found.
	 */
	@Override
	public Object fireNow(D2WContext c) {
		String rhsKey = keyPath();
		
		// Get the default value
		StringBuffer sb = new StringBuffer(rhsKey);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, default_key);
		Object result = c.valueForKey(sb.toString());

		// Get the preference for the RHS key
		String prefKey = ERXExtensions.userPreferencesKeyFromContext(rhsKey, c);
		NSKeyValueCoding userPreferences = (NSKeyValueCoding) c.valueForKey(UserPreferencesKey);
		if (userPreferences != null) {
			Object pref = userPreferences.valueForKey(prefKey);
			if(pref != null) { result = pref; }
		}

		// Return the preference
		return result;
	}

}
