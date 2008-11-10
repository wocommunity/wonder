package er.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;

public class ERXRestUtils {
	// MS: Yes, this is wrong, but I'll fix it later ...
	public static EOEntity getEntityNamed(String name) {
		EOEntity e = EOModelGroup.defaultGroup().entityNamed(name);
		if (e == null) {
			throw new RuntimeException("Could not find entity named '" + name + "'");
		}
		return e;
	}
}
