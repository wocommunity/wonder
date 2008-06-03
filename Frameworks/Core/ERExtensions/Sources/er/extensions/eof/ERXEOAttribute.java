package er.extensions.eof;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXEOAttribute exists only to make the EOAttribute(EOEntity, String) 
 * constructor public.
 * 
 * @author mschrag
 */
public class ERXEOAttribute extends EOAttribute {
	public ERXEOAttribute() {
		super();
	}

	public ERXEOAttribute(NSDictionary plist, Object owner) {
		super(plist, owner);
	}

	public ERXEOAttribute(EOEntity entity, String definition) {
		super(entity, definition);
	}
}
