package er.extensions.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSData;

import er.extensions.foundation.ERXStringUtilities;

/**
 * Serializable Global ID class. You can use this as a custom attribute value type.
 * Note that a EOKeyGlobalID and an ERXKeyGlobalID are never equals! 
 * This especially means, that you shouldn't pass as ERXKeyGlobalID in eg. {@link com.webobjects.eocontrol.EOEditingContext#faultForGlobalID(com.webobjects.eocontrol.EOGlobalID, EOEditingContext)}.
 * Instead use the EOKeyGlobalID you get via {@link #globalID()}. 
 * @author ak
 *
 */
public class ERXKeyGlobalID extends EOKeyGlobalID {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private EOKeyGlobalID _gid;
	
	public ERXKeyGlobalID(String entityName, Object values[]) {
		super(entityName, 0);
		_gid = EOKeyGlobalID.globalIDWithEntityName(entityName, values);
	}

	public EOKeyGlobalID globalID() {
		return _gid;
	}
	
	public String asString() {
		return entityName() + "." + ERXEOControlUtilities.primaryKeyStringForGlobalID(this);
	}

	public NSData asData() {
		return new NSData(asString().getBytes());
	}

	public static ERXKeyGlobalID fromString(String value) {
		if(value == null) {
			return null;
		}
		String entityName = ERXStringUtilities.firstPropertyKeyInKeyPath(value);
		String valueString = ERXStringUtilities.keyPathWithoutFirstProperty(value);
		EOKeyGlobalID gid = (EOKeyGlobalID) ERXEOControlUtilities.globalIDForString(null, entityName, valueString);
		return globalIDForGID(gid);
	}

	public static ERXKeyGlobalID fromData(NSData value) {
		return fromString(new String(value.bytes()));
	}

	public static ERXKeyGlobalID globalIDForGID(EOKeyGlobalID gid) {
		return new ERXKeyGlobalID(gid.entityName(), gid.keyValues());
	}

	@Override
	public Object[] _keyValuesNoCopy() {
		return _gid._keyValuesNoCopy();
	}

	@Override
	public int keyCount() {
		return _gid.keyCount();
	}

	@Override
	public String toString() {
		return _gid.toString();
	}

	@Override
	public Object[] keyValues() {
		return _gid.keyValues();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ERXKeyGlobalID?((ERXKeyGlobalID)obj).globalID().equals(_gid):false;
	}
	
	@Override
	public int hashCode() {
		return _gid.hashCode();
	}
}
