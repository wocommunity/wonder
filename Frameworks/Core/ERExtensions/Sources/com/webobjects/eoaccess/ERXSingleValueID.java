/**
 * 
 */
package com.webobjects.eoaccess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation._NSUtilities;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Gives up the means to plug into toString() and may or may not be a bit faster.
 *
 * @property er.extensions.ERXGlobalID.optimize
 *
 * @author ak
 */
public class ERXSingleValueID extends EOKeyGlobalID {

	public static Boolean optimize = false;
	
	static final long serialVersionUID = 657069456L;
	private static final String SerializationValuesFieldKey = "values";
	public static final Class _CLASS = _NSUtilities._classWithFullySpecifiedName("com.webobjects.eoaccess.ERXSingleValueID");
	private static final ObjectStreamField serialPersistentFields[] = { new ObjectStreamField("values", ((Object) (new Object[0])).getClass()) };
	
	protected Object _value;

	public ERXSingleValueID(String entityName, Object value) {
		super(entityName, _hashCode(entityName, value));
		_value = value != null ? value : NSKeyValueCoding.NullValue;
	}

	public Object[] keyValues() {
		return new Object[] {_value};
	}

	public Object[] _keyValuesNoCopy() {
		return keyValues();
	}

	public final int keyCount() {
		return 1;
	}

	public Object clone() {
		ERXSingleValueID result = new ERXSingleValueID(_literalEntityName(), _value);
		_prepClone(result);
		return result;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append('[');
		result.append(getClass().getSimpleName());
		result.append(':');
		result.append(entityName());
		result.append(' ');
		if(_value instanceof NSData) {
			result.append("decode('" + ERXStringUtilities.byteArrayToHexString(((NSData)_value)._bytesNoCopy()) + "', 'hex')");
		} else {
			result.append(_value);
		}

		result.append(']');
		return result.toString();
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ERXSingleValueID))
			return false;
		ERXSingleValueID other = (ERXSingleValueID) obj;
		Object thisValue = _value;
		Object otherValue = other._value;
		if (thisValue == otherValue || (hashCode() == other.hashCode() && thisValue.hashCode() == otherValue.hashCode())) {
			String entityName = _literalEntityName();
			String otherEntityName = other._literalEntityName();
			if(entityName == otherEntityName || entityName.equals(otherEntityName)) {
				return thisValue.equals(otherValue);
			}
		}
		
		return false;
	}

	private static int _hashCode(String entityName, Object value) {
		int hashCode = entityName.hashCode();
		if (value != null)
			hashCode ^= value.hashCode();

		return hashCode == 0 ? 42 : hashCode;
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		java.io.ObjectOutputStream.PutField fields = s.putFields();
		fields.put("values", ((Object) (_keyValuesNoCopy())));
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		java.io.ObjectInputStream.GetField fields = null;
		fields = s.readFields();
		Object[] values = (Object[]) (Object[]) fields.get("values", ((Object) (new Object[0])));
		_value = values[0];
	}

	public static EOKeyGlobalID singleGlobalIDWithEntityName(String entityName, Object primaryKey) {
		return new ERXSingleValueID(entityName, primaryKey);
	}

	public static EOKeyGlobalID globalIDWithEntityName(String entityName, Object values[]) {
		if(optimize == null) {
			optimize = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXGlobalID.optimize", false);
		}
		if (values != null && values.length == 1 && optimize) {
			Object primaryKey = values[0];
			return singleGlobalIDWithEntityName(entityName, primaryKey);
		}
		return _defaultGlobalIDWithEntityName(entityName, values);
	}
}
